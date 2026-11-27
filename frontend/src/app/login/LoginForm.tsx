"use client";

import { useCallback, useState, useSyncExternalStore } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getPasskeyAuthenticationOptions } from "@/lib/api/client";
import { useAuth } from "@/lib/auth/AuthProvider";
import type { User } from "@/lib/auth/AuthProvider";
import {
  PasskeyCancelledError,
  getPasskeyCredential,
  isPasskeySupported,
} from "@/lib/auth/passkey";
import { safeRedirectPath } from "@/lib/url";

/** ライブラリ等の外部購読はないため、購読関数は何もしない */
const subscribeNoop = () => () => {};

/**
 * ログインフォームコンポーネント
 *
 * ログインはパスキー（端末の指紋・顔認証・PIN）のみで行う。パスキーはアカウントを特定する情報を
 * 端末側に持つ（discoverable credential）ため、アカウントIDの入力は不要
 */
export function LoginForm() {
  const [error, setError] = useState("");
  const [isPasskeyLoading, setIsPasskeyLoading] = useState(false);
  const { loginWithPasskey } = useAuth();
  const router = useRouter();

  // パスキー非対応のブラウザ（WebAuthn 非対応・非安全なコンテキスト）ではログインできない。
  // SSR/ハイドレーション時は true を返し、対応ブラウザで注意書きがちらつかないようにする。
  const passkeySupported = useSyncExternalStore(
    subscribeNoop,
    () => isPasskeySupported(),
    () => true
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
   * 遷移先アカウントIDはトークンの sub を採用し、セッション復元経路（AuthProvider）と揃える
   */
  const redirectAfterLogin = useCallback(
    (loggedInUser: User) => {
      const redirect =
        typeof window !== "undefined"
          ? safeRedirectPath(new URLSearchParams(window.location.search).get("redirect"))
          : null;
      router.push(redirect ?? `/photo/${loggedInUser.accountId}/photo_list`);
    },
    [router]
  );

  /**
   * 「パスキーでログイン」ボタン
   */
  const handlePasskeyLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isPasskeyLoading) return;
    setError("");
    setIsPasskeyLoading(true);
    try {
      const options = await getPasskeyAuthenticationOptions();
      const credential = await getPasskeyCredential(options);
      const loggedInUser = await loginWithPasskey(credential);
      redirectAfterLogin(loggedInUser);
    } catch (err) {
      // 利用者自身によるキャンセルは失敗として表示しない
      if (!(err instanceof PasskeyCancelledError)) {
        setError(err instanceof Error ? err.message : "パスキーによる認証に失敗しました");
      }
    } finally {
      setIsPasskeyLoading(false);
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
          onSubmit={handlePasskeyLogin}
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

          <p className="text-[#444] text-sm mb-[10px]">
            登録した端末のパスキー（指紋・顔認証・PIN）でログインします。
          </p>

          {!passkeySupported && (
            <p className="text-[#C62828] text-xs font-bold text-left mb-[10px]">
              このブラウザはパスキーに対応していないため、ログインできません。パスキーに対応したブラウザをご利用ください
            </p>
          )}

          {error && (
            <p
              role="alert"
              className="text-[#C62828] text-xs font-bold text-left rounded-[5px] mb-[10px]"
            >
              {error}
            </p>
          )}

          <div className="flex flex-col gap-1">
            <Link href="/register" className="text-[0.8em] text-[#1565C0] no-underline">
              Create an account
            </Link>
            <Link href="/recovery" className="text-[0.8em] text-[#1565C0] no-underline">
              パスキーを失った場合（リカバリーコードで復旧）
            </Link>
          </div>

          <button
            type="submit"
            disabled={isPasskeyLoading || !passkeySupported}
            className="absolute left-0 bottom-0 w-full h-[60px] max-h-[60px] bg-[#1565C0] text-white border-none border-b-[7px] border-b-[rgba(0,0,0,0.1)] rounded-b-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#1565C0] focus:border-b-[4px] disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {isPasskeyLoading ? "認証しています…" : "パスキーでログイン"}
          </button>
        </form>
      </div>
    </div>
  );
}
