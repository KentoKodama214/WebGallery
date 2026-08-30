"use client";

import { useEffect, useRef, useState } from "react";
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
  // 追加読み込みの失敗通知（取得済みの一覧は維持したまま表示する）
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);
  // インクリメントで 1 ページ目の再取得をトリガーする（再読み込みボタン用）
  const [reloadKey, setReloadKey] = useState(0);
  // 取得リクエストの世代。初期ロード・再読み込み・もっと見るは開始時に採番し、
  // 自分が最新でなければ結果を破棄する（後着レスポンスが新しい一覧を上書きする競合を防ぐ）
  const loadSeqRef = useRef(0);

  useEffect(() => {
    let cancelled = false;
    const seq = ++loadSeqRef.current;
    getAccountList(1)
      .then((data) => {
        if (cancelled || loadSeqRef.current !== seq) return;
        setAccounts(data.accountList);
        setIsLast(data.isLast);
      })
      .catch((err) => {
        if (cancelled || loadSeqRef.current !== seq) return;
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      })
      .finally(() => {
        if (!cancelled && loadSeqRef.current === seq) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  /**
   * エラー画面からの再読み込み
   */
  const handleReload = () => {
    setError(null);
    setLoadMoreError(null);
    setIsLoading(true);
    setPageNo(1);
    setAccounts([]);
    setReloadKey((k) => k + 1);
  };

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    const nextPage = pageNo + 1;
    const seq = ++loadSeqRef.current;
    setIsLoadingMore(true);
    setLoadMoreError(null);
    try {
      const data = await getAccountList(nextPage);
      // 再読み込み等で世代が変わっていたら、古いページを継ぎ足さない
      if (loadSeqRef.current !== seq) return;
      setAccounts((prev) => [...prev, ...data.accountList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      // 取得済みの一覧は維持し、通知だけ表示する
      setLoadMoreError(
        err instanceof Error ? err.message : "エラーが発生しました"
      );
    } finally {
      if (loadSeqRef.current === seq) setIsLoadingMore(false);
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
      <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
        <p className="text-red-500">{error}</p>
        <button
          type="button"
          onClick={handleReload}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          再読み込み
        </button>
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

      {loadMoreError && (
        <p role="alert" className="text-red-500 text-sm">
          {loadMoreError}
        </p>
      )}

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
