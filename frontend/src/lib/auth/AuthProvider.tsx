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
      const success = await apiClient.refresh();
      if (success) {
        const token = apiClient.getAccessToken();
        if (token) {
          const payload = parseJwt(token);
          setUser({ accountId: payload.sub, accountNo: payload.accountNo });
        }
      }
      setIsLoading(false);
    };
    initAuth();
  }, []);

  const login = useCallback(async (accountId: string, password: string) => {
    await apiClient.login(accountId, password);
    const token = apiClient.getAccessToken();
    if (token) {
      const payload = parseJwt(token);
      setUser({ accountId, accountNo: payload.accountNo });
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

/**
 * JWTのペイロードをパースする
 */
function parseJwt(token: string): { sub: string; accountNo: number; accountName: string; role: string } {
  const base64Url = token.split(".")[1];
  const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split("")
      .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
      .join("")
  );
  return JSON.parse(jsonPayload);
}
