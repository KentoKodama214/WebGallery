"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPhotoList,
  addFavorite,
  deleteFavorite,
  type PhotoListItem,
  type PhotoListParams,
} from "@/lib/api/client";
import styles from "./PhotoList.module.css";

interface PhotoListProps {
  photoAccountId: string;
}

/**
 * 写真一覧コンポーネント
 * フィルター・グリッド表示・ページネーションを提供
 */
export function PhotoList({ photoAccountId }: PhotoListProps) {
  const { isAuthenticated, user } = useAuth();
  const [photos, setPhotos] = useState<PhotoListItem[]>([]);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pageNo, setPageNo] = useState(1);

  // フィルター状態
  const [directionKbn, setDirectionKbn] = useState("");
  const [isFavoriteFilter, setIsFavoriteFilter] = useState("");
  const [tagList, setTagList] = useState("");
  const [sortBy, setSortBy] = useState("photoAt");
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const galleryRef = useRef<HTMLDivElement>(null);
  const lightboxRef = useRef<InstanceType<typeof import("photoswipe/lightbox").default> | null>(null);
  const photosRef = useRef<PhotoListItem[]>(photos);
  photosRef.current = photos;

  const isOwner = user?.accountId === photoAccountId;

  const buildParams = (page: number): PhotoListParams => ({
    directionKbn: directionKbn || undefined,
    isFavorite: isFavoriteFilter || undefined,
    tagList: tagList || undefined,
    sortBy: sortBy || undefined,
    pageNo: page,
  });

  const fetchPhotos = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getPhotoList(photoAccountId, { pageNo: 1 });
      setPhotos(data.photoList);
      setIsLast(data.isLast);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoading(false);
    }
  }, [photoAccountId]);

  useEffect(() => {
    fetchPhotos();
  }, [fetchPhotos]);

  // PhotoSwipe初期化（photos変更時に再初期化）
  useEffect(() => {
    if (!galleryRef.current || photos.length === 0) return;

    let lightbox: InstanceType<typeof import("photoswipe/lightbox").default> | null = null;

    const initPhotoSwipe = async () => {
      const { default: PhotoSwipeLightbox } = await import("photoswipe/lightbox");
      await import("photoswipe/style.css");

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
              }).catch(() => {});
            },
          });

          // お気に入り解除ボタン
          lightbox!.pswp!.ui!.registerElement({
            name: "cancel-favorite-button",
            order: 9,
            isButton: true,
            html: "",
            onInit: (el) => {
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
              }).catch(() => {});
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
            lightbox!.pswp!.on("change", () => {
              const currSlideElement = lightbox!.pswp!.currSlide!.data.element;
              let captionHTML = "";
              if (currSlideElement) {
                const hiddenCaption = currSlideElement.querySelector(".hidden-caption-content");
                if (hiddenCaption) {
                  captionHTML = hiddenCaption.innerHTML;
                } else {
                  const img = currSlideElement.querySelector("img");
                  captionHTML = img?.getAttribute("alt") || "";
                }
              }
              el.innerHTML = captionHTML || "";
            });
          },
        });
      });

      lightbox.init();
      lightboxRef.current = lightbox;
    };

    initPhotoSwipe();

    return () => {
      if (lightbox) {
        lightbox.destroy();
        lightbox = null;
      }
      lightboxRef.current = null;
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [photos.length, isAuthenticated]);

  const handleLoadMore = async () => {
    const nextPage = pageNo + 1;
    setIsLoadingMore(true);
    try {
      const data = await getPhotoList(photoAccountId, buildParams(nextPage));
      setPhotos((prev) => [...prev, ...data.photoList]);
      setIsLast(data.isLast);
      setPageNo(nextPage);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoadingMore(false);
    }
  };

  const handleFilter = async () => {
    setIsFilterOpen(false);
    setIsLoading(true);
    setError(null);
    setPageNo(1);
    try {
      const data = await getPhotoList(photoAccountId, buildParams(1));
      setPhotos(data.photoList);
      setIsLast(data.isLast);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleFavorite = async (e: React.MouseEvent, photo: PhotoListItem) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      if (photo.isFavorite) {
        await deleteFavorite(photo.accountNo, photo.photoNo);
      } else {
        await addFavorite(photo.accountNo, photo.photoNo);
      }
      setPhotos((prev) =>
        prev.map((p) =>
          p.accountNo === photo.accountNo && p.photoNo === photo.photoNo
            ? { ...p, isFavorite: !p.isFavorite }
            : p
        )
      );
    } catch {
      // エラー時は何もしない
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

  // フィルターテキスト生成
  const buildFilterText = () => {
    const parts: string[] = [];
    if (directionKbn === "vertical") parts.push("縦写真");
    if (directionKbn === "horizontal") parts.push("横写真");
    if (isFavoriteFilter === "true") parts.push("お気に入り写真のみ");
    if (tagList) parts.push(tagList);
    if (sortBy === "photoAt") parts.push("撮影日順");
    if (sortBy === "favorite") parts.push("お気に入り数順");
    if (sortBy === "season") parts.push("季節・時期順");
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
          onClick={() => setIsFilterOpen(false)}
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
          data-testid="filter-trigger"
        >
          <span>
            <img
              className={styles.filterIconImg}
              src="/image/filter.png"
              alt="フィルター"
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
            {photos.map((photo) => (
              <div
                key={`${photo.accountNo}-${photo.photoNo}`}
                className={`${styles.photo} group`}
              >
                <div className="pswp-gallery__item">
                  <a
                    href={photo.imageFilePath}
                    data-pswp-width="1600"
                    data-pswp-height="1200"
                    target="_blank"
                    rel="noreferrer"
                  >
                    <img
                      src={photo.imageFilePath}
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
                  <img
                    className="opacity-0 group-hover:opacity-100 transition-opacity"
                    src={photo.isFavorite ? "/image/heart_on.png" : "/image/heart_off.png"}
                    alt={photo.isFavorite ? "お気に入り" : "お気に入りではない"}
                    onClick={(e) => handleToggleFavorite(e, photo)}
                    style={{
                      position: "absolute",
                      bottom: "8px",
                      right: "8px",
                      width: "25px",
                      height: "25px",
                      cursor: "pointer",
                    }}
                  />
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* もっと見るボタン */}
      {!isLoading && !error && !isLast && (
        <div className={styles.showMore}>
          <span
            className={styles.showMoreText}
            onClick={isLoadingMore ? undefined : handleLoadMore}
          >
            {isLoadingMore ? "読み込み中..." : "+もっと見る"}
          </span>
        </div>
      )}

      {/* 写真追加ボタン（オーナーのみ） */}
      {isOwner && (
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
