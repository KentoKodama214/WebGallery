import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { RegisterForm } from "../RegisterForm";
import { PasskeyCancelledError } from "@/lib/auth/passkey";

// モック
const mockPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const mockRegisterAccount = jest.fn();
const mockGetSignupOptions = jest.fn();
const mockGetPrefectures = jest.fn();

jest.mock("@/lib/api/client", () => ({
  registerAccount: (...args: unknown[]) => mockRegisterAccount(...args),
  getSignupOptions: (...args: unknown[]) => mockGetSignupOptions(...args),
  getPrefectures: (...args: unknown[]) => mockGetPrefectures(...args),
}));

const mockCreatePasskeyCredential = jest.fn();
let mockPasskeySupported = true;

jest.mock("@/lib/auth/passkey", () => {
  const actual = jest.requireActual("@/lib/auth/passkey");
  return {
    ...actual,
    isPasskeySupported: () => mockPasskeySupported,
    createPasskeyCredential: (...args: unknown[]) => mockCreatePasskeyCredential(...args),
  };
});

const mockPrefectureData = [
  {
    groupName: "北海道・東北地方",
    prefectures: [
      { kbnCode: "Hokkaido", kbnJapaneseName: "北海道" },
      { kbnCode: "Aomori", kbnJapaneseName: "青森県" },
    ],
  },
  {
    groupName: "関東地方",
    prefectures: [{ kbnCode: "Tokyo", kbnJapaneseName: "東京都" }],
  },
];

const SIGNUP_OPTIONS = { challenge: "c", user: { id: "u", name: "testuser1" } };
const RECOVERY_CODES = ["ABCD-EFGH-JKMN-PQRS", "0123-4567-89AB-CDEF"];

/** フォームの表示を待つ */
async function renderForm() {
  render(<RegisterForm />);
  await waitFor(() => {
    expect(screen.getByText("Create an Account")).toBeInTheDocument();
  });
}

/** 必須項目（アカウントID・アカウント名）を入力する */
async function fillRequired(user: ReturnType<typeof userEvent.setup>, accountId = "testuser1") {
  await user.type(screen.getByLabelText("アカウントID"), accountId);
  await user.type(screen.getByLabelText("アカウント名"), "テストユーザー");
}

