"use client";

import { useEffect, useRef, useState } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAdminPhotoList,
  updateAdminPhotoVisibility,
  type AdminPhotoListItem,
  type AdminPhotoListParams,
} from "@/lib/api/client";
import { sanitizeImageUrl } from "@/lib/url";
import { ModalDialog } from "@/components/ui/ModalDialog";

/** 公開状態による絞り込みの値（""=すべて） */
type VisibilityFilter = "" | "hidden" | "public";

/** 公開状態による絞り込みの選択肢 */
const VISIBILITY_OPTIONS: { value: VisibilityFilter; label: string }[] = [
  { value: "", label: "すべて" },
  { value: "public", label: "公開中" },
  { value: "hidden", label: "非公開" },
];

/** 理由の最大文字数（backendの`Consts.MODERATION_REASON_MAX_LENGTH`に合わせる） */
const REASON_MAX_LENGTH = 500;

/** 公開状態変更の確認対象 */
interface PendingVisibilityChange {
  accountNo: number;
  accountId: string;
  photoNo: number;
  /** 変更後の管理者非公開フラグ（trueなら非公開にする） */
  isHiddenByAdmin: boolean;
}

/**
 * 絞り込み条件からAPIの検索条件を組み立てる
 *
 * @param accountId 写真所有者のアカウントID（空文字なら絞り込まない）
 * @param visibility 公開状態による絞り込み
 * @param pageNo ページ番号
 * @returns {@link AdminPhotoListParams}
 */
const toParams = (
  accountId: string,
  visibility: VisibilityFilter,
  pageNo: number
): AdminPhotoListParams => ({
  accountId: accountId || undefined,
  isHiddenByAdmin: visibility === "" ? undefined : visibility === "hidden",
  pageNo,
});

/**
 * 管理者用写真管理コンポーネント
 *
 * 不適切な画像を非公開にする（所有者本人以外には表示されなくなる）、または非公開を解除する
 */
