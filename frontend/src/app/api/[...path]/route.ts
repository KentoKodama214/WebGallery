import { type NextRequest, NextResponse } from "next/server";

/**
 * バックエンドAPIへの汎用プロキシ
 *
 * すべての `/api/*` リクエスト（GET/POST/PUT/DELETE/PATCH）をバックエンドへ
 * 中継する。`next.config.ts` の rewrites ではダイナミックルートより先に評価され
 * 個別のルートハンドラー（multipart アップロード等）を握り潰してしまうため、
 * プロキシ処理をこのキャッチオールルートハンドラーに一本化している。
 *
 * - リクエストボディはバッファせず、ReadableStream のままバックエンドへ中継する
 *   （multipart/form-data も扱える）。過大なボディでメモリを消費しないよう、
 *   1. content-length ヘッダーが上限を超えるリクエストは即座に 413 を返す
 *   2. content-length を詐称・省略したリクエストに備え、中継ストリームで累積バイト数を
 *      数え、上限を超えた時点でストリームをエラーにして 413 を返す
 *   （このキャッチオールルートは proxy の matcher 対象外のため
 *   next.config.ts の experimental.proxyClientMaxBodySize は適用されない。
 *   メモリ上限の担保はこのハンドラー自身で行う）
 * - バックエンドへの中継には 30 秒のタイムアウトを設け、応答が無い場合は 504 を返す
 * - Cookie（refreshToken 等）とバックエンドの Set-Cookie を双方向に転送する
 * - クライアントが詐称しうる転送系ヘッダー（X-Forwarded-* / Forwarded / X-Real-IP）は一旦除去し、
 *   バックエンドのレート制限が使う X-Forwarded-For だけを、前段プロキシ（ALB / CloudFront）が
 *   付与した値の左端＝実クライアント IP に載せ直してから中継する。このアプリは信頼できる L7
 *   プロキシ経由でのみ到達可能な構成を前提とする（バックエンドの `TRUSTED_PROXIES` と対で機能）。
 */

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

/** 転送を許可するリクエストボディの最大サイズ（サーブレット上限に合わせて 6MB） */
const MAX_BODY_SIZE = 6 * 1024 * 1024;

/** バックエンドへの中継リクエストのタイムアウト（ミリ秒） */
const BACKEND_TIMEOUT_MS = 30_000;

/**
 * バックエンドへ同時に中継するリクエスト数の上限
 *
 * このキャッチオールルートは同時実行数の上限を持たないため、大量同時リクエスト
 * （特に最大 6MB をバッファしうるアップロード）で Node プロセスのメモリ・接続が
 * 枯渇しうる。上限を超えた分は待たせずに 503 で突き放し（ロードシェディング）、
 * 前段の WAF / ロードバランサ側のレート制限と併せて多層で守る。
 * 環境変数 `PROXY_MAX_CONCURRENCY` で上書き可能。
 */
const MAX_CONCURRENT_REQUESTS = Number(process.env.PROXY_MAX_CONCURRENCY) || 100;

/** 現在バックエンドへ中継中のリクエスト数 */
let inFlightRequests = 0;

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
 * 実クライアント IP を求める
 *
 * 前段プロキシ（ALB / CloudFront）が付与した `X-Forwarded-For` の左端を実クライアント IP とみなす。
 * このアプリは信頼できる L7 プロキシ経由でのみ到達可能な構成を前提とし、バックエンドは
 * この載せ直した `X-Forwarded-For` を `TRUSTED_PROXIES` の範囲でのみ信頼する。
 *
 * @param request 受信したリクエスト
 * @returns 実クライアント IP。特定できなければ null
 */
function resolveClientIp(request: NextRequest): string | null {
  const forwardedFor = request.headers.get("x-forwarded-for");
  const first = forwardedFor?.split(",")[0]?.trim();
  return first ? first : null;
}

/** リクエストボディが上限を超えたことを表すエラー */
class BodyTooLargeError extends Error {
  constructor() {
    super("request body exceeds the size limit");
    this.name = "BodyTooLargeError";
  }
}

/**
 * 発生した例外（および `fetch` がラップした `cause`）が {@link BodyTooLargeError} かを判定する
 */
function isBodyTooLargeError(err: unknown): boolean {
  return (
    err instanceof BodyTooLargeError ||
    (err instanceof Error && err.cause instanceof BodyTooLargeError)
  );
}

/**
 * リクエストボディを、上限バイト数を超えたらエラーにする ReadableStream でラップする
 *
 * content-length を信頼せず、実際に届いたバイト数を数えながら**バッファせずに**
 * バックエンドへ中継する。上限を超えた時点でストリームをエラーにして読み取り元も
 * キャンセルするため、メモリ使用量は「最後に読んだチャンク1個分」に収まる。
 * ストリームがエラーになると中継中の `fetch` が reject するため、呼び出し側で
 * {@link isBodyTooLargeError} を見て 413 を返す。
 *
 * @param source 受信したリクエストボディ
 * @param limit  許容する最大バイト数
 * @returns 上限付きの ReadableStream
 */
function limitedBodyStream(
  source: ReadableStream<Uint8Array>,
  limit: number
): ReadableStream<Uint8Array> {
  const reader = source.getReader();
  let total = 0;
  return new ReadableStream<Uint8Array>({
    async pull(controller) {
      const { done, value } = await reader.read();
      if (done) {
        controller.close();
        return;
      }
      total += value.byteLength;
      if (total > limit) {
        controller.error(new BodyTooLargeError());
        await reader.cancel().catch(() => {});
        return;
      }
      controller.enqueue(value);
    },
    cancel(reason) {
      return reader.cancel(reason);
    },
  });
}

