import { snapMove, snapResize } from "../guides";

/** 吸着する範囲（横・縦とも 1%） */
const THRESHOLD = { x: 1, y: 1 };

/** 他の枠（左 10%・上 20%・横幅 30%・縦幅 40%。右端 40%・下端 60%・中央 25%, 40%） */
const OTHER = { x: 10, y: 20, width: 30, height: 40 };

describe("frame/guides", () => {
  describe("snapMove", () => {
    it("枠の左端・中央・右端のうち、吸着先に最も近いものを揃えること", () => {
      // 左端 10.6% は他の枠の左端（10%）に吸着する
      const left = snapMove({ x: 10.6, y: 70, width: 20, height: 10 }, [OTHER], THRESHOLD);
      expect(left.x).toBeCloseTo(10);
      expect(left.guides.vertical).toEqual([10]);

      // 右端 99.5% は台紙の右端（100%）に吸着する
      const right = snapMove({ x: 79.5, y: 70, width: 20, height: 10 }, [OTHER], THRESHOLD);
      expect(right.x).toBeCloseTo(80);
      expect(right.guides.vertical).toEqual([100]);
    });

    it("枠の中央を台紙の中央に揃え、上端・中央・下端も同じように吸着させること", () => {
      const result = snapMove({ x: 40.5, y: 59.3, width: 20, height: 10 }, [OTHER], THRESHOLD);
      // 中央 50.5% → 50%、上端 59.3% → 他の枠の下端（60%）。揃えた結果、左端（40%）も他の枠の右端と揃う
      expect(result.x).toBeCloseTo(40);
      expect(result.y).toBeCloseTo(60);
      expect(result.guides).toEqual({ vertical: [40, 50], horizontal: [60] });
    });

    it("吸着する範囲内に吸着先がなければ動かさず、補助線も引かないこと", () => {
      const result = snapMove({ x: 63, y: 73, width: 10, height: 10 }, [OTHER], THRESHOLD);
      expect(result).toMatchObject({ x: 63, y: 73, guides: { vertical: [], horizontal: [] } });
    });

    it("吸着する範囲が 0 なら、ちょうど揃っている位置だけに補助線を引くこと", () => {
      const result = snapMove({ x: 10.4, y: 20, width: 5, height: 5 }, [OTHER], { x: 0, y: 0 });
      expect(result.x).toBe(10.4);
      expect(result.guides).toEqual({ vertical: [], horizontal: [20] });
    });
  });

  describe("snapResize", () => {
    it("右端・下端を吸着先に揃え、左上の位置は動かさないこと", () => {
      const result = snapResize({ x: 50, y: 30, width: 49.4, height: 29.5 }, [OTHER], THRESHOLD, true);
      expect(result).toMatchObject({ x: 50, y: 30 });
      expect(result.width).toBeCloseTo(50);
      expect(result.height).toBeCloseTo(30);
      expect(result.guides).toEqual({ vertical: [100], horizontal: [60] });
    });

    it("高さを変更しない場合（メッセージ枠）は、横幅だけを揃えること", () => {
      const result = snapResize({ x: 5, y: 30, width: 34.5, height: 29.5 }, [OTHER], THRESHOLD, false);
      expect(result.width).toBeCloseTo(35);
      expect(result.height).toBe(29.5);
      expect(result.guides).toEqual({ vertical: [40], horizontal: [] });
    });
  });
});
