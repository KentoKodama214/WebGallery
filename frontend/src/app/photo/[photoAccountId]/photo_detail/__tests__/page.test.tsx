import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PhotoDetailPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));

const mockPhotoDetail = jest.fn();
jest.mock("../PhotoDetail", () => ({
  PhotoDetail: (props: { photoAccountId: string; photoNo: number }) => {
    mockPhotoDetail(props);
    return <div data-testid="photo-detail" />;
  },
}));

/** Next.js の searchParams/params は Promise で渡される */
function makeProps(
  photoAccountId: string,
  searchParams: Record<string, string | string[] | undefined>
) {
  return {
    params: Promise.resolve({ photoAccountId }),
    searchParams: Promise.resolve(searchParams),
  };
}

describe("PhotoDetailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("accountId・photoNo が正しい場合はPhotoDetailを表示すること", async () => {
    const ui = await PhotoDetailPage(makeProps("validaccount1", { photoNo: "5" }));
    render(ui);

    expect(screen.getByTestId("photo-detail")).toBeInTheDocument();
    expect(mockPhotoDetail).toHaveBeenCalledWith({
      photoAccountId: "validaccount1",
      photoNo: 5,
    });
  });

  it("accountIdが不正な形式の場合は404として扱う（notFoundを呼ぶ）", async () => {
    // 存在しないリソースは 200 でメッセージを出すのではなく 404 として扱う
    // （`not-found.tsx` が描画され、noindex も付与される）
    await expect(PhotoDetailPage(makeProps("a", { photoNo: "5" }))).rejects.toThrow();
    expect(mockPhotoDetail).not.toHaveBeenCalled();
  });

  it.each([
    ["未指定", undefined],
    ["数値でない", "abc"],
    ["ゼロ", "0"],
    ["負数", "-1"],
    ["小数", "1.5"],
    ["指数表記", "1e3"],
    ["16進表記", "0x10"],
    ["前後空白あり", " 5 "],
  ])("photoNoが%s場合は404として扱う（notFoundを呼ぶ）", async (_label, value) => {
    await expect(
      PhotoDetailPage(makeProps("validaccount1", { photoNo: value }))
    ).rejects.toThrow();
    expect(mockPhotoDetail).not.toHaveBeenCalled();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await PhotoDetailPage(makeProps("validaccount1", { photoNo: "5" }));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
