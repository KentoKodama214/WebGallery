"use client";

import { useCallback, useEffect, useState } from "react";
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
  const isEditMode = accountNo !== undefined && photoNo !== undefined;

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

  const loadPhotoData = useCallback(async () => {
    if (!isEditMode || !accountNo || !photoNo) return;
    setIsDataLoading(true);
    try {
      const data: PhotoDetailResponse = await getPhotoDetail(
        photoAccountId,
        accountNo,
        photoNo
      );
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
        const date = new Date(data.photoAt);
        const local = new Date(
          date.getTime() - date.getTimezoneOffset() * 60000
        );
        setPhotoAt(local.toISOString().slice(0, 16));
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
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsDataLoading(false);
    }
  }, [isEditMode, photoAccountId, accountNo, photoNo]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      loadPhotoData();
    }
  }, [authLoading, isAuthenticated, loadPhotoData]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImageFile(file);
      const reader = new FileReader();
      reader.onload = () => setImagePreview(reader.result as string);
      reader.readAsDataURL(file);
    }
  };

  const handleAddTag = () => {
    setTags([
      ...tags,
      { tagNo: nextTagNo, tagJapaneseName: "", tagEnglishName: "" },
    ]);
    setNextTagNo(nextTagNo + 1);
  };

  const handleRemoveTag = (tagNo: number) => {
    setTags(tags.filter((t) => t.tagNo !== tagNo));
  };

  const handleTagChange = (
    tagNo: number,
    field: "tagJapaneseName" | "tagEnglishName",
    value: string
  ) => {
    setTags(
      tags.map((t) => (t.tagNo === tagNo ? { ...t, [field]: value } : t))
    );
  };

  const validate = (): string[] => {
    const errors: string[] = [];
    if (!isEditMode && !imageFile) {
      errors.push("画像ファイルを選択してください");
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
      formData.append("accountNo", String(user!.accountNo));
      formData.append("directionKbn", directionKbn);

      if (isEditMode && photoNo) {
        formData.append("photoNo", String(photoNo));
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
        if (isEditMode && photoNo) {
          formData.append(
            `photoTagRegistRequestList[${index}].photoNo`,
            String(photoNo)
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

      await savePhoto(photoAccountId, formData, isEditMode);
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
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="w-full text-white"
              data-testid="image-input"
            />
            {imagePreview && (
              <div className="mt-2 flex justify-center">
                <img
                  src={imagePreview}
                  alt="プレビュー"
                  className="max-w-full max-h-[300px]"
                  style={{ objectFit: "contain" }}
                  data-testid="image-preview"
                />
              </div>
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
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm text-gray-400">タグ</label>
              <button
                type="button"
                onClick={handleAddTag}
                className="bg-transparent border border-gray-500 text-gray-300 px-3 py-1 text-sm cursor-pointer hover:bg-gray-800"
                data-testid="add-tag-button"
              >
                + タグ追加
              </button>
            </div>
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
          </div>

          {/* 送信ボタン */}
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
          <div className="bg-gray-900 border border-gray-700 p-6 max-w-sm w-full mx-4 text-center">
            <p className="text-white mb-4">写真を保存しました</p>
            <button
              onClick={() =>
                router.push(`/photo/${photoAccountId}/photo_list`)
              }
              className="bg-blue-600 text-white px-6 py-2 border-none cursor-pointer hover:bg-blue-700"
            >
              写真一覧へ
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
