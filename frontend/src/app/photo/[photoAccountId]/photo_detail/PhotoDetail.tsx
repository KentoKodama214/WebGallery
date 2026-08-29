"use client";

import { useEffect, useState } from "react";
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
  const [error, setError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isFavoriteProcessing, setIsFavoriteProcessing] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const isOwner = user?.accountId === photoAccountId;

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
    if (!photo || isFavoriteProcessing) return;
    setIsFavoriteProcessing(true);
    try {
      if (photo.isFavorite) {
        await deleteFavorite(photo.accountNo, photo.photoNo);
      } else {
        await addFavorite(photo.accountNo, photo.photoNo);
      }
      setPhoto({ ...photo, isFavorite: !photo.isFavorite });
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsFavoriteProcessing(false);
    }
  };

  /**
   * 写真削除
   */
  const handleDelete = async () => {
    if (!photo || isDeleting) return;
    setIsDeleting(true);
    try {
      await deletePhoto(photoAccountId, {
        photoNo: photo.photoNo,
        imageFilePath: photo.imageFilePath,
      });
      router.push(`/photo/${photoAccountId}/photo_list`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
      setIsDeleting(false);
    }
  };

  const formatPhotoAt = (photoAt: string | null): string => {
    if (!photoAt) return "";
    const date = new Date(photoAt);
    return date.toLocaleString("ja-JP", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
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

      {/* 右上アイコン群 */}
      <div>
        {isOwner && (
          <img
            src="/image/edit.png"
            alt="編集"
            onClick={() => router.push(`/photo/${photoAccountId}/photo_setting?accountNo=${photo.accountNo}&photoNo=${photo.photoNo}`)}
            style={{
              position: "fixed",
              top: "2%",
              right: "170px",
              width: "25px",
              height: "25px",
              cursor: "pointer",
              zIndex: 50,
            }}
          />
        )}
        {isOwner && (
          <img
            src="/image/trash.png"
            alt="削除"
            onClick={() => setShowDeleteConfirm(true)}
            style={{
              position: "fixed",
              top: "2%",
              right: "130px",
              width: "25px",
              height: "25px",
              cursor: "pointer",
              zIndex: 50,
            }}
          />
        )}
        {isAuthenticated && (
          <img
            src={photo.isFavorite ? "/image/heart_on.png" : "/image/heart_off.png"}
            alt={photo.isFavorite ? "お気に入り解除" : "お気に入り登録"}
            onClick={handleFavoriteToggle}
            data-testid="favorite-button"
            style={{
              position: "fixed",
              top: "2%",
              right: "90px",
              width: "25px",
              height: "25px",
              cursor: isFavoriteProcessing ? "not-allowed" : "pointer",
              zIndex: 50,
            }}
          />
        )}
      </div>

      {/* メインコンテンツ */}
      <div style={{ padding: "70px 5% 5% 5%", overflow: "auto" }}>
        {/* 画像 */}
        <div>
          <img
            src={photo.imageFilePath}
            alt={photo.photoJapaneseTitle || photo.photoEnglishTitle || "写真"}
            style={{
              display: "block",
              margin: "auto",
              width: "100%",
              maxWidth: "1000px",
              maxHeight: "600px",
              objectFit: "contain",
            }}
          />
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
        <div
          style={{
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
          data-testid="delete-confirm-dialog"
        >
          <div style={{
            position: "relative",
            display: "inline-block",
            maxWidth: "500px",
            width: "90%",
          }}>
            <div
              onClick={() => setShowDeleteConfirm(false)}
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
                borderRadius: "50%",
                cursor: "pointer",
              }}
            >
              &times;
            </div>
            <div style={{
              background: "#fff",
              textAlign: "center",
              lineHeight: 1.8,
              padding: "20px",
            }}>
              <p style={{ color: "#000", margin: "1em 0" }}>写真を削除してもよろしいですか？</p>
              <div style={{ display: "flex", justifyContent: "center", gap: "12px" }}>
                <button
                  onClick={() => setShowDeleteConfirm(false)}
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
          </div>
        </div>
      )}
    </div>
  );
}
