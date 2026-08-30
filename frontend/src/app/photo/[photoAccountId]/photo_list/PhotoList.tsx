"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPhotoList,
  getPhotoUpperLimit,
  addFavorite,
  deleteFavorite,
  type PhotoListItem,
  type PhotoListParams,
} from "@/lib/api/client";
import { getCookie, setCookie } from "@/lib/cookie";
import { onActivateKey } from "@/lib/a11y";
import { sanitizeImageUrl } from "@/lib/url";
import styles from "./PhotoList.module.css";

const COOKIE_MAX_AGE = 1800; // 30分

interface PhotoListFilter {
  directionKbn: string;
  isFavoriteFilter: string;
  tagList: string;
  sortBy: string;
}

const DEFAULT_FILTER: PhotoListFilter = {
  directionKbn: "",
  isFavoriteFilter: "",
  tagList: "",
  sortBy: "photoAt",
};

/**
 * フィルター条件 Cookie 名（ギャラリー単位でスコープする）
 *
 * 単一キーで全ギャラリー・全ユーザー共有にすると、別ユーザーのギャラリー閲覧時に
 * 前回条件が漏れて適用されるため、`photoAccountId` を含めて分離する。
 * `photoAccountId` は半角英数字（バリデーション済み）なので Cookie 名として安全。
 */
function filterCookieName(photoAccountId: string): string {
  return `photoListFilter_${photoAccountId}`;
}

/**
 * Cookieに保存されたフィルター条件を読み込む
 *
 * @param photoAccountId 対象ギャラリーのアカウントID
 * @returns 保存済みのフィルター条件。無ければ既定値
 */
function readStoredFilter(photoAccountId: string): PhotoListFilter {
  if (typeof document === "undefined") return DEFAULT_FILTER;
  const raw = getCookie(filterCookieName(photoAccountId));
  if (!raw) return DEFAULT_FILTER;
  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    // 各キーは文字列のときだけ採用する（細工・破損した cookie で
    // select の value がオブジェクトになる等の破綻を防ぐ）
    const pick = (key: keyof PhotoListFilter): string =>
      typeof parsed[key] === "string"
        ? (parsed[key] as string)
        : DEFAULT_FILTER[key];
    return {
      directionKbn: pick("directionKbn"),
      isFavoriteFilter: pick("isFavoriteFilter"),
      tagList: pick("tagList"),
      sortBy: pick("sortBy"),
    };
  } catch {
    return DEFAULT_FILTER;
  }
}

/**
 * 閲覧者の権限で許可されないフィルター条件を取り除く
 *
 * - お気に入り絞り込みは認証済みユーザーのみ
 * - 向き（縦/横）絞り込みはギャラリー所有者のみ
 *
 * Cookie に保存済みの条件が、ログアウト後や他人のギャラリー閲覧時に
 * そのままリクエストへ付与されるのを防ぐ。
 */
function sanitizeFilterForViewer(
  filter: PhotoListFilter,
  opts: { isAuthenticated: boolean; isOwner: boolean }
): PhotoListFilter {
  return {
    ...filter,
    isFavoriteFilter: opts.isAuthenticated ? filter.isFavoriteFilter : "",
    directionKbn: opts.isOwner ? filter.directionKbn : "",
  };
}

/** お気に入り操作の進行中管理に使うキー */
function favoriteKey(accountNo: number, photoNo: number): string {
  return `${accountNo}-${photoNo}`;
}

interface PhotoListProps {
  photoAccountId: string;
}

/**
 * 写真一覧コンポーネント
 * フィルター・グリッド表示・ページネーションを提供
 */
