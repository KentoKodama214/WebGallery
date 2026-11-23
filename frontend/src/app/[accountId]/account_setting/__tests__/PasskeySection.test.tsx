import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { PasskeySection } from "../PasskeySection";

const mockGetPasskeys = jest.fn();
const mockCreateOptions = jest.fn();
const mockRegisterPasskey = jest.fn();
const mockRenamePasskey = jest.fn();
const mockDeletePasskey = jest.fn();
const mockCreatePasskeyCredential = jest.fn();
let mockPasskeySupported = true;

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
    getPasskeys: (...args: unknown[]) => mockGetPasskeys(...args),
    createPasskeyRegistrationOptions: (...args: unknown[]) => mockCreateOptions(...args),
    registerPasskey: (...args: unknown[]) => mockRegisterPasskey(...args),
    renamePasskey: (...args: unknown[]) => mockRenamePasskey(...args),
    deletePasskey: (...args: unknown[]) => mockDeletePasskey(...args),
  };
});

jest.mock("@/lib/auth/passkey", () => {
  const actual = jest.requireActual("@/lib/auth/passkey");
  return {
    ...actual,
    isPasskeySupported: () => mockPasskeySupported,
    createPasskeyCredential: (...args: unknown[]) => mockCreatePasskeyCredential(...args),
  };
});

const { ApiError } = jest.requireMock("@/lib/api/client") as {
  ApiError: new (message: string, status: number) => Error;
};
const { PasskeyCancelledError } = jest.requireActual("@/lib/auth/passkey") as {
  PasskeyCancelledError: new () => Error;
};

const PASSKEY_MAC = {
  passkeyNo: 1,
  passkeyName: "MacBookのTouch ID",
  isSynced: true,
  createdAt: "2026-01-01T00:00:00Z",
  lastUsedAt: "2026-01-02T00:00:00Z",
};
const PASSKEY_KEY = {
  passkeyNo: 2,
  passkeyName: "セキュリティキー",
  isSynced: false,
  createdAt: "2026-01-03T00:00:00Z",
  lastUsedAt: null,
};

