"use client";

import { useEffect, useRef, useState } from "react";
import { ModalDialog } from "@/components/ui/ModalDialog";
import {
  searchPortfolioPhotos,
  type PortfolioPhotoCandidate,
  type PortfolioPhotoSearchFilter,
} from "@/lib/api/client";
import { sanitizeImageUrl } from "@/lib/url";
import { PORTFOLIO_PHOTO_MAX_SIZE } from "@/lib/validation";

/** 写真選択ダイアログで選んだ写真 */
export interface PickedPhoto {
  /** 写真番号 */
  photoNo: number;
  /** 画像URL（署名付きURL） */
  imageFilePath: string;
  /** 一覧・プレビューで表示する写真の名前 */
  label: string;
  /** 向き区分（vertical / horizontal / square / none） */
  directionKbn: string;
}

interface PhotoPickerDialogProps {
  /** 写真の所有者（＝ログイン中の本人）のアカウントID */
  accountId: string;
  /** ダイアログを開いた時点で選択済みの写真（表示順） */
  initialSelected: PickedPhoto[];
  /** 「完了」で、選択した写真（表示順）を確定する */
  onComplete: (selected: PickedPhoto[]) => void;
  /** 「キャンセル」・Escapeで、選択を変更せずに閉じる */
  onCancel: () => void;
}

/** 向き区分ごとの写真の枠の縦横比 */
const DIRECTION_ASPECT_RATIOS: Record<string, string> = {
  vertical: "3 / 4",
  horizontal: "4 / 3",
  square: "1 / 1",
};

/** 向きが未設定の写真の枠の縦横比 */
const DEFAULT_ASPECT_RATIO = "1 / 1";

/** 検索語の最大文字数（写真のタイトル・撮影場所の名前の最大文字数と同じ） */
const KEYWORD_MAX_LENGTH = 100;

/**
 * 検索結果の写真を、選択済みの写真の形に変換する（タイトルが未入力の場合は写真番号を表示名にする）
 *
 * @param photo 検索結果の写真
 * @returns 選択した写真
 */
function toPickedPhoto(photo: PortfolioPhotoCandidate): PickedPhoto {
  return {
    photoNo: photo.photoNo,
    imageFilePath: photo.imageFilePath,
    label: photo.photoJapaneseTitle || `写真${photo.photoNo}`,
    directionKbn: photo.directionKbn,
  };
}

/**
 * 写真の支援技術向けの名前を求める（タイトルがある場合は写真番号に添える）
 *
 * @param photo 選択した写真
 * @returns 名前（例：写真3（金閣寺））
 */
function toAccessibleName(photo: PickedPhoto): string {
  const numbered = `写真${photo.photoNo}`;
  return photo.label === numbered ? numbered : `${numbered}（${photo.label}）`;
}

interface PhotoTileProps {
  /** 表示する写真 */
  photo: PickedPhoto;
  /** 支援技術向けの名前 */
  ariaLabel: string;
  /** 押せない状態か */
  disabled?: boolean;
  /** 右上に表示するバッジ（選択済みの一覧での表示順） */
  badge?: number;
  /** 押したときの処理 */
  onClick: () => void;
}

/**
 * 写真1枚分のボタン（枠は向きに合わせた縦横比で表示する）
 */
function PhotoTile({ photo, ariaLabel, disabled, badge, onClick }: PhotoTileProps) {
  const src = sanitizeImageUrl(photo.imageFilePath);
  return (
    <button
      type="button"
      aria-label={ariaLabel}
      disabled={disabled}
      onClick={onClick}
      style={{ aspectRatio: DIRECTION_ASPECT_RATIOS[photo.directionKbn] ?? DEFAULT_ASPECT_RATIO }}
      className={`relative block h-32 overflow-hidden rounded border-4 bg-gray-200 disabled:opacity-40 ${
        badge !== undefined ? "border-blue-700" : "border-transparent hover:border-gray-400"
      }`}
    >
      {src ? (
        <img src={src} alt="" className="w-full h-full object-cover" />
      ) : (
        <span className="text-xs text-gray-600">{photo.label}</span>
      )}
      {badge !== undefined && (
        <span className="absolute top-1 right-1 bg-blue-700 text-white text-xs px-1.5 py-0.5 rounded">
          {badge}
        </span>
      )}
    </button>
  );
}

