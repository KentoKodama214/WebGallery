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

  it("accountIdが不正な形式の場合は「写真が見つかりません」を表示すること", async () => {
    const ui = await PhotoDetailPage(makeProps("a", { photoNo: "5" }));
    render(ui);

    expect(screen.getByText("写真が見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("photo-detail")).not.toBeInTheDocument();
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
  ])("photoNoが%s場合は「写真が見つかりません」を表示すること", async (_label, value) => {
    const ui = await PhotoDetailPage(makeProps("validaccount1", { photoNo: value }));
    render(ui);

    expect(screen.getByText("写真が見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("photo-detail")).not.toBeInTheDocument();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await PhotoDetailPage(makeProps("validaccount1", { photoNo: "5" }));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
