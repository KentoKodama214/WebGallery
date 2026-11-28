import { render, screen, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FrameCanvas, frameBoxStyle, mmToCqw, mmToMatPercent } from "../FrameCanvas";

const slot = { x: 10, y: 20, width: 30, height: 40, rotation: -5, borderColor: "#ffffff", borderWidth: 1, shadow: true };
const text = {
  x: 5,
  y: 80,
  width: 90,
  rotation: 0,
  fontFamily: "mincho" as const,
  fontSize: 3,
  color: "#333333",
  align: "center" as const,
  bold: true,
  italic: true,
};

describe("FrameCanvas", () => {
  it("額縁のサイズの縦横比・背景・外枠で描画し、写真枠・メッセージ枠を実寸の位置・傾き・フォントどおりに配置すること", () => {
    // 外枠 5mm のため台紙は 190×140mm。太さ・文字サイズは額縁の横幅 200mm を基準に 1mm が 0.5cqw になる
    render(
      <FrameCanvas
        size={{ width: 200, height: 150 }}
        backgroundColor="#f5f0e6"
        borderColor="#3b2a1a"
        borderWidth={5}
        label="組写真：金閣寺"
        slots={[{ ...slot, photo: { url: "/images/1.jpg", alt: "金閣寺" } }]}
        texts={[{ ...text, text: "春の訪れ" }]}
      />
    );

    const canvas = screen.getByRole("img", { name: "組写真：金閣寺" });
    expect(canvas).toHaveStyle({ aspectRatio: "200 / 150", backgroundColor: "#3b2a1a" });
    // 外枠の太さは額縁自身の padding ではなく台紙の inset で指定する（cqw を額縁の横幅を基準に解決させるため）
    expect(canvas.style.padding).toBe("");
    const mat = within(canvas).getByTestId("frame-mat");
    expect(mat).toHaveStyle({ backgroundColor: "#f5f0e6" });
    expect(mat.style.inset).toBe("2.5cqw");

    const slotElement = screen.getByTestId("frame-slot");
    expect(slotElement.style.left).toBe(`${(10 * 100) / 190}%`);
    expect(slotElement.style.top).toBe(`${(20 * 100) / 140}%`);
    expect(slotElement.style.width).toBe(`${(30 * 100) / 190}%`);
    expect(slotElement.style.height).toBe(`${(40 * 100) / 140}%`);
    expect(slotElement).toHaveStyle({ transform: "rotate(-5deg)" });
    expect(within(slotElement).getByAltText("金閣寺")).toHaveAttribute("src", "/images/1.jpg");

    const textElement = screen.getByTestId("frame-text");
    expect(textElement).toHaveTextContent("春の訪れ");
    // jsdom は cqw を解釈できないため、文字サイズは換算する関数のテストで確認する
    expect(textElement).toHaveStyle({ textAlign: "center", fontWeight: "700", fontStyle: "italic", color: "#333333" });
    expect(textElement.style.fontFamily).toContain("Mincho");
  });

  it("許可されていないURLの写真・写真のない枠は画像を表示せず、空き枠の文言を指定した場合だけ表示すること", () => {
    const { rerender } = render(
      <FrameCanvas
        size={{ width: 300, height: 300 }}
        backgroundColor="#ffffff"
        borderColor="#000000"
        borderWidth={0}
        slots={[
          { ...slot, photo: { url: "javascript:alert(1)", alt: "" } },
          { ...slot, photo: null },
        ]}
        texts={[]}
      />
    );

    expect(screen.queryByRole("img", { name: "" })).toBeInTheDocument();
    expect(document.querySelectorAll("img")).toHaveLength(0);
    expect(screen.queryByText("写真2")).not.toBeInTheDocument();

    rerender(
      <FrameCanvas
        size={{ width: 300, height: 300 }}
        backgroundColor="#ffffff"
        borderColor="#000000"
        borderWidth={0}
        slots={[{ ...slot, photo: null }, { ...slot, photo: null }]}
        texts={[]}
        emptySlotLabel={(index) => `写真${index + 1}`}
      />
    );
    expect(screen.getByText("写真1")).toBeInTheDocument();
    expect(screen.getByText("写真2")).toBeInTheDocument();
  });

  it("文面が空のメッセージ枠は描画しないこと", () => {
    render(
      <FrameCanvas
        size={{ width: 300, height: 300 }}
        backgroundColor="#ffffff"
        borderColor="#000000"
        borderWidth={0}
        slots={[]}
        texts={[{ ...text, text: "" }, { ...text, text: "2つ目" }]}
      />
    );

    expect(screen.getAllByTestId("frame-text")).toHaveLength(1);
    expect(screen.getByTestId("frame-text")).toHaveTextContent("2つ目");
  });

  it("操作用の要素を重ねる場合は、画像として扱わず、台紙と同じ位置の別レイヤーに描画すること", () => {
    render(
      <FrameCanvas
        size={{ width: 300, height: 300 }}
        backgroundColor="#ffffff"
        borderColor="#000000"
        borderWidth={3}
        slots={[]}
        texts={[]}
        label="使われない代替テキスト"
      >
        <button type="button">操作</button>
      </FrameCanvas>
    );

    expect(screen.queryByRole("img")).not.toBeInTheDocument();
    expect(within(screen.getByTestId("frame-overlay")).getByRole("button", { name: "操作" })).toBeInTheDocument();
    expect(screen.getByTestId("frame-overlay")).toHaveStyle({ inset: "1cqw" });
  });

  describe("mmToCqw / mmToMatPercent / frameBoxStyle", () => {
    it("実寸（mm）を額縁の横幅に対する % の cqw に換算すること", () => {
      expect(mmToCqw(30, 300)).toBe("10cqw");
      expect(mmToCqw(0, 300)).toBe("0cqw");
    });

    it("台紙の左上からの実寸（mm）を台紙に対する % に換算すること", () => {
      expect(mmToMatPercent(50, 200)).toBe("25%");
    });

    it("位置・大きさは横方向を台紙の横幅、縦方向を台紙の縦幅で換算し、傾きが0の場合は回転させず、高さを持たない枠は高さを指定しないこと", () => {
      const style = frameBoxStyle({ x: 20, y: 30, width: 50, rotation: 0 }, { width: 200, height: 100 });

      expect(style.transform).toBeUndefined();
      expect(style.height).toBeUndefined();
      expect(style).toMatchObject({ left: "10%", top: "30%", width: "25%" });
      expect(frameBoxStyle({ x: 0, y: 0, width: 1, height: 50, rotation: 3 }, { width: 200, height: 100 })).toMatchObject({
        height: "50%",
        transform: "rotate(3deg)",
      });
    });
  });
});
