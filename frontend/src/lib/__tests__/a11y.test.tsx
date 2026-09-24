import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { onActivateKey } from "../a11y";

describe("onActivateKey", () => {
  it("Enterキーでハンドラを実行し、既定動作を抑止すること", () => {
    const handler = jest.fn();
    const preventDefault = jest.fn();
    const onKeyDown = onActivateKey(handler);

    onKeyDown({ key: "Enter", preventDefault } as unknown as React.KeyboardEvent);

    expect(handler).toHaveBeenCalledTimes(1);
    expect(preventDefault).toHaveBeenCalledTimes(1);
  });

  it("Spaceキーでハンドラを実行すること", () => {
    const handler = jest.fn();
    const onKeyDown = onActivateKey(handler);

    onKeyDown({ key: " ", preventDefault: jest.fn() } as unknown as React.KeyboardEvent);

    expect(handler).toHaveBeenCalledTimes(1);
  });

  it("それ以外のキーではハンドラを実行しないこと", () => {
    const handler = jest.fn();
    const onKeyDown = onActivateKey(handler);

    onKeyDown({ key: "a", preventDefault: jest.fn() } as unknown as React.KeyboardEvent);

    expect(handler).not.toHaveBeenCalled();
  });
});

describe("useDialog（ModalDialog経由）", () => {
  it("フォーカス可能な要素が無い場合、Tabキーでコンテナ自身にフォーカスを留めること", () => {
    render(
      <ModalDialog label="確認">
        <p>フォーカス可能な要素なし</p>
      </ModalDialog>
    );

    const dialog = screen.getByRole("dialog");
    expect(dialog).toHaveFocus();

    fireEvent.keyDown(document, { key: "Tab" });

    expect(dialog).toHaveFocus();
  });

  it("Shift+Tabで先頭要素からフォーカスされている場合、最後の要素へ循環すること", () => {
    render(
      <ModalDialog label="確認">
        <button>最初</button>
        <button>最後</button>
      </ModalDialog>
    );

    const first = screen.getByRole("button", { name: "最初" });
    const last = screen.getByRole("button", { name: "最後" });

    first.focus();
    expect(first).toHaveFocus();

    fireEvent.keyDown(document, { key: "Tab", shiftKey: true });

    expect(last).toHaveFocus();
  });

  it("Tabでコンテナ外にフォーカスがある場合、最初の要素へ戻すこと", () => {
    render(
      <>
        <button data-testid="outside">外部ボタン</button>
        <ModalDialog label="確認">
          <button>最初</button>
          <button>最後</button>
        </ModalDialog>
      </>
    );

    const outside = screen.getByTestId("outside");
    const first = screen.getByRole("button", { name: "最初" });

    outside.focus();
    expect(outside).toHaveFocus();

    fireEvent.keyDown(document, { key: "Tab" });

    expect(first).toHaveFocus();
  });
});
