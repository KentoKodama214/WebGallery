import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PortfolioEditForm } from "../PortfolioEditForm";

const mockGetPortfolioDetail = jest.fn();
const mockGetPortfolioTemplateList = jest.fn();
const mockSavePortfolio = jest.fn();
const mockUpdatePortfolioStatus = jest.fn();
const mockDeletePortfolio = jest.fn();
const mockSearchPortfolioPhotos = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPortfolioDetail: (...args: unknown[]) => mockGetPortfolioDetail(...args),
  getPortfolioTemplateList: (...args: unknown[]) => mockGetPortfolioTemplateList(...args),
  savePortfolio: (...args: unknown[]) => mockSavePortfolio(...args),
  updatePortfolioStatus: (...args: unknown[]) => mockUpdatePortfolioStatus(...args),
  deletePortfolio: (...args: unknown[]) => mockDeletePortfolio(...args),
  searchPortfolioPhotos: (...args: unknown[]) => mockSearchPortfolioPhotos(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const mockPush = jest.fn();
const mockReplace = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
}));

/** グリッド型・公開中のポートフォリオ詳細 */
const gridDetail = {
  portfolioNo: 3,
  theme: "日本の自然",
  description: "説明文",
  templateKbn: "grid" as const,
  templateSettings: { rows: [{ pattern: "TWO_EQUAL" }] },
  statusKbn: "published" as const,
  createdAt: "2026-01-01T00:00:00+09:00",
  updatedAt: "2026-01-01T00:00:00+09:00",
  photoList: [
    {
      photoNo: 5,
      displayOrder: 1,
      photoSettings: {},
      photoJapaneseTitle: "金閣寺",
      imageFilePath: "",
      directionKbn: "horizontal",
    },
    {
      photoNo: 8,
      displayOrder: 2,
      photoSettings: {},
      photoJapaneseTitle: "茶畑",
      imageFilePath: "",
      directionKbn: "square",
    },
  ],
  tagList: [{ tagNo: 1, tagJapaneseName: "自然", tagEnglishName: "nature" }],
};

