"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { registerInquiry } from "@/lib/api/client";
import {
  INQUIRY_SUBJECT_MAX_LENGTH,
  INQUIRY_BODY_MAX_LENGTH,
  clearError,
} from "@/lib/validation";
import { ModalDialog } from "@/components/ui/ModalDialog";

/**
 * お問い合わせ投稿フォームコンポーネント
 */
export function InquiryForm() {
  const router = useRouter();

  const [subject, setSubject] = useState("");
  const [body, setBody] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showModal, setShowModal] = useState(false);

  /**
   * バリデーション
   */
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!subject.trim()) {
      newErrors.subject = "件名を入力してください";
    } else if (subject.length > INQUIRY_SUBJECT_MAX_LENGTH) {
      newErrors.subject = `件名は${INQUIRY_SUBJECT_MAX_LENGTH}文字以内で入力してください`;
    }

    if (!body.trim()) {
      newErrors.body = "本文を入力してください";
    } else if (body.length > INQUIRY_BODY_MAX_LENGTH) {
      newErrors.body = `本文は${INQUIRY_BODY_MAX_LENGTH}文字以内で入力してください`;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 送信する
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError("");

    if (!validate()) return;

    setIsSubmitting(true);
    try {
      await registerInquiry({ subject, body });
      setShowModal(true);
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "お問い合わせの登録に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[whitesmoke] font-['Open_Sans',sans-serif]">
      <div className="flex justify-center pt-12 pb-16 px-4">
        <div className="w-full max-w-[480px]">
          <Link
            href="/inquiry/list"
            className="text-[#2196F3] hover:underline text-sm block mb-4"
          >
            &larr; お問い合わせ一覧へ戻る
          </Link>
          <form
            onSubmit={handleSubmit}
            className="bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5"
          >
            <p className="text-[#444] text-[1.2em] font-bold mt-[10px] mb-[30px] border-b border-[#eee] pb-5">
              お問い合わせ
            </p>

            <label htmlFor="inquiry-subject" className="block text-[#444] text-sm mb-1">
              件名
            </label>
            <input
              id="inquiry-subject"
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              onBlur={() => {
                if (!subject.trim()) {
                  setErrors((prev) => ({ ...prev, subject: "件名を入力してください" }));
                } else if (subject.length > INQUIRY_SUBJECT_MAX_LENGTH) {
                  setErrors((prev) => ({
                    ...prev,
                    subject: `件名は${INQUIRY_SUBJECT_MAX_LENGTH}文字以内で入力してください`,
                  }));
                } else {
                  setErrors((prev) => clearError(prev, "subject"));
                }
              }}
              maxLength={INQUIRY_SUBJECT_MAX_LENGTH}
              aria-invalid={errors.subject ? true : undefined}
              aria-describedby={errors.subject ? "inquiry-subject-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.subject && (
              <p id="inquiry-subject-error" className="text-[lightcoral] text-xs font-bold mb-2">
                {errors.subject}
              </p>
            )}

            <label htmlFor="inquiry-body" className="block text-[#444] text-sm mb-1 mt-2">
              本文
            </label>
            <textarea
              id="inquiry-body"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              onBlur={() => {
                if (!body.trim()) {
                  setErrors((prev) => ({ ...prev, body: "本文を入力してください" }));
                } else if (body.length > INQUIRY_BODY_MAX_LENGTH) {
                  setErrors((prev) => ({
                    ...prev,
                    body: `本文は${INQUIRY_BODY_MAX_LENGTH}文字以内で入力してください`,
                  }));
                } else {
                  setErrors((prev) => clearError(prev, "body"));
                }
              }}
              maxLength={INQUIRY_BODY_MAX_LENGTH}
              rows={8}
              aria-invalid={errors.body ? true : undefined}
              aria-describedby={errors.body ? "inquiry-body-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3] resize-y"
            />
            {errors.body && (
              <p id="inquiry-body-error" className="text-[lightcoral] text-xs font-bold mb-2">
                {errors.body}
              </p>
            )}

            {submitError && (
              <p role="alert" className="text-[lightcoral] text-xs font-bold mb-2">
                {submitError}
              </p>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full h-[50px] bg-[#2196F3] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#2196F3] disabled:opacity-70 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? (
                <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
              ) : (
                <span>送信</span>
              )}
            </button>
          </form>
        </div>
      </div>

      {showModal && (
        <ModalDialog
          label="お問い合わせ登録完了"
          onClose={() => {
            setShowModal(false);
            router.push("/inquiry/list");
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-4">お問い合わせを受け付けました</p>
          <button
            type="button"
            data-dialog-initial-focus
            onClick={() => {
              setShowModal(false);
              router.push("/inquiry/list");
            }}
            className="w-full h-[40px] bg-[#2196F3] text-white border-none rounded-sm cursor-pointer"
          >
            お問い合わせ一覧へ
          </button>
        </ModalDialog>
      )}
    </div>
  );
}
