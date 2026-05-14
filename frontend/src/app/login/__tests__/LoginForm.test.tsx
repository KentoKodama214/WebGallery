import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { LoginForm } from "../LoginForm";

// モック
const mockLogin = jest.fn();
const mockPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    login: mockLogin,
    user: null,
    isAuthenticated: false,
    isLoading: false,
    logout: jest.fn(),
  }),
}));

describe("LoginForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("ログインフォームが正しくレンダリングされること", () => {
    render(<LoginForm />);

    expect(screen.getByPlaceholderText("User ID")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log in" })).toBeInTheDocument();
    expect(screen.getByText("Create an account")).toBeInTheDocument();
  });

  it("ログイン成功時にリダイレクトされること", async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith("testuser1", "password1");
      expect(mockPush).toHaveBeenCalledWith("/photo/testuser1/photo_list");
    });
  });

  it("ログイン失敗時にエラーメッセージが表示されること", async () => {
    mockLogin.mockRejectedValueOnce(
      new Error("アカウントIDまたはパスワードが間違っています。")
    );
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "wrongpass");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウントIDまたはパスワードが間違っています。")
      ).toBeInTheDocument();
    });
  });

  it("送信中はボタンが無効化されること", async () => {
    mockLogin.mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 1000))
    );
    const user = userEvent.setup();

    render(<LoginForm />);

    await user.type(screen.getByPlaceholderText("User ID"), "testuser1");
    await user.type(screen.getByPlaceholderText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(screen.getByRole("button")).toBeDisabled();
  });
});
