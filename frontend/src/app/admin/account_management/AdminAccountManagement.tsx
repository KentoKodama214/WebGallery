"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAdminAccountList,
  unlockAccount,
  lockAccount,
  type AdminAccountListItem,
} from "@/lib/api/client";

/**
 * 管理者用アカウント管理コンポーネント
 */
export function AdminAccountManagement() {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [accounts, setAccounts] = useState<AdminAccountListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);

  const isAdmin = user?.role === "ROLE_ADMIN";

  useEffect(() => {
    if (isAuthLoading) return;
    if (!isAuthenticated || !isAdmin) {
      setIsLoading(false);
      return;
    }

    fetchAccounts();
  }, [isAuthLoading, isAuthenticated, isAdmin]);

  const fetchAccounts = async () => {
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getAdminAccountList(1);
      setAccounts(data.accountList);
      setIsLast(data.isLast);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    const nextPage = pageNo + 1;
    setIsLoadingMore(true);
    try {
      const data = await getAdminAccountList(nextPage);
      setAccounts((prev) => [...prev, ...data.accountList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoadingMore(false);
    }
  };

  const handleUnlock = async (accountNo: number, accountId: string) => {
    if (!confirm(`${accountId} のロックを解除しますか？`)) return;
    setMessage(null);
    setError(null);
    try {
      const result = await unlockAccount(accountNo);
      setMessage(result.message);
      await fetchAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    }
  };

  const handleLock = async (accountNo: number, accountId: string) => {
    if (!confirm(`${accountId} を強制ロックしますか？`)) return;
    setMessage(null);
    setError(null);
    try {
      const result = await lockAccount(accountNo);
      setMessage(result.message);
      await fetchAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    }
  };

  if (isAuthLoading || isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (!isAuthenticated || !isAdmin) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p className="text-red-500">管理者権限がありません。</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
        <p className="text-red-500">{error}</p>
        <button
          onClick={fetchAccounts}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          再読み込み
        </button>
      </div>
    );
  }

  const formatDatetime = (datetime: string | null): string => {
    if (!datetime) return "-";
    const date = new Date(datetime);
    if (date.getFullYear() <= 1900) return "-";
    return date.toLocaleString("ja-JP");
  };

  const authorityLabel = (kbn: string): string => {
    switch (kbn) {
      case "administrator": return "管理者";
      case "special-user": return "特別ユーザー";
      case "normal-user": return "一般ユーザー";
      case "mini-user": return "簡易ユーザー";
      default: return kbn;
    }
  };

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <h2 className="text-xl font-bold">アカウント管理</h2>

      {message && (
        <p className="text-green-600 font-medium">{message}</p>
      )}

      <div
        className="w-full max-w-[1100px] overflow-x-auto"
        style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
      >
        <table className="w-full border-collapse">
          <thead>
            <tr style={{ backgroundColor: "#2196F3" }}>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">No</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">ID</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">アカウント名</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">権限</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">状態</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">最終ログイン</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">失敗回数</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">操作</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr
                key={account.accountNo}
                className="bg-white transition-colors"
                style={{ cursor: "default" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.backgroundColor = "#fffae9")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.backgroundColor = "white")
                }
              >
                <td className="py-3 px-4 border border-gray-300">{account.accountNo}</td>
                <td className="py-3 px-4 border border-gray-300">{account.accountId}</td>
                <td className="py-3 px-4 border border-gray-300">{account.accountName}</td>
                <td className="py-3 px-4 border border-gray-300">{authorityLabel(account.authorityKbn)}</td>
                <td className="py-3 px-4 border border-gray-300">
                  {account.isDeleted ? (
                    <span className="text-gray-500">削除済み</span>
                  ) : account.loginFailureCount >= 10 ? (
                    <span className="text-red-500 font-bold">ロック中</span>
                  ) : (
                    <span className="text-green-600">有効</span>
                  )}
                </td>
                <td className="py-3 px-4 border border-gray-300">{formatDatetime(account.lastLoginDatetime)}</td>
                <td className="py-3 px-4 border border-gray-300">{account.loginFailureCount}</td>
                <td className="py-3 px-4 border border-gray-300">
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleUnlock(account.accountNo, account.accountId)}
                      className="px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
                      disabled={account.loginFailureCount === 0}
                    >
                      ロック解除
                    </button>
                    <button
                      onClick={() => handleLock(account.accountNo, account.accountId)}
                      className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
                      disabled={account.loginFailureCount >= 10}
                    >
                      強制ロック
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {!isLast && (
        <button
          onClick={handleLoadMore}
          disabled={isLoadingMore}
          data-testid="show-more-button"
          className="px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
        >
          {isLoadingMore ? "読み込み中..." : "＋もっと見る"}
        </button>
      )}
    </div>
  );
}
