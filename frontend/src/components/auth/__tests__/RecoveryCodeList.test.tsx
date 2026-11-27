import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { RecoveryCodeList } from "../RecoveryCodeList";

const CODES = ["ABCD-EFGH-JKMN-PQRS", "0123-4567-89AB-CDEF"];

describe("RecoveryCodeList", () => {
  it("すべてのコードを一覧表示すること", () => {
    render(<RecoveryCodeList codes={CODES} accountId="testuser1" />);

    const items = screen.getAllByRole("listitem");
    expect(items.map((item) => item.textContent)).toEqual(CODES);
  });

  it("コピーで改行区切りのコードをクリップボードへ書き込み、結果を通知すること", async () => {
    const user = userEvent.setup();
    const writeText = jest.spyOn(navigator.clipboard, "writeText").mockResolvedValue(undefined);
    render(<RecoveryCodeList codes={CODES} accountId="testuser1" />);

    await user.click(screen.getByRole("button", { name: "コピー" }));

    expect(writeText).toHaveBeenCalledWith(CODES.join("\n"));
    expect(await screen.findByRole("status")).toHaveTextContent("コピーしました");
    writeText.mockRestore();
  });

  it("クリップボードへ書き込めない場合は、手動での書き写しを促すこと", async () => {
    const user = userEvent.setup();
    const writeText = jest
      .spyOn(navigator.clipboard, "writeText")
      .mockRejectedValue(new Error("denied"));
    render(<RecoveryCodeList codes={CODES} accountId="testuser1" />);

    await user.click(screen.getByRole("button", { name: "コピー" }));

    expect(await screen.findByRole("status")).toHaveTextContent("手動で書き写してください");
    writeText.mockRestore();
  });

  it("ダウンロードで、アカウントIDを含むファイル名のテキストファイルを保存させること", async () => {
    const user = userEvent.setup();
    const createObjectURL = jest.fn(() => "blob:recovery");
    const revokeObjectURL = jest.fn();
    Object.assign(URL, { createObjectURL, revokeObjectURL });
    const click = jest
      .spyOn(HTMLAnchorElement.prototype, "click")
      .mockImplementation(function (this: HTMLAnchorElement) {
        expect(this.download).toBe("webgallery-recovery-codes-testuser1.txt");
        expect(this.href).toBe("blob:recovery");
      });
    render(<RecoveryCodeList codes={CODES} accountId="testuser1" />);

    await user.click(screen.getByRole("button", { name: "ダウンロード" }));

    expect(click).toHaveBeenCalledTimes(1);
    expect(createObjectURL).toHaveBeenCalledWith(expect.any(Blob));
    expect(revokeObjectURL).toHaveBeenCalledWith("blob:recovery");
    click.mockRestore();
  });
});
