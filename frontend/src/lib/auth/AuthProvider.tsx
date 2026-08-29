"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import type { ReactNode } from "react";
import * as apiClient from "@/lib/api/client";

export interface User {
  accountId: string;
  accountNo: number;
  role: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (accountId: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // マウント時にリフレッシュを試行し、既存セッションを復元
    const initAuth = async () => {
      try {
        const success = await apiClient.refresh();
        if (success) {
          const token = apiClient.getAccessToken();
          const payload = token ? parseJwt(token) : null;
          if (payload) {
            setUser({
              accountId: payload.sub,
              accountNo: payload.accountNo,
              role: payload.role,
            });
          }
        }
      } catch {
        // セッション復元に失敗しても未認証として続行する
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    initAuth();
  }, []);

  const login = useCallback(async (accountId: string, password: string) => {
    await apiClient.login(accountId, password);
    const token = apiClient.getAccessToken();
    const payload = token ? parseJwt(token) : null;
    if (payload) {
      setUser({ accountId, accountNo: payload.accountNo, role: payload.role });
    }
  }, []);

  const logout = useCallback(async () => {
    await apiClient.logout();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

interface JwtPayload {
  sub: string;
  accountNo: number;
  accountName: string;
  role: string;
}

/**
 * JWTのペイロードをパースする
 *
 * 署名の検証は行わないため、ここで得られる情報（role等）はUI表示の出し分けにのみ
 * 使用し、認可の判断に用いてはならない。認可は必ずバックエンドで行われる。
 *
 * @param token JWTアクセストークン
 * @returns パース済みペイロード。不正なトークンの場合はnull
 */
function parseJwt(token: string): JwtPayload | null {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    // base64urlはパディングが省略されるため補完する
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const jsonPayload = decodeURIComponent(
      atob(padded)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const parsed = JSON.parse(jsonPayload) as Partial<JwtPayload>;
    // UI 表示に必要な項目が揃っていない場合は不正なトークンとして扱う
    if (
      typeof parsed.sub !== "string" ||
      typeof parsed.accountNo !== "number" ||
      typeof parsed.role !== "string"
    ) {
      return null;
    }
    return parsed as JwtPayload;
  } catch {
    return null;
  }
}
