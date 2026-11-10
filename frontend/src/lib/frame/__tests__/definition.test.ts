import {
  FRAME_SIZE_PRESETS,
  FRAME_SLOT_MAX_SIZE,
  FRAME_TEXT_MAX_SIZE,
  clamp,
  createDefaultDefinition,
  createSlot,
  createText,
  findFrameSizePresetIndex,
  parseFrameDefinition,
  round,
  toFontFamilyCss,
  toFrameDefinitionJson,
} from "../definition";

describe("frame/definition", () => {
  describe("clamp / round", () => {
    it("範囲外の値は範囲内に収め、数値でない・有限でない値は既定値にすること", () => {
      expect(clamp(150, 0, 100, 5)).toBe(100);
      expect(clamp(-1, 0, 100, 5)).toBe(0);
      expect(clamp(50, 0, 100, 5)).toBe(50);
      expect(clamp("50", 0, 100, 5)).toBe(5);
      expect(clamp(Number.NaN, 0, 100, 5)).toBe(5);
      expect(clamp(Number.POSITIVE_INFINITY, 0, 100, 5)).toBe(5);
    });

    it("指定した桁数に丸めること", () => {
      expect(round(1.26)).toBe(1.3);
      expect(round(1.234, 2)).toBe(1.23);
      expect(round(1.5, 0)).toBe(2);
    });
  });

  describe("findFrameSizePresetIndex", () => {
    const labelOf = (width: number, height: number) =>
      FRAME_SIZE_PRESETS[findFrameSizePresetIndex({ width, height })]?.label;

    it("A判は横向き・縦向きを別の選択肢として当てはめること", () => {
      expect(labelOf(297, 210)).toBe("A4 横（297×210mm）");
      expect(labelOf(210, 297)).toBe("A4 縦（210×297mm）");
      expect(labelOf(420, 297)).toBe("A3 横（420×297mm）");
      expect(labelOf(297, 420)).toBe("A3 縦（297×420mm）");
      expect(labelOf(594, 420)).toBe("A2 横（594×420mm）");
      expect(labelOf(420, 594)).toBe("A2 縦（420×594mm）");
    });

    it("写真用紙の判は縦横を入れ替えたサイズも同じ選択肢に当てはめ、どれにも当てはまらなければ -1 を返すこと", () => {
      expect(labelOf(305, 254)).toBe("四つ切（305×254mm）");
      expect(labelOf(254, 305)).toBe("四つ切（305×254mm）");
      expect(findFrameSizePresetIndex({ width: 500, height: 400 })).toBe(-1);
    });
  });

  describe("toFontFamilyCss", () => {
    it("フォントの選択肢に対応するフォントスタックを返し、未知の値はゴシックにすること", () => {
      expect(toFontFamilyCss("mincho")).toContain("Mincho");
      expect(toFontFamilyCss("mincho")).toMatch(/serif$/);
      expect(toFontFamilyCss("unknown")).toBe(toFontFamilyCss("gothic"));
    });
  });

  describe("createDefaultDefinition / createSlot / createText", () => {
    it("新しい額縁テンプレートは写真枠3つ・メッセージ枠1つを持ち、保存できる範囲の値であること", () => {
      const definition = createDefaultDefinition();

      expect(definition.slots).toHaveLength(3);
      expect(definition.texts).toHaveLength(1);
      // 保存用のJSONに変換しても値が変わらない（範囲内の値である）こと
      expect(parseFrameDefinition(toFrameDefinitionJson(definition))).toEqual(definition);
    });

    it("追加する写真枠・メッセージ枠は、追加するたびに位置をずらし、額縁の範囲内に置くこと", () => {
      expect(createSlot(1).x).not.toBe(createSlot(0).x);
      expect(createText(10).y).toBeLessThanOrEqual(100);
    });
  });

  describe("parseFrameDefinition", () => {
    it("保存済みの額縁定義をそのまま変換すること", () => {
      const json = {
        size: { width: 254, height: 305 },
        background: { color: "#f5f0e6" },
        border: { color: "#3b2a1a", width: 4 },
        slots: [
          { x: 5, y: 10, width: 40, height: 60, rotation: -3, borderColor: "#ffffff", borderWidth: 1, shadow: true },
        ],
        texts: [
          { x: 10, y: 80, width: 80, rotation: 2, fontFamily: "script", fontSize: 4, color: "#000000", align: "right", bold: true, italic: true },
        ],
      };

      expect(parseFrameDefinition(json)).toEqual(json);
    });

    it("欠けた項目は既定値で補い、範囲外の値は範囲内に収め、不正な色・選択肢は既定値にすること", () => {
      const parsed = parseFrameDefinition({
        size: { width: 10 },
        background: { color: "red" },
        slots: [{ x: -5, y: 120, width: 0, rotation: 999 }, "オブジェクトでない写真枠"],
        texts: [{ fontFamily: "Comic Sans", align: "justify", fontSize: 100, color: "#fff" }],
      });

      expect(parsed.size).toEqual({ width: 50, height: 254 });
      expect(parsed.background.color).toBe("#ffffff");
      expect(parsed.border).toEqual({ color: "#000000", width: 0 });
      expect(parsed.slots).toEqual([
        { x: 0, y: 100, width: 1, height: 40, rotation: 180, borderColor: "#ffffff", borderWidth: 0, shadow: false },
      ]);
      expect(parsed.texts[0]).toMatchObject({ fontFamily: "gothic", align: "left", fontSize: 30, color: "#333333", bold: false, italic: false });
    });

    it("写真枠・メッセージ枠は上限の数までとし、オブジェクトでない値は空の額縁定義として扱うこと", () => {
      const slot = { x: 0, y: 0, width: 10, height: 10 };
      const text = { x: 0, y: 0, width: 10, fontFamily: "gothic", fontSize: 3 };
      const parsed = parseFrameDefinition({
        size: { width: 300, height: 300 },
        slots: Array.from({ length: FRAME_SLOT_MAX_SIZE + 5 }, () => slot),
        texts: Array.from({ length: FRAME_TEXT_MAX_SIZE + 5 }, () => text),
      });

      expect(parsed.slots).toHaveLength(FRAME_SLOT_MAX_SIZE);
      expect(parsed.texts).toHaveLength(FRAME_TEXT_MAX_SIZE);
      expect(parseFrameDefinition(null).slots).toEqual([]);
      expect(parseFrameDefinition([]).texts).toEqual([]);
    });
  });

  describe("toFrameDefinitionJson", () => {
    it("数値の桁数を揃え、位置・大きさを範囲内に収めること", () => {
      const definition = createDefaultDefinition();
      definition.size = { width: 366.4, height: 254.6 };
      definition.slots = [{ ...definition.slots[0], x: 12.345, y: -3, width: 120, rotation: 3.14159 }];

      const json = toFrameDefinitionJson(definition) as {
        size: { width: number; height: number };
        slots: { x: number; y: number; width: number; rotation: number }[];
      };

      expect(json.size).toEqual({ width: 366, height: 255 });
      expect(json.slots[0]).toMatchObject({ x: 12.3, y: 0, width: 100, rotation: 3.1 });
    });
  });
});
