"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  registerAccount,
  getPrefectures,
} from "@/lib/api/client";
import type { PrefectureGroup } from "@/lib/api/client";

/**
 * アカウント登録フォームコンポーネント
 * AccountSettingFormをベースに登録用に変更
 */
export function RegisterForm() {
  const router = useRouter();

  const [accountId, setAccountId] = useState("");
  const [accountName, setAccountName] = useState("");
  const [password, setPassword] = useState("");
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
  const [submitError, setSubmitError] = useState("");

  useEffect(() => {
    getPrefectures()
      .then((data) => setPrefectureGroups(data))
      .catch(() => {
        // 都道府県取得失敗時は空のまま続行
      })
      .finally(() => setIsLoading(false));
  }, []);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!accountId || !/^[a-zA-Z0-9]{8,16}$/.test(accountId)) {
      newErrors.accountId = "半角英数字で8〜16文字で入力してください";
    }

    if (!accountName.trim()) {
      newErrors.accountName = "アカウント名を入力してください";
    }

    if (!password || !/^[a-zA-Z0-9]{8,}$/.test(password)) {
      newErrors.password = "半角英数字で8文字以上で入力してください";
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
    setSubmitError("");

    if (!validate()) return;

    setIsSubmitting(true);

    try {
      const result = await registerAccount({
        accountId,
        accountName,
        password,
        birthdate: birthdate || null,
        sexKbn,
        birthplacePrefectureKbnCode,
        residentPrefectureKbnCode,
        freeMemo,
      });

      if (!result.isSuccess) {
        setSubmitError("このアカウントIDは既に使われています");
        return;
      }

      setShowModal(true);
      setTimeout(() => {
        router.push("/login");
      }, 5000);
    } catch {
      setSubmitError("登録に失敗しました");
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
          href="/login"
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
              Create an Account
            </p>

            <label className="block text-[#444] text-sm mb-1">アカウントID</label>
            <input
              type="text"
              value={accountId}
              onChange={(e) => setAccountId(e.target.value)}
              placeholder="半角英数字で8〜16文字"
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.accountId && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.accountId}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">アカウント名</label>
            <input
              type="text"
              value={accountName}
              onChange={(e) => setAccountName(e.target.value)}
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.accountName && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.accountName}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">パスワード</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="半角英数字で8文字以上"
              className="block w-full p-[10px] mb-1 border border-[#ddd] rounded-sm text-[#444] outline-none focus:border-[#2196F3]"
            />
            {errors.password && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{errors.password}</p>
            )}

            <label className="block text-[#444] text-sm mb-1 mt-2">生年月日</label>
            <input
              type="date"
              value={birthdate}
              onChange={(e) => setBirthdate(e.target.value)}
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

            {submitError && (
              <p className="text-[lightcoral] text-xs font-bold mb-2">{submitError}</p>
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
            <p className="text-[#444] text-center">
              アカウントを登録しました。ログインページへ移動します。
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
