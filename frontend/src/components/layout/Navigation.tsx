"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";

/**
 * ナビゲーションコンポーネント
 * 認証状態に応じてリンクを表示
 */
export function Navigation() {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated || !user) {
    return null;
  }

  return (
    <nav>
      <Link
        href={`/photo/${user.accountId}/photo_list`}
        className="fixed top-[2%] left-[2%] text-xl text-gray-400 z-[1000] no-underline"
      >
        {user.accountId}
      </Link>
    </nav>
  );
}
