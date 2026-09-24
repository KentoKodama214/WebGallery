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
    it("ネットワーク例外では未ログイン確定にせず、クールダウン経過後に再試行する", async () => {
      const nowSpy = jest.spyOn(Date, "now").mockReturnValue(1_000_000);

      // 1回目: refresh がネットワーク例外 → クールダウン開始
      fetchMock.mockRejectedValueOnce(new Error("network"));
      expect(await client.refresh()).toBe(false);

      // クールダウン中: fetchWithAuth は refresh を再試行しない（本リクエストのみ）
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
      await client.fetchWithAuth("/api/v1/accounts/me");
      let refreshCalls = fetchMock.mock.calls.filter((c) =>
        String(c[0]).includes("/api/v1/auth/refresh")
      );
      expect(refreshCalls.length).toBe(1);

      // クールダウン（5秒）経過後: 再試行され、回復できる（anonymousに固定されていない）
      nowSpy.mockReturnValue(1_000_000 + 6_000);
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "recovered" }));
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
      await client.fetchWithAuth("/api/v1/accounts/me");

      refreshCalls = fetchMock.mock.calls.filter((c) =>
        String(c[0]).includes("/api/v1/auth/refresh")
      );
      expect(refreshCalls.length).toBe(2);

      nowSpy.mockRestore();
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

  describe("updateAccount / deleteAccount のエラー", () => {
    beforeEach(async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "token-1" }));
      await client.login("user", "pass");
    });

    it("updateAccount は HTTP エラー時に status 付きの ApiError を投げる", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({ errorMessage: "現在のパスワードが正しくありません" }, { status: 403 })
      );

      const err = await client
        .updateAccount("me", {
          accountId: "me",
          accountName: "n",
          newPassword: "newpassword1",
          currentPassword: "wrong",
          birthdate: null,
          sexKbn: "none",
          birthplacePrefectureKbnCode: "none",
          residentPrefectureKbnCode: "none",
          freeMemo: "",
        })
        .catch((e) => e);

      expect(err).toBeInstanceOf(client.ApiError);
      expect(err.status).toBe(403);
      expect(err.message).toBe("現在のパスワードが正しくありません");
    });

    it("deleteAccount は HTTP エラー時に status 付きの ApiError を投げる", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({ errorMessage: "現在のパスワードが正しくありません" }, { status: 403 })
      );

      const err = await client.deleteAccount("me", "wrong").catch((e) => e);

      expect(err).toBeInstanceOf(client.ApiError);
      expect(err.status).toBe(403);
    });

    it("deleteAccount は POST /deletion で現在のパスワードを JSON ボディで送る", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 200 }));

      await client.deleteAccount("me", "password01");

      const [url, init] = fetchMock.mock.calls.at(-1)!;
      expect(String(url)).toContain("/api/v1/accounts/me/deletion");
      expect(init?.method).toBe("POST");
      expect(new Headers(init?.headers).get("Content-Type")).toBe("application/json");
      expect(JSON.parse(init?.body as string)).toEqual({ currentPassword: "password01" });
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

    it("login でネットワーク例外が発生した場合は接続エラーメッセージを投げる", async () => {
      fetchMock.mockRejectedValueOnce(new Error("network"));
      await expect(client.login("user", "pass")).rejects.toThrow(
        "サーバーに接続できませんでした。時間をおいて再度お試しください"
      );
    });

    it("refresh 応答の JSON パースに失敗した場合は false を返す", async () => {
      fetchMock.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => {
          throw new Error("invalid json");
        },
        headers: new Headers(),
      } as unknown as Response);
      const ok = await client.refresh();
      expect(ok).toBe(false);
      expect(client.getAccessToken()).toBeNull();
    });

    it("refresh が 5xx の場合はクールダウンを設定し、直後の再試行はスキップされる", async () => {
      const nowSpy = jest.spyOn(Date, "now").mockReturnValue(2_000_000);
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 500 }));
      expect(await client.refresh()).toBe(false);

      const ok = await client.refresh();
      expect(ok).toBe(false);
      expect(fetchMock).toHaveBeenCalledTimes(1);
      nowSpy.mockRestore();
    });
  });

  describe("setAccessToken / clearAuthState", () => {
    it("setAccessToken でトークンを直接設定・取得できる", () => {
      client.setAccessToken("manual-token");
      expect(client.getAccessToken()).toBe("manual-token");
    });

    it("clearAuthState でトークンが null になる", () => {
      client.setAccessToken("manual-token");
      client.clearAuthState();
      expect(client.getAccessToken()).toBeNull();
    });
  });

  describe("logout", () => {
    it("ログアウトAPIを呼び出し、成功時に認証状態をクリアする", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "token-1" }));
      await client.login("user", "pass");

      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 200 }));
      await client.logout();

      expect(client.getAccessToken()).toBeNull();
      const lastCall = fetchMock.mock.calls.at(-1)!;
      expect(String(lastCall[0])).toContain("/api/v1/auth/logout");
      expect(lastCall[1]?.method).toBe("POST");
    });

    it("ログアウトAPIが例外を投げても認証状態はクリアされる", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "token-1" }));
      await client.login("user", "pass");

      fetchMock.mockRejectedValueOnce(new Error("network"));
      await client.logout();

      expect(client.getAccessToken()).toBeNull();
    });
  });

  describe("readErrorMessage / readJson のフォールバック", () => {
    beforeEach(async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "token-1" }));
      await client.login("user", "pass");
    });

    it("エラー応答の本文が空の場合は既定メッセージを使う", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 400, text: "" }));
      await expect(client.getPrefectures()).rejects.toThrow(
        "都道府県一覧の取得に失敗しました"
      );
    });

    it("エラー応答の本文が非JSONの場合は既定メッセージを使う", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({}, { status: 400, text: "not-json" })
      );
      await expect(client.getPrefectures()).rejects.toThrow(
        "都道府県一覧の取得に失敗しました"
      );
    });

    it("エラー応答の text() が例外を投げた場合は既定メッセージを使う", async () => {
      fetchMock.mockResolvedValueOnce({
        ok: false,
        status: 400,
        text: async () => {
          throw new Error("boom");
        },
        json: async () => ({}),
        headers: new Headers(),
      } as unknown as Response);
      await expect(client.getPrefectures()).rejects.toThrow(
        "都道府県一覧の取得に失敗しました"
      );
    });

    it("成功応答のJSONパースに失敗した場合は解釈不能メッセージを投げる", async () => {
      fetchMock.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => {
          throw new Error("invalid json");
        },
        headers: new Headers(),
      } as unknown as Response);
      await expect(client.getPrefectures()).rejects.toThrow(
        "サーバーからの応答を解釈できませんでした。時間をおいて再度お試しください"
      );
    });
  });

  describe("registerAccount（未ログインで呼び出す生fetch）", () => {
    const data: Parameters<ClientModule["registerAccount"]>[0] = {
      accountId: "newuser",
      accountName: "新規ユーザー",
      password: "password01",
      birthdate: null,
      sexKbn: "none",
      birthplacePrefectureKbnCode: "none",
      residentPrefectureKbnCode: "none",
      freeMemo: "",
    };

    it("成功時は登録結果を返す", async () => {
      fetchMock.mockResolvedValueOnce(
        makeResponse({ httpStatus: 201, isSuccess: true, message: "ok" })
      );
      const result = await client.registerAccount(data);
      expect(result.isSuccess).toBe(true);
      const [url, init] = fetchMock.mock.calls.at(-1)!;
      expect(String(url)).toContain("/api/v1/accounts");
      expect(init?.method).toBe("POST");
    });

    it("エラー時は既定メッセージを投げる", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 400 }));
      await expect(client.registerAccount(data)).rejects.toThrow(
        "アカウントの登録に失敗しました"
      );
    });
  });

  describe("認証付きAPI関数群", () => {
    beforeEach(async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ accessToken: "token-1" }));
      await client.login("user", "pass");
    });

    type Case = {
      name: string;
      call: () => Promise<unknown>;
      defaultMessage: string;
      urlIncludes: string;
      method: string;
    };

    const cases: Case[] = [
      {
        name: "getAccountList",
        call: () => client.getAccountList(1),
        defaultMessage: "アカウント一覧の取得に失敗しました",
        urlIncludes: "/api/v1/accounts?pageNo=1",
        method: "GET",
      },
      {
        name: "getAccount",
        call: () => client.getAccount("acc1"),
        defaultMessage: "アカウント情報の取得に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1",
        method: "GET",
      },
      {
        name: "getPrefectures",
        call: () => client.getPrefectures(),
        defaultMessage: "都道府県一覧の取得に失敗しました",
        urlIncludes: "/api/v1/prefectures",
        method: "GET",
      },
      {
        name: "getPhotoList",
        call: () =>
          client.getPhotoList("acc1", {
            directionKbn: "landscape",
            isFavorite: "true",
            tagList: "1,2",
            sortBy: "newest",
            pageNo: 2,
            searchExecuted: true,
            referer: "https://example.com",
            logInitialView: true,
          }),
        defaultMessage: "写真一覧の取得に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos?",
        method: "GET",
      },
      {
        name: "getPhotoUpperLimit",
        call: () => client.getPhotoUpperLimit("acc1"),
        defaultMessage: "写真登録上限の取得に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos/upper-limit",
        method: "GET",
      },
      {
        name: "getPhotoDetail",
        call: () => client.getPhotoDetail("acc1", 1, "https://example.com"),
        defaultMessage: "写真詳細の取得に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos/1",
        method: "GET",
      },
      {
        name: "deletePhoto",
        call: () =>
          client.deletePhoto("acc1", { photoNo: 1, imageFilePath: "path" }),
        defaultMessage: "写真の削除に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos",
        method: "DELETE",
      },
      {
        name: "addFavorite",
        call: () => client.addFavorite(1, 2),
        defaultMessage: "お気に入りの登録に失敗しました",
        urlIncludes: "/api/v1/photos/favorites",
        method: "POST",
      },
      {
        name: "deleteFavorite",
        call: () => client.deleteFavorite(1, 2),
        defaultMessage: "お気に入りの解除に失敗しました",
        urlIncludes: "/api/v1/photos/favorites",
        method: "DELETE",
      },
      {
        name: "savePhoto",
        call: () => client.savePhoto("acc1", new FormData()),
        defaultMessage: "写真の保存に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos",
        method: "PUT",
      },
      {
        name: "registPhotos",
        call: () => client.registPhotos("acc1", new FormData()),
        defaultMessage: "写真の登録に失敗しました",
        urlIncludes: "/api/v1/accounts/acc1/photos",
        method: "POST",
      },
      {
        name: "getAdminAccountList",
        call: () => client.getAdminAccountList(1),
        defaultMessage: "アカウント一覧の取得に失敗しました",
        urlIncludes: "/api/v1/admin/accounts?pageNo=1",
        method: "GET",
      },
      {
        name: "unlockAccount",
        call: () => client.unlockAccount(1),
        defaultMessage: "アカウントのロック解除に失敗しました",
        urlIncludes: "/api/v1/admin/accounts/1/unlock",
        method: "PUT",
      },
      {
        name: "lockAccount",
        call: () => client.lockAccount(1),
        defaultMessage: "アカウントのロックに失敗しました",
        urlIncludes: "/api/v1/admin/accounts/1/lock",
        method: "PUT",
      },
      {
        name: "updateAccountAuthority",
        call: () => client.updateAccountAuthority(1, "admin"),
        defaultMessage: "権限の変更に失敗しました",
        urlIncludes: "/api/v1/admin/accounts/1/authority",
        method: "PUT",
      },
      {
        name: "registerInquiry",
        call: () => client.registerInquiry({ subject: "件名", body: "本文" }),
        defaultMessage: "お問い合わせの登録に失敗しました",
        urlIncludes: "/api/v1/inquiries",
        method: "POST",
      },
      {
        name: "getInquiryList",
        call: () => client.getInquiryList(1),
        defaultMessage: "お問い合わせ一覧の取得に失敗しました",
        urlIncludes: "/api/v1/inquiries?pageNo=1",
        method: "GET",
      },
      {
        name: "getInquiryDetail",
        call: () => client.getInquiryDetail(1),
        defaultMessage: "お問い合わせ詳細の取得に失敗しました",
        urlIncludes: "/api/v1/inquiries/1",
        method: "GET",
      },
      {
        name: "withdrawInquiry",
        call: () => client.withdrawInquiry(1),
        defaultMessage: "お問い合わせの取り下げに失敗しました",
        urlIncludes: "/api/v1/inquiries/1/withdrawal",
        method: "POST",
      },
      {
        name: "getAdminInquiryList",
        call: () => client.getAdminInquiryList(1, "unreplied"),
        defaultMessage: "お問い合わせ一覧の取得に失敗しました",
        urlIncludes: "/api/v1/admin/inquiries?",
        method: "GET",
      },
      {
        name: "getAdminInquiryDetail",
        call: () => client.getAdminInquiryDetail(1),
        defaultMessage: "お問い合わせ詳細の取得に失敗しました",
        urlIncludes: "/api/v1/admin/inquiries/1",
        method: "GET",
      },
      {
        name: "replyToInquiry",
        call: () => client.replyToInquiry(1, "返信本文"),
        defaultMessage: "返信の登録に失敗しました",
        urlIncludes: "/api/v1/admin/inquiries/1/replies",
        method: "POST",
      },
    ];

    it.each(cases)(
      "$name: 成功時はJSONを返し、期待するURL・メソッドで呼び出す",
      async ({ call, urlIncludes, method }) => {
        fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
        const result = await call();
        expect(result).toEqual({ ok: true });

        const lastCall = fetchMock.mock.calls.at(-1)!;
        expect(String(lastCall[0])).toContain(urlIncludes);
        expect(lastCall[1]?.method ?? "GET").toBe(method);
      }
    );

    it.each(cases)(
      "$name: エラー時は既定メッセージを投げる",
      async ({ call, defaultMessage }) => {
        fetchMock.mockResolvedValueOnce(makeResponse({}, { status: 500 }));
        await expect(call()).rejects.toThrow(defaultMessage);
      }
    );

    it("getAdminInquiryList はstatusKbn省略時にクエリへ含めない", async () => {
      fetchMock.mockResolvedValueOnce(makeResponse({ ok: true }));
      await client.getAdminInquiryList(1);
      const lastCall = fetchMock.mock.calls.at(-1)!;
      expect(String(lastCall[0])).not.toContain("statusKbn");
    });
  });
});
