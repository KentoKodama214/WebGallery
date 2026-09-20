import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { Header } from "../Header";

jest.mock("next/font/local", () => () => ({ className: "" }));

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const mockUseAuth = jest.fn();
jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

describe("Header", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("未認証の場合", () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: null,
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
    });

    it("Photographers・Sign Inのリンクが表示されること", () => {
      render(<Header />);

      expect(screen.getByRole("link", { name: "Photographers" })).toHaveAttribute(
        "href",
        "/account_list"
      );
      expect(screen.getByRole("link", { name: "Sign In" })).toHaveAttribute(
        "href",
        "/login"
      );
    });

    it("My Gallery・Account Setting・Sign Outは表示されないこと", () => {
      render(<Header />);

      expect(screen.queryByText("My Gallery")).not.toBeInTheDocument();
      expect(screen.queryByText("Account Setting")).not.toBeInTheDocument();
      expect(screen.queryByTestId("logout-button")).not.toBeInTheDocument();
    });
  });

  describe("認証済みの場合", () => {
    const mockLogout = jest.fn();

    beforeEach(() => {
      mockLogout.mockResolvedValue(undefined);
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { accountId: "testuser1", accountNo: 1, role: "USER" },
        isLoading: false,
        login: jest.fn(),
        logout: mockLogout,
      });
    });

    it("My Gallery・Photographers・Account Setting・Inquiry・Sign Outが表示されること", () => {
      render(<Header />);

      expect(screen.getByRole("link", { name: "My Gallery" })).toHaveAttribute(
        "href",
        "/photo/testuser1/photo_list"
      );
      expect(screen.getByRole("link", { name: "Photographers" })).toHaveAttribute(
        "href",
        "/account_list"
      );
      expect(
        screen.getByRole("link", { name: "Account Setting" })
      ).toHaveAttribute("href", "/testuser1/account_setting");
      expect(screen.getByRole("link", { name: "Inquiry" })).toHaveAttribute(
        "href",
        "/inquiry/list"
      );
      expect(screen.getByTestId("logout-button")).toBeInTheDocument();
    });

    it("Sign Outボタンをクリックするとログアウトして/loginへ遷移すること", async () => {
      render(<Header />);

      fireEvent.click(screen.getByTestId("logout-button"));

      await waitFor(() => {
        expect(mockLogout).toHaveBeenCalled();
      });
      await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith("/login");
      });
    });
  });

  describe("ハンバーガーメニューの開閉", () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: null,
        isLoading: false,
        login: jest.fn(),
        logout: jest.fn(),
      });
    });

    it("初期状態ではメニューが閉じていること", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      expect(button).toHaveAttribute("aria-expanded", "false");
    });

    it("クリックでメニューが開閉すること", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      fireEvent.click(button);
      expect(button).toHaveAttribute("aria-expanded", "true");

      fireEvent.click(button);
      expect(button).toHaveAttribute("aria-expanded", "false");
    });

    it("Enterキーでもメニューが開くこと", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      fireEvent.keyDown(button, { key: "Enter" });
      expect(button).toHaveAttribute("aria-expanded", "true");
    });

    it("Spaceキーでもメニューが開くこと", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      fireEvent.keyDown(button, { key: " " });
      expect(button).toHaveAttribute("aria-expanded", "true");
    });

    it("メニュー内のリンクをクリックすると閉じること", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      fireEvent.click(button);
      expect(button).toHaveAttribute("aria-expanded", "true");

      fireEvent.click(screen.getByRole("link", { name: "Photographers" }));

      expect(button).toHaveAttribute("aria-expanded", "false");
    });

    it("Escapeキーでメニューが閉じ、トグルボタンへフォーカスが戻ること", () => {
      render(<Header />);

      const button = screen.getByTestId("hamburger-button");
      fireEvent.click(button);
      expect(button).toHaveAttribute("aria-expanded", "true");

      fireEvent.keyDown(document, { key: "Escape" });

      expect(button).toHaveAttribute("aria-expanded", "false");
      expect(button).toHaveFocus();
    });

    it("メニュー展開中はTabキーでフォーカスがメニュー内を循環すること", () => {
      render(<Header />);

      fireEvent.click(screen.getByTestId("hamburger-button"));

      const links = screen.getAllByRole("link");
      const first = links[0];
      const last = links[links.length - 1];

      expect(first).toHaveFocus();

      // 最後の要素で Tab → 先頭へ循環
      last.focus();
      fireEvent.keyDown(document, { key: "Tab" });
      expect(first).toHaveFocus();

      // 先頭の要素で Shift+Tab → 最後尾へ循環
      fireEvent.keyDown(document, { key: "Tab", shiftKey: true });
      expect(last).toHaveFocus();
    });
  });
});
