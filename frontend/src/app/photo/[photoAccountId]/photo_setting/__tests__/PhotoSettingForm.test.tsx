import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoSettingForm } from "../PhotoSettingForm";

// モック
const mockGetPhotoDetail = jest.fn();
const mockSavePhoto = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPhotoDetail: (...args: unknown[]) => mockGetPhotoDetail(...args),
  savePhoto: (...args: unknown[]) => mockSavePhoto(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const mockPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const samplePhoto = {
  accountNo: 1,
  photoNo: 10,
  isFavorite: false,
  photoAt: "2024-03-15T10:30:00+09:00",
  locationNo: null,
  address: null,
  latitude: null,
  longitude: null,
  locationName: null,
  imageFilePath: "/photos/test.jpg",
  photoJapaneseTitle: "テスト写真",
  photoEnglishTitle: "Test Photo",
  caption: "テストキャプション",
  directionKbn: "horizontal",
  focalLength: 50,
  fValue: 1.8,
  shutterSpeed: 0.01,
  iso: 400,
  photoTagList: [
    {
      accountNo: 1,
      photoNo: 10,
      tagNo: 1,
      tagJapaneseName: "風景",
      tagEnglishName: "landscape",
    },
  ],
};

describe("PhotoSettingForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1", accountNo: 1 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
  });

  it("新規モードでフォームが表示されること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("写真登録")).toBeInTheDocument();
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });
  });

  it("編集モードでデータが反映されること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoSettingForm photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("写真編集")).toBeInTheDocument();
    });

    expect(screen.getByTestId("japanese-title-input")).toHaveValue(
      "テスト写真"
    );
    expect(screen.getByTestId("english-title-input")).toHaveValue("Test Photo");
    expect(screen.getByTestId("caption-input")).toHaveValue("テストキャプション");
    expect(screen.getByTestId("focal-length-input")).toHaveValue(50);
    expect(screen.getByTestId("f-value-input")).toHaveValue(1.8);
    expect(screen.getByTestId("iso-input")).toHaveValue(400);
    expect(screen.getByTestId("image-preview")).toBeInTheDocument();
  });

  it("新規モードで画像なしの場合にバリデーションエラーが表示されること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(screen.getByTestId("validation-errors")).toBeInTheDocument();
      expect(
        screen.getByText("画像ファイルを選択してください")
      ).toBeInTheDocument();
    });
    // スクリーンリーダーに通知されるようrole="alert"が付与されていること
    expect(screen.getByTestId("validation-errors")).toHaveAttribute(
      "role",
      "alert"
    );
  });

  it("保存失敗時のエラーメッセージにrole=alertが付与されること", async () => {
    mockSavePhoto.mockRejectedValue(new Error("保存に失敗しました"));

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    const file = new File(["dummy"], "test.jpg", { type: "image/jpeg" });
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files: [file] },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(screen.getByText("保存に失敗しました")).toBeInTheDocument();
    });
    expect(screen.getByText("保存に失敗しました")).toHaveAttribute(
      "role",
      "alert"
    );
  });

  it("保存処理中に連打しても多重送信されないこと", async () => {
    let resolveSavePhoto: (value: { photoNo: number; imageFilePath: string }) => void;
    mockSavePhoto.mockReturnValue(
      new Promise((resolve) => {
        resolveSavePhoto = resolve;
      })
    );

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    const file = new File(["dummy"], "test.jpg", { type: "image/jpeg" });
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files: [file] },
    });

    fireEvent.click(screen.getByTestId("submit-button"));
    fireEvent.click(screen.getByTestId("submit-button"));
    fireEvent.click(screen.getByTestId("submit-button"));

    expect(mockSavePhoto).toHaveBeenCalledTimes(1);

    resolveSavePhoto!({ photoNo: 1, imageFilePath: "/photos/test.jpg" });
    await waitFor(() => {
      expect(screen.getByTestId("success-modal")).toBeInTheDocument();
    });
  });

  it("タグの追加・削除ができること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("add-tag-button")).toBeInTheDocument();
    });

    // タグ追加
    fireEvent.click(screen.getByTestId("add-tag-button"));

    expect(screen.getByTestId("tag-entry-1")).toBeInTheDocument();

    // タグ名入力
    fireEvent.change(screen.getByTestId("tag-japanese-1"), {
      target: { value: "テストタグ" },
    });

    expect(screen.getByTestId("tag-japanese-1")).toHaveValue("テストタグ");

    // 2つ目のタグを追加
    fireEvent.click(screen.getByTestId("add-tag-button"));
    expect(screen.getByTestId("tag-entry-2")).toBeInTheDocument();

    // 1つ目のタグを削除
    fireEvent.click(screen.getByTestId("remove-tag-1"));
    expect(screen.queryByTestId("tag-entry-1")).not.toBeInTheDocument();
    expect(screen.getByTestId("tag-entry-2")).toBeInTheDocument();
  });

  it("保存成功後に成功モーダルが表示されること", async () => {
    mockSavePhoto.mockResolvedValue({ isSuccess: true });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    // ファイル選択をシミュレート
    const file = new File(["dummy"], "test.jpg", { type: "image/jpeg" });
    const input = screen.getByTestId("image-input");
    fireEvent.change(input, { target: { files: [file] } });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(screen.getByTestId("success-modal")).toBeInTheDocument();
      expect(screen.getByText("写真を保存しました")).toBeInTheDocument();
    });
  });

  it("未認証の場合にログインページへリダイレクトされること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/login");
    });
  });

  it("権限がない場合にエラーメッセージが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other_user", accountNo: 2 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(
        screen.getByText("この操作を行う権限がありません")
      ).toBeInTheDocument();
    });
  });

  it("読み込んだ写真の所有者がログインユーザーでない場合は権限エラーになること", async () => {
    // パスは自分（user1）だが、返ってきた写真は別アカウント（accountNo=2）のもの
    mockGetPhotoDetail.mockResolvedValue({ ...samplePhoto, accountNo: 2 });

    render(
      <PhotoSettingForm photoAccountId="user1" accountNo={2} photoNo={10} />
    );

    await waitFor(() => {
      expect(
        screen.getByText("この操作を行う権限がありません")
      ).toBeInTheDocument();
    });
    expect(mockSavePhoto).not.toHaveBeenCalled();
  });

  it("新規追加タグは tagNo を送信せず、既存タグは送信すること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);
    mockSavePhoto.mockResolvedValue({
      isSuccess: true,
      photoNo: 10,
      imageFilePath: "/photos/test.jpg",
    });

    render(
      <PhotoSettingForm photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByTestId("add-tag-button")).toBeInTheDocument();
    });

    // 新規タグを1つ追加（既存タグ tagNo=1 の次なので tagNo=2 が採番される）
    fireEvent.click(screen.getByTestId("add-tag-button"));
    fireEvent.change(screen.getByTestId("tag-japanese-2"), {
      target: { value: "新規タグ" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(mockSavePhoto).toHaveBeenCalled();
    });

    const formData = mockSavePhoto.mock.calls[0][1] as FormData;
    // 既存タグ（index 0）は実タグ番号を送る
    expect(formData.get("photoTagRegistRequestList[0].tagNo")).toBe("1");
    // 新規タグ（index 1）は tagNo を送らない
    expect(formData.get("photoTagRegistRequestList[1].tagNo")).toBeNull();
    expect(formData.get("photoTagRegistRequestList[1].tagJapaneseName")).toBe(
      "新規タグ"
    );
  });
});
