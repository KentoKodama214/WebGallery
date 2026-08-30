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
 * リダイレクト先クエリパラメータを検証し、安全な内部パスのみを返す
 *
 * 自オリジン基準で URL として解決し、オリジンが一致するものだけを許可する。
 * さらに、解決後のパス自体がプロトコル相対（`//host` / `/\host`）になっていないかを
 * 再確認する。`new URL("/..//evil.com", origin)` は `pathname` が `//evil.com` になり、
 * これをそのまま `router.push` へ渡すと外部オリジンへ遷移し得るため
 * （オープンリダイレクト）、入力・出力の双方でプロトコル相対表現を弾く。
 *
 * @param value redirect クエリパラメータの値
 * @returns 安全な内部パス（pathname + search + hash）。無効な場合は null
 */
export function safeRedirectPath(value: string | null | undefined): string | null {
  if (!value) return null;
  if (typeof window === "undefined") return null;
  // 先頭が "/" 以外（絶対 URL・相対パス）は受け付けない
  if (!value.startsWith("/")) return null;
  // プロトコル相対（`//evil.com`）・バックスラッシュ経由（`/\evil.com`）は入力段階で弾く
  if (value.startsWith("//") || value.startsWith("/\\")) return null;
  try {
    const url = new URL(value, window.location.origin);
    if (url.origin !== window.location.origin) return null;
    const path = `${url.pathname}${url.search}${url.hash}`;
    // 解決後のパスがプロトコル相対になっていないか再確認する
    // （例: "/..//evil.com" → pathname が "//evil.com"）
    if (path.startsWith("//") || path.startsWith("/\\")) return null;
    return path;
  } catch {
    return null;
  }
}

/**
 * 画像の読み込み元として許可するオリジン
 *
 * `NEXT_PUBLIC_IMAGE_BASE_URL`（写真配信元。例: `https://cdn.example.com/`）が
 * 設定されている場合はそのオリジンのみを許可する。未設定の場合は後方互換のため
 * すべての `https:` を許可する（従来動作）。
 *
 * @returns 許可オリジン。未設定・不正な場合は null
 */
function allowedImageOrigin(): string | null {
  const base = process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
  if (!base) return null;
  try {
    return new URL(base).origin;
  } catch {
    return null;
  }
}

/**
 * 画像として安全に参照できる URL のみを通す
 *
 * `imageFilePath` はバックエンドがサーバー設定から再生成するため通常は信頼できるが、
 * `href` / `src` へ差し込む前にスキーム・オリジンを検証し、`javascript:` / `data:` /
 * プロトコル相対 URL（`//host`）などを弾く。蓄積型 XSS やオープンリダイレクト、
 * 外部ホストへのトラッキング送信の経路を塞ぐ。
 *
 * 許可するのは以下のみ。
 * - アプリ内の絶対パス（`/` 始まり。ただし `//` `/\` は除く）
 * - `https:` の絶対 URL（`http:` は不許可、認証情報付き URL は常に拒否）で、
 *   **かつ `NEXT_PUBLIC_IMAGE_BASE_URL` が設定されており、そのオリジンと一致するもの**。
 *   `NEXT_PUBLIC_IMAGE_BASE_URL` 未設定時は外部の絶対 URL をすべて拒否する
 *   （CSP の `img-src` フォールバックと歩調を合わせ、XSS 時に任意の外部ホストへ
 *   画像リクエストでデータを持ち出す経路を塞ぐ。外部の画像配信元を使う構成では
 *   必ず `NEXT_PUBLIC_IMAGE_BASE_URL` を設定すること）。
 *
 * @param url 検証対象の URL
 * @returns 安全と判断できる場合はそのままの文字列。危険・不正な場合は空文字
 */
export function sanitizeImageUrl(url: string | null | undefined): string {
  if (!url) return "";
  const trimmed = url.trim();
  if (!trimmed) return "";
  // プロトコル相対 URL（`//evil.com`）・バックスラッシュ経由は外部ホストを指すため拒否する
  if (trimmed.startsWith("//") || trimmed.startsWith("/\\")) return "";
  // アプリ内の絶対パスは許可する
  if (trimmed.startsWith("/")) return trimmed;
  try {
    const parsed = new URL(trimmed);
    // 認証情報を埋め込んだ URL は拒否する
    if (parsed.username || parsed.password) return "";
    // CSP(img-src)に合わせ https のみ許可する
    if (parsed.protocol === "https:") {
      const allowed = allowedImageOrigin();
      // 許可オリジンが未設定なら外部の絶対 URL は一切通さない（フェイルクローズ）
      if (!allowed || parsed.origin !== allowed) return "";
      return trimmed;
    }
  } catch {
    // 相対パスや不正な文字列は new URL が例外を投げる
  }
  return "";
}
