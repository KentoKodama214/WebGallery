"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPhotoDetail,
  savePhoto,
  type PhotoDetailResponse,
} from "@/lib/api/client";

interface TagEntry {
  tagNo: number;
  tagJapaneseName: string;
  tagEnglishName: string;
}

/** アップロード可能な画像ファイルの最大サイズ（5MB） */
const MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024;

interface PhotoSettingFormProps {
  photoAccountId: string;
  accountNo?: number;
  photoNo?: number;
}

/**
 * 写真設定フォームコンポーネント
 */
export function PhotoSettingForm({
  photoAccountId,
  accountNo,
  photoNo,
}: PhotoSettingFormProps) {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();
  const [savedPhotoNo, setSavedPhotoNo] = useState<number | undefined>(photoNo);
  const isEditMode = savedPhotoNo !== undefined;

  const fileInputRef = useRef<HTMLInputElement>(null);

  // フォーム状態
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [existingImageFilePath, setExistingImageFilePath] = useState("");
  const [photoJapaneseTitle, setPhotoJapaneseTitle] = useState("");
  const [photoEnglishTitle, setPhotoEnglishTitle] = useState("");
  const [caption, setCaption] = useState("");
  const [photoAt, setPhotoAt] = useState("");
  const [directionKbn, setDirectionKbn] = useState("horizontal");
  const [focalLength, setFocalLength] = useState("");
  const [fValue, setFValue] = useState("");
  const [shutterSpeed, setShutterSpeed] = useState("");
  const [iso, setIso] = useState("");
  const [tags, setTags] = useState<TagEntry[]>([]);
  const [nextTagNo, setNextTagNo] = useState(1);

  // UI状態
  const [isDataLoading, setIsDataLoading] = useState(isEditMode);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [authLoading, isAuthenticated, router]);

  /**
   * 初期表示（編集モード時に既存データを読み込む）
   */
  useEffect(() => {
    if (authLoading || !isAuthenticated) return;
    if (!isEditMode || !accountNo || !photoNo) return;

    let cancelled = false;
    const load = async () => {
      try {
        const data: PhotoDetailResponse = await getPhotoDetail(
          photoAccountId,
          accountNo,
          photoNo
        );
        if (cancelled) return;
        setPhotoJapaneseTitle(data.photoJapaneseTitle || "");
        setPhotoEnglishTitle(data.photoEnglishTitle || "");
        setCaption(data.caption || "");
        setDirectionKbn(data.directionKbn || "horizontal");
        setFocalLength(data.focalLength != null ? String(data.focalLength) : "");
        setFValue(data.fValue != null ? String(data.fValue) : "");
        setShutterSpeed(
          data.shutterSpeed != null ? String(data.shutterSpeed) : ""
        );
        setIso(data.iso != null ? String(data.iso) : "");
        setExistingImageFilePath(data.imageFilePath);
        setImagePreview(data.imageFilePath);

        if (data.photoAt) {
          // photoAt は「撮影された壁時計時刻」（バックエンドは LocalDateTime で受け取る）。
          // タイムゾーン変換をせず ISO 文字列の日時部分をそのまま datetime-local へ渡す。
          // （閲覧端末のTZに依存して往復のたびにずれる不具合を防ぐ）
          const match = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.exec(data.photoAt);
          if (match) setPhotoAt(match[0]);
        }

        if (data.photoTagList && data.photoTagList.length > 0) {
          const tagEntries = data.photoTagList.map((t) => ({
            tagNo: t.tagNo,
            tagJapaneseName: t.tagJapaneseName,
            tagEnglishName: t.tagEnglishName,
          }));
          setTags(tagEntries);
          const maxTagNo = Math.max(...tagEntries.map((t) => t.tagNo));
          setNextTagNo(maxTagNo + 1);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "エラーが発生しました");
        }
      } finally {
        if (!cancelled) setIsDataLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [authLoading, isAuthenticated, isEditMode, photoAccountId, accountNo, photoNo]);

  /**
   * 画像選択
   */
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > MAX_IMAGE_FILE_SIZE) {
      setValidationErrors(["画像ファイルは5MB以下にしてください"]);
      e.target.value = "";
      return;
    }
    setValidationErrors([]);
    setImageFile(file);
    const reader = new FileReader();
    reader.onload = () => setImagePreview(reader.result as string);
    reader.readAsDataURL(file);
  };

  /**
   * タグ追加
   */
  const handleAddTag = () => {
    setTags([
      ...tags,
      { tagNo: nextTagNo, tagJapaneseName: "", tagEnglishName: "" },
    ]);
    setNextTagNo(nextTagNo + 1);
  };

  /**
   * タグ削除
   */
  const handleRemoveTag = (tagNo: number) => {
    setTags(tags.filter((t) => t.tagNo !== tagNo));
  };

  /**
   * タグ変更
   */
  const handleTagChange = (
    tagNo: number,
    field: "tagJapaneseName" | "tagEnglishName",
    value: string
  ) => {
    setTags(
      tags.map((t) => (t.tagNo === tagNo ? { ...t, [field]: value } : t))
    );
  };

  /**
   * バリデーション
   */
  const validate = (): string[] => {
    const errors: string[] = [];
    if (!isEditMode && !imageFile) {
      errors.push("画像ファイルを選択してください");
    }
    if (imageFile && imageFile.size > MAX_IMAGE_FILE_SIZE) {
      errors.push("画像ファイルは5MB以下にしてください");
    }
    if (photoAt) {
      const photoDate = new Date(photoAt);
      if (photoDate > new Date()) {
        errors.push("撮影日時は過去の日時を指定してください");
      }
    }
    if (focalLength && Number(focalLength) <= 0) {
      errors.push("焦点距離は正の値を入力してください");
    }
    if (fValue && Number(fValue) <= 0) {
      errors.push("F値は正の値を入力してください");
    }
    if (shutterSpeed && Number(shutterSpeed) <= 0) {
      errors.push("シャッタースピードは正の値を入力してください");
    }
    if (iso && Number(iso) <= 0) {
      errors.push("ISOは正の値を入力してください");
    }
    for (const tag of tags) {
      if (!tag.tagJapaneseName.trim()) {
        errors.push("タグの日本語名は必須です");
        break;
      }
      if (/( |　)/.test(tag.tagJapaneseName)) {
        errors.push("タグの日本語名にスペースは使用できません");
        break;
      }
    }
    return errors;
  };

  /**
   * 登録する
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validate();
    if (errors.length > 0) {
      setValidationErrors(errors);
      return;
    }
    setValidationErrors([]);
    setIsSaving(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append("directionKbn", directionKbn);

      if (isEditMode && savedPhotoNo) {
        formData.append("photoNo", String(savedPhotoNo));
      }
      if (imageFile) {
        formData.append("imageFile", imageFile);
      }
      if (existingImageFilePath) {
        formData.append("imageFilePath", existingImageFilePath);
      }
      if (photoJapaneseTitle) {
        formData.append("photoJapaneseTitle", photoJapaneseTitle);
      }
      if (photoEnglishTitle) {
        formData.append("photoEnglishTitle", photoEnglishTitle);
      }
      if (caption) {
        formData.append("caption", caption);
      }
      if (photoAt) {
        formData.append("photoAt", photoAt);
      }
      if (focalLength) {
        formData.append("focalLength", focalLength);
      }
      if (fValue) {
        formData.append("fValue", fValue);
      }
      if (shutterSpeed) {
        formData.append("shutterSpeed", shutterSpeed);
      }
      if (iso) {
        formData.append("iso", iso);
      }

      tags.forEach((tag, index) => {
        formData.append(
          `photoTagRegistRequestList[${index}].accountNo`,
          String(user!.accountNo)
        );
        if (isEditMode && savedPhotoNo) {
          formData.append(
            `photoTagRegistRequestList[${index}].photoNo`,
            String(savedPhotoNo)
          );
        }
        formData.append(
          `photoTagRegistRequestList[${index}].tagNo`,
          String(tag.tagNo)
        );
        formData.append(
          `photoTagRegistRequestList[${index}].tagJapaneseName`,
          tag.tagJapaneseName
        );
        formData.append(
          `photoTagRegistRequestList[${index}].tagEnglishName`,
          tag.tagEnglishName || ""
        );
      });

      const result = await savePhoto(photoAccountId, formData, isEditMode);
      setSavedPhotoNo(result.photoNo);
      setExistingImageFilePath(result.imageFilePath);
      setShowSuccessModal(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsSaving(false);
    }
  };

  if (authLoading || isDataLoading) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-gray-400">読み込み中...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  if (user?.accountId !== photoAccountId) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-red-500">この操作を行う権限がありません</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black text-white pb-10">
      <header>
        <Link
          href={`/photo/${photoAccountId}/photo_list`}
          className="fixed top-[5px] left-[10px] text-xl text-gray-400 z-[1000] no-underline"
        >
          &larr; back
        </Link>
      </header>
      <div className="max-w-2xl mx-auto px-4 pt-8">
        <h1 className="text-xl font-bold mb-6 text-center">
          {isEditMode ? "写真編集" : "写真登録"}
        </h1>

        {error && (
          <div className="mb-4 p-3 border border-red-500 text-red-500">
            {error}
          </div>
        )}

        {validationErrors.length > 0 && (
          <div
            className="mb-4 p-3 border border-red-500 text-red-500"
            data-testid="validation-errors"
          >
            {validationErrors.map((err, i) => (
              <p key={i}>{err}</p>
            ))}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* 画像アップロード */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">
              画像ファイル{!isEditMode && " *"}
            </label>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="hidden"
              data-testid="image-input"
            />
            {imagePreview ? (
              <div
                className="mt-2 flex flex-col items-center cursor-pointer"
                onClick={() => fileInputRef.current?.click()}
              >
                <img
                  src={imagePreview}
                  alt="プレビュー"
                  className="max-w-full max-h-[300px]"
                  style={{ objectFit: "contain" }}
                  data-testid="image-preview"
                />
                {imageFile && (
                  <p className="text-sm text-gray-400 mt-1">{imageFile.name}</p>
                )}
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="bg-gray-700 text-white px-4 py-2 border border-gray-600 cursor-pointer hover:bg-gray-600"
              >
                ファイルを選択
              </button>
            )}
          </div>

          {/* タイトル（日本語） */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">
              タイトル（日本語）
            </label>
            <input
              type="text"
              value={photoJapaneseTitle}
              onChange={(e) => setPhotoJapaneseTitle(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="japanese-title-input"
            />
          </div>

          {/* タイトル（英語） */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">
              タイトル（英語）
            </label>
            <input
              type="text"
              value={photoEnglishTitle}
              onChange={(e) => setPhotoEnglishTitle(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="english-title-input"
            />
          </div>

          {/* 撮影日時 */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">
              撮影日時
            </label>
            <input
              type="datetime-local"
              value={photoAt}
              onChange={(e) => setPhotoAt(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="photo-at-input"
            />
          </div>

          {/* 向き */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">向き *</label>
            <select
              value={directionKbn}
              onChange={(e) => setDirectionKbn(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="direction-select"
            >
              <option value="horizontal">横</option>
              <option value="vertical">縦</option>
            </select>
          </div>

          {/* EXIF情報 */}
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-sm text-gray-400 mb-1">
                焦点距離 (mm)
              </label>
              <input
                type="number"
                value={focalLength}
                onChange={(e) => setFocalLength(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="focal-length-input"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-1">F値</label>
              <input
                type="number"
                step="0.1"
                value={fValue}
                onChange={(e) => setFValue(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="f-value-input"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-1">
                シャッタースピード (秒)
              </label>
              <input
                type="number"
                step="0.0001"
                value={shutterSpeed}
                onChange={(e) => setShutterSpeed(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="shutter-speed-input"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-1">ISO</label>
              <input
                type="number"
                value={iso}
                onChange={(e) => setIso(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="iso-input"
              />
            </div>
          </div>

          {/* キャプション */}
          <div className="mb-4">
            <label className="block text-sm text-gray-400 mb-1">
              キャプション
            </label>
            <textarea
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              rows={4}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="caption-input"
            />
          </div>

          {/* タグ */}
          <div className="mb-6">
            <label className="text-sm text-gray-400 block mb-2">タグ</label>
            {tags.map((tag) => (
              <div
                key={tag.tagNo}
                className="flex gap-2 mb-2"
                data-testid={`tag-entry-${tag.tagNo}`}
              >
                <input
                  type="text"
                  value={tag.tagJapaneseName}
                  onChange={(e) =>
                    handleTagChange(tag.tagNo, "tagJapaneseName", e.target.value)
                  }
                  placeholder="タグ名（日本語）*"
                  className="flex-1 bg-gray-800 text-white border border-gray-600 p-2"
                  data-testid={`tag-japanese-${tag.tagNo}`}
                />
                <input
                  type="text"
                  value={tag.tagEnglishName}
                  onChange={(e) =>
                    handleTagChange(tag.tagNo, "tagEnglishName", e.target.value)
                  }
                  placeholder="タグ名（英語）"
                  className="flex-1 bg-gray-800 text-white border border-gray-600 p-2"
                  data-testid={`tag-english-${tag.tagNo}`}
                />
                <button
                  type="button"
                  onClick={() => handleRemoveTag(tag.tagNo)}
                  className="bg-red-600 text-white px-3 border-none cursor-pointer hover:bg-red-700"
                  data-testid={`remove-tag-${tag.tagNo}`}
                >
                  ×
                </button>
              </div>
            ))}
            <div className="flex justify-center mt-2">
              <button
                type="button"
                onClick={handleAddTag}
                className="bg-transparent border border-gray-500 text-gray-300 px-3 py-1 text-sm cursor-pointer hover:bg-gray-800"
                data-testid="add-tag-button"
              >
                + タグを追加
              </button>
            </div>
          </div>

          {/* 保存・キャンセルボタン */}
          <div className="flex justify-center gap-4">
            <button
              type="button"
              onClick={() =>
                router.push(`/photo/${photoAccountId}/photo_list`)
              }
              className="bg-gray-600 text-white px-6 py-2 border-none cursor-pointer hover:bg-gray-700"
            >
              キャンセル
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 text-white px-6 py-2 border-none cursor-pointer hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              data-testid="submit-button"
            >
              {isSaving ? "保存中..." : "保存"}
            </button>
          </div>
        </form>
      </div>

      {/* 成功モーダル */}
      {showSuccessModal && (
        <div
          className="fixed inset-0 bg-black/70 flex justify-center items-center z-50"
          data-testid="success-modal"
        >
          <div className="bg-gray-900 border border-gray-700 p-6 max-w-sm w-full mx-4 text-center relative">
            <button
              onClick={() => setShowSuccessModal(false)}
              className="absolute top-2 right-3 text-xl text-gray-400 bg-transparent border-none cursor-pointer"
            >
              &times;
            </button>
            <p className="text-white">写真を保存しました</p>
          </div>
        </div>
      )}
    </div>
  );
}
