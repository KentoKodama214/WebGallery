import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { AccountSettingForm } from "../AccountSettingForm";

// モック
const mockLogout = jest.fn();
const mockPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    login: jest.fn(),
    user: { accountId: "testuser1" },
    isAuthenticated: true,
    isLoading: false,
    logout: mockLogout,
  }),
}));

const mockGetAccount = jest.fn();
const mockUpdateAccount = jest.fn();
const mockGetPrefectures = jest.fn();
const mockDeleteAccount = jest.fn();

jest.mock("@/lib/api/client", () => {
  class ApiError extends Error {
    status: number;
    constructor(message: string, status: number) {
      super(message);
      this.name = "ApiError";
      this.status = status;
    }
  }
  return {
    ApiError,
    getAccount: (...args: unknown[]) => mockGetAccount(...args),
    updateAccount: (...args: unknown[]) => mockUpdateAccount(...args),
    getPrefectures: (...args: unknown[]) => mockGetPrefectures(...args),
    deleteAccount: (...args: unknown[]) => mockDeleteAccount(...args),
  };
});

// テストからも同じ形の ApiError を投げられるように再取得する
const { ApiError } = jest.requireMock("@/lib/api/client") as {
  ApiError: new (message: string, status: number) => Error;
};

const mockAccountData = {
  accountId: "testuser1",
  accountName: "テストユーザー",
  birthdate: "2000-01-01",
  sexKbn: "man",
  birthplacePrefectureKbnCode: "Hokkaido",
  residentPrefectureKbnCode: "Tokyo",
  freeMemo: "テストメモ",
};

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