export function AdminPhotoManagement() {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [photos, setPhotos] = useState<AdminPhotoListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // 追加読み込みの失敗通知（取得済みの一覧は維持したまま表示する）
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  // 公開状態変更の失敗通知（取得済みの一覧は維持したまま表示する）
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);
  const [visibilityFilter, setVisibilityFilter] = useState<VisibilityFilter>("");
  // 入力中のアカウントIDと、検索ボタンで適用済みのアカウントID（入力のたびに再取得しない）
  const [accountIdInput, setAccountIdInput] = useState("");
  const [appliedAccountId, setAppliedAccountId] = useState("");
  const [pendingChange, setPendingChange] = useState<PendingVisibilityChange | null>(null);
  const [reason, setReason] = useState("");
  const [isActionProcessing, setIsActionProcessing] = useState(false);
  // 取得リクエストの世代。初期ロード・再取得・もっと見るは開始時に採番し、
  // 自分が最新でなければ結果を破棄する（後着レスポンスが新しい一覧を上書きする競合を防ぐ）
  const loadSeqRef = useRef(0);
  // 「+もっと見る」の再入防止（isLoadingMore の setState 反映前の連打対策）
  const isLoadingMoreRef = useRef(false);

  const isAdmin = user?.role === "ROLE_ADMIN";
  const canView = !isAuthLoading && isAuthenticated && isAdmin;

  /**
   * 1ページ目を取得し直す（再読み込みボタンで使用）
   */
  const fetchPhotos = async () => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getAdminPhotoList(toParams(appliedAccountId, visibilityFilter, 1));
      if (loadSeqRef.current !== seq) return;
      setPhotos(data.photoList);
      setIsLast(data.isLast);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      if (loadSeqRef.current === seq) setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!canView) return;

    let cancelled = false;
    const seq = ++loadSeqRef.current;
    const load = async () => {
      setIsLoading(true);
      setPageNo(1);
      try {
        const data = await getAdminPhotoList(toParams(appliedAccountId, visibilityFilter, 1));
        if (cancelled || loadSeqRef.current !== seq) return;
        setPhotos(data.photoList);
        setIsLast(data.isLast);
        setError(null);
      } catch (err) {
        if (cancelled || loadSeqRef.current !== seq) return;
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      } finally {
        if (!cancelled && loadSeqRef.current === seq) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [canView, appliedAccountId, visibilityFilter]);

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    if (isLoadingMoreRef.current) return;
    isLoadingMoreRef.current = true;
    const nextPage = pageNo + 1;
    const seq = ++loadSeqRef.current;
    setIsLoadingMore(true);
    setLoadMoreError(null);
    try {
      const data = await getAdminPhotoList(toParams(appliedAccountId, visibilityFilter, nextPage));
      if (loadSeqRef.current !== seq) return;
      setPhotos((prev) => [...prev, ...data.photoList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setLoadMoreError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      isLoadingMoreRef.current = false;
      if (loadSeqRef.current === seq) setIsLoadingMore(false);
    }
  };

  /**
   * 確認ダイアログで「実行」が押されたときの処理
   *
   * 一覧の取得はリードレプリカへ振り分けられうるため、成功後に再取得すると直前の変更が反映されていないことがある。
   * そのため再取得はせず、操作した写真の状態だけを手元で書き換える
   */
  const handleConfirmChange = async () => {
    if (!pendingChange || isActionProcessing || !reason.trim()) return;
    setIsActionProcessing(true);
    setMessage(null);
    setActionError(null);
    try {
      const result = await updateAdminPhotoVisibility(
        pendingChange.accountNo,
        pendingChange.photoNo,
        pendingChange.isHiddenByAdmin,
        reason.trim()
      );
      setMessage(result.message);
      setPhotos((prev) =>
        prev.map((photo) =>
          photo.accountNo === pendingChange.accountNo && photo.photoNo === pendingChange.photoNo
            ? { ...photo, isHiddenByAdmin: pendingChange.isHiddenByAdmin }
            : photo
        )
      );
      setPendingChange(null);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "エラーが発生しました");
      setPendingChange(null);
    } finally {
      setIsActionProcessing(false);
    }
  };

  const formatDatetime = (datetime: string): string => {
    const date = new Date(datetime);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString("ja-JP");
  };

  // 見出しレベル1はどの表示状態でも1つ存在させる（一覧本体を出せない状態でも欠落させない）
  if (isAuthLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <h1 className="sr-only">写真管理</h1>
        <p>読み込み中...</p>
      </div>
    );
  }

  if (!isAuthenticated || !isAdmin) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <h1 className="sr-only">写真管理</h1>
        <p className="text-red-500">管理者権限がありません。</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <h1 className="text-xl font-bold">写真管理</h1>

      <div className="w-full max-w-[1100px] flex flex-wrap items-center gap-4 px-4">
        <form
          className="flex items-center gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            setMessage(null);
            setAppliedAccountId(accountIdInput.trim());
          }}
        >
          <label htmlFor="account-id-filter" className="text-sm text-[#444]">
            アカウントID
          </label>
          <input
            id="account-id-filter"
            type="text"
            value={accountIdInput}
            onChange={(e) => setAccountIdInput(e.target.value)}
            placeholder="未入力なら全アカウント"
            className="border border-gray-300 rounded-sm px-2 py-1"
          />
          <button
            type="submit"
            className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            検索
          </button>
        </form>
        <div className="flex items-center gap-2">
          <label htmlFor="visibility-filter" className="text-sm text-[#444]">
            公開状態
          </label>
          <select
            id="visibility-filter"
            value={visibilityFilter}
            onChange={(e) => {
              setMessage(null);
              setVisibilityFilter(e.target.value as VisibilityFilter);
            }}
            className="border border-gray-300 rounded-sm px-2 py-1"
          >
            {VISIBILITY_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/*
        ライブリージョンはメッセージと同時にDOMへ挿入すると読み上げられないため、常設して中身だけを差し替える
        （AdminAccountManagement と同じ理由で、空のときは `empty:sr-only` でレイアウト上の余白からだけ外す）
      */}
      <p role="status" className="text-green-600 font-medium empty:sr-only">
        {message}
      </p>

      <p role="alert" className="text-red-500 text-sm empty:sr-only">
        {actionError}
      </p>

      {/* 絞り込みの変更や再取得では一覧本体だけを切り替える（操作中の入力欄・ライブリージョンをアンマウントしない） */}
      {isLoading ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p>読み込み中...</p>
        </div>
      ) : error ? (
        <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
          <p className="text-red-500">{error}</p>
          <button
            onClick={fetchPhotos}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            再読み込み
          </button>
        </div>
      ) : photos.length === 0 ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p>該当する写真はありません。</p>
        </div>
      ) : (
        <div
          className="w-full max-w-[1100px] overflow-x-auto"
          style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
        >
          <table className="w-full border-collapse">
            <thead>
              <tr style={{ backgroundColor: "#2196F3" }}>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">写真</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">ID</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">アカウント名</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">タイトル</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">登録日時</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">状態</th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">操作</th>
              </tr>
            </thead>
            <tbody>
              {photos.map((photo) => {
                const imageSrc = sanitizeImageUrl(photo.imageFilePath);
                return (
                  <tr
                    key={`${photo.accountNo}-${photo.photoNo}`}
                    data-testid={`admin-photo-row-${photo.accountNo}-${photo.photoNo}`}
                    className="bg-white"
                  >
                    <td className="py-2 px-4 border border-gray-300">
                      {imageSrc ? (
                        <a href={imageSrc} target="_blank" rel="noreferrer">
                          <img
                            src={imageSrc}
                            alt={photo.photoJapaneseTitle || "写真"}
                            className="w-[120px] h-[90px] object-contain bg-black"
                          />
                        </a>
                      ) : (
                        <span className="text-gray-500 text-sm">画像を表示できません</span>
                      )}
                    </td>
                    <td className="py-2 px-4 border border-gray-300">{photo.accountId}</td>
                    <td className="py-2 px-4 border border-gray-300">{photo.accountName}</td>
                    <td className="py-2 px-4 border border-gray-300 break-all">
                      {photo.photoJapaneseTitle || "-"}
                    </td>
                    <td className="py-2 px-4 border border-gray-300">{formatDatetime(photo.createdAt)}</td>
                    <td className="py-2 px-4 border border-gray-300">
                      {photo.isHiddenByAdmin ? (
                        <span className="text-red-500 font-bold">非公開</span>
                      ) : (
                        <span className="text-green-600">公開中</span>
                      )}
                    </td>
                    <td className="py-2 px-4 border border-gray-300">
                      <button
                        onClick={() => {
                          setReason("");
                          setActionError(null);
                          setPendingChange({
                            accountNo: photo.accountNo,
                            accountId: photo.accountId,
                            photoNo: photo.photoNo,
                            isHiddenByAdmin: !photo.isHiddenByAdmin,
                          });
                        }}
                        className={`px-3 py-1 text-sm text-white rounded ${
                          photo.isHiddenByAdmin
                            ? "bg-green-500 hover:bg-green-600"
                            : "bg-red-500 hover:bg-red-600"
                        }`}
                      >
                        {photo.isHiddenByAdmin ? "非公開を解除" : "非公開にする"}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <p role="alert" className="text-red-500 text-sm empty:sr-only">
        {loadMoreError}
      </p>

      {!isLoading && !error && !isLast && (
        <button
          onClick={handleLoadMore}
          disabled={isLoadingMore}
          data-testid="show-more-button"
          className="px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
        >
          {isLoadingMore ? "読み込み中..." : "＋もっと見る"}
        </button>
      )}

      {/* 公開状態変更の確認ダイアログ（理由は必須） */}
      {pendingChange && (
        <ModalDialog
          testId="visibility-confirm-dialog"
          label={pendingChange.isHiddenByAdmin ? "写真の非公開の確認" : "写真の非公開解除の確認"}
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => {
            if (isActionProcessing) return;
            setPendingChange(null);
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg max-w-[420px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-2">
            {pendingChange.isHiddenByAdmin
              ? `${pendingChange.accountId} の写真（No.${pendingChange.photoNo}）を非公開にしますか？`
              : `${pendingChange.accountId} の写真（No.${pendingChange.photoNo}）の非公開を解除しますか？`}
          </p>
          {pendingChange.isHiddenByAdmin && (
            <p className="text-[#666] text-xs text-center mb-4">
              非公開にすると、所有者本人以外には写真一覧・写真詳細・公開ポートフォリオで表示されなくなります。
            </p>
          )}
          <label htmlFor="moderation-reason" className="block text-sm text-[#444] mb-1">
            理由（必須。所有者には表示されません）
          </label>
          <textarea
            id="moderation-reason"
            data-dialog-initial-focus
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            maxLength={REASON_MAX_LENGTH}
            rows={4}
            className="w-full border border-gray-300 rounded-sm px-2 py-1 mb-4 text-[#444]"
          />
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setPendingChange(null)}
              disabled={isActionProcessing}
              className="flex-1 h-[40px] bg-gray-300 text-[#444] rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              キャンセル
            </button>
            <button
              type="button"
              onClick={handleConfirmChange}
              disabled={isActionProcessing || !reason.trim()}
              className={`flex-1 h-[40px] text-white rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed ${
                pendingChange.isHiddenByAdmin ? "bg-red-500" : "bg-green-500"
              }`}
            >
              {isActionProcessing ? "処理中..." : "実行"}
            </button>
          </div>
        </ModalDialog>
      )}
    </div>
  );
}
