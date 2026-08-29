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

  it("setCookie / getCookie で値を往復でき、URLエンコードされる", () => {
    setCookie("j", JSON.stringify({ a: 1, b: "x;y" }), 100);
    expect(getCookie("j")).toBe('{"a":1,"b":"x;y"}');
  });

  it("存在しないcookieはnull", () => {
    expect(getCookie("missing")).toBeNull();
  });

  it("deleteCookieで取得できなくなる", () => {
    setCookie("temp", "1", 100);
    expect(getCookie("temp")).toBe("1");
    deleteCookie("temp");
    expect(getCookie("temp")).toBeNull();
  });
});
