import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { RecoveryForm } from "../RecoveryForm";
import { PasskeyCancelledError } from "@/lib/auth/passkey";

const mockPush = jest.fn();
const mockGetRecoveryOptions = jest.fn();
const mockCreatePasskeyCredential = jest.fn();
const mockLoginWithRecoveryCode = jest.fn();
let mockPasskeySupported = true;

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock("@/lib/api/client", () => ({
  getRecoveryOptions: (...args: unknown[]) => mockGetRecoveryOptions(...args),
}));

jest.mock("@/lib/auth/passkey", () => {
  const actual = jest.requireActual("@/lib/auth/passkey");
  return {
    ...actual,
    isPasskeySupported: () => mockPasskeySupported,
    createPasskeyCredential: (...args: unknown[]) => mockCreatePasskeyCredential(...args),
  };
});

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    loginWithRecoveryCode: mockLoginWithRecoveryCode,
    user: null,
    isAuthenticated: false,
    isLoading: false,
  }),
}));

const OPTIONS = { challenge: "c", user: { id: "u", name: "testuser1" } };

describe("RecoveryForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockPasskeySupported = true;
    mockGetRecoveryOptions.mockResolvedValue(OPTIONS);
    mockCreatePasskeyCredential.mockResolvedValue('{"id":"new"}');
    mockLoginWithRecoveryCode.mockResolvedValue({
      accountId: "testuser1",
      accountNo: 1,
      role: "ROLE_USER",
    });
  });

  it("アカウントID・リカバリーコード・新しいパスキーの名前の入力欄と、ログインへ戻る導線を表示すること", () => {
    render(<RecoveryForm />);

    expect(screen.getByRole("heading", { name: "リカバリーコードで復旧" })).toBeInTheDocument();
    expect(screen.getByLabelText("アカウントID")).toBeInTheDocument();
    expect(screen.getByLabelText("リカバリーコード")).toBeInTheDocument();
    expect(screen.getByLabelText("新しいパスキーの名前")).toHaveValue("復旧したパスキー");
    expect(screen.getByText("← back")).toHaveAttribute("href", "/login");
  });

  it("リカバリーコードで本人確認し、新しいパスキーを作成・登録してアカウント設定へ遷移すること", async () => {
    const user = userEvent.setup();
    render(<RecoveryForm />);

    await user.type(screen.getByLabelText("アカウントID"), " testuser1 ");
    await user.type(screen.getByLabelText("リカバリーコード"), "abcd-efgh-jkmn-pqrs");
    await user.clear(screen.getByLabelText("新しいパスキーの名前"));
    await user.type(screen.getByLabelText("新しいパスキーの名前"), "新しいiPhone");
    await user.click(screen.getByRole("button", { name: "新しいパスキーを登録してログイン" }));

    await screen.findByRole("button", { name: "新しいパスキーを登録してログイン" });
    expect(mockGetRecoveryOptions).toHaveBeenCalledWith("testuser1", "abcd-efgh-jkmn-pqrs");
    expect(mockCreatePasskeyCredential).toHaveBeenCalledWith(OPTIONS);
    expect(mockLoginWithRecoveryCode).toHaveBeenCalledWith('{"id":"new"}', "新しいiPhone");
    expect(mockPush).toHaveBeenCalledWith("/testuser1/account_setting");
  });

  it("未入力の項目がある場合はエラーを表示し、APIを呼ばないこと", async () => {
    const user = userEvent.setup();
    render(<RecoveryForm />);

    await user.click(screen.getByRole("button", { name: "新しいパスキーを登録してログイン" }));

    expect(screen.getByText("アカウントIDを入力してください")).toBeInTheDocument();
    expect(screen.getByText("リカバリーコードを入力してください")).toBeInTheDocument();
    expect(mockGetRecoveryOptions).not.toHaveBeenCalled();
  });

  it("アカウントIDまたはリカバリーコードが正しくない場合は、バックエンドのメッセージを表示すること", async () => {
    mockGetRecoveryOptions.mockRejectedValueOnce(
      new Error("アカウントIDまたはリカバリーコードが正しくありません。")
    );
    const user = userEvent.setup();
    render(<RecoveryForm />);

    await user.type(screen.getByLabelText("アカウントID"), "testuser1");
    await user.type(screen.getByLabelText("リカバリーコード"), "wrong");
    await user.click(screen.getByRole("button", { name: "新しいパスキーを登録してログイン" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "アカウントIDまたはリカバリーコードが正しくありません。"
    );
    expect(mockCreatePasskeyCredential).not.toHaveBeenCalled();
    // 照合に失敗したコードは使用済みにならないため、入力を残す
    expect(screen.getByLabelText("リカバリーコード")).toHaveValue("wrong");
  });

  it("パスキーの作成をキャンセルした場合は、使用済みのコードを消し、別のコードでのやり直しを促すこと", async () => {
    mockCreatePasskeyCredential.mockRejectedValueOnce(new PasskeyCancelledError());
    const user = userEvent.setup();
    render(<RecoveryForm />);

    await user.type(screen.getByLabelText("アカウントID"), "testuser1");
    await user.type(screen.getByLabelText("リカバリーコード"), "abcd-efgh-jkmn-pqrs");
    await user.click(screen.getByRole("button", { name: "新しいパスキーを登録してログイン" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("別のコードでやり直してください");
    expect(screen.getByLabelText("リカバリーコード")).toHaveValue("");
    expect(mockLoginWithRecoveryCode).not.toHaveBeenCalled();
  });

  it("パスキー非対応のブラウザでは送信ボタンを無効にし、理由を表示すること", () => {
    mockPasskeySupported = false;
    render(<RecoveryForm />);

    expect(
      screen.getByRole("button", { name: "新しいパスキーを登録してログイン" })
    ).toBeDisabled();
    expect(
      screen.getByText("このブラウザはパスキーに対応していないため、復旧できません")
    ).toBeInTheDocument();
  });
});
