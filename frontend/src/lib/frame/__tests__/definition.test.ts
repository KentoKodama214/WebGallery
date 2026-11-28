import {
  FRAME_SIZE_PRESETS,
  PHOTO_SIZE_PRESETS,
  FRAME_SLOT_MAX_SIZE,
  FRAME_TEXT_MAX_SIZE,
  clamp,
  createDefaultDefinition,
  createSlot,
  createText,
  findFrameSizePresetIndex,
  findSizePresetIndex,
  fitToMat,
  getBorderWidthMax,
  getMatSize,
  parseFrameDefinition,
  round,
  toFontFamilyCss,
  toFrameDefinitionJson,
  toPresetSize,
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

  describe("findSizePresetIndex / toPresetSize（写真枠のサイズ）", () => {
    const photoLabelOf = (width: number, height: number) =>
      PHOTO_SIZE_PRESETS[findSizePresetIndex(PHOTO_SIZE_PRESETS, { width, height })]?.label;

    it("写真のプリントサイズは縦横を入れ替えたサイズも同じ選択肢に当てはめ、A判は向きごとに当てはめること", () => {
      expect(photoLabelOf(127, 89)).toBe("L判（127×89mm）");
      expect(photoLabelOf(127, 178)).toBe("2L判（178×127mm）");
      expect(photoLabelOf(210, 297)).toBe("A4 縦（210×297mm）");
      expect(photoLabelOf(297, 210)).toBe("A4 横（297×210mm）");
      expect(photoLabelOf(148, 210)).toBe("A5 縦（148×210mm）");
      expect(findSizePresetIndex(PHOTO_SIZE_PRESETS, { width: 120, height: 150 })).toBe(-1);
    });

    it("写真用紙の判は指定した向きに合わせ、A判は選択肢の向きのまま返すこと", () => {
      const twoL = PHOTO_SIZE_PRESETS.find((preset) => preset.label.startsWith("2L判"))!;
      const a4Portrait = PHOTO_SIZE_PRESETS.find((preset) => preset.label.startsWith("A4 縦"))!;
      expect(toPresetSize(twoL, true)).toEqual({ width: 127, height: 178 });
      expect(toPresetSize(twoL, false)).toEqual({ width: 178, height: 127 });
      expect(toPresetSize(a4Portrait, false)).toEqual({ width: 210, height: 297 });
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

    it("追加する写真枠・メッセージ枠は、追加するたびに位置をずらし、台紙の範囲内に置くこと", () => {
      const mat = { width: 348, height: 236 };
      expect(createSlot(1, mat).x).not.toBe(createSlot(0, mat).x);
      const slot = createSlot(4, mat);
      expect(slot.x + slot.width).toBeLessThanOrEqual(mat.width);
      expect(slot.y + slot.height).toBeLessThanOrEqual(mat.height);
      expect(createText(10, mat).y).toBeLessThanOrEqual(mat.height);
    });
  });

  describe("getMatSize / getBorderWidthMax / fitToMat", () => {
    it("台紙の大きさは、額縁のサイズから外枠の太さの分を両側から除いた大きさであること", () => {
      expect(getMatSize({ width: 366, height: 254 }, 9)).toEqual({ width: 348, height: 236 });
    });

    it("外枠の太さの上限は、台紙の縦横が 10mm 残る太さとし、200mm を超えないこと", () => {
      // 短辺 254mm から 10mm を残すため (254-10)/2=122mm
      expect(getBorderWidthMax({ width: 366, height: 254 })).toBe(122);
      expect(getBorderWidthMax({ width: 51, height: 3000 })).toBe(20.5);
      expect(getBorderWidthMax({ width: 3000, height: 3000 })).toBe(200);
    });

    it("額縁を小さくしたり外枠を太くしたりして台紙からはみ出した枠を、台紙の内側に収めること", () => {
      const definition = createDefaultDefinition();
      const fitted = fitToMat({
        ...definition,
        size: { width: 200, height: 100 },
        border: { ...definition.border, width: 60 },
      });

      // 外枠は (100-10)/2=45mm まで。台紙は 110×10mm になる
      expect(fitted.border.width).toBe(45);
      for (const slot of fitted.slots) {
        expect(slot.x).toBeLessThanOrEqual(110);
        expect(slot.y).toBeLessThanOrEqual(10);
        expect(slot.width).toBeLessThanOrEqual(110);
        expect(slot.height).toBeLessThanOrEqual(10);
      }
      expect(fitted.slots[0]).toMatchObject({ x: 21, y: 10, width: 94, height: 10, rotation: -2 });
      expect(fitted.texts[0]).toMatchObject({ x: 34.8, y: 10, width: 110 });
    });

    it("台紙に収まっている枠は変えないこと", () => {
      const definition = createDefaultDefinition();
      expect(fitToMat(definition)).toEqual(definition);
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
        slots: [{ x: -5, y: 300, width: 0, rotation: 999 }, "オブジェクトでない写真枠"],
        texts: [{ fontFamily: "Comic Sans", align: "justify", fontSize: 300, color: "#fff" }],
      });

      expect(parsed.size).toEqual({ width: 50, height: 254 });
      expect(parsed.background.color).toBe("#ffffff");
      expect(parsed.border).toEqual({ color: "#000000", width: 0 });
      // 位置・大きさは台紙（外枠なしのため 50×254mm）の範囲内に収め、欠けた大きさは台紙の縦横にする
      expect(parsed.slots).toEqual([
        { x: 0, y: 254, width: 1, height: 254, rotation: 180, borderColor: "#ffffff", borderWidth: 0, shadow: false },
      ]);
      expect(parsed.texts[0]).toMatchObject({ fontFamily: "gothic", align: "left", fontSize: 200, color: "#333333", bold: false, italic: false });
    });

    it("外枠は台紙の縦横が 10mm 残る太さまでとし、写真枠・メッセージ枠は外枠の内側の台紙の範囲内に収めること", () => {
      const parsed = parseFrameDefinition({
        size: { width: 300, height: 100 },
        border: { width: 80 },
        slots: [{ x: 500, y: 500, width: 500, height: 500 }],
        texts: [{ x: 500, y: 500, width: 500, fontFamily: "gothic", fontSize: 5 }],
      });

      // 外枠は (100-10)/2=45mm まで。台紙は 210×10mm になる
      expect(parsed.border.width).toBe(45);
      expect(parsed.slots[0]).toMatchObject({ x: 210, y: 10, width: 210, height: 10 });
      expect(parsed.texts[0]).toMatchObject({ x: 210, y: 10, width: 210 });
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
    it("数値の桁数を揃え（額縁のサイズは 1mm、それ以外は 0.1 単位）、位置・大きさを台紙の範囲内に収めること", () => {
      const definition = createDefaultDefinition();
      definition.size = { width: 366.4, height: 254.6 };
      definition.border = { ...definition.border, width: 9.04 };
      definition.slots = [{ ...definition.slots[0], x: 12.345, y: -3, width: 400, rotation: 3.14159, borderWidth: 2.96 }];
      definition.texts = [{ ...definition.texts[0], fontSize: 8.75 }];

      const json = toFrameDefinitionJson(definition) as {
        size: { width: number; height: number };
        border: { width: number };
        slots: { x: number; y: number; width: number; rotation: number; borderWidth: number }[];
        texts: { fontSize: number }[];
      };

      expect(json.size).toEqual({ width: 366, height: 255 });
      expect(json.border.width).toBe(9);
      // 台紙は 366-9×2=348mm × 255-9×2=237mm
      expect(json.slots[0]).toMatchObject({ x: 12.3, y: 0, width: 348, rotation: 3.1, borderWidth: 3 });
      expect(json.texts[0].fontSize).toBe(8.8);
    });

    it("丸めた位置・大きさが台紙の縦横を超えないこと", () => {
      const definition = createDefaultDefinition();
      // 台紙は 366-9.3×2=347.4mm（浮動小数点では 347.4 ちょうどにならない）
      definition.border = { ...definition.border, width: 9.3 };
      definition.slots = [{ ...definition.slots[0], x: 347.4, width: 347.4 }];

      const json = toFrameDefinitionJson(definition) as { slots: { x: number; width: number }[] };

      expect(json.slots[0].x).toBeLessThanOrEqual(366 - 9.3 * 2 + 1e-6);
      expect(json.slots[0].width).toBe(json.slots[0].x);
      expect(json.slots[0].x).toBeCloseTo(347.4);
    });
  });
});
