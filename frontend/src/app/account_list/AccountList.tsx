"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getAccountList, type AccountListItem } from "@/lib/api/client";

/**
 * アカウント一覧コンポーネント
 */
export function AccountList() {
  const [accounts, setAccounts] = useState<AccountListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getAccountList()
      .then((data) => {
        setAccounts(data);
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

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
    <div className="flex justify-center py-8">
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
    </div>
  );
}
