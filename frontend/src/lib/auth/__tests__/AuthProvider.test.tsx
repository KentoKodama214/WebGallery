import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import { AuthProvider, useAuth } from "../AuthProvider";

// APIクライアントのモック
jest.mock("@/lib/api/client", () => ({
  login: jest.fn(),
  logout: jest.fn(),
  refresh: jest.fn(),
  getAccessToken: jest.fn(),
  setAccessToken: jest.fn(),
}));

import * as apiClient from "@/lib/api/client";

const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

// テスト用JWTトークンを生成（ペイロード: { sub: "testuser1", accountNo: 1, accountName: "Test", role: "USER" }）
function createTestJwt(sub: string, accountNo: number): string {
  const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const payload = btoa(
    JSON.stringify({ sub, accountNo, accountName: "Test", role: "USER" })
  );
  return `${header}.${payload}.signature`;
}

function TestComponent() {
  const { user, isAuthenticated, isLoading, login, logout } = useAuth();

  return (
    <div>
      <p data-testid="loading">{isLoading.toString()}</p>
      <p data-testid="authenticated">{isAuthenticated.toString()}</p>
      <p data-testid="user">{user?.accountId || "null"}</p>
      <button onClick={() => login("testuser1", "password1")}>Login</button>
      <button onClick={() => logout()}>Logout</button>
    </div>
  );
}

describe("AuthProvider", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedApiClient.refresh.mockResolvedValue(false);
  });

  it("初期状態ではisLoadingがtrueであること", () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    // 初期レンダリング時はloadingがtrue（すぐにfalseになる場合もある）
    expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
  });

  it("リフレッシュ失敗時は未認証状態であること", async () => {
    mockedApiClient.refresh.mockResolvedValue(false);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
    expect(screen.getByTestId("user")).toHaveTextContent("null");
  });

  it("ログイン成功時にユーザー情報が設定されること", async () => {
    mockedApiClient.refresh.mockResolvedValue(false);
    const testToken = createTestJwt("testuser1", 1);
    mockedApiClient.login.mockResolvedValue({
      accessToken: testToken,
      expiresIn: 900,
    });
    mockedApiClient.getAccessToken.mockReturnValue(testToken);

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    await user.click(screen.getByText("Login"));

    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
      expect(screen.getByTestId("user")).toHaveTextContent("testuser1");
    });
  });

  it("ログアウト時にユーザー情報がクリアされること", async () => {
    mockedApiClient.refresh.mockResolvedValue(false);
    const testToken = createTestJwt("testuser1", 1);
    mockedApiClient.login.mockResolvedValue({
      accessToken: testToken,
      expiresIn: 900,
    });
    mockedApiClient.getAccessToken.mockReturnValue(testToken);
    mockedApiClient.logout.mockResolvedValue(undefined);

    const user = userEvent.setup();

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    await user.click(screen.getByText("Login"));

    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
    });

    await user.click(screen.getByText("Logout"));

    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
      expect(screen.getByTestId("user")).toHaveTextContent("null");
    });
  });
});
