"use client";

import { useState, useSyncExternalStore } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getRecoveryOptions } from "@/lib/api/client";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  PasskeyCancelledError,
  createPasskeyCredential,
  isPasskeySupported,
} from "@/lib/auth/passkey";
import { PASSKEY_NAME_MAX_LENGTH, clearError } from "@/lib/validation";

/** ライブラリ等の外部購読はないため、購読関数は何もしない */
const subscribeNoop = () => () => {};

/** 復旧時に登録するパスキーの名前の初期値 */
const DEFAULT_PASSKEY_NAME = "復旧したパスキー";

/**
 * リカバリーコードによる復旧フォームコンポーネント
 *
 * アカウントIDとリカバリーコードで本人確認し、この端末に新しいパスキーを作成・登録してログインする。
 * 入力したリカバリーコードは本人確認の時点で使用済みになる（パスキーの作成をキャンセルした場合も戻らない）
 */
export function RecoveryForm() {
  const router = useRouter();
  const { loginWithRecoveryCode } = useAuth();

  const [accountId, setAccountId] = useState("");
  const [recoveryCode, setRecoveryCode] = useState("");
  const [passkeyName, setPasskeyName] = useState(DEFAULT_PASSKEY_NAME);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // パスキー非対応のブラウザでは復旧（新しいパスキーの作成）ができない。
  // SSR/ハイドレーション時は true を返し、対応ブラウザで注意書きがちらつかないようにする。
  const passkeySupported = useSyncExternalStore(
    subscribeNoop,
    () => isPasskeySupported(),
    () => true
  );

  /**
   * バリデーション
   */
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!accountId.trim()) {
      newErrors.accountId = "アカウントIDを入力してください";
    }
    if (!recoveryCode.trim()) {
      newErrors.recoveryCode = "リカバリーコードを入力してください";
    }
    if (!passkeyName.trim()) {
      newErrors.passkeyName = "パスキー名を入力してください";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 復旧する（リカバリーコードで本人確認 → 端末で新しいパスキーを作成 → 登録してログイン）
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;
    setSubmitError("");
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      const options = await getRecoveryOptions(accountId.trim(), recoveryCode);
      // リカバリーコードはこの時点で使用済みになる。以降で失敗した場合は別のコードでやり直す
      setRecoveryCode("");
      const credential = await createPasskeyCredential(options);
      const loggedInUser = await loginWithRecoveryCode(credential, passkeyName.trim());
      router.push(`/${loggedInUser.accountId}/account_setting`);
    } catch (err) {
      if (err instanceof PasskeyCancelledError) {
        setSubmitError(
          "パスキーの作成がキャンセルされました。入力したリカバリーコードは使用済みのため、別のコードでやり直してください"
        );
      } else {
        setSubmitError(err instanceof Error ? err.message : "復旧に失敗しました");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[whitesmoke] font-['Open_Sans',sans-serif]">
      <header>
        <Link
          href="/login"
          className="fixed top-[5px] left-[10px] text-xl text-gray-600 z-[1000] no-underline"
        >
          &larr; back
        </Link>
      </header>

      <div className="flex justify-center pt-12 pb-16 px-4">
        <div className="w-full max-w-[320px]">
          <form
            onSubmit={handleSubmit}
            className="bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5"
          >
            <h1 className="text-[#444] text-[1.2em] font-bold mt-[10px] mb-[20px] border-b border-[#eee] pb-5">
              リカバリーコードで復旧
            </h1>

            <p className="text-[#444] text-xs mb-4">
              パスキーをすべて失った場合に、アカウント登録時（または再発行時）に保管したリカバリーコードを使って、この端末に新しいパスキーを登録します。
              入力したリカバリーコードは使用済みになります。
            </p>

            <label htmlFor="recovery-account-id" className="block text-[#444] text-sm mb-1">アカウントID</label>
            <input
              id="recovery-account-id"
              type="text"
              autoComplete="username"
              value={accountId}
              onChange={(e) => {
                setAccountId(e.target.value);
                setErrors((prev) => clearError(prev, "accountId"));
              }}
              aria-invalid={errors.accountId ? true : undefined}
              aria-describedby={errors.accountId ? "recovery-account-id-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.accountId && (
              <p id="recovery-account-id-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.accountId}</p>
            )}

            <label htmlFor="recovery-code" className="block text-[#444] text-sm mb-1 mt-2">リカバリーコード</label>
            <input
              id="recovery-code"
              type="text"
              autoComplete="one-time-code"
              autoCapitalize="characters"
              spellCheck={false}
              value={recoveryCode}
              onChange={(e) => {
                setRecoveryCode(e.target.value);
                setErrors((prev) => clearError(prev, "recoveryCode"));
              }}
              placeholder="XXXX-XXXX-XXXX-XXXX"
              aria-invalid={errors.recoveryCode ? true : undefined}
              aria-describedby={errors.recoveryCode ? "recovery-code-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] font-mono outline-none focus:border-[#2196F3]"
            />
            {errors.recoveryCode && (
              <p id="recovery-code-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.recoveryCode}</p>
            )}

            <label htmlFor="recovery-passkey-name" className="block text-[#444] text-sm mb-1 mt-2">新しいパスキーの名前</label>
            <input
              id="recovery-passkey-name"
              type="text"
              maxLength={PASSKEY_NAME_MAX_LENGTH}
              value={passkeyName}
              onChange={(e) => {
                setPasskeyName(e.target.value);
                setErrors((prev) => clearError(prev, "passkeyName"));
              }}
              aria-invalid={errors.passkeyName ? true : undefined}
              aria-describedby={errors.passkeyName ? "recovery-passkey-name-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.passkeyName && (
              <p id="recovery-passkey-name-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.passkeyName}</p>
            )}

            {!passkeySupported && (
              <p className="text-[#C62828] text-xs font-bold mb-2">
                このブラウザはパスキーに対応していないため、復旧できません
              </p>
            )}

            {submitError && (
              <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">{submitError}</p>
            )}

            <button
              type="submit"
              disabled={isSubmitting || !passkeySupported}
              className="w-full h-[50px] bg-[#1565C0] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#1565C0] disabled:opacity-70 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? "復旧しています…" : "新しいパスキーを登録してログイン"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
