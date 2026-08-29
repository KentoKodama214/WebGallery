/**
 * URL の安全性検証ユーティリティ
 */

/**
 * 現在のパスを `redirect` クエリに載せたログインページの URL を返す
 *
 * 認証ガードで `/login` へ退避させる際に利用し、再ログイン後に元の画面へ
 * 戻れるようにする（`/login` 側は {@link safeRedirectPath} で検証してから使う）。
 *
 * @returns `/login` もしくは `/login?redirect=...`
 */
export function loginUrlWithRedirect(): string {
  if (typeof window === "undefined") return "/login";
  const path = window.location.pathname;
  // ルート・ログイン自身へ戻す意味はないため redirect を付けない
  if (!path || path === "/" || path === "/login") return "/login";
  const current = `${path}${window.location.search}`;
  return `/login?redirect=${encodeURIComponent(current)}`;
}

/**
 * 画像として安全に参照できる URL のみを通す
 *
 * バックエンド由来の値であっても `imageFilePath` は写真編集時にクライアントが
 * 送信しうる項目のため、`href` / `src` へ差し込む前にスキームを検証する。
 * `javascript:` / `data:` / プロトコル相対 URL（`//host`）などを弾き、
 * 蓄積型 XSS やオープンリダイレクトの経路を塞ぐ。
 *
 * 許可するのは以下のみ。
 * - アプリ内の絶対パス（`/` 始まり）
 * - `https:` の絶対 URL（CSP の `img-src` に合わせる。`http:` は不許可）
 *   ただし認証情報付き URL（`https://user:pass@host/...`）は拒否する
 *
 * @param url 検証対象の URL
 * @returns 安全と判断できる場合はそのままの文字列。危険・不正な場合は空文字
 */
export function sanitizeImageUrl(url: string | null | undefined): string {
  if (!url) return "";
  const trimmed = url.trim();
  if (!trimmed) return "";
  // プロトコル相対 URL（`//evil.com`）は外部ホストを指すため拒否する
  if (trimmed.startsWith("//")) return "";
  // アプリ内の絶対パスは許可する
  if (trimmed.startsWith("/")) return trimmed;
  try {
    const parsed = new URL(trimmed);
    // 認証情報を埋め込んだ URL は拒否する
    if (parsed.username || parsed.password) return "";
    // CSP(img-src)に合わせ https のみ許可する
    if (parsed.protocol === "https:") {
      return trimmed;
    }
  } catch {
    // 相対パスや不正な文字列は new URL が例外を投げる
  }
  return "";
}
