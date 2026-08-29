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
