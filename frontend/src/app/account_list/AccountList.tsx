"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getAccountList, type AccountListItem } from "@/lib/api/client";

/**
 * アカウント一覧コンポーネント
 */
export function AccountList() {
  const [accounts, setAccounts] = useState<AccountListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);

  useEffect(() => {
    getAccountList(1)
      .then((data) => {
        setAccounts(data.accountList);
        setIsLast(data.isLast);
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    const nextPage = pageNo + 1;
    setIsLoadingMore(true);
    try {
      const data = await getAccountList(nextPage);
      setAccounts((prev) => [...prev, ...data.accountList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoadingMore(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p className="text-red-500">{error}</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <div
        className="w-[650px]"
        style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
      >
        <table className="w-full border-collapse">
          <thead>
            <tr style={{ backgroundColor: "#2196F3" }}>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                ID
              </th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                アカウント名
              </th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                ギャラリー
              </th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr
                key={account.accountId}
                className="bg-white transition-colors"
                style={{ cursor: "default" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.backgroundColor = "#fffae9")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.backgroundColor = "white")
                }
              >
                <td className="py-3 px-4 border border-gray-300">
                  {account.accountId}
                </td>
                <td className="py-3 px-4 border border-gray-300">
                  {account.accountName}
                </td>
                <td className="py-3 px-4 border border-gray-300">
                  <Link
                    href={`/photo/${account.accountId}/photo_list`}
                    className="text-blue-600 hover:underline"
                  >
                    ギャラリーを見る
                  </Link>
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