describe("PasskeySection", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockPasskeySupported = true;
    mockGetPasskeys.mockResolvedValue({ passkeys: [PASSKEY_MAC, PASSKEY_KEY], maxCount: 10 });
  });

  it("登録済みのパスキーを一覧表示し、同期パスキーには印を付けること", async () => {
    render(<PasskeySection accountId="testuser1" />);

    const list = await screen.findByRole("list", { name: "登録済みのパスキー" });
    const items = within(list).getAllByRole("listitem");
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent("MacBookのTouch ID");
    expect(within(items[0]).getByText("同期")).toBeInTheDocument();
    expect(within(items[1]).queryByText("同期")).not.toBeInTheDocument();
    // 未使用のパスキーの最終使用日時は「-」と表示する
    expect(items[1]).toHaveTextContent("最終使用：-");
    expect(mockGetPasskeys).toHaveBeenCalledWith("testuser1");
  });

  it("パスキーが無い場合はその旨を表示すること", async () => {
    mockGetPasskeys.mockResolvedValueOnce({ passkeys: [], maxCount: 10 });

    render(<PasskeySection accountId="testuser1" />);

    expect(await screen.findByText("登録済みのパスキーはありません")).toBeInTheDocument();
  });

  it("一覧の取得に失敗した場合はエラーを表示し、再読み込みで再取得できること", async () => {
    mockGetPasskeys.mockRejectedValueOnce(new Error("パスキー一覧の取得に失敗しました"));
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);

    expect(await screen.findByRole("alert")).toHaveTextContent("パスキー一覧の取得に失敗しました");
    expect(screen.getByRole("button", { name: "パスキーを登録" })).toBeDisabled();

    await user.click(screen.getByRole("button", { name: "再読み込み" }));

    expect(await screen.findByRole("list", { name: "登録済みのパスキー" })).toBeInTheDocument();
    expect(mockGetPasskeys).toHaveBeenCalledTimes(2);
  });

  it("本人確認のうえ端末でパスキーを作成し、登録後に一覧を再取得すること", async () => {
    mockCreateOptions.mockResolvedValueOnce({ challenge: "c" });
    mockCreatePasskeyCredential.mockResolvedValueOnce('{"id":"cred"}');
    mockRegisterPasskey.mockResolvedValueOnce({ passkeyNo: 3, passkeyName: "iPhone" });
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "パスキーを登録" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの登録" });
    await user.type(within(dialog).getByLabelText("パスキー名"), "  iPhone  ");
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "password01");
    await user.click(within(dialog).getByRole("button", { name: "登録" }));

    expect(await screen.findByRole("status")).toHaveTextContent("パスキーを登録しました");
    expect(mockCreateOptions).toHaveBeenCalledWith("testuser1", "password01");
    expect(mockCreatePasskeyCredential).toHaveBeenCalledWith({ challenge: "c" });
    // 前後の空白を除いた名前で登録する
    expect(mockRegisterPasskey).toHaveBeenCalledWith("testuser1", '{"id":"cred"}', "iPhone");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    await waitFor(() => expect(mockGetPasskeys).toHaveBeenCalledTimes(2));
  });

  it("パスキー名・現在のパスワードが未入力の場合は、APIを呼ばずにエラーを表示すること", async () => {
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "パスキーを登録" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの登録" });
    await user.click(within(dialog).getByRole("button", { name: "登録" }));

    expect(within(dialog).getByRole("alert")).toHaveTextContent("パスキー名を入力してください");

    await user.type(within(dialog).getByLabelText("パスキー名"), "iPhone");
    await user.click(within(dialog).getByRole("button", { name: "登録" }));

    expect(within(dialog).getByRole("alert")).toHaveTextContent("現在のパスワードを入力してください");
    expect(mockCreateOptions).not.toHaveBeenCalled();
  });

  it("現在のパスワードが一致しない場合は、端末でのパスキー作成に進まずエラーを表示すること", async () => {
    mockCreateOptions.mockRejectedValueOnce(new ApiError("現在のパスワードが正しくありません。", 403));
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "パスキーを登録" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの登録" });
    await user.type(within(dialog).getByLabelText("パスキー名"), "iPhone");
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "wrong");
    await user.click(within(dialog).getByRole("button", { name: "登録" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent(
      "現在のパスワードが正しくありません。"
    );
    expect(mockCreatePasskeyCredential).not.toHaveBeenCalled();
    expect(mockRegisterPasskey).not.toHaveBeenCalled();
  });

  it("端末でのパスキー作成をキャンセルした場合は、登録せずダイアログに留まること", async () => {
    mockCreateOptions.mockResolvedValueOnce({ challenge: "c" });
    mockCreatePasskeyCredential.mockRejectedValueOnce(new PasskeyCancelledError());
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "パスキーを登録" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの登録" });
    await user.type(within(dialog).getByLabelText("パスキー名"), "iPhone");
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "password01");
    await user.click(within(dialog).getByRole("button", { name: "登録" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent(
      "パスキーの作成がキャンセルされました"
    );
    expect(mockRegisterPasskey).not.toHaveBeenCalled();
  });

  it("登録数が上限に達している場合は登録ボタンを無効にし、案内を表示すること", async () => {
    mockGetPasskeys.mockResolvedValueOnce({ passkeys: [PASSKEY_MAC, PASSKEY_KEY], maxCount: 2 });

    render(<PasskeySection accountId="testuser1" />);

    expect(await screen.findByText(/パスキーは2件まで登録できます/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "パスキーを登録" })).toBeDisabled();
  });

  it("非対応のブラウザでは登録ボタンを無効にし、案内を表示すること", async () => {
    mockPasskeySupported = false;

    render(<PasskeySection accountId="testuser1" />);

    expect(
      await screen.findByText("このブラウザはパスキーに対応していないため、登録できません")
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "パスキーを登録" })).toBeDisabled();
  });

  it("パスキー名を変更し、変更後に一覧を再取得すること", async () => {
    mockRenamePasskey.mockResolvedValueOnce(undefined);
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "MacBookのTouch IDの名前を変更" }));
    const dialog = screen.getByRole("dialog", { name: "パスキー名の変更" });
    const input = within(dialog).getByLabelText("パスキー名");
    expect(input).toHaveValue("MacBookのTouch ID");
    await user.clear(input);
    await user.type(input, "仕事用Mac");
    await user.click(within(dialog).getByRole("button", { name: "変更" }));

    expect(await screen.findByRole("status")).toHaveTextContent("パスキー名を変更しました");
    expect(mockRenamePasskey).toHaveBeenCalledWith("testuser1", 1, "仕事用Mac");
    await waitFor(() => expect(mockGetPasskeys).toHaveBeenCalledTimes(2));
  });

  it("本人確認のうえパスキーを削除し、削除後に一覧を再取得すること", async () => {
    mockDeletePasskey.mockResolvedValueOnce(undefined);
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "セキュリティキーを削除" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの削除の確認" });
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "password01");
    await user.click(within(dialog).getByRole("button", { name: "削除" }));

    expect(await screen.findByRole("status")).toHaveTextContent("パスキーを削除しました");
    expect(mockDeletePasskey).toHaveBeenCalledWith("testuser1", 2, "password01");
    await waitFor(() => expect(mockGetPasskeys).toHaveBeenCalledTimes(2));
  });

  it("削除で現在のパスワードが一致しない場合は、ダイアログにエラーを表示すること", async () => {
    mockDeletePasskey.mockRejectedValueOnce(new ApiError("現在のパスワードが正しくありません。", 403));
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "セキュリティキーを削除" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの削除の確認" });
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "wrong");
    await user.click(within(dialog).getByRole("button", { name: "削除" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent(
      "現在のパスワードが正しくありません。"
    );
  });

  it("削除対象が既に削除済み（404）の場合は、ダイアログを閉じて一覧を再取得すること", async () => {
    mockDeletePasskey.mockRejectedValueOnce(new ApiError("パスキーが存在しません。", 404));
    const user = userEvent.setup();

    render(<PasskeySection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "セキュリティキーを削除" }));
    const dialog = screen.getByRole("dialog", { name: "パスキーの削除の確認" });
    await user.type(within(dialog).getByLabelText("現在のパスワード"), "password01");
    await user.click(within(dialog).getByRole("button", { name: "削除" }));

    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    await waitFor(() => expect(mockGetPasskeys).toHaveBeenCalledTimes(2));
  });
});
