import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoList } from "../PhotoList";

// モック
const mockGetPhotoList = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPhotoList: (...args: unknown[]) => mockGetPhotoList(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

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
  });

  it("読み込み中の表示がされること", () => {
    mockGetPhotoList.mockReturnValue(new Promise(() => {}));

    render(<PhotoList photoAccountId="user1" />);

    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  it("写真一覧が正しく表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      const images = screen.getAllByRole("img");
      expect(images).toHaveLength(2);
      expect(images[0]).toHaveAttribute("alt", "テスト写真1");
      expect(images[1]).toHaveAttribute("alt", "テスト写真2");
    });
  });

  it("写真が0件の場合にメッセージが表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: [],
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("写真がありません")).toBeInTheDocument();
    });
  });

  it("エラー時にエラーメッセージが表示されること", async () => {
    mockGetPhotoList.mockRejectedValue(
      new Error("写真一覧の取得に失敗しました")
    );

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(
        screen.getByText("写真一覧の取得に失敗しました")
      ).toBeInTheDocument();
    });
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: false,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("+もっと見る")).toBeInTheDocument();
    });
  });

  it("isLastがtrueのとき「もっと見る」ボタンが表示されないこと", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByRole("img")).toHaveLength(2);
    });

    expect(screen.queryByText("+もっと見る")).not.toBeInTheDocument();
  });

  it("「もっと見る」ボタンをクリックすると追加の写真が読み込まれること", async () => {
    mockGetPhotoList
      .mockResolvedValueOnce({
        isLast: false,
        photoList: samplePhotos,
      })
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
      expect(screen.getAllByRole("img")).toHaveLength(3);
    });

    expect(screen.queryByText("+もっと見る")).not.toBeInTheDocument();
  });

  it("フィルターパネルの開閉ができること", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByRole("img")).toHaveLength(2);
    });

    // フィルターパネルは初期状態で非表示
    expect(screen.queryByTestId("filter-panel")).not.toBeInTheDocument();

    // フィルターボタンをクリックして開く
    fireEvent.click(screen.getByText("フィルター"));
    expect(screen.getByTestId("filter-panel")).toBeInTheDocument();

    // もう一度クリックして閉じる
    fireEvent.click(screen.getByText("フィルター"));
    expect(screen.queryByTestId("filter-panel")).not.toBeInTheDocument();
  });

  it("認証済みユーザーにはお気に入りフィルターが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });

    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByRole("img")).toHaveLength(2);
    });

    fireEvent.click(screen.getByText("フィルター"));

    expect(screen.getByText("お気に入り")).toBeInTheDocument();
  });

  it("未認証ユーザーにはお気に入りフィルターが表示されないこと", async () => {
    mockGetPhotoList.mockResolvedValue({
      isLast: true,
      photoList: samplePhotos,
    });

    render(<PhotoList photoAccountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByRole("img")).toHaveLength(2);
    });

    fireEvent.click(screen.getByText("フィルター"));

    expect(screen.queryByText("お気に入り")).not.toBeInTheDocument();
  });
});
