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

/**
 * matcher がそのパスを対象にするか（＝proxy が走り CSP が付くか）を判定する
 *
 * Next.js は文字列の matcher を正規表現へ変換して pathname と照合する。この matcher は
 * path-to-regexp のパラメータ（`:param`）を含まない純粋な正規表現なので、前後を
 * アンカーした RegExp として評価すれば同じ判定になる。
 */
function isProxied(pathname: string): boolean {
  const matcher = Array.isArray(config.matcher)
    ? config.matcher[0]
    : (config.matcher as string);
  return new RegExp(`^${matcher}$`).test(pathname);
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
    expect(isProxied("/api/v1/accounts")).toBe(false);
    expect(isProxied("/api")).toBe(false);
    expect(isProxied("/_next/static/chunks/main.js")).toBe(false);
    expect(isProxied("/_next/image")).toBe(false);
    expect(isProxied("/favicon.ico")).toBe(false);
    expect(isProxied("/ui/filter.png")).toBe(false);
  });

  it("除外語で始まるだけのページパスは除外しない（CSP が付く）", () => {
    // accountId は半角英数字8〜20文字（lib/validation.ts）なので `api` で始まる値を
    // 誰でも登録できる。前方一致で除外すると、このページだけ CSP と nonce が
    // 付かないまま公開されてしまう
    expect(isProxied("/apiuser1/account_setting")).toBe(true);
    expect(isProxied("/apitest12/account_setting")).toBe(true);
    // `favicon.ico` の `.` を任意1文字として扱っていないこと
    expect(isProxied("/faviconXico")).toBe(true);
    // 通常のページパスは従来どおり対象
    expect(isProxied("/login")).toBe(true);
    expect(isProxied("/photo/testuser1/photo_list")).toBe(true);
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
    // Nominatim（撮影場所の地図選択UI用）は常時許可される静的な追加のため、それ以外の
    // 動的オリジンが追加されていないことを確認する
    expect(csp).toMatch(
      /connect-src 'self' https:\/\/nominatim\.openstreetmap\.org(?! http)/
    );
  });

  it("NEXT_PUBLIC_API_BASE_URL のオリジンが 'null'（opaque origin）の場合は connect-src に追加しない", async () => {
    jest.resetModules();
    process.env.NEXT_PUBLIC_API_BASE_URL = "data:text/plain,abc";
    const { proxy: proxyWithOpaqueOrigin } = await import("../proxy");

    const res = proxyWithOpaqueOrigin(makeRequest("/login"));
    const csp = res.headers.get("content-security-policy");
    expect(csp).toMatch(
      /connect-src 'self' https:\/\/nominatim\.openstreetmap\.org(?! http)/
    );
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
