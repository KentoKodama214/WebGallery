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

describe("PhotoPickerDialog", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("写真の枠を向き（縦3:4・横4:3・正方形1:1）に合わせた縦横比で表示し、向きを表示すること", async () => {
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
    expect(within(items[0]).getByText("縦")).toBeInTheDocument();
    expect(within(items[1]).getByText("横")).toBeInTheDocument();
    expect(within(items[2]).getByText("正方形")).toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenCalledWith("user1", {}, 1);
  });

  it("タイトル・場所・タグで検索し、「もっと見る」では同じ条件で次のページを読み込むこと", async () => {
    mockSearchPortfolioPhotos
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(1, "vertical")] })
      .mockResolvedValueOnce({ isLast: false, photoList: [candidate(2, "horizontal", "金閣寺")] })
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(3, "square", "銀閣寺")] });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );
    await screen.findAllByTestId("photo-picker-item");

    fireEvent.change(screen.getByLabelText("タイトル"), { target: { value: " 閣寺 " } });
    fireEvent.change(screen.getByLabelText("場所"), { target: { value: "京都" } });
    fireEvent.change(screen.getByLabelText("タグ"), { target: { value: "寺" } });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(await screen.findByRole("button", { name: "写真2（金閣寺）" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "写真1" })).not.toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenLastCalledWith(
      "user1",
      { title: "閣寺", location: "京都", tag: "寺" },
      1
    );

    fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));

    expect(await screen.findByRole("button", { name: "写真3（銀閣寺）" })).toBeInTheDocument();
    expect(mockSearchPortfolioPhotos).toHaveBeenLastCalledWith(
      "user1",
      { title: "閣寺", location: "京都", tag: "寺" },
      2
    );
  });

  it("条件に一致する写真がない場合は、その旨を表示すること", async () => {
    mockSearchPortfolioPhotos
      .mockResolvedValueOnce({ isLast: true, photoList: [candidate(1, "vertical")] })
      .mockResolvedValueOnce({ isLast: true, photoList: [] });

    render(
      <PhotoPickerDialog accountId="user1" initialSelected={[]} onComplete={jest.fn()} onCancel={jest.fn()} />
    );
    await screen.findAllByTestId("photo-picker-item");

    fireEvent.change(screen.getByLabelText("タグ"), { target: { value: "存在しない" } });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(await screen.findByText("条件に一致する写真がありません")).toBeInTheDocument();
  });

  it("完了で、開く前の選択（表示順）の後ろに新しく選んだ写真を選んだ順に返し、選択を外した写真は除くこと", async () => {
    mockSearchPortfolioPhotos.mockResolvedValue({
      isLast: true,
      photoList: [candidate(1, "vertical", "富士山"), candidate(2, "horizontal"), candidate(3, "square")],
    });
    const onComplete = jest.fn();

    render(
      <PhotoPickerDialog
        accountId="user1"
        initialSelected={[
          { photoNo: 3, imageFilePath: "", label: "写真3" },
          { photoNo: 2, imageFilePath: "", label: "写真2" },
        ]}
        onComplete={onComplete}
        onCancel={jest.fn()}
      />
    );
    await screen.findAllByTestId("photo-picker-item");
    expect(screen.getByText("選択中 2 / 200枚")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "写真2" }));
    fireEvent.click(screen.getByRole("button", { name: "写真1（富士山）" }));
    fireEvent.click(screen.getByRole("button", { name: "完了" }));

    await waitFor(() => {
      expect(onComplete).toHaveBeenCalledWith([
        { photoNo: 3, imageFilePath: "", label: "写真3" },
        { photoNo: 1, imageFilePath: "", label: "富士山" },
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
    fireEvent.click(await screen.findByRole("button", { name: "写真1" }));
    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(onCancel).toHaveBeenCalled();
    expect(onComplete).not.toHaveBeenCalled();
  });
});
