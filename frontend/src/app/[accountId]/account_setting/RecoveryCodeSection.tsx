"use client";

import { useEffect, useState } from "react";
import { getRecoveryCodeStatus, regenerateRecoveryCodes } from "@/lib/api/client";
import type { RecoveryCodeStatus } from "@/lib/api/client";
import { PasskeyCancelledError } from "@/lib/auth/passkey";
import { reauthenticateWithPasskey } from "@/lib/auth/reauthenticate";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { RecoveryCodeList } from "@/components/auth/RecoveryCodeList";

/** 開いているダイアログ */
type DialogState = { kind: "none" } | { kind: "confirm" } | { kind: "codes"; codes: string[] };

interface RecoveryCodeSectionProps {
  accountId: string;
}

/**
 * アカウント設定画面のリカバリーコード管理セクション
 *
 * 未使用のコードの残数を表示し、再発行（パスキーによる本人確認のうえ）を行う。
 * コード自体はサーバーに保存されていないため、再発行の直後にしか表示できない。
 */
export function RecoveryCodeSection({ accountId }: RecoveryCodeSectionProps) {
  const [status, setStatus] = useState<RecoveryCodeStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [notice, setNotice] = useState("");
  const [dialog, setDialog] = useState<DialogState>({ kind: "none" });
  const [dialogError, setDialogError] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  // 残数の取得（再発行の後は reloadKey を進めて再取得する）
  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      try {
        const result = await getRecoveryCodeStatus(accountId);
        if (cancelled) return;
        setStatus(result);
        setLoadError("");
      } catch (err) {
        if (!cancelled) {
          setLoadError(
            err instanceof Error ? err.message : "リカバリーコードの状態の取得に失敗しました"
          );
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };
    load();
    return () => {
      cancelled = true;
    };
  }, [accountId, reloadKey]);

  const closeDialog = () => {
    if (isProcessing) return;
    // 発行したコードを表示中に閉じた場合は、残数を最新化する
    if (dialog.kind === "codes") {
      setNotice("リカバリーコードを再発行しました");
      setReloadKey((key) => key + 1);
    }
    setDialog({ kind: "none" });
    setDialogError("");
  };

  /**
   * リカバリーコードの再発行（パスキーで本人確認 → 再発行）
   */
  const handleRegenerate = async () => {
    setIsProcessing(true);
    setDialogError("");
    try {
      const credential = await reauthenticateWithPasskey(accountId);
      const codes = await regenerateRecoveryCodes(accountId, credential);
      setDialog({ kind: "codes", codes });
    } catch (err) {
      // 利用者自身によるパスキー操作のキャンセルは失敗として表示しない
      if (!(err instanceof PasskeyCancelledError)) {
        setDialogError(
          err instanceof Error ? err.message : "リカバリーコードの再発行に失敗しました"
        );
      }
    } finally {
      setIsProcessing(false);
    }
  };

  const isRunningLow = status !== null && status.remainingCount <= 3;

  return (
    <section
      aria-labelledby="recovery-code-section-title"
      className="mt-6 bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5"
    >
      <h2 id="recovery-code-section-title" className="text-[#444] text-base font-bold mb-2">
        リカバリーコード
      </h2>
      <p className="text-[#444] text-xs mb-3">
        パスキーをすべて失ったときに、ログイン画面の「リカバリーコードで復旧」から新しいパスキーを登録するために使います。各コードは1回だけ使えます。
      </p>

      {notice && (
        <p role="status" className="text-[#1565C0] text-xs font-bold mb-2">
          {notice}
        </p>
      )}

      {isLoading ? (
        <div className="flex justify-center py-2">
          <span className="inline-block w-5 h-5 border-[3px] border-[#2196F3] border-t-transparent rounded-full animate-spin" />
        </div>
      ) : loadError ? (
        <div className="mb-3">
          <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">
            {loadError}
          </p>
          <button
            type="button"
            onClick={() => {
              setIsLoading(true);
              setLoadError("");
              setReloadKey((key) => key + 1);
            }}
            className="px-3 py-1 text-xs bg-[#1565C0] text-white rounded-sm cursor-pointer"
          >
            再読み込み
          </button>
        </div>
      ) : (
        status && (
          <p className="text-[#444] text-sm mb-3">
            未使用のコード：
            <span className="font-bold">
              {status.remainingCount} / {status.issueCount}
            </span>
            {isRunningLow && (
              <span className="block text-[#C62828] text-xs font-bold mt-1">
                残りが少なくなっています。再発行してください
              </span>
            )}
          </p>
        )
      )}

      <button
        type="button"
        onClick={() => {
          setNotice("");
          setDialogError("");
          setDialog({ kind: "confirm" });
        }}
        disabled={isLoading || !!loadError}
        className="w-full h-[44px] bg-[#1565C0] text-white border-none rounded-sm cursor-pointer transition-shadow duration-100 hover:shadow-[0px_1px_3px_#1565C0] disabled:opacity-70 disabled:cursor-not-allowed"
      >
        リカバリーコードを再発行
      </button>

      {dialog.kind === "confirm" && (
        <ModalDialog
          label="リカバリーコードの再発行の確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={closeDialog}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[320px] w-[90%]"
        >
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleRegenerate();
            }}
          >
            <p className="text-[#444] text-sm mb-2">
              再発行すると、今お持ちの未使用のリカバリーコードはすべて使えなくなります。よろしいですか？
            </p>
            <p className="text-[#666] text-xs mb-3">
              「再発行」を押すと、本人確認のためにパスキー（生体認証・PIN）を求められます。
            </p>
            {dialogError && (
              <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">
                {dialogError}
              </p>
            )}
            <div className="flex gap-3">
              <button
                type="button"
                data-dialog-initial-focus
                onClick={closeDialog}
                disabled={isProcessing}
                className="flex-1 h-[40px] bg-gray-300 text-[#444] border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                キャンセル
              </button>
              <button
                type="submit"
                disabled={isProcessing}
                className="flex-1 h-[40px] bg-[#1565C0] text-white border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isProcessing ? "再発行しています…" : "再発行"}
              </button>
            </div>
          </form>
        </ModalDialog>
      )}

      {dialog.kind === "codes" && (
        <ModalDialog
          label="新しいリカバリーコード"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={closeDialog}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[360px] w-[90%]"
        >
          <p className="text-[#444] text-sm font-bold mb-2">
            新しいリカバリーコードを安全な場所に保管してください
          </p>
          <p className="text-[#666] text-xs mb-3">
            この画面を閉じると、コードは二度と表示できません。
          </p>
          <RecoveryCodeList codes={dialog.codes} accountId={accountId} />
          <button
            type="button"
            data-dialog-initial-focus
            onClick={closeDialog}
            className="w-full h-[40px] mt-2 bg-[#1565C0] text-white border-none rounded-sm cursor-pointer"
          >
            保管しました
          </button>
        </ModalDialog>
      )}
    </section>
  );
}
