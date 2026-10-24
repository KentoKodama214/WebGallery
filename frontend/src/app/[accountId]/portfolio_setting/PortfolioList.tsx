"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPortfolioList,
  getPortfolioUpperLimit,
  type PortfolioListFilter,
  type PortfolioListItem,
} from "@/lib/api/client";
import { PORTFOLIO_THEME_MAX_LENGTH, TAG_NAME_MAX_LENGTH } from "@/lib/validation";
import { PORTFOLIO_STATUS_LABELS, PORTFOLIO_TEMPLATE_LABELS } from "./labels";

interface PortfolioListProps {
  /** URLのアカウントID */
  accountId: string;
}

/**
 * 本人のポートフォリオ一覧（下書きを含む）コンポーネント
 *
 * テーマ・タグ名（部分一致）で絞り込める。作成数が権限ごとの上限に達している場合は新規作成できない。
 */
export function PortfolioList({ accountId }: PortfolioListProps) {
  const { user } = useAuth();
  const isOwner = !!user && user.accountId === accountId;

  const [portfolios, setPortfolios] = useState<PortfolioListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 絞り込みの入力値と、一覧の取得に使った（検索ボタンで確定した）条件
  const [themeInput, setThemeInput] = useState("");
  const [tagInput, setTagInput] = useState("");
  const [appliedFilter, setAppliedFilter] = useState<PortfolioListFilter>({});
  // 作成数が上限に達しているか（取得に失敗した場合は作成を妨げない。バックエンドでも上限を検証する）
  const [isReachedUpperLimit, setIsReachedUpperLimit] = useState(false);
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

  // 作成数の上限に達しているかを取得する
  useEffect(() => {
    if (!isOwner) return;
    let cancelled = false;
    getPortfolioUpperLimit(accountId)
      .then((data) => {
        if (!cancelled) setIsReachedUpperLimit(data.isReachedUpperLimit);
      })
      .catch(() => {
        // 取得に失敗しても新規作成は妨げない（作成時にバックエンドが上限を検証する）
      });
    return () => {
      cancelled = true;
    };
  }, [isOwner, accountId]);

  /**
   * 指定した条件で一覧を取得し直す
   *
   * @param filter 絞り込み条件
   */
  const loadList = async (filter: PortfolioListFilter) => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getPortfolioList(accountId, filter);
      if (loadSeqRef.current !== seq) return;
      setPortfolios(data);
      setAppliedFilter(filter);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      if (loadSeqRef.current === seq) setIsLoading(false);
    }
  };

  /** 取得失敗時の再読み込み（直前に確定した条件で取得し直す） */
  const reload = () => loadList(appliedFilter);

  /** 入力した条件で絞り込む */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadList({ theme: themeInput.trim(), tag: tagInput.trim() });
  };

  /** 絞り込み条件を解除する */
  const handleClear = () => {
    setThemeInput("");
    setTagInput("");
    loadList({});
  };

  const isFiltered = !!appliedFilter.theme || !!appliedFilter.tag;

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
        {isReachedUpperLimit ? (
          <button
            type="button"
            disabled
            aria-describedby="portfolio-upper-limit-message"
            className="px-4 py-2 text-sm bg-blue-700 text-white rounded disabled:opacity-50"
          >
            新規作成
          </button>
        ) : (
          <Link
            href={`/${accountId}/portfolio_setting/edit`}
            className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
          >
            新規作成
          </Link>
        )}
      </div>
      {isReachedUpperLimit && (
        <p
          id="portfolio-upper-limit-message"
          className="w-full max-w-[800px] px-4 text-sm text-red-700 text-right"
        >
          作成できるポートフォリオの数が上限に達しています
        </p>
      )}

      <form
        onSubmit={handleSearch}
        role="search"
        aria-label="ポートフォリオの絞り込み"
        className="w-full max-w-[800px] px-4 flex flex-wrap items-end gap-3"
      >
        <div className="flex flex-col">
          <label htmlFor="portfolio-filter-theme" className="text-sm font-bold mb-1">
            テーマ
          </label>
          <input
            id="portfolio-filter-theme"
            type="text"
            value={themeInput}
            maxLength={PORTFOLIO_THEME_MAX_LENGTH}
            onChange={(e) => setThemeInput(e.target.value)}
            className="border border-gray-300 rounded px-3 py-2"
          />
        </div>
        <div className="flex flex-col">
          <label htmlFor="portfolio-filter-tag" className="text-sm font-bold mb-1">
            タグ
          </label>
          <input
            id="portfolio-filter-tag"
            type="text"
            value={tagInput}
            maxLength={TAG_NAME_MAX_LENGTH}
            onChange={(e) => setTagInput(e.target.value)}
            className="border border-gray-300 rounded px-3 py-2"
          />
        </div>
        <button
          type="submit"
          className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
        >
          検索
        </button>
        <button
          type="button"
          onClick={handleClear}
          className="px-4 py-2 text-sm border border-gray-400 rounded hover:bg-gray-50"
        >
          クリア
        </button>
      </form>

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
        <p className="text-gray-600">
          {isFiltered ? "条件に一致するポートフォリオはありません" : "ポートフォリオはありません"}
        </p>
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
