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
 * @param name Cookie名
 * @returns Cookie値（なければnull）
 */
export function getCookie(name: string): string | null {
  const cookies = document.cookie.split("; ");
  for (const cookie of cookies) {
    const [key, ...rest] = cookie.split("=");
    if (key === name) {
      return decodeURIComponent(rest.join("="));
    }
  }
  return null;
}

/**
 * Cookieを削除する
 * @param name Cookie名
 */
export function deleteCookie(name: string): void {
  document.cookie = `${name}=; path=/; max-age=0`;
}
