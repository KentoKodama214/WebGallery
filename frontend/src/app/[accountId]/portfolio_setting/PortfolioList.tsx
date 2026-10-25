"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPortfolioList,
  getPortfolioUpperLimit,
  updatePortfolioDisplayOrder,
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
 * 一覧は表示順（公開ページの一覧と同じ順）に並び、各行の上下ボタンで並び替えられる。
 * 並び替えは全件の並びを保存するため、絞り込み中は行えない。
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
  // 並び順の保存中か（保存中は続けて並び替えできないようにし、保存の順序の入れ違いを防ぐ）
  const [isSavingOrder, setIsSavingOrder] = useState(false);
  // 並び替えの結果（成功・失敗）のメッセージ
  const [orderMessage, setOrderMessage] = useState<{ text: string; isError: boolean } | null>(
    null
  );
  // 並び替えた行のボタンへフォーカスを戻すための参照（キー：`{ポートフォリオ番号}-{up|down}`）
  const moveButtonRefs = useRef(new Map<string, HTMLButtonElement>());
  // 保存後にフォーカスを戻すボタンのキー
  const pendingFocusRef = useRef<string | null>(null);

  // 並び替えの保存が終わったら、移動したボタンへフォーカスを戻す
  // （保存中はボタンを無効にするためフォーカスが外れる）。
  // 先頭・末尾に達してそのボタンが無効になった場合は、反対方向のボタンへ移す
  useEffect(() => {
    const pendingFocus = pendingFocusRef.current;
    if (!pendingFocus || isSavingOrder) return;
    pendingFocusRef.current = null;
    const [portfolioNo, direction] = pendingFocus.split("-");
    const target = moveButtonRefs.current.get(pendingFocus);
    const fallback = moveButtonRefs.current.get(
      `${portfolioNo}-${direction === "up" ? "down" : "up"}`
    );
    if (target && !target.disabled) {
      target.focus();
    } else if (fallback && !fallback.disabled) {
      fallback.focus();
    }
  }, [isSavingOrder]);

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

  /**
   * ポートフォリオを1つ上・下へ移動し、並び順を保存する
   *
   * 画面の並びを先に入れ替え、保存に失敗した場合はサーバーから一覧を取り直す
   * （一覧の表示後に他のタブ等でポートフォリオが登録・削除されていた場合も、最新の並びに戻る）。
   *
   * @param index 移動するポートフォリオの一覧内の位置
   * @param direction 移動する方向
   */
  const movePortfolio = async (index: number, direction: "up" | "down") => {
    const targetIndex = direction === "up" ? index - 1 : index + 1;
    if (isFiltered || isSavingOrder || targetIndex < 0 || targetIndex >= portfolios.length) {
      return;
    }
    const reordered = [...portfolios];
    [reordered[index], reordered[targetIndex]] = [reordered[targetIndex], reordered[index]];
    const moved = portfolios[index];

    setPortfolios(reordered);
    setIsSavingOrder(true);
    setOrderMessage(null);
    pendingFocusRef.current = `${moved.portfolioNo}-${direction}`;
    try {
      await updatePortfolioDisplayOrder(
        accountId,
        reordered.map((portfolio) => portfolio.portfolioNo)
      );
      setOrderMessage({ text: `「${moved.theme}」の並び順を変更しました`, isError: false });
    } catch (err) {
      setOrderMessage({
        text: err instanceof Error ? err.message : "並び順の更新に失敗しました",
        isError: true,
      });
      await loadList(appliedFilter);
    } finally {
      setIsSavingOrder(false);
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

      {isFiltered && portfolios.length > 0 && (
        <p
          id="portfolio-order-disabled-message"
          className="w-full max-w-[800px] px-4 text-sm text-gray-700"
        >
          絞り込み中は並び替えできません。並び替えるには条件をクリアしてください
        </p>
      )}
      <p
        role="status"
        className={`w-full max-w-[800px] px-4 text-sm ${
          orderMessage?.isError ? "text-red-700" : "text-gray-700"
        }`}
      >
        {orderMessage?.text}
      </p>

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
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  公開ページ
                </th>
                <th className="py-3 px-4 text-left text-white font-bold border border-gray-300">
                  並び順
                </th>
              </tr>
            </thead>
            <tbody>
              {portfolios.map((portfolio, index) => (
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
                  <td className="py-3 px-4 border border-gray-300">
                    {/* 公開ページは公開中のポートフォリオだけが閲覧できるため、公開中の行にだけリンクを載せる */}
                    {portfolio.statusKbn === "published" ? (
                      <Link
                        href={`/portfolio/${accountId}/${portfolio.portfolioNo}`}
                        target="_blank"
                        rel="noopener"
                        aria-label={`${portfolio.theme}の公開ページ（新しいタブで開く）`}
                        className="text-blue-700 hover:underline"
                      >
                        表示
                      </Link>
                    ) : (
                      <span className="text-gray-600">-</span>
                    )}
                  </td>
                  <td className="py-3 px-4 border border-gray-300">
                    <div className="flex gap-1">
                      {(["up", "down"] as const).map((direction) => (
                        <button
                          key={direction}
                          type="button"
                          ref={(element) => {
                            const key = `${portfolio.portfolioNo}-${direction}`;
                            if (element) {
                              moveButtonRefs.current.set(key, element);
                            } else {
                              moveButtonRefs.current.delete(key);
                            }
                          }}
                          onClick={() => movePortfolio(index, direction)}
                          disabled={
                            isFiltered ||
                            isSavingOrder ||
                            (direction === "up" ? index === 0 : index === portfolios.length - 1)
                          }
                          aria-label={`${portfolio.theme}を${direction === "up" ? "上" : "下"}へ移動`}
                          aria-describedby={isFiltered ? "portfolio-order-disabled-message" : undefined}
                          className="w-8 h-8 border border-gray-400 rounded hover:bg-gray-50 disabled:opacity-40 disabled:hover:bg-transparent"
                        >
                          <span aria-hidden="true">{direction === "up" ? "↑" : "↓"}</span>
                        </button>
                      ))}
                    </div>
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
