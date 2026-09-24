/**
 * @jest-environment node
 */
import type { NextRequest } from "next/server";
import { proxy, config } from "../proxy";

/** テスト用に最小限のNextRequestを作る */
function makeRequest(pathname: string): NextRequest {
  const url = `http://localhost${pathname}`;
  return {
    nextUrl: new URL(url),
    url,
    headers: new Headers(),
  } as unknown as NextRequest;
}

describe("proxy (ルーティング制御)", () => {
  it("ルート(/)は/loginへリダイレクトする", () => {
    const res = proxy(makeRequest("/"));
    expect(res.status).toBe(307);
    expect(res.headers.get("location")).toBe("http://localhost/login");
  });

  it("ルート以外はそのまま通過する（リダイレクトしない）", () => {
    const res = proxy(makeRequest("/login"));
    // NextResponse.next() は location を持たない
    expect(res.headers.get("location")).toBeNull();
  });

  it("matcherがapiと静的ファイルを除外している", () => {
    const matcher = Array.isArray(config.matcher)
      ? config.matcher[0]
      : config.matcher;
    expect(matcher).toContain("?!api");
  });
});

describe("proxy (Content-Security-Policy)", () => {
  it("ページレスポンスに nonce 付き CSP を付与する", () => {
    const res = proxy(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toBeTruthy();
    // script-src から 'unsafe-inline' を排除し、nonce + strict-dynamic を使う
    expect(csp).toMatch(/script-src [^;]*'nonce-[^']+'/);
    expect(csp).toMatch(/script-src [^;]*'strict-dynamic'/);
    expect(csp).not.toMatch(/script-src [^;]*'unsafe-inline'/);
    expect(csp).toContain("object-src 'none'");
    expect(csp).toContain("frame-ancestors 'none'");
  });

  it("style-src-elem で <style>/<link> 要素を nonce または自オリジンに限定する", () => {
    const res = proxy(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    // <style> 要素側は 'unsafe-inline' を含めない（注入された <style> を防ぐ）
    expect(csp).toMatch(/style-src-elem 'self' 'nonce-[^']+'/);
    expect(csp).not.toMatch(/style-src-elem [^;]*'unsafe-inline'/);
    // style 属性（React の style={{}}）向けのフォールバックは維持する
    expect(csp).toMatch(/style-src 'self' 'unsafe-inline'/);
  });

  it("リクエストごとに異なる nonce を生成する", () => {
    const csp1 = proxy(makeRequest("/login")).headers.get(
      "content-security-policy"
    );
    const csp2 = proxy(makeRequest("/login")).headers.get(
      "content-security-policy"
    );
    const nonce1 = csp1?.match(/'nonce-([^']+)'/)?.[1];
    const nonce2 = csp2?.match(/'nonce-([^']+)'/)?.[1];
    expect(nonce1).toBeTruthy();
    expect(nonce2).toBeTruthy();
    expect(nonce1).not.toBe(nonce2);
  });

  it("リダイレクトレスポンスにも CSP を付与する", () => {
    const res = proxy(makeRequest("/"));
    expect(res.headers.get("content-security-policy")).toBeTruthy();
  });
});

describe("proxy (画像・API オリジンの環境変数)", () => {
  const originalImageBaseUrl = process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
  const originalApiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  afterEach(() => {
    if (originalImageBaseUrl === undefined) {
      delete process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
    } else {
      process.env.NEXT_PUBLIC_IMAGE_BASE_URL = originalImageBaseUrl;
    }
    if (originalApiBaseUrl === undefined) {
      delete process.env.NEXT_PUBLIC_API_BASE_URL;
    } else {
      process.env.NEXT_PUBLIC_API_BASE_URL = originalApiBaseUrl;
    }
    jest.resetModules();
  });

  it("NEXT_PUBLIC_IMAGE_BASE_URL が不正なURLの場合は img-src に追加オリジンを含めない", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_IMAGE_BASE_URL = "not a valid url";
    const { proxy: proxyWithInvalidImageBase } = await import("../proxy");

    const res = proxyWithInvalidImageBase(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toMatch(/img-src 'self' data: blob:/);
  });

  it("NEXT_PUBLIC_API_BASE_URL が不正なURLの場合は connect-src に追加オリジンを含めない", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_API_BASE_URL = "not a valid url";
    const { proxy: proxyWithInvalidApiBase } = await import("../proxy");

    const res = proxyWithInvalidApiBase(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toMatch(/connect-src 'self'(?! http)/);
  });

  it("NEXT_PUBLIC_API_BASE_URL のオリジンが 'null'（opaque origin）の場合は connect-src に追加しない", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_API_BASE_URL = "data:text/plain,abc";
    const { proxy: proxyWithOpaqueOrigin } = await import("../proxy");

    const res = proxyWithOpaqueOrigin(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toMatch(/connect-src 'self'(?! http)/);
  });

  it("NEXT_PUBLIC_IMAGE_BASE_URL が有効なURLの場合は img-src にそのオリジンを追加する", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_IMAGE_BASE_URL = "https://cdn.example.com/";
    const { proxy: proxyWithImageBase } = await import("../proxy");

    const res = proxyWithImageBase(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toContain("https://cdn.example.com");
  });

  it("NEXT_PUBLIC_API_BASE_URL が有効なURLの場合は connect-src にそのオリジンを追加する", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_API_BASE_URL = "https://api.example.com";
    const { proxy: proxyWithApiBase } = await import("../proxy");

    const res = proxyWithApiBase(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toContain("https://api.example.com");
  });
});
