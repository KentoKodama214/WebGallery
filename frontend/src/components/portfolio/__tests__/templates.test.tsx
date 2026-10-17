import { render, screen, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { renderPortfolioLayout } from "../templates/registry";
import type { PortfolioLayout, PublicPhoto } from "@/lib/portfolio-api/types";

function photo(photoNo: number, title = `写真${photoNo}`): PublicPhoto {
  return {
    photoNo,
    // アプリ内の絶対パスは sanitizeImageUrl で許可される
    url: `/photos/${photoNo}.jpg`,
    japaneseTitle: title,
    englishTitle: "",
    direction: "HORIZONTAL",
  };
}

describe("ポートフォリオのテンプレート", () => {
  it("フィーチャー型は写真ごとにテーマ・サブテーマ・説明のセクションを表示し、左右を交互に入れ替えること", () => {
    render(
      <>
        {renderPortfolioLayout({
          __typename: "FeatureLayout",
          sections: [
            { photo: photo(1, "金閣寺"), theme: "日本の自然", subTheme: "京都", description: "金色に輝く" },
            { photo: photo(2), theme: "光と影", subTheme: "", description: "" },
          ],
        })}
      </>
    );

    const sections = screen.getAllByTestId("feature-section");
    expect(sections).toHaveLength(2);
    expect(within(sections[0]).getByRole("heading", { name: "日本の自然" })).toBeInTheDocument();
    expect(within(sections[0]).getByText("京都")).toBeInTheDocument();
    expect(within(sections[0]).getByText("金色に輝く")).toBeInTheDocument();
    expect(within(sections[0]).getByRole("img", { name: "金閣寺" })).toHaveAttribute("src", "/photos/1.jpg");
    expect(sections[0].className).toContain("md:flex-row");
    expect(sections[1].className).toContain("md:flex-row-reverse");
    // 未設定のサブテーマ・説明の段落は表示しない（見出しだけになる）
    expect(sections[0].querySelectorAll("p")).toHaveLength(2);
    expect(sections[1].querySelectorAll("p")).toHaveLength(0);
  });

  it("グリッド型は行パターンごとに写真を並べ、横長1枚+正方形1枚は横幅を2:1にすること", () => {
    render(
      <>
        {renderPortfolioLayout({
          __typename: "GridLayout",
          rows: [
            { pattern: "THREE_EQUAL", photos: [1, 2, 3].map((no) => ({ photo: photo(no), caption: "" })) },
            {
              pattern: "WIDE_NARROW",
              photos: [
                { photo: photo(4), caption: "橋杭岩" },
                { photo: photo(5), caption: "" },
              ],
            },
          ],
        })}
      </>
    );

    const rows = screen.getAllByTestId("grid-row");
    expect(rows.map((row) => row.dataset.pattern)).toEqual(["THREE_EQUAL", "WIDE_NARROW"]);
    expect(within(rows[0]).getAllByRole("img")).toHaveLength(3);
    const wideNarrow = rows[1].querySelectorAll("figure");
    expect((wideNarrow[0] as HTMLElement).style.flex).toContain("2");
    expect((wideNarrow[1] as HTMLElement).style.flex).toContain("1");
    expect(within(rows[1]).getByText("橋杭岩")).toBeInTheDocument();
  });

  it("画像URLが許可されたオリジンでない場合は、画像の代わりに写真のタイトルを表示すること", () => {
    render(
      <>
        {renderPortfolioLayout({
          __typename: "GridLayout",
          rows: [
            {
              pattern: "TWO_EQUAL",
              photos: [
                { photo: { ...photo(1, "外部画像"), url: "https://evil.example.com/x.jpg" }, caption: "" },
                { photo: photo(2), caption: "" },
              ],
            },
          ],
        })}
      </>
    );

    expect(screen.queryByRole("img", { name: "外部画像" })).not.toBeInTheDocument();
    expect(screen.getByText("外部画像")).toBeInTheDocument();
  });

  it("未対応のレイアウトの型の場合は、画面を壊さずにメッセージを表示すること", () => {
    render(<>{renderPortfolioLayout({ __typename: "SlideLayout" } as unknown as PortfolioLayout)}</>);

    expect(screen.getByText("このポートフォリオは表示できません")).toBeInTheDocument();
  });
});
