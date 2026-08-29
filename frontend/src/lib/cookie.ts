/**
 * Cookieをセットする
 * @param name Cookie名
 * @param value Cookie値
 * @param maxAgeSeconds 有効期限（秒）
 */
export function setCookie(name: string, value: string, maxAgeSeconds: number): void {
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAgeSeconds}; SameSite=Lax`;
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