/**
 * リクエストをバックエンドへ中継する（同時実行数の上限を適用するエントリポイント）
 *
 * 中継中のリクエスト数が {@link MAX_CONCURRENT_REQUESTS} に達している場合は、
 * バックエンドへ中継せず即座に 503 を返す（ロードシェディング）。
 *
 * @param request 受信したリクエスト
 * @param path    `/api/` 以降のパストークン
 * @returns バックエンドのレスポンスを引き継いだレスポンス
 */
async function proxy(request: NextRequest, path: string[]): Promise<NextResponse> {
  if (inFlightRequests >= MAX_CONCURRENT_REQUESTS) {
    return NextResponse.json(
      { message: "アクセスが集中しています。しばらくしてから再度お試しください" },
      { status: 503, headers: { "retry-after": "5" } }
    );
  }
  inFlightRequests++;
  try {
    return await forwardToBackend(request, path);
  } finally {
    inFlightRequests--;
  }
}

/**
 * リクエストをバックエンドへ中継する
 *
 * @param request 受信したリクエスト
 * @param path    `/api/` 以降のパストークン
 * @returns バックエンドのレスポンスを引き継いだレスポンス
 */
async function forwardToBackend(
  request: NextRequest,
  path: string[]
): Promise<NextResponse> {
  // パストラバーサル対策：ドットセグメント・空セグメントは拒否する
  // （encodeURIComponent は "." や ".." をエスケープしないため、
  //   バックエンドの URL 解決で `/api` プレフィックス外へ抜けるのを防ぐ）
  if (path.some((seg) => seg === "" || seg === "." || seg === "..")) {
    return NextResponse.json(
      { message: "不正なリクエストパスです" },
      { status: 400 }
    );
  }

  // CSRF 対策：状態を変更するメソッド（POST/PUT/DELETE/PATCH）は、リクエスト元が
  // 自サイトであることを Origin もしくは Referer で必ず検証する。どちらのヘッダーも
  // 無い場合は検証不能として拒否する（クロスサイトの <form>/fetch からクッキーだけで
  // 実行される攻撃 ―― 強制ログアウト等 ―― を塞ぐ）。
  // このルートハンドラーはブラウザからのみ呼ばれるため、Origin/Referer を欠く
  // 正当なクライアントは存在しない。
  const isStateChanging =
    request.method !== "GET" && request.method !== "HEAD";
  if (isStateChanging) {
    // 多層防御：ブラウザが付与する Fetch Metadata で cross-site を明示的に拒否する
    const fetchSite = request.headers.get("sec-fetch-site");
    if (fetchSite === "cross-site") {
      return NextResponse.json(
        { message: "リクエスト元が不正です" },
        { status: 403 }
      );
    }

    // 自ホスト（リクエスト URL 由来 / Host ヘッダー）
    const selfHosts = [
      request.nextUrl.host,
      request.headers.get("host"),
    ].filter((h): h is string => !!h);

    /** ヘッダー値（絶対 URL）のホストが自ホストと一致するか。値が無い場合は null */
    const hostMatches = (value: string | null): boolean | null => {
      if (!value) return null;
      try {
        return selfHosts.includes(new URL(value).host);
      } catch {
        return false;
      }
    };

    const originResult = hostMatches(request.headers.get("origin"));
    const refererResult =
      originResult === null ? hostMatches(request.headers.get("referer")) : null;

    // Origin と Referer のどちらも無い（両方 null）＝検証不能、または一致しない ⇒ 拒否
    const verified = originResult === true || refererResult === true;
    if (!verified) {
      return NextResponse.json(
        { message: "リクエスト元が不正です" },
        { status: 403 }
      );
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

  // バックエンドのレート制限が参照する X-Forwarded-For を、前段プロキシが付与した
  // 実クライアント IP に載せ直す（クライアント詐称値は EXCLUDED_REQUEST_HEADERS で除去済み）
  const clientIp = resolveClientIp(request);
  if (clientIp) {
    headers.set("x-forwarded-for", clientIp);
  }

  const hasBody = request.method !== "GET" && request.method !== "HEAD";

  let body: ReadableStream<Uint8Array> | undefined;

  if (hasBody) {
    const contentLength = Number(request.headers.get("content-length"));
    if (Number.isFinite(contentLength) && contentLength > MAX_BODY_SIZE) {
      return NextResponse.json(
        { message: "リクエストサイズが大きすぎます" },
        { status: 413 }
      );
    }

    if (request.body) {
      body = limitedBodyStream(request.body, MAX_BODY_SIZE);
    }
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(url, {
      method: request.method,
      headers,
      body,
      // ストリームボディの送信には duplex 指定が必須（Node/undici）
      ...(body ? { duplex: "half" } : {}),
      redirect: "manual",
      // スロー応答・ハングでコネクションが滞留しないようタイムアウトを設ける
      signal: AbortSignal.timeout(BACKEND_TIMEOUT_MS),
    } as RequestInit & { duplex?: "half" });
  } catch (err) {
    // 中継ストリームが上限超過でエラーになった場合は 413 を返す
    if (isBodyTooLargeError(err)) {
      return NextResponse.json(
        { message: "リクエストサイズが大きすぎます" },
        { status: 413 }
      );
    }
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
