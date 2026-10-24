"use client";

import { useEffect, useRef, useState } from "react";
import { ModalDialog } from "@/components/ui/ModalDialog";
import {
  searchPortfolioPhotos,
  type PortfolioPhotoCandidate,
  type PortfolioPhotoSearchFilter,
} from "@/lib/api/client";
import { sanitizeImageUrl } from "@/lib/url";
import { PORTFOLIO_PHOTO_MAX_SIZE, TAG_NAME_MAX_LENGTH } from "@/lib/validation";

/** 写真選択ダイアログで選んだ写真 */
export interface PickedPhoto {
  /** 写真番号 */
  photoNo: number;
  /** 画像URL（署名付きURL） */
  imageFilePath: string;
  /** 一覧・プレビューで表示する写真の名前 */
  label: string;
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

/** 向き区分ごとの写真の枠の縦横比と、向きの表示名 */
const DIRECTION_FRAMES: Record<string, { aspectRatio: string; label: string }> = {
  vertical: { aspectRatio: "3 / 4", label: "縦" },
  horizontal: { aspectRatio: "4 / 3", label: "横" },
  square: { aspectRatio: "1 / 1", label: "正方形" },
};

/** 向きが未設定の写真の枠 */
const DEFAULT_FRAME = { aspectRatio: "1 / 1", label: "" };

/**
 * 写真の表示名を求める（タイトルが未入力の場合は写真番号）
 *
 * @param photo 検索結果の写真
 * @returns 表示名
 */
function toLabel(photo: PortfolioPhotoCandidate): string {
  return photo.photoJapaneseTitle || `写真${photo.photoNo}`;
}

/**
 * ポートフォリオに設定する写真を、自分が登録した写真から選ぶダイアログ
 *
 * 本人専用の写真検索APIをページ単位で取得し、タイトル・撮影場所・タグで絞り込める。
 * 写真の枠は向き（縦・横・正方形）に合わせた縦横比で表示する。
 * 選択はダイアログの中だけで保持し、「完了」で確定する（「キャンセル」では開く前の選択に戻る）。
 * 新しく選んだ写真は、選んだ順に表示順の末尾へ追加される。
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
  // 絞り込みの入力値と、検索に使った（検索ボタンで確定した）条件
  const [titleInput, setTitleInput] = useState("");
  const [locationInput, setLocationInput] = useState("");
  const [tagInput, setTagInput] = useState("");
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
    loadPage({ title: titleInput.trim(), location: locationInput.trim(), tag: tagInput.trim() }, 1);
  };

  /**
   * 写真の選択・選択解除を切り替える
   *
   * @param photo 検索結果の写真
   */
  const toggle = (photo: PortfolioPhotoCandidate) => {
    setSelected((prev) =>
      prev.some((p) => p.photoNo === photo.photoNo)
        ? prev.filter((p) => p.photoNo !== photo.photoNo)
        : [...prev, { photoNo: photo.photoNo, imageFilePath: photo.imageFilePath, label: toLabel(photo) }]
    );
  };

  const isFull = selected.length >= PORTFOLIO_PHOTO_MAX_SIZE;
  const isFiltered = !!appliedFilter.title || !!appliedFilter.location || !!appliedFilter.tag;
  const inputClass = "border border-gray-300 rounded px-2 py-1 text-sm";

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
        <label className="flex flex-col text-xs font-bold">
          タイトル
          <input
            type="text"
            value={titleInput}
            maxLength={100}
            onChange={(e) => setTitleInput(e.target.value)}
            className={inputClass}
          />
        </label>
        <label className="flex flex-col text-xs font-bold">
          場所
          <input
            type="text"
            value={locationInput}
            maxLength={100}
            onChange={(e) => setLocationInput(e.target.value)}
            className={inputClass}
          />
        </label>
        <label className="flex flex-col text-xs font-bold">
          タグ
          <input
            type="text"
            value={tagInput}
            maxLength={TAG_NAME_MAX_LENGTH}
            onChange={(e) => setTagInput(e.target.value)}
            className={inputClass}
          />
        </label>
        <button
          type="submit"
          className="px-3 py-1 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
        >
          検索
        </button>
      </form>
      <div className="p-4 overflow-y-auto flex-1">
        {photos.length === 0 && !isLoading && !error && (
          <p className="text-gray-600">
            {isFiltered ? "条件に一致する写真がありません" : "登録済みの写真がありません"}
          </p>
        )}
        <ul className="flex flex-wrap gap-2">
          {photos.map((photo) => {
            const order = selected.findIndex((p) => p.photoNo === photo.photoNo);
            const isSelected = order >= 0;
            const src = sanitizeImageUrl(photo.imageFilePath);
            const frame = DIRECTION_FRAMES[photo.directionKbn] ?? DEFAULT_FRAME;
            return (
              <li key={photo.photoNo} data-testid="photo-picker-item" data-direction={photo.directionKbn}>
                <button
                  type="button"
                  aria-pressed={isSelected}
                  aria-label={`写真${photo.photoNo}${photo.photoJapaneseTitle ? `（${photo.photoJapaneseTitle}）` : ""}`}
                  disabled={!isSelected && isFull}
                  onClick={() => toggle(photo)}
                  style={{ aspectRatio: frame.aspectRatio }}
                  className={`relative block h-32 overflow-hidden rounded border-4 bg-gray-200 disabled:opacity-40 ${
                    isSelected ? "border-blue-700" : "border-transparent"
                  }`}
                >
                  {src ? (
                    <img src={src} alt="" className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-xs text-gray-600">{toLabel(photo)}</span>
                  )}
                  {frame.label && (
                    <span className="absolute bottom-1 left-1 bg-black/70 text-white text-xs px-1.5 py-0.5 rounded">
                      {frame.label}
                    </span>
                  )}
                  {isSelected && (
                    <span className="absolute top-1 right-1 bg-blue-700 text-white text-xs px-1.5 py-0.5 rounded">
                      {order + 1}
                    </span>
                  )}
                </button>
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
      </div>
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