export function PhotoList({ photoAccountId }: PhotoListProps) {
  const { isAuthenticated, user, isLoading: authLoading } = useAuth();
  // マウント時に一度だけ Cookie を読む（従来は 5 回パースしていた）
  const initialFilter = useMemo(
    () => readStoredFilter(photoAccountId),
    [photoAccountId]
  );
  const [photos, setPhotos] = useState<PhotoListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  // 写真追加ボタンの表示可否。上限チェックが成功し、かつ上限未達のときだけ true。
  // （取得失敗時は誤ったボタンを見せないよう false のままにする）
  const [canAddPhoto, setCanAddPhoto] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // 操作失敗（お気に入り更新等）の通知。一覧表示は維持したまま表示する
  const [actionError, setActionError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);

  // フィルター編集状態（パネル内の入力値。初期値はCookieから復元）
  const [directionKbn, setDirectionKbn] = useState(initialFilter.directionKbn);
  const [isFavoriteFilter, setIsFavoriteFilter] = useState(
    initialFilter.isFavoriteFilter
  );
  const [tagList, setTagList] = useState(initialFilter.tagList);
  const [sortBy, setSortBy] = useState(initialFilter.sortBy);
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  // 実際に一覧へ適用されているフィルター条件（「絞り込み」実行時に確定する）。
  // ページネーション（+もっと見る）は編集中の値ではなくこちらを使う。
  const [appliedFilter, setAppliedFilter] =
    useState<PhotoListFilter>(initialFilter);

  const galleryRef = useRef<HTMLDivElement>(null);
  const lightboxRef = useRef<InstanceType<typeof import("photoswipe/lightbox").default> | null>(null);
  const photosRef = useRef<PhotoListItem[]>(photos);
  // 一覧取得リクエストの世代。初期ロード・絞り込み・もっと見るは開始時に
  // 採番し、自分が最新でなければ結果を破棄する（後着の初期ロードが
  // 絞り込み結果やページ追加を上書きする競合を防ぐ）
  const loadSeqRef = useRef(0);
  // 「+もっと見る」の再入防止（isLoadingMore の setState 反映前の連打対策）
  const isLoadingMoreRef = useRef(false);
  // お気に入り操作の多重実行防止（(accountNo-photoNo) 単位で進行中を管理）
  const favoriteInFlightRef = useRef<Set<string>>(new Set());

  // お気に入りボタン等のコールバックから最新のphotosを参照できるようにする
  useEffect(() => {
    photosRef.current = photos;
  }, [photos]);

  const isOwner = user?.accountId === photoAccountId;
  const hasPhotos = photos.length > 0;

  const buildParams = (
    filter: PhotoListFilter,
    page: number
  ): PhotoListParams => {
    // リクエスト直前に、閲覧者権限で許可されない条件を最終的に取り除く（多層防御）
    const safe = sanitizeFilterForViewer(filter, { isAuthenticated, isOwner });
    return {
      directionKbn: safe.directionKbn || undefined,
      isFavorite: safe.isFavoriteFilter || undefined,
      tagList: safe.tagList || undefined,
      sortBy: safe.sortBy || undefined,
      pageNo: page,
    };
  };

  /**
   * フィルターパネルの編集値を適用済みの値に戻す（絞り込みを実行せず閉じる場合）
   */
  const resetFilterEdits = () => {
    setDirectionKbn(appliedFilter.directionKbn);
    setIsFavoriteFilter(appliedFilter.isFavoriteFilter);
    setTagList(appliedFilter.tagList);
    setSortBy(appliedFilter.sortBy);
  };

  /**
   * フィルター条件をCookieに保存する
   */
  const saveFilterToCookie = useCallback(
    (filter: PhotoListFilter) => {
      setCookie(
        filterCookieName(photoAccountId),
        JSON.stringify(filter),
        COOKIE_MAX_AGE
      );
    },
    [photoAccountId]
  );

  /**
   * 写真一覧取得（初期化時）
   * Cookieから復元したフィルター条件でAPIを呼び出す
   */
  useEffect(() => {
    // 認証状態が確定してから読み込む（未確定のまま実行すると、閲覧者権限に
    // 応じたフィルターのサニタイズ・お気に入り状態の判定が正しく行えない）
    if (authLoading) return;

    let cancelled = false;

    // 保存済み条件のうち、現在の閲覧者権限で許可されないものを取り除く
    const filter = sanitizeFilterForViewer(readStoredFilter(photoAccountId), {
      isAuthenticated,
      isOwner,
    });
    // サニタイズ後の条件で Cookie を上書きし、有効期限もリセットする
    saveFilterToCookie(filter);

    const params: PhotoListParams = {
      directionKbn: filter.directionKbn || undefined,
      isFavorite: filter.isFavoriteFilter || undefined,
      tagList: filter.tagList || undefined,
      sortBy: filter.sortBy || undefined,
      pageNo: 1,
    };

    const seq = ++loadSeqRef.current;
    const load = async () => {
      try {
        const data = await getPhotoList(photoAccountId, params);
        if (cancelled || loadSeqRef.current !== seq) return;
        setPhotos(data.photoList);
        setIsLast(data.isLast);
        setPageNo(1);
        setAppliedFilter(filter);
        setError(null);
      } catch (err) {
        if (!cancelled && loadSeqRef.current === seq) {
          setError(err instanceof Error ? err.message : "エラーが発生しました");
        }
      } finally {
        if (!cancelled && loadSeqRef.current === seq) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [photoAccountId, saveFilterToCookie, authLoading, isAuthenticated, isOwner]);

  useEffect(() => {
    if (!isOwner) return;
    let cancelled = false;
    getPhotoUpperLimit(photoAccountId)
      .then((data) => {
        if (!cancelled) setCanAddPhoto(!data.isReachedUpperLimit);
      })
      .catch(() => {
        if (!cancelled) setCanAddPhoto(false);
      });
    return () => {
      cancelled = true;
    };
  }, [isOwner, photoAccountId]);

  // PhotoSwipe初期化
  // ライトボックスは開くたびにギャラリーDOMを読み直すため、写真の増減では
  // 再初期化せず、写真の有無と認証状態が変わったときだけ作り直す
  useEffect(() => {
    if (!galleryRef.current || !hasPhotos) return;

    let cancelled = false;
    let lightbox: InstanceType<typeof import("photoswipe/lightbox").default> | null = null;

    const initPhotoSwipe = async () => {
      const { default: PhotoSwipeLightbox } = await import("photoswipe/lightbox");
      await import("photoswipe/style.css");
      // 非同期import中にアンマウント／依存変更でcleanupが走った場合は中断する
      // （同期cleanupがlightbox未生成のまま終わり、孤立インスタンスが残るのを防ぐ）
      if (cancelled || !galleryRef.current) return;

      lightbox = new PhotoSwipeLightbox({
        gallery: galleryRef.current!,
        children: ".pswp-gallery__item",
        pswpModule: () => import("photoswipe"),
        padding: { top: 60, bottom: 90, left: 60, right: 60 },
      });

      lightbox.on("uiRegister", () => {
        // お気に入りボタン（認証済みのみ）
        if (isAuthenticated) {
          // お気に入り追加ボタン
          lightbox!.pswp!.ui!.registerElement({
            name: "add-favorite-button",
            order: 9,
            isButton: true,
            html: "",
            onInit: (el) => {
              el.setAttribute("aria-label", "お気に入りに追加");
              el.setAttribute("title", "お気に入りに追加");
              lightbox!.pswp!.on("change", () => {
                const idx = lightbox!.pswp!.currIndex;
                const photo = photosRef.current[idx];
                el.style.display = photo?.isFavorite ? "none" : "block";
              });
            },
            onClick: () => {
              const idx = lightbox!.pswp!.currIndex;
              const photo = photosRef.current[idx];
              if (!photo || photo.isFavorite) return;
              const key = favoriteKey(photo.accountNo, photo.photoNo);
              if (favoriteInFlightRef.current.has(key)) return;
              favoriteInFlightRef.current.add(key);
              addFavorite(photo.accountNo, photo.photoNo).then(() => {
                setPhotos((prev) =>
                  prev.map((p) =>
                    p.accountNo === photo.accountNo && p.photoNo === photo.photoNo
                      ? { ...p, isFavorite: true }
                      : p
                  )
                );
                const addBtn = document.querySelector(".pswp__button--add-favorite-button") as HTMLElement | null;
                const cancelBtn = document.querySelector(".pswp__button--cancel-favorite-button") as HTMLElement | null;
                if (addBtn) addBtn.style.display = "none";
                if (cancelBtn) cancelBtn.style.display = "block";
              }).catch(() => {
                setActionError("お気に入りの更新に失敗しました");
              }).finally(() => {
                favoriteInFlightRef.current.delete(key);
              });
            },
          });

          // お気に入り解除ボタン
          lightbox!.pswp!.ui!.registerElement({
            name: "cancel-favorite-button",
            order: 9,
            isButton: true,
            html: "",
            onInit: (el) => {
              el.setAttribute("aria-label", "お気に入りから外す");
              el.setAttribute("title", "お気に入りから外す");
              lightbox!.pswp!.on("change", () => {
                const idx = lightbox!.pswp!.currIndex;
                const photo = photosRef.current[idx];
                el.style.display = photo?.isFavorite ? "block" : "none";
              });
            },
            onClick: () => {
              const idx = lightbox!.pswp!.currIndex;
              const photo = photosRef.current[idx];
              if (!photo || !photo.isFavorite) return;
              const key = favoriteKey(photo.accountNo, photo.photoNo);
              if (favoriteInFlightRef.current.has(key)) return;
              favoriteInFlightRef.current.add(key);
              deleteFavorite(photo.accountNo, photo.photoNo).then(() => {
                setPhotos((prev) =>
                  prev.map((p) =>
                    p.accountNo === photo.accountNo && p.photoNo === photo.photoNo
                      ? { ...p, isFavorite: false }
                      : p
                  )
                );
                const addBtn = document.querySelector(".pswp__button--add-favorite-button") as HTMLElement | null;
                const cancelBtn = document.querySelector(".pswp__button--cancel-favorite-button") as HTMLElement | null;
                if (addBtn) addBtn.style.display = "block";
                if (cancelBtn) cancelBtn.style.display = "none";
              }).catch(() => {
                setActionError("お気に入りの更新に失敗しました");
              }).finally(() => {
                favoriteInFlightRef.current.delete(key);
              });
            },
          });
        }

        // キャプション
        lightbox!.pswp!.ui!.registerElement({
          name: "custom-caption",
          order: 10,
          isButton: false,
          appendTo: "root",
          html: "Caption text",
          onInit: (el) => {
            el.style.cursor = "pointer";
            el.addEventListener("click", (e) => {
              // 既存のリンククリックはそのまま動作させる
              if ((e.target as HTMLElement).closest("a")) return;
              const detailLink = el.querySelector(".show_detail a") as HTMLAnchorElement | null;
              if (detailLink) {
                detailLink.click();
              }
            });
            lightbox!.pswp!.on("change", () => {
              const currSlideElement = lightbox!.pswp!.currSlide!.data.element;
              // innerHTML代入を避け、DOMノードの複製またはテキストで差し込む（XSS対策）
              el.replaceChildren();
              if (!currSlideElement) return;
              const hiddenCaption = currSlideElement.querySelector(
                ".hidden-caption-content"
              );
              if (hiddenCaption) {
                const clone = hiddenCaption.cloneNode(true) as HTMLElement;
                el.append(...Array.from(clone.childNodes));
              } else {
                const img = currSlideElement.querySelector("img");
                el.textContent = img?.getAttribute("alt") || "";
              }
            });
          },
        });
      });

      lightbox.init();
      lightboxRef.current = lightbox;
    };

    initPhotoSwipe();

    return () => {
      cancelled = true;
      if (lightbox) {
        lightbox.destroy();
        lightbox = null;
      }
      lightboxRef.current = null;
    };
  }, [hasPhotos, isAuthenticated]);

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
    setActionError(null);
    try {
      // 編集中の値ではなく、適用済みフィルターでページを取得する
      const data = await getPhotoList(
        photoAccountId,
        buildParams(appliedFilter, nextPage)
      );
      if (loadSeqRef.current !== seq) return;
      setPhotos((prev) => [...prev, ...data.photoList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      // 追加読み込みの失敗では取得済みの一覧を維持し、通知だけ表示する
      setActionError(
        err instanceof Error ? err.message : "エラーが発生しました"
      );
    } finally {
      isLoadingMoreRef.current = false;
      if (loadSeqRef.current === seq) setIsLoadingMore(false);
    }
  };

  /**
   * 絞り込み
   */
  const handleFilter = async () => {
    const seq = ++loadSeqRef.current;
    setIsFilterOpen(false);
    setIsLoading(true);
    setError(null);
    setActionError(null);
    setPageNo(1);

    const nextFilter: PhotoListFilter = {
      directionKbn,
      isFavoriteFilter,
      tagList,
      sortBy,
    };
    setAppliedFilter(nextFilter);
    // フィルター条件をCookieに保存
    saveFilterToCookie(nextFilter);

    try {
      const data = await getPhotoList(
        photoAccountId,
        buildParams(nextFilter, 1)
      );
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

  /**
   * お気に入り登録／解除
   */
  const handleToggleFavorite = async (e: React.MouseEvent, photo: PhotoListItem) => {
    e.preventDefault();
    e.stopPropagation();
    const key = favoriteKey(photo.accountNo, photo.photoNo);
    // 同一写真への操作が進行中なら無視する（連打による重複リクエスト・
    // 楽観更新の二重反転で UI とサーバ状態が食い違うのを防ぐ）
    if (favoriteInFlightRef.current.has(key)) return;
    favoriteInFlightRef.current.add(key);
    setActionError(null);
    // 相対トグルではなく、実行するアクションに対応した絶対値で更新する
    const nextFavorite = !photo.isFavorite;
    try {
      if (photo.isFavorite) {
        await deleteFavorite(photo.accountNo, photo.photoNo);
      } else {
        await addFavorite(photo.accountNo, photo.photoNo);
      }
      setPhotos((prev) =>
        prev.map((p) =>
          p.accountNo === photo.accountNo && p.photoNo === photo.photoNo
            ? { ...p, isFavorite: nextFavorite }
            : p
        )
      );
    } catch (err) {
      setActionError(
        err instanceof Error ? err.message : "お気に入りの更新に失敗しました"
      );
    } finally {
      favoriteInFlightRef.current.delete(key);
    }
  };

  // 画像の自然サイズを取得してdata属性に設定
  const handleImageLoad = (e: React.SyntheticEvent<HTMLImageElement>) => {
    const img = e.currentTarget;
    const anchor = img.closest("a");
    if (anchor) {
      anchor.setAttribute("data-pswp-width", String(img.naturalWidth));
      anchor.setAttribute("data-pswp-height", String(img.naturalHeight));
    }
  };

  // フィルターテキスト生成（一覧に適用されている条件を表示する）
  const buildFilterText = () => {
    const parts: string[] = [];
    if (appliedFilter.directionKbn === "vertical") parts.push("縦写真");
    if (appliedFilter.directionKbn === "horizontal") parts.push("横写真");
    if (appliedFilter.isFavoriteFilter === "true") parts.push("お気に入り写真のみ");
    if (appliedFilter.tagList) parts.push(appliedFilter.tagList);
    if (appliedFilter.sortBy === "photoAt") parts.push("撮影日順");
    if (appliedFilter.sortBy === "favorite") parts.push("お気に入り数順");
    if (appliedFilter.sortBy === "season") parts.push("季節・時期順");
    return parts.length > 0 ? parts.join(", ") : "";
  };

  return (
    <div style={{ backgroundColor: "black", minHeight: "100vh" }}>
      {/* フィルターオーバーレイ */}
      <div
        className={`${styles.filterOverlay} ${isFilterOpen ? styles.filterOpen : ""}`}
        data-testid="filter-panel"
      >
        {/* 閉じるボタン（X形） */}
        <div
          className={styles.filterCloseButton}
          onClick={() => {
            resetFilterEdits();
            setIsFilterOpen(false);
          }}
          onKeyDown={onActivateKey(() => {
            resetFilterEdits();
            setIsFilterOpen(false);
          })}
          role="button"
          tabIndex={0}
          aria-label="フィルターを閉じる"
          data-testid="filter-close-button"
        >
          <span></span>
          <span></span>
          <span></span>
        </div>

        {/* フィルターフォーム */}
        <div className={styles.filterForm}>
          {/* 向き（オーナーのみ） */}
          {isOwner && (
            <select
              value={directionKbn}
              onChange={(e) => setDirectionKbn(e.target.value)}
            >
              <option value=""></option>
              <option value="vertical">縦写真</option>
              <option value="horizontal">横写真</option>
            </select>
          )}

          {/* お気に入り（認証済みのみ） */}
          {isAuthenticated && (
            <select
              value={isFavoriteFilter}
              onChange={(e) => setIsFavoriteFilter(e.target.value)}
            >
              <option value=""></option>
              <option value="true">お気に入り写真のみ</option>
            </select>
          )}

          {/* タグ検索 */}
          <input
            type="text"
            value={tagList}
            onChange={(e) => setTagList(e.target.value)}
            placeholder="キーワードを入力"
          />

          {/* 並び順 */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="photoAt">撮影日順</option>
            <option value="favorite">お気に入り数順</option>
            <option value="season">季節・時期順</option>
          </select>

          {/* 絞り込みボタン */}
          <button
            className={styles.filterSubmitButton}
            onClick={handleFilter}
          >
            絞り込み
          </button>
        </div>
      </div>

      {/* 写真コンテナ */}
      <div className={styles.photosContainer}>
        {/* フィルタートリガー */}
        <div
          className={styles.filterTrigger}
          onClick={() => setIsFilterOpen(true)}
          onKeyDown={onActivateKey(() => setIsFilterOpen(true))}
          role="button"
          tabIndex={0}
          aria-label="フィルターを開く"
          aria-expanded={isFilterOpen}
          data-testid="filter-trigger"
        >
          <span>
            <img
              className={styles.filterIconImg}
              src="/image/filter.png"
              alt=""
            />
          </span>
          <span className={styles.filterText}>{buildFilterText()}</span>
        </div>

        {/* ローディング */}
        {isLoading && (
          <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
            <p style={{ color: "#9ca3af" }}>読み込み中...</p>
          </div>
        )}

        {/* エラー */}
        {error && (
          <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
            <p style={{ color: "#ef4444" }}>{error}</p>
          </div>
        )}

        {/* 操作失敗の通知（一覧表示は維持したまま表示する） */}
        {actionError && (
          <div
            role="alert"
            style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: "12px", padding: "8px", color: "#ef4444" }}
          >
            <span>{actionError}</span>
            <button
              type="button"
              onClick={() => setActionError(null)}
              aria-label="閉じる"
              style={{ background: "none", border: "none", color: "inherit", cursor: "pointer", fontSize: "16px", lineHeight: 1, padding: 0 }}
            >
              &times;
            </button>
          </div>
        )}

        {/* 写真が0件 */}
        {!isLoading && !error && photos.length === 0 && (
          <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
            <p style={{ color: "#9ca3af" }}>写真がありません</p>
          </div>
        )}

        {/* 写真グリッド */}
        {!isLoading && !error && photos.length > 0 && (
          <div
            ref={galleryRef}
            style={{ display: "flex", flexWrap: "wrap", justifyContent: "flex-start", gap: "10px", margin: "0 auto" }}
          >
            {photos.map((photo) => {
              const imageSrc = sanitizeImageUrl(photo.imageFilePath);
              return (
              <div
                key={`${photo.accountNo}-${photo.photoNo}`}
                className={`${styles.photo} group`}
              >
                <div className="pswp-gallery__item">
                  <a
                    href={imageSrc}
                    data-pswp-width="1600"
                    data-pswp-height="1200"
                    target="_blank"
                    rel="noreferrer"
                  >
                    <img
                      src={imageSrc}
                      alt={photo.caption || "写真"}
                      className={styles.picture}
                      onLoad={handleImageLoad}
                    />
                  </a>
                  <div className="hidden-caption-content" style={{ display: "none" }}>
                    <p className="caption_content">{photo.caption || ""}</p>
                    <p className="show_detail">
                      <a href={`/photo/${photoAccountId}/photo_detail?accountNo=${photo.accountNo}&photoNo=${photo.photoNo}`}>
                        詳細
                      </a>
                    </p>
                  </div>
                </div>
                {/* お気に入りアイコン（認証済みのみ、ホバー時に右下表示） */}
                {isAuthenticated && (
                  <button
                    type="button"
                    className="opacity-0 group-hover:opacity-100 transition-opacity"
                    aria-label={photo.isFavorite ? "お気に入りから外す" : "お気に入りに追加"}
                    aria-pressed={photo.isFavorite}
                    onClick={(e) => handleToggleFavorite(e, photo)}
                    style={{
                      position: "absolute",
                      bottom: "8px",
                      right: "8px",
                      width: "25px",
                      height: "25px",
                      cursor: "pointer",
                      padding: 0,
                      border: "none",
                      background: "none",
                    }}
                  >
                    <img
                      src={photo.isFavorite ? "/image/heart_on.png" : "/image/heart_off.png"}
                      alt=""
                      style={{ width: "100%", height: "100%", display: "block" }}
                    />
                  </button>
                )}
              </div>
              );
            })}
          </div>
        )}
      </div>

      {/* もっと見るボタン */}
      {!isLoading && !error && !isLast && (
        <div className={styles.showMore}>
          <button
            type="button"
            className={styles.showMoreText}
            onClick={handleLoadMore}
            disabled={isLoadingMore}
            style={{ background: "none", border: "none", color: "inherit", font: "inherit" }}
          >
            {isLoadingMore ? "読み込み中..." : "+もっと見る"}
          </button>
        </div>
      )}

      {/* 写真追加ボタン（オーナーのみ、上限チェック成功かつ上限未達の場合） */}
      {isOwner && canAddPhoto && (
        <Link
          href={`/photo/${photoAccountId}/photo_setting`}
          className={styles.photoSettingButton}
        >
          ＋写真追加
        </Link>
      )}
    </div>
  );
}
