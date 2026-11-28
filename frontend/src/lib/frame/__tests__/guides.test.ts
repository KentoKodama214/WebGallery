import { snapMove, snapResize } from "../guides";

/** 吸着する範囲（横・縦とも 1mm） */
const THRESHOLD = { x: 1, y: 1 };

/** 台紙の大きさ（100×100mm。中央 50mm） */
const MAT = { width: 100, height: 100 };

/** 他の枠（左 10mm・上 20mm・横幅 30mm・縦幅 40mm。右端 40mm・下端 60mm・中央 25mm, 40mm） */
const OTHER = { x: 10, y: 20, width: 30, height: 40 };

describe("frame/guides", () => {
  describe("snapMove", () => {
    it("枠の左端・中央・右端のうち、吸着先に最も近いものを揃えること", () => {
      // 左端 10.6mm は他の枠の左端（10mm）に吸着する
      const left = snapMove({ x: 10.6, y: 70, width: 20, height: 10 }, [OTHER], MAT, THRESHOLD);
      expect(left.x).toBeCloseTo(10);
      expect(left.guides.vertical).toEqual([10]);

      // 右端 99.5mm は台紙の右端（100mm）に吸着する
      const right = snapMove({ x: 79.5, y: 70, width: 20, height: 10 }, [OTHER], MAT, THRESHOLD);
      expect(right.x).toBeCloseTo(80);
      expect(right.guides.vertical).toEqual([100]);
    });

    it("枠の中央を台紙の中央に揃え、上端・中央・下端も同じように吸着させること", () => {
      const result = snapMove({ x: 40.5, y: 59.3, width: 20, height: 10 }, [OTHER], MAT, THRESHOLD);
      // 中央 50.5mm → 50mm、上端 59.3mm → 他の枠の下端（60mm）。揃えた結果、左端（40mm）も他の枠の右端と揃う
      expect(result.x).toBeCloseTo(40);
      expect(result.y).toBeCloseTo(60);
      expect(result.guides).toEqual({ vertical: [40, 50], horizontal: [60] });
    });

    it("吸着する範囲内に吸着先がなければ動かさず、補助線も引かないこと", () => {
      const result = snapMove({ x: 63, y: 73, width: 10, height: 10 }, [OTHER], MAT, THRESHOLD);
      expect(result).toMatchObject({ x: 63, y: 73, guides: { vertical: [], horizontal: [] } });
    });

    it("台紙の端・中央は、台紙の大きさから求めること", () => {
      // 300×200mm の台紙の中央は 150mm, 100mm、右端・下端は 300mm, 200mm
      const center = snapMove({ x: 120.6, y: 89.5, width: 60, height: 20 }, [], { width: 300, height: 200 }, THRESHOLD);
      expect(center).toMatchObject({ x: 120, y: 90, guides: { vertical: [150], horizontal: [100] } });

      const edge = snapMove({ x: 239.5, y: 179.4, width: 60, height: 20 }, [], { width: 300, height: 200 }, THRESHOLD);
      expect(edge).toMatchObject({ x: 240, guides: { vertical: [300], horizontal: [200] } });
      expect(edge.y).toBeCloseTo(180);
    });

    it("吸着する範囲が 0 なら、ちょうど揃っている位置だけに補助線を引くこと", () => {
      const result = snapMove({ x: 10.4, y: 20, width: 5, height: 5 }, [OTHER], MAT, { x: 0, y: 0 });
      expect(result.x).toBe(10.4);
      expect(result.guides).toEqual({ vertical: [], horizontal: [20] });
    });
  });

  describe("snapResize", () => {
    it("右端・下端を吸着先に揃え、左上の位置は動かさないこと", () => {
      const result = snapResize({ x: 50, y: 30, width: 49.4, height: 29.5 }, [OTHER], MAT, THRESHOLD, true);
      expect(result).toMatchObject({ x: 50, y: 30 });
      expect(result.width).toBeCloseTo(50);
      expect(result.height).toBeCloseTo(30);
      expect(result.guides).toEqual({ vertical: [100], horizontal: [60] });
    });

    it("高さを変更しない場合（メッセージ枠）は、横幅だけを揃えること", () => {
      const result = snapResize({ x: 5, y: 30, width: 34.5, height: 29.5 }, [OTHER], MAT, THRESHOLD, false);
      expect(result.width).toBeCloseTo(35);
      expect(result.height).toBe(29.5);
      expect(result.guides).toEqual({ vertical: [40], horizontal: [] });
    });
  });
});
