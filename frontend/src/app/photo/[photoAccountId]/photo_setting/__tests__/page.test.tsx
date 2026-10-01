import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PhotoSettingPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const mockPhotoSettingForm = jest.fn();
jest.mock("../PhotoSettingForm", () => ({
  PhotoSettingForm: (props: {
    photoAccountId: string;
    accountNo?: number;
    photoNo?: number;
  }) => {
    mockPhotoSettingForm(props);
    return <div data-testid="photo-setting-form" />;
  },
}));

function makeProps(
  photoAccountId: string,
  searchParams: Record<string, string | string[] | undefined>
) {
  return {
    params: Promise.resolve({ photoAccountId }),
    searchParams: Promise.resolve(searchParams),
  };
}

describe("PhotoSettingPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("新規登録（accountNo・photoNoともに未指定）の場合はフォームを表示すること", async () => {
    const ui = await PhotoSettingPage(makeProps("validaccount1", {}));
    render(ui);

    expect(screen.getByTestId("photo-setting-form")).toBeInTheDocument();
    expect(mockPhotoSettingForm).toHaveBeenCalledWith({
      photoAccountId: "validaccount1",
      accountNo: undefined,
      photoNo: undefined,
    });
  });

  it("編集（accountNo・photoNoともに指定）の場合はフォームを表示すること", async () => {
    const ui = await PhotoSettingPage(
      makeProps("validaccount1", { accountNo: "1", photoNo: "5" })
    );
    render(ui);

    expect(screen.getByTestId("photo-setting-form")).toBeInTheDocument();
    expect(mockPhotoSettingForm).toHaveBeenCalledWith({
      photoAccountId: "validaccount1",
      accountNo: 1,
      photoNo: 5,
    });
  });

  it.each([
    ["accountNoのみ指定された", "validaccount1", { accountNo: "1" }],
    ["photoNoのみ指定された", "validaccount1", { photoNo: "5" }],
    ["accountIdが不正な形式の", "a", {}],
  ])(
    "%s場合は404として扱う（notFoundを呼ぶ）",
    async (_label, photoAccountId, searchParams) => {
      // 不正なURLは 200 でメッセージを出すのではなく 404 として扱う
      // （`not-found.tsx` が描画され、noindex も付与される）
      await expect(
        PhotoSettingPage(makeProps(photoAccountId, searchParams))
      ).rejects.toThrow();
      expect(mockPhotoSettingForm).not.toHaveBeenCalled();
    }
  );

  it("Header・Footerを表示すること", async () => {
    const ui = await PhotoSettingPage(makeProps("validaccount1", {}));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
