import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { GridPreview } from "../GridPreview";
import { EMPTY_PHOTO_SETTINGS, type SelectedPhoto } from "../types";

/**
 * 表示用の写真を生成する（画像URLは表示できない値にして、写真の名前で判別する）
 *
 * @param photoNo 写真番号
 * @returns {@link SelectedPhoto}
 */
function photo(photoNo: number): SelectedPhoto {
  return {
    photoNo,
    imageFilePath: "",
    label: `写真${photoNo}`,
    settings: { ...EMPTY_PHOTO_SETTINGS },
  };
}

describe("GridPreview", () => {
  it("行パターンの枚数ずつ、写真を表示順に各行へ割り当てること", () => {
    render(
      <GridPreview
        photos={[1, 2, 3, 4, 5, 6, 7].map(photo)}
        rows={["TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW"]}
      />
    );

    const rows = screen.getAllByTestId("grid-preview-row");
    expect(rows).toHaveLength(3);
    expect(rows[0]).toHaveTextContent("写真1写真2");
    expect(rows[1]).toHaveTextContent("写真3写真4写真5");
    expect(rows[2]).toHaveTextContent("写真6写真7");
    expect(screen.queryByText(/行に割り当てられていない写真/)).not.toBeInTheDocument();
    expect(screen.queryByText("空き")).not.toBeInTheDocument();
  });

  it("写真が足りない場合は空き枠を表示すること", () => {
    render(<GridPreview photos={[photo(1)]} rows={["THREE_EQUAL"]} />);

    expect(screen.getAllByText("空き")).toHaveLength(2);
  });

  it("写真が余る場合は、行に割り当てられていない写真の枚数を表示すること", () => {
    render(<GridPreview photos={[1, 2, 3].map(photo)} rows={["TWO_EQUAL"]} />);

    expect(screen.getByText("行に割り当てられていない写真が1枚あります")).toBeInTheDocument();
  });
});
