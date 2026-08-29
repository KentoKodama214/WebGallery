import { type NextRequest, NextResponse } from "next/server";

/**
 * バックエンドAPIへの汎用プロキシ
 *
 * すべての `/api/*` リクエスト（GET/POST/PUT/DELETE/PATCH）をバックエンドへ
 * 中継する。`next.config.ts` の rewrites ではダイナミックルートより先に評価され
 * 個別のルートハンドラー（multipart アップロード等）を握り潰してしまうため、
 * プロキシ処理をこのキャッチオールルートハンドラーに一本化している。
 *
 * - リクエストボディはそのままストリーム転送するため multipart/form-data も扱える
 * - Cookie（refreshToken 等）とバックエンドの Set-Cookie を双方向に転送する
 */

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

/** バックエンドへ転送しないリクエストヘッダー（fetch が再設定するもの） */
const EXCLUDED_REQUEST_HEADERS = new Set([
  "host",
  "connection",
  "content-length",
  "transfer-encoding",
  "accept-encoding",
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
  const body = hasBody ? await request.arrayBuffer() : undefined;

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
