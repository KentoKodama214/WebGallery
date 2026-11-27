import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { LoginForm } from "../LoginForm";
import { PasskeyCancelledError } from "@/lib/auth/passkey";

// モック
const mockLoginWithPasskey = jest.fn();
const mockPush = jest.fn();
const mockGetPasskeyAuthenticationOptions = jest.fn();
const mockGetPasskeyCredential = jest.fn();
let mockPasskeySupported = true;

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
    getPasskeyCredential: (...args: unknown[]) => mockGetPasskeyCredential(...args),
  };
});

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    loginWithPasskey: mockLoginWithPasskey,
    user: null,
    isAuthenticated: false,
    isLoading: false,
    logout: jest.fn(),
  }),
}));

const OPTIONS = { challenge: "c", rpId: "localhost" };

describe("LoginForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockPasskeySupported = true;
    window.history.replaceState({}, "", "/login");
    mockGetPasskeyAuthenticationOptions.mockResolvedValue(OPTIONS);
    mockGetPasskeyCredential.mockResolvedValue('{"id":"cred"}');
  });

  it("パスワード欄を持たず、パスキーでのログインボタン・新規登録・復旧への導線を表示すること", () => {
    render(<LoginForm />);

    expect(screen.queryByPlaceholderText("Password")).not.toBeInTheDocument();
    expect(screen.queryByPlaceholderText("User ID")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "パスキーでログイン" })).toBeEnabled();
    expect(screen.getByRole("link", { name: "Create an account" })).toHaveAttribute(
      "href",
      "/register"
    );
    expect(
      screen.getByRole("link", { name: "パスキーを失った場合（リカバリーコードで復旧）" })
    ).toHaveAttribute("href", "/recovery");
  });

  it("ボタン押下でパスキー認証し、トークン由来のアカウントIDの写真一覧へ遷移すること", async () => {
    mockLoginWithPasskey.mockResolvedValueOnce({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
    });
    expect(mockGetPasskeyCredential).toHaveBeenCalledWith(OPTIONS);
    expect(mockLoginWithPasskey).toHaveBeenCalledWith('{"id":"cred"}');
  });

  it("安全な redirect クエリがある場合はそのパスへ遷移する", async () => {
    window.history.replaceState({}, "", "/login?redirect=%2Ftestuser1%2Faccount_setting");
    mockLoginWithPasskey.mockResolvedValueOnce({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/testuser1/account_setting");
    });
  });

  it("オープンリダイレクトを狙う redirect クエリは無視して既定の遷移先へ向かう", async () => {
    window.history.replaceState({}, "", "/login?redirect=%2F%2Fevil.example.com");
    mockLoginWithPasskey.mockResolvedValueOnce({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
    });
  });

  it("アカウント削除後（?deleted=1）は削除完了メッセージを表示する", () => {
    window.history.replaceState({}, "", "/login?deleted=1");

    render(<LoginForm />);

    expect(screen.getByRole("status")).toHaveTextContent("アカウントを削除しました");
  });

  it("パスキーの選択をキャンセルした場合はエラーを表示せず、ログインもしないこと", async () => {
    mockGetPasskeyCredential.mockRejectedValueOnce(new PasskeyCancelledError());
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "パスキーでログイン" })).toBeEnabled();
    });
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(mockLoginWithPasskey).not.toHaveBeenCalled();
  });

  it("パスキーログインが失敗した場合はメッセージを role=alert で表示すること", async () => {
    mockLoginWithPasskey.mockRejectedValueOnce(new Error("パスキーによる認証に失敗しました。"));
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "パスキーによる認証に失敗しました。"
    );
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("Errorインスタンスでない例外の場合は既定のエラーメッセージが表示されること", async () => {
    mockGetPasskeyAuthenticationOptions.mockRejectedValueOnce("unexpected");
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "パスキーによる認証に失敗しました"
    );
  });

  it("認証中はボタンが無効化され、二重に要求しないこと", async () => {
    let resolveCredential: (value: string) => void = () => {};
    mockGetPasskeyCredential.mockReturnValueOnce(
      new Promise<string>((resolve) => {
        resolveCredential = resolve;
      })
    );
    mockLoginWithPasskey.mockResolvedValueOnce({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
    const user = userEvent.setup();

    render(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "パスキーでログイン" }));

    const busy = await screen.findByRole("button", { name: "認証しています…" });
    expect(busy).toBeDisabled();
    await user.click(busy);
    expect(mockGetPasskeyAuthenticationOptions).toHaveBeenCalledTimes(1);

    resolveCredential('{"id":"cred"}');
    await waitFor(() => expect(mockPush).toHaveBeenCalled());
  });

  it("パスキー非対応のブラウザではボタンを無効にし、理由を表示すること", () => {
    mockPasskeySupported = false;

    render(<LoginForm />);

    expect(screen.getByRole("button", { name: "パスキーでログイン" })).toBeDisabled();
    expect(
      screen.getByText(/このブラウザはパスキーに対応していないため、ログインできません/)
    ).toBeInTheDocument();
  });
});
