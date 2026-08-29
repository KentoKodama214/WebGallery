import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoList } from "../PhotoList";

const mockGetPhotoList = jest.fn();
const mockGetPhotoUpperLimit = jest.fn();
const mockAddFavorite = jest.fn();
const mockDeleteFavorite = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPhotoList: (...args: unknown[]) => mockGetPhotoList(...args),
  getPhotoUpperLimit: (...args: unknown[]) => mockGetPhotoUpperLimit(...args),
  addFavorite: (...args: unknown[]) => mockAddFavorite(...args),
  deleteFavorite: (...args: unknown[]) => mockDeleteFavorite(...args),
}));

const mockUseAuth = jest.fn();
jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

jest.mock("@/lib/cookie", () => ({
  getCookie: jest.fn(() => null),
  setCookie: jest.fn(),
}));

// PhotoSwipe（動的import）をスタブ化する
jest.mock(
  "photoswipe/lightbox",
  () => ({
    __esModule: true,
    default: class {
      on() {}
      init() {}
      destroy() {}
    },
  }),
  { virtual: true }
);
jest.mock("photoswipe", () => ({ __esModule: true, default: {} }), {
  virtual: true,
});

const samplePhotos = [
  {
    accountNo: 1,
    photoNo: 1,
    isFavorite: true,
    imageFilePath: "/photos/photo1.jpg",
    caption: "テスト写真1",
    directionKbn: "horizontal",
  },
  {
    accountNo: 1,
    photoNo: 2,
    isFavorite: false,
    imageFilePath: "/photos/photo2.jpg",
    caption: "テスト写真2",
    directionKbn: "vertical",
  },
];

/** キャプションを持つ写真グリッド画像だけを抽出する */
function galleryImages(): HTMLElement[] {
  return screen
    .getAllByRole("img")
    .filter((img) => (img.getAttribute("alt") || "").startsWith("テスト写真"));
}

describe("PhotoList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoUpperLimit.mockResolvedValue({ isReachedUpperLimit: false });
  });

  it("読み込み中の表示がされること", () => {
    mockGetPhotoList.mockReturnValue(new Promise(() => {}));
    render(<PhotoList photoAccountId="user1" />);
    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  it("写真一覧が正しく表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    const images = galleryImages();
    expect(images[0]).toHaveAttribute("src", "/photos/photo1.jpg");
    expect(images[1]).toHaveAttribute("src", "/photos/photo2.jpg");
  });

  it("写真が0件の場合にメッセージが表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: [] });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("写真がありません")).toBeInTheDocument();
    });
  });

  it("エラー時にエラーメッセージが表示されること", async () => {
    mockGetPhotoList.mockRejectedValue(new Error("写真一覧の取得に失敗しました"));
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("写真一覧の取得に失敗しました")).toBeInTheDocument();
    });
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: false, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });
  });

  it("isLastがtrueのとき「もっと見る」ボタンが表示されないこと", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    expect(screen.queryByText("+もっと見る")).not.toBeInTheDocument();
  });

  it("「もっと見る」クリックで追加の写真が読み込まれること", async () => {
    mockGetPhotoList
      .mockResolvedValueOnce({ isLast: false, photoList: samplePhotos })
      .mockResolvedValueOnce({
        isLast: true,
        photoList: [
          {
            accountNo: 1,
            photoNo: 3,
            isFavorite: false,
            imageFilePath: "/photos/photo3.jpg",
            caption: "テスト写真3",
            directionKbn: "horizontal",
          },
        ],
      });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("+もっと見る"));

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(3);
    });
    expect(screen.queryByText("+もっと見る")).not.toBeInTheDocument();
  });

  it("フィルターパネルの開閉ができること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    const panel = screen.getByTestId("filter-panel");
    expect(panel.className).not.toMatch(/filterOpen/);

    fireEvent.click(screen.getByTestId("filter-trigger"));
    expect(screen.getByTestId("filter-panel").className).toMatch(/filterOpen/);

    fireEvent.click(screen.getByTestId("filter-close-button"));
    expect(screen.getByTestId("filter-panel").className).not.toMatch(/filterOpen/);
  });

  it("認証済みユーザーにはお気に入りフィルターが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    expect(screen.getByText("お気に入り写真のみ")).toBeInTheDocument();
  });

  it("未認証ユーザーにはお気に入りフィルターが表示されないこと", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    expect(screen.queryByText("お気に入り写真のみ")).not.toBeInTheDocument();
  });

  it("オーナーには写真追加ボタンが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("＋写真追加")).toBeInTheDocument();
    });
  });

  it("上限到達時は写真追加ボタンが表示されないこと", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    mockGetPhotoUpperLimit.mockResolvedValue({ isReachedUpperLimit: true });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    expect(screen.queryByText("＋写真追加")).not.toBeInTheDocument();
  });

  it("お気に入りアイコンのクリックで登録APIが呼ばれること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    mockAddFavorite.mockResolvedValue({ isSuccess: true });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(screen.getByAltText("お気に入りではない"));

    await waitFor(() => {
      expect(mockAddFavorite).toHaveBeenCalledWith(1, 2);
    });
  });
});
