import { type NextRequest, NextResponse } from "next/server";

/**
 * バックエンドAPIへの汎用プロキシ
 *
 * すべての `/api/*` リクエスト（GET/POST/PUT/DELETE/PATCH）をバックエンドへ
 * 中継する。`next.config.ts` の rewrites ではダイナミックルートより先に評価され
 * 個別のルートハンドラー（multipart アップロード等）を握り潰してしまうため、
 * プロキシ処理をこのキャッチオールルートハンドラーに一本化している。
 *
 * - リクエストボディは一旦バッファリングしてから転送する（multipart/form-data も
 *   扱える）。過大なボディでメモリを消費しないよう、
 *   1. content-length ヘッダーが上限を超えるリクエストは即座に 413 を返す
 *   2. content-length を詐称・省略したリクエストに備え、ボディを読み取りながら
 *      累積バイト数を数え、上限を超えた時点で読み取りを打ち切って 413 を返す
 *   （このキャッチオールルートは proxy の matcher 対象外のため
 *   next.config.ts の experimental.proxyClientMaxBodySize は適用されない。
 *   メモリ上限の担保はこのハンドラー自身で行う）
 * - バックエンドへの中継には 30 秒のタイムアウトを設け、応答が無い場合は 504 を返す
 * - Cookie（refreshToken 等）とバックエンドの Set-Cookie を双方向に転送する
 * - クライアントが詐称しうる転送系ヘッダー（X-Forwarded-* 等）は除去する。
 *   これはあくまで詐称防止であり、バックエンドは現状クライアント IP に依存した
 *   判定（ロックはアカウント単位）を行っていない。IP ベースのレート制限等を
 *   導入する場合は、信頼できる送信元 IP を別途載せ直す必要がある。
 */

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

/** 転送を許可するリクエストボディの最大サイズ（サーブレット上限に合わせて 6MB） */
const MAX_BODY_SIZE = 6 * 1024 * 1024;

/** バックエンドへの中継リクエストのタイムアウト（ミリ秒） */
const BACKEND_TIMEOUT_MS = 30_000;

/** バックエンドへ転送しないリクエストヘッダー */
const EXCLUDED_REQUEST_HEADERS = new Set([
  // fetch が再設定するもの
  "host",
  "connection",
  "content-length",
  "transfer-encoding",
  "accept-encoding",
  // クライアントによる詐称を防ぐため除去する転送系ヘッダー
  // （バックエンドが IP ベースでレート制限・監査ログ・ロック判定を行う場合の対策）
  "x-forwarded-for",
  "x-forwarded-host",
  "x-forwarded-proto",
  "x-forwarded-port",
  "x-real-ip",
  "forwarded",
]);

/** クライアントへ返さないレスポンスヘッダー */
const EXCLUDED_RESPONSE_HEADERS = new Set([
  "content-encoding",
  "content-length",
  "transfer-encoding",
  "connection",
]);

/**
 * リクエストボディを上限付きで読み取る
 *
 * content-length を信頼せず、実際に届いたバイト数を数えながらバッファリングする。
 * 上限を超えた時点でストリームの読み取りを打ち切るため、過大なボディを送られても
 * メモリ使用量は「上限 + 最後に読んだチャンク1個分」に収まる。
 *
 * @param request 受信したリクエスト
 * @param limit   許容する最大バイト数
 * @returns 読み取ったボディ。上限を超えた場合は null
 */
async function readBodyWithLimit(
  request: NextRequest,
  limit: number
): Promise<ArrayBuffer | null> {
  if (!request.body) return new ArrayBuffer(0);

  const reader = request.body.getReader();
  const chunks: Uint8Array[] = [];
  let total = 0;

  try {
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      if (!value) continue;
      total += value.byteLength;
      if (total > limit) {
        await reader.cancel();
        return null;
      }
      chunks.push(value);
    }
  } finally {
    reader.releaseLock();
  }

  const merged = new Uint8Array(total);
  let offset = 0;
  for (const chunk of chunks) {
    merged.set(chunk, offset);
    offset += chunk.byteLength;
  }
  return merged.buffer;
}

/**
 * リクエストをバックエンドへ中継する
 *
 * @param request 受信したリクエスト
 * @param path    `/api/` 以降のパストークン
 * @returns バックエンドのレスポンスを引き継いだレスポンス
 */
