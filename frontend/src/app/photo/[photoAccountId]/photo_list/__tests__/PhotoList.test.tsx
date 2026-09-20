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

/** PhotoSwipe の pswp インスタンスが公開するUI要素（テストからのアクセス用） */
type MockRegisteredElement = {
  el: HTMLElement;
  onClick?: () => void;
};

/** モック化した PhotoSwipeLightbox インスタンス一覧（各テストから最新のものを参照する） */
const mockLightboxInstances: MockPhotoSwipeLightbox[] = [];

class MockPhotoSwipeLightbox {
  private handlers: Record<string, () => void> = {};
  private changeHandlers: Array<() => void> = [];
  elements: Record<string, MockRegisteredElement> = {};
  pswp: {
    currIndex: number;
    currSlide: { data: { element: HTMLElement | null } };
    on: (event: string, handler: () => void) => void;
    ui: { registerElement: (config: Record<string, unknown>) => void };
  } | null = null;

  constructor() {
    mockLightboxInstances.push(this);
  }

  on(event: string, handler: () => void) {
    this.handlers[event] = handler;
  }

  init() {
    this.pswp = {
      currIndex: 0,
      currSlide: { data: { element: null } },
      on: (event: string, handler: () => void) => {
        if (event === "change") this.changeHandlers.push(handler);
      },
      ui: {
        registerElement: (config: Record<string, unknown>) => {
          const el = document.createElement("button");
          // 実プロダクトの pswp が付与するクラス名を模し、document.querySelector経由の参照を成立させる
          el.className = `pswp__button--${config.name as string}`;
          // 既定は非表示（getByRoleの対象外）にしておき、他テストのグリッド上ボタン検索と衝突させない。
          // 表示状態は各テストが triggerChange 経由で明示的に切り替える
          el.style.display = "none";
          document.body.appendChild(el);
          const entry: MockRegisteredElement = { el };
          this.elements[config.name as string] = entry;
          (config.onInit as ((el: HTMLElement) => void) | undefined)?.(el);
          entry.onClick = config.onClick as (() => void) | undefined;
        },
      },
    };
    this.handlers.uiRegister?.();
  }

  /** currIndex を切り替えて "change" ハンドラを発火させる */
  triggerChange(currIndex: number) {
    if (!this.pswp) return;
    this.pswp.currIndex = currIndex;
    this.changeHandlers.forEach((h) => h());
  }

  destroy() {}
}

