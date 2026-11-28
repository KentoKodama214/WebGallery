"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  consumeNextLogoutDestination,
  setNextLogoutDestination,
} from "@/lib/auth/loginRedirect";
import {
  getAccount,
  updateAccount,
  deleteAccount,
  getPrefectures,
} from "@/lib/api/client";
import type { PrefectureGroup } from "@/lib/api/client";
import {
  ACCOUNT_NAME_MAX_LENGTH,
  FREE_MEMO_MAX_LENGTH,
  clearError,
  isPastDate,
} from "@/lib/validation";
import { PasskeyCancelledError } from "@/lib/auth/passkey";
import { reauthenticateWithPasskey } from "@/lib/auth/reauthenticate";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { PasskeySection } from "./PasskeySection";
import { RecoveryCodeSection } from "./RecoveryCodeSection";

interface AccountSettingFormProps {
  accountId: string;
}

/**
 * アカウント設定フォームコンポーネント
 */
export function AccountSettingForm({ accountId }: AccountSettingFormProps) {
  const router = useRouter();
  const { user, logout, isLoading: authLoading } = useAuth();

  const isAuthenticated = !!user;
  const isOwner = isAuthenticated && user.accountId === accountId;

  const [formAccountId, setFormAccountId] = useState(accountId);
  const [accountName, setAccountName] = useState("");
  const [birthdate, setBirthdate] = useState("");
  const [sexKbn, setSexKbn] = useState("none");
  const [birthplacePrefectureKbnCode, setBirthplacePrefectureKbnCode] = useState("none");
  const [residentPrefectureKbnCode, setResidentPrefectureKbnCode] = useState("none");
  const [freeMemo, setFreeMemo] = useState("");

  const [prefectureGroups, setPrefectureGroups] = useState<PrefectureGroup[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  // 削除成功後は logout で user が消え isOwner が false になるが、その一瞬に
  // 「権限がありません」画面を見せないためのフラグ（/login へ退避するまでの繋ぎ表示）
  const [deleteSucceeded, setDeleteSucceeded] = useState(false);
  const [deleteError, setDeleteErrorState] = useState("");
  // deleteError を表示する要素の React key。エラーメッセージ本体をセットする箇所でのみ
  // インクリメントする（クリア時は変えない）。同一文言のエラーが連続しても要素を
  // 再マウントさせ、role="alert" がスクリーンリーダーへ確実に再通知されるようにする
  // （React は同一文字列の再セットではテキストノードを更新せず、変化なしとみなすため）。
  const [deleteErrorSeq, setDeleteErrorSeq] = useState(0);
  const setDeleteError = (message: string) => {
    setDeleteErrorState(message);
    if (message) setDeleteErrorSeq((seq) => seq + 1);
  };

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [duplicateError, setDuplicateErrorState] = useState("");
  // duplicateError 版。理由は deleteErrorSeq と同じ
  const [duplicateErrorSeq, setDuplicateErrorSeq] = useState(0);
  const setDuplicateError = (message: string) => {
    setDuplicateErrorState(message);
    if (message) setDuplicateErrorSeq((seq) => seq + 1);
  };
  // 初期データ取得の失敗（ログインへは飛ばさず画面内で通知し、再試行させる）
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const modalTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  /*
   * 初期表示（本人のページの場合のみデータを取得する。他人のページはガードで弾く）
   */
  useEffect(() => {
    if (authLoading || !isOwner) return;

    let cancelled = false;
    const load = async () => {
      try {
        const [accountData, prefectureData] = await Promise.all([
          getAccount(accountId),
          getPrefectures(),
        ]);
        if (cancelled) return;
        setFormAccountId(accountData.accountId);
        setAccountName(accountData.accountName);
        setBirthdate(accountData.birthdate || "");
        setSexKbn(accountData.sexKbn || "none");
        setBirthplacePrefectureKbnCode(accountData.birthplacePrefectureKbnCode || "none");
        setResidentPrefectureKbnCode(accountData.residentPrefectureKbnCode || "none");
        setFreeMemo(accountData.freeMemo || "");
        setPrefectureGroups(prefectureData);
      } catch (err) {
        // 一時的なサーバーエラーでログインへ飛ばさず、画面内で再試行させる。
        // 未ログインの場合は別のeffectが/loginへ誘導する。
        if (!cancelled) {
          setLoadError(
            err instanceof Error ? err.message : "情報の取得に失敗しました"
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
    // router は再取得のトリガーではないため依存に含めない
  }, [authLoading, isOwner, accountId, reloadKey]);

  // 未ログイン時の /login への誘導は、このコンポーネントをラップする <AuthGuard> が行う。

  // アンマウント時にタイマーを破棄する
  useEffect(() => {
    return () => {
      if (modalTimerRef.current) clearTimeout(modalTimerRef.current);
    };
  }, []);

  /**
   * バリデーション
   */
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!accountName.trim()) {
      newErrors.accountName = "アカウント名を入力してください";
    } else if (accountName.length > ACCOUNT_NAME_MAX_LENGTH) {
      newErrors.accountName = `アカウント名は${ACCOUNT_NAME_MAX_LENGTH}文字以内で入力してください`;
    }

    if (freeMemo.length > FREE_MEMO_MAX_LENGTH) {
      newErrors.freeMemo = `メモは${FREE_MEMO_MAX_LENGTH}文字以内で入力してください`;
    }

    if (birthdate && !isPastDate(birthdate)) {
      newErrors.birthdate = "過去の日付を入力してください";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * 登録する
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setDuplicateError("");

    if (!validate()) return;

    setIsSubmitting(true);

    try {
      const result = await updateAccount(accountId, {
        accountId: formAccountId,
        accountName,
        birthdate: birthdate || null,
        sexKbn,
        birthplacePrefectureKbnCode,
        residentPrefectureKbnCode,
        freeMemo,
      });

      // アカウントIDは変更不可（入力欄はdisabled）だが、バックエンド契約に沿って防御的に扱う
      if (result.isDuplicateAccountId) {
        setDuplicateError("このアカウントIDは既に使われています");
        return;
      }

      setShowModal(true);
      modalTimerRef.current = setTimeout(() => setShowModal(false), 5000);
    } catch (err) {
      setDuplicateError(err instanceof Error ? err.message : "更新に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * アカウント削除
   *
   * 削除の直前に、端末のパスキーで本人確認（生体認証・PIN）を行う
   */
  const handleDeleteAccount = async () => {
    setIsDeleting(true);
    setDeleteError("");

    let deleted = false;
    try {
      const credential = await reauthenticateWithPasskey(accountId);
      await deleteAccount(accountId, credential);
      deleted = true;
    } catch (err) {
      // 利用者自身によるパスキー操作のキャンセルは失敗として表示しない
      if (!(err instanceof PasskeyCancelledError)) {
        // 削除失敗（本人確認の失敗等）はダイアログを開いたまま通知する
        setDeleteError(err instanceof Error ? err.message : "アカウント削除に失敗しました");
      }
    }

    if (deleted) {
      // 削除は確定済み。以降の logout の通信失敗で /login への遷移を止めない。
      // ローカルのトークン破棄は logout 内で行われ、サーバー側のリフレッシュトークンは削除済み。
      //
      // アカウント削除後は退避元（本人だけが開けるアカウント設定ページ）へ戻す
      // redirect クエリを付けさせない。付くと、同じタブで別アカウントにログインし直した
      // 際にそのページが開き「この操作を行う権限がありません」と表示されてしまうため、
      // AuthGuard の退避先を削除完了ページ（/login?deleted=1）に固定する。
      setShowDeleteConfirm(false);
      setDeleteSucceeded(true);
      setNextLogoutDestination("/login?deleted=1");
      try {
        await logout();
        // logout 成功時は user が消え、AuthGuard が上記の退避先へ遷移させる
      } catch {
        // logout の通信に失敗すると AuthGuard の退避が働かないため、明示的に遷移する
        consumeNextLogoutDestination();
        router.replace("/login?deleted=1");
      }
      return;
    }

    setIsDeleting(false);
  };

  // アカウント削除に成功した直後（logout で user が消えたが /login へ退避する前）。
  // 「権限がありません」ではなく削除完了の繋ぎ表示を出す。
  if (deleteSucceeded) {
    return (
      <div className="min-h-screen bg-[whitesmoke] flex flex-col items-center justify-center gap-4">
        <div className="inline-block w-8 h-8 border-4 border-[#2196F3] border-t-transparent rounded-full animate-spin" />
        <p className="text-[#444]">アカウントを削除しました</p>
      </div>
    );
  }

  // 認証状態の確定と未ログイン時の /login 誘導は <AuthGuard> が担う。
  // ここでは本人ページの初期データ取得中のみスピナーを表示する
  if (authLoading || (isOwner && isLoading)) {
    return (
      <div className="min-h-screen bg-[whitesmoke] flex items-center justify-center">
        <div className="inline-block w-8 h-8 border-4 border-[#2196F3] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  // ログイン済みだが他人のアカウント設定を開こうとした場合
  if (!isOwner) {
    return (
      <div className="min-h-screen bg-[whitesmoke] flex items-center justify-center">
        <p className="text-red-500">この操作を行う権限がありません</p>
      </div>
    );
  }

  // 初期データ取得に失敗した場合は画面内で通知し、再試行させる
  if (loadError) {
    return (
      <div className="min-h-screen bg-[whitesmoke] flex flex-col items-center justify-center gap-4">
        <p className="text-red-500">{loadError}</p>
        <button
          type="button"
          onClick={() => {
            setLoadError("");
            setIsLoading(true);
            setReloadKey((k) => k + 1);
          }}
          className="px-4 py-2 bg-[#1565C0] text-white rounded-sm cursor-pointer"
        >
          再読み込み
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[whitesmoke] font-['Open_Sans',sans-serif]">
      <header>
        <Link
          href={`/photo/${accountId}/photo_list`}
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
            <h1 className="text-[#444] text-[1.2em] font-bold mt-[10px] mb-[30px] border-b border-[#eee] pb-5">
              アカウント設定
            </h1>

            <label htmlFor="account-setting-id" className="block text-[#444] text-sm mb-1">アカウントID</label>
            <input
              id="account-setting-id"
              type="text"
              value={formAccountId}
              disabled
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] bg-gray-100 cursor-not-allowed"
            />

            <label htmlFor="account-setting-name" className="block text-[#444] text-sm mb-1">アカウント名</label>
            <input
              id="account-setting-name"
              type="text"
              value={accountName}
              onChange={(e) => setAccountName(e.target.value)}
              maxLength={ACCOUNT_NAME_MAX_LENGTH}
              onBlur={() => {
                if (!accountName.trim()) {
                  setErrors((prev) => ({ ...prev, accountName: "アカウント名を入力してください" }));
                } else {
                  setErrors((prev) => clearError(prev, "accountName"));
                }
              }}
              aria-invalid={errors.accountName ? true : undefined}
              aria-describedby={errors.accountName ? "account-setting-name-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.accountName && (
              <p id="account-setting-name-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.accountName}</p>
            )}

            <label htmlFor="account-setting-birthdate" className="block text-[#444] text-sm mb-1 mt-2">生年月日</label>
            <input
              id="account-setting-birthdate"
              type="date"
              value={birthdate}
              onChange={(e) => {
                const value = e.target.value;
                setBirthdate(value);
                if (value && !isPastDate(value)) {
                  setErrors((prev) => ({ ...prev, birthdate: "過去の日付を入力してください" }));
                } else {
                  setErrors((prev) => clearError(prev, "birthdate"));
                }
              }}
              aria-invalid={errors.birthdate ? true : undefined}
              aria-describedby={errors.birthdate ? "account-setting-birthdate-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.birthdate && (
              <p id="account-setting-birthdate-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.birthdate}</p>
            )}

            <label htmlFor="account-setting-sex" className="block text-[#444] text-sm mb-1 mt-2">性別</label>
            <select
              id="account-setting-sex"
              value={sexKbn}
              onChange={(e) => setSexKbn(e.target.value)}
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            >
              <option value="none"></option>
              <option value="man">男性</option>
              <option value="woman">女性</option>
            </select>

            <label htmlFor="account-setting-birthplace" className="block text-[#444] text-sm mb-1">出身地</label>
            <select
              id="account-setting-birthplace"
              value={birthplacePrefectureKbnCode}
              onChange={(e) => setBirthplacePrefectureKbnCode(e.target.value)}
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            >
              <option value="none"></option>
              {prefectureGroups.map((group) => (
                <optgroup key={group.groupName} label={group.groupName}>
                  {group.prefectures.map((pref) => (
                    <option key={pref.kbnCode} value={pref.kbnCode}>
                      {pref.kbnJapaneseName}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>

            <label htmlFor="account-setting-resident" className="block text-[#444] text-sm mb-1">居住地</label>
            <select
              id="account-setting-resident"
              value={residentPrefectureKbnCode}
              onChange={(e) => setResidentPrefectureKbnCode(e.target.value)}
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            >
              <option value="none"></option>
              {prefectureGroups.map((group) => (
                <optgroup key={group.groupName} label={group.groupName}>
                  {group.prefectures.map((pref) => (
                    <option key={pref.kbnCode} value={pref.kbnCode}>
                      {pref.kbnJapaneseName}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>

            <label htmlFor="account-setting-free-memo" className="block text-[#444] text-sm mb-1">メモ</label>
            <input
              id="account-setting-free-memo"
              type="text"
              value={freeMemo}
              onChange={(e) => setFreeMemo(e.target.value)}
              maxLength={FREE_MEMO_MAX_LENGTH}
              aria-invalid={errors.freeMemo ? true : undefined}
              aria-describedby={errors.freeMemo ? "account-setting-free-memo-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.freeMemo && (
              <p id="account-setting-free-memo-error" className="text-[#C62828] text-xs font-bold mb-2">{errors.freeMemo}</p>
            )}

            {duplicateError && (
              <p key={duplicateErrorSeq} role="alert" className="text-[#C62828] text-xs font-bold mb-2">{duplicateError}</p>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full h-[50px] bg-[#1565C0] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#1565C0] disabled:opacity-70 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? (
                <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
              ) : (
                <span>登録</span>
              )}
            </button>
          </form>

          <PasskeySection accountId={accountId} />

          <RecoveryCodeSection accountId={accountId} />

          <div className="mt-6 bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5">
            <p className="text-[#444] text-sm mb-3">アカウントを削除すると、登録した写真やお気に入りはすべて削除され、復旧できなくなります。</p>
            <button
              type="button"
              onClick={() => setShowDeleteConfirm(true)}
              className="w-full h-[50px] bg-[#C62828] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#C62828]"
            >
              アカウント削除
            </button>
          </div>
        </div>
      </div>

      {showDeleteConfirm && (
        <ModalDialog
          label="アカウント削除の確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => {
            if (isDeleting) return;
            setShowDeleteConfirm(false);
            setDeleteError("");
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-2">
            登録した写真やお気に入りはすべて削除され、復旧できなくなります。よろしいですか？
          </p>
          <p className="text-[#666] text-xs text-center mb-4">
            「はい」を押すと、本人確認のためにパスキー（生体認証・PIN）を求められます。
          </p>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleDeleteAccount();
            }}
          >
            {deleteError && (
              <p key={deleteErrorSeq} role="alert" className="text-[#C62828] text-xs font-bold mb-2">{deleteError}</p>
            )}
            <div className="flex gap-3">
              <button
                type="button"
                data-dialog-initial-focus
                onClick={() => {
                  setShowDeleteConfirm(false);
                  setDeleteError("");
                }}
                disabled={isDeleting}
                className="flex-1 h-[40px] bg-gray-300 text-[#444] border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                いいえ
              </button>
              <button
                type="submit"
                disabled={isDeleting}
                className="flex-1 h-[40px] bg-[#C62828] text-white border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isDeleting ? (
                  <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
                ) : (
                  "はい"
                )}
              </button>
            </div>
          </form>
        </ModalDialog>
      )}

      {showModal && (
        <ModalDialog
          label="アカウント更新完了"
          onClose={() => setShowModal(false)}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]"
        >
          <button
            type="button"
            aria-label="閉じる"
            onClick={() => setShowModal(false)}
            className="absolute top-2 right-3 text-xl text-gray-400 bg-transparent border-none cursor-pointer"
          >
            &times;
          </button>
          <p className="text-[#444] text-center">アカウント情報を更新しました</p>
        </ModalDialog>
      )}
    </div>
  );
}
