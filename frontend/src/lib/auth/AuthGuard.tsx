"use client";

import { useEffect } from "react";
import type { ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import { loginUrlWithRedirect } from "@/lib/url";

interface AuthGuardProps {
  /** 認証済みのときだけレンダリングする保護コンテンツ */
  children: ReactNode;
  /** 認証確認中に表示する内容（未指定時は中立的なスピナー） */
  fallback?: ReactNode;
}

/**
 * ログイン必須ページのガード
 *
 * 認証状態が確定するまで `children` を一切マウントせず、未ログインが確定したら
 * `/login` へ誘導する。保護対象のコンポーネント（フォーム等）が未ログインでも
 * 一瞬マウントされてしまう問題（レビュー M-1）を防ぐ。
 *
 * リダイレクトが何らかの理由で進まなくても無限スピナーにならないよう、
 * 未ログイン確定時は明示的なメッセージとログインリンクを表示する（レビュー L-1）。
 *
 * 認可（管理者権限など）はここでは扱わない。配下のコンポーネント側で判定する。
 */
export function AuthGuard({ children, fallback }: AuthGuardProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace(loginUrlWithRedirect());
    }
  }, [isLoading, isAuthenticated, router]);

  if (isLoading) {
    return <>{fallback ?? <AuthGuardFallback />}</>;
  }

  if (!isAuthenticated) {
    return (
      <div
        style={{
          minHeight: "60vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "12px",
          padding: "0 16px",
          textAlign: "center",
        }}
      >
        <p>ログインが必要です。</p>
        <a href={loginUrlWithRedirect()} style={{ color: "#2196F3" }}>
          ログインページへ移動
        </a>
      </div>
    );
  }

  return <>{children}</>;
}

/** 認証確認中のデフォルト表示 */
function AuthGuardFallback() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <span
        role="status"
        aria-label="読み込み中"
        className="inline-block w-8 h-8 border-4 border-[#2196F3] border-t-transparent rounded-full animate-spin"
      />
    </div>
  );
}
