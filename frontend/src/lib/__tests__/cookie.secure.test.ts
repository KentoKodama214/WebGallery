/**
 * @jest-environment jsdom
 * @jest-environment-options {"url": "https://example.com/"}
 */
import { setCookie, getCookie } from "../cookie";

describe("cookie（https 環境）", () => {
  it("setCookie は Secure 属性を付与し、値を往復できる", () => {
    const proto = Object.getPrototypeOf(document);
    let written = "";
    const spy = jest
      .spyOn(proto, "cookie", "set")
      .mockImplementation((value) => {
        written = String(value);
      });

    setCookie("foo", "bar", 100);

    spy.mockRestore();
    expect(written).toContain("; Secure");
    expect(written).toContain("SameSite=Lax");

    // 実際の document.cookie でも往復できる（https 環境なので Secure でも保存される）
    setCookie("j", "1", 100);
    expect(getCookie("j")).toBe("1");
  });
});
