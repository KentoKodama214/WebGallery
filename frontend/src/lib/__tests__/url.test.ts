import { sanitizeImageUrl, loginUrlWithRedirect } from "../url";

describe("sanitizeImageUrl", () => {
  it("https の絶対URLはそのまま通す", () => {
    expect(sanitizeImageUrl("https://cdn.example.com/a.jpg")).toBe(
      "https://cdn.example.com/a.jpg"
    );
  });

  it("http（非https）の絶対URLは空文字にする", () => {
    expect(sanitizeImageUrl("http://example.com/a.jpg")).toBe("");
  });

  it("認証情報付きURLは空文字にする", () => {
    expect(sanitizeImageUrl("https://user:pass@evil.com/a.jpg")).toBe("");
  });

  it("アプリ内の絶対パスは通す", () => {
    expect(sanitizeImageUrl("/image/photo.jpg")).toBe("/image/photo.jpg");
  });

  it("javascript: スキームは空文字にする", () => {
    expect(sanitizeImageUrl("javascript:alert(1)")).toBe("");
    expect(sanitizeImageUrl("  JavaScript:alert(1)")).toBe("");
  });

  it("data: スキームは空文字にする", () => {
    expect(sanitizeImageUrl("data:text/html,<script>alert(1)</script>")).toBe("");
  });

  it("プロトコル相対URL（//host）は空文字にする", () => {
    expect(sanitizeImageUrl("//evil.com/a.jpg")).toBe("");
  });

  it("相対パス・空値は空文字にする", () => {
    expect(sanitizeImageUrl("photo.jpg")).toBe("");
    expect(sanitizeImageUrl("")).toBe("");
    expect(sanitizeImageUrl(null)).toBe("");
    expect(sanitizeImageUrl(undefined)).toBe("");
  });
});

describe("loginUrlWithRedirect", () => {
  afterEach(() => {
    window.history.replaceState({}, "", "/");
  });

  it("ルート・ログイン画面では redirect を付けない", () => {
    window.history.replaceState({}, "", "/");
    expect(loginUrlWithRedirect()).toBe("/login");
    window.history.replaceState({}, "", "/login");
    expect(loginUrlWithRedirect()).toBe("/login");
  });

  it("保護ページからは現在パスを redirect に載せる", () => {
    window.history.replaceState({}, "", "/aaaa1111/account_setting?tab=1");
    expect(loginUrlWithRedirect()).toBe(
      "/login?redirect=%2Faaaa1111%2Faccount_setting%3Ftab%3D1"
    );
  });
});
