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
} from "@/lib/api/client";
import type { PrefectureGroup } from "@/lib/api/client";
import { PASSWORD_PATTERN, clearError, isPastDate } from "@/lib/validation";

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

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [duplicateError, setDuplicateError] = useState("");
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

  // 未ログインの場合はログインページへ誘導する
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [authLoading, isAuthenticated, router]);

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
      newErrors.newPassword = "半角英数字で8文字以上で入力してください";
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
        newPassword,
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

      // パスワード変更時は再ログインが必要
      if (result.isPasswordChanged) {
        await logout();
        router.push("/login");
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
   */
  const handleDeleteAccount = async () => {
    setIsDeleting(true);
    try {
      await deleteAccount(accountId);
      await logout();
      setShowDeleteConfirm(false);
      setShowDeleteCompleteModal(true);
      redirectTimerRef.current = setTimeout(() => {
        router.push("/login");
      }, 3000);
    } catch (err) {
      setShowDeleteConfirm(false);
      setDuplicateError(err instanceof Error ? err.message : "アカウント削除に失敗しました");
    } finally {
      setIsDeleting(false);
    }
  };

  // 認証確認中／未ログイン（ログインへ遷移するまで）／本人ページのデータ取得中
  if (authLoading || !isAuthenticated || (isOwner && isLoading)) {
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

            <label className="block text-[#444] text-sm mb-1">アカウントID</label>
            <input
              type="text"
              value={formAccountId}
              disabled
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] bg-gray-100 cursor-not-allowed"
            />

            <label className="block text-[#444] text-sm mb-1">アカウント名</label>
            <input
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
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.accountName && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.accountName}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">新しいパスワード</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              onBlur={() => {
                if (newPassword && !PASSWORD_PATTERN.test(newPassword)) {
                  setErrors((prev) => ({ ...prev, newPassword: "半角英数字で8文字以上で入力してください" }));
                } else {
                  setErrors((prev) => clearError(prev, "newPassword"));
                }
              }}
              placeholder="半角英数字で8文字以上"
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.newPassword && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.newPassword}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">生年月日</label>
            <input
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
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.birthdate && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.birthdate}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">性別</label>
            <select
              value={sexKbn}
              onChange={(e) => setSexKbn(e.target.value)}
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            >
              <option value="none"></option>
              <option value="man">男性</option>
              <option value="woman">女性</option>
            </select>

            <label className="block text-[#444] text-sm mb-1">出身地</label>
            <select
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

            <label className="block text-[#444] text-sm mb-1">居住地</label>
            <select
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

            <label className="block text-[#444] text-sm mb-1">メモ</label>
            <input
              type="text"
              value={freeMemo}
              onChange={(e) => setFreeMemo(e.target.value)}
              className="block w-full p-[10px] mb-[10px] border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />

            {duplicateError && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{duplicateError}</p>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
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
        <div className="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]">
          <div className="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]">
            <p className="text-[#444] text-center mb-4">
              登録した写真やお気に入りはすべて削除され、復旧できなくなります。よろしいですか？
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                disabled={isDeleting}
                className="flex-1 h-[40px] bg-gray-300 text-[#444] border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                いいえ
              </button>
              <button
                onClick={handleDeleteAccount}
                disabled={isDeleting}
                className="flex-1 h-[40px] bg-[#e53935] text-white border-none rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isDeleting ? (
                  <span className="inline-block w-5 h-5 border-[3px] border-white border-t-[rgba(255,255,255,0.3)] rounded-full animate-spin" />
                ) : (
                  "はい"
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteCompleteModal && (
        <div className="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]">
          <div className="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]">
            <p className="text-[#444] text-center">アカウントを削除しました</p>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]">
          <div className="bg-white rounded-md p-6 shadow-lg relative max-w-[300px] w-[90%]">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-2 right-3 text-xl text-gray-400 bg-transparent border-none cursor-pointer"
            >
              &times;
            </button>
            <p className="text-[#444] text-center">アカウントを登録しました</p>
          </div>
        </div>
      )}
    </div>
  );
}