// PhotoSwipe（動的import）をスタブ化する
jest.mock(
  "photoswipe/lightbox",
  () => ({
    __esModule: true,
    default: MockPhotoSwipeLightbox,
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

/**
 * 写真グリッド内のお気に入りボタンを取得する（PhotoSwipeモックが
 * document.body直下に生成する同名ボタンと区別するため、写真グリッド側のみ
 * が持つ aria-pressed 属性で絞り込む）
 */
function galleryFavoriteButton(name: string): HTMLElement {
  const found = screen
    .getAllByRole("button", { name })
    .find((el) => el.hasAttribute("aria-pressed"));
  if (!found) throw new Error(`グリッド内にお気に入りボタン「${name}」が見つかりません`);
  return found;
}

describe("PhotoList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLightboxInstances.length = 0;
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

  afterEach(() => {
    // registerElement がテスト用に document.body へ直接追加したボタンを後片付けする
    document.querySelectorAll('[class^="pswp__button--"]').forEach((el) => el.remove());
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

    fireEvent.click(galleryFavoriteButton("お気に入りに追加"));

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

    fireEvent.click(galleryFavoriteButton("お気に入りに追加"));

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

    const btn = galleryFavoriteButton("お気に入りに追加");
    fireEvent.click(btn);
    fireEvent.click(btn);
    fireEvent.click(btn);

    await waitFor(() => {
      expect(mockAddFavorite).toHaveBeenCalledTimes(1);
    });
  });

  it("不正なJSON形式のCookieの場合は既定フィルターにフォールバックすること", async () => {
    mockGetCookie.mockReturnValue("{invalid-json");
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalled();
    });
    const params = mockGetPhotoList.mock.calls[0][1];
    expect(params.sortBy).toBe("photoAt");
    expect(params.tagList).toBeUndefined();
  });

  it("sessionStorageが利用できない環境でもエラーにならず一覧を表示できること", async () => {
    const getItemSpy = jest
      .spyOn(Storage.prototype, "getItem")
      .mockImplementation(() => {
        throw new Error("blocked");
      });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="other" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    getItemSpy.mockRestore();
  });

  it("写真登録上限の取得に失敗した場合は写真追加ボタンが表示されないこと", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    mockGetPhotoUpperLimit.mockRejectedValue(new Error("取得失敗"));

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });
    expect(screen.queryByText("＋写真追加")).not.toBeInTheDocument();
  });

  it("絞り込み実行で編集中の条件が適用され、一覧が更新されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValueOnce({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(screen.getByTestId("filter-trigger"));
    const selects = screen.getAllByRole("combobox") as HTMLSelectElement[];
    const orderSelect = selects.find((s) =>
      Array.from(s.options).some((o) => o.value === "favorite")
    )!;
    fireEvent.change(orderSelect, { target: { value: "favorite" } });
    const tagInput = screen.getByPlaceholderText("キーワードを入力");
    fireEvent.change(tagInput, { target: { value: "夕焼け" } });

    mockGetPhotoList.mockResolvedValueOnce({
      isLast: true,
      photoList: [samplePhotos[0]],
    });
    fireEvent.click(screen.getByText("絞り込み"));

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(1);
    });
    const lastCall = mockGetPhotoList.mock.calls.at(-1)!;
    expect(lastCall[1].sortBy).toBe("favorite");
    expect(lastCall[1].tagList).toBe("夕焼け");
    expect(lastCall[1].searchExecuted).toBe(true);
    // パネルは絞り込み実行で自動的に閉じる
    expect(screen.getByTestId("filter-panel").className).not.toMatch(/filterOpen/);
  });

  it("絞り込み実行時にAPIエラーが発生した場合はエラーメッセージが表示されること", async () => {
    mockGetPhotoList.mockResolvedValueOnce({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(screen.getByTestId("filter-trigger"));
    mockGetPhotoList.mockRejectedValueOnce(new Error("写真一覧の取得に失敗しました"));
    fireEvent.click(screen.getByText("絞り込み"));

    await waitFor(() => {
      expect(screen.getByText("写真一覧の取得に失敗しました")).toBeInTheDocument();
    });
  });

  it("お気に入り済み写真で「お気に入りから外す」を押すと解除APIが呼ばれること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "other" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    mockDeleteFavorite.mockResolvedValue({ isSuccess: true });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(galleryFavoriteButton("お気に入りから外す"));

    await waitFor(() => {
      expect(mockDeleteFavorite).toHaveBeenCalledWith(1, 1);
    });
  });

  it("画像ロード時にリンクへpswpの幅・高さ属性が設定されること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    const img = galleryImages()[0];
    Object.defineProperty(img, "naturalWidth", { value: 800, configurable: true });
    Object.defineProperty(img, "naturalHeight", { value: 600, configurable: true });
    fireEvent.load(img);

    const anchor = img.closest("a")!;
    expect(anchor).toHaveAttribute("data-pswp-width", "800");
    expect(anchor).toHaveAttribute("data-pswp-height", "600");
  });

  it("操作失敗通知の閉じるボタンでメッセージが消えること", async () => {
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
    fireEvent.click(galleryFavoriteButton("お気に入りに追加"));

    await waitFor(() => {
      expect(screen.getByText("お気に入りの登録に失敗しました")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "閉じる" }));
    expect(
      screen.queryByText("お気に入りの登録に失敗しました")
    ).not.toBeInTheDocument();
  });

  it("「もっと見る」でAPIエラーが発生した場合は一覧を維持しつつエラーを通知すること", async () => {
    mockGetPhotoList.mockResolvedValueOnce({ isLast: false, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });

    mockGetPhotoList.mockRejectedValueOnce(new Error("追加取得に失敗しました"));
    fireEvent.click(screen.getByText("+もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("追加取得に失敗しました")).toBeInTheDocument();
    });
    expect(galleryImages()).toHaveLength(2);
    expect(screen.getByText("+もっと見る")).toBeInTheDocument();
  });

  it("オーナーが向き（縦/横）を選択して絞り込むと条件がリクエストに反映されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetPhotoList.mockResolvedValueOnce({ isLast: true, photoList: samplePhotos });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    fireEvent.click(screen.getByTestId("filter-trigger"));
    const selects = screen.getAllByRole("combobox") as HTMLSelectElement[];
    const directionSelect = selects.find((s) =>
      Array.from(s.options).some((o) => o.value === "vertical")
    )!;
    fireEvent.change(directionSelect, { target: { value: "vertical" } });
    const favoriteSelect = selects.find((s) =>
      Array.from(s.options).some((o) => o.value === "true")
    )!;
    fireEvent.change(favoriteSelect, { target: { value: "true" } });

    mockGetPhotoList.mockResolvedValueOnce({ isLast: true, photoList: [] });
    fireEvent.click(screen.getByText("絞り込み"));

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalledTimes(2);
    });
    const lastCall = mockGetPhotoList.mock.calls.at(-1)!;
    expect(lastCall[1].directionKbn).toBe("vertical");
    expect(lastCall[1].isFavorite).toBe("true");
  });

  it("フィルタートリガーはEnterキーでも開くこと", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(galleryImages()).toHaveLength(2);
    });

    expect(screen.getByTestId("filter-panel").className).not.toMatch(/filterOpen/);
    fireEvent.keyDown(screen.getByTestId("filter-trigger"), {
      key: "Enter",
      code: "Enter",
    });
    expect(screen.getByTestId("filter-panel").className).toMatch(/filterOpen/);
  });

  it("フィルター閉じるボタンはEnterキーでも閉じ、編集値を破棄すること", async () => {
    mockGetPhotoList.mockResolvedValue({ isLast: false, photoList: samplePhotos });
    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("filter-trigger"));
    const tagInput = screen.getByPlaceholderText("キーワードを入力");
    fireEvent.change(tagInput, { target: { value: "破棄される値" } });

    fireEvent.keyDown(screen.getByTestId("filter-close-button"), {
      key: "Enter",
      code: "Enter",
    });
    expect(screen.getByTestId("filter-panel").className).not.toMatch(/filterOpen/);

    mockGetPhotoList.mockClear();
    mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: [] });
    fireEvent.click(screen.getByText("+もっと見る"));

    await waitFor(() => {
      expect(mockGetPhotoList).toHaveBeenCalled();
    });
    expect(mockGetPhotoList.mock.calls[0][1].tagList).toBeUndefined();
  });

  describe("PhotoSwipeライトボックスのお気に入りボタン（uiRegister）", () => {
    /** ギャラリー描画後、モックLightboxのuiRegister初期化が完了するまで待つ */
    async function renderAndWaitForLightbox() {
      render(<PhotoList photoAccountId="user1" />);
      await waitFor(() => {
        expect(galleryImages()).toHaveLength(2);
      });
      await waitFor(() => {
        expect(mockLightboxInstances.at(-1)?.pswp).not.toBeNull();
      });
      return mockLightboxInstances.at(-1)!;
    }

    it("未認証の場合はお気に入りボタンが登録されないこと", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      expect(lightbox.elements["add-favorite-button"]).toBeUndefined();
      expect(lightbox.elements["cancel-favorite-button"]).toBeUndefined();
    });

    it("認証済みの場合はお気に入り追加・解除ボタンが登録されること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      expect(lightbox.elements["add-favorite-button"]).toBeDefined();
      expect(lightbox.elements["cancel-favorite-button"]).toBeDefined();
      expect(lightbox.elements["add-favorite-button"].el).toHaveAttribute(
        "aria-label",
        "お気に入りに追加"
      );
      expect(lightbox.elements["cancel-favorite-button"].el).toHaveAttribute(
        "aria-label",
        "お気に入りから外す"
      );
    });

    it("表示中の写真がお気に入り済みかどうかでボタンの表示・非表示が切り替わること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      // samplePhotos[0] はお気に入り済み、samplePhotos[1] は未登録
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      lightbox.triggerChange(0);
      expect(lightbox.elements["add-favorite-button"].el.style.display).toBe("none");
      expect(lightbox.elements["cancel-favorite-button"].el.style.display).toBe("block");

      lightbox.triggerChange(1);
      expect(lightbox.elements["add-favorite-button"].el.style.display).toBe("block");
      expect(lightbox.elements["cancel-favorite-button"].el.style.display).toBe("none");
    });

    it("お気に入り追加ボタンをクリックするとAPIが呼ばれ、成功後にボタンの表示が切り替わること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      mockAddFavorite.mockResolvedValue({ httpStatus: 200, isSuccess: true, message: "" });
      const lightbox = await renderAndWaitForLightbox();

      // samplePhotos[1]（未お気に入り）を表示中とする
      lightbox.triggerChange(1);
      lightbox.elements["add-favorite-button"].onClick?.();

      await waitFor(() => {
        expect(mockAddFavorite).toHaveBeenCalledWith(1, 2);
      });
      await waitFor(() => {
        expect(lightbox.elements["add-favorite-button"].el.style.display).toBe("none");
      });
      expect(lightbox.elements["cancel-favorite-button"].el.style.display).toBe("block");
    });

    it("お気に入り済みの写真では追加ボタンをクリックしてもAPIが呼ばれないこと", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      // samplePhotos[0]（お気に入り済み）を表示中とする
      lightbox.triggerChange(0);
      lightbox.elements["add-favorite-button"].onClick?.();

      expect(mockAddFavorite).not.toHaveBeenCalled();
    });

    it("お気に入り追加に失敗した場合はエラーメッセージが表示されること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      mockAddFavorite.mockRejectedValue(new Error("network error"));
      const lightbox = await renderAndWaitForLightbox();

      lightbox.triggerChange(1);
      lightbox.elements["add-favorite-button"].onClick?.();

      await waitFor(() => {
        expect(screen.getByRole("alert")).toHaveTextContent("お気に入りの更新に失敗しました");
      });
    });

    it("お気に入り解除ボタンをクリックするとAPIが呼ばれ、成功後にボタンの表示が切り替わること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      mockDeleteFavorite.mockResolvedValue({ httpStatus: 200, isSuccess: true, message: "" });
      const lightbox = await renderAndWaitForLightbox();

      // samplePhotos[0]（お気に入り済み）を表示中とする
      lightbox.triggerChange(0);
      lightbox.elements["cancel-favorite-button"].onClick?.();

      await waitFor(() => {
        expect(mockDeleteFavorite).toHaveBeenCalledWith(1, 1);
      });
      await waitFor(() => {
        expect(lightbox.elements["cancel-favorite-button"].el.style.display).toBe("none");
      });
      expect(lightbox.elements["add-favorite-button"].el.style.display).toBe("block");
    });

    it("未登録の写真では解除ボタンをクリックしてもAPIが呼ばれないこと", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      lightbox.triggerChange(1);
      lightbox.elements["cancel-favorite-button"].onClick?.();

      expect(mockDeleteFavorite).not.toHaveBeenCalled();
    });

    it("お気に入り解除に失敗した場合はエラーメッセージが表示されること", async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "other" },
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      mockDeleteFavorite.mockRejectedValue(new Error("network error"));
      const lightbox = await renderAndWaitForLightbox();

      lightbox.triggerChange(0);
      lightbox.elements["cancel-favorite-button"].onClick?.();

      await waitFor(() => {
        expect(screen.getByRole("alert")).toHaveTextContent("お気に入りの更新に失敗しました");
      });
    });
  });

  describe("PhotoSwipeライトボックスのキャプション（uiRegister）", () => {
    async function renderAndWaitForLightbox() {
      render(<PhotoList photoAccountId="user1" />);
      await waitFor(() => {
        expect(galleryImages()).toHaveLength(2);
      });
      await waitFor(() => {
        expect(mockLightboxInstances.at(-1)?.pswp).not.toBeNull();
      });
      return mockLightboxInstances.at(-1)!;
    }

    it("change時、表示中スライドに隠しキャプションがあればその内容を複製すること", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      const galleryItem = document.querySelector(".pswp-gallery__item") as HTMLElement;
      lightbox.pswp!.currSlide.data.element = galleryItem;
      lightbox.triggerChange(0);

      const captionEl = lightbox.elements["custom-caption"].el;
      expect(captionEl.querySelector(".caption_content")).toHaveTextContent(
        "テスト写真1"
      );
    });

    it("change時、隠しキャプションが無ければ画像のalt属性をテキスト表示すること", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      const bareElement = document.createElement("div");
      const img = document.createElement("img");
      img.setAttribute("alt", "代替テキスト");
      bareElement.appendChild(img);
      lightbox.pswp!.currSlide.data.element = bareElement;

      lightbox.triggerChange(0);

      expect(lightbox.elements["custom-caption"].el.textContent).toBe(
        "代替テキスト"
      );
    });

    it("change時、表示中スライドが無ければキャプションを空にすること", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      lightbox.pswp!.currSlide.data.element = null;
      lightbox.triggerChange(0);

      expect(lightbox.elements["custom-caption"].el.textContent).toBe("");
    });

    it("キャプションクリックで詳細リンクへの遷移がトリガーされること", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      const galleryItem = document.querySelector(".pswp-gallery__item") as HTMLElement;
      lightbox.pswp!.currSlide.data.element = galleryItem;
      lightbox.triggerChange(0);

      const captionEl = lightbox.elements["custom-caption"].el;
      const detailLink = captionEl.querySelector(".show_detail a") as HTMLAnchorElement;
      const clickSpy = jest.spyOn(detailLink, "click").mockImplementation(() => {});

      fireEvent.click(captionEl);

      expect(clickSpy).toHaveBeenCalledTimes(1);
    });

    it("キャプション内のリンク自体のクリックでは詳細リンクへの遷移を発火しないこと", async () => {
      mockGetPhotoList.mockResolvedValue({ isLast: true, photoList: samplePhotos });
      const lightbox = await renderAndWaitForLightbox();

      const galleryItem = document.querySelector(".pswp-gallery__item") as HTMLElement;
      lightbox.pswp!.currSlide.data.element = galleryItem;
      lightbox.triggerChange(0);

      const captionEl = lightbox.elements["custom-caption"].el;
      const detailLink = captionEl.querySelector(".show_detail a") as HTMLAnchorElement;
      const clickSpy = jest.spyOn(detailLink, "click").mockImplementation(() => {});

      fireEvent.click(detailLink);

      expect(clickSpy).not.toHaveBeenCalled();
    });
  });
});
