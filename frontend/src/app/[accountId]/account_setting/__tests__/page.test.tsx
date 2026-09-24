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
  ])("accountIdが%s場合は「ページが見つかりません」を表示すること", async (_label, accountId) => {
    const ui = await AccountSettingPage(makeProps(accountId));
    render(ui);

    expect(screen.getByText("ページが見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("account-setting-form")).not.toBeInTheDocument();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await AccountSettingPage(makeProps("validaccount1"));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