/**
 * ポートフォリオに設定する写真を、自分が登録した写真から選ぶダイアログ
 *
 * 上段に未選択の写真、下段に選択済みの写真（表示順）を分けて表示する。
 * 未選択の写真を押すと選択済みの末尾に追加し、選択済みの写真を押すと選択を解除する。
 * 未選択の写真は本人専用の写真検索APIをページ単位で取得して写真番号の降順に表示し、1つの検索語で
 * タイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致するものに絞り込める。
 * 写真の枠は向き（縦・横・正方形）に合わせた縦横比で表示する。
 * 選択はダイアログの中だけで保持し、「完了」で確定する（「キャンセル」では開く前の選択に戻る）。
 */
export function PhotoPickerDialog({
  accountId,
  initialSelected,
  onComplete,
  onCancel,
}: PhotoPickerDialogProps) {
  const [selected, setSelected] = useState<PickedPhoto[]>(initialSelected);
  const [photos, setPhotos] = useState<PortfolioPhotoCandidate[]>([]);
  const [pageNo, setPageNo] = useState(1);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 検索語の入力値と、検索に使った（検索ボタンで確定した）条件
  const [keywordInput, setKeywordInput] = useState("");
  const [appliedFilter, setAppliedFilter] = useState<PortfolioPhotoSearchFilter>({});
  // 取得リクエストの世代。後着レスポンスが一覧を上書きする競合を防ぐ
  const loadSeqRef = useRef(0);

  /**
   * 指定した条件・ページの写真を取得する（1ページ目は一覧を置き換え、それ以降は末尾に追加する）
   *
   * @param filter 絞り込み条件
   * @param nextPageNo ページ番号
   */
  const loadPage = async (filter: PortfolioPhotoSearchFilter, nextPageNo: number) => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    try {
      const data = await searchPortfolioPhotos(accountId, filter, nextPageNo);
      if (loadSeqRef.current !== seq) return;
      setPhotos((prev) => (nextPageNo === 1 ? data.photoList : [...prev, ...data.photoList]));
      setIsLast(data.isLast);
      setPageNo(nextPageNo);
      setAppliedFilter(filter);
    } catch (err) {
      if (loadSeqRef.current !== seq) return;
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      if (loadSeqRef.current === seq) setIsLoading(false);
    }
  };

  // 初回表示時に1ページ目を読み込む（状態の更新は応答受信後だけに行う）
  useEffect(() => {
    const seq = ++loadSeqRef.current;
    const load = async () => {
      try {
        const data = await searchPortfolioPhotos(accountId, {}, 1);
        if (loadSeqRef.current !== seq) return;
        setPhotos(data.photoList);
        setIsLast(data.isLast);
        setPageNo(1);
      } catch (err) {
        if (loadSeqRef.current !== seq) return;
        setError(err instanceof Error ? err.message : "エラーが発生しました");
      } finally {
        if (loadSeqRef.current === seq) setIsLoading(false);
      }
    };
    load();
    return () => {
      loadSeqRef.current++;
    };
  }, [accountId]);

  /** 入力した条件で検索し直す */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadPage({ keyword: keywordInput.trim() }, 1);
  };

  /**
   * 未選択の写真を選択済みの末尾に追加する
   *
   * @param photo 検索結果の写真
   */
  const select = (photo: PortfolioPhotoCandidate) => {
    setSelected((prev) =>
      prev.some((p) => p.photoNo === photo.photoNo) ? prev : [...prev, toPickedPhoto(photo)]
    );
  };

  /**
   * 選択済みの写真の選択を解除する
   *
   * @param photoNo 写真番号
   */
  const unselect = (photoNo: number) => {
    setSelected((prev) => prev.filter((p) => p.photoNo !== photoNo));
  };

  const selectedPhotoNos = new Set(selected.map((p) => p.photoNo));
  const unselectedPhotos = photos.filter((photo) => !selectedPhotoNos.has(photo.photoNo));
  const isFull = selected.length >= PORTFOLIO_PHOTO_MAX_SIZE;
  const isFiltered = !!appliedFilter.keyword;

  /** 未選択の一覧が空のときの案内 */
  const unselectedEmptyMessage =
    photos.length > 0
      ? "未選択の写真はありません"
      : isFiltered
        ? "条件に一致する写真がありません"
        : "登録済みの写真がありません";

  return (
    <ModalDialog
      onClose={onCancel}
      label="写真を選択"
      testId="photo-picker-dialog"
      overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
      containerClassName="bg-white rounded shadow-lg w-full max-w-[900px] max-h-[90vh] flex flex-col"
    >
      <div className="flex justify-between items-center p-4 border-b">
        <h2 className="text-lg font-bold">写真を選択</h2>
        <p className="text-sm text-gray-600">
          選択中 {selected.length} / {PORTFOLIO_PHOTO_MAX_SIZE}枚
        </p>
      </div>
      <form
        onSubmit={handleSearch}
        role="search"
        aria-label="写真の絞り込み"
        className="flex flex-wrap items-end gap-2 px-4 py-3 border-b"
      >
        <label className="flex flex-col text-xs font-bold flex-1 min-w-[200px]">
          検索語（タイトル・場所・住所・タグ）
          <input
            type="text"
            value={keywordInput}
            maxLength={KEYWORD_MAX_LENGTH}
            onChange={(e) => setKeywordInput(e.target.value)}
            className="border border-gray-300 rounded px-2 py-1 text-sm"
          />
        </label>
        <button
          type="submit"
          className="px-3 py-1 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
        >
          検索
        </button>
      </form>
      <section
        aria-labelledby="photo-picker-unselected-heading"
        data-testid="photo-picker-unselected"
        className="p-4 overflow-y-auto flex-1 min-h-[160px]"
      >
        <h3 id="photo-picker-unselected-heading" className="text-sm font-bold mb-2">
          未選択の写真
        </h3>
        {unselectedPhotos.length === 0 && !isLoading && !error && (
          <p className="text-gray-600 text-sm">{unselectedEmptyMessage}</p>
        )}
        <ul className="flex flex-wrap gap-2">
          {unselectedPhotos.map((photo) => {
            const picked = toPickedPhoto(photo);
            return (
              <li key={photo.photoNo} data-testid="photo-picker-item" data-direction={photo.directionKbn}>
                <PhotoTile
                  photo={picked}
                  ariaLabel={`${toAccessibleName(picked)}を選択`}
                  disabled={isFull}
                  onClick={() => select(photo)}
                />
              </li>
            );
          })}
        </ul>
        {isLoading && <p className="text-center py-4">読み込み中...</p>}
        <p role="alert" className="text-red-700 text-sm empty:sr-only">
          {error}
        </p>
        {!isLoading && !isLast && (
          <div className="flex justify-center pt-4">
            <button
              type="button"
              onClick={() => loadPage(appliedFilter, pageNo + 1)}
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
            >
              ＋もっと見る
            </button>
          </div>
        )}
      </section>
      <section
        aria-labelledby="photo-picker-selected-heading"
        data-testid="photo-picker-selected"
        className="p-4 border-t overflow-y-auto max-h-[35vh] bg-gray-50"
      >
        <h3 id="photo-picker-selected-heading" className="text-sm font-bold mb-2">
          選択済みの写真（表示順）
        </h3>
        {selected.length === 0 && <p className="text-gray-600 text-sm">選択済みの写真はありません</p>}
        <ul className="flex flex-wrap gap-2">
          {selected.map((photo, index) => (
            <li key={photo.photoNo} data-testid="photo-picker-selected-item" data-direction={photo.directionKbn}>
              <PhotoTile
                photo={photo}
                ariaLabel={`${toAccessibleName(photo)}の選択を解除`}
                badge={index + 1}
                onClick={() => unselect(photo.photoNo)}
              />
            </li>
          ))}
        </ul>
      </section>
      <div className="flex justify-end gap-3 p-4 border-t">
        <button type="button" onClick={onCancel} className="px-4 py-2 border rounded">
          キャンセル
        </button>
        <button
          type="button"
          onClick={() => onComplete(selected)}
          className="px-4 py-2 bg-blue-700 text-white rounded hover:bg-blue-800"
        >
          完了
        </button>
      </div>
    </ModalDialog>
  );
}
