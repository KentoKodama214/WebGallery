import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FrameTemplateEditor } from "../FrameTemplateEditor";

const mockGetFrameTemplate = jest.fn();
const mockSaveFrameTemplate = jest.fn();
const mockDeleteFrameTemplate = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getFrameTemplate: (...args: unknown[]) => mockGetFrameTemplate(...args),
  saveFrameTemplate: (...args: unknown[]) => mockSaveFrameTemplate(...args),
  deleteFrameTemplate: (...args: unknown[]) => mockDeleteFrameTemplate(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const mockPush = jest.fn();
const mockReplace = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
}));

// jsdom は PointerEvent を持たず、ポインタ操作の座標（clientX・clientY）を渡せないため、MouseEvent で代用する
class TestPointerEvent extends MouseEvent {
  pointerId: number;
  constructor(type: string, init: PointerEventInit = {}) {
    super(type, init);
    this.pointerId = init.pointerId ?? 1;
  }
}

/** 台紙・枠の大きさ（px）。台紙の 1% は横 10px・縦 7px になる */
const RECT = { left: 0, top: 0, width: 1000, height: 700, right: 1000, bottom: 700, x: 0, y: 0 };

/** 保存済みの額縁テンプレート（写真枠2・メッセージ枠1） */
const savedTemplate = {
  frameTemplateNo: 4,
  templateName: "横長 2枚組",
  frameDefinition: {
    size: { width: 305, height: 254 },
    background: { color: "#f5f0e6" },
    border: { color: "#3b2a1a", width: 4 },
    slots: [
      { x: 5, y: 10, width: 40, height: 60, rotation: 0, borderColor: "#ffffff", borderWidth: 1, shadow: true },
      { x: 55, y: 10, width: 40, height: 60, rotation: 0, borderColor: "#ffffff", borderWidth: 1, shadow: false },
    ],
    texts: [
      { x: 10, y: 80, width: 80, rotation: 0, fontFamily: "mincho", fontSize: 3, color: "#333333", align: "center", bold: false },
    ],
  },
  createdAt: "2026-01-01T00:00:00+09:00",
  updatedAt: "2026-01-01T00:00:00+09:00",
};

/** 数値の入力欄の値を取得する */
function numberValue(label: RegExp): number {
  return Number((screen.getByLabelText(label) as HTMLInputElement).value);
}

/** 保存したリクエストの額縁定義 */
function savedDefinition() {
  return mockSaveFrameTemplate.mock.calls.at(-1)![1].frameDefinition as {
    size: { width: number; height: number };
    slots: { x: number; y: number; width: number; height: number; rotation: number }[];
    texts: { fontFamily: string; bold: boolean }[];
  };
}

