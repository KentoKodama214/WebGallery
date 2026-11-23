"use client";

import { useEffect, useState, useSyncExternalStore } from "react";
import {
  ApiError,
  createPasskeyRegistrationOptions,
  deletePasskey,
  getPasskeys,
  registerPasskey,
  renamePasskey,
} from "@/lib/api/client";
import type { PasskeyItem } from "@/lib/api/client";
import {
  PasskeyCancelledError,
  createPasskeyCredential,
  isPasskeySupported,
} from "@/lib/auth/passkey";
import { ModalDialog } from "@/components/ui/ModalDialog";

/** パスキー名の最大文字数（バックエンドの `Consts.PASSKEY_NAME_MAX_LENGTH` と揃える） */
export const PASSKEY_NAME_MAX_LENGTH = 50;

/** ライブラリ等の外部購読はないため、購読関数は何もしない */
const subscribeNoop = () => () => {};

/** 日時を「2026/01/01 09:00」の形式で表示する */
const dateTimeFormatter = new Intl.DateTimeFormat("ja-JP", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
});

function formatDateTime(value: string | null): string {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : dateTimeFormatter.format(date);
}

/** 開いているダイアログ */
type DialogState =
  | { kind: "none" }
  | { kind: "register" }
  | { kind: "rename"; passkey: PasskeyItem }
  | { kind: "delete"; passkey: PasskeyItem };

interface PasskeySectionProps {
  accountId: string;
}

/**
 * アカウント設定画面のパスキー管理セクション
 *
 * パスキーの一覧・登録・名前変更・削除を行う。登録と削除はアクセストークンの漏洩時の被害を限定するため、
 * 現在のパスワードによる本人確認を必要とする（バックエンドで検証する）。
 */