async function proxy(request: NextRequest, path: string[]): Promise<NextResponse> {
  // パストラバーサル対策：ドットセグメント・空セグメントは拒否する
  // （encodeURIComponent は "." や ".." をエスケープしないため、
  //   バックエンドの URL 解決で `/api` プレフィックス外へ抜けるのを防ぐ）
  if (path.some((seg) => seg === "" || seg === "." || seg === "..")) {
    return NextResponse.json(
      { message: "不正なリクエストパスです" },
      { status: 400 }
    );
  }

  // CSRF 対策：状態を変更するメソッド（POST/PUT/DELETE/PATCH）は、Origin
  // ヘッダーが存在する場合に自サイトと一致することを要求する。クロスサイトの
  // <form>/fetch からクッキーだけで実行される攻撃（強制ログアウト等）を塞ぐ。
  // Origin を送出しないクライアント（サーバー間通信・一部ツール）は素通しする。
  const isStateChanging =
    request.method !== "GET" && request.method !== "HEAD";
  if (isStateChanging) {
    // 多層防御：ブラウザが付与する Fetch Metadata で cross-site を明示的に拒否する
    // （Origin 検証と独立して機能し、Origin を欠く一部のクロスサイト経路も塞ぐ）
    const fetchSite = request.headers.get("sec-fetch-site");
    if (fetchSite === "cross-site") {
      return NextResponse.json(
        { message: "リクエスト元が不正です" },
        { status: 403 }
      );
    }

    const origin = request.headers.get("origin");
    if (origin) {
      let originHost: string | null = null;
      try {
        originHost = new URL(origin).host;
      } catch {
        originHost = null;
      }
      // 自ホスト（リクエスト URL 由来 / Host ヘッダー）のいずれかと一致すれば許可
      const selfHosts = [
        request.nextUrl.host,
        request.headers.get("host"),
      ].filter((h): h is string => !!h);
      if (!originHost || !selfHosts.includes(originHost)) {
        return NextResponse.json(
          { message: "リクエスト元が不正です" },
          { status: 403 }
        );
      }
    }
  }

  const search = request.nextUrl.search;
  const url = `${BACKEND_URL}/api/${path.map(encodeURIComponent).join("/")}${search}`;

  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (!EXCLUDED_REQUEST_HEADERS.has(key.toLowerCase())) {
      headers.set(key, value);
    }
  });

  const hasBody = request.method !== "GET" && request.method !== "HEAD";

  let body: ArrayBuffer | undefined;

  if (hasBody) {
    const contentLength = Number(request.headers.get("content-length"));
    if (Number.isFinite(contentLength) && contentLength > MAX_BODY_SIZE) {
      return NextResponse.json(
        { message: "リクエストサイズが大きすぎます" },
        { status: 413 }
      );
    }

    const buffered = await readBodyWithLimit(request, MAX_BODY_SIZE);
    if (buffered === null) {
      return NextResponse.json(
        { message: "リクエストサイズが大きすぎます" },
        { status: 413 }
      );
    }
    body = buffered;
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(url, {
      method: request.method,
      headers,
      body,
      redirect: "manual",
      // スロー応答・ハングでコネクションが滞留しないようタイムアウトを設ける
      signal: AbortSignal.timeout(BACKEND_TIMEOUT_MS),
    });
  } catch (err) {
    if (err instanceof DOMException && err.name === "TimeoutError") {
      return NextResponse.json(
        { message: "バックエンドの応答がありませんでした" },
        { status: 504 }
      );
    }
    return NextResponse.json(
      { message: "バックエンドとの通信に失敗しました" },
      { status: 502 }
    );
  }

  const responseHeaders = new Headers();
  backendResponse.headers.forEach((value, key) => {
    if (!EXCLUDED_RESPONSE_HEADERS.has(key.toLowerCase())) {
      responseHeaders.set(key, value);
    }
  });

  // Location ヘッダーの正規化
  // - バックエンドの絶対URLを指す場合は内部ホストを露出させないよう相対パス化する
  // - 自オリジン内の相対パスはそのまま通す
  // - それ以外（外部の絶対URL・プロトコル相対）は、バックエンド応答を起点とした
  //   反射型オープンリダイレクトを防ぐため削除する
  const location = responseHeaders.get("location");
  if (location) {
    // `BACKEND_URL` の「オリジン境界」で一致した場合のみ相対パス化する。
    // 単純な startsWith だと `https://backend.example.com.evil.com/…` のような
    // ホスト詐称も一致してしまうため、直後が `/` であること（またはURL全体が
    // `BACKEND_URL` そのもの）を要求する。
    const isBackendAbsolute =
      location === BACKEND_URL || location.startsWith(`${BACKEND_URL}/`);
    if (isBackendAbsolute) {
      const relative = location.slice(BACKEND_URL.length) || "/";
      // slice 結果が `//host` / `/\host`（プロトコル相対）になっていないか再確認する。
      // 例: バックエンドが `${BACKEND_URL}//evil.com/x` を返したケース。
      if (
        relative.startsWith("/") &&
        !relative.startsWith("//") &&
        !relative.startsWith("/\\")
      ) {
        responseHeaders.set("location", relative);
      } else {
        responseHeaders.delete("location");
      }
    } else if (
      !location.startsWith("/") ||
      location.startsWith("//") ||
      location.startsWith("/\\")
    ) {
      responseHeaders.delete("location");
    }
  }

  // Set-Cookie は Headers#forEach で結合されてしまうため個別に転送する
  const setCookie = backendResponse.headers.getSetCookie?.() ?? [];
  responseHeaders.delete("set-cookie");
  for (const cookie of setCookie) {
    responseHeaders.append("set-cookie", cookie);
  }

  return new NextResponse(backendResponse.body, {
    status: backendResponse.status,
    statusText: backendResponse.statusText,
    headers: responseHeaders,
  });
}

type Context = { params: Promise<{ path: string[] }> };

/** GETリクエストをプロキシする */
export async function GET(request: NextRequest, context: Context) {
  return proxy(request, (await context.params).path);
}

/** POSTリクエストをプロキシする */
export async function POST(request: NextRequest, context: Context) {
  return proxy(request, (await context.params).path);
}

/** PUTリクエストをプロキシする */
export async function PUT(request: NextRequest, context: Context) {
  return proxy(request, (await context.params).path);
}

/** DELETEリクエストをプロキシする */
export async function DELETE(request: NextRequest, context: Context) {
  return proxy(request, (await context.params).path);
}

/** PATCHリクエストをプロキシする */
export async function PATCH(request: NextRequest, context: Context) {
  return proxy(request, (await context.params).path);
}
