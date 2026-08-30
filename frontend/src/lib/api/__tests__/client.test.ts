/**
 * APIクライアント（認証リフレッシュ・エラーメッセージ選別）の単体テスト
 */

type ClientModule = typeof import("../client");

/** 簡易レスポンスを生成する */
function makeResponse(
  body: unknown,
  init: { status?: number; text?: string } = {}
): Response {
  const status = init.status ?? 200;
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
    text: async () =>
      init.text ?? (typeof body === "string" ? body : JSON.stringify(body)),
    headers: new Headers(),
  } as Response;
}

/** 解決を外部から制御できるプロミス */
function deferred<T>() {
  let resolve!: (v: T) => void;
  let reject!: (e: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe("api/client", () => {
  let client: ClientModule;
  let fetchMock: jest.Mock;

  beforeEach(async () => {
    jest.resetModules();
    fetchMock = jest.fn();
    global.fetch = fetchMock as unknown as typeof fetch;
    client = await import("../client");
  });

  describe("login / readErrorMessage", () => {
    it("成功時にアクセストークンを保持する", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({ accessToken: "token-1", expiresIn: 3600 })
      );

      const result = await client.login("user", "pass");

      expect(result.accessToken).toBe("token-1");
      expect(client.getAccessToken()).toBe("token-1");
    });

    it("5xxエラー時はバックエンドの内部メッセージではなく既定文言を投げる", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse(
          { message: "NullPointerException at ..." },
          { status: 500 }
        )
      );

      await expect(client.login("user", "pass")).rejects.toThrow(
        "ログインに失敗しました"
      );
    });

    it("4xxエラー時はバックエンドのerrorMessageを採用する", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse(
          { errorMessage: "アカウントIDまたはパスワードが間違っています。" },
          { status: 401 }
        )
      );

      await expect(client.login("user", "pass")).rejects.toThrow(
        "アカウントIDまたはパスワードが間違っています。"
      );
    });
  });

  describe("refresh のシングルフライト", () => {
    it("並行呼び出しでもリフレッシュAPIは1回だけ叩かれる", async () => {
      const d = deferred<Response>();
      fetchMock.mockReturnValueOnce(d.promise);

      const p1 = client.refresh();
      const p2 = client.refresh();

      d.resolve(makeResponse({ accessToken: "token-refreshed" }));
      const [r1, r2] = await Promise.all([p1, p2]);

      expect(r1).toBe(true);
      expect(r2).toBe(true);
      expect(fetchMock).toHaveBeenCalledTimes(1);
      expect(fetchMock.mock.calls[0][0]).toContain("/api/v1/auth/refresh");
    });
  });

  describe("refresh の失敗時のセッション状態", () => {
    it("ネットワーク例外では未ログイン確定にせず、次回も再試行する", async () => {
      // 1回目: refresh がネットワーク例外
      fetchMock.mockRejectedValueOnce(new Error("network"));
      expect(await client.refresh()).toBe(false);

      // 2回目: fetchWithAuth が再度 refresh を試みる（anonymousに固定されていない）
      fetchMock.mockResolvedValueOnce(
        makeResponse({ accessToken: "recovered" })
      );
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));

      await client.fetchWithAuth("/api/v1/accounts/me");

      const refreshCalls = fetchMock.mock.calls.filter((c) =>
        String(c[0]).includes("/api/v1/auth/refresh")
      );
      expect(refreshCalls.length).toBe(2);
    });

    it("401が返った場合は未ログイン確定とし、以降のfetchWithAuthはrefreshしない", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({ errorMessage: "unauthorized" }, { status: 401 })
      );
      expect(await client.refresh()).toBe(false);

      // 以降の fetchWithAuth は refresh を試みず本リクエストのみ
      fetchMock.mockResolvedValueOnce(makeResponse({ data: [] }));
      await client.fetchWithAuth("/api/v1/accounts");

      const refreshCalls = fetchMock.mock.calls.filter((c) =>
        String(c[0]).includes("/api/v1/auth/refresh")
      );
      expect(refreshCalls.length).toBe(1);
    });
  });

  describe("マウント時 refresh とログインの競合", () => {
    it("進行中の refresh が 401 で返っても、その間に成立したログインの状態を上書きしない", async () => {
      // マウント時の refresh を開始（応答は保留）
      const d = deferred<Response>();
      fetchMock.mockReturnValueOnce(d.promise);
      const refreshPromise = client.refresh();

      // refresh 応答が返る前にログイン成功
      fetchMock.mockResolvedValueOnce(
        makeResponse({ accessToken: "login-token", expiresIn: 3600 })
      );
      await client.login("user", "pass");
      expect(client.getAccessToken()).toBe("login-token");

      // 遅れて refresh が 401 で返る（古い世代なので状態を書き換えない）
      d.resolve(makeResponse({ errorMessage: "unauthorized" }, { status: 401 }));
      expect(await refreshPromise).toBe(false);

      // ログインのトークンが維持されている
      expect(client.getAccessToken()).toBe("login-token");

      // sessionAuthState が anonymous に落ちていない（＝ fetchWithAuth が
      // 先読み refresh を試みることはあっても、ログイン済みとして振る舞える）
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
      const res = await client.fetchWithAuth("/api/v1/accounts/me");
      expect(res.ok).toBe(true);
      const lastCall = fetchMock.mock.calls[fetchMock.mock.calls.length - 1];
      expect((lastCall[1].headers as Headers).get("Authorization")).toBe(
        "Bearer login-token"
      );
    });
  });

  describe("fetchWithAuth", () => {
    it("401受信時にリフレッシュしてリトライする", async () => {
      // ログイン済みにしておく
      fetchMock.mockResolvedValueOnce(
        makeResponse({ accessToken: "old-token" })
      );
      await client.login("user", "pass");

      // 1回目の本リクエスト: 401
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 401 }));
      // refresh: 成功
      fetchMock.mockResolvedValueOnce(
        makeResponse({ accessToken: "new-token" })
      );
      // リトライ: 成功
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));

      const res = await client.fetchWithAuth("/api/v1/accounts/me");
      expect(res.ok).toBe(true);

      const retryCall = fetchMock.mock.calls[fetchMock.mock.calls.length - 1];
      const headers = retryCall[1].headers as Headers;
      expect(headers.get("Authorization")).toBe("Bearer new-token");
    });

    it("リフレッシュ後の再リクエストも401なら認証状態をクリアする", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "old-token" }));
      await client.login("user", "pass");

      // 本リクエスト: 401 → refresh 成功 → リトライも 401
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 401 }));
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "new-token" }));
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 401 }));

      const res = await client.fetchWithAuth("/api/v1/accounts/me");
      expect(res.status).toBe(401);
      // 認証状態がクリアされ、以降の fetchWithAuth は先読みリフレッシュしない
      expect(client.getAccessToken()).toBeNull();

      const callsBefore = fetchMock.mock.calls.length;
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
      await client.fetchWithAuth("/api/v1/accounts/me");
      // 追加のリクエストは本リクエスト1回のみ（先読みリフレッシュが走らない）
      const newCalls = fetchMock.mock.calls.slice(callsBefore);
      expect(newCalls).toHaveLength(1);
      expect(String(newCalls[0][0])).toContain("/api/v1/accounts/me");
    });
  });

  describe("不正なトークン応答の扱い", () => {
    it("login 応答に文字列 accessToken が無ければ失敗として扱う", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ expiresIn: 3600 }));
      await expect(client.login("user", "pass")).rejects.toThrow();
      expect(client.getAccessToken()).toBeNull();
    });

    it("refresh 応答に文字列 accessToken が無ければ false を返す", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({}));
      const ok = await client.refresh();
      expect(ok).toBe(false);
      expect(client.getAccessToken()).toBeNull();
    });
  });
});
