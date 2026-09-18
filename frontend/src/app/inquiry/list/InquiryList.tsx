"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { getInquiryList, type InquiryListItem } from "@/lib/api/client";

/** ステータス区分の表示ラベル */
const STATUS_LABELS: Record<string, string> = {
  unreplied: "未対応",
  replied: "回答あり",
  withdrawn: "取り下げ",
};

/**
 * 自分のお問い合わせ一覧コンポーネント
 */
export function InquiryList() {
  const [inquiries, setInquiries] = useState<InquiryListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);
  // 取得リクエストの世代。後着レスポンスが新しい一覧を上書きする競合を防ぐ
  const loadSeqRef = useRef(0);
  const isLoadingMoreRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    const seq = ++loadSeqRef.current;
    const load = async () => {
      try {
        const data = await getInquiryList(1);
        if (cancelled || loadSeqRef.current !== seq) return;
        setInquiries(data.inquiryList);
        setIsLast(data.isLast);
        setError(null);
      } catch (err) {
        if (cancelled || loadSeqRef.current !== seq) return;
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      } finally {
        if (!cancelled && loadSeqRef.current === seq) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const fetchInquiries = async () => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getInquiryList(1);
      if (loadSeqRef.current !== seq) return;
      setInquiries(data.inquiryList);
      setIsLast(data.isLast);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      if (loadSeqRef.current === seq) setIsLoading(false);
    }
  };

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    if (isLoadingMoreRef.current) return;
    isLoadingMoreRef.current = true;
    const nextPage = pageNo + 1;
    const seq = ++loadSeqRef.current;
    setIsLoadingMore(true);
    setLoadMoreError(null);
    try {
      const data = await getInquiryList(nextPage);
      if (loadSeqRef.current !== seq) return;
      setInquiries((prev) => [...prev, ...data.inquiryList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setLoadMoreError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      isLoadingMoreRef.current = false;
      if (loadSeqRef.current === seq) setIsLoadingMore(false);
    }
  };

  const formatDatetime = (datetime: string): string => {
    const date = new Date(datetime);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString("ja-JP");
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
          onClick={fetchInquiries}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          再読み込み
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <div className="w-full max-w-[800px] flex justify-between items-center px-4">
        <h2 className="text-xl font-bold">お問い合わせ一覧</h2>
        <Link
          href="/inquiry"
          className="px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          新規お問い合わせ
        </Link>
      </div>

      {inquiries.length === 0 ? (
        <p className="text-gray-500">お問い合わせはありません</p>
      ) : (
        <div
          className="w-full max-w-[800px] overflow-x-auto"
          style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
        >
          <table className="w-full border-collapse bg-white">
            <thead>
              <tr style={{ backgroundColor: "#2196F3" }}>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  件名
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  ステータス
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  投稿日時
                </th>
              </tr>
            </thead>
            <tbody>
              {inquiries.map((inquiry) => (
                <tr key={inquiry.inquiryNo}>
                  <td className="py-3 px-4 border border-gray-300">
                    <Link
                      href={`/inquiry/detail?inquiryNo=${inquiry.inquiryNo}`}
                      className="text-[#2196F3] hover:underline"
                    >
                      {inquiry.subject}
                    </Link>
                    {inquiry.statusKbn === "replied" && !inquiry.isReadByUser && (
                      <span className="ml-2 inline-block px-2 py-0.5 text-xs bg-red-500 text-white rounded-full">
                        未読
                      </span>
                    )}
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    <span
                      className={
                        inquiry.statusKbn === "replied"
                          ? "text-green-600"
                          : inquiry.statusKbn === "withdrawn"
                            ? "text-gray-400"
                            : "text-gray-500"
                      }
                    >
                      {STATUS_LABELS[inquiry.statusKbn] ?? inquiry.statusKbn}
                    </span>
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    {formatDatetime(inquiry.createdAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

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
