import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AuthGuard } from "../AuthGuard";

const mockReplace = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mockReplace }),
}));

const mockUseAuth = jest.fn();
jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

describe("AuthGuard", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    window.history.replaceState({}, "", "/e2etestaccount/account_setting");
  });

  it("認証確認中は children をマウントせずフォールバックを表示する", () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: true });

    render(
      <AuthGuard>
        <div>protected</div>
      </AuthGuard>
    );

    expect(screen.queryByText("protected")).not.toBeInTheDocument();
    expect(screen.getByRole("status")).toBeInTheDocument();
    expect(mockReplace).not.toHaveBeenCalled();
  });

  it("未ログイン確定なら children を出さず /login へ誘導し、リンクも表示する", async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: false });

    render(
      <AuthGuard>
        <div>protected</div>
      </AuthGuard>
    );

    expect(screen.queryByText("protected")).not.toBeInTheDocument();
    expect(screen.getByText("ログインが必要です。")).toBeInTheDocument();
    const link = screen.getByRole("link", { name: "ログインページへ移動" });
    expect(link).toHaveAttribute(
      "href",
      "/login?redirect=%2Fe2etestaccount%2Faccount_setting"
    );
    await waitFor(() => {
      expect(mockReplace).toHaveBeenCalledWith(
        "/login?redirect=%2Fe2etestaccount%2Faccount_setting"
      );
    });
  });

  it("認証済みなら children をレンダリングする", () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });

    render(
      <AuthGuard>
        <div>protected</div>
      </AuthGuard>
    );

    expect(screen.getByText("protected")).toBeInTheDocument();
    expect(mockReplace).not.toHaveBeenCalled();
  });
});
