/**
 * @jest-environment node
 */
import { NextRequest } from "next/server";
import { GET, POST } from "../route";

type Ctx = { params: Promise<{ path: string[] }> };

function ctx(path: string[]): Ctx {
  return { params: Promise.resolve({ path }) };
}

describe("APIプロキシ route", () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn();
    global.fetch = fetchMock as unknown as typeof fetch;
  });

  it("バックエンドへパスとクエリを引き継いで中継する", async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { "content-type": "application/json" },
      })
    );

    const req = new NextRequest("http://localhost/api/v1/accounts?pageNo=2", {
      headers: { cookie: "refreshToken=abc" },
    });
    const res = await GET(req, ctx(["v1", "accounts"]));

    expect(res.status).toBe(200);
    const [calledUrl, init] = fetchMock.mock.calls[0];
    expect(calledUrl).toBe("http://localhost:8080/api/v1/accounts?pageNo=2");
    // Cookie は転送される
    expect((init.headers as Headers).get("cookie")).toBe("refreshToken=abc");
  });

  it("クライアント由来の X-Forwarded-* / X-Real-IP を除去する", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      headers: {
        "x-forwarded-for": "1.2.3.4",
        "x-forwarded-host": "evil.example",
        "x-real-ip": "1.2.3.4",
        forwarded: "for=1.2.3.4",
      },
    });
    await GET(req, ctx(["v1", "accounts"]));

    const headers = fetchMock.mock.calls[0][1].headers as Headers;
    expect(headers.get("x-forwarded-for")).toBeNull();
    expect(headers.get("x-forwarded-host")).toBeNull();
    expect(headers.get("x-real-ip")).toBeNull();
    expect(headers.get("forwarded")).toBeNull();
  });

  it("バックエンド未到達時は502を返す", async () => {
    fetchMock.mockRejectedValueOnce(new Error("ECONNREFUSED"));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      method: "POST",
      body: JSON.stringify({ a: 1 }),
      headers: { "content-type": "application/json" },
    });
    const res = await POST(req, ctx(["v1", "accounts"]));

    expect(res.status).toBe(502);
    const body = await res.json();
    expect(body.message).toContain("バックエンド");
  });

  it("バックエンド応答がタイムアウトした場合は504を返す", async () => {
    fetchMock.mockRejectedValueOnce(
      new DOMException("The operation timed out.", "TimeoutError")
    );

    const req = new NextRequest("http://localhost/api/v1/accounts");
    const res = await GET(req, ctx(["v1", "accounts"]));

    expect(res.status).toBe(504);
    const body = await res.json();
    expect(body.message).toContain("応答");
  });

  it("content-length が上限を超えるリクエストは413を返し、転送しない", async () => {
    const req = new NextRequest(
      "http://localhost/api/v1/accounts/foo/photos",
      {
        method: "POST",
        headers: { "content-length": String(7 * 1024 * 1024) },
      }
    );
    const res = await POST(req, ctx(["v1", "accounts", "foo", "photos"]));

    expect(res.status).toBe(413);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("content-length を伴わない過大なボディはストリーム読み取り中に打ち切って413を返す", async () => {
    const oneMb = new Uint8Array(1024 * 1024);
    let emitted = 0;
    const body = new ReadableStream<Uint8Array>({
      pull(controller) {
        if (emitted++ < 8) {
          controller.enqueue(oneMb);
        } else {
          controller.close();
        }
      },
    });
    const fakeRequest = {
      method: "POST",
      headers: new Headers({ "content-type": "application/octet-stream" }),
      nextUrl: { search: "" },
      body,
    } as unknown as NextRequest;

    const res = await POST(fakeRequest, ctx(["v1", "accounts", "foo", "photos"]));

    expect(res.status).toBe(413);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("ドットセグメントを含むパスは中継せず400を返す", async () => {
    const req = new NextRequest("http://localhost/api/v1/x");
    const res = await GET(req, ctx(["v1", "..", "actuator"]));

    expect(res.status).toBe(400);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("複数の Set-Cookie を個別に転送する", async () => {
    const backendHeaders = new Headers();
    backendHeaders.append("set-cookie", "a=1; Path=/");
    backendHeaders.append("set-cookie", "b=2; Path=/api/v1/auth");
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 200, headers: backendHeaders })
    );

    const req = new NextRequest("http://localhost/api/v1/auth/login", {
      method: "POST",
    });
    const res = await POST(req, ctx(["v1", "auth", "login"]));

    const cookies = res.headers.getSetCookie();
    expect(cookies).toEqual([
      "a=1; Path=/",
      "b=2; Path=/api/v1/auth",
    ]);
  });

  it("状態変更メソッドで Origin が自サイトと一致すれば中継する", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { origin: "http://localhost" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(204);
    expect(fetchMock).toHaveBeenCalled();
  });

  it("状態変更メソッドで Origin が別サイトなら403を返し中継しない", async () => {
    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { origin: "https://evil.example" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(403);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("Origin ヘッダーが無い状態変更メソッドは素通しする", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      method: "POST",
      body: JSON.stringify({ a: 1 }),
      headers: { "content-type": "application/json" },
    });
    const res = await POST(req, ctx(["v1", "accounts"]));

    expect(res.status).toBe(204);
    expect(fetchMock).toHaveBeenCalled();
  });

  it("バックエンド絶対URLの Location を相対パスへ書き換える", async () => {
    const headers = new Headers({ location: "http://localhost:8080/api/v1/foo" });
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 302, headers })
    );

    const req = new NextRequest("http://localhost/api/v1/redirect");
    const res = await GET(req, ctx(["v1", "redirect"]));

    expect(res.headers.get("location")).toBe("/api/v1/foo");
  });
});