describe("PortfolioEditForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({ user: { accountId: "user1", accountNo: 1 } });
    // テンプレート一覧の取得に失敗しても、画面側の既定値で表示を続けられること
    mockGetPortfolioTemplateList.mockRejectedValue(new Error("取得失敗"));
  });

  describe("新規作成", () => {
    it("テーマが未入力の場合はエラーを表示し、保存しないこと", async () => {
      render(<PortfolioEditForm accountId="user1" />);

      fireEvent.click(screen.getByRole("button", { name: "下書きとして登録" }));

      expect(await screen.findByText("テーマを入力してください")).toBeInTheDocument();
      expect(mockSavePortfolio).not.toHaveBeenCalled();
    });

    it("写真を選択していない場合は、下書きでもエラーを表示し、保存しないこと", async () => {
      render(<PortfolioEditForm accountId="user1" />);

      fireEvent.change(screen.getByLabelText(/テーマ/, { selector: "#portfolio-theme" }), {
        target: { value: "日本の自然" },
      });
      fireEvent.click(screen.getByRole("button", { name: "下書きとして登録" }));

      expect(await screen.findByText("写真を1枚以上選択してください")).toBeInTheDocument();
      expect(screen.queryByText("テーマを入力してください")).not.toBeInTheDocument();
      expect(mockSavePortfolio).not.toHaveBeenCalled();
    });

    it("作成数の上限に達している場合は、上限に達した旨を表示し、編集用URLへ切り替えないこと", async () => {
      mockSearchPortfolioPhotos.mockResolvedValue({
        isLast: true,
        photoList: [
          { photoNo: 11, photoJapaneseTitle: "富士山", imageFilePath: "", directionKbn: "horizontal" },
        ],
      });
      mockSavePortfolio.mockRejectedValue(
        new Error("作成できるポートフォリオの数が上限に達しています。")
      );

      render(<PortfolioEditForm accountId="user1" />);
      fireEvent.change(screen.getByLabelText(/テーマ/, { selector: "#portfolio-theme" }), {
        target: { value: "日本の自然" },
      });
      fireEvent.click(screen.getByRole("button", { name: "写真を選択" }));
      const dialog = await screen.findByTestId("photo-picker-dialog");
      fireEvent.click(await within(dialog).findByRole("button", { name: "写真11（富士山）" }));
      fireEvent.click(within(dialog).getByRole("button", { name: "完了" }));

      fireEvent.click(screen.getByRole("button", { name: "下書きとして登録" }));

      expect(
        await screen.findByText("作成できるポートフォリオの数が上限に達しています。")
      ).toBeInTheDocument();
      expect(mockReplace).not.toHaveBeenCalled();
    });

    it("タグは上限（30件）まで追加でき、上限に達すると追加できないこと", () => {
      render(<PortfolioEditForm accountId="user1" />);

      const addTag = screen.getByRole("button", { name: "＋タグを追加" });
      for (let i = 0; i < 30; i++) {
        fireEvent.click(addTag);
      }

      expect(screen.getByLabelText("タグ30（日本語）")).toBeInTheDocument();
      expect(addTag).toBeDisabled();
    });

    it("「＋タグを追加」ボタンは、タグの入力欄より後（タグ欄の下）に表示すること", () => {
      render(<PortfolioEditForm accountId="user1" />);

      const addTag = screen.getByRole("button", { name: "＋タグを追加" });
      fireEvent.click(addTag);

      const tagInput = screen.getByLabelText("タグ1（日本語）");
      expect(
        tagInput.compareDocumentPosition(addTag) & Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    });

    it("写真選択ダイアログでキャンセルすると、ダイアログ内で選んだ写真を反映しないこと", async () => {
      mockSearchPortfolioPhotos.mockResolvedValue({
        isLast: true,
        photoList: [
          { photoNo: 11, photoJapaneseTitle: "富士山", imageFilePath: "", directionKbn: "vertical" },
        ],
      });

      render(<PortfolioEditForm accountId="user1" />);
      fireEvent.click(screen.getByRole("button", { name: "写真を選択" }));
      const dialog = await screen.findByTestId("photo-picker-dialog");
      fireEvent.click(await within(dialog).findByRole("button", { name: "写真11（富士山）" }));
      expect(within(dialog).getByText("選択中 1 / 200枚")).toBeInTheDocument();
      fireEvent.click(within(dialog).getByRole("button", { name: "キャンセル" }));

      expect(screen.queryByTestId("photo-picker-dialog")).not.toBeInTheDocument();
      expect(screen.queryAllByTestId("portfolio-photo-item")).toHaveLength(0);
      expect(screen.getByText("写真が選択されていません")).toBeInTheDocument();
    });

    it("フィーチャー型のプレビューは、写真ごとにテーマ・サブテーマ・説明をダイアログで表示すること", async () => {
      mockSearchPortfolioPhotos.mockResolvedValue({
        isLast: true,
        photoList: [
          { photoNo: 11, photoJapaneseTitle: "富士山", imageFilePath: "", directionKbn: "vertical" },
          { photoNo: 12, photoJapaneseTitle: "茶畑", imageFilePath: "", directionKbn: "horizontal" },
        ],
      });

      render(<PortfolioEditForm accountId="user1" />);
      // 写真がない間はプレビューできない
      expect(screen.queryByRole("button", { name: "プレビューを表示" })).not.toBeInTheDocument();

      fireEvent.click(screen.getByRole("button", { name: "写真を選択" }));
      const picker = await screen.findByTestId("photo-picker-dialog");
      fireEvent.click(await within(picker).findByRole("button", { name: "写真11（富士山）" }));
      fireEvent.click(within(picker).getByRole("button", { name: "写真12（茶畑）" }));
      fireEvent.click(within(picker).getByRole("button", { name: "完了" }));
      fireEvent.change(screen.getByLabelText("富士山のテーマ"), { target: { value: "日本の山" } });
      fireEvent.change(screen.getByLabelText("富士山のサブテーマ"), { target: { value: "静岡" } });

      fireEvent.click(screen.getByRole("button", { name: "プレビューを表示" }));

      const dialog = screen.getByRole("dialog", { name: "プレビュー" });
      const sections = within(dialog).getAllByTestId("feature-preview-section");
      expect(sections).toHaveLength(2);
      expect(within(sections[0]).getByRole("heading", { name: "日本の山" })).toBeInTheDocument();
      expect(within(sections[0]).getByText("静岡")).toBeInTheDocument();
      // テーマが未入力の写真は、公開できないことを示す
      expect(within(sections[1]).getByText(/テーマが未入力です/)).toBeInTheDocument();
    });

    it("一覧へ戻るリンクを、見出しより前（左上）に表示すること", () => {
      render(<PortfolioEditForm accountId="user1" />);

      const backLink = screen.getByRole("link", { name: /ポートフォリオ一覧へ戻る/ });
      expect(backLink).toHaveAttribute("href", "/user1/portfolio_setting");
      const heading = screen.getByRole("heading", { level: 1, name: "ポートフォリオ作成" });
      expect(
        backLink.compareDocumentPosition(heading) & Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    });

    it("選択した写真とフィーチャー型の項目だけを送って下書き登録し、編集用URLへ切り替えること", async () => {
      mockSearchPortfolioPhotos.mockResolvedValue({
        isLast: true,
        photoList: [
          { photoNo: 11, photoJapaneseTitle: "富士山", imageFilePath: "", directionKbn: "horizontal" },
          { photoNo: 12, photoJapaneseTitle: "", imageFilePath: "", directionKbn: "square" },
        ],
      });
      mockSavePortfolio.mockResolvedValue({
        isSuccess: true,
        message: "ポートフォリオを登録しました。",
        portfolioNo: 7,
      });

      render(<PortfolioEditForm accountId="user1" />);

      expect(mockGetPortfolioDetail).not.toHaveBeenCalled();
      fireEvent.change(screen.getByLabelText(/テーマ/, { selector: "#portfolio-theme" }), {
        target: { value: "日本の自然" },
      });

      // 写真を選択する（選択した順に表示順の末尾へ追加される）
      fireEvent.click(screen.getByRole("button", { name: "写真を選択" }));
      const dialog = await screen.findByTestId("photo-picker-dialog");
      fireEvent.click(await within(dialog).findByRole("button", { name: "写真12" }));
      fireEvent.click(within(dialog).getByRole("button", { name: "写真11（富士山）" }));
      expect(within(dialog).getByRole("button", { name: "写真12" })).toHaveAttribute(
        "aria-pressed",
        "true"
      );
      fireEvent.click(within(dialog).getByRole("button", { name: "完了" }));
      expect(screen.getAllByTestId("portfolio-photo-item")).toHaveLength(2);

      fireEvent.change(screen.getByLabelText("富士山のテーマ"), {
        target: { value: "日本の山" },
      });

      fireEvent.click(screen.getByRole("button", { name: "下書きとして登録" }));

      await waitFor(() => {
        expect(mockSavePortfolio).toHaveBeenCalledWith(
          "user1",
          {
            theme: "日本の自然",
            description: "",
            templateKbn: "feature",
            templateSettings: {},
            photoList: [
              { photoNo: 12, photoSettings: {} },
              { photoNo: 11, photoSettings: { theme: "日本の山" } },
            ],
            tagList: [],
          },
          undefined
        );
      });
      expect(mockReplace).toHaveBeenCalledWith("/user1/portfolio_setting/edit?portfolioNo=7");
      expect(await screen.findByText("ポートフォリオを登録しました。")).toBeInTheDocument();
      // 保存済みになったため、公開状態を切り替えられる
      expect(screen.getByRole("button", { name: "公開する" })).toBeEnabled();
    });
  });

  describe("編集", () => {
    it("登録済みの内容を読み込み、グリッド型の行の枚数と写真の枚数を表示すること", async () => {
      mockGetPortfolioDetail.mockResolvedValue(gridDetail);

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);

      expect(await screen.findByDisplayValue("日本の自然")).toBeInTheDocument();
      expect(mockGetPortfolioDetail).toHaveBeenCalledWith("user1", 3);
      expect(screen.getByTestId("portfolio-status")).toHaveTextContent("公開中");
      // グリッド型は写真単位の設定を持たないため、写真ごとの入力欄を表示しない
      expect(screen.queryByLabelText("金閣寺のキャプション")).not.toBeInTheDocument();
      expect(screen.queryByLabelText("金閣寺のテーマ")).not.toBeInTheDocument();
      expect(screen.getAllByTestId("grid-row-item")).toHaveLength(1);
      expect(screen.getByTestId("grid-photo-count")).toHaveTextContent(
        "行の枚数の合計 2枚 / 写真 2枚"
      );
      expect(screen.getByLabelText("タグ1（日本語）")).toHaveValue("自然");
    });

    it("グリッド型のプレビューは画面内に表示せず、ボタンを押すとダイアログで表示すること", async () => {
      mockGetPortfolioDetail.mockResolvedValue(gridDetail);

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);
      await screen.findByDisplayValue("日本の自然");
      expect(screen.queryByTestId("grid-preview")).not.toBeInTheDocument();

      fireEvent.click(screen.getByRole("button", { name: "プレビューを表示" }));
      const dialog = screen.getByRole("dialog", { name: "プレビュー" });
      expect(within(dialog).getByTestId("grid-preview")).toBeInTheDocument();
      expect(within(dialog).getAllByTestId("grid-preview-row")).toHaveLength(1);

      fireEvent.click(within(dialog).getByRole("button", { name: "閉じる" }));
      expect(screen.queryByRole("dialog", { name: "プレビュー" })).not.toBeInTheDocument();
    });

    it("行を追加すると枚数の不一致を表示し、写真の並べ替えとグリッド型の項目だけを送って更新すること", async () => {
      mockGetPortfolioDetail.mockResolvedValue(gridDetail);
      mockSavePortfolio.mockResolvedValue({
        isSuccess: true,
        message: "ポートフォリオを更新しました。",
        portfolioNo: 3,
      });

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);
      await screen.findByDisplayValue("日本の自然");

      fireEvent.click(screen.getByRole("button", { name: "行を追加" }));
      expect(screen.getByTestId("grid-photo-count")).toHaveTextContent(
        "行の枚数の合計 4枚 / 写真 2枚（公開するには一致させてください）"
      );
      fireEvent.change(screen.getByLabelText("2行目の組み合わせ"), {
        target: { value: "THREE_EQUAL" },
      });
      fireEvent.click(screen.getByRole("button", { name: "茶畑を上へ移動" }));

      fireEvent.click(screen.getByRole("button", { name: "保存" }));

      await waitFor(() => {
        expect(mockSavePortfolio).toHaveBeenCalledWith(
          "user1",
          {
            theme: "日本の自然",
            description: "説明文",
            templateKbn: "grid",
            templateSettings: { rows: [{ pattern: "TWO_EQUAL" }, { pattern: "THREE_EQUAL" }] },
            photoList: [
              { photoNo: 8, photoSettings: {} },
              { photoNo: 5, photoSettings: {} },
            ],
            tagList: [{ tagJapaneseName: "自然", tagEnglishName: "nature" }],
          },
          3
        );
      });
      expect(mockReplace).not.toHaveBeenCalled();
    });

    it("未保存の変更がある間は公開状態を切り替えられないこと", async () => {
      mockGetPortfolioDetail.mockResolvedValue(gridDetail);

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);
      await screen.findByDisplayValue("日本の自然");
      expect(screen.getByRole("button", { name: "下書きに戻す" })).toBeEnabled();

      fireEvent.change(screen.getByDisplayValue("説明文"), { target: { value: "変更" } });

      expect(screen.getByRole("button", { name: "下書きに戻す" })).toBeDisabled();
      expect(
        screen.getByText("公開状態を切り替えるには、先に変更を保存してください")
      ).toBeInTheDocument();
    });

    it("公開状態を切り替えると状態の表示を更新し、失敗した場合はエラーを表示すること", async () => {
      mockGetPortfolioDetail.mockResolvedValue({ ...gridDetail, statusKbn: "draft" });
      mockUpdatePortfolioStatus.mockRejectedValueOnce(
        new Error("公開に必要な設定が不足しています。テンプレートの設定内容を確認してください。")
      );
      mockUpdatePortfolioStatus.mockResolvedValueOnce({
        isSuccess: true,
        message: "ポートフォリオを公開しました。",
        portfolioNo: 3,
      });

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);
      await screen.findByDisplayValue("日本の自然");

      fireEvent.click(screen.getByRole("button", { name: "公開する" }));
      expect(
        await screen.findByText(
          "公開に必要な設定が不足しています。テンプレートの設定内容を確認してください。"
        )
      ).toBeInTheDocument();
      expect(screen.getByTestId("portfolio-status")).toHaveTextContent("下書き");

      fireEvent.click(screen.getByRole("button", { name: "公開する" }));
      expect(await screen.findByText("ポートフォリオを公開しました。")).toBeInTheDocument();
      expect(mockUpdatePortfolioStatus).toHaveBeenLastCalledWith("user1", 3, "published");
      expect(screen.getByTestId("portfolio-status")).toHaveTextContent("公開中");
      expect(screen.getByRole("button", { name: "下書きに戻す" })).toBeInTheDocument();
    });

    it("削除を確認すると削除して一覧へ戻ること", async () => {
      mockGetPortfolioDetail.mockResolvedValue(gridDetail);
      mockDeletePortfolio.mockResolvedValue({ isSuccess: true });

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={3} />);
      await screen.findByDisplayValue("日本の自然");

      fireEvent.click(screen.getByRole("button", { name: "削除" }));
      expect(screen.getByText("ポートフォリオを削除してもよろしいですか？")).toBeInTheDocument();
      expect(screen.getByText("削除したポートフォリオは復元できません。")).toBeInTheDocument();
      fireEvent.click(screen.getByRole("button", { name: "削除する" }));

      await waitFor(() => {
        expect(mockDeletePortfolio).toHaveBeenCalledWith("user1", 3);
        expect(mockPush).toHaveBeenCalledWith("/user1/portfolio_setting");
      });
    });

    it("読み込みに失敗した場合はエラーと一覧へのリンクを表示すること", async () => {
      mockGetPortfolioDetail.mockRejectedValue(new Error("ポートフォリオが存在しません。"));

      render(<PortfolioEditForm accountId="user1" initialPortfolioNo={9} />);

      expect(await screen.findByText("ポートフォリオが存在しません。")).toBeInTheDocument();
      expect(screen.getByRole("link", { name: "ポートフォリオ一覧へ戻る" })).toHaveAttribute(
        "href",
        "/user1/portfolio_setting"
      );
    });
  });

  it("他人のアカウントIDの場合は権限がない旨を表示し、内容を取得しないこと", () => {
    render(<PortfolioEditForm accountId="otheruser1" initialPortfolioNo={3} />);

    expect(screen.getByText("この操作を行う権限がありません")).toBeInTheDocument();
    expect(mockGetPortfolioDetail).not.toHaveBeenCalled();
  });
});