describe("RegisterForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockPasskeySupported = true;
    mockGetPrefectures.mockResolvedValue(mockPrefectureData);
    mockGetSignupOptions.mockResolvedValue(SIGNUP_OPTIONS);
    mockCreatePasskeyCredential.mockResolvedValue('{"id":"cred"}');
    mockRegisterAccount.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "",
      recoveryCodes: RECOVERY_CODES,
    });
  });

  it("パスワード欄を持たず、パスキー名の欄（初期値あり）と登録ボタンを表示すること", async () => {
    await renderForm();

    expect(screen.getByPlaceholderText("半角英数字で8〜20文字")).toBeInTheDocument();
    expect(screen.queryByLabelText("パスワード")).not.toBeInTheDocument();
    expect(screen.getByLabelText("パスキー名")).toHaveValue("最初のパスキー");
    expect(screen.getByRole("button", { name: "登録" })).toBeEnabled();
    expect(screen.getByText("← back")).toHaveAttribute("href", "/login");
  });

  it("都道府県が正しく表示されること", async () => {
    await renderForm();

    // 出身地と居住地の2つのselectにそれぞれ同じ都道府県が表示される
    expect(screen.getAllByText("北海道")).toHaveLength(2);
    expect(screen.getAllByText("青森県")).toHaveLength(2);
    expect(screen.getAllByText("東京都")).toHaveLength(2);
  });

  it("登録すると、新規登録オプション → パスキー作成 → 登録の順に呼び、リカバリーコードを表示すること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.clear(screen.getByLabelText("パスキー名"));
    await user.type(screen.getByLabelText("パスキー名"), "MacBook");
    await user.click(screen.getByRole("button", { name: "登録" }));

    const dialog = await screen.findByRole("dialog", { name: "アカウント登録完了" });
    expect(mockGetSignupOptions).toHaveBeenCalledWith("testuser1", "テストユーザー");
    expect(mockCreatePasskeyCredential).toHaveBeenCalledWith(SIGNUP_OPTIONS);
    expect(mockRegisterAccount).toHaveBeenCalledWith(
      expect.objectContaining({
        accountId: "testuser1",
        accountName: "テストユーザー",
        credential: '{"id":"cred"}',
        passkeyName: "MacBook",
      })
    );
    expect(mockRegisterAccount.mock.calls[0][0]).not.toHaveProperty("password");

    const codes = screen.getByRole("list", { name: "リカバリーコード" });
    expect(codes).toHaveTextContent("ABCD-EFGH-JKMN-PQRS");
    expect(codes).toHaveTextContent("0123-4567-89AB-CDEF");
    // 保管を確認するまで自動では遷移しない
    expect(mockPush).not.toHaveBeenCalled();
    expect(dialog).toBeInTheDocument();
  });

  it("「保管しました」を押すとログインページへ遷移すること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.click(screen.getByRole("button", { name: "登録" }));

    await user.click(await screen.findByRole("button", { name: "保管しました（ログインへ）" }));

    expect(mockPush).toHaveBeenCalledWith("/login");
  });

  it("登録成功後はフォームを再送信できないこと", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);

    const submitButton = screen.getByRole("button", { name: "登録" });
    await user.click(submitButton);
    await screen.findByRole("dialog", { name: "アカウント登録完了" });

    expect(submitButton).toBeDisabled();
    expect(mockRegisterAccount).toHaveBeenCalledTimes(1);
  });

  it("アカウントID重複時はバックエンドのメッセージを表示し、パスキーを作成しないこと", async () => {
    mockGetSignupOptions.mockRejectedValueOnce(
      new Error("このアカウントIDは既に使われています。")
    );
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "このアカウントIDは既に使われています。"
    );
    expect(mockCreatePasskeyCredential).not.toHaveBeenCalled();
    expect(mockRegisterAccount).not.toHaveBeenCalled();
  });

  it("パスキーの作成をキャンセルした場合は、やり直しを促すメッセージを表示し登録しないこと", async () => {
    mockCreatePasskeyCredential.mockRejectedValueOnce(new PasskeyCancelledError());
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "パスキーの作成がキャンセルされました"
    );
    expect(mockRegisterAccount).not.toHaveBeenCalled();
    expect(screen.getByRole("button", { name: "登録" })).toBeEnabled();
  });

  it("アカウントIDが不正な形式の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user, "short");
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(
      await screen.findByText("半角英数字で8〜20文字で入力してください")
    ).toBeInTheDocument();
    expect(mockGetSignupOptions).not.toHaveBeenCalled();
  });

  it("アカウント名が空の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await user.type(screen.getByLabelText("アカウントID"), "testuser1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(await screen.findByText("アカウント名を入力してください")).toBeInTheDocument();
    expect(mockGetSignupOptions).not.toHaveBeenCalled();
  });

  it("パスキー名が空の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.clear(screen.getByLabelText("パスキー名"));
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(await screen.findByText("パスキー名を入力してください")).toBeInTheDocument();
    expect(mockGetSignupOptions).not.toHaveBeenCalled();
  });

  it("API呼び出し失敗時にエラーメッセージが表示されること", async () => {
    mockRegisterAccount.mockRejectedValueOnce(new Error("アカウントの登録に失敗しました"));
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("アカウントの登録に失敗しました");
  });

  it("アカウント名のフォーカスアウトで空ならエラー、入力すれば消えること", async () => {
    const user = userEvent.setup();
    await renderForm();

    const accountNameInput = screen.getByLabelText("アカウント名");
    await user.click(accountNameInput);
    await user.tab();
    expect(await screen.findByText("アカウント名を入力してください")).toBeInTheDocument();

    await user.type(accountNameInput, "テストユーザー");
    await user.tab();
    await waitFor(() => {
      expect(screen.queryByText("アカウント名を入力してください")).not.toBeInTheDocument();
    });
  });

  it("性別・出身地・居住地・メモを入力すると登録APIへ反映されること", async () => {
    const user = userEvent.setup();
    await renderForm();
    await fillRequired(user);

    await user.selectOptions(screen.getByLabelText("性別"), "woman");
    await user.selectOptions(screen.getByLabelText("出身地"), "Aomori");
    await user.selectOptions(screen.getByLabelText("居住地"), "Tokyo");
    await user.type(screen.getByLabelText("メモ"), "よろしくお願いします");

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(mockRegisterAccount).toHaveBeenCalledWith(
        expect.objectContaining({
          sexKbn: "woman",
          birthplacePrefectureKbnCode: "Aomori",
          residentPrefectureKbnCode: "Tokyo",
          freeMemo: "よろしくお願いします",
        })
      );
    });
  });

  it("パスキー非対応のブラウザでは登録ボタンを無効にし、理由を表示すること", async () => {
    mockPasskeySupported = false;
    await renderForm();

    expect(screen.getByRole("button", { name: "登録" })).toBeDisabled();
    expect(
      screen.getByText("このブラウザはパスキーに対応していないため、アカウントを登録できません")
    ).toBeInTheDocument();
  });
});
