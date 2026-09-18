"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  getInquiryDetail,
  withdrawInquiry,
  type InquiryDetail as InquiryDetailData,
} from "@/lib/api/client";
import { ModalDialog } from "@/components/ui/ModalDialog";

/** ステータス区分の表示ラベル */
const STATUS_LABELS: Record<string, string> = {
  unreplied: "未対応",
  replied: "回答済み",
  withdrawn: "取り下げ",
};

interface InquiryDetailProps {
  inquiryNo: number;
}

/**
 * 自分のお問い合わせ詳細コンポーネント（返信一覧を含む）
 */
export function InquiryDetail({ inquiryNo }: InquiryDetailProps) {
  const [detail, setDetail] = useState<InquiryDetailData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showWithdrawConfirm, setShowWithdrawConfirm] = useState(false);
  const [isWithdrawing, setIsWithdrawing] = useState(false);
  const [withdrawError, setWithdrawError] = useState("");

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setIsLoading(true);
      try {
        const data = await getInquiryDetail(inquiryNo);
        if (cancelled) return;
        setDetail(data);
        setError(null);
      } catch (err) {
        if (cancelled) return;
        setError(err instanceof Error ? err.message : "お問い合わせ詳細の取得に失敗しました");
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [inquiryNo]);

  const formatDatetime = (datetime: string): string => {
    const date = new Date(datetime);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString("ja-JP");
  };

  /**
   * 取り下げを実行する
   */
  const handleWithdraw = async () => {
    setIsWithdrawing(true);
    setWithdrawError("");
    try {
      await withdrawInquiry(inquiryNo);
      setDetail((prev) => (prev ? { ...prev, statusKbn: "withdrawn" } : prev));
      setShowWithdrawConfirm(false);
    } catch (err) {
      setWithdrawError(err instanceof Error ? err.message : "お問い合わせの取り下げに失敗しました");
    } finally {
      setIsWithdrawing(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
        <p className="text-red-500">{error ?? "お問い合わせが見つかりません"}</p>
        <Link href="/inquiry/list" className="text-[#2196F3] hover:underline">
          お問い合わせ一覧へ戻る
        </Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4 px-4">
      <div className="w-full max-w-[700px]">
        <Link href="/inquiry/list" className="text-[#2196F3] hover:underline text-sm">
          &larr; お問い合わせ一覧へ戻る
        </Link>

        <div className="bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-6 mt-4">
          <div className="flex justify-between items-start mb-2">
            <h2 className="text-lg font-bold text-[#444]">{detail.subject}</h2>
            <span
              className={
                detail.statusKbn === "replied"
                  ? "text-green-600 text-sm font-bold"
                  : detail.statusKbn === "withdrawn"
                    ? "text-gray-400 text-sm font-bold"
                    : "text-red-600 text-sm font-bold"
              }
            >
              {STATUS_LABELS[detail.statusKbn] ?? detail.statusKbn}
            </span>
          </div>
          <p className="text-gray-400 text-xs mb-4">{formatDatetime(detail.createdAt)}</p>
          <p className="text-[#444] whitespace-pre-wrap">{detail.body}</p>
        </div>

        {detail.statusKbn !== "withdrawn" && (
          <div className="mt-4">
            <button
              type="button"
              onClick={() => setShowWithdrawConfirm(true)}
              className="text-sm text-[lightcoral] hover:underline cursor-pointer bg-none border-none p-0"
            >
              このお問い合わせを取り下げる
            </button>
          </div>
        )}

        {detail.replyList.length > 0 && (
          <div className="mt-6 flex flex-col gap-4">
            <h3 className="text-md font-bold text-[#444]">返信</h3>
            {detail.replyList.map((reply) => (
              <div
                key={reply.replyNo}
                className="bg-blue-50 rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.15)] p-5"
              >
                <p className="text-gray-400 text-xs mb-2">{formatDatetime(reply.createdAt)}</p>
                <p className="text-[#444] whitespace-pre-wrap">{reply.body}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {showWithdrawConfirm && (
        <ModalDialog
          testId="withdraw-confirm-dialog"
          label="お問い合わせ取り下げの確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => {
            if (isWithdrawing) return;
            setShowWithdrawConfirm(false);
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg max-w-[320px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-4">
            このお問い合わせを取り下げますか？取り下げ後は元に戻せません。
          </p>
          {withdrawError && (
            <p role="alert" className="text-[lightcoral] text-xs font-bold mb-2 text-center">
              {withdrawError}
            </p>
          )}
          <div className="flex gap-3">
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => setShowWithdrawConfirm(false)}
              disabled={isWithdrawing}
              className="flex-1 h-[40px] bg-gray-300 text-[#444] rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              キャンセル
            </button>
            <button
              type="button"
              onClick={handleWithdraw}
              disabled={isWithdrawing}
              className="flex-1 h-[40px] bg-[lightcoral] text-white rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {isWithdrawing ? "処理中..." : "取り下げる"}
            </button>
          </div>
        </ModalDialog>
      )}
    </div>
  );
}
