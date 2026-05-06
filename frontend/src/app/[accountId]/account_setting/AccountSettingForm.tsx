"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAccount,
  updateAccount,
  getPrefectures,
} from "@/lib/api/client";
import type {
  AccountDetail,
  PrefectureGroup,
} from "@/lib/api/client";

interface AccountSettingFormProps {
  accountId: string;
}

/**
 * アカウント設定フォームコンポーネント
 * 既存のThymeleafデザインをTailwind CSSで再現
 */
export function AccountSettingForm({ accountId }: AccountSettingFormProps) {
  const router = useRouter();
  const { logout } = useAuth();

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

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [duplicateError, setDuplicateError] = useState("");

  const loadData = useCallback(async () => {
    try {
      const [accountData, prefectureData] = await Promise.all([
        getAccount(accountId),
        getPrefectures(),
      ]);

      setFormAccountId(accountData.accountId);
      setAccountName(accountData.accountName);
      setBirthdate(accountData.birthdate || "");
      setSexKbn(accountData.sexKbn || "none");
      setBirthplacePrefectureKbnCode(accountData.birthplacePrefectureKbnCode || "none");
      setResidentPrefectureKbnCode(accountData.residentPrefectureKbnCode || "none");
      setFreeMemo(accountData.freeMemo || "");
      setPrefectureGroups(prefectureData);
    } catch {
      router.push("/login");
    } finally {
      setIsLoading(false);
    }
  }, [accountId, router]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!accountName.trim()) {
      newErrors.accountName = "アカウント名を入力してください";
    }

    if (newPassword && !/^[a-zA-Z0-9]{8,}$/.test(newPassword)) {
      newErrors.newPassword = "半角英数字で8文字以上で入力してください";
    }

    if (birthdate) {
      const birthdateDate = new Date(birthdate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (birthdateDate >= today) {
        newErrors.birthdate = "過去の日付を入力してください";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

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

      if (result.isDuplicateAccountId) {
        setDuplicateError("このアカウントIDは既に使われています");
        return;
      }

      if (result.isAccountIdChanged || result.isPasswordChanged) {
        await logout();
        router.push("/login");
        return;
      }

      setShowModal(true);
      setTimeout(() => setShowModal(false), 5000);
    } catch {
      setDuplicateError("更新に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[whitesmoke] flex items-center justify-center">
        <div className="inline-block w-8 h-8 border-4 border-[#2196F3] border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[whitesmoke] font-['Open_Sans',sans-serif]">
      <header>
        <a
          href={`/photo/${accountId}/photo_list`}
          className="fixed top-[5px] left-[10px] text-xl text-gray-400 z-[1000] no-underline"
        >
          &larr; back
        </a>
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
                  setErrors((prev) => {
                    const { accountName: _, ...rest } = prev;
                    return rest;
                  });
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
                if (value) {
                  const inputDate = new Date(value);
                  const today = new Date();
                  today.setHours(0, 0, 0, 0);
                  if (inputDate >= today) {
                    setErrors((prev) => ({ ...prev, birthdate: "過去の日付を入力してください" }));
                  } else {
                    setErrors((prev) => {
                      const { birthdate: _, ...rest } = prev;
                      return rest;
                    });
                  }
                } else {
                  setErrors((prev) => {
                    const { birthdate: _, ...rest } = prev;
                    return rest;
                  });
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
        </div>
      </div>

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
