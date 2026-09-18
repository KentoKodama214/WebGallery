"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAdminInquiryList,
  type AdminInquiryListItem,
  type InquiryStatusKbn,
} from "@/lib/api/client";

/** ステータス区分の選択肢（表示順もこの並びに従う） */
const STATUS_OPTIONS: { value: InquiryStatusKbn | ""; label: string }[] = [
  { value: "", label: "すべて" },
  { value: "unreplied", label: "未対応" },
  { value: "replied", label: "回答済み" },
  { value: "withdrawn", label: "取り下げ" },
];

const STATUS_LABELS: Record<string, string> = {
  unreplied: "未対応",
  replied: "回答済み",
  withdrawn: "取り下げ",
};

/**
 * 管理者用お問い合わせ管理コンポーネント
 */
export function AdminInquiryManagement() {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [inquiries, setInquiries] = useState<AdminInquiryListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);
  const [statusFilter, setStatusFilter] = useState<InquiryStatusKbn | "">("");
  const loadSeqRef = useRef(0);
  const isLoadingMoreRef = useRef(false);

  const isAdmin = user?.role === "ROLE_ADMIN";
  const canView = !isAuthLoading && isAuthenticated && isAdmin;

  const fetchInquiries = async (status: InquiryStatusKbn | "") => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getAdminInquiryList(1, status || undefined);
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

  useEffect(() => {
    if (!canView) return;

    let cancelled = false;
    const seq = ++loadSeqRef.current;
    const load = async () => {
      setIsLoading(true);
      try {
        const data = await getAdminInquiryList(1, statusFilter || undefined);
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
  }, [canView, statusFilter]);

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
      const data = await getAdminInquiryList(nextPage, statusFilter || undefined);
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

  if (isAuthLoading) {
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
          onClick={() => fetchInquiries(statusFilter)}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          再読み込み
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <h2 className="text-xl font-bold">お問い合わせ管理</h2>

      <div className="w-full max-w-[1000px] flex items-center gap-2 px-4">
        <label htmlFor="status-filter" className="text-sm text-[#444]">
          ステータス
        </label>
        <select
          id="status-filter"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as InquiryStatusKbn | "")}
          className="border border-gray-300 rounded-sm px-2 py-1"
        >
          {STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {inquiries.length === 0 ? (
        <p className="text-gray-500">お問い合わせはありません</p>
      ) : (
        <div
          className="w-full max-w-[1000px] overflow-x-auto"
          style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
        >
          <table className="w-full border-collapse bg-white">
            <thead>
              <tr style={{ backgroundColor: "#2196F3" }}>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  ID
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  アカウントID
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  アカウント名
                </th>
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
                <tr key={inquiry.inquiryId}>
                  <td className="py-3 px-4 border border-gray-300">{inquiry.inquiryId}</td>
                  <td className="py-3 px-4 border border-gray-300">{inquiry.accountId}</td>
                  <td className="py-3 px-4 border border-gray-300">{inquiry.accountName}</td>
                  <td className="py-3 px-4 border border-gray-300">
                    <Link
                      href={`/admin/inquiry_management/detail?inquiryId=${inquiry.inquiryId}`}
                      className="text-[#2196F3] hover:underline"
                    >
                      {inquiry.subject}
                    </Link>
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    <span
                      className={
                        inquiry.statusKbn === "replied"
                          ? "text-green-600"
                          : inquiry.statusKbn === "withdrawn"
                            ? "text-gray-400"
                            : "text-red-600"
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
