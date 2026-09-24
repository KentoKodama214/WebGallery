"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  getPhotoDetail,
  getPhotoUpperLimit,
  registPhotos,
  savePhoto,
  type PhotoDetailResponse,
} from "@/lib/api/client";
import { loginUrlWithRedirect, sanitizeImageUrl } from "@/lib/url";
import { ModalDialog } from "@/components/ui/ModalDialog";

interface TagEntry {
  /** 一覧の React key 兼、既存タグの場合はバックエンドのタグ番号 */
  tagNo: number;
  /** この画面で新規追加されたタグ（サーバー未登録）かどうか */
  isNew: boolean;
  tagJapaneseName: string;
  tagEnglishName: string;
}

/** アップロード可能な画像ファイルの最大サイズ（5MB） */
const MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024;

/** 新規一括登録で1回に選択できる写真の最大枚数（バックエンドのConsts.PHOTO_BULK_REGIST_MAX_SIZEと合わせる） */
const MAX_BULK_REGIST_SIZE = 10;

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
  // 既存データの読み込みは accountNo / photoNo の両方が URL から渡された場合のみ行う
  const shouldLoadExisting = accountNo !== undefined && photoNo !== undefined;

  const fileInputRef = useRef<HTMLInputElement>(null);

  // フォーム状態
  // 新規登録時のみ使用（複数ファイル対応）。編集時は既存画像1枚のプレビューのみのため imagePreview を使う
  const [imageFiles, setImageFiles] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [existingImageFilePath, setExistingImageFilePath] = useState("");
  // 新規登録時のみ使用。アカウントの残り登録可能枚数（null=無制限、undefined=未取得）
  const [remainingCount, setRemainingCount] = useState<number | null | undefined>(undefined);
  const [photoJapaneseTitle, setPhotoJapaneseTitle] = useState("");
  const [photoEnglishTitle, setPhotoEnglishTitle] = useState("");
  const [caption, setCaption] = useState("");
  const [photoAt, setPhotoAt] = useState("");
  const [directionKbn, setDirectionKbn] = useState("horizontal");
  const [focalLength, setFocalLength] = useState("");
  const [fValue, setFValue] = useState("");
  const [shutterSpeed, setShutterSpeed] = useState("");
  const [iso, setIso] = useState("");
  // 位置情報公開フラグ。新規登録時は安全側に倒して「公開しない」を既定とする
  const [isLocationPublic, setIsLocationPublic] = useState(false);
  const [tags, setTags] = useState<TagEntry[]>([]);
  const [nextTagNo, setNextTagNo] = useState(1);

  // UI状態
  const [isDataLoading, setIsDataLoading] = useState(shouldLoadExisting);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("写真を保存しました");
  // 新規一括登録の成功時のみtrue。成功モーダルを閉じたら一覧へ戻る
  const [isBulkRegistSucceeded, setIsBulkRegistSucceeded] = useState(false);
  // 読み込んだ写真の所有者がログインユーザーでない場合（細工 URL 等）
  const [isForbidden, setIsForbidden] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push(loginUrlWithRedirect());
    }
  }, [authLoading, isAuthenticated, router]);

  /**
   * 初期表示（編集モード時に既存データを読み込む）
   */
  useEffect(() => {
    if (authLoading || !isAuthenticated) return;
    if (!shouldLoadExisting || !accountNo || !photoNo) return;

    let cancelled = false;
    const load = async () => {
      try {
        const data: PhotoDetailResponse = await getPhotoDetail(
          photoAccountId,
          photoNo
        );
        if (cancelled) return;
        // URL パス（photoAccountId）は自分でも、クエリで他人の写真番号を
        // 指した細工 URL では他人の写真が返りうる。所有者一致を確認する。
        if (
          user?.accountNo !== undefined &&
          data.accountNo !== user.accountNo
        ) {
          setIsForbidden(true);
          return;
        }
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
        setIsLocationPublic(data.isLocationPublic ?? false);
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
            isNew: false,
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
  }, [authLoading, isAuthenticated, shouldLoadExisting, photoAccountId, accountNo, photoNo, user?.accountNo]);

  /**
   * 新規登録モードのみ、アカウントの残り登録可能枚数を取得する
   *
   * 複数ファイル選択時に、権限上の残り枚数を超える選択をその場で拒否するために使用する。
   * 取得に失敗した場合は無制限扱い（undefinedのまま）とし、最終的な上限判定はバックエンドに委ねる
   */
  useEffect(() => {
    if (authLoading || !isAuthenticated || isEditMode) return;

    let cancelled = false;
    getPhotoUpperLimit(photoAccountId)
      .then((data) => {
        if (!cancelled) setRemainingCount(data.remainingCount);
      })
      .catch(() => {
        // 取得失敗時はフロント側の事前チェックを諦め、バックエンドの最終チェックに委ねる
      });
    return () => {
      cancelled = true;
    };
  }, [authLoading, isAuthenticated, isEditMode, photoAccountId]);

  // 今回選択できる残り枚数（アカウントの残り登録可能枚数と1リクエストの上限枚数のうち小さい方）。
  // remainingCountが未取得（undefined）または無制限（null）の場合は1リクエストの上限のみで制限する
  const maxSelectable =
    remainingCount == null ? MAX_BULK_REGIST_SIZE : Math.min(remainingCount, MAX_BULK_REGIST_SIZE);

  /**
   * 画像選択（新規登録モードのみ。複数選択可）
   *
   * 選択後の合計枚数が残り登録可能枚数・1リクエストの上限枚数を超える場合は、選択自体を拒否する。
   * 画像ファイルは登録後に差し替え・追加できない（バックエンドが更新時はDB上の既存パスを維持する
   * 仕様のため）。編集モードでは呼び出されない
   */
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(e.target.files ?? []);
    e.target.value = "";
    if (selectedFiles.length === 0) return;

    if (selectedFiles.some((file) => file.size > MAX_IMAGE_FILE_SIZE)) {
      setValidationErrors(["画像ファイルは5MB以下にしてください"]);
      return;
    }

    if (imageFiles.length + selectedFiles.length > maxSelectable) {
      setValidationErrors([
        `一度に登録できる写真は残り${Math.max(0, maxSelectable - imageFiles.length)}枚までです`,
      ]);
      return;
    }

    setValidationErrors([]);
    setImageFiles([...imageFiles, ...selectedFiles]);
    // プレビューは各ファイルのFileReader完了を待たず、まず選択枚数分のプレースホルダーを
    // 追加して即座に一覧へ反映する（完了順が選択順と一致しない場合があるため、インデックスを
    // 固定して差し替えることで表示順のずれを防ぐ）
    const startIndex = imagePreviews.length;
    setImagePreviews([...imagePreviews, ...selectedFiles.map(() => "")]);
    selectedFiles.forEach((file, i) => {
      const reader = new FileReader();
      reader.onload = () => {
        setImagePreviews((prev) => {
          const next = [...prev];
          next[startIndex + i] = reader.result as string;
          return next;
        });
      };
      reader.readAsDataURL(file);
    });
  };

  /**
   * 選択済み画像の削除（新規登録モードのみ）
   */
  const handleRemoveImage = (index: number) => {
    setImageFiles(imageFiles.filter((_, i) => i !== index));
    setImagePreviews(imagePreviews.filter((_, i) => i !== index));
  };

  /**
   * タグ追加
   */
  const handleAddTag = () => {
    setTags([
      ...tags,
      { tagNo: nextTagNo, isNew: true, tagJapaneseName: "", tagEnglishName: "" },
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
    if (!isEditMode) {
      if (imageFiles.length === 0) {
        errors.push("画像ファイルを選択してください");
      }
      if (imageFiles.length > maxSelectable) {
        errors.push(`一度に登録できる写真は${maxSelectable}枚までです`);
      }
      if (imageFiles.some((file) => file.size > MAX_IMAGE_FILE_SIZE)) {
        errors.push("画像ファイルは5MB以下にしてください");
      }
    }
    // backend側は新規登録・更新のいずれも photoJapaneseTitle に @NotBlank を付与しているため、
    // isEditMode を問わず必須にする
    if (!photoJapaneseTitle.trim()) {
      errors.push("タイトル（日本語）を入力してください");
    }
    if (photoAt) {
      const photoDate = new Date(photoAt);
      if (photoDate > new Date()) {
        errors.push("撮影日時は過去の日時を指定してください");
      }
    }
    const positiveNumberChecks: { value: string; label: string }[] = [
      { value: focalLength, label: "焦点距離" },
      { value: fValue, label: "F値" },
      { value: shutterSpeed, label: "シャッタースピード" },
      { value: iso, label: "ISO" },
    ];
    for (const { value, label } of positiveNumberChecks) {
      if (!value) continue;
      const num = Number(value);
      if (!Number.isFinite(num) || num <= 0) {
        errors.push(`${label}は正の数値を入力してください`);
      }
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
    if (isSaving) return;
    // 送信中のセッション失効に備えた防御的チェック（通常はガードで弾かれる）
    if (!user) {
      setError("セッションが切れました。お手数ですが再度ログインしてください");
      return;
    }
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

      if (isEditMode && savedPhotoNo) {
        formData.append("photoNo", String(savedPhotoNo));
      }
      if (isEditMode) {
        // 向き区分は更新時のみ送信する（新規登録時はバックエンドが画像の実際のピクセルサイズから判定するため、
        // 複数枚の向きが混在してもクライアントの単一選択と食い違わない）
        formData.append("directionKbn", directionKbn);
        if (existingImageFilePath) {
          formData.append("imageFilePath", existingImageFilePath);
        }
      } else {
        // 新規一括登録：複数ファイルを同じフィールド名で送信する（共通のタイトル〜タグは1セットのみ）
        imageFiles.forEach((file) => formData.append("imageFiles", file));
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
      formData.append("isLocationPublic", String(isLocationPublic));

      tags.forEach((tag, index) => {
        formData.append(
          `photoTagRegistRequestList[${index}].accountNo`,
          String(user.accountNo)
        );
        if (isEditMode && savedPhotoNo) {
          formData.append(
            `photoTagRegistRequestList[${index}].photoNo`,
            String(savedPhotoNo)
          );
        }
        // 新規タグはクライアント採番値を送らない（バックエンドは保存時に
        // タグ番号を 1..N へ振り直すため、既存レコードの主キーと解釈されうる
        // 値を送るのを避ける）。既存タグは実タグ番号をそのまま送る。
        if (!tag.isNew) {
          formData.append(
            `photoTagRegistRequestList[${index}].tagNo`,
            String(tag.tagNo)
          );
        }
        formData.append(
          `photoTagRegistRequestList[${index}].tagJapaneseName`,
          tag.tagJapaneseName
        );
        formData.append(
          `photoTagRegistRequestList[${index}].tagEnglishName`,
          tag.tagEnglishName || ""
        );
      });

      if (isEditMode) {
        const result = await savePhoto(photoAccountId, formData);
        setSavedPhotoNo(result.photoNo);
        setExistingImageFilePath(result.imageFilePath);
        setSuccessMessage("写真を保存しました");
        setIsBulkRegistSucceeded(false);
      } else {
        const result = await registPhotos(photoAccountId, formData);
        setSuccessMessage(`${result.registeredCount}枚の写真を保存しました`);
        setIsBulkRegistSucceeded(true);
      }
      setShowSuccessModal(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsSaving(false);
    }
  };

  /**
   * 成功モーダルを閉じる
   *
   * 新規一括登録の直後は、このフォームで続けて編集できる対象が存在しないため一覧へ戻る。
   * 更新の場合は同じ写真の編集を続けられるよう、モーダルを閉じるだけに留める
   */
  const handleCloseSuccessModal = () => {
    setShowSuccessModal(false);
    if (isBulkRegistSucceeded) {
      router.push(`/photo/${photoAccountId}/photo_list`);
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

  if (user?.accountId !== photoAccountId || isForbidden) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-red-500">この操作を行う権限がありません</p>
      </div>
    );
  }

  // 編集モードの既存写真プレビュー用 src（サニタイズ済み URL）。
  // サニタイズで空になる（＝許可されない URL）場合は null にしてプレビューを出さない。
  const previewSrc = sanitizeImageUrl(imagePreview) || null;
  const remainingSelectable = Math.max(0, maxSelectable - imageFiles.length);

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
          <div className="mb-4 p-3 border border-red-500 text-red-500" role="alert">
            {error}
          </div>
        )}

        {validationErrors.length > 0 && (
          <div
            className="mb-4 p-3 border border-red-500 text-red-500"
            data-testid="validation-errors"
            role="alert"
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
            {isEditMode ? (
              // 画像ファイルは登録後は差し替え不可（バックエンド仕様）。編集モードでは
              // 選択操作自体をUI上から無くし、既存画像のプレビュー表示のみを行う
              previewSrc && (
                <div className="mt-2 flex flex-col items-center">
                  <img
                    src={previewSrc}
                    alt="プレビュー"
                    className="max-w-full max-h-[300px]"
                    style={{ objectFit: "contain" }}
                    data-testid="image-preview"
                  />
                  <p className="text-sm text-gray-400 mt-1">
                    画像ファイルは登録後に変更できません
                  </p>
                </div>
              )
            ) : (
              <>
                {/* 新規登録時のみ複数ファイルを選択できる。写真ごとに個別のメタデータは
                    設定できず、タイトル〜タグは選択した全ての写真に共通で登録される */}
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  className="hidden"
                  aria-label="画像ファイル"
                  data-testid="image-input"
                />
                {imagePreviews.length > 0 && (
                  <div className="grid grid-cols-3 gap-2 mt-2 mb-2">
                    {imagePreviews.map((src, index) => (
                      <div
                        key={index}
                        className="relative min-h-24 bg-gray-800"
                        data-testid={`image-preview-item-${index}`}
                      >
                        {src && (
                          <img
                            src={src}
                            alt={`プレビュー${index + 1}`}
                            className="w-full h-24 object-cover border border-gray-600"
                            data-testid={`image-preview-${index}`}
                          />
                        )}
                        <button
                          type="button"
                          onClick={() => handleRemoveImage(index)}
                          className="absolute top-0 right-0 bg-red-600 text-white w-5 h-5 leading-none border-none cursor-pointer hover:bg-red-700"
                          aria-label={`${index + 1}枚目の画像を削除`}
                          data-testid={`remove-image-${index}`}
                        >
                          ×
                        </button>
                      </div>
                    ))}
                  </div>
                )}
                {remainingSelectable > 0 ? (
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="bg-gray-700 text-white px-4 py-2 border border-gray-600 cursor-pointer hover:bg-gray-600"
                  >
                    {imageFiles.length > 0 ? "＋ファイルを追加" : "ファイルを選択"}
                  </button>
                ) : (
                  <p className="text-sm text-gray-400">
                    登録枚数の上限に達しているため、これ以上写真を追加できません
                  </p>
                )}
                {remainingCount !== null && remainingCount !== undefined && (
                  <p className="text-xs text-gray-400 mt-1">
                    今回はあと{remainingSelectable}枚まで選択できます（アカウントの残り登録可能枚数:
                    {remainingCount}枚）
                  </p>
                )}
              </>
            )}
          </div>

          {/* タイトル（日本語） */}
          <div className="mb-4">
            <label htmlFor="photo-japanese-title" className="block text-sm text-gray-400 mb-1">
              タイトル（日本語）
            </label>
            <input
              id="photo-japanese-title"
              type="text"
              value={photoJapaneseTitle}
              onChange={(e) => setPhotoJapaneseTitle(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="japanese-title-input"
            />
          </div>

          {/* タイトル（英語） */}
          <div className="mb-4">
            <label htmlFor="photo-english-title" className="block text-sm text-gray-400 mb-1">
              タイトル（英語）
            </label>
            <input
              id="photo-english-title"
              type="text"
              value={photoEnglishTitle}
              onChange={(e) => setPhotoEnglishTitle(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="english-title-input"
            />
          </div>

          {/* 撮影日時 */}
          <div className="mb-4">
            <label htmlFor="photo-at" className="block text-sm text-gray-400 mb-1">
              撮影日時
            </label>
            <input
              id="photo-at"
              type="datetime-local"
              value={photoAt}
              onChange={(e) => setPhotoAt(e.target.value)}
              className="w-full bg-gray-800 text-white border border-gray-600 p-2"
              data-testid="photo-at-input"
            />
          </div>

          {/* 向き（編集時のみ。新規登録時はバックエンドが画像の実際のピクセルサイズから判定するため選択不要） */}
          {isEditMode && (
            <div className="mb-4">
              <label htmlFor="photo-direction" className="block text-sm text-gray-400 mb-1">向き *</label>
              <select
                id="photo-direction"
                value={directionKbn}
                onChange={(e) => setDirectionKbn(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="direction-select"
              >
                <option value="horizontal">横</option>
                <option value="vertical">縦</option>
              </select>
            </div>
          )}

          {/* EXIF情報 */}
          {!isEditMode && (
            <p className="text-xs text-gray-400 mb-2">
              画像ファイルに焦点距離、F値、シャッタースピード、ISOの情報があれば自動登録されます
            </p>
          )}
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label htmlFor="photo-focal-length" className="block text-sm text-gray-400 mb-1">
                焦点距離 (mm)
              </label>
              <input
                id="photo-focal-length"
                type="number"
                value={focalLength}
                onChange={(e) => setFocalLength(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="focal-length-input"
              />
            </div>
            <div>
              <label htmlFor="photo-f-value" className="block text-sm text-gray-400 mb-1">F値</label>
              <input
                id="photo-f-value"
                type="number"
                step="0.1"
                value={fValue}
                onChange={(e) => setFValue(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="f-value-input"
              />
            </div>
            <div>
              <label htmlFor="photo-shutter-speed" className="block text-sm text-gray-400 mb-1">
                シャッタースピード (秒)
              </label>
              <input
                id="photo-shutter-speed"
                type="number"
                step="0.0001"
                value={shutterSpeed}
                onChange={(e) => setShutterSpeed(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="shutter-speed-input"
              />
            </div>
            <div>
              <label htmlFor="photo-iso" className="block text-sm text-gray-400 mb-1">ISO</label>
              <input
                id="photo-iso"
                type="number"
                value={iso}
                onChange={(e) => setIso(e.target.value)}
                className="w-full bg-gray-800 text-white border border-gray-600 p-2"
                data-testid="iso-input"
              />
            </div>
          </div>

          {/* 位置情報の公開設定 */}
          <div className="mb-4">
            <label className="flex items-center gap-2 text-sm text-gray-400">
              <input
                type="checkbox"
                checked={isLocationPublic}
                onChange={(e) => setIsLocationPublic(e.target.checked)}
                data-testid="location-public-checkbox"
              />
              撮影場所（緯度経度・住所・ロケーション名）を他のユーザーにも公開する
            </label>
            <p className="text-xs text-gray-400 mt-1">
              オフの場合、撮影場所は本人にのみ表示されます。
            </p>
          </div>

          {/* キャプション */}
          <div className="mb-4">
            <label htmlFor="photo-caption" className="block text-sm text-gray-400 mb-1">
              キャプション
            </label>
            <textarea
              id="photo-caption"
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
                  aria-label="タグ名（日本語）"
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
                  aria-label="タグ名（英語）"
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
        <ModalDialog
          testId="success-modal"
          label="保存完了"
          onClose={handleCloseSuccessModal}
          overlayClassName="fixed inset-0 bg-black/70 flex justify-center items-center z-50"
          containerClassName="bg-gray-900 border border-gray-700 p-6 max-w-sm w-full mx-4 text-center relative"
        >
          <button
            type="button"
            aria-label="閉じる"
            onClick={handleCloseSuccessModal}
            className="absolute top-2 right-3 text-xl text-gray-400 bg-transparent border-none cursor-pointer"
          >
            &times;
          </button>
          <p className="text-white">{successMessage}</p>
        </ModalDialog>
      )}
    </div>
  );
}
