"use client";

import { useEffect, useRef, useState } from "react";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { getPhotoList, type PhotoListItem } from "@/lib/api/client";
import { sanitizeImageUrl } from "@/lib/url";
import { PORTFOLIO_PHOTO_MAX_SIZE } from "@/lib/validation";

interface PhotoPickerDialogProps {
  /** 写真の所有者（＝ログイン中の本人）のアカウントID */
  accountId: string;
  /** 選択済みの写真番号 */
  selectedPhotoNos: number[];
  /** 写真の選択・選択解除を切り替える */
  onToggle: (photo: PhotoListItem) => void;
  /** ダイアログを閉じる */
  onClose: () => void;
}

/**
 * ポートフォリオに設定する写真を、自分が登録した写真から選ぶダイアログ
 *
 * 写真一覧APIをページ単位で取得し、「もっと見る」で続きを読み込む。
 * 写真をクリックするたびに選択・選択解除を切り替え、選択した順に表示順の末尾へ追加される。
 */
export function PhotoPickerDialog({
  accountId,
  selectedPhotoNos,
  onToggle,
  onClose,
}: PhotoPickerDialogProps) {
  const [photos, setPhotos] = useState<PhotoListItem[]>([]);
  const [pageNo, setPageNo] = useState(1);
  const [isLast, setIsLast] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 取得リクエストの世代。後着レスポンスが一覧を上書きする競合を防ぐ
  const loadSeqRef = useRef(0);

  const loadPage = async (nextPageNo: number) => {
    const seq = ++loadSeqRef.current;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getPhotoList(accountId, { pageNo: nextPageNo });
      if (loadSeqRef.current !== seq) return;
      setPhotos((prev) => (nextPageNo === 1 ? data.photoList : [...prev, ...data.photoList]));
      setIsLast(data.isLast);
      setPageNo(nextPageNo);
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
        const data = await getPhotoList(accountId, { pageNo: 1 });
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

  const isFull = selectedPhotoNos.length >= PORTFOLIO_PHOTO_MAX_SIZE;

  return (
    <ModalDialog
      onClose={onClose}
      label="写真を選択"
      testId="photo-picker-dialog"
      overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
      containerClassName="bg-white rounded shadow-lg w-full max-w-[900px] max-h-[90vh] flex flex-col"
    >
      <div className="flex justify-between items-center p-4 border-b">
        <h2 className="text-lg font-bold">写真を選択</h2>
        <p className="text-sm text-gray-600">
          選択中 {selectedPhotoNos.length} / {PORTFOLIO_PHOTO_MAX_SIZE}枚
        </p>
      </div>
      <div className="p-4 overflow-y-auto flex-1">
        {photos.length === 0 && !isLoading && !error && (
          <p className="text-gray-600">登録済みの写真がありません</p>
        )}
        <ul className="grid grid-cols-3 sm:grid-cols-4 gap-2">
          {photos.map((photo) => {
            const isSelected = selectedPhotoNos.includes(photo.photoNo);
            const src = sanitizeImageUrl(photo.imageFilePath);
            return (
              <li key={photo.photoNo}>
                <button
                  type="button"
                  aria-pressed={isSelected}
                  aria-label={`写真${photo.photoNo}${photo.caption ? `（${photo.caption}）` : ""}`}
                  disabled={!isSelected && isFull}
                  onClick={() => onToggle(photo)}
                  className={`relative block w-full aspect-square overflow-hidden rounded border-4 disabled:opacity-40 ${
                    isSelected ? "border-blue-700" : "border-transparent"
                  }`}
                >
                  {src ? (
                    <img src={src} alt="" className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-xs text-gray-600">写真{photo.photoNo}</span>
                  )}
                  {isSelected && (
                    <span className="absolute top-1 right-1 bg-blue-700 text-white text-xs px-1.5 py-0.5 rounded">
                      {selectedPhotoNos.indexOf(photo.photoNo) + 1}
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
              onClick={() => loadPage(pageNo + 1)}
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
            >
              ＋もっと見る
            </button>
          </div>
        )}
      </div>
      <div className="flex justify-end p-4 border-t">
        <button
          type="button"
          onClick={onClose}
          className="px-4 py-2 bg-blue-700 text-white rounded hover:bg-blue-800"
        >
          完了
        </button>
      </div>
    </ModalDialog>
  );
}