export function PasskeySection({ accountId }: PasskeySectionProps) {
  const [passkeys, setPasskeys] = useState<PasskeyItem[]>([]);
  const [maxCount, setMaxCount] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [notice, setNotice] = useState("");
  const [dialog, setDialog] = useState<DialogState>({ kind: "none" });
  const [dialogPassword, setDialogPassword] = useState("");
  const [dialogName, setDialogName] = useState("");
  const [dialogError, setDialogError] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);

  // パスキー非対応のブラウザでは登録ボタンを無効にする（一覧・削除は行える）。
  // SSR/ハイドレーション時は true を返し、対応ブラウザで不要な注意書きがちらつかないようにする。
  const passkeySupported = useSyncExternalStore(
    subscribeNoop,
    () => isPasskeySupported(),
    () => true
  );

  const [reloadKey, setReloadKey] = useState(0);

  // パスキー一覧の取得（登録・名前変更・削除の後は reloadKey を進めて再取得する）
  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      try {
        const result = await getPasskeys(accountId);
        if (cancelled) return;
        setPasskeys(result.passkeys);
        setMaxCount(result.maxCount);
        setLoadError("");
      } catch (err) {
        if (!cancelled) {
          setLoadError(err instanceof Error ? err.message : "パスキー一覧の取得に失敗しました");
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

  /** パスキー一覧を再取得する */
  const reload = () => setReloadKey((key) => key + 1);

  const openDialog = (next: DialogState) => {
    setDialog(next);
    setDialogPassword("");
    setDialogError("");
    setDialogName(next.kind === "rename" ? next.passkey.passkeyName : "");
    setNotice("");
  };

  const closeDialog = () => {
    if (isProcessing) return;
    setDialog({ kind: "none" });
    setDialogPassword("");
    setDialogName("");
    setDialogError("");
  };

  const reachedLimit = maxCount !== null && passkeys.length >= maxCount;

  /**
   * パスキーの登録（本人確認 → 端末でのパスキー作成 → 登録）
   */
  const handleRegister = async () => {
    const name = dialogName.trim();
    if (!name) {
      setDialogError("パスキー名を入力してください");
      return;
    }
    if (!dialogPassword) {
      setDialogError("現在のパスワードを入力してください");
      return;
    }
    setIsProcessing(true);
    setDialogError("");
    try {
      const options = await createPasskeyRegistrationOptions(accountId, dialogPassword);
      const credential = await createPasskeyCredential(options);
      await registerPasskey(accountId, credential, name);
      setDialog({ kind: "none" });
      setNotice("パスキーを登録しました");
      reload();
    } catch (err) {
      if (err instanceof PasskeyCancelledError) {
        setDialogError("パスキーの作成がキャンセルされました");
      } else {
        setDialogError(err instanceof Error ? err.message : "パスキーの登録に失敗しました");
      }
    } finally {
      setIsProcessing(false);
    }
  };

  /**
   * パスキー名の変更
   */
  const handleRename = async (passkey: PasskeyItem) => {
    const name = dialogName.trim();
    if (!name) {
      setDialogError("パスキー名を入力してください");
      return;
    }
    setIsProcessing(true);
    setDialogError("");
    try {
      await renamePasskey(accountId, passkey.passkeyNo, name);
      setDialog({ kind: "none" });
      setNotice("パスキー名を変更しました");
      reload();
    } catch (err) {
      setDialogError(err instanceof Error ? err.message : "パスキー名の変更に失敗しました");
    } finally {
      setIsProcessing(false);
    }
  };

  /**
   * パスキーの削除（本人確認のうえ）
   */
  const handleDelete = async (passkey: PasskeyItem) => {
    if (!dialogPassword) {
      setDialogError("現在のパスワードを入力してください");
      return;
    }
    setIsProcessing(true);
    setDialogError("");
    try {
      await deletePasskey(accountId, passkey.passkeyNo, dialogPassword);
      setDialog({ kind: "none" });
      setNotice("パスキーを削除しました");
      reload();
    } catch (err) {
      // 他のタブ等で既に削除済みの場合は、一覧を最新化して閉じる
      if (err instanceof ApiError && err.status === 404) {
        setDialog({ kind: "none" });
        reload();
        return;
      }
      setDialogError(err instanceof Error ? err.message : "パスキーの削除に失敗しました");
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <section
      aria-labelledby="passkey-section-title"
      className="mt-6 bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5"
    >
      <h2 id="passkey-section-title" className="text-[#444] text-base font-bold mb-2">
        パスキー
      </h2>
      <p className="text-[#444] text-xs mb-3">
        パスキーを登録すると、パスワードの代わりに端末の指紋・顔認証・PINでログインできます。
        フィッシングサイトでは使えないため、パスワードより安全です。
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
              reload();
            }}
            className="px-3 py-1 text-xs bg-[#1565C0] text-white rounded-sm cursor-pointer"
          >
            再読み込み
          </button>
        </div>
      ) : passkeys.length === 0 ? (
        <p className="text-[#666] text-xs mb-3">登録済みのパスキーはありません</p>
      ) : (
        <ul className="mb-3 divide-y divide-[#eee] border-y border-[#eee]" aria-label="登録済みのパスキー">
          {passkeys.map((passkey) => (
            <li key={passkey.passkeyNo} className="py-2">
              <p className="text-[#444] text-sm font-bold break-all">
                {passkey.passkeyName}
                {passkey.isSynced && (
                  <span className="ml-2 align-middle text-[10px] font-normal text-[#1565C0] border border-[#1565C0] rounded-sm px-1">
                    同期
                  </span>
                )}
              </p>
              <p className="text-[#666] text-[11px]">
                登録：{formatDateTime(passkey.createdAt)} ／ 最終使用：
                {formatDateTime(passkey.lastUsedAt)}
              </p>
              <div className="flex gap-2 mt-1">
                <button
                  type="button"
                  onClick={() => openDialog({ kind: "rename", passkey })}
                  aria-label={`${passkey.passkeyName}の名前を変更`}
                  className="text-xs text-[#1565C0] bg-transparent border-none cursor-pointer p-0 underline"
                >
                  名前を変更
                </button>
                <button
                  type="button"
                  onClick={() => openDialog({ kind: "delete", passkey })}
                  aria-label={`${passkey.passkeyName}を削除`}
                  className="text-xs text-[#C62828] bg-transparent border-none cursor-pointer p-0 underline"
                >
                  削除
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {!passkeySupported && (
        <p className="text-[#666] text-xs mb-2">このブラウザはパスキーに対応していないため、登録できません</p>
      )}
      {reachedLimit && (
        <p className="text-[#666] text-xs mb-2">
          パスキーは{maxCount}件まで登録できます。新しく登録するには、不要なパスキーを削除してください
        </p>
      )}
      <button
        type="button"
        onClick={() => openDialog({ kind: "register" })}
        disabled={isLoading || !!loadError || !passkeySupported || reachedLimit}
        className="w-full h-[44px] bg-[#1565C0] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#1565C0] disabled:opacity-70 disabled:cursor-not-allowed"
      >
        パスキーを登録
      </button>

      {dialog.kind === "register" && (
        <ModalDialog
          label="パスキーの登録"
          initialFocusSelector="#passkey-register-name"
          onClose={closeDialog}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[320px] w-[90%]"
        >
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleRegister();
            }}
          >
            <p className="text-[#444] text-sm mb-3">
              本人確認のため現在のパスワードを入力してください。続けて、端末の画面に従ってパスキーを作成します。
            </p>
            <label htmlFor="passkey-register-name" className="block text-[#444] text-sm mb-1">
              パスキー名
            </label>
            <input
              id="passkey-register-name"
              type="text"
              placeholder="例：MacBookのTouch ID"
              maxLength={PASSKEY_NAME_MAX_LENGTH}
              value={dialogName}
              onChange={(e) => {
                setDialogName(e.target.value);
                setDialogError("");
              }}
              className="block w-full p-[10px] mb-2 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            <label htmlFor="passkey-register-password" className="block text-[#444] text-sm mb-1">
              現在のパスワード
            </label>
            <input
              id="passkey-register-password"
              type="password"
              autoComplete="current-password"
              value={dialogPassword}
              onChange={(e) => {
                setDialogPassword(e.target.value);
                setDialogError("");
              }}
              className="block w-full p-[10px] mb-2 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {dialogError && (
              <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">
                {dialogError}
              </p>
            )}
            <div className="flex gap-3">
              <button
                type="button"
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
                {isProcessing ? "登録しています…" : "登録"}
              </button>
            </div>
          </form>
        </ModalDialog>
      )}

      {dialog.kind === "rename" && (
        <ModalDialog
          label="パスキー名の変更"
          initialFocusSelector="#passkey-rename-name"
          onClose={closeDialog}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[320px] w-[90%]"
        >
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleRename(dialog.passkey);
            }}
          >
            <label htmlFor="passkey-rename-name" className="block text-[#444] text-sm mb-1">
              パスキー名
            </label>
            <input
              id="passkey-rename-name"
              type="text"
              maxLength={PASSKEY_NAME_MAX_LENGTH}
              value={dialogName}
              onChange={(e) => {
                setDialogName(e.target.value);
                setDialogError("");
              }}
              className="block w-full p-[10px] mb-2 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {dialogError && (
              <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">
                {dialogError}
              </p>
            )}
            <div className="flex gap-3">
              <button
                type="button"
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
                変更
              </button>
            </div>
          </form>
        </ModalDialog>
      )}

      {dialog.kind === "delete" && (
        <ModalDialog
          label="パスキーの削除の確認"
          initialFocusSelector="#passkey-delete-password"
          onClose={closeDialog}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[320px] w-[90%]"
        >
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleDelete(dialog.passkey);
            }}
          >
            <p className="text-[#444] text-sm mb-3 break-all">
              「{dialog.passkey.passkeyName}」を削除すると、このパスキーではログインできなくなります。よろしいですか？
            </p>
            <label htmlFor="passkey-delete-password" className="block text-[#444] text-sm mb-1">
              現在のパスワード
            </label>
            <input
              id="passkey-delete-password"
              type="password"
              autoComplete="current-password"
              value={dialogPassword}
              onChange={(e) => {
                setDialogPassword(e.target.value);
                setDialogError("");
              }}
              className="block w-full p-[10px] mb-2 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {dialogError && (
              <p role="alert" className="text-[#C62828] text-xs font-bold mb-2">
                {dialogError}
              </p>
            )}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={closeDialog}
                disabled={isProcessing}
                className="flex-1 h-[40px] bg-gray-300 text-[#444] border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                いいえ
              </button>
              <button
                type="submit"
                disabled={isProcessing}
                className="flex-1 h-[40px] bg-[#C62828] text-white border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                削除
              </button>
            </div>
          </form>
        </ModalDialog>
      )}
    </section>
  );
}
