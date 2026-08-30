import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoList } from "../PhotoList";
import { getCookie, setCookie } from "@/lib/cookie";

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

const mockGetCookie = getCookie as jest.Mock;
const mockSetCookie = setCookie as jest.Mock;

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
    mockGetCookie.mockReturnValue(null);
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

  it("許可されない画像URLの写真はリンク化されず代替表示になること", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: [
        {
          accountNo: 1,
          photoNo: 9,
          isFavorite: false,
          imageFilePath: "//evil.example/x.jpg",
          caption: "危険な写真",
          directionKbn: "horizontal",
        },
      ],
    });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("画像を表示できません")).toBeInTheDocument();
    });
    // 壊れた <a href=""> が生成されていないこと
    expect(
      document.querySelector('a[href=""]')
    ).toBeNull();
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

    fireEvent.click(screen.getByRole("button", { name: "お気に入りに追加" }));

    await waitFor(() => {
      expect(mockAddFavorite).toHaveBeenCalledWith(1, 2);
    });
  });

  it("お気に入り更新失敗時にエラーが通知され、一覧は維持されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    mockAddFavorite.mockRejectedValue(new Error("お気に入りの登録に失敗しました"));

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(screen.getByRole("button", { name: "お気に入りに追加" }));

    await waitFor(() => {
      expect(
        screen.getByText("お気に入りの登録に失敗しました")
      ).toBeInTheDocument();
    });
    expect(galleryImages()).toHaveLength(2);
  });

  it("フィルター未適用で閉じた場合、編集値は破棄され「もっと見る」は元の条件で取得すること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: false, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });

    // フィルターパネルを開き、並び順を変更するが「絞り込み」は押さない
    fireEvent.click(screen.getByTestId("filter-trigger"));
    const selects = screen.getAllByRole("combobox") as HTMLSelectElement[];
    const orderSelect = selects.find((s) =>
      Array.from(s.options).some((o) => o.value === "favorite")
    )!;
    fireEvent.change(orderSelect, { target: { value: "favorite" } });

    // 適用せずに閉じる
    fireEvent.click(screen.getByTestId("filter-close-button"));

    mockGetPhotoList.mockClear();
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: [] });

    fireEvent.click(screen.getByText("+もっと見る"));

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalled();
    });
    const params = mockGetPhotoList.mock.calls[0][1];
    // 未適用のためデフォルト（photoAt）のまま
    expect(params.sortBy).toBe("photoAt");
  });

  it("フィルター Cookie はギャラリー（photoAccountId）単位のキーで保存されること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(mockSetCookie).toHaveBeenCalled();
    });
    expect(mockSetCookie.mock.calls[0][0]).toBe("photoListFilter_user1");
  });

  it("未認証ではお気に入り絞り込み条件をリクエストに含めないこと", async () => {
    mockGetCookie.mockReturnValue(
      JSON.stringify({
        directionKbn: "",
        isFavoriteFilter: "true",
        tagList: "",
        sortBy: "photoAt",
      })
    );
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalled();
    });
    expect(mockGetPhotoList.mock.calls[0][1].isFavorite).toBeUndefined();
  });

  it("非オーナーには向き（縦/横）絞り込み条件をリクエストに含めないこと", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other", accountNo: 99 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetCookie.mockReturnValue(
      JSON.stringify({
        directionKbn: "vertical",
        isFavoriteFilter: "",
        tagList: "",
        sortBy: "photoAt",
      })
    );
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalled();
    });
    expect(mockGetPhotoList.mock.calls[0][1].directionKbn).toBeUndefined();
  });

  it("お気に入りアイコンの連打でも登録APIは1回だけ呼ばれること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other", accountNo: 99 },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    // 解決しないプロミスで「進行中」を保持する
    mockAddFavorite.mockReturnValue(new Promise(() => {}));

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    const btn = screen.getByRole("button", { name: "お気に入りに追加" });
    fireEvent.click(btn);
    fireEvent.click(btn);
    fireEvent.click(btn);

    await waitFor(() => {
      expect(mockAddFavorite).toHaveBeenCalledTimes(1);
    });
  });
});
