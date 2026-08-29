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
 *   扱える）。過大なボディでメモリを消費しないよう、content-length が上限を
 *   超えるリクエストは転送せず 413 を返す（併せて next.config.ts の
 *   experimental.proxyClientMaxBodySize でもバッファ上限を設定している）
 * - Cookie（refreshToken 等）とバックエンドの Set-Cookie を双方向に転送する
 * - クライアントが詐称しうる転送系ヘッダー（X-Forwarded-* 等）は除去する。
 *   これはあくまで詐称防止であり、バックエンドは現状クライアント IP に依存した
 *   判定（ロックはアカウント単位）を行っていない。IP ベースのレート制限等を
 *   導入する場合は、信頼できる送信元 IP を別途載せ直す必要がある。
 */

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

/** 転送を許可するリクエストボディの最大サイズ（サーブレット上限に合わせて 6MB） */
const MAX_BODY_SIZE = 6 * 1024 * 1024;

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
 * リクエストをバックエンドへ中継する
 *
 * @param request 受信したリクエスト
 * @param path    `/api/` 以降のパストークン
 * @returns バックエンドのレスポンスを引き継いだレスポンス
 */
async function proxy(request: NextRequest, path: string[]): Promise<NextResponse> {
  const search = request.nextUrl.search;
  const url = `${BACKEND_URL}/api/${path.map(encodeURIComponent).join("/")}${search}`;

  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (!EXCLUDED_REQUEST_HEADERS.has(key.toLowerCase())) {
      headers.set(key, value);
    }
  });

  const hasBody = request.method !== "GET" && request.method !== "HEAD";

  if (hasBody) {
    const contentLength = Number(request.headers.get("content-length"));
    if (Number.isFinite(contentLength) && contentLength > MAX_BODY_SIZE) {
      return NextResponse.json(
        { message: "リクエストサイズが大きすぎます" },
        { status: 413 }
      );
    }
  }

  const body = hasBody ? await request.arrayBuffer() : undefined;

  if (body !== undefined && body.byteLength > MAX_BODY_SIZE) {
    return NextResponse.json(
      { message: "リクエストサイズが大きすぎます" },
      { status: 413 }
    );
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(url, {
      method: request.method,
      headers,
      body,
      redirect: "manual",
    });
  } catch {
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

  // バックエンドの絶対URLを指す Location は内部ホストを露出させるため相対パス化する
  const location = responseHeaders.get("location");
  if (location && location.startsWith(BACKEND_URL)) {
    responseHeaders.set("location", location.slice(BACKEND_URL.length) || "/");
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
