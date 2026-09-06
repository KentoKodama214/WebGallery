import { sanitizeImageUrl, loginUrlWithRedirect, safeRedirectPath } from "../url";

describe("sanitizeImageUrl", () => {
  it("NEXT_PUBLIC_IMAGE_BASE_URL 未設定時は外部の https 絶対URLも空文字にする（フェイルクローズ）", () => {
    expect(sanitizeImageUrl("https://cdn.example.com/a.jpg")).toBe("");
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

  it("バックスラッシュ始まり（/\\host）は空文字にする", () => {
    expect(sanitizeImageUrl("/\\evil.com/a.jpg")).toBe("");
  });

  describe("NEXT_PUBLIC_IMAGE_BASE_URL 設定時", () => {
    const original = process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
    afterEach(() => {
      if (original === undefined) delete process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
      else process.env.NEXT_PUBLIC_IMAGE_BASE_URL = original;
    });

    it("許可オリジンの https URL のみ通し、他オリジンは空文字にする", () => {
      process.env.NEXT_PUBLIC_IMAGE_BASE_URL = "https://cdn.example.com/";
      expect(sanitizeImageUrl("https://cdn.example.com/a.jpg")).toBe(
        "https://cdn.example.com/a.jpg"
      );
      expect(sanitizeImageUrl("https://evil.com/a.jpg")).toBe("");
    });
  });

  describe("開発環境（NODE_ENV=development）", () => {
    const original = process.env.NODE_ENV;
    beforeEach(() => {
      (process.env as { NODE_ENV?: string }).NODE_ENV = "development";
    });
    afterEach(() => {
      (process.env as { NODE_ENV?: string }).NODE_ENV = original;
    });

    it("localhost / 127.0.0.1 の http URL（MinIO 署名付き URL）を通す", () => {
      expect(
        sanitizeImageUrl("http://localhost:9000/web-gallery-local/aaaa/DSC1.jpg?X-Amz-Signature=x")
      ).toBe("http://localhost:9000/web-gallery-local/aaaa/DSC1.jpg?X-Amz-Signature=x");
      expect(sanitizeImageUrl("http://127.0.0.1:9000/bucket/key.jpg")).toBe(
        "http://127.0.0.1:9000/bucket/key.jpg"
      );
    });

    it("localhost 以外の http URL は開発環境でも空文字にする", () => {
      expect(sanitizeImageUrl("http://evil.example.com/a.jpg")).toBe("");
    });
  });
});

describe("safeRedirectPath", () => {
  it("同一オリジンの内部パスはそのまま返す", () => {
    expect(safeRedirectPath("/aaaa1111/account_setting?tab=1")).toBe(
      "/aaaa1111/account_setting?tab=1"
    );
  });

  it("null・空・先頭スラッシュ以外は null", () => {
    expect(safeRedirectPath(null)).toBeNull();
    expect(safeRedirectPath("")).toBeNull();
    expect(safeRedirectPath("photo_list")).toBeNull();
    expect(safeRedirectPath("https://evil.com")).toBeNull();
  });

  it("プロトコル相対・バックスラッシュ始まりは null", () => {
    expect(safeRedirectPath("//evil.com")).toBeNull();
    expect(safeRedirectPath("/\\evil.com")).toBeNull();
  });

  it("解決後にプロトコル相対になるバイパス（/..//evil.com）を弾く", () => {
    expect(safeRedirectPath("/..//evil.com")).toBeNull();
    expect(safeRedirectPath("/foo/..//evil.com")).toBeNull();
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
