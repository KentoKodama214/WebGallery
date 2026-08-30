import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { RegisterForm } from "../RegisterForm";

// モック
const mockPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const mockRegisterAccount = jest.fn();
const mockGetPrefectures = jest.fn();

jest.mock("@/lib/api/client", () => ({
  registerAccount: (...args: unknown[]) => mockRegisterAccount(...args),
  getPrefectures: (...args: unknown[]) => mockGetPrefectures(...args),
}));

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

describe("RegisterForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    mockGetPrefectures.mockResolvedValue(mockPrefectureData);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("登録フォームが正しくレンダリングされること", async () => {
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    expect(screen.getByPlaceholderText("半角英数字で8〜16文字")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "登録" })).toBeInTheDocument();
    expect(screen.getByText("← back")).toBeInTheDocument();
  });

  it("backリンクが/loginを指していること", async () => {
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const backLink = screen.getByText("← back");
    expect(backLink).toHaveAttribute("href", "/login");
  });

  it("都道府県が正しく表示されること", async () => {
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    // 出身地と居住地の2つのselectにそれぞれ同じ都道府県が表示される
    expect(screen.getAllByText("北海道")).toHaveLength(2);
    expect(screen.getAllByText("青森県")).toHaveLength(2);
    expect(screen.getAllByText("東京都")).toHaveLength(2);
  });

  it("アカウントIDフィールドが入力可能であること", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const accountIdInput = screen.getByPlaceholderText("半角英数字で8〜16文字");
    expect(accountIdInput).not.toBeDisabled();
    await user.type(accountIdInput, "testuser1");
    expect(accountIdInput).toHaveValue("testuser1");
  });

  it("登録成功時にモーダルが表示され5秒後にログインページへ遷移すること", async () => {
    mockRegisterAccount.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "",
    });

    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const textInputs = screen.getAllByRole("textbox");
    const accountNameInput = textInputs[1]; // 2番目のテキスト入力がアカウント名

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "testuser1");
    await user.type(accountNameInput, "テストユーザー");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "password1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByText("アカウントを登録しました。ログインページへ移動します。")).toBeInTheDocument();
    });

    jest.advanceTimersByTime(5000);

    expect(mockPush).toHaveBeenCalledWith("/login");
  });

  it("アカウントID重複時にエラーメッセージが表示されること", async () => {
    mockRegisterAccount.mockResolvedValue({
      httpStatus: 200,
      isSuccess: false,
      message: "",
    });

    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const textInputs = screen.getAllByRole("textbox");
    const accountNameInput = textInputs[1];

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "testuser1");
    await user.type(accountNameInput, "テストユーザー");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "password1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("このアカウントIDは既に使われています")
      ).toBeInTheDocument();
    });
  });

  it("アカウントIDが不正な形式の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const textInputs = screen.getAllByRole("textbox");
    const accountNameInput = textInputs[1];

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "short");
    await user.type(accountNameInput, "テスト");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "password1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("半角英数字で8〜16文字で入力してください")
      ).toBeInTheDocument();
    });

    expect(mockRegisterAccount).not.toHaveBeenCalled();
  });

  it("アカウント名が空の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "testuser1");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "password1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウント名を入力してください")
      ).toBeInTheDocument();
    });

    expect(mockRegisterAccount).not.toHaveBeenCalled();
  });

  it("パスワードが不正な形式の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const textInputs = screen.getAllByRole("textbox");
    const accountNameInput = textInputs[1];

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "testuser1");
    await user.type(accountNameInput, "テスト");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "short");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("英字と数字を含む半角8〜72文字で入力してください")
      ).toBeInTheDocument();
    });

    expect(mockRegisterAccount).not.toHaveBeenCalled();
  });

  it("API呼び出し失敗時にエラーメッセージが表示されること", async () => {
    mockRegisterAccount.mockRejectedValue(new Error("アカウントの登録に失敗しました"));

    const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
    render(<RegisterForm />);

    await waitFor(() => {
      expect(screen.getByText("Create an Account")).toBeInTheDocument();
    });

    const textInputs = screen.getAllByRole("textbox");
    const accountNameInput = textInputs[1];

    await user.type(screen.getByPlaceholderText("半角英数字で8〜16文字"), "testuser1");
    await user.type(accountNameInput, "テストユーザー");
    await user.type(screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"), "password1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウントの登録に失敗しました")
      ).toBeInTheDocument();
    });
  });
});
