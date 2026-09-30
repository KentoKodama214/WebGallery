import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import AccountSettingPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const mockAccountSettingForm = jest.fn();
jest.mock("../AccountSettingForm", () => ({
  AccountSettingForm: (props: { accountId: string }) => {
    mockAccountSettingForm(props);
    return <div data-testid="account-setting-form" />;
  },
}));

function makeProps(accountId: string) {
  return { params: Promise.resolve({ accountId }) };
}

describe("AccountSettingPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("accountIdが正しい形式の場合はフォームを表示すること", async () => {
    const ui = await AccountSettingPage(makeProps("validaccount1"));
    render(ui);

    expect(screen.getByTestId("account-setting-form")).toBeInTheDocument();
    expect(mockAccountSettingForm).toHaveBeenCalledWith({
      accountId: "validaccount1",
    });
  });

  it.each([
    ["短すぎる", "short1"],
    ["記号を含む", "invalid-id!"],
  ])("accountIdが%s場合は404として扱う（notFoundを呼ぶ）", async (_label, accountId) => {
    // 存在しないリソースは 200 でメッセージを出すのではなく 404 として扱う
    // （`not-found.tsx` が描画され、noindex も付与される）
    await expect(AccountSettingPage(makeProps(accountId))).rejects.toThrow();
    expect(mockAccountSettingForm).not.toHaveBeenCalled();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await AccountSettingPage(makeProps("validaccount1"));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
