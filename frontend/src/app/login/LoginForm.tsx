"use client";

import { useCallback, useEffect, useRef, useState, useSyncExternalStore } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getPasskeyAuthenticationOptions } from "@/lib/api/client";
import { useAuth } from "@/lib/auth/AuthProvider";
import type { User } from "@/lib/auth/AuthProvider";
import {
  PasskeyCancelledError,
  getPasskeyCredential,
  isConditionalMediationAvailable,
  isPasskeySupported,
} from "@/lib/auth/passkey";
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
  const [isPasskeyLoading, setIsPasskeyLoading] = useState(false);
  const { login, loginWithPasskey } = useAuth();
  const router = useRouter();
  // ユーザー名欄のオートフィルからパスキーを選べるようにする待機（Conditional UI）の中止用
  const conditionalAbortRef = useRef<AbortController | null>(null);

  // パスキー非対応のブラウザ（WebAuthn 非対応・非安全なコンテキスト）ではボタンを出さない。
  // SSR/ハイドレーション時は false を返して不整合を避ける。
  const passkeySupported = useSyncExternalStore(
    subscribeNoop,
    () => isPasskeySupported(),
    () => false
  );

  // アカウント削除完了後にこの画面へ退避してきた場合の通知（?deleted=1）。
  // クエリはクライアントでのみ参照し、SSR/ハイドレーション時は false を返して不整合を避ける。
  const deletedNotice = useSyncExternalStore(
    subscribeNoop,
    () => new URLSearchParams(window.location.search).get("deleted") === "1",
    () => false
  );

  /**
   * ログイン後の画面へ遷移する
   *
   * 遷移先アカウントIDは入力値ではなくトークンの sub を採用し、セッション復元経路（AuthProvider）と揃える
   */
  const redirectAfterLogin = useCallback(
    (loggedInUser: User | undefined, fallbackAccountId: string) => {
      const targetAccountId = loggedInUser?.accountId ?? fallbackAccountId;
      const redirect =
        typeof window !== "undefined"
          ? safeRedirectPath(new URLSearchParams(window.location.search).get("redirect"))
          : null;
      router.push(redirect ?? `/photo/${targetAccountId}/photo_list`);
    },
    [router]
  );

  /**
   * 認証器が返した認証レスポンスでログインし、ログイン後の画面へ遷移する
   *
   * @param credential 認証レスポンス（`PublicKeyCredential#toJSON()` の形式の JSON 文字列）
   */
  const completePasskeyLogin = useCallback(
    async (credential: string) => {
      setError("");
      setIsPasskeyLoading(true);
      try {
        const loggedInUser = await loginWithPasskey(credential);
        redirectAfterLogin(loggedInUser, "");
      } catch (err) {
        setError(err instanceof Error ? err.message : "パスキーによる認証に失敗しました");
      } finally {
        setIsPasskeyLoading(false);
      }
    },
    [loginWithPasskey, redirectAfterLogin]
  );

  // 対応ブラウザでは、ユーザー名欄のオートフィル候補にパスキーを表示する（Conditional UI）。
  // 選ばれるまで待機し続けるため、画面を離れる・ボタン操作に切り替える際は中止する
  useEffect(() => {
    if (!passkeySupported) return;
    const controller = new AbortController();
    conditionalAbortRef.current = controller;
    (async () => {
      if (!(await isConditionalMediationAvailable()) || controller.signal.aborted) return;
      let credential: string;
      try {
        const options = await getPasskeyAuthenticationOptions();
        credential = await getPasskeyCredential(options, {
          conditional: true,
          signal: controller.signal,
        });
      } catch {
        // 待ち受けの開始・中止・キャンセルの失敗は表示しない。利用者はまだ何も操作しておらず、
        // ボタンやパスワードでのログインはそのまま使えるため（パスキーが選ばれた後の失敗のみ表示する）
        return;
      }
      if (controller.signal.aborted) return;
      await completePasskeyLogin(credential);
    })();
    return () => controller.abort();
  }, [passkeySupported, completePasskeyLogin]);

  /**
   * 「パスキーでログイン」ボタン
   */
  const handlePasskeyLogin = async () => {
    // オートフィル待ちが残っていると新しいパスキー要求が拒否されるため、先に中止する
    conditionalAbortRef.current?.abort();
    setError("");
    let credential: string;
    try {
      const options = await getPasskeyAuthenticationOptions();
      credential = await getPasskeyCredential(options, { conditional: false, signal: undefined });
    } catch (err) {
      // 利用者自身によるキャンセルは失敗として表示しない
      if (err instanceof PasskeyCancelledError) return;
      setError(err instanceof Error ? err.message : "パスキーによる認証に失敗しました");
      return;
    }
    await completePasskeyLogin(credential);
  };

  /**
   * ログイン
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const loggedInUser = await login(accountId, password);
      redirectAfterLogin(loggedInUser, accountId);
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
            // "webauthn" を付けると、対応ブラウザでオートフィル候補にパスキーが並ぶ（Conditional UI）
            autoComplete="username webauthn"
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

          {passkeySupported && (
            <button
              type="button"
              onClick={handlePasskeyLogin}
              disabled={isLoading || isPasskeyLoading}
              className="block w-full p-[10px] mt-[10px] mb-[10px] border border-[#1565C0] rounded-sm text-[#1565C0] bg-white cursor-pointer transition-colors duration-100 hover:bg-[#E3F2FD] disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {isPasskeyLoading ? "認証しています…" : "パスキーでログイン"}
            </button>
          )}

          <Link href="/register" className="text-[0.8em] text-[#1565C0] no-underline">
            Create an account
          </Link>

          <button
            type="submit"
            disabled={isLoading || isPasskeyLoading}
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