describe("AccountSettingForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    window.sessionStorage.clear();
    mockGetAccount.mockResolvedValue(mockAccountData);
    mockGetPrefectures.mockResolvedValue(mockPrefectureData);
  });

  it("アカウント設定フォームが正しくレンダリングされること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    expect(screen.getByDisplayValue("testuser1")).toBeInTheDocument();
    expect(screen.getByDisplayValue("テストユーザー")).toBeInTheDocument();
    expect(screen.getByDisplayValue("2000-01-01")).toBeInTheDocument();
    expect(screen.getByDisplayValue("テストメモ")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "登録" })).toBeInTheDocument();
    expect(screen.getByText("← back")).toBeInTheDocument();
  });

  it("都道府県が正しく表示されること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    // 出身地と居住地の2つのselectにそれぞれ同じ都道府県が表示される
    expect(screen.getAllByText("北海道")).toHaveLength(2);
    expect(screen.getAllByText("青森県")).toHaveLength(2);
    expect(screen.getAllByText("東京都")).toHaveLength(2);
  });

  it("アカウントIDフィールドが無効化されていること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    const accountIdInput = screen.getByDisplayValue("testuser1");
    expect(accountIdInput).toBeDisabled();
  });

  it("更新成功時にモーダルが表示されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: false,
      isAccountIdChanged: false,
      isPasswordChanged: false,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByText("アカウントを登録しました")).toBeInTheDocument();
    });
  });

  it("パスワード変更時にログアウトしてログインページへリダイレクトされること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: false,
      isAccountIdChanged: false,
      isPasswordChanged: true,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    const passwordInput = screen.getByPlaceholderText("英字と数字を含む半角8〜72文字");
    await user.type(passwordInput, "newpassword1");
    await user.type(screen.getByLabelText("現在のパスワード"), "oldpassword1");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalled();
      expect(mockPush).toHaveBeenCalledWith("/login");
    });
    expect(mockUpdateAccount).toHaveBeenCalledWith(
      "testuser1",
      expect.objectContaining({ newPassword: "newpassword1", currentPassword: "oldpassword1" })
    );
  });

  it("新しいパスワード入力時に現在のパスワードが未入力だとエラーになり送信されないこと", async () => {
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    await user.type(
      screen.getByPlaceholderText("英字と数字を含む半角8〜72文字"),
      "newpassword1"
    );
    await user.click(screen.getByRole("button", { name: "登録" }));

    expect(
      await screen.findByText("現在のパスワードを入力してください")
    ).toBeInTheDocument();
    expect(mockUpdateAccount).not.toHaveBeenCalled();
  });

  it("アカウント削除は現在のパスワード入力後に実行され、ログアウトされること", async () => {
    mockDeleteAccount.mockResolvedValue(undefined);

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = within(screen.getByRole("dialog"));
    // パスワード未入力で「はい」を押すとエラーになり、削除は呼ばれない
    await user.click(dialog.getByRole("button", { name: "はい" }));
    expect(
      await dialog.findByText("現在のパスワードを入力してください")
    ).toBeInTheDocument();
    expect(mockDeleteAccount).not.toHaveBeenCalled();

    await user.type(dialog.getByLabelText("現在のパスワード"), "mypassword1");
    await user.click(dialog.getByRole("button", { name: "はい" }));

    await waitFor(() => {
      expect(mockDeleteAccount).toHaveBeenCalledWith("testuser1", "mypassword1");
      expect(mockLogout).toHaveBeenCalled();
    });
  });

  it("アカウントID重複時にエラーメッセージが表示されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: true,
      isAccountIdChanged: true,
      isPasswordChanged: false,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("このアカウントIDは既に使われています")
      ).toBeInTheDocument();
    });
  });

  it("現在のパスワードの確認に連続失敗するとクールダウン表示になり、以降の送信が止まること", async () => {
    mockUpdateAccount.mockRejectedValue(
      new ApiError("現在のパスワードが正しくありません", 403)
    );

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    const passwordInput = screen.getByPlaceholderText("英字と数字を含む半角8〜72文字");
    const currentInput = screen.getByLabelText("現在のパスワード");

    for (let i = 0; i < 3; i++) {
      await user.clear(passwordInput);
      await user.type(passwordInput, "newpassword1");
      await user.clear(currentInput);
      await user.type(currentInput, "wrongpass1");
      await user.click(screen.getByRole("button", { name: "登録" }));
      await waitFor(() => {
        expect(
          screen.getByText("現在のパスワードが正しくありません")
        ).toBeInTheDocument();
      });
    }

    expect(mockUpdateAccount).toHaveBeenCalledTimes(3);
    expect(
      await screen.findByText(/しばらく待ってから再度お試しください/)
    ).toBeInTheDocument();

    // クールダウン中は送信ボタンが無効化され、API は呼ばれない
    await user.click(screen.getByRole("button", { name: "登録" }));
    expect(mockUpdateAccount).toHaveBeenCalledTimes(3);
  });

  it("再認証クールダウンは sessionStorage に保存され、再マウント後も維持されること", async () => {
    mockUpdateAccount.mockRejectedValue(
      new ApiError("現在のパスワードが正しくありません", 403)
    );

    const user = userEvent.setup();
    const { unmount } = render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    const passwordInput = screen.getByPlaceholderText("英字と数字を含む半角8〜72文字");
    const currentInput = screen.getByLabelText("現在のパスワード");

    for (let i = 0; i < 3; i++) {
      await user.clear(passwordInput);
      await user.type(passwordInput, "newpassword1");
      await user.clear(currentInput);
      await user.type(currentInput, "wrongpass1");
      await user.click(screen.getByRole("button", { name: "登録" }));
      await waitFor(() => {
        expect(
          screen.getByText("現在のパスワードが正しくありません")
        ).toBeInTheDocument();
      });
    }

    expect(
      await screen.findByText(/しばらく待ってから再度お試しください/)
    ).toBeInTheDocument();

    // 別のインスタンスとして再マウントしてもクールダウン表示が復元される
    unmount();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    expect(
      await screen.findByText(/しばらく待ってから再度お試しください/)
    ).toBeInTheDocument();
  });

  it("アカウント名が空の場合にバリデーションエラーが表示されること", async () => {
    mockGetAccount.mockResolvedValue({
      ...mockAccountData,
      accountName: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウント名を入力してください")
      ).toBeInTheDocument();
    });

    expect(mockUpdateAccount).not.toHaveBeenCalled();
  });

  it("パスワードが不正な形式の場合にバリデーションエラーが表示されること", async () => {
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });

    const passwordInput = screen.getByPlaceholderText("英字と数字を含む半角8〜72文字");
    await user.type(passwordInput, "short");
    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("英字と数字を含む半角8〜72文字で入力してください")
      ).toBeInTheDocument();
    });

    expect(mockUpdateAccount).not.toHaveBeenCalled();
  });

  it("データ取得失敗時はログインへ飛ばさず、画面内でエラーと再読み込みを表示すること", async () => {
    mockGetAccount.mockRejectedValueOnce(new Error("情報の取得に失敗しました"));

    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByText("情報の取得に失敗しました")).toBeInTheDocument();
    });
    expect(mockPush).not.toHaveBeenCalledWith("/login");
    expect(screen.getByRole("button", { name: "再読み込み" })).toBeInTheDocument();

    // 再読み込みで成功すればフォームが表示される
    mockGetAccount.mockResolvedValue(mockAccountData);
    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: "再読み込み" }));

    await waitFor(() => {
      expect(screen.getByText("Account Setting")).toBeInTheDocument();
    });
  });
});
