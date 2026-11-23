import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { LoginForm } from "../LoginForm";

// モック
const mockLogin = jest.fn();
const mockLoginWithPasskey = jest.fn();
const mockPush = jest.fn();
const mockGetPasskeyAuthenticationOptions = jest.fn();
const mockGetPasskeyCredential = jest.fn();
// 既存のテストに影響しないよう、既定はパスキー非対応のブラウザとして扱う
let mockPasskeySupported = false;
let mockConditionalAvailable = false;

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock("@/lib/api/client", () => ({
  getPasskeyAuthenticationOptions: (...args: unknown[]) =>
    mockGetPasskeyAuthenticationOptions(...args),
}));

jest.mock("@/lib/auth/passkey", () => {
  const actual = jest.requireActual("@/lib/auth/passkey");
  return {
    ...actual,
    isPasskeySupported: () => mockPasskeySupported,
    isConditionalMediationAvailable: async () => mockConditionalAvailable,
    getPasskeyCredential: (...args: unknown[]) => mockGetPasskeyCredential(...args),
  };
});

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    login: mockLogin,
    loginWithPasskey: mockLoginWithPasskey,
    user: null,
    isAuthenticated: false,
    isLoading: false,
    logout: jest.fn(),
  }),
}));

describe("LoginForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("ログインフォームが正しくレンダリングされること", () => {
    render(<LoginForm />);

    expect(screen.getByPlaceholderText("User ID")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log in" })).toBeInTheDocument();
    expect(screen.getByText("Create an account")).toBeInTheDocument();
  });

  it("ログイン成功時にトークン由来のアカウントIDでリダイレクトされること", async () => {
    // 入力値は "TestUser1" だが、遷移先はトークンの sub（"testuser1"）を採用する
    mockLogin.mockResolvedValueOnce({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "TestUser1");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith("TestUser1", "password1");
      expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
    });
  });

  it("安全な redirect クエリがある場合はそのパスへ遷移する", async () => {
    window.history.replaceState(
      {},
      "",
      "/login?redirect=%2Faaaa1111%2Faccount_setting"
    );
    mockLogin.mockResolvedValueOnce({
      accountId: "aaaa1111",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "aaaa1111");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/aaaa1111/account_setting");
    });
    window.history.replaceState({}, "", "/login");
  });

  it("オープンリダイレクトを狙う redirect クエリは無視して既定の遷移先へ向かう", async () => {
    window.history.replaceState(
      {},
      "",
      "/login?redirect=" + encodeURIComponent("/..//evil.com")
    );
    mockLogin.mockResolvedValueOnce({
      accountId: "aaaa1111",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "aaaa1111");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/photo/aaaa1111/photo_list");
    });
    expect(mockPush).not.toHaveBeenCalledWith("//evil.com");
    window.history.replaceState({}, "", "/login");
  });

  it("アカウント削除後（?deleted=1）は削除完了メッセージを表示する", async () => {
    window.history.replaceState({}, "", "/login?deleted=1");

    render(<LoginForm />);

    expect(
      await screen.findByText("アカウントを削除しました")
    ).toBeInTheDocument();
    window.history.replaceState({}, "", "/login");
  });

  it("ログイン失敗時にエラーメッセージが表示されること", async () => {
    mockLogin.mockRejectedValueOnce(
      new Error("アカウントIDまたはパスワードが間違っています。")
    );
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "wrongpass");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウントIDまたはパスワードが間違っています。")
      ).toBeInTheDocument();
    });
  });

  it("Errorインスタンスでない例外の場合は既定のエラーメッセージが表示されること", async () => {
    mockLogin.mockRejectedValueOnce("network down");
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(screen.getByText("ログインに失敗しました")).toBeInTheDocument();
    });
  });

  it("送信中はボタンが無効化されること", async () => {
    mockLogin.mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 1000))
    );
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(screen.getByRole("button")).toBeDisabled();
  });

  describe("パスキーでのログイン", () => {
    const { PasskeyCancelledError } = jest.requireActual("@/lib/auth/passkey") as {
      PasskeyCancelledError: new () => Error;
    };
    const OPTIONS = { challenge: "c", rpId: "localhost", allowCredentials: [] };

    beforeEach(() => {
      mockPasskeySupported = true;
      mockConditionalAvailable = false;
      window.history.replaceState(null, "", "/login");
      mockGetPasskeyAuthenticationOptions.mockResolvedValue(OPTIONS);
    });

    afterEach(() => {
      mockPasskeySupported = false;
      mockConditionalAvailable = false;
    });

    it("非対応のブラウザでは「パスキーでログイン」ボタンを表示しないこと", () => {
      mockPasskeySupported = false;

      render(<LoginForm />);

      expect(screen.queryByRole("button", { name: "パスキーでログイン" })).not.toBeInTheDocument();
    });

    it("ユーザー名欄はオートフィルでパスキーを選べるよう autocomplete に webauthn を含むこと", () => {
      render(<LoginForm />);

      expect(screen.getByPlaceholderText("User ID")).toHaveAttribute(
        "autocomplete",
        "username webauthn"
      );
    });

    it("ボタン押下でパスキー認証し、トークン由来のアカウントIDの画面へ遷移すること", async () => {
      mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"cred"}');
      mockLoginWithPasskey.mockResolvedValueOnce({
        accountId: "testuser1",
        accountNo: 1,
        role: "ROLE_USER",
      });
      const user = userEvent.setup();

      render(<LoginForm />);
      await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

      await waitFor(() => {
        expect(mockLoginWithPasskey).toHaveBeenCalledWith('{"id":"cred"}');
        expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
      });
      expect(mockGetPasskeyCredential).toHaveBeenCalledWith(OPTIONS, {
        conditional: false,
        signal: undefined,
      });
      expect(mockLogin).not.toHaveBeenCalled();
    });

    it("パスキーの選択をキャンセルした場合はエラーを表示せず、ログインもしないこと", async () => {
      mockGetPasskeyCredential.mockRejectedValueOnce(new PasskeyCancelledError());
      const user = userEvent.setup();

      render(<LoginForm />);
      await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

      await waitFor(() => expect(mockGetPasskeyCredential).toHaveBeenCalled());
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
      expect(mockLoginWithPasskey).not.toHaveBeenCalled();
      expect(mockPush).not.toHaveBeenCalled();
    });

    it("パスキーログインが失敗した場合はメッセージを role=alert で表示すること", async () => {
      mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"cred"}');
      mockLoginWithPasskey.mockRejectedValueOnce(new Error("アカウントがロックされています。"));
      const user = userEvent.setup();

      render(<LoginForm />);
      await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

      expect(await screen.findByRole("alert")).toHaveTextContent("アカウントがロックされています。");
      expect(mockPush).not.toHaveBeenCalled();
    });

    it("オートフィルに対応している場合、画面表示時にオートフィルからのパスキー選択を待ち受けること", async () => {
      mockConditionalAvailable = true;
      mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"autofill"}');
      mockLoginWithPasskey.mockResolvedValueOnce({
        accountId: "testuser1",
        accountNo: 1,
        role: "ROLE_USER",
      });

      render(<LoginForm />);

      await waitFor(() => {
        expect(mockLoginWithPasskey).toHaveBeenCalledWith('{"id":"autofill"}');
        expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
      });
      const [, options] = mockGetPasskeyCredential.mock.calls[0];
      expect(options.conditional).toBe(true);
      expect(options.signal).toBeInstanceOf(AbortSignal);
    });

    it("オートフィルの待ち受けを開始できなかった場合は、エラーを表示しないこと", async () => {
      mockConditionalAvailable = true;
      mockGetPasskeyCredential.mockRejectedValueOnce(new Error("パスキーによる認証に失敗しました"));

      render(<LoginForm />);

      await waitFor(() => expect(mockGetPasskeyCredential).toHaveBeenCalled());
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
      expect(mockLoginWithPasskey).not.toHaveBeenCalled();
    });

    it("オートフィルで選んだパスキーでのログインが失敗した場合は、メッセージを表示すること", async () => {
      mockConditionalAvailable = true;
      mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"autofill"}');
      mockLoginWithPasskey.mockRejectedValueOnce(new Error("パスキーによる認証に失敗しました。"));

      render(<LoginForm />);

      expect(await screen.findByRole("alert")).toHaveTextContent("パスキーによる認証に失敗しました。");
    });

    it("オートフィルの待ち受け中にボタンを押すと、待ち受けを中止してからパスキーを要求すること", async () => {
      mockConditionalAvailable = true;
      // オートフィルの待ち受けは中止されるまで解決しない
      mockGetPasskeyCredential.mockImplementationOnce(
        (_options: unknown, { signal }: { signal: AbortSignal }) =>
          new Promise((_resolve, reject) => {
            signal.addEventListener("abort", () => reject(new PasskeyCancelledError()));
          })
      );
      mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"modal"}');
      mockLoginWithPasskey.mockResolvedValueOnce({
        accountId: "testuser1",
        accountNo: 1,
        role: "ROLE_USER",
      });
      const user = userEvent.setup();

      render(<LoginForm />);
      await waitFor(() => expect(mockGetPasskeyCredential).toHaveBeenCalledTimes(1));
      const conditionalSignal = mockGetPasskeyCredential.mock.calls[0][1].signal as AbortSignal;

      await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

      await waitFor(() => expect(mockLoginWithPasskey).toHaveBeenCalledWith('{"id":"modal"}'));
      expect(conditionalSignal.aborted).toBe(true);
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
  });
});
