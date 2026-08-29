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
    // API・Next内部アセット・静的ファイルを除外し、ページ遷移だけを対象にする
    "/((?!api|_next/static|_next/image|_next/data|favicon.ico|.*\\.(?:png|jpe?g|gif|svg|webp|avif|ico|css|js|map|woff2?|ttf)$).*)",
  ],
};
