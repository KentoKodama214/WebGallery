"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import { getPortfolioList, type PortfolioListItem } from "@/lib/api/client";
import { PORTFOLIO_STATUS_LABELS, PORTFOLIO_TEMPLATE_LABELS } from "./labels";

interface PortfolioListProps {
  /** URLのアカウントID */
  accountId: string;
}

/**
 * 本人のポートフォリオ一覧（下書きを含む）コンポーネント
 */
export function PortfolioList({ accountId }: PortfolioListProps) {
  const { user } = useAuth();
  const isOwner = !!user && user.accountId === accountId;

  const [portfolios, setPortfolios] = useState<PortfolioListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 取得リクエストの世代。後着レスポンスが新しい一覧を上書きする競合を防ぐ
  const loadSeqRef = useRef(0);

  // 初回表示時の読み込み（状態の更新は応答受信後だけに行う）
  useEffect(() => {
    if (!isOwner) return;
    const seq = ++loadSeqRef.current;
    const load = async () => {
      try {
        const data = await getPortfolioList(accountId);
        if (loadSeqRef.current !== seq) return;
        setPortfolios(data);
        setError(null);
      } catch (err) {
        if (loadSeqRef.current !== seq) return;
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      } finally {
        if (loadSeqRef.current === seq) setIsLoading(false);
      }
    };
    load();
    return () => {
      // アンマウント後に届いたレスポンスで状態を更新しない
      loadSeqRef.current++;
    };
  }, [isOwner, accountId]);

  /** 取得失敗時の再読み込み */
  const reload = async () => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getPortfolioList(accountId);
      if (loadSeqRef.current !== seq) return;
      setPortfolios(data);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      if (loadSeqRef.current === seq) setIsLoading(false);
    }
  };

  // ログイン済みだが他人のポートフォリオ設定を開こうとした場合（認可はバックエンドでも実施）
  if (!isOwner) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-700">この操作を行う権限がありません</p>
      </div>
    );
  }

  const formatDatetime = (datetime: string): string => {
    const date = new Date(datetime);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString("ja-JP");
  };

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <div className="w-full max-w-[800px] flex justify-between items-center px-4">
        <h1 className="text-xl font-bold">ポートフォリオ設定</h1>
        <div className="flex items-center gap-4">
          <Link
            href={`/portfolio/${accountId}`}
            target="_blank"
            rel="noopener"
            className="text-sm text-blue-700 hover:underline"
          >
            公開ページを見る
          </Link>
          <Link
            href={`/${accountId}/portfolio_setting/edit`}
            className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
          >
            新規作成
          </Link>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p>読み込み中...</p>
        </div>
      ) : error ? (
        <div className="flex flex-col justify-center items-center min-h-[200px] gap-4">
          <p className="text-red-700">{error}</p>
          <button
            onClick={reload}
            className="px-4 py-2 bg-blue-700 text-white rounded hover:bg-blue-800"
          >
            再読み込み
          </button>
        </div>
      ) : portfolios.length === 0 ? (
        <p className="text-gray-600">ポートフォリオはありません</p>
      ) : (
        <div
          className="w-full max-w-[800px] overflow-x-auto"
          style={{ boxShadow: "0 2px 8px rgba(0, 0, 0, 0.15)" }}
        >
          <table className="w-full border-collapse bg-white">
            <thead>
              <tr style={{ backgroundColor: "#1565C0" }}>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  テーマ
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  テンプレート
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  写真
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  状態
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  更新日時
                </th>
              </tr>
            </thead>
            <tbody>
              {portfolios.map((portfolio) => (
                <tr key={portfolio.portfolioNo} data-testid="portfolio-row">
                  <td className="py-3 px-4 border border-gray-300">
                    <Link
                      href={`/${accountId}/portfolio_setting/edit?portfolioNo=${portfolio.portfolioNo}`}
                      className="text-blue-700 hover:underline"
                    >
                      {portfolio.theme}
                    </Link>
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    {PORTFOLIO_TEMPLATE_LABELS[portfolio.templateKbn] ?? portfolio.templateKbn}
                  </td>
                  <td className="py-3 px-4 border border-gray-300">{portfolio.photoCount}枚</td>
                  <td className="py-3 px-4 border border-gray-300">
                    <span
                      className={
                        portfolio.statusKbn === "published"
                          ? "inline-block px-2 py-0.5 text-xs bg-green-700 text-white rounded-full"
                          : "inline-block px-2 py-0.5 text-xs bg-gray-600 text-white rounded-full"
                      }
                    >
                      {PORTFOLIO_STATUS_LABELS[portfolio.statusKbn] ?? portfolio.statusKbn}
                    </span>
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    {formatDatetime(portfolio.updatedAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
