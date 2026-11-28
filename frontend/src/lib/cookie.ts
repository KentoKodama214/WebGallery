/**
 * Cookie名として許容する文字（RFC6265 の token から、実装上安全な範囲に限定）
 *
 * 呼び出し側が未検証の入力（URL パスパラメータ等）を Cookie 名に紛れ込ませても、
 * `;` や空白・`=` による `document.cookie` セッターへの属性インジェクションを防ぐ。
 */
const COOKIE_NAME_PATTERN = /^[A-Za-z0-9_-]+$/;

/**
 * Cookieをセットする
 *
 * HTTPS で配信されている場合は `Secure` 属性を付与し、平文 HTTP へは送出させない
 * （多層防御。ローカル開発の http://localhost では付与しないため従来どおり動作する）。
 *
 * @param name Cookie名（半角英数字・`_`・`-` のみ。不正な場合は例外）
 * @param value Cookie値
 * @param maxAgeSeconds 有効期限（秒）
 */
export function setCookie(name: string, value: string, maxAgeSeconds: number): void {
  if (!COOKIE_NAME_PATTERN.test(name)) {
    throw new Error(`不正なCookie名です: ${name}`);
  }
  const secure =
    typeof location !== "undefined" && location.protocol === "https:"
      ? "; Secure"
      : "";
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAgeSeconds}; SameSite=Lax${secure}`;
}

/**
 * Cookieを取得する
 *
 * `decodeURIComponent` は不正なパーセントエンコーディング（`%` 単体等）で `URIError` を投げる。
 * `setCookie` は必ず `encodeURIComponent` するため自アプリ由来では起きないが、親ドメインの
 * 別サービスやブラウザ拡張が同名Cookieを不正な形式で書いた場合に投げうる。呼び出し側は
 * レンダリング中（`useMemo` 等）からも呼ぶため、ここで投げるとページ全体がエラーバウンダリへ
 * 落ちてしまう。デコードできない値は生の文字列として返し、読み出しで失敗させない。
 *
 * @param name Cookie名
 * @returns Cookie値（なければnull）
 */
export function getCookie(name: string): string | null {
  const cookies = document.cookie.split("; ");
  for (const cookie of cookies) {
    const [key, ...rest] = cookie.split("=");
    if (key === name) {
      const raw = rest.join("=");
      try {
        return decodeURIComponent(raw);
      } catch {
        return raw;
      }
    }
  }
  return null;
}
