"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPhotoDetail,
  deletePhoto,
  addFavorite,
  deleteFavorite,
  type PhotoDetailResponse,
} from "@/lib/api/client";
import { sanitizeImageUrl } from "@/lib/url";
import { ModalDialog } from "@/components/ui/ModalDialog";

interface PhotoDetailProps {
  photoAccountId: string;
  accountNo: number;
  photoNo: number;
}

/**
 * 写真詳細コンポーネント
 */
export function PhotoDetail({
  photoAccountId,
  accountNo,
  photoNo,
}: PhotoDetailProps) {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();
  const [photo, setPhoto] = useState<PhotoDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  // error: 初期ロード失敗（写真を表示できない）。actionError: 操作失敗（写真表示は維持）
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isFavoriteProcessing, setIsFavoriteProcessing] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  // お気に入り操作の多重実行防止（setState 反映前の連打・キーリピート対策。
  // PhotoList と同じく ref で即時に弾く）
  const favoriteInFlightRef = useRef(false);

  // owner 判定は URL パスだけでなく、表示中の写真の実所有者（photo.accountNo）が
  // ログインユーザー自身であることまで確認する。細工 URL
  // （/photo/<自分>/photo_detail?accountNo=<他人>&photoNo=<他人の写真>）で
  // 編集・削除 UI が出るのを防ぐ（多層防御。認可はバックエンドでも実施）。
  const isOwner =
    !!user &&
    user.accountId === photoAccountId &&
    (photo === null || photo.accountNo === user.accountNo);

  /**
   * 写真詳細取得
   */
  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        const data = await getPhotoDetail(photoAccountId, accountNo, photoNo);
        if (!cancelled) {
          setPhoto(data);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "エラーが発生しました");
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [photoAccountId, accountNo, photoNo]);

  /**
   * お気に入り登録／解除
   */
  const handleFavoriteToggle = async () => {
    if (!photo || favoriteInFlightRef.current) return;
    favoriteInFlightRef.current = true;
    setIsFavoriteProcessing(true);
    setActionError(null);
    // 実行するアクションに対応した絶対値で更新する（相対トグルの二重反転を避ける）
    const nextFavorite = !photo.isFavorite;
    try {
      if (photo.isFavorite) {
        await deleteFavorite(photo.accountNo, photo.photoNo);
      } else {
        await addFavorite(photo.accountNo, photo.photoNo);
      }
      setPhoto((prev) => (prev ? { ...prev, isFavorite: nextFavorite } : prev));
    } catch (err) {
      // 操作失敗は写真表示を維持したまま通知する
      setActionError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      favoriteInFlightRef.current = false;
      setIsFavoriteProcessing(false);
    }
  };

  /**
   * 写真削除
   */
  const handleDelete = async () => {
    if (!photo || isDeleting) return;
    setIsDeleting(true);
    setActionError(null);
    try {
      await deletePhoto(photoAccountId, {
        photoNo: photo.photoNo,
        imageFilePath: photo.imageFilePath,
      });
      router.push(`/photo/${photoAccountId}/photo_list`);
    } catch (err) {
      // 削除失敗時は確認ダイアログを開いたままエラーを通知する
      setActionError(err instanceof Error ? err.message : "エラーが発生しました");
      setIsDeleting(false);
    }
  };

  /**
   * 撮影日時を表示用に整形する
   *
   * `photoAt` は「撮影された壁時計時刻」を表すため、閲覧者のタイムゾーンへ変換せず
   * ISO 文字列の日時部分をそのまま整形する。
   */
  const formatPhotoAt = (photoAt: string | null): string => {
    if (!photoAt) return "";
    const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(photoAt);
    if (!match) return "";
    const [, year, month, day, hour, minute] = match;
    return `${year}/${month}/${day} ${hour}:${minute}`;
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-gray-400">読み込み中...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-red-500">{error}</p>
      </div>
    );
  }

  if (!photo) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-gray-400">写真が見つかりません</p>
      </div>
    );
  }

  /**
   * EXIF設定テキスト生成
   */
  const buildSettingText = (): string => {
    const parts: string[] = [];
    if (photo.focalLength != null) parts.push(`${photo.focalLength}mm`);
    if (photo.fValue != null) parts.push(`F${photo.fValue}`);
    if (photo.shutterSpeed != null) parts.push(`${photo.shutterSpeed}sec`);
    if (photo.iso != null) parts.push(`iso${photo.iso}`);
    return parts.join(" ");
  };

  // 表示画像の src。サニタイズで空（＝許可されない URL）の場合は代替表示にして、
  // <img src=""> でページ自身が再取得されるのを避ける
  const imageSrc = sanitizeImageUrl(photo.imageFilePath);
  const imageStyle = {
    display: "block",
    margin: "auto",
    width: "100%",
    maxWidth: "1000px",
    maxHeight: "600px",
    objectFit: "contain",
  } as const;

  return (
    <div style={{ backgroundColor: "#000", color: "#fff", minHeight: "100vh" }}>
      {/* 戻るリンク */}
      <header>
        <Link
          href={`/photo/${photoAccountId}/photo_list`}
          className="fixed top-[5px] left-[10px] text-xl text-gray-400 z-[1000] no-underline"
        >
          &larr; back
        </Link>
      </header>

      {/* 操作失敗の通知（写真表示は維持したまま表示する） */}
      {actionError && (
        <div
          role="alert"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            zIndex: 200,
            background: "#dc2626",
            color: "#fff",
            padding: "10px 16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "12px",
          }}
        >
          <span>{actionError}</span>
          <button
            type="button"
            onClick={() => setActionError(null)}
            aria-label="閉じる"
            style={{
              background: "none",
              border: "none",
              color: "#fff",
              cursor: "pointer",
              fontSize: "18px",
              lineHeight: 1,
              padding: 0,
            }}
          >
            &times;
          </button>
        </div>
      )}

      {/* 右上アイコン群 */}
      <div>
        {isOwner && (
          <button
            type="button"
            aria-label="編集"
            onClick={() => router.push(`/photo/${photoAccountId}/photo_setting?accountNo=${photo.accountNo}&photoNo=${photo.photoNo}`)}
            style={{
              position: "fixed",
              top: "2%",
              right: "170px",
              width: "25px",
              height: "25px",
              cursor: "pointer",
              zIndex: 50,
              padding: 0,
              border: "none",
              background: "none",
            }}
          >
            <img src="/image/edit.png" alt="" style={{ width: "100%", height: "100%", display: "block" }} />
          </button>
        )}
        {isOwner && (
          <button
            type="button"
            aria-label="削除"
            onClick={() => setShowDeleteConfirm(true)}
            style={{
              position: "fixed",
              top: "2%",
              right: "130px",
              width: "25px",
              height: "25px",
              cursor: "pointer",
              zIndex: 50,
              padding: 0,
              border: "none",
              background: "none",
            }}
          >
            <img src="/image/trash.png" alt="" style={{ width: "100%", height: "100%", display: "block" }} />
          </button>
        )}
        {isAuthenticated && (
          <button
            type="button"
            aria-label={photo.isFavorite ? "お気に入り解除" : "お気に入り登録"}
            aria-pressed={photo.isFavorite}
            onClick={handleFavoriteToggle}
            disabled={isFavoriteProcessing}
            data-testid="favorite-button"
            style={{
              position: "fixed",
              top: "2%",
              right: "90px",
              width: "25px",
              height: "25px",
              cursor: isFavoriteProcessing ? "not-allowed" : "pointer",
              zIndex: 50,
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

      {/* メインコンテンツ */}
      <div style={{ padding: "70px 5% 5% 5%", overflow: "auto" }}>
        {/* 画像 */}
        <div>
          {imageSrc ? (
            <img
              src={imageSrc}
              alt={photo.photoJapaneseTitle || photo.photoEnglishTitle || "写真"}
              style={imageStyle}
            />
          ) : (
            <div
              style={{
                ...imageStyle,
                height: "300px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#9ca3af",
                background: "#1f2937",
              }}
            >
              画像を表示できません
            </div>
          )}
        </div>

        {/* 詳細情報 */}
        <div style={{ maxWidth: "1000px", margin: "auto" }}>
          {photo.photoJapaneseTitle && (
            <p style={{ fontSize: "20px", textAlign: "center", margin: "5px 0 1px 0" }}>
              {photo.photoJapaneseTitle}
            </p>
          )}
          {photo.photoEnglishTitle && (
            <p style={{ fontSize: "12px", textAlign: "center", margin: "3px 0 5px 0" }}>
              {photo.photoEnglishTitle}
            </p>
          )}

          {photo.caption && (
            <p style={{
              fontSize: "16px",
              margin: "25px 0 30px 0",
              marginInline: "auto",
              maxInlineSize: "max-content",
              overflowWrap: "break-word",
              wordBreak: "break-all",
            }}>
              {photo.caption}
            </p>
          )}

          <p style={{ fontSize: "12px", textAlign: "center", margin: "1px 0" }}>
            {formatPhotoAt(photo.photoAt)} {photo.locationName || ""}
          </p>

          {buildSettingText() && (
            <p style={{ fontSize: "12px", textAlign: "center", margin: "1px 0" }}>
              {buildSettingText()}
            </p>
          )}

          {/* タグ一覧 */}
          {photo.photoTagList && photo.photoTagList.length > 0 && (
            <div style={{ marginTop: "16px" }}>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", justifyContent: "center" }}>
                {photo.photoTagList.map((tag) => (
                  <span
                    key={tag.tagNo}
                    style={{
                      border: "1px solid #4b5563",
                      color: "#d1d5db",
                      padding: "4px 12px",
                      fontSize: "14px",
                    }}
                  >
                    {tag.tagJapaneseName || tag.tagEnglishName}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 削除確認ダイアログ */}
      {showDeleteConfirm && (
        <ModalDialog
          testId="delete-confirm-dialog"
          label="写真の削除確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => {
            if (isDeleting) return;
            setShowDeleteConfirm(false);
            setActionError(null);
          }}
          overlayStyle={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            textAlign: "center",
            background: "rgba(0, 0, 0, 50%)",
            padding: "40px 20px",
            overflow: "auto",
            zIndex: 100,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
          containerStyle={{
            position: "relative",
            display: "inline-block",
            maxWidth: "500px",
            width: "90%",
          }}
        >
          <button
            type="button"
            aria-label="閉じる"
            onClick={() => {
              setShowDeleteConfirm(false);
              setActionError(null);
            }}
            style={{
              position: "absolute",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              top: "-15px",
              right: "-15px",
              width: "30px",
              height: "30px",
              color: "#fff",
              background: "#000",
              border: "none",
              borderRadius: "50%",
              cursor: "pointer",
            }}
          >
            &times;
          </button>
          <div style={{
            background: "#fff",
            textAlign: "center",
            lineHeight: 1.8,
            padding: "20px",
          }}>
            <p style={{ color: "#000", margin: "1em 0" }}>写真を削除してもよろしいですか？</p>
            {actionError && (
              <p role="alert" style={{ color: "#dc2626", margin: "0 0 1em 0", fontSize: "14px" }}>
                {actionError}
              </p>
            )}
            <div style={{ display: "flex", justifyContent: "center", gap: "12px" }}>
              <button
                data-dialog-initial-focus
                onClick={() => {
                  setShowDeleteConfirm(false);
                  setActionError(null);
                }}
                style={{
                  background: "#4b5563",
                  color: "#fff",
                  padding: "8px 16px",
                  border: "none",
                  cursor: "pointer",
                }}
              >
                キャンセル
              </button>
              <button
                onClick={handleDelete}
                disabled={isDeleting}
                style={{
                  background: "#dc2626",
                  color: "#fff",
                  padding: "8px 16px",
                  border: "none",
                  cursor: isDeleting ? "not-allowed" : "pointer",
                  opacity: isDeleting ? 0.5 : 1,
                }}
              >
                {isDeleting ? "削除中..." : "削除する"}
              </button>
            </div>
          </div>
        </ModalDialog>
      )}
    </div>
  );
}
