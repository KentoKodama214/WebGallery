import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { AccountSettingForm } from "../AccountSettingForm";

// モック
const mockLogout = jest.fn();
const mockPush = jest.fn();
const mockReplace = jest.fn();
const mockSetNextLogoutDestination = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
}));

jest.mock("@/lib/auth/loginRedirect", () => ({
  setNextLogoutDestination: (...args: unknown[]) =>
    mockSetNextLogoutDestination(...args),
  consumeNextLogoutDestination: jest.fn(),
}));

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    user: { accountId: "testuser1" },
    isAuthenticated: true,
    isLoading: false,
    logout: mockLogout,
  }),
}));

// パスキー管理セクションは独自に API を呼ぶため、このテストではスタブに差し替える
// （セクション自体の挙動は PasskeySection.test.tsx で検証する）
jest.mock("../PasskeySection", () => ({
  PasskeySection: ({ accountId }: { accountId: string }) => (
    <div data-testid="passkey-section">{accountId}</div>
  ),
}));

// リカバリーコード管理セクションも同様にスタブへ差し替える（RecoveryCodeSection.test.tsx で検証する）
jest.mock("../RecoveryCodeSection", () => ({
  RecoveryCodeSection: ({ accountId }: { accountId: string }) => (
    <div data-testid="recovery-code-section">{accountId}</div>
  ),
}));

