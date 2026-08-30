"use client";

import { useEffect, useRef, useState } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getAdminAccountList,
  unlockAccount,
  lockAccount,
  type AdminAccountListItem,
} from "@/lib/api/client";
import { LOGIN_FAILURE_LOCK_THRESHOLD } from "@/lib/consts";
import { ModalDialog } from "@/components/ui/ModalDialog";

/** ロック操作の確認対象 */
interface PendingLockAction {
  type: "lock" | "unlock";
  accountNo: number;
  accountId: string;
}

/**
 * 管理者用アカウント管理コンポーネント
 */
export function AdminAccountManagement() {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [accounts, setAccounts] = useState<AdminAccountListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // 追加読み込みの失敗通知（取得済みの一覧は維持したまま表示する）
  const [loadMoreError, setLoadMoreError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);
  const [pendingAction, setPendingAction] = useState<PendingLockAction | null>(null);
  const [isActionProcessing, setIsActionProcessing] = useState(false);
  // 取得リクエストの世代。初期ロード・再取得・もっと見るは開始時に採番し、
  // 自分が最新でなければ結果を破棄する（後着レスポンスが新しい一覧を上書きする競合を防ぐ）
  const loadSeqRef = useRef(0);
  // 「+もっと見る」の再入防止（isLoadingMore の setState 反映前の連打対策）
  const isLoadingMoreRef = useRef(false);

  const isAdmin = user?.role === "ROLE_ADMIN";
  const canView = !isAuthLoading && isAuthenticated && isAdmin;

  /**
   * 1ページ目を取得し直す（再読み込みボタン・ロック操作後に使用）
   */
  const fetchAccounts = async () => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getAdminAccountList(1);
      if (loadSeqRef.current !== seq) return;
      setAccounts(data.accountList);
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
      try {
        const data = await getAdminAccountList(1);
        if (cancelled || loadSeqRef.current !== seq) return;
        setAccounts(data.accountList);
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
  }, [canView]);

  /**
   * +もっと見る
   */
  const handleLoadMore = async () => {
    // setState 反映前の連打で同一ページを二重取得しないよう ref で再入を防ぐ
    if (isLoadingMoreRef.current) return;
    isLoadingMoreRef.current = true;
    const nextPage = pageNo + 1;
    const seq = ++loadSeqRef.current;
    setIsLoadingMore(true);
    // 追加読み込み時は古いロック操作の成功メッセージ・失敗通知を消す
    setMessage(null);
    setLoadMoreError(null);
    try {
      const data = await getAdminAccountList(nextPage);
      // 再取得等で世代が変わっていたら、古いページを継ぎ足さない
      if (loadSeqRef.current !== seq) return;
      setAccounts((prev) => [...prev, ...data.accountList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      // 取得済みの一覧は維持し、通知だけ表示する
      setLoadMoreError(
        err instanceof Error ? err.message : "エラーが発生しました"
      );
    } finally {
      isLoadingMoreRef.current = false;
      if (loadSeqRef.current === seq) setIsLoadingMore(false);
    }
  };

  /**
   * 確認ダイアログで「実行」が押されたときの処理
   */
  const handleConfirmAction = async () => {
    if (!pendingAction || isActionProcessing) return;
    setIsActionProcessing(true);
    setMessage(null);
    setError(null);
    try {
      const result =
        pendingAction.type === "unlock"
          ? await unlockAccount(pendingAction.accountNo)
          : await lockAccount(pendingAction.accountNo);
      setMessage(result.message);
      setPendingAction(null);
      await fetchAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
      setPendingAction(null);
    } finally {
      setIsActionProcessing(false);
    }
  };

  if (isAuthLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (!isAuthenticated || !isAdmin) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p className="text-red-500">管理者権限がありません。</p>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[200px]">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
        <p className="text-red-500">{error}</p>
        <button
          onClick={fetchAccounts}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          再読み込み
        </button>
      </div>
    );
  }

  const formatDatetime = (datetime: string | null): string => {
    if (!datetime) return "-";
    const date = new Date(datetime);
    // 未ログイン（バックエンドの最小日時センチネル）や不正な値は "-" で表示
    if (Number.isNaN(date.getTime()) || date.getFullYear() <= 1900) return "-";
    return date.toLocaleString("ja-JP");
  };

  const authorityLabel = (kbn: string): string => {
    switch (kbn) {
      case "administrator": return "管理者";
      case "special-user": return "特別ユーザー";
      case "normal-user": return "一般ユーザー";
      case "mini-user": return "簡易ユーザー";
      default: return kbn;
    }
  };

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <h2 className="text-xl font-bold">アカウント管理</h2>

      {message && (
        <p className="text-green-600 font-medium">{message}</p>
      )}

      <div
        className="w-full max-w-[1100px] overflow-x-auto"
        style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
      >
        <table className="w-full border-collapse">
          <thead>
            <tr style={{ backgroundColor: "#2196F3" }}>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">No</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">ID</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">アカウント名</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">権限</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">状態</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">最終ログイン</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">失敗回数</th>
              <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">操作</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr
                key={account.accountNo}
                className="bg-white transition-colors"
                style={{ cursor: "default" }}
                onMouseEnter={(e) =>
                  (e.currentTarget.style.backgroundColor = "#fffae9")
                }
                onMouseLeave={(e) =>
                  (e.currentTarget.style.backgroundColor = "white")
                }
              >
                <td className="py-3 px-4 border border-gray-300">{account.accountNo}</td>
                <td className="py-3 px-4 border border-gray-300">{account.accountId}</td>
                <td className="py-3 px-4 border border-gray-300">{account.accountName}</td>
                <td className="py-3 px-4 border border-gray-300">{authorityLabel(account.authorityKbn)}</td>
                <td className="py-3 px-4 border border-gray-300">
                  {account.isDeleted ? (
                    <span className="text-gray-500">削除済み</span>
                  ) : account.loginFailureCount >= LOGIN_FAILURE_LOCK_THRESHOLD ? (
                    <span className="text-red-500 font-bold">ロック中</span>
                  ) : (
                    <span className="text-green-600">有効</span>
                  )}
                </td>
                <td className="py-3 px-4 border border-gray-300">{formatDatetime(account.lastLoginDatetime)}</td>
                <td className="py-3 px-4 border border-gray-300">{account.loginFailureCount}</td>
                <td className="py-3 px-4 border border-gray-300">
                  <div className="flex gap-2">
                    <button
                      onClick={() =>
                        setPendingAction({
                          type: "unlock",
                          accountNo: account.accountNo,
                          accountId: account.accountId,
                        })
                      }
                      className="px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
                      disabled={account.isDeleted || account.loginFailureCount === 0}
                    >
                      ロック解除
                    </button>
                    <button
                      onClick={() =>
                        setPendingAction({
                          type: "lock",
                          accountNo: account.accountNo,
                          accountId: account.accountId,
                        })
                      }
                      className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
                      disabled={
                        account.isDeleted ||
                        account.loginFailureCount >= LOGIN_FAILURE_LOCK_THRESHOLD
                      }
                    >
                      強制ロック
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {loadMoreError && (
        <p role="alert" className="text-red-500 text-sm">
          {loadMoreError}
        </p>
      )}

      {!isLast && (
        <button
          onClick={handleLoadMore}
          disabled={isLoadingMore}
          data-testid="show-more-button"
          className="px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
        >
          {isLoadingMore ? "読み込み中..." : "＋もっと見る"}
        </button>
      )}

      {/* ロック操作の確認ダイアログ */}
      {pendingAction && (
        <ModalDialog
          testId="lock-confirm-dialog"
          label={
            pendingAction.type === "unlock"
              ? "ロック解除の確認"
              : "強制ロックの確認"
          }
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => {
            if (isActionProcessing) return;
            setPendingAction(null);
          }}
          overlayClassName="fixed inset-0 bg-[rgba(0,0,0,0.5)] flex items-center justify-center z-[2000]"
          containerClassName="bg-white rounded-md p-6 shadow-lg max-w-[320px] w-[90%]"
        >
          <p className="text-[#444] text-center mb-4">
            {pendingAction.type === "unlock"
              ? `${pendingAction.accountId} のロックを解除しますか？`
              : `${pendingAction.accountId} を強制ロックしますか？`}
          </p>
          <div className="flex gap-3">
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => setPendingAction(null)}
              disabled={isActionProcessing}
              className="flex-1 h-[40px] bg-gray-300 text-[#444] rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              キャンセル
            </button>
            <button
              type="button"
              onClick={handleConfirmAction}
              disabled={isActionProcessing}
              className={`flex-1 h-[40px] text-white rounded-sm cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed ${
                pendingAction.type === "unlock" ? "bg-green-500" : "bg-red-500"
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
