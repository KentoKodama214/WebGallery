"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";

/**
 * ログインフォームコンポーネント
 * 既存のThymeleafデザインをTailwind CSSで再現
 */
export function LoginForm() {
  const [accountId, setAccountId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      await login(accountId, password);
      router.push(`/photo/${accountId}/photo_list`);
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
      className="min-h-screen bg-cover bg-center bg-fixed m-0 font-['Open_Sans',sans-serif]"
      style={{
        backgroundImage:
          "url('https://www.kkodama-photo.com/wp/wp-content/uploads/2020/10/DSC15567-scaled.jpg')",
      }}
    >
      <div className="flex items-center justify-center min-h-screen w-full p-5 bg-[rgba(4,40,68,0.50)]">
        <form
          onSubmit={handleSubmit}
          className="relative bg-white rounded-sm shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-[10px_20px_80px_20px] w-[90%] max-w-[320px]"
        >
          <p className="text-[#444] text-[1.2em] font-bold my-[10px_0_30px_0] mt-[10px] mb-[30px] border-b border-[#eee] pb-5">
            Log in
          </p>

          <input
            type="text"
            name="username"
            placeholder="User ID"
            tabIndex={1}
            autoFocus
            value={accountId}
            onChange={(e) => setAccountId(e.target.value)}
            className="block w-full p-[15px_10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] transition-all duration-200 outline-none focus:border-[#2196F3] focus:border-l-[35px]"
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            tabIndex={2}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="block w-full p-[15px_10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] transition-all duration-200 outline-none focus:border-[#2196F3] focus:border-l-[35px]"
          />

          {error && (
            <p className="text-[lightcoral] text-xs font-bold text-left rounded-[5px]">
              {error}
            </p>
          )}

          <a href="/contact" className="text-[0.8em] text-[#2196F3] no-underline">
            Forgot your password?
          </a>
          <br />
          <a href="/register" className="text-[0.8em] text-[#2196F3] no-underline">
            Create an account
          </a>

          <button
            type="submit"
            tabIndex={3}
            disabled={isLoading}
            className="absolute left-0 bottom-0 w-full h-[60px] max-h-[60px] bg-[#2196F3] text-white border-none border-b-[7px] border-b-[rgba(0,0,0,0.1)] rounded-b-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#2196F3] focus:border-b-[4px] disabled:opacity-70 disabled:cursor-not-allowed"
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
