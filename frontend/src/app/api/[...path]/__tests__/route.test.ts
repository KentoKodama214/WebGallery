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

  it("クライアント由来の X-Forwarded-Host / X-Forwarded-Proto / Forwarded / X-Real-IP を除去する", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      headers: {
        "x-forwarded-host": "evil.example",
        "x-forwarded-proto": "http",
        "x-real-ip": "1.2.3.4",
        forwarded: "for=1.2.3.4",
      },
    });
    await GET(req, ctx(["v1", "accounts"]));

    const headers = fetchMock.mock.calls[0][1].headers as Headers;
    expect(headers.get("x-forwarded-host")).toBeNull();
    expect(headers.get("x-forwarded-proto")).toBeNull();
    expect(headers.get("x-real-ip")).toBeNull();
    expect(headers.get("forwarded")).toBeNull();
  });

  it("前段プロキシが付与した X-Forwarded-For の左端を実クライアント IP として載せ直す", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      headers: {
        // 左端＝ALB が記録した実クライアント IP、以降＝中継プロキシ
        "x-forwarded-for": "203.0.113.9, 10.0.1.5",
      },
    });
    await GET(req, ctx(["v1", "accounts"]));

    const headers = fetchMock.mock.calls[0][1].headers as Headers;
    expect(headers.get("x-forwarded-for")).toBe("203.0.113.9");
  });

  it("X-Forwarded-For が無ければバックエンドへも付与しない", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/accounts");
    await GET(req, ctx(["v1", "accounts"]));

    const headers = fetchMock.mock.calls[0][1].headers as Headers;
    expect(headers.get("x-forwarded-for")).toBeNull();
  });

  it("バックエンド未到達時は502を返す", async () => {
    fetchMock.mockRejectedValueOnce(new Error("ECONNREFUSED"));

    const req = new NextRequest("http://localhost/api/v1/accounts", {
      method: "POST",
      body: JSON.stringify({ a: 1 }),
      headers: { "content-type": "application/json", origin: "http://localhost" },
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
        headers: {
          "content-length": String(7 * 1024 * 1024),
          origin: "http://localhost",
        },
      }
    );
    const res = await POST(req, ctx(["v1", "accounts", "foo", "photos"]));

    expect(res.status).toBe(413);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("content-length を伴わない過大なボディはストリーム中継中に打ち切って413を返す", async () => {
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
      headers: new Headers({
        "content-type": "application/octet-stream",
        origin: "http://localhost",
        host: "localhost",
      }),
      nextUrl: { search: "", host: "localhost" },
      body,
    } as unknown as NextRequest;

    // バックエンドがボディ（中継ストリーム）を読み進めると、上限超過で
    // ストリームがエラーになり fetch が reject する
    fetchMock.mockImplementationOnce(async (_url, init) => {
      const reader = (init.body as ReadableStream<Uint8Array>).getReader();
      for (;;) {
        const { done } = await reader.read();
        if (done) break;
      }
      return new Response(null, { status: 200 });
    });

    const res = await POST(fakeRequest, ctx(["v1", "accounts", "foo", "photos"]));

    expect(res.status).toBe(413);
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
      headers: { origin: "http://localhost" },
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

  it("Sec-Fetch-Site: cross-site の状態変更メソッドは403を返し中継しない", async () => {
    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { "sec-fetch-site": "cross-site" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(403);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("Sec-Fetch-Site: same-origin の状態変更メソッドは中継する", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { "sec-fetch-site": "same-origin", origin: "http://localhost" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(204);
    expect(fetchMock).toHaveBeenCalled();
  });

  it("Origin も Referer も無い状態変更メソッドは検証不能として403を返す", async () => {
    const req = new NextRequest("http://localhost/api/v1/accounts", {
      method: "POST",
      body: JSON.stringify({ a: 1 }),
      headers: { "content-type": "application/json" },
    });
    const res = await POST(req, ctx(["v1", "accounts"]));

    expect(res.status).toBe(403);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("Origin は無いが Referer が自サイトなら中継する", async () => {
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { referer: "http://localhost/photo/user1/photo_list" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(204);
    expect(fetchMock).toHaveBeenCalled();
  });

  it("Referer が別サイトなら403を返し中継しない", async () => {
    const req = new NextRequest("http://localhost/api/v1/auth/logout", {
      method: "POST",
      headers: { referer: "https://evil.example/x" },
    });
    const res = await POST(req, ctx(["v1", "auth", "logout"]));

    expect(res.status).toBe(403);
    expect(fetchMock).not.toHaveBeenCalled();
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

  it("外部の絶対URLを指す Location は反射型オープンリダイレクト防止のため削除する", async () => {
    const headers = new Headers({ location: "https://evil.example/steal" });
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 302, headers })
    );

    const req = new NextRequest("http://localhost/api/v1/redirect");
    const res = await GET(req, ctx(["v1", "redirect"]));

    expect(res.headers.get("location")).toBeNull();
  });

  it("自オリジン内の相対パスの Location はそのまま通す", async () => {
    const headers = new Headers({ location: "/login" });
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 302, headers })
    );

    const req = new NextRequest("http://localhost/api/v1/redirect");
    const res = await GET(req, ctx(["v1", "redirect"]));

    expect(res.headers.get("location")).toBe("/login");
  });

  it("BACKEND_URL をプレフィックスに持つだけのホスト詐称 Location は削除する", async () => {
    const headers = new Headers({
      location: "http://localhost:8080.evil.example/steal",
    });
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 302, headers })
    );

    const req = new NextRequest("http://localhost/api/v1/redirect");
    const res = await GET(req, ctx(["v1", "redirect"]));

    expect(res.headers.get("location")).toBeNull();
  });

  it("相対パス化した結果がプロトコル相対（//host）になる Location は削除する", async () => {
    const headers = new Headers({
      location: "http://localhost:8080//evil.example/steal",
    });
    fetchMock.mockResolvedValueOnce(
      new Response(null, { status: 302, headers })
    );

    const req = new NextRequest("http://localhost/api/v1/redirect");
    const res = await GET(req, ctx(["v1", "redirect"]));

    expect(res.headers.get("location")).toBeNull();
  });

  it("同時中継数が上限に達している間は 503 を返し、バックエンドへ中継しない", async () => {
    jest.resetModules();
    process.env.PROXY_MAX_CONCURRENCY = "2";

    let releaseBackend: () => void = () => {};
    const pending = new Promise<Response>((resolve) => {
      releaseBackend = () => resolve(new Response(null, { status: 204 }));
    });
    const gatedFetch = jest.fn().mockReturnValue(pending);
    global.fetch = gatedFetch as unknown as typeof fetch;

    const route = await import("../route");
    const makeReq = () => new NextRequest("http://localhost/api/v1/accounts");

    // 上限（2）まではバックエンドへ中継され、応答待ちで滞留する
    const inflight1 = route.GET(makeReq(), ctx(["v1", "accounts"]));
    const inflight2 = route.GET(makeReq(), ctx(["v1", "accounts"]));

    // 3件目は上限超過で即座に 503（fetch は呼ばれない）
    const shed = await route.GET(makeReq(), ctx(["v1", "accounts"]));
    expect(shed.status).toBe(503);
    expect(shed.headers.get("retry-after")).toBe("5");
    expect(gatedFetch).toHaveBeenCalledTimes(2);

    releaseBackend();
    await Promise.all([inflight1, inflight2]);

    delete process.env.PROXY_MAX_CONCURRENCY;
    jest.resetModules();
  });
});
