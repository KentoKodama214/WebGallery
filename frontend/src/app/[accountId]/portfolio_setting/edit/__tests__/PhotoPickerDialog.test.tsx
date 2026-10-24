import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PhotoPickerDialog } from "../PhotoPickerDialog";

const mockSearchPortfolioPhotos = jest.fn();

jest.mock("@/lib/api/client", () => ({
  searchPortfolioPhotos: (...args: unknown[]) => mockSearchPortfolioPhotos(...args),
}));

/**
 * 検索結果の写真を生成する
 *
 * @param photoNo 写真番号
 * @param directionKbn 向き区分
 * @param title タイトル
 * @returns 検索結果の写真
 */
function candidate(photoNo: number, directionKbn: string, title = "") {
  return { photoNo, photoJapaneseTitle: title, imageFilePath: "", directionKbn };
}

/**
 * 一覧に表示されている写真ボタンの名前を、表示順に取得する
 *
 * @param testId 一覧の data-testid
 * @returns ボタンの名前のリスト
 */
function tileNames(testId: string): string[] {
  return within(screen.getByTestId(testId))
    .queryAllByRole("button")
    .filter((button) => button.textContent !== "＋もっと見る")
    .map((button) => button.getAttribute("aria-label") ?? "");
}

describe("PhotoPickerDialog", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("写真の枠を向き（縦3:4・横4:3・正方形1:1）に合わせた縦横比で表示し、向きのラベルは表示しないこと", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({
      isLast: true,
      photoList: [candidate(1, "vertical"), candidate(2, "horizontal"), candidate(3, "square")],
    });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );

    const items = await screen.findAllByTestId("photo-picker-item");
    const frames = items.map((item) => within(item).getByRole("button"));
    expect(frames.map((frame) => frame.style.getPropertyValue("aspect-ratio"))).toEqual([
      "3 / 4",
      "4 / 3",
      "1 / 1",
    ]);
    expect(screen.queryByText("縦")).not.toBeInTheDocument();
    expect(screen.queryByText("横")).not.toBeInTheDocument();
    expect(screen.queryByText("正方形")).not.toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenCalledWith("user1", {}, 1);
  });

  it("1つの検索語で検索し、「もっと見る」では同じ検索語で次のページを読み込むこと", async () => {
    mockSearchPortfolioPhotos
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(1, "vertical")] })
      .mockResolvedValueOnce({ isLast: false, photoList: [candidate(2, "horizontal", "金閣寺")] })
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(3, "square", "銀閣寺")] });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );
    await screen.findAllByTestId("photo-picker-item");

    // 検索語の入力欄は1つだけ
    expect(within(screen.getByRole("search")).getAllByRole("textbox")).toHaveLength(1);
    fireEvent.change(screen.getByLabelText("検索語（タイトル・場所・住所・タグ）"), {
      target: { value: " 閣寺 " },
    });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(await screen.findByRole("button", { name: "写真2（金閣寺）を選択" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "写真1を選択" })).not.toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenLastCalledWith("user1", { keyword: "閣寺" }, 1);

    fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));

    expect(await screen.findByRole("button", { name: "写真3（銀閣寺）を選択" })).toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenLastCalledWith("user1", { keyword: "閣寺" }, 2);
  });

  it("条件に一致する写真がない場合は、その旨を表示すること", async () => {
    mockSearchPortfolioPhotos
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(1, "vertical")] })
      .mockResolvedValueOnce({ isLast: true, photoList: [] });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );
    await screen.findAllByTestId("photo-picker-item");

    fireEvent.change(screen.getByLabelText("検索語（タイトル・場所・住所・タグ）"), {
      target: { value: "存在しない" },
    });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(await screen.findByText("条件に一致する写真がありません")).toBeInTheDocument();
  });

  it("未選択の写真を上、選択済みの写真を下に分けて表示し、押すと互いの一覧へ移ること", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({
      isLast: true,
      photoList: [candidate(1, "vertical", "富士山"), candidate(2, "horizontal"), candidate(3, "square")],
    });

    render(
      <PhotoPickerDialog
        accountId="user1"
        initialSelected={[{ photoNo: 3, imageFilePath: "", label: "写真3", directionKbn: "square" }]}
        onComplete={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    await screen.findAllByTestId("photo-picker-item");

    // 未選択の一覧が選択済みの一覧より前（上）にある
    const unselected = screen.getByTestId("photo-picker-unselected");
    const selected = screen.getByTestId("photo-picker-selected");
    expect(unselected.compareDocumentPosition(selected) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(tileNames("photo-picker-unselected")).toEqual(["写真1（富士山）を選択", "写真2を選択"]);
    expect(tileNames("photo-picker-selected")).toEqual(["写真3の選択を解除"]);

    // 未選択を押すと選択済みの末尾に追加される
    fireEvent.click(screen.getByRole("button", { name: "写真1（富士山）を選択" }));
    expect(tileNames("photo-picker-unselected")).toEqual(["写真2を選択"]);
    expect(tileNames("photo-picker-selected")).toEqual([
      "写真3の選択を解除",
      "写真1（富士山）の選択を解除",
    ]);
    expect(within(selected).getByText("2")).toBeInTheDocument();
    expect(screen.getByText("選択中 2 / 200枚")).toBeInTheDocument();

    // 選択済みを押すと解除され、未選択の一覧（検索結果の順）に戻る
    fireEvent.click(screen.getByRole("button", { name: "写真3の選択を解除" }));
    expect(tileNames("photo-picker-unselected")).toEqual(["写真2を選択", "写真3を選択"]);
    expect(tileNames("photo-picker-selected")).toEqual(["写真1（富士山）の選択を解除"]);
  });

  it("選択済みの写真がない場合は、その旨を表示すること", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({ isLast: true, photoList: [candidate(1, "vertical")] });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );
    await screen.findAllByTestId("photo-picker-item");

    expect(
      within(screen.getByTestId("photo-picker-selected")).getByText("選択済みの写真はありません")
    ).toBeInTheDocument();
  });

  it("検索結果がすべて選択済みの場合は、未選択の写真がないことを表示すること", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({ isLast: true, photoList: [candidate(1, "vertical")] });

    render(
      <PhotoPickerDialog
        accountId="user1"
        initialSelected={[{ photoNo: 1, imageFilePath: "", label: "写真1", directionKbn: "vertical" }]}
        onComplete={jest.fn()}
        onCancel={jest.fn()}
      />
    );

    expect(await screen.findByText("未選択の写真はありません")).toBeInTheDocument();
  });

  it("完了で、選択済みの一覧の順（開く前の選択の後ろに新しく選んだ順）に返し、解除した写真は除くこと", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({
      isLast: true,
      photoList: [candidate(1, "vertical", "富士山"), candidate(2, "horizontal"), candidate(3, "square")],
    });
    const onComplete = jest.fn();

    render(
      <PhotoPickerDialog
        accountId="user1"
        initialSelected={[
          { photoNo: 3, imageFilePath: "", label: "写真3", directionKbn: "square" },
          { photoNo: 2, imageFilePath: "", label: "写真2", directionKbn: "horizontal" },
        ]}
        onComplete={onComplete}
        onCancel={jest.fn()}
      />
    );
    await screen.findAllByTestId("photo-picker-item");
    expect(screen.getByText("選択中 2 / 200枚")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "写真2の選択を解除" }));
    fireEvent.click(screen.getByRole("button", { name: "写真1（富士山）を選択" }));
    fireEvent.click(screen.getByRole("button", { name: "完了" }));

    await waitFor(() => {
      expect(onComplete).toHaveBeenCalledWith([
        { photoNo: 3, imageFilePath: "", label: "写真3", directionKbn: "square" },
        { photoNo: 1, imageFilePath: "", label: "富士山", directionKbn: "vertical" },
      ]);
    });
  });

  it("キャンセルでは選択を返さずに閉じること", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({ isLast: true, photoList: [candidate(1, "vertical")] });
    const onComplete = jest.fn();
    const onCancel = jest.fn();

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={onComplete} onCancel={onCancel} />
    );
    fireEvent.click(await screen.findByRole("button", { name: "写真1を選択" }));
    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(onCancel).toHaveBeenCalled();
    expect(onComplete).not.toHaveBeenCalled();
  });
});
