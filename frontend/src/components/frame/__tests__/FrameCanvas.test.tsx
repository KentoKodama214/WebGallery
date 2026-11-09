import { render, screen, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FrameCanvas, frameBoxStyle } from "../FrameCanvas";

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
};

describe("FrameCanvas", () => {
  it("額縁のサイズの縦横比・背景・外枠で描画し、写真枠・メッセージ枠を位置・傾き・フォントどおりに配置すること", () => {
    render(
      <FrameCanvas
        size={{ width: 366, height: 254 }}
        backgroundColor="#f5f0e6"
        borderColor="#3b2a1a"
        borderWidth={2.5}
        label="組写真：金閣寺"
        slots={[{ ...slot, photo: { url: "/images/1.jpg", alt: "金閣寺" } }]}
        texts={[{ ...text, text: "春の訪れ" }]}
      />
    );

    const canvas = screen.getByRole("img", { name: "組写真：金閣寺" });
    expect(canvas).toHaveStyle({ aspectRatio: "366 / 254", backgroundColor: "#3b2a1a", padding: "2.5cqw" });
    expect(within(canvas).getByTestId("frame-mat")).toHaveStyle({ backgroundColor: "#f5f0e6" });

    const slotElement = screen.getByTestId("frame-slot");
    expect(slotElement).toHaveStyle({ left: "10%", top: "20%", width: "30%", height: "40%", transform: "rotate(-5deg)" });
    expect(within(slotElement).getByAltText("金閣寺")).toHaveAttribute("src", "/images/1.jpg");

    const textElement = screen.getByTestId("frame-text");
    expect(textElement).toHaveTextContent("春の訪れ");
    expect(textElement).toHaveStyle({ fontSize: "3cqw", textAlign: "center", fontWeight: "700", color: "#333333" });
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
    expect(screen.getByTestId("frame-overlay")).toHaveStyle({ inset: "3cqw" });
  });

  describe("frameBoxStyle", () => {
    it("傾きが0の場合は回転させず、高さを持たない枠は高さを指定しないこと", () => {
      const style = frameBoxStyle({ x: 1, y: 2, width: 3, rotation: 0 });

      expect(style.transform).toBeUndefined();
      expect(style.height).toBeUndefined();
      expect(style).toMatchObject({ left: "1%", top: "2%", width: "3%" });
    });
  });
});
