import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { RecoveryCodeSection } from "../RecoveryCodeSection";

const mockGetRecoveryCodeStatus = jest.fn();
const mockRegenerateRecoveryCodes = jest.fn();
const mockReauthenticate = jest.fn();

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
    getRecoveryCodeStatus: (...args: unknown[]) => mockGetRecoveryCodeStatus(...args),
    regenerateRecoveryCodes: (...args: unknown[]) => mockRegenerateRecoveryCodes(...args),
  };
});

// 本人確認（navigator.credentials.get を伴う）はスタブに差し替える
jest.mock("@/lib/auth/reauthenticate", () => ({
  reauthenticateWithPasskey: (...args: unknown[]) => mockReauthenticate(...args),
}));

const { ApiError } = jest.requireMock("@/lib/api/client") as {
  ApiError: new (message: string, status: number) => Error;
};
const { PasskeyCancelledError } = jest.requireActual("@/lib/auth/passkey") as {
  PasskeyCancelledError: new () => Error;
};

const NEW_CODES = ["ABCD-EFGH-JKMN-PQRS", "0123-4567-89AB-CDEF"];

describe("RecoveryCodeSection", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGetRecoveryCodeStatus.mockResolvedValue({ remainingCount: 8, issueCount: 10 });
    mockReauthenticate.mockResolvedValue('{"id":"reauth"}');
    mockRegenerateRecoveryCodes.mockResolvedValue(NEW_CODES);
  });

  it("未使用のリカバリーコードの残数を表示すること（コード自体は表示しない）", async () => {
    render(<RecoveryCodeSection accountId="testuser1" />);

    expect(await screen.findByText("8 / 10")).toBeInTheDocument();
    expect(screen.queryByRole("list", { name: "リカバリーコード" })).not.toBeInTheDocument();
    expect(mockGetRecoveryCodeStatus).toHaveBeenCalledWith("testuser1");
  });

  it("残数が少ない場合は再発行を促すこと", async () => {
    mockGetRecoveryCodeStatus.mockResolvedValueOnce({ remainingCount: 2, issueCount: 10 });

    render(<RecoveryCodeSection accountId="testuser1" />);

    expect(await screen.findByText("残りが少なくなっています。再発行してください")).toBeInTheDocument();
  });

  it("状態の取得に失敗した場合はエラーを表示し、再読み込みで再取得できること", async () => {
    mockGetRecoveryCodeStatus.mockRejectedValueOnce(
      new Error("リカバリーコードの状態の取得に失敗しました")
    );
    const user = userEvent.setup();

    render(<RecoveryCodeSection accountId="testuser1" />);

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "リカバリーコードの状態の取得に失敗しました"
    );
    expect(screen.getByRole("button", { name: "リカバリーコードを再発行" })).toBeDisabled();

    await user.click(screen.getByRole("button", { name: "再読み込み" }));
    expect(await screen.findByText("8 / 10")).toBeInTheDocument();
  });

  it("パスキーで本人確認のうえ再発行し、新しいコードを一度だけ表示して、閉じると残数を再取得すること", async () => {
    const user = userEvent.setup();

    render(<RecoveryCodeSection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "リカバリーコードを再発行" }));
    const confirm = screen.getByRole("dialog", { name: "リカバリーコードの再発行の確認" });
    await user.click(within(confirm).getByRole("button", { name: "再発行" }));

    const codesDialog = await screen.findByRole("dialog", { name: "新しいリカバリーコード" });
    expect(mockReauthenticate).toHaveBeenCalledWith("testuser1");
    expect(mockRegenerateRecoveryCodes).toHaveBeenCalledWith("testuser1", '{"id":"reauth"}');
    const list = within(codesDialog).getByRole("list", { name: "リカバリーコード" });
    expect(list).toHaveTextContent("ABCD-EFGH-JKMN-PQRS");
    expect(list).toHaveTextContent("0123-4567-89AB-CDEF");

    mockGetRecoveryCodeStatus.mockResolvedValueOnce({ remainingCount: 10, issueCount: 10 });
    await user.click(within(codesDialog).getByRole("button", { name: "保管しました" }));

    expect(await screen.findByRole("status")).toHaveTextContent("リカバリーコードを再発行しました");
    expect(await screen.findByText("10 / 10")).toBeInTheDocument();
    expect(screen.queryByRole("list", { name: "リカバリーコード" })).not.toBeInTheDocument();
  });

  it("本人確認でパスキーの操作をキャンセルした場合は、エラーを表示せず再発行もしないこと", async () => {
    mockReauthenticate.mockRejectedValueOnce(new PasskeyCancelledError());
    const user = userEvent.setup();

    render(<RecoveryCodeSection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "リカバリーコードを再発行" }));
    const confirm = screen.getByRole("dialog", { name: "リカバリーコードの再発行の確認" });
    await user.click(within(confirm).getByRole("button", { name: "再発行" }));

    await waitFor(() => {
      expect(within(confirm).getByRole("button", { name: "再発行" })).toBeEnabled();
    });
    expect(within(confirm).queryByRole("alert")).not.toBeInTheDocument();
    expect(mockRegenerateRecoveryCodes).not.toHaveBeenCalled();
  });

  it("本人確認に失敗した場合は、確認ダイアログにエラーを表示すること", async () => {
    mockRegenerateRecoveryCodes.mockRejectedValueOnce(
      new ApiError("パスキーによる本人確認に失敗しました。", 403)
    );
    const user = userEvent.setup();

    render(<RecoveryCodeSection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "リカバリーコードを再発行" }));
    const confirm = screen.getByRole("dialog", { name: "リカバリーコードの再発行の確認" });
    await user.click(within(confirm).getByRole("button", { name: "再発行" }));

    expect(await within(confirm).findByRole("alert")).toHaveTextContent(
      "パスキーによる本人確認に失敗しました。"
    );
  });

  it("確認ダイアログのキャンセルで、何もせずに閉じること", async () => {
    const user = userEvent.setup();

    render(<RecoveryCodeSection accountId="testuser1" />);
    await user.click(await screen.findByRole("button", { name: "リカバリーコードを再発行" }));
    await user.click(screen.getByRole("button", { name: "キャンセル" }));

    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(mockReauthenticate).not.toHaveBeenCalled();
  });
});