describe("FrameTemplateEditor", () => {
  const originalPointerEvent = window.PointerEvent;

  beforeAll(() => {
    window.PointerEvent = TestPointerEvent as unknown as typeof PointerEvent;
  });

  afterAll(() => {
    window.PointerEvent = originalPointerEvent;
  });

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({ user: { accountId: "user1", accountNo: 1 } });
    jest.spyOn(HTMLElement.prototype, "getBoundingClientRect").mockReturnValue({
      ...RECT,
      toJSON: () => RECT,
    } as DOMRect);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("新規作成", () => {
    it("既定の額縁（写真枠3・メッセージ枠1）を表示し、写真枠には当てはめる順の番号を表示すること", () => {
      render(<FrameTemplateEditor accountId="user1" />);

      expect(screen.getByRole("heading", { name: "額縁テンプレート作成" })).toBeInTheDocument();
      expect(screen.getByTestId("frame-element-count")).toHaveTextContent("写真枠 3/30・メッセージ枠 1/10");
      expect(screen.getByRole("button", { name: "写真枠1" })).toBeInTheDocument();
      expect(screen.getByText("写真3")).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: "額縁" })).toBeInTheDocument();
    });

    it("テンプレート名が未入力の場合はエラーを表示し、保存しないこと", async () => {
      render(<FrameTemplateEditor accountId="user1" />);

      fireEvent.click(screen.getByRole("button", { name: "登録する" }));

      expect(await screen.findByText("テンプレート名を入力してください")).toBeInTheDocument();
      expect(mockSaveFrameTemplate).not.toHaveBeenCalled();
    });

    it("写真枠をすべて削除した場合はエラーを表示し、保存しないこと", async () => {
      render(<FrameTemplateEditor accountId="user1" />);

      for (let i = 0; i < 3; i++) {
        fireEvent.keyDown(screen.getByRole("button", { name: "写真枠1" }), { key: "Delete" });
      }
      fireEvent.change(screen.getByLabelText(/テンプレート名/), { target: { value: "メッセージだけ" } });
      fireEvent.click(screen.getByRole("button", { name: "登録する" }));

      expect(await screen.findByText("写真枠を1つ以上配置してください")).toBeInTheDocument();
      expect(mockSaveFrameTemplate).not.toHaveBeenCalled();
    });

    it("写真枠・メッセージ枠を追加すると、追加した枠を選択してその設定を表示すること", () => {
      render(<FrameTemplateEditor accountId="user1" />);

      fireEvent.click(screen.getByRole("button", { name: "＋写真枠" }));
      expect(screen.getByRole("heading", { name: "写真枠4" })).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "写真枠4" })).toHaveAttribute("aria-pressed", "true");

      fireEvent.click(screen.getByRole("button", { name: "＋メッセージ枠" }));
      expect(screen.getByRole("heading", { name: "メッセージ枠2" })).toBeInTheDocument();
      expect(screen.getByTestId("frame-element-count")).toHaveTextContent("写真枠 4/30・メッセージ枠 2/10");
    });

    it("登録すると額縁定義を送り、以降の保存が同じ額縁テンプレートに対して行われるよう編集用URLへ切り替えること", async () => {
      mockSaveFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを登録しました。", frameTemplateNo: 7 });

      render(<FrameTemplateEditor accountId="user1" />);

      fireEvent.change(screen.getByLabelText(/テンプレート名/), { target: { value: "  ワイド 3枚組 " } });
      fireEvent.click(screen.getByRole("button", { name: "登録する" }));

      expect(await screen.findByText("額縁テンプレートを登録しました。")).toBeInTheDocument();
      const [accountId, data, frameTemplateNo] = mockSaveFrameTemplate.mock.calls[0];
      expect(accountId).toBe("user1");
      expect(frameTemplateNo).toBeUndefined();
      expect(data.templateName).toBe("ワイド 3枚組");
      expect(savedDefinition().slots).toHaveLength(3);
      expect(savedDefinition().texts).toHaveLength(1);
      expect(mockReplace).toHaveBeenCalledWith("/user1/portfolio_setting/frame_templates/edit?frameTemplateNo=7");

      // 登録後の保存は更新になる
      fireEvent.click(screen.getByRole("button", { name: "閉じる" }));
      fireEvent.click(screen.getByRole("button", { name: "保存する" }));
      await waitFor(() => expect(mockSaveFrameTemplate).toHaveBeenCalledTimes(2));
      expect(mockSaveFrameTemplate.mock.calls[1][2]).toBe(7);
    });

    it("保存に失敗した場合は、エラーを表示すること", async () => {
      mockSaveFrameTemplate.mockRejectedValue(new Error("額縁の設定内容に誤りがあります。"));

      render(<FrameTemplateEditor accountId="user1" />);

      fireEvent.change(screen.getByLabelText(/テンプレート名/), { target: { value: "額縁" } });
      fireEvent.click(screen.getByRole("button", { name: "登録する" }));

      expect(await screen.findByText("額縁の設定内容に誤りがあります。")).toBeInTheDocument();
      expect(mockReplace).not.toHaveBeenCalled();
    });
  });

  describe("キャンバスの操作", () => {
    it("写真枠をドラッグすると、ポインタの移動量を台紙に対する%に換算して移動すること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      const box = await screen.findByRole("button", { name: "写真枠2" });
      fireEvent.pointerDown(box, { button: 0, pointerId: 1, clientX: 100, clientY: 100 });
      fireEvent.pointerMove(box, { pointerId: 1, clientX: 200, clientY: 170 });
      fireEvent.pointerUp(box, { pointerId: 1 });

      // 横 100px・縦 70px の移動は、台紙（1000×700px）の 10% ずつ
      expect(screen.getByRole("heading", { name: "写真枠2" })).toBeInTheDocument();
      expect(numberValue(/^左/)).toBe(65);
      expect(numberValue(/^上/)).toBe(20);
      expect(box).toHaveStyle({ left: "65%", top: "20%" });
    });

    it("右下のつまみをドラッグすると写真枠の大きさを、上のつまみをドラッグすると傾きを変えること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      const box = await screen.findByRole("button", { name: "写真枠2" });
      fireEvent.focus(box);
      const resize = within(box).getByTestId("frame-handle-resize");
      fireEvent.pointerDown(resize, { button: 0, pointerId: 2, clientX: 0, clientY: 0 });
      fireEvent.pointerMove(resize, { pointerId: 2, clientX: 50, clientY: -70 });
      fireEvent.pointerUp(resize, { pointerId: 2 });
      expect(numberValue(/^横幅/)).toBe(45);
      expect(numberValue(/^縦幅/)).toBe(50);

      // 回転の軸は枠の中心（500, 350）。中心の右へポインタを動かすと、上のつまみが右を向く 90° になる
      const rotate = within(box).getByTestId("frame-handle-rotate");
      fireEvent.pointerDown(rotate, { button: 0, pointerId: 3, clientX: 500, clientY: 0 });
      fireEvent.pointerMove(rotate, { pointerId: 3, clientX: 900, clientY: 350 });
      expect(numberValue(/^傾き/)).toBe(90);
      // 0° 付近は 0° に吸着する
      fireEvent.pointerMove(rotate, { pointerId: 3, clientX: 510, clientY: 0 });
      expect(numberValue(/^傾き/)).toBe(0);
      // Shift キーを押している間は 15° 刻みにする
      fireEvent.pointerMove(rotate, { pointerId: 3, clientX: 900, clientY: 100, shiftKey: true });
      expect(numberValue(/^傾き/) % 15).toBe(0);
      fireEvent.pointerUp(rotate, { pointerId: 3 });
    });

    it("選択中の枠は矢印キーで移動（Shift キーで大きく移動）でき、Delete キーで削除できること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      const box = await screen.findByRole("button", { name: "メッセージ枠1" });
      fireEvent.focus(box);
      fireEvent.keyDown(box, { key: "ArrowRight" });
      fireEvent.keyDown(box, { key: "ArrowUp", shiftKey: true });
      expect(numberValue(/^左/)).toBe(10.5);
      expect(numberValue(/^上/)).toBe(75);

      fireEvent.keyDown(box, { key: "Delete" });
      expect(screen.queryByRole("button", { name: "メッセージ枠1" })).not.toBeInTheDocument();
      expect(screen.getByRole("heading", { name: "額縁" })).toBeInTheDocument();
    });
  });

  describe("設定パネル", () => {
    it("額縁のサイズを選ぶと向きを保ったまま変更し、向きを切り替えると縦横を入れ替えること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      mockSaveFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを更新しました。", frameTemplateNo: 4 });
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      const sizeSelect = await screen.findByLabelText("サイズ");
      expect(sizeSelect).toHaveDisplayValue("四つ切（305×254mm）");
      fireEvent.change(sizeSelect, { target: { value: "6" } });
      expect(numberValue(/^横（mm）/)).toBe(420);
      expect(numberValue(/^縦（mm）/)).toBe(297);
      fireEvent.click(screen.getByLabelText("縦長"));
      expect(numberValue(/^横（mm）/)).toBe(297);
      expect(numberValue(/^縦（mm）/)).toBe(420);

      fireEvent.click(screen.getByRole("button", { name: "保存する" }));
      await waitFor(() => expect(mockSaveFrameTemplate).toHaveBeenCalled());
      expect(savedDefinition().size).toEqual({ width: 297, height: 420 });
      expect(mockSaveFrameTemplate.mock.calls[0][2]).toBe(4);
    });

    it("写真枠の順番を入れ替えると、写真を当てはめる順が入れ替わること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      mockSaveFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを更新しました。", frameTemplateNo: 4 });
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      fireEvent.focus(await screen.findByRole("button", { name: "写真枠2" }));
      fireEvent.click(screen.getByRole("button", { name: "順番を前へ" }));
      expect(screen.getByRole("heading", { name: "写真枠1" })).toBeInTheDocument();

      fireEvent.click(screen.getByRole("button", { name: "保存する" }));
      await waitFor(() => expect(mockSaveFrameTemplate).toHaveBeenCalled());
      expect(savedDefinition().slots.map((slot) => slot.x)).toEqual([55, 5]);
    });

    it("メッセージ枠のフォント・太字を変更でき、確認用の文面はキャンバスに表示するが保存しないこと", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      mockSaveFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを更新しました。", frameTemplateNo: 4 });
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      fireEvent.focus(await screen.findByRole("button", { name: "メッセージ枠1" }));
      fireEvent.change(screen.getByLabelText("フォント"), { target: { value: "script" } });
      fireEvent.click(screen.getByLabelText("太字"));
      fireEvent.change(screen.getByLabelText("確認用の文面"), { target: { value: "Spring has come" } });
      expect(screen.getByTestId("frame-text")).toHaveTextContent("Spring has come");

      fireEvent.click(screen.getByRole("button", { name: "保存する" }));
      await waitFor(() => expect(mockSaveFrameTemplate).toHaveBeenCalled());
      expect(savedDefinition().texts[0]).toMatchObject({ fontFamily: "script", bold: true });
      expect(JSON.stringify(mockSaveFrameTemplate.mock.calls[0][1])).not.toContain("Spring has come");
    });

    it("選択中の写真枠を複製すると、少しずらした写真枠を末尾に追加すること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      fireEvent.focus(await screen.findByRole("button", { name: "写真枠1" }));
      fireEvent.click(screen.getByRole("button", { name: "複製" }));

      expect(screen.getByRole("heading", { name: "写真枠3" })).toBeInTheDocument();
      expect(numberValue(/^左/)).toBe(8);
      expect(numberValue(/^上/)).toBe(13);
    });
  });

  describe("編集", () => {
    it("登録済みの額縁テンプレートを読み込んで表示すること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);

      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      expect(await screen.findByDisplayValue("横長 2枚組")).toBeInTheDocument();
      expect(mockGetFrameTemplate).toHaveBeenCalledWith("user1", 4);
      expect(screen.getByRole("heading", { name: "額縁テンプレート編集" })).toBeInTheDocument();
      expect(screen.getByTestId("frame-element-count")).toHaveTextContent("写真枠 2/30・メッセージ枠 1/10");
    });

    it("読み込みに失敗した場合は、エラーと一覧へ戻るリンクを表示すること", async () => {
      mockGetFrameTemplate.mockRejectedValue(new Error("額縁テンプレートが存在しません。"));

      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      expect(await screen.findByText("額縁テンプレートが存在しません。")).toBeInTheDocument();
      expect(screen.getByRole("link", { name: "額縁テンプレート一覧へ戻る" })).toHaveAttribute(
        "href",
        "/user1/portfolio_setting/frame_templates"
      );
    });

    it("削除を確認すると額縁テンプレートを削除し、一覧へ戻ること", async () => {
      mockGetFrameTemplate.mockResolvedValue(savedTemplate);
      mockDeleteFrameTemplate.mockResolvedValue({ message: "額縁テンプレートを削除しました。" });

      render(<FrameTemplateEditor accountId="user1" initialFrameTemplateNo={4} />);

      fireEvent.click(await screen.findByRole("button", { name: "削除" }));
      fireEvent.click(screen.getByRole("button", { name: "削除する" }));

      await waitFor(() => expect(mockPush).toHaveBeenCalledWith("/user1/portfolio_setting/frame_templates"));
      expect(mockDeleteFrameTemplate).toHaveBeenCalledWith("user1", 4);
    });
  });

  it("他人のアカウントの画面を開いた場合は、読み込まずに権限がない旨を表示すること", () => {
    render(<FrameTemplateEditor accountId="other" initialFrameTemplateNo={4} />);

    expect(screen.getByText("この操作を行う権限がありません")).toBeInTheDocument();
    expect(mockGetFrameTemplate).not.toHaveBeenCalled();
  });
});
