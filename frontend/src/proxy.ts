import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/**
 * ルーティング制御
 * - ルート（/）へのアクセスは /login へリダイレクトする
 *
 * ログイン必須ページのガードはクライアント側（各画面コンポーネント）で行う。
 * リフレッシュトークンcookieは `Path=/api/v1/auth` で発行されており、
 * ページルートのリクエストには送信されないため、ミドルウェアでは判定できない。
 */
export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // ルートページはログインへリダイレクト
  if (pathname === "/") {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    // 静的ファイルとAPIを除外
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\.png$|.*\\.jpg$|.*\\.svg$).*)",
  ],
};
