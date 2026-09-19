import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoSettingForm } from "../PhotoSettingForm";

// モック
const mockGetPhotoDetail = jest.fn();
const mockSavePhoto = jest.fn();
const mockRegistPhotos = jest.fn();
const mockGetPhotoUpperLimit = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPhotoDetail: (...args: unknown[]) => mockGetPhotoDetail(...args),
  savePhoto: (...args: unknown[]) => mockSavePhoto(...args),
  registPhotos: (...args: unknown[]) => mockRegistPhotos(...args),
  getPhotoUpperLimit: (...args: unknown[]) => mockGetPhotoUpperLimit(...args),
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
  isLocationPublic: true,
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
    // 新規登録モードのマウント時に呼ばれる。既定では無制限扱いとする
    mockGetPhotoUpperLimit.mockResolvedValue({
      isReachedUpperLimit: false,
      remainingCount: null,
    });
  });

  it("新規モードでフォームが表示されること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("写真登録")).toBeInTheDocument();
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });
  });

  it("新規モードでは「向き」の選択UIが表示されないこと（バックエンドが画像から自動判定するため）", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    expect(screen.queryByTestId("direction-select")).not.toBeInTheDocument();
  });

  it("新規モードでは、画像ファイルからEXIF情報が自動登録される旨の案内文が表示されること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    expect(
      screen.getByText(
        "画像ファイルに焦点距離、F値、シャッタースピード、ISOの情報があれば自動登録されます"
      )
    ).toBeInTheDocument();
  });

  it("編集モードでは、EXIF情報自動登録の案内文が表示されないこと", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoSettingForm photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("写真編集")).toBeInTheDocument();
    });

    expect(
      screen.queryByText(
        "画像ファイルに焦点距離、F値、シャッタースピード、ISOの情報があれば自動登録されます"
      )
    ).not.toBeInTheDocument();
  });

  it("新規モードで送信すると、directionKbnを送信しないこと（バックエンドが画像から自動判定するため）", async () => {
    mockRegistPhotos.mockResolvedValue({ isSuccess: true, registeredCount: 1 });

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
      expect(mockRegistPhotos).toHaveBeenCalled();
    });
    const formData = mockRegistPhotos.mock.calls[0][1] as FormData;
    expect(formData.get("directionKbn")).toBeNull();
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
    // 編集モードでは向きの選択UIが表示され、画像ファイルの実際の値（未変更）を保持すること
    expect(screen.getByTestId("direction-select")).toHaveValue("horizontal");
  });

  it("編集モードで送信すると、directionKbnを送信すること", async () => {
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
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(mockSavePhoto).toHaveBeenCalled();
    });
    const formData = mockSavePhoto.mock.calls[0][1] as FormData;
    expect(formData.get("directionKbn")).toBe("horizontal");
  });

  it("編集モードでは画像ファイルの差し替えができないこと", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoSettingForm photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByTestId("image-preview")).toBeInTheDocument();
    });

    // バックエンドが更新時に画像ファイルを無視する仕様のため、選択UI自体が存在しないこと
    expect(screen.queryByTestId("image-input")).not.toBeInTheDocument();
    expect(
      screen.getByText("画像ファイルは登録後に変更できません")
    ).toBeInTheDocument();
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
    mockRegistPhotos.mockRejectedValue(new Error("保存に失敗しました"));

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
    let resolveRegistPhotos: (value: { registeredCount: number }) => void;
    mockRegistPhotos.mockReturnValue(
      new Promise((resolve) => {
        resolveRegistPhotos = resolve;
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

    expect(mockRegistPhotos).toHaveBeenCalledTimes(1);

    resolveRegistPhotos!({ registeredCount: 1 });
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

  it("保存成功後に成功モーダルが登録枚数付きで表示されること", async () => {
    mockRegistPhotos.mockResolvedValue({ isSuccess: true, registeredCount: 1 });

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
      expect(screen.getByText("1枚の写真を保存しました")).toBeInTheDocument();
    });
  });

  it("複数枚選択して送信すると、共通のメタデータで一括登録されること", async () => {
    mockRegistPhotos.mockResolvedValue({ isSuccess: true, registeredCount: 3 });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    const files = [
      new File(["dummy1"], "test1.jpg", { type: "image/jpeg" }),
      new File(["dummy2"], "test2.jpg", { type: "image/jpeg" }),
      new File(["dummy3"], "test3.jpg", { type: "image/jpeg" }),
    ];
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files },
    });

    expect(screen.getByTestId("image-preview-item-0")).toBeInTheDocument();
    expect(screen.getByTestId("image-preview-item-2")).toBeInTheDocument();

    fireEvent.change(screen.getByTestId("japanese-title-input"), {
      target: { value: "共通タイトル" },
    });

    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(mockRegistPhotos).toHaveBeenCalled();
      expect(screen.getByText("3枚の写真を保存しました")).toBeInTheDocument();
    });

    const formData = mockRegistPhotos.mock.calls[0][1] as FormData;
    expect(formData.getAll("imageFiles")).toHaveLength(3);
    expect(formData.get("photoJapaneseTitle")).toBe("共通タイトル");
  });

  it("選択済みの画像を削除できること", async () => {
    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    const files = [
      new File(["dummy1"], "test1.jpg", { type: "image/jpeg" }),
      new File(["dummy2"], "test2.jpg", { type: "image/jpeg" }),
    ];
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files },
    });

    expect(screen.getByTestId("image-preview-item-1")).toBeInTheDocument();

    fireEvent.click(screen.getByTestId("remove-image-0"));

    expect(screen.queryByTestId("image-preview-item-1")).not.toBeInTheDocument();
    expect(screen.getByTestId("image-preview-item-0")).toBeInTheDocument();
  });

  it("アカウントの残り登録可能枚数を超えて選択すると拒否されること", async () => {
    mockGetPhotoUpperLimit.mockResolvedValue({
      isReachedUpperLimit: false,
      remainingCount: 2,
    });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });
    // remainingCount の取得（非同期）が反映されるのを待つ
    await waitFor(() => {
      expect(mockGetPhotoUpperLimit).toHaveBeenCalled();
    });

    const files = [
      new File(["dummy1"], "test1.jpg", { type: "image/jpeg" }),
      new File(["dummy2"], "test2.jpg", { type: "image/jpeg" }),
      new File(["dummy3"], "test3.jpg", { type: "image/jpeg" }),
    ];
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files },
    });

    await waitFor(() => {
      expect(screen.getByTestId("validation-errors")).toBeInTheDocument();
    });
    expect(screen.queryByTestId("image-preview-item-0")).not.toBeInTheDocument();
  });

  it("新規モードでは位置情報公開チェックボックスが未チェックで、送信時に isLocationPublic=false を送ること", async () => {
    mockRegistPhotos.mockResolvedValue({ isSuccess: true, registeredCount: 1 });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    const checkbox = screen.getByTestId(
      "location-public-checkbox"
    ) as HTMLInputElement;
    expect(checkbox.checked).toBe(false);

    const file = new File(["dummy"], "test.jpg", { type: "image/jpeg" });
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files: [file] },
    });
    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(mockRegistPhotos).toHaveBeenCalled();
    });
    const formData = mockRegistPhotos.mock.calls[0][1] as FormData;
    expect(formData.get("isLocationPublic")).toBe("false");
  });

  it("位置情報公開チェックを入れて送信すると isLocationPublic=true を送ること", async () => {
    mockRegistPhotos.mockResolvedValue({ isSuccess: true, registeredCount: 1 });

    render(<PhotoSettingForm photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByTestId("submit-button")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("location-public-checkbox"));
    const file = new File(["dummy"], "test.jpg", { type: "image/jpeg" });
    fireEvent.change(screen.getByTestId("image-input"), {
      target: { files: [file] },
    });
    fireEvent.click(screen.getByTestId("submit-button"));

    await waitFor(() => {
      expect(mockRegistPhotos).toHaveBeenCalled();
    });
    const formData = mockRegistPhotos.mock.calls[0][1] as FormData;
    expect(formData.get("isLocationPublic")).toBe("true");
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
