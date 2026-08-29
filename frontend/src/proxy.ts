import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/**
 * 認証状態に基づくルーティング制御
 * - ルート（/）へのアクセスは/loginへリダイレクト
 * - 保護ルートへのアクセス時、リフレッシュトークンcookieが無ければ/loginへリダイレクト
 *
 * ログイン済みかどうかの最終判定はバックエンドのJWT検証で行う。
 * ここでのcookieチェックは未ログインユーザーに保護ページの骨組みを
 * 表示させないための第一段のガードに過ぎない。
 */
export function proxy(request: NextRequest) {
  const refreshToken = request.cookies.get("refreshToken");
  const { pathname } = request.nextUrl;

  // ルートページはログインへリダイレクト
  if (pathname === "/") {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  // 保護ルートへのアクセス時にリフレッシュトークンが無い場合はログインへリダイレクト
  if (!refreshToken && isProtectedRoute(pathname)) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

/** ログインが必須のルートのパターン */
const PROTECTED_ROUTE_PATTERNS: RegExp[] = [
  // 管理者用ページ
  /^\/admin(\/|$)/,
  // アカウント設定（/{accountId}/account_setting）
  /^\/[^/]+\/account_setting$/,
  // 写真登録・編集（/photo/{photoAccountId}/photo_setting）
  /^\/photo\/[^/]+\/photo_setting$/,
];

/**
 * 指定されたパスがログイン必須の保護ルートかどうかを判定する
 *
 * @param pathname 判定対象のパス
 * @returns 保護ルートの場合true
 */
function isProtectedRoute(pathname: string): boolean {
  return PROTECTED_ROUTE_PATTERNS.some((pattern) => pattern.test(pathname));
}

export const config = {
  matcher: [
    // 静的ファイルとAPIを除外
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\.png$|.*\\.jpg$|.*\\.svg$).*)",
  ],
};
