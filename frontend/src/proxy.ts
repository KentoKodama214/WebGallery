import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/**
 * ルーティング制御 ＋ Content-Security-Policy（nonce 方式）の付与
 *
 * - ルート（/）へのアクセスは /login へリダイレクトする
 * - すべてのページレスポンスに、リクエストごとに生成した nonce を含む CSP を付与する
 *
 * ログイン必須ページのガードはクライアント側（`<AuthGuard>` および各画面コンポーネント）
 * で行う。リフレッシュトークン cookie は `Path=/api/v1/auth` で発行されており、
 * ページルートのリクエストには送信されないため、ここでは認証判定できない。
 *
 * ## CSP を proxy で組み立てる理由
 * `script-src` から `'unsafe-inline'` を外すには、Next.js のハイドレーション用
 * インラインスクリプトを nonce で個別許可する必要がある。nonce はリクエストごとに
 * 変わるため静的な `next.config.ts` の `headers()` では表現できず、proxy で
 * 生成して「リクエストヘッダー（Next.js が SSR 時に読み取る）」と
 * 「レスポンスヘッダー（ブラウザが適用する）」の双方へ設定する。
 * この方式は全ページの動的レンダリングを要求する（`app/layout.tsx` の
 * `export const dynamic = "force-dynamic"` で担保）。
 *
 * `style-src` の扱い（多層防御）：
 * - `style-src`（フォールバック）は `'unsafe-inline'` を維持する。本アプリは要素の
 *   `style` 属性（React の `style={{...}}`）を多用しており、これは nonce では許可できないため。
 * - 本番では加えて `style-src-elem 'self' 'nonce-...'` を指定し、`<style>`／`<link>` 要素は
 *   nonce（Next.js が自身の生成タグへ自動付与）または自オリジンのみに限定する。これにより
 *   XSS で注入された `<style>`（CSS による情報窃取・UI 偽装）を対応ブラウザでブロックする。
 *   `style-src-elem` 非対応の古いブラウザは `style-src` にフォールバックし、従来どおり動作する。
 * - インライン `style` 属性は引き続き許可される（`style-src-attr` は指定せず `style-src` に委ねる）。
 *   属性の完全排除には全コンポーネントの `style={{}}` 撤去が必要なため、段階対応とする。
 */

const isDev = process.env.NODE_ENV === "development";

/**
 * 写真の配信元オリジン（`NEXT_PUBLIC_IMAGE_BASE_URL` 例: `https://cdn.example.com/`）。
 * 設定されていれば CSP の `img-src` をそのオリジンに限定する。
 * 未設定の場合は外部ホストを一切許可しない（`'self' data: blob:` のみ）。
 */
const imageBaseOrigin = (() => {
  const base = process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
  if (!base) return null;
  try {
    return new URL(base).origin;
  } catch {
    return null;
  }
})();

/**
 * バックエンド API のオリジン（`NEXT_PUBLIC_API_BASE_URL` 例: `https://api.example.com`）。
 * 別オリジンを指定した場合、`connect-src 'self'` のままだと API 通信がブロックされるため、
 * そのオリジンを `connect-src` に追加する。未設定（同一オリジンの `/api` プロキシ経由）なら不要。
 */
const apiBaseOrigin = (() => {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL;
  if (!base) return null;
  try {
    const origin = new URL(base).origin;
    return origin === "null" ? null : origin;
  } catch {
    return null;
  }
})();

/**
 * リクエスト単位の nonce を含む CSP ヘッダー値を組み立てる
 *
 * @param nonce このリクエスト用に生成した base64 nonce
 * @returns CSP ヘッダー値
 */
function buildCsp(nonce: string): string {
  return [
    "default-src 'self'",
    // ハイドレーション用インラインスクリプトは nonce で個別許可し、そこから読み込まれる
    // スクリプトは 'strict-dynamic' で許可する。開発時は React の eval を許可する
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'${isDev ? " 'unsafe-eval'" : ""}`,
    // style 属性（React の style={{...}}）を多用するため 'unsafe-inline' を維持する（フォールバック）
    "style-src 'self' 'unsafe-inline'",
    // 本番のみ：<style>/<link> 要素は nonce または自オリジンに限定し、注入された <style> を防ぐ
    ...(isDev ? [] : [`style-src-elem 'self' 'nonce-${nonce}'`]),
    `img-src 'self' data: blob:${imageBaseOrigin ? ` ${imageBaseOrigin}` : ""}`,
    "font-src 'self' data:",
    `connect-src 'self'${apiBaseOrigin ? ` ${apiBaseOrigin}` : ""}${isDev ? " ws:" : ""}`,
    "worker-src 'self' blob:",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
  ].join("; ").concat(";");
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // リクエストごとに予測不能な nonce を生成する
  const nonce = Buffer.from(crypto.randomUUID()).toString("base64");
  const csp = buildCsp(nonce);

  // Next.js が SSR 時に nonce を各 <script> へ適用できるよう、
  // nonce と CSP をリクエストヘッダーにも載せる
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);
  requestHeaders.set("Content-Security-Policy", csp);

  // ルートページはログインへリダイレクト
  if (pathname === "/") {
    const redirect = NextResponse.redirect(new URL("/login", request.url));
    redirect.headers.set("Content-Security-Policy", csp);
    return redirect;
  }

  const response = NextResponse.next({ request: { headers: requestHeaders } });
  response.headers.set("Content-Security-Policy", csp);
  return response;
}

export const config = {
  matcher: [
    // API・Next内部アセット・静的ファイルを除外し、ページ遷移だけを対象にする
    "/((?!api|_next/static|_next/image|_next/data|favicon.ico|.*\\.(?:png|jpe?g|gif|svg|webp|avif|ico|css|js|map|woff2?|ttf)$).*)",
  ],
};
