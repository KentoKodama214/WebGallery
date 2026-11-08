import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AdminPhotoManagement } from "../AdminPhotoManagement";

// モック
const mockGetAdminPhotoList = jest.fn();
const mockUpdateAdminPhotoVisibility = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAdminPhotoList: (...args: unknown[]) => mockGetAdminPhotoList(...args),
  updateAdminPhotoVisibility: (...args: unknown[]) => mockUpdateAdminPhotoVisibility(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const publicPhoto = {
  accountNo: 1,
  accountId: "user0001",
  accountName: "ユーザー1",
  photoNo: 3,
  photoJapaneseTitle: "タイトル3",
  imageFilePath: "https://example.com/1/3-abc.jpg",
  isHiddenByAdmin: false,
  createdAt: "2026-11-01T10:00:00+09:00",
};

const hiddenPhoto = {
  ...publicPhoto,
  photoNo: 2,
  photoJapaneseTitle: "タイトル2",
  imageFilePath: "https://example.com/1/2-abc.jpg",
  isHiddenByAdmin: true,
};

describe("AdminPhotoManagement", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "admin1", role: "ROLE_ADMIN" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
  });

  it("管理者でない場合、一覧を取得せず権限がない旨を表示すること", () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1", role: "ROLE_USER" },
      isLoading: false,
    });

    render(<AdminPhotoManagement />);

    expect(screen.getByText("管理者権限がありません。")).toBeInTheDocument();
    expect(mockGetAdminPhotoList).not.toHaveBeenCalled();
  });

  it("写真一覧を取得し、公開状態に応じたラベルと操作ボタンを表示すること", async () => {
    mockGetAdminPhotoList.mockResolvedValue({ isLast: true, photoList: [publicPhoto, hiddenPhoto] });

    render(<AdminPhotoManagement />);

    await waitFor(() => {
      expect(screen.getByText("タイトル3")).toBeInTheDocument();
    });
    expect(mockGetAdminPhotoList).toHaveBeenCalledWith({
      accountId: undefined,
      isHiddenByAdmin: undefined,
      pageNo: 1,
    });

    const publicRow = screen.getByTestId("admin-photo-row-1-3");
    expect(within(publicRow).getByText("公開中")).toBeInTheDocument();
    expect(within(publicRow).getByRole("button", { name: "非公開にする" })).toBeInTheDocument();

    const hiddenRow = screen.getByTestId("admin-photo-row-1-2");
    expect(within(hiddenRow).getByText("非公開")).toBeInTheDocument();
    expect(within(hiddenRow).getByRole("button", { name: "非公開を解除" })).toBeInTheDocument();
  });

  it("公開状態・アカウントIDで絞り込んだ場合、その条件で取得し直すこと", async () => {
    mockGetAdminPhotoList.mockResolvedValue({ isLast: true, photoList: [hiddenPhoto] });

    render(<AdminPhotoManagement />);
    await waitFor(() => {
      expect(mockGetAdminPhotoList).toHaveBeenCalledTimes(1);
    });

    fireEvent.change(screen.getByLabelText("公開状態"), { target: { value: "hidden" } });
    await waitFor(() => {
      expect(mockGetAdminPhotoList).toHaveBeenLastCalledWith({
        accountId: undefined,
        isHiddenByAdmin: true,
        pageNo: 1,
      });
    });

    // アカウントIDは入力のたびには取得せず、検索ボタンで適用する
    fireEvent.change(screen.getByLabelText("アカウントID"), { target: { value: " user0001 " } });
    expect(mockGetAdminPhotoList).toHaveBeenCalledTimes(2);
    fireEvent.click(screen.getByRole("button", { name: "検索" }));
    await waitFor(() => {
      expect(mockGetAdminPhotoList).toHaveBeenLastCalledWith({
        accountId: "user0001",
        isHiddenByAdmin: true,
        pageNo: 1,
      });
    });
  });

  it("理由を入力するまで実行できず、非公開にすると一覧を再取得せずに該当の写真の状態を更新すること", async () => {
    mockGetAdminPhotoList.mockResolvedValue({ isLast: true, photoList: [publicPhoto] });
    mockUpdateAdminPhotoVisibility.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "写真を非公開にしました。",
    });

    render(<AdminPhotoManagement />);
    await waitFor(() => {
      expect(screen.getByText("タイトル3")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "非公開にする" }));
    const dialog = screen.getByTestId("visibility-confirm-dialog");
    const executeButton = within(dialog).getByRole("button", { name: "実行" });
    expect(executeButton).toBeDisabled();

    // 空白のみの理由では実行できない
    fireEvent.change(within(dialog).getByLabelText(/理由/), { target: { value: "   " } });
    expect(executeButton).toBeDisabled();

    fireEvent.change(within(dialog).getByLabelText(/理由/), {
      target: { value: " 不適切な画像のため " },
    });
    expect(executeButton).toBeEnabled();
    fireEvent.click(executeButton);

    await waitFor(() => {
      expect(screen.getByRole("status")).toHaveTextContent("写真を非公開にしました。");
    });
    expect(mockUpdateAdminPhotoVisibility).toHaveBeenCalledWith(1, 3, true, "不適切な画像のため");
    expect(screen.queryByTestId("visibility-confirm-dialog")).not.toBeInTheDocument();
    const row = screen.getByTestId("admin-photo-row-1-3");
    expect(within(row).getByText("非公開")).toBeInTheDocument();
    expect(within(row).getByRole("button", { name: "非公開を解除" })).toBeInTheDocument();
    // リードレプリカの遅延で古い状態に戻らないよう、成功後に一覧を再取得しない
    expect(mockGetAdminPhotoList).toHaveBeenCalledTimes(1);
  });

  it("非公開の解除に失敗した場合、エラーを通知し、写真の状態を変えないこと", async () => {
    mockGetAdminPhotoList.mockResolvedValue({ isLast: true, photoList: [hiddenPhoto] });
    mockUpdateAdminPhotoVisibility.mockRejectedValue(new Error("写真が存在しません。"));

    render(<AdminPhotoManagement />);
    await waitFor(() => {
      expect(screen.getByText("タイトル2")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "非公開を解除" }));
    const dialog = screen.getByTestId("visibility-confirm-dialog");
    fireEvent.change(within(dialog).getByLabelText(/理由/), { target: { value: "誤判定" } });
    fireEvent.click(within(dialog).getByRole("button", { name: "実行" }));

    await waitFor(() => {
      expect(screen.getAllByRole("alert")[0]).toHaveTextContent("写真が存在しません。");
    });
    expect(mockUpdateAdminPhotoVisibility).toHaveBeenCalledWith(1, 2, false, "誤判定");
    expect(within(screen.getByTestId("admin-photo-row-1-2")).getByText("非公開")).toBeInTheDocument();
  });

  it("続きがある場合、もっと見るで次のページを同じ条件で取得して継ぎ足すこと", async () => {
    mockGetAdminPhotoList
      .mockResolvedValueOnce({ isLast: false, photoList: [publicPhoto] })
      .mockResolvedValueOnce({ isLast: true, photoList: [hiddenPhoto] });

    render(<AdminPhotoManagement />);
    await waitFor(() => {
      expect(screen.getByText("タイトル3")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("show-more-button"));

    await waitFor(() => {
      expect(screen.getByText("タイトル2")).toBeInTheDocument();
    });
    expect(screen.getByText("タイトル3")).toBeInTheDocument();
    expect(mockGetAdminPhotoList).toHaveBeenLastCalledWith({
      accountId: undefined,
      isHiddenByAdmin: undefined,
      pageNo: 2,
    });
    expect(screen.queryByTestId("show-more-button")).not.toBeInTheDocument();
  });

  it("写真が0件の場合、該当なしを表示すること", async () => {
    mockGetAdminPhotoList.mockResolvedValue({ isLast: true, photoList: [] });

    render(<AdminPhotoManagement />);

    await waitFor(() => {
      expect(screen.getByText("該当する写真はありません。")).toBeInTheDocument();
    });
  });
});