// 退会前のパスキーによる本人確認（navigator.credentials.get を伴う）はスタブに差し替える
const mockReauthenticate = jest.fn();
jest.mock("@/lib/auth/reauthenticate", () => ({
  reauthenticateWithPasskey: (...args: unknown[]) => mockReauthenticate(...args),
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
    mockGetAccount.mockResolvedValue(mockAccountData);
    mockReauthenticate.mockResolvedValue('{"id":"reauth"}');
    mockGetPrefectures.mockResolvedValue(mockPrefectureData);
  });

  it("アカウント設定フォームが正しくレンダリングされること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    expect(screen.getByDisplayValue("testuser1")).toBeInTheDocument();
    expect(screen.getByDisplayValue("テストユーザー")).toBeInTheDocument();
    expect(screen.getByDisplayValue("2000-01-01")).toBeInTheDocument();
    expect(screen.getByDisplayValue("テストメモ")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "登録" })).toBeInTheDocument();
    expect(screen.getByText("← back")).toBeInTheDocument();
  });

  it("パスワードの入力欄を持たないこと", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await screen.findByRole("heading", { name: "アカウント設定" });
    expect(screen.queryByLabelText("現在のパスワード")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("新しいパスワード")).not.toBeInTheDocument();
  });

  it("パスキー管理・リカバリーコード管理のセクションが対象アカウントIDで配置されること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    expect(await screen.findByTestId("passkey-section")).toHaveTextContent("testuser1");
    expect(screen.getByTestId("recovery-code-section")).toHaveTextContent("testuser1");
  });

  it("都道府県が正しく表示されること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    // 出身地と居住地の2つのselectにそれぞれ同じ都道府県が表示される
    expect(screen.getAllByText("北海道")).toHaveLength(2);
    expect(screen.getAllByText("青森県")).toHaveLength(2);
    expect(screen.getAllByText("東京都")).toHaveLength(2);
  });

  it("アカウントIDフィールドが無効化されていること", async () => {
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    const accountIdInput = screen.getByDisplayValue("testuser1");
    expect(accountIdInput).toBeDisabled();
  });

  it("更新成功時にモーダルが表示されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: false,
      isAccountIdChanged: false,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByText("アカウント情報を更新しました")).toBeInTheDocument();
    });
  });

  it("アカウント削除は「はい」でパスキーによる本人確認を行ってから実行され、ログアウトされること", async () => {
    mockDeleteAccount.mockResolvedValue(undefined);

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = within(screen.getByRole("dialog"));
    // パスワードの入力欄は無く、パスキーで本人確認する旨を案内する
    expect(dialog.queryByLabelText("現在のパスワード")).not.toBeInTheDocument();
    expect(dialog.getByText(/本人確認のためにパスキー/)).toBeInTheDocument();

    await user.click(dialog.getByRole("button", { name: "はい" }));

    await waitFor(() => {
      expect(mockReauthenticate).toHaveBeenCalledWith("testuser1");
      expect(mockDeleteAccount).toHaveBeenCalledWith("testuser1", '{"id":"reauth"}');
      expect(mockLogout).toHaveBeenCalled();
    });
    // 退避元（本人専用ページ）を redirect クエリに載せず、削除完了ページへ退避させる
    expect(mockSetNextLogoutDestination).toHaveBeenCalledWith("/login?deleted=1");
    // logout 成功時の /login 遷移は AuthGuard が担うため、ここでは replace しない
    expect(mockReplace).not.toHaveBeenCalled();
    // 退避するまでの繋ぎとして削除完了表示になり、権限エラーは出さない
    expect(await screen.findByText("アカウントを削除しました")).toBeInTheDocument();
    expect(
      screen.queryByText("この操作を行う権限がありません")
    ).not.toBeInTheDocument();
  });

  it("本人確認でパスキーの操作をキャンセルした場合は、エラーを表示せず削除もしないこと", async () => {
    const { PasskeyCancelledError } = jest.requireActual("@/lib/auth/passkey");
    mockReauthenticate.mockRejectedValueOnce(new PasskeyCancelledError());

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);
    await screen.findByRole("heading", { name: "アカウント設定" });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = within(screen.getByRole("dialog"));
    await user.click(dialog.getByRole("button", { name: "はい" }));

    await waitFor(() => {
      expect(dialog.getByRole("button", { name: "はい" })).toBeEnabled();
    });
    expect(dialog.queryByRole("alert")).not.toBeInTheDocument();
    expect(mockDeleteAccount).not.toHaveBeenCalled();
  });

  it("本人確認に失敗した場合は、ダイアログを開いたままエラーを表示すること", async () => {
    mockDeleteAccount.mockRejectedValueOnce(
      new ApiError("パスキーによる本人確認に失敗しました。", 403)
    );

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);
    await screen.findByRole("heading", { name: "アカウント設定" });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = within(screen.getByRole("dialog"));
    await user.click(dialog.getByRole("button", { name: "はい" }));

    expect(await dialog.findByRole("alert")).toHaveTextContent(
      "パスキーによる本人確認に失敗しました。"
    );
    expect(mockLogout).not.toHaveBeenCalled();
  });
  it("アカウント削除の logout が失敗しても /login?deleted=1 へ遷移すること", async () => {
    mockDeleteAccount.mockResolvedValue(undefined);
    mockLogout.mockRejectedValueOnce(new Error("network"));

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = within(screen.getByRole("dialog"));
    await user.click(dialog.getByRole("button", { name: "はい" }));

    await waitFor(() => {
      expect(mockReplace).toHaveBeenCalledWith("/login?deleted=1");
    });
  });

  it("アカウントID重複時にエラーメッセージがrole=alertで表示されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: true,
      isAccountIdChanged: true,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "このアカウントIDは既に使われています"
      );
    });
  });

  it("同一のエラーメッセージが連続発生しても通知要素が再マウントされ、スクリーンリーダーへ再通知されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: true,
      isAccountIdChanged: true,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));
    const firstAlert = await waitFor(() => screen.getByRole("alert"));
    expect(firstAlert).toHaveTextContent("このアカウントIDは既に使われています");

    // 同一の文言のエラーを再度発生させる。React が同一文字列のテキストノード更新を
    // 省略しても、DOM 要素自体が再マウントされる（= key が変わる）ことを確認する
    await user.click(screen.getByRole("button", { name: "登録" }));
    await waitFor(() => {
      expect(screen.getByRole("alert")).not.toBe(firstAlert);
    });
  });

  it("アカウント名が空の場合にバリデーションエラーが表示されること", async () => {
    mockGetAccount.mockResolvedValue({
      ...mockAccountData,
      accountName: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウント名を入力してください")
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
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });
  });

  it("他人のアカウント設定ページを開いた場合は権限エラーを表示すること", async () => {
    render(<AccountSettingForm accountId="otheruser1" />);

    await waitFor(() => {
      expect(screen.getByText("この操作を行う権限がありません")).toBeInTheDocument();
    });
    expect(mockGetAccount).not.toHaveBeenCalled();
  });

  it("生年月日が未来日の場合にonChangeでバリデーションエラーが表示され、修正で消えること", async () => {
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    const birthdateInput = screen.getByLabelText("生年月日");
    const futureYear = new Date().getFullYear() + 1;
    await user.clear(birthdateInput);
    await user.type(birthdateInput, `${futureYear}-01-01`);

    await waitFor(() => {
      expect(screen.getByText("過去の日付を入力してください")).toBeInTheDocument();
    });

    await user.clear(birthdateInput);
    await user.type(birthdateInput, "2000-01-01");

    await waitFor(() => {
      expect(screen.queryByText("過去の日付を入力してください")).not.toBeInTheDocument();
    });
  });

  it("アカウント名のフォーカスアウトで空ならエラー、入力すれば消えること", async () => {
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    const nameInput = screen.getByLabelText("アカウント名");
    await user.clear(nameInput);
    await user.tab();

    await waitFor(() => {
      expect(screen.getByText("アカウント名を入力してください")).toBeInTheDocument();
    });

    await user.type(nameInput, "新しい名前");
    await user.tab();

    await waitFor(() => {
      expect(screen.queryByText("アカウント名を入力してください")).not.toBeInTheDocument();
    });
  });

  it("性別・出身地・居住地・メモを変更すると更新APIへ反映されること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: false,
      isAccountIdChanged: false,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText("性別"), "woman");
    await user.selectOptions(screen.getByLabelText("出身地"), "Aomori");
    await user.selectOptions(screen.getByLabelText("居住地"), "Tokyo");
    const memoInput = screen.getByLabelText("メモ");
    await user.clear(memoInput);
    await user.type(memoInput, "更新後のメモ");

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(mockUpdateAccount).toHaveBeenCalledWith(
        "testuser1",
        expect.objectContaining({
          sexKbn: "woman",
          birthplacePrefectureKbnCode: "Aomori",
          residentPrefectureKbnCode: "Tokyo",
          freeMemo: "更新後のメモ",
        })
      );
    });
  });

  it("削除確認モーダルの「いいえ」ボタンで削除を中止すること", async () => {
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    expect(screen.getByRole("dialog", { name: "アカウント削除の確認" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "いいえ" }));

    await waitFor(() => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });
    expect(mockDeleteAccount).not.toHaveBeenCalled();
  });

  it("削除確認モーダルをEscapeキーで閉じられ、表示中のエラーがリセットされること", async () => {
    mockDeleteAccount.mockRejectedValueOnce(
      new ApiError("パスキーによる本人確認に失敗しました。", 403)
    );
    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const dialog = screen.getByRole("dialog", { name: "アカウント削除の確認" });
    await user.click(within(dialog).getByRole("button", { name: "はい" }));
    await within(dialog).findByRole("alert");
    await user.keyboard("{Escape}");

    await waitFor(() => {
      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });

    // 再度開いたとき、前回のエラーが破棄されていること
    await user.click(screen.getByRole("button", { name: "アカウント削除" }));
    const reopenedDialog = screen.getByRole("dialog", { name: "アカウント削除の確認" });
    expect(within(reopenedDialog).queryByRole("alert")).not.toBeInTheDocument();
  });
  it("更新完了モーダルの閉じるボタンでモーダルを閉じられること", async () => {
    mockUpdateAccount.mockResolvedValue({
      httpStatus: 200,
      isDuplicateAccountId: false,
      isAccountIdChanged: false,
      message: "",
    });

    const user = userEvent.setup();
    render(<AccountSettingForm accountId="testuser1" />);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "アカウント設定" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByText("アカウント情報を更新しました")).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "閉じる" }));

    await waitFor(() => {
      expect(screen.queryByText("アカウント情報を更新しました")).not.toBeInTheDocument();
    });
  });
});
