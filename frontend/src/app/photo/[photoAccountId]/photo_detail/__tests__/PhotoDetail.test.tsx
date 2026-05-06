import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoDetail } from "../PhotoDetail";

// モック
const mockGetPhotoDetail = jest.fn();
const mockDeletePhoto = jest.fn();
const mockAddFavorite = jest.fn();
const mockDeleteFavorite = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPhotoDetail: (...args: unknown[]) => mockGetPhotoDetail(...args),
  deletePhoto: (...args: unknown[]) => mockDeletePhoto(...args),
  addFavorite: (...args: unknown[]) => mockAddFavorite(...args),
  deleteFavorite: (...args: unknown[]) => mockDeleteFavorite(...args),
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
  locationName: "東京タワー",
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
    {
      accountNo: 1,
      photoNo: 10,
      tagNo: 2,
      tagJapaneseName: "夜景",
      tagEnglishName: "night",
    },
  ],
};

describe("PhotoDetail", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
  });

  it("読み込み中の表示がされること", () => {
    mockGetPhotoDetail.mockReturnValue(new Promise(() => {}));

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  it("写真詳細が正しく表示されること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("テスト写真")).toBeInTheDocument();
      expect(screen.getByText("Test Photo")).toBeInTheDocument();
      expect(screen.getByText("テストキャプション")).toBeInTheDocument();
    });

    expect(screen.getByRole("img")).toHaveAttribute("src", "/photos/test.jpg");
  });

  it("EXIF情報が表示されること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("50mm")).toBeInTheDocument();
      expect(screen.getByText("F1.8")).toBeInTheDocument();
      expect(screen.getByText("0.01秒")).toBeInTheDocument();
      expect(screen.getByText("400")).toBeInTheDocument();
    });
  });

  it("タグが表示されること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("風景")).toBeInTheDocument();
      expect(screen.getByText("夜景")).toBeInTheDocument();
    });
  });

  it("撮影場所が表示されること", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("東京タワー")).toBeInTheDocument();
    });
  });

  it("オーナーの場合に編集・削除ボタンが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1", accountNo: 1 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("編集")).toBeInTheDocument();
      expect(screen.getByText("削除")).toBeInTheDocument();
    });
  });

  it("非オーナーの場合に編集・削除ボタンが表示されないこと", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other_user", accountNo: 2 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("テスト写真")).toBeInTheDocument();
    });

    expect(screen.queryByText("編集")).not.toBeInTheDocument();
    expect(screen.queryByText("削除")).not.toBeInTheDocument();
  });

  it("認証済みの場合にお気に入りボタンが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other_user", accountNo: 2 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByTestId("favorite-button")).toBeInTheDocument();
    });
  });

  it("未認証の場合にお気に入りボタンが表示されないこと", async () => {
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("テスト写真")).toBeInTheDocument();
    });

    expect(screen.queryByTestId("favorite-button")).not.toBeInTheDocument();
  });

  it("お気に入りトグルが動作すること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other_user", accountNo: 2 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);
    mockAddFavorite.mockResolvedValue({ isSuccess: true });

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(
        screen.getByText("☆ お気に入り登録")
      ).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("favorite-button"));

    await waitFor(() => {
      expect(
        screen.getByText("★ お気に入り解除")
      ).toBeInTheDocument();
    });

    expect(mockAddFavorite).toHaveBeenCalledWith(1, 10);
  });

  it("削除確認→削除→リダイレクトが動作すること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1", accountNo: 1 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoDetail.mockResolvedValue(samplePhoto);
    mockDeletePhoto.mockResolvedValue({ isSuccess: true });

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(screen.getByText("削除")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("削除"));

    expect(screen.getByTestId("delete-confirm-dialog")).toBeInTheDocument();
    expect(screen.getByText("この写真を削除しますか？")).toBeInTheDocument();

    fireEvent.click(screen.getByText("削除する"));

    await waitFor(() => {
      expect(mockDeletePhoto).toHaveBeenCalledWith("user1", {
        accountNo: 1,
        photoNo: 10,
        imageFilePath: "/photos/test.jpg",
      });
      expect(mockPush).toHaveBeenCalledWith("/photo/user1/photo_list");
    });
  });

  it("エラー時にエラーメッセージが表示されること", async () => {
    mockGetPhotoDetail.mockRejectedValue(
      new Error("写真詳細の取得に失敗しました")
    );

    render(
      <PhotoDetail photoAccountId="user1" accountNo={1} photoNo={10} />
    );

    await waitFor(() => {
      expect(
        screen.getByText("写真詳細の取得に失敗しました")
      ).toBeInTheDocument();
    });
  });
});
