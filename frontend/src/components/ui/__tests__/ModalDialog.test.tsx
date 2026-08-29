import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { ModalDialog } from "../ModalDialog";

/** 開閉できるダイアログを持つテスト用ホスト */
function Host() {
  const [open, setOpen] = useState(false);
  return (
    <div>
      <button type="button" onClick={() => setOpen(true)}>
        開く
      </button>
      {open && (
        <ModalDialog
          label="確認"
          onClose={() => setOpen(false)}
          overlayClassName="overlay"
          containerClassName="container"
        >
          <button type="button">最初</button>
          <button type="button">最後</button>
        </ModalDialog>
      )}
    </div>
  );
}

describe("ModalDialog", () => {
  it("role=dialog と aria-modal, アクセシブルネームを付与する", async () => {
    const user = userEvent.setup();
    render(<Host />);
    await user.click(screen.getByRole("button", { name: "開く" }));

    const dialog = screen.getByRole("dialog");
    expect(dialog).toHaveAttribute("aria-modal", "true");
    expect(dialog).toHaveAccessibleName("確認");
  });

  it("開いたときにダイアログ内の最初の要素へフォーカスを移す", async () => {
    const user = userEvent.setup();
    render(<Host />);
    await user.click(screen.getByRole("button", { name: "開く" }));

    expect(screen.getByRole("button", { name: "最初" })).toHaveFocus();
  });

  it("Escape キーで onClose が呼ばれ、フォーカスが開いた要素へ戻る", async () => {
    const user = userEvent.setup();
    render(<Host />);
    const opener = screen.getByRole("button", { name: "開く" });
    await user.click(opener);

    await user.keyboard("{Escape}");

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(opener).toHaveFocus();
  });

  it("Tab がダイアログ内で循環する（末尾から先頭へ）", async () => {
    const user = userEvent.setup();
    render(<Host />);
    await user.click(screen.getByRole("button", { name: "開く" }));

    const last = screen.getByRole("button", { name: "最後" });
    last.focus();
    await user.tab();

    expect(screen.getByRole("button", { name: "最初" })).toHaveFocus();
  });
});
