import {
  ACCOUNT_ID_PATTERN,
  PASSKEY_NAME_MAX_LENGTH,
  clearError,
  isPastDate,
  isValidAccountId,
} from "../validation";

describe("validation", () => {
  describe("ACCOUNT_ID_PATTERN", () => {
    it("半角英数字8〜20文字を許可する", () => {
      expect(ACCOUNT_ID_PATTERN.test("abcd1234")).toBe(true);
      expect(ACCOUNT_ID_PATTERN.test("a".repeat(20))).toBe(true);
    });

    it("7文字以下・21文字以上・記号・全角は拒否する", () => {
      expect(ACCOUNT_ID_PATTERN.test("abc123")).toBe(false);
      expect(ACCOUNT_ID_PATTERN.test("a".repeat(21))).toBe(false);
      expect(ACCOUNT_ID_PATTERN.test("abcd-123")).toBe(false);
      expect(ACCOUNT_ID_PATTERN.test("ａｂｃｄ１２３４")).toBe(false);
    });
  });

  describe("isValidAccountId", () => {
    it("アカウントID形式の文字列のみ true", () => {
      expect(isValidAccountId("aaaa1111")).toBe(true);
      expect(isValidAccountId("abc123")).toBe(false);
      expect(isValidAccountId("aaaa1111; Path=/")).toBe(false);
      expect(isValidAccountId("aaaa 1111")).toBe(false);
      expect(isValidAccountId(null)).toBe(false);
      expect(isValidAccountId(undefined)).toBe(false);
    });
  });

  describe("PASSKEY_NAME_MAX_LENGTH", () => {
    it("バックエンドのパスキー名の上限（50文字）と一致する", () => {
      expect(PASSKEY_NAME_MAX_LENGTH).toBe(50);
    });
  });

  describe("clearError", () => {
    it("指定キーを取り除いた新しいオブジェクトを返す", () => {
      const errors = { a: "x", b: "y" };
      const next = clearError(errors, "a");
      expect(next).toEqual({ b: "y" });
      expect(next).not.toBe(errors);
    });

    it("対象キーが無い場合は同一参照を返す", () => {
      const errors = { a: "x" };
      expect(clearError(errors, "b")).toBe(errors);
    });
  });

  describe("isPastDate", () => {
    it("過去日はtrue、当日・未来日はfalse", () => {
      const today = new Date();
      const yyyy = today.getFullYear();
      const mm = String(today.getMonth() + 1).padStart(2, "0");
      const dd = String(today.getDate()).padStart(2, "0");
      expect(isPastDate(`${yyyy}-${mm}-${dd}`)).toBe(false);
      expect(isPastDate("2000-01-01")).toBe(true);
      expect(isPastDate("2999-01-01")).toBe(false);
    });

    it("存在しない日付・不正な形式・空文字はfalse", () => {
      expect(isPastDate("2026-02-29")).toBe(false); // 非閏年
      expect(isPastDate("2026-13-01")).toBe(false);
      expect(isPastDate("2026-01-32")).toBe(false);
      expect(isPastDate("2026/01/01")).toBe(false);
      expect(isPastDate("")).toBe(false);
    });
  });
});
