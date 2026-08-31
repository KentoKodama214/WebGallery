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
  login: (accountId: string, password: string) => Promise<User>;
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
          } else {
            // リフレッシュは成功したがトークンを解釈できない場合は、
            // 「API は認証済みだが画面は未ログイン」の不整合を避けるため
            // 認証状態をまとめてクリアする
            apiClient.clearAuthState();
          }
        }
      } catch {
        // apiClient.refresh() はネットワーク例外・5xx を内部で握って false を返すため
        // 通常ここには到達しない。parseJwt / setUser 等が想定外に投げた場合の保険として、
        // セッション復元に失敗しても未認証で続行する。
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    initAuth();
  }, []);

  const login = useCallback(async (accountId: string, password: string): Promise<User> => {
    await apiClient.login(accountId, password);
    const token = apiClient.getAccessToken();
    const payload = token ? parseJwt(token) : null;
    if (!payload) {
      // 発行されたトークンを解釈できない場合はログイン失敗として扱う。
      // apiClient.login が立てた認証状態（accessToken / sessionAuthState / epoch）も
      // まとめてクリアし、「画面は未ログインなのに API だけ認証済み」の不整合を防ぐ。
      apiClient.clearAuthState();
      throw new Error("ログインに失敗しました");
    }
    // アカウントIDは入力値ではなくトークンの sub を採用し、セッション復元経路と揃える
    const nextUser: User = {
      accountId: payload.sub,
      accountNo: payload.accountNo,
      role: payload.role,
    };
    setUser(nextUser);
    return nextUser;
  }, []);

  const logout = useCallback(async () => {
    await apiClient.logout();
    setUser(null);
  }, []);

  // アクセストークンの有効期限が近づいたら先読みでリフレッシュする。
  // これにより「期限切れ後の最初の API 呼び出しで 401 → リフレッシュ」の往復を減らす。
  // exp を持たないトークン（テスト用など）では何もしない。
  //
  // リフレッシュが失敗した場合はここでは状態を変えない（一時的な 5xx / オフラインを
  // 未ログイン扱いにしないため）。失効の確定は従来どおり `fetchWithAuth` の
  // 401 → refresh → `clearAuthState` 経路に委ねる。
  useEffect(() => {
    if (!user) return;
    const token = apiClient.getAccessToken();
    const payload = token ? parseJwt(token) : null;
    if (!payload?.exp) return;

    // 期限の 30 秒前（最短でも 5 秒後）に実行する
    const leadMs = 30_000;
    const delay = Math.max(payload.exp * 1000 - Date.now() - leadMs, 5_000);

    const timer = setTimeout(async () => {
      try {
        const refreshed = await apiClient.refresh();
        if (!refreshed) return;
        const nextToken = apiClient.getAccessToken();
        const nextPayload = nextToken ? parseJwt(nextToken) : null;
        if (nextPayload) {
          setUser({
            accountId: nextPayload.sub,
            accountNo: nextPayload.accountNo,
            role: nextPayload.role,
          });
        }
      } catch {
        // 先読みリフレッシュの失敗は無視する（次の API 呼び出しで扱う）
      }
    }, delay);

    return () => clearTimeout(timer);
  }, [user]);

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
  /** 有効期限（UNIX 秒）。標準的な JWT には含まれる */
  exp?: number;
}

/**
 * JWTのペイロードをパースする
 *
 * 署名の検証は行わないため、ここで得られる情報（role等）はUI表示の出し分けにのみ
 * 使用し、認可の判断に用いてはならない。認可は必ずバックエンドで行われる。
 *
 * `exp` が含まれ、かつ既に期限切れのトークンは無効（null）として扱う。
 * リフレッシュ／ログイン直後は必ず有効期限が先のトークンが得られるため、
 * これにより「期限切れトークンで画面だけログイン状態」を防ぐ。
 *
 * @param token JWTアクセストークン
 * @returns パース済みペイロード。不正・期限切れのトークンの場合はnull
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
    // exp を持つ場合は数値であることと、既に期限切れでないことを確認する
    if (parsed.exp !== undefined) {
      if (typeof parsed.exp !== "number" || parsed.exp * 1000 <= Date.now()) {
        return null;
      }
    }
    return parsed as JwtPayload;
  } catch {
    return null;
  }
}
