import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FrameTemplateList } from "../FrameTemplateList";

const mockGetFrameTemplateList = jest.fn();
const mockDeleteFrameTemplate = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getFrameTemplateList: (...args: unknown[]) => mockGetFrameTemplateList(...args),
  deleteFrameTemplate: (...args: unknown[]) => mockDeleteFrameTemplate(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

/** 額縁テンプレート（写真枠2・メッセージ枠1） */
function template(frameTemplateNo: number, templateName = `額縁${frameTemplateNo}`) {
  return {
    frameTemplateNo,
    templateName,
    frameDefinition: {
      size: { width: 366, height: 254 },
      slots: [
        { x: 5, y: 10, width: 40, height: 60 },
        { x: 55, y: 10, width: 40, height: 60 },
      ],
      texts: [{ x: 10, y: 80, width: 80, fontFamily: "mincho", fontSize: 3 }],
    },
    createdAt: "2026-01-01T00:00:00+09:00",
    updatedAt: "2026-01-01T00:00:00+09:00",
  };
}

describe("FrameTemplateList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({ user: { accountId: "user1", accountNo: 1 } });
  });

  it("本人の額縁テンプレートを、額縁の見本・写真枠の数・サイズとあわせて表示し、編集画面へリンクすること", async () => {
    mockGetFrameTemplateList.mockResolvedValue({
      frameTemplateList: [template(1, "横長 2枚組"), template(2)],
    });

    render(<FrameTemplateList accountId="user1" />);

    const items = await screen.findAllByTestId("frame-template-item");
    expect(items).toHaveLength(2);
    expect(mockGetFrameTemplateList).toHaveBeenCalledWith("user1");
    expect(within(items[0]).getByText("横長 2枚組")).toBeInTheDocument();
    expect(within(items[0]).getByText(/写真枠 2・メッセージ枠 1・366×254mm/)).toBeInTheDocument();
    expect(within(items[0]).getByRole("img", { name: "横長 2枚組の見本" })).toBeInTheDocument();
    expect(within(items[0]).getAllByTestId("frame-slot")).toHaveLength(2);
    expect(within(items[0]).getByRole("link", { name: "横長 2枚組を編集" })).toHaveAttribute(
      "href",
      "/user1/portfolio_setting/frame_templates/edit?frameTemplateNo=1"
    );
    expect(screen.getByRole("link", { name: "新規作成" })).toHaveAttribute(
      "href",
      "/user1/portfolio_setting/frame_templates/edit"
    );
  });

  it("額縁テンプレートがない場合は、その旨を表示すること", async () => {
    mockGetFrameTemplateList.mockResolvedValue({ frameTemplateList: [] });

    render(<FrameTemplateList accountId="user1" />);

    expect(await screen.findByText("額縁テンプレートがありません")).toBeInTheDocument();
  });

  it("作成数が上限（50個）に達している場合は、新規作成できず上限に達した旨を表示すること", async () => {
    mockGetFrameTemplateList.mockResolvedValue({
      frameTemplateList: Array.from({ length: 50 }, (_, i) => template(i + 1)),
    });

    render(<FrameTemplateList accountId="user1" />);

    expect(await screen.findByRole("button", { name: "新規作成" })).toBeDisabled();
    expect(screen.queryByRole("link", { name: "新規作成" })).not.toBeInTheDocument();
    expect(screen.getByText(/上限に達しています/)).toBeInTheDocument();
  });

  it("取得に失敗した場合は、エラーを表示すること", async () => {
    mockGetFrameTemplateList.mockRejectedValue(new Error("額縁テンプレート一覧の取得に失敗しました"));

    render(<FrameTemplateList accountId="user1" />);

    expect(await screen.findByText("額縁テンプレート一覧の取得に失敗しました")).toBeInTheDocument();
  });

  it("削除を確認すると額縁テンプレートを削除し、一覧から取り除くこと", async () => {
    mockGetFrameTemplateList.mockResolvedValue({ frameTemplateList: [template(1), template(2)] });
    mockDeleteFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを削除しました。" });

    render(<FrameTemplateList accountId="user1" />);

    fireEvent.click(await screen.findByRole("button", { name: "額縁1を削除" }));
    const dialog = screen.getByRole("dialog", { name: "額縁テンプレートの削除確認" });
    expect(within(dialog).getByText(/選択時にコピーした額縁で表示され続けます/)).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole("button", { name: "削除する" }));

    await waitFor(() => expect(screen.getAllByTestId("frame-template-item")).toHaveLength(1));
    expect(mockDeleteFrameTemplate).toHaveBeenCalledWith("user1", 1);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("削除に失敗した場合は、確認ダイアログを開いたままエラーを表示し、一覧から取り除かないこと", async () => {
    mockGetFrameTemplateList.mockResolvedValue({ frameTemplateList: [template(1)] });
    mockDeleteFrameTemplate.mockRejectedValue(new Error("額縁テンプレートが存在しません。"));

    render(<FrameTemplateList accountId="user1" />);

    fireEvent.click(await screen.findByRole("button", { name: "額縁1を削除" }));
    fireEvent.click(screen.getByRole("button", { name: "削除する" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("額縁テンプレートが存在しません。");
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getAllByTestId("frame-template-item")).toHaveLength(1);
  });

  it("他人のアカウントの画面を開いた場合は、取得せずに権限がない旨を表示すること", () => {
    render(<FrameTemplateList accountId="other" />);

    expect(screen.getByText("この操作を行う権限がありません")).toBeInTheDocument();
    expect(mockGetFrameTemplateList).not.toHaveBeenCalled();
  });
});
