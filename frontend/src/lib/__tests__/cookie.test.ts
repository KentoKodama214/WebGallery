import { setCookie, getCookie, deleteCookie } from "../cookie";

describe("cookie", () => {
  beforeEach(() => {
    for (const c of document.cookie.split("; ")) {
      const name = c.split("=")[0];
      if (name) document.cookie = `${name}=; path=/; max-age=0`;
    }
  });

  it("setCookie は SameSite=Lax / path=/ / max-age を付与する", () => {
    const writes: string[] = [];
    const proto = Object.getPrototypeOf(document);
    const spy = jest
      .spyOn(proto, "cookie", "set")
      .mockImplementation((value) => {
        writes.push(String(value));
      });

    setCookie("foo", "bar", 100);

    spy.mockRestore();
    expect(writes[0]).toContain("SameSite=Lax");
    expect(writes[0]).toContain("path=/");
    expect(writes[0]).toContain("max-age=100");
  });

  it("http（既定の jsdom 環境）では Secure 属性を付けない", () => {
    // https 環境で Secure が付くことは cookie.secure.test.ts で検証する
    const proto = Object.getPrototypeOf(document);
    let written = "";
    const spy = jest
      .spyOn(proto, "cookie", "set")
      .mockImplementation((value) => {
        written = String(value);
      });

    setCookie("foo", "bar", 100);

    spy.mockRestore();
    expect(written).not.toContain("Secure");
  });

  it("setCookie / getCookie で値を往復でき、URLエンコードされる", () => {
    setCookie("j", JSON.stringify({ a: 1, b: "x;y" }), 100);
    expect(getCookie("j")).toBe('{"a":1,"b":"x;y"}');
  });

  it("存在しないcookieはnull", () => {
    expect(getCookie("missing")).toBeNull();
  });

  it("不正なCookie名（`;`・空白・`=` を含む）は例外を投げ、書き込まない", () => {
    const proto = Object.getPrototypeOf(document);
    const spy = jest.spyOn(proto, "cookie", "set").mockImplementation(() => {});

    expect(() => setCookie("evil; Path=/", "x", 100)).toThrow("不正なCookie名");
    expect(() => setCookie("a b", "x", 100)).toThrow();
    expect(() => setCookie("k=v", "x", 100)).toThrow();
    expect(spy).not.toHaveBeenCalled();

    spy.mockRestore();
  });

  it("英数字・`_`・`-` のみの名前は許可される", () => {
    setCookie("photoListFilter_aaaa1111", "1", 100);
    expect(getCookie("photoListFilter_aaaa1111")).toBe("1");
  });

  it("deleteCookieで取得できなくなる", () => {
    setCookie("temp", "1", 100);
    expect(getCookie("temp")).toBe("1");
    deleteCookie("temp");
    expect(getCookie("temp")).toBeNull();
  });
});
