"use client";

import { useState, useSyncExternalStore } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import { safeRedirectPath } from "@/lib/url";

/** ライブラリ等の外部購読はないため、購読関数は何もしない */
const subscribeNoop = () => () => {};

/**
 * ログインフォームコンポーネント
 */
export function LoginForm() {
  const [accountId, setAccountId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  // アカウント削除完了後にこの画面へ退避してきた場合の通知（?deleted=1）。
  // クエリはクライアントでのみ参照し、SSR/ハイドレーション時は false を返して不整合を避ける。
  const deletedNotice = useSyncExternalStore(
    subscribeNoop,
    () => new URLSearchParams(window.location.search).get("deleted") === "1",
    () => false
  );

  /**
   * ログイン
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      // ログイン後の遷移先アカウントIDは入力値ではなくトークンの sub を採用し、
      // セッション復元経路（AuthProvider）と揃える
      const loggedInUser = await login(accountId, password);
      const targetAccountId = loggedInUser?.accountId ?? accountId;
      const redirect =
        typeof window !== "undefined"
          ? safeRedirectPath(new URLSearchParams(window.location.search).get("redirect"))
          : null;
      router.push(redirect ?? `/photo/${targetAccountId}/photo_list`);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("ログインに失敗しました");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen bg-[#042844] bg-cover bg-center bg-fixed m-0 font-['Open_Sans',sans-serif]"
      style={{
        // 背景画像はセルフホストする（外部ホスト依存による可用性・Referer 経由の
        // アクセス情報漏れを避ける）。読み込み前・失敗時は bg-[#042844] を表示する
        backgroundImage: "url('/ui/login-bg.jpg')",
      }}
    >
      <div className="flex items-center justify-center min-h-screen w-full p-5 bg-[rgba(4,40,68,0.50)]">
        <form
          onSubmit={handleSubmit}
          className="relative bg-white rounded-sm shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-[10px_20px_80px_20px] w-[90%] max-w-[320px]"
        >
          <h1 className="text-[#444] text-[1.2em] font-bold my-[10px_0_30px_0] mt-[10px] mb-[30px] border-b border-[#eee] pb-5">
            Log in
          </h1>

          {deletedNotice && !error && (
            <p
              role="status"
              className="text-[#1565C0] text-xs font-bold text-left mb-[10px]"
            >
              アカウントを削除しました
            </p>
          )}

          <label htmlFor="login-account-id" className="sr-only">
            アカウントID
          </label>
          <input
            id="login-account-id"
            type="text"
            name="username"
            placeholder="User ID"
            autoFocus
            autoComplete="username"
            value={accountId}
            onChange={(e) => setAccountId(e.target.value)}
            className="block w-full p-[15px_10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] transition-all duration-200 outline-none focus:border-[#2196F3] focus:border-l-[35px]"
          />

          <label htmlFor="login-password" className="sr-only">
            パスワード
          </label>
          <input
            id="login-password"
            type="password"
            name="password"
            placeholder="Password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="block w-full p-[15px_10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] transition-all duration-200 outline-none focus:border-[#2196F3] focus:border-l-[35px]"
          />

          {error && (
            <p
              role="alert"
              className="text-[lightcoral] text-xs font-bold text-left rounded-[5px]"
            >
              {error}
            </p>
          )}

          <Link href="/register" className="text-[0.8em] text-[#1565C0] no-underline">
            Create an account
          </Link>

          <button
            type="submit"
            disabled={isLoading}
            className="absolute left-0 bottom-0 w-full h-[60px] max-h-[60px] bg-[#1565C0] text-white border-none border-b-[7px] border-b-[rgba(0,0,0,0.1)] rounded-b-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#1565C0] focus:border-b-[4px] disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
            ) : (
              <span>Log in</span>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
