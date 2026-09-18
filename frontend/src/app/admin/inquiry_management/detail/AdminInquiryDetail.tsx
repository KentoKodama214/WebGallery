"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAdminInquiryDetail,
  replyToInquiry,
  type AdminInquiryDetail as AdminInquiryDetailData,
} from "@/lib/api/client";
import { INQUIRY_BODY_MAX_LENGTH } from "@/lib/validation";

/** ステータス区分の表示ラベル */
const STATUS_LABELS: Record<string, string> = {
  unreplied: "未対応",
  replied: "回答済み",
  withdrawn: "取り下げ",
};

interface AdminInquiryDetailProps {
  inquiryId: number;
}

/**
 * 管理者用お問い合わせ詳細コンポーネント（返信一覧・返信投稿フォームを含む）
 */
export function AdminInquiryDetail({ inquiryId }: AdminInquiryDetailProps) {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [detail, setDetail] = useState<AdminInquiryDetailData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [replyBody, setReplyBody] = useState("");
  const [replyError, setReplyError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const isAdmin = user?.role === "ROLE_ADMIN";
  const canView = !isAuthLoading && isAuthenticated && isAdmin;

  useEffect(() => {
    if (!canView) return;

    let cancelled = false;
    const load = async () => {
      setIsLoading(true);
      try {
        const data = await getAdminInquiryDetail(inquiryId);
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
  }, [canView, inquiryId]);

  const formatDatetime = (datetime: string): string => {
    const date = new Date(datetime);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString("ja-JP");
  };

  /**
   * 返信送信
   */
  const handleReplySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);

    if (!replyBody.trim()) {
      setReplyError("返信内容を入力してください");
      return;
    }
    if (replyBody.length > INQUIRY_BODY_MAX_LENGTH) {
      setReplyError(`返信内容は${INQUIRY_BODY_MAX_LENGTH}文字以内で入力してください`);
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await replyToInquiry(inquiryId, replyBody);
      setMessage(result.message);
      setReplyBody("");
      setReplyError("");
      const data = await getAdminInquiryDetail(inquiryId);
      setDetail(data);
    } catch (err) {
      setReplyError(err instanceof Error ? err.message : "返信の登録に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
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

  if (error || !detail) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
        <p className="text-red-500">{error ?? "お問い合わせが見つかりません"}</p>
        <Link href="/admin/inquiry_management" className="text-[#2196F3] hover:underline">
          お問い合わせ管理へ戻る
        </Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4 px-4">
      <div className="w-full max-w-[700px]">
        <Link href="/admin/inquiry_management" className="text-[#2196F3] hover:underline text-sm">
          &larr; お問い合わせ管理へ戻る
        </Link>

        {message && <p className="text-green-600 font-medium mt-2">{message}</p>}

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
          <p className="text-gray-400 text-xs mb-1">
            {detail.accountId}（{detail.accountName}） / {formatDatetime(detail.createdAt)}
          </p>
          <p className="text-[#444] whitespace-pre-wrap mt-3">{detail.body}</p>
        </div>

        {detail.replyList.length > 0 && (
          <div className="mt-6 flex flex-col gap-4">
            <h3 className="text-md font-bold text-[#444]">返信履歴</h3>
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

        {detail.statusKbn === "withdrawn" ? (
          <p className="text-gray-400 text-sm mt-6 text-center">
            このお問い合わせは取り下げられているため、返信できません。
          </p>
        ) : (
          <form
            onSubmit={handleReplySubmit}
            className="bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-6 mt-6"
          >
            <label htmlFor="reply-body" className="block text-[#444] text-sm mb-1 font-bold">
              返信する
            </label>
            <textarea
              id="reply-body"
              value={replyBody}
              onChange={(e) => {
                setReplyBody(e.target.value);
                setReplyError((prev) => (prev ? "" : prev));
              }}
              maxLength={INQUIRY_BODY_MAX_LENGTH}
              rows={6}
              aria-invalid={replyError ? true : undefined}
              aria-describedby={replyError ? "reply-body-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3] resize-y"
            />
            {replyError && (
              <p id="reply-body-error" role="alert" className="text-[lightcoral] text-xs font-bold mb-2">
                {replyError}
              </p>
            )}
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full h-[45px] bg-[#2196F3] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#2196F3] disabled:opacity-70 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? "送信中..." : "返信を送信"}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
