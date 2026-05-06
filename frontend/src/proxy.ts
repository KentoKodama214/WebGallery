import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/**
 * 認証状態に基づくルーティング制御
 * - ルート（/）へのアクセスは/loginへリダイレクト
 * - ログインページ: cookieが存在すれば写真一覧へリダイレクト
 * - 保護ルートへのアクセス時、リフレッシュトークンcookieが無ければ/loginへリダイレクト
 */
export function proxy(request: NextRequest) {
  const refreshToken = request.cookies.get("refreshToken");
  const { pathname } = request.nextUrl;

  // ルートページはログインへリダイレクト
  if (pathname === "/") {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  // ログインページにリフレッシュトークンがある場合（認証済み）
  // → 実際のリダイレクト先はクライアント側のAuthProviderで処理するためそのまま通す
  // （アカウントIDがcookieに含まれないため、サーバー側では写真一覧URLを構築できない）

  // 保護ルートへのアクセス時にリフレッシュトークンが無い場合はログインへリダイレクト
  if (!refreshToken && isProtectedRoute(pathname)) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  return NextResponse.next();
}

function isProtectedRoute(_pathname: string): boolean {
  return false;
}

export const config = {
  matcher: [
    // 静的ファイルとAPIを除外
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\.png$|.*\\.jpg$|.*\\.svg$).*)",
  ],
};
