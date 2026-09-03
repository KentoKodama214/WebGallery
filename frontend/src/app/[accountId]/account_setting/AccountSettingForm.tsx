"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAccount,
  updateAccount,
  deleteAccount,
  getPrefectures,
  ApiError,
} from "@/lib/api/client";
import type { PrefectureGroup } from "@/lib/api/client";
import {
  PASSWORD_PATTERN,
  PASSWORD_ERROR_MESSAGE,
  PASSWORD_PLACEHOLDER,
  clearError,
  isPastDate,
} from "@/lib/validation";
import { ModalDialog } from "@/components/ui/ModalDialog";

/** 再認証（現在のパスワード）に連続して失敗した場合に操作を一時停止する回数 */
const REAUTH_MAX_ATTEMPTS = 3;
/** 再認証の一時停止時間（ミリ秒）。バックエンドのロックとは別のクライアント側の連打抑止 */
const REAUTH_COOLDOWN_MS = 60_000;

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
  const [newPassword, setNewPassword] = useState("");
  // パスワード変更時の本人確認用（現在のパスワード）
  const [currentPassword, setCurrentPassword] = useState("");
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
  const [showDeleteCompleteModal, setShowDeleteCompleteModal] = useState(false);
  // アカウント削除時の本人確認用（現在のパスワード）
  const [deletePassword, setDeletePassword] = useState("");
  const [deleteError, setDeleteError] = useState("");

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [duplicateError, setDuplicateError] = useState("");
  // 再認証（現在のパスワード）の連続失敗によるクライアント側の一時停止
  const reauthFailCountRef = useRef(0);
  const [reauthCooldownUntil, setReauthCooldownUntil] = useState(0);
  const [now, setNow] = useState(() => Date.now());
  const isReauthCoolingDown = now < reauthCooldownUntil;
  // 初期データ取得の失敗（ログインへは飛ばさず画面内で通知し、再試行させる）
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const modalTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const redirectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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

  // 再認証クールダウンの残り時間表示を更新し、期限が来たら解除するためのタイマー
  useEffect(() => {
    if (!isReauthCoolingDown) return;
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [isReauthCoolingDown]);

  // アンマウント時にタイマーを破棄する
  useEffect(() => {
    return () => {
      if (modalTimerRef.current) clearTimeout(modalTimerRef.current);
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
    };
  }, []);

  /**
   * バリデーション
   */
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!accountName.trim()) {
      newErrors.accountName = "アカウント名を入力してください";
    }

    if (newPassword && !PASSWORD_PATTERN.test(newPassword)) {
      newErrors.newPassword = PASSWORD_ERROR_MESSAGE;
    }

    // パスワードを変更する場合は現在のパスワードの入力を必須とする
    if (newPassword && !currentPassword) {
      newErrors.currentPassword = "現在のパスワードを入力してください";
    }

    if (birthdate && !isPastDate(birthdate)) {
      newErrors.birthdate = "過去の日付を入力してください";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const reauthCooldownMessage =
    "現在のパスワードの確認に連続して失敗しました。しばらく待ってから再度お試しください。";

  /**
   * 再認証（現在のパスワード）失敗を記録し、上限に達したらクライアント側で操作を一時停止する
   * （バックエンドのアカウントロックとは別の、連打抑止のための多層防御）
   */
  const recordReauthFailure = () => {
    reauthFailCountRef.current += 1;
    if (reauthFailCountRef.current >= REAUTH_MAX_ATTEMPTS) {
      setReauthCooldownUntil(Date.now() + REAUTH_COOLDOWN_MS);
      setNow(Date.now());
    }
  };

  /** 再認証成功時にクライアント側の失敗カウンタ・クールダウンをリセットする */
  const resetReauthState = () => {
    reauthFailCountRef.current = 0;
    setReauthCooldownUntil(0);
  };

  /**
   * 登録する
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setDuplicateError("");

    if (!validate()) return;

    // パスワード変更（＝再認証が必要）で連続失敗中は、クールダウンが明けるまで送信しない
    if (newPassword && isReauthCoolingDown) {
      setDuplicateError(reauthCooldownMessage);
      return;
    }

    setIsSubmitting(true);

    try {
      const result = await updateAccount(accountId, {
        accountId: formAccountId,
        accountName,
        newPassword,
        currentPassword: newPassword ? currentPassword : "",
        birthdate: birthdate || null,
        sexKbn,
        birthplacePrefectureKbnCode,
        residentPrefectureKbnCode,
        freeMemo,
      });

      // アカウントIDは変更不可（入力欄はdisabled）だが、バックエンド契約に沿って防御的に扱う
      // （この経路ではバックエンドが本人確認を行わずに返すため、再認証カウンタはリセットしない）
      if (result.isDuplicateAccountId) {
        setDuplicateError("このアカウントIDは既に使われています");
        return;
      }

      // ここまで来れば更新成功。再認証の失敗カウンタをリセットする
      resetReauthState();

      // パスワード変更時は再ログインが必要
      if (result.isPasswordChanged) {
        await logout();
        router.push("/login");
        return;
      }

      setShowModal(true);
      modalTimerRef.current = setTimeout(() => setShowModal(false), 5000);
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        // 現在のパスワード不一致・ロック。連打抑止のため失敗を記録する
        recordReauthFailure();
      }
      setDuplicateError(err instanceof Error ? err.message : "更新に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * アカウント削除
   */
  const handleDeleteAccount = async () => {
    // 削除には現在のパスワードによる本人確認が必要
    if (!deletePassword) {
      setDeleteError("現在のパスワードを入力してください");
      return;
    }
    // 再認証に連続失敗中は、クールダウンが明けるまで削除を実行しない
    if (isReauthCoolingDown) {
      setDeleteError(reauthCooldownMessage);
      return;
    }
    setIsDeleting(true);
    setDeleteError("");
    try {
      await deleteAccount(accountId, deletePassword);
      resetReauthState();
      await logout();
      setShowDeleteConfirm(false);
      setShowDeleteCompleteModal(true);
      redirectTimerRef.current = setTimeout(() => {
        router.push("/login");
      }, 3000);
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        // 現在のパスワード不一致・ロック。連打抑止のため失敗を記録する
        recordReauthFailure();
      }
      // 削除失敗（パスワード不一致等）はダイアログを開いたまま通知する
      setDeleteError(err instanceof Error ? err.message : "アカウント削除に失敗しました");
    } finally {
      setIsDeleting(false);
    }
  };

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
          className="px-4 py-2 bg-[#2196F3] text-white rounded-sm cursor-pointer"
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
          className="fixed top-[5px] left-[10px] text-xl text-gray-400 z-[1000] no-underline"
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
            <p className="text-[#444] text-[1.2em] font-bold mt-[10px] mb-[30px] border-b border-[#eee] pb-5">
              Account Setting
            </p>

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
              <p id="account-setting-name-error" className="text-[lightcoral] text-xs font-bold mb-2">{errors.accountName}</p>
            )}

            <label htmlFor="account-setting-current-password" className="block text-[#444] text-sm mb-1 mt-2">
              現在のパスワード
            </label>
            <input
              id="account-setting-current-password"
              type="password"
              autoComplete="current-password"
              value={currentPassword}
              onChange={(e) => {
                setCurrentPassword(e.target.value);
                setErrors((prev) => clearError(prev, "currentPassword"));
              }}
              onBlur={() => {
                if (newPassword && !currentPassword) {
                  setErrors((prev) => ({ ...prev, currentPassword: "現在のパスワードを入力してください" }));
                } else {
                  setErrors((prev) => clearError(prev, "currentPassword"));
                }
              }}
              placeholder="パスワードを変更する場合のみ入力してください"
              aria-invalid={errors.currentPassword ? true : undefined}
              aria-describedby={errors.currentPassword ? "account-setting-current-password-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.currentPassword && (
              <p id="account-setting-current-password-error" className="text-[lightcoral] text-xs font-bold mb-2">{errors.currentPassword}</p>
            )}

            <label htmlFor="account-setting-password" className="block text-[#444] text-sm mb-1 mt-2">新しいパスワード</label>
            <input
              id="account-setting-password"
              type="password"
              autoComplete="new-password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              onBlur={() => {
                if (newPassword && !PASSWORD_PATTERN.test(newPassword)) {
                  setErrors((prev) => ({ ...prev, newPassword: PASSWORD_ERROR_MESSAGE }));
                } else {
                  setErrors((prev) => clearError(prev, "newPassword"));
                }
              }}
              placeholder={PASSWORD_PLACEHOLDER}
              aria-invalid={errors.newPassword ? true : undefined}
              aria-describedby={errors.newPassword ? "account-setting-password-error" : undefined}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.newPassword && (
              <p id="account-setting-password-error" className="text-[lightcoral] text-xs font-bold mb-2">{errors.newPassword}</p>
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
              <p id="account-setting-birthdate-error" className="text-[lightcoral] text-xs font-bold mb-2">{errors.birthdate}</p>
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
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />

            {duplicateError && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{duplicateError}</p>
            )}

            {isReauthCoolingDown && (
              <p role="alert" className="text-[lightcoral] text-xs font-bold mb-2">
                {reauthCooldownMessage}（約{Math.ceil((reauthCooldownUntil - now) / 1000)}秒）
              </p>
            )}

            <button
              type="submit"
              disabled={isSubmitting || (!!newPassword && isReauthCoolingDown)}
              className="w-full h-[50px] bg-[#2196F3] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#2196F3] disabled:opacity-70 disabled:cursor-not-allowed mt-2"
            >
              {isSubmitting ? (
                <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
              ) : (
                <span>登録</span>
              )}
            </button>
          </form>

          <div className="mt-6 bg-white rounded-md shadow-[0px_1px_5px_rgba(0,0,0,0.3)] p-5">
            <p className="text-[#444] text-sm mb-3">アカウントを削除すると、登録した写真やお気に入りはすべて削除され、復旧できなくなります。</p>
            <button
              type="button"
              onClick={() => setShowDeleteConfirm(true)}
              className="w-full h-[50px] bg-[#e53935] text-white border-none rounded-sm cursor-pointer transition-all duration-100 hover:shadow-[0px_1px_3px_#e53935]"
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
            setDeletePassword("");
            setDeleteError("");
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-4">
            登録した写真やお気に入りはすべて削除され、復旧できなくなります。よろしいですか？
          </p>
          <label htmlFor="account-delete-password" className="block text-[#444] text-sm mb-1">
            現在のパスワード
          </label>
          <input
            id="account-delete-password"
            type="password"
            autoComplete="current-password"
            value={deletePassword}
            onChange={(e) => {
              setDeletePassword(e.target.value);
              setDeleteError("");
            }}
            className="block w-full p-[10px] mb-2 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
          />
          {deleteError && (
            <p role="alert" className="text-[lightcoral] text-xs font-bold mb-2">{deleteError}</p>
          )}
          <div className="flex gap-3">
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => {
                setShowDeleteConfirm(false);
                setDeletePassword("");
                setDeleteError("");
              }}
              disabled={isDeleting}
              className="flex-1 h-[40px] bg-gray-300 text-[#444] border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              いいえ
            </button>
            <button
              type="button"
              onClick={handleDeleteAccount}
              disabled={isDeleting || isReauthCoolingDown}
              className="flex-1 h-[40px] bg-[#e53935] text-white border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {isDeleting ? (
                <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
              ) : (
                "はい"
              )}
            </button>
          </div>
        </ModalDialog>
      )}

      {showDeleteCompleteModal && (
        <ModalDialog
          label="アカウント削除完了"
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]"
        >
          <p className="text-[#444] text-center">アカウントを削除しました</p>
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
          <p className="text-[#444] text-center">アカウントを登録しました</p>
        </ModalDialog>
      )}
    </div>
  );
}
