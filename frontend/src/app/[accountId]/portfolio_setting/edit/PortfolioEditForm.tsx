"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import {
  deletePortfolio,
  getPortfolioDetail,
  getPortfolioTemplateList,
  savePortfolio,
  updatePortfolioStatus,
  type PortfolioDetail,
  type PortfolioSaveData,
  type PortfolioStatusKbn,
  type PortfolioTemplateKbn,
  type PortfolioTemplateList,
} from "@/lib/api/client";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { sanitizeImageUrl } from "@/lib/url";
import {
  PORTFOLIO_DESCRIPTION_MAX_LENGTH,
  PORTFOLIO_TAG_MAX_SIZE,
  PORTFOLIO_THEME_MAX_LENGTH,
  TAG_NAME_MAX_LENGTH,
} from "@/lib/validation";
import { PORTFOLIO_STATUS_LABELS, PORTFOLIO_TEMPLATE_LABELS } from "../labels";
import { FeaturePreview } from "./FeaturePreview";
import { GridPreview } from "./GridPreview";
import { PhotoPickerDialog, type PickedPhoto } from "./PhotoPickerDialog";
import {
  EMPTY_PHOTO_SETTINGS,
  GRID_ROW_PATTERN_LABELS,
  GRID_ROW_PATTERN_PHOTO_COUNT,
  DEFAULT_ASPECT_RATIO,
  DIRECTION_ASPECT_RATIOS,
  DIRECTION_LABELS,
  PHOTO_SETTING_KEYS,
  type PhotoSettingValues,
  type SelectedPhoto,
} from "./types";

interface PortfolioEditFormProps {
  /** URLのアカウントID */
  accountId: string;
  /** 編集対象のポートフォリオ番号（新規作成の場合は未指定） */
  initialPortfolioNo?: number;
}

/** タグの入力値 */
interface TagInput {
  tagJapaneseName: string;
  tagEnglishName: string;
}

/** 新しく追加する行の既定の行パターン */
const DEFAULT_GRID_ROW_PATTERN = "TWO_EQUAL";

/**
 * 写真単位の設定（JSON）を入力値に変換する。文字列以外の値・未知の項目は無視する
 *
 * @param settings ポートフォリオ詳細APIの写真単位の設定
 * @returns 入力値
 */
function toPhotoSettingValues(settings: Record<string, unknown>): PhotoSettingValues {
  const values = { ...EMPTY_PHOTO_SETTINGS };
  (Object.keys(values) as (keyof PhotoSettingValues)[]).forEach((key) => {
    const value = settings[key];
    if (typeof value === "string") values[key] = value;
  });
  return values;
}

/**
 * グリッド型のポートフォリオ単位の設定（JSON）から行パターンの一覧を取り出す
 *
 * @param templateSettings ポートフォリオ詳細APIのポートフォリオ単位の設定
 * @returns 行パターンの一覧
 */
function toGridRows(templateSettings: Record<string, unknown>): string[] {
  const rows = templateSettings.rows;
  if (!Array.isArray(rows)) return [];
  return rows
    .map((row) =>
      row && typeof row === "object" && typeof (row as { pattern?: unknown }).pattern === "string"
        ? (row as { pattern: string }).pattern
        : null
    )
    .filter((pattern): pattern is string => pattern !== null);
}

/**
 * ポートフォリオ詳細APIの内容を、フォームの入力値に変換する
 *
 * @param detail ポートフォリオ詳細
 * @returns フォームの入力値
 */
function toFormState(detail: PortfolioDetail) {
  return {
    portfolioNo: detail.portfolioNo,
    statusKbn: detail.statusKbn,
    theme: detail.theme,
    description: detail.description,
    templateKbn: detail.templateKbn,
    gridRows: toGridRows(detail.templateSettings),
    photos: detail.photoList.map(
      (photo): SelectedPhoto => ({
        photoNo: photo.photoNo,
        imageFilePath: photo.imageFilePath,
        label: photo.photoJapaneseTitle || `写真${photo.photoNo}`,
        directionKbn: photo.directionKbn,
        settings: toPhotoSettingValues(photo.photoSettings),
      })
    ),
    tags: detail.tagList.map(
      (tag): TagInput => ({
        tagJapaneseName: tag.tagJapaneseName,
        tagEnglishName: tag.tagEnglishName,
      })
    ),
  };
}

/**
 * ポートフォリオの作成・編集フォーム
 *
 * テンプレートごとに、写真単位の設定（フィーチャー型：テーマ・サブテーマ・説明。グリッド型は持たない）と
 * ポートフォリオ単位の設定（グリッド型：行パターン）を入力する。保存は常に下書きの内容として行い、
 * 公開状態は保存済みのポートフォリオに対して別途切り替える（未保存の変更がある間は切り替えさせない）。
 */
export function PortfolioEditForm({ accountId, initialPortfolioNo }: PortfolioEditFormProps) {
  const router = useRouter();
  const { user } = useAuth();
  const isOwner = !!user && user.accountId === accountId;

  const [portfolioNo, setPortfolioNo] = useState<number | undefined>(initialPortfolioNo);
  const [statusKbn, setStatusKbn] = useState<PortfolioStatusKbn>("draft");
  const [theme, setTheme] = useState("");
  const [description, setDescription] = useState("");
  const [templateKbn, setTemplateKbn] = useState<PortfolioTemplateKbn>("feature");
  const [photos, setPhotos] = useState<SelectedPhoto[]>([]);
  const [gridRows, setGridRows] = useState<string[]>([]);
  const [tags, setTags] = useState<TagInput[]>([]);
  const [templateList, setTemplateList] = useState<PortfolioTemplateList | null>(null);

  const [isLoading, setIsLoading] = useState(initialPortfolioNo !== undefined);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  // 保存後に入力内容を変更したかどうか（未保存の変更がある間は公開状態を切り替えさせない）
  const [isDirty, setIsDirty] = useState(initialPortfolioNo === undefined);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showPicker, setShowPicker] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  // 取得リクエストの世代。後着レスポンスが入力中の内容を上書きする競合を防ぐ
  const loadSeqRef = useRef(0);
  // このフォームで新規登録したポートフォリオ番号。登録後の編集用URLへの切り替えでは、
  // フォームが保存済みの内容を保持しているため読み込み直さない（読み込みの応答が、
  // その間に切り替えた公開状態などを登録直後の内容で上書きするのを防ぐ）
  const registeredPortfolioNoRef = useRef<number | undefined>(undefined);

  // 編集時は登録済みの内容を読み込む
  useEffect(() => {
    if (!isOwner || initialPortfolioNo === undefined) return;
    if (initialPortfolioNo === registeredPortfolioNoRef.current) return;
    const seq = ++loadSeqRef.current;
    const load = async () => {
      try {
        const detail = await getPortfolioDetail(accountId, initialPortfolioNo);
        if (loadSeqRef.current !== seq) return;
        const form = toFormState(detail);
        setPortfolioNo(form.portfolioNo);
        setStatusKbn(form.statusKbn);
        setTheme(form.theme);
        setDescription(form.description);
        setTemplateKbn(form.templateKbn);
        setGridRows(form.gridRows);
        setPhotos(form.photos);
        setTags(form.tags);
        setIsDirty(false);
      } catch (err) {
        if (loadSeqRef.current !== seq) return;
        setLoadError(err instanceof Error ? err.message : "エラーが発生しました");
      } finally {
        if (loadSeqRef.current === seq) setIsLoading(false);
      }
    };
    load();
    return () => {
      loadSeqRef.current++;
    };
  }, [isOwner, accountId, initialPortfolioNo]);

  // テンプレート・行パターンの一覧（取得失敗時は定義済みの既定値で表示を続ける）
  useEffect(() => {
    if (!isOwner) return;
    let cancelled = false;
    getPortfolioTemplateList()
      .then((data) => {
        if (!cancelled) setTemplateList(data);
      })
      .catch(() => {
        // 表示名・選択肢は画面側の既定値で代替できるため、通知しない
      });
    return () => {
      cancelled = true;
    };
  }, [isOwner]);

  /** 入力内容を変更したことを記録し、前回の保存結果の通知を消す */
  const markDirty = () => {
    setIsDirty(true);
    setSuccessMessage("");
  };

  const templateOptions: { templateKbn: PortfolioTemplateKbn; displayName: string }[] =
    templateList?.templateList ??
    (Object.keys(PORTFOLIO_TEMPLATE_LABELS) as PortfolioTemplateKbn[]).map((kbn) => ({
      templateKbn: kbn,
      displayName: PORTFOLIO_TEMPLATE_LABELS[kbn],
    }));
  const rowPatternOptions =
    templateList?.gridRowPatternList ??
    Object.keys(GRID_ROW_PATTERN_PHOTO_COUNT).map((pattern) => ({
      pattern,
      displayName: GRID_ROW_PATTERN_LABELS[pattern],
      photoCount: GRID_ROW_PATTERN_PHOTO_COUNT[pattern],
    }));
  const rowPhotoCount = (pattern: string): number =>
    rowPatternOptions.find((option) => option.pattern === pattern)?.photoCount ??
    GRID_ROW_PATTERN_PHOTO_COUNT[pattern] ??
    0;
  const gridTotalPhotoCount = gridRows.reduce((sum, pattern) => sum + rowPhotoCount(pattern), 0);

  /**
   * 写真選択ダイアログで確定した選択を反映する
   *
   * 選択済みのままの写真は入力済みの設定を引き継ぎ、新しく選んだ写真は表示順の末尾に追加する
   * （ダイアログが返す順は、開く前の表示順＋新しく選んだ順）。選択が変わらなかった場合は未保存の変更としない。
   *
   * @param picked ダイアログで確定した写真（表示順）
   */
  const handleCompletePick = (picked: PickedPhoto[]) => {
    setShowPicker(false);
    const isUnchanged =
      picked.length === photos.length && picked.every((p, i) => p.photoNo === photos[i].photoNo);
    if (isUnchanged) return;
    markDirty();
    setPhotos((prev) =>
      picked.map(
        (p) =>
          prev.find((photo) => photo.photoNo === p.photoNo) ?? {
            ...p,
            settings: { ...EMPTY_PHOTO_SETTINGS },
          }
      )
    );
  };

  /**
   * 写真の表示順を入れ替える
   *
   * @param index 移動する写真の位置
   * @param offset 移動量（-1：上へ、1：下へ）
   */
  const movePhoto = (index: number, offset: -1 | 1) => {
    const target = index + offset;
    if (target < 0 || target >= photos.length) return;
    markDirty();
    setPhotos((prev) => {
      const next = [...prev];
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  };

  const removePhoto = (photoNo: number) => {
    markDirty();
    setPhotos((prev) => prev.filter((p) => p.photoNo !== photoNo));
  };

  const updatePhotoSetting = (photoNo: number, key: keyof PhotoSettingValues, value: string) => {
    markDirty();
    setPhotos((prev) =>
      prev.map((p) => (p.photoNo === photoNo ? { ...p, settings: { ...p.settings, [key]: value } } : p))
    );
  };

  /** 入力内容を検証し、エラーがあれば項目ごとのメッセージを設定する */
  const validate = (): boolean => {
    const nextErrors: Record<string, string> = {};
    if (!theme.trim()) {
      nextErrors.theme = "テーマを入力してください";
    }
    if (photos.length === 0) {
      // 下書きでも写真のないポートフォリオは保存できない（バックエンドも400で拒否する）
      nextErrors.photos = "写真を1枚以上選択してください";
    }
    tags.forEach((tag, index) => {
      if (!tag.tagJapaneseName.trim()) {
        nextErrors[`tag-${index}`] = "タグ（日本語）を入力してください";
      } else if (/[ 　]/.test(tag.tagJapaneseName) || /[ 　]/.test(tag.tagEnglishName)) {
        nextErrors[`tag-${index}`] = "スペースは使用できません";
      }
    });
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  /** 保存するリクエストを組み立てる（現在のテンプレートの項目だけを送る） */
  const buildSaveData = (): PortfolioSaveData => ({
    theme: theme.trim(),
    description,
    templateKbn,
    templateSettings:
      templateKbn === "grid" ? { rows: gridRows.map((pattern) => ({ pattern })) } : {},
    photoList: photos.map((photo) => ({
      photoNo: photo.photoNo,
      photoSettings: Object.fromEntries(
        PHOTO_SETTING_KEYS[templateKbn]
          .map((key) => [key, photo.settings[key]] as const)
          .filter(([, value]) => value !== "")
      ),
    })),
    tagList: tags.map((tag) => ({
      tagJapaneseName: tag.tagJapaneseName.trim(),
      tagEnglishName: tag.tagEnglishName.trim(),
    })),
  });

  const handleSave = async () => {
    if (isSubmitting) return;
    setSubmitError("");
    setSuccessMessage("");
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const result = await savePortfolio(accountId, buildSaveData(), portfolioNo);
      setIsDirty(false);
      setSuccessMessage(result.message);
      if (portfolioNo === undefined) {
        // 新規登録後は、以降の保存・公開が同じポートフォリオに対して行われるよう編集用URLへ切り替える
        registeredPortfolioNoRef.current = result.portfolioNo;
        setPortfolioNo(result.portfolioNo);
        router.replace(`/${accountId}/portfolio_setting/edit?portfolioNo=${result.portfolioNo}`);
      }
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChangeStatus = async (nextStatus: PortfolioStatusKbn) => {
    if (isSubmitting || portfolioNo === undefined || isDirty) return;
    setSubmitError("");
    setSuccessMessage("");
    setIsSubmitting(true);
    try {
      const result = await updatePortfolioStatus(accountId, portfolioNo, nextStatus);
      setStatusKbn(nextStatus);
      setSuccessMessage(result.message);
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (isSubmitting || portfolioNo === undefined) return;
    setDeleteError("");
    setIsSubmitting(true);
    try {
      await deletePortfolio(accountId, portfolioNo);
      router.push(`/${accountId}/portfolio_setting`);
    } catch (err) {
      // 削除失敗時は確認ダイアログを開いたままエラーを通知する
      setDeleteError(err instanceof Error ? err.message : "エラーが発生しました");
      setIsSubmitting(false);
    }
  };

  // ログイン済みだが他人のポートフォリオ設定を開こうとした場合（認可はバックエンドでも実施）
  if (!isOwner) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-700">この操作を行う権限がありません</p>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-4">
        <p className="text-red-700">{loadError}</p>
        <Link href={`/${accountId}/portfolio_setting`} className="text-blue-700 hover:underline">
          ポートフォリオ一覧へ戻る
        </Link>
      </div>
    );
  }

  const inputClass = "w-full border border-gray-300 rounded px-3 py-2";

  /** プレビューを表示するボタン（フィーチャー型は「写真を選択」の右、グリッド型は「＋行を追加」の右に置く） */
  const previewButton = (
    <button
      type="button"
      onClick={() => setShowPreview(true)}
      className="px-4 py-2 text-sm border border-blue-700 text-blue-700 rounded hover:bg-blue-50"
    >
      プレビューを表示
    </button>
  );

  /** 「写真を選択」ボタン（フィーチャー型は写真があるときプレビューを右に並べる） */
  const photoActions = (
    <div className="flex flex-wrap gap-3">
      <button
        type="button"
        onClick={() => setShowPicker(true)}
        className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
      >
        写真を選択
      </button>
      {templateKbn === "feature" && photos.length > 0 && previewButton}
    </div>
  );

  return (
    <div className="flex flex-col items-center py-8 px-4">
      <div className="w-full max-w-[900px] flex flex-col gap-6">
        <Link
          href={`/${accountId}/portfolio_setting`}
          className="text-blue-700 hover:underline text-sm self-start"
        >
          &larr; ポートフォリオ一覧へ戻る
        </Link>
        <div className="flex flex-wrap justify-between items-center gap-2">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold">
              {portfolioNo === undefined ? "ポートフォリオ作成" : "ポートフォリオ編集"}
            </h1>
            {portfolioNo !== undefined && (
              <span
                data-testid="portfolio-status"
                className={
                  statusKbn === "published"
                    ? "inline-block px-2 py-0.5 text-xs bg-green-700 text-white rounded-full"
                    : "inline-block px-2 py-0.5 text-xs bg-gray-600 text-white rounded-full"
                }
              >
                {PORTFOLIO_STATUS_LABELS[statusKbn]}
              </span>
            )}
          </div>
          {portfolioNo !== undefined && (
            <button
              type="button"
              onClick={() => {
                setDeleteError("");
                setShowDeleteConfirm(true);
              }}
              disabled={isSubmitting}
              className="px-6 py-2 border border-red-500 text-red-700 rounded hover:bg-red-50 disabled:opacity-50"
            >
              削除
            </button>
          )}
        </div>

        {/* 基本情報（テーマ・説明文） */}
        <section className="flex flex-col gap-4">
          <div>
            <label htmlFor="portfolio-theme" className="block font-bold mb-1">
              テーマ<span className="text-red-700">*</span>
            </label>
            <input
              id="portfolio-theme"
              type="text"
              value={theme}
              maxLength={PORTFOLIO_THEME_MAX_LENGTH}
              aria-invalid={!!errors.theme}
              aria-describedby={errors.theme ? "portfolio-theme-error" : undefined}
              onChange={(e) => {
                markDirty();
                setTheme(e.target.value);
              }}
              className={inputClass}
            />
            {errors.theme && (
              <p id="portfolio-theme-error" className="text-red-700 text-sm mt-1">
                {errors.theme}
              </p>
            )}
          </div>
          <div>
            <label htmlFor="portfolio-description" className="block font-bold mb-1">
              説明文
            </label>
            <textarea
              id="portfolio-description"
              value={description}
              maxLength={PORTFOLIO_DESCRIPTION_MAX_LENGTH}
              rows={4}
              onChange={(e) => {
                markDirty();
                setDescription(e.target.value);
              }}
              className={inputClass}
            />
          </div>
        </section>

        {/* タグ */}
        <section className="flex flex-col gap-3" aria-labelledby="portfolio-tags-heading">
          <h2 id="portfolio-tags-heading" className="font-bold">
            タグ
          </h2>
          {tags.map((tag, index) => (
            <div key={index} className="flex flex-col gap-1">
              <div className="flex gap-2 items-center">
                <input
                  type="text"
                  aria-label={`タグ${index + 1}（日本語）`}
                  placeholder="日本語"
                  value={tag.tagJapaneseName}
                  maxLength={TAG_NAME_MAX_LENGTH}
                  onChange={(e) => {
                    markDirty();
                    const value = e.target.value;
                    setTags((prev) =>
                      prev.map((t, i) => (i === index ? { ...t, tagJapaneseName: value } : t))
                    );
                  }}
                  className={inputClass}
                />
                <input
                  type="text"
                  aria-label={`タグ${index + 1}（英語）`}
                  placeholder="英語"
                  value={tag.tagEnglishName}
                  maxLength={TAG_NAME_MAX_LENGTH}
                  onChange={(e) => {
                    markDirty();
                    const value = e.target.value;
                    setTags((prev) =>
                      prev.map((t, i) => (i === index ? { ...t, tagEnglishName: value } : t))
                    );
                  }}
                  className={inputClass}
                />
                <button
                  type="button"
                  aria-label={`タグ${index + 1}を削除`}
                  onClick={() => {
                    markDirty();
                    setTags((prev) => prev.filter((_, i) => i !== index));
                  }}
                  className="px-2 py-1 text-sm border border-red-400 text-red-700 rounded"
                >
                  ×
                </button>
              </div>
              {errors[`tag-${index}`] && (
                <p className="text-red-700 text-sm">{errors[`tag-${index}`]}</p>
              )}
            </div>
          ))}
          <div>
            <button
              type="button"
              disabled={tags.length >= PORTFOLIO_TAG_MAX_SIZE}
              onClick={() => {
                markDirty();
                setTags((prev) => [...prev, { tagJapaneseName: "", tagEnglishName: "" }]);
              }}
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800 disabled:opacity-50"
            >
              ＋タグを追加
            </button>
          </div>
        </section>

        {/* テンプレート */}
        <section className="flex flex-col gap-4">
          <fieldset>
            <legend className="font-bold mb-1">テンプレート</legend>
            <div className="flex gap-6">
              {templateOptions.map((option) => (
                <label key={option.templateKbn} className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="portfolio-template"
                    value={option.templateKbn}
                    checked={templateKbn === option.templateKbn}
                    onChange={() => {
                      markDirty();
                      setTemplateKbn(option.templateKbn);
                    }}
                  />
                  {option.displayName}
                </label>
              ))}
            </div>
          </fieldset>
        </section>

        {/* グリッド型の行パターン（テンプレートの下、写真より上） */}
        {templateKbn === "grid" && (
          <section className="flex flex-col gap-3" aria-labelledby="portfolio-rows-heading">
            <h2 id="portfolio-rows-heading" className="font-bold">
              行の組み合わせ
            </h2>
            <p
              data-testid="grid-photo-count"
              className={
                gridTotalPhotoCount === photos.length && photos.length > 0
                  ? "text-sm text-green-700"
                  : "text-sm text-red-700"
              }
            >
              行の枚数の合計 {gridTotalPhotoCount}枚 / 写真 {photos.length}枚
              {gridTotalPhotoCount !== photos.length && "（公開するには一致させてください）"}
            </p>
            <ol className="flex flex-col gap-2">
              {gridRows.map((pattern, index) => (
                <li key={index} className="flex items-center gap-2" data-testid="grid-row-item">
                  <span className="text-sm w-12">{index + 1}行目</span>
                  <select
                    aria-label={`${index + 1}行目の組み合わせ`}
                    value={pattern}
                    onChange={(e) => {
                      markDirty();
                      const value = e.target.value;
                      setGridRows((prev) => prev.map((p, i) => (i === index ? value : p)));
                    }}
                    className="border border-gray-300 rounded px-2 py-1"
                  >
                    {rowPatternOptions.map((option) => (
                      <option key={option.pattern} value={option.pattern}>
                        {option.displayName}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    aria-label={`${index + 1}行目を削除`}
                    onClick={() => {
                      markDirty();
                      setGridRows((prev) => prev.filter((_, i) => i !== index));
                    }}
                    className="px-2 py-1 text-sm border border-red-400 text-red-700 rounded"
                  >
                    ×
                  </button>
                </li>
              ))}
            </ol>
            {/* 行の追加は行の一覧の下に置き、行があるときはプレビューを右に並べる */}
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => {
                  markDirty();
                  setGridRows((prev) => [...prev, DEFAULT_GRID_ROW_PATTERN]);
                }}
                className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
              >
                ＋行を追加
              </button>
              {gridRows.length > 0 && previewButton}
            </div>
          </section>
        )}

        {/* 写真（「写真を選択」は、未選択の間は見出しの下、選択後は写真の一覧の下に置く） */}
        <section className="flex flex-col gap-3" aria-labelledby="portfolio-photos-heading">
          <h2 id="portfolio-photos-heading" className="font-bold">
            写真（{photos.length}枚）
          </h2>
          {errors.photos && (
            <p id="portfolio-photos-error" className="text-red-700 text-sm">
              {errors.photos}
            </p>
          )}
          {photos.length === 0 ? (
            <>
              {photoActions}
              <p className="text-gray-600 text-sm">写真が選択されていません</p>
            </>
          ) : (
            <ol className="flex flex-col gap-3">
              {photos.map((photo, index) => {
                const src = sanitizeImageUrl(photo.imageFilePath);
                return (
                  <li
                    key={photo.photoNo}
                    data-testid="portfolio-photo-item"
                    className="flex gap-3 border border-gray-200 rounded p-3"
                  >
                    {/* 枠を写真の向きに合わせた縦横比にし、向きの表示名も添える */}
                    <div
                      data-testid="portfolio-photo-frame"
                      style={{
                        aspectRatio: DIRECTION_ASPECT_RATIOS[photo.directionKbn] ?? DEFAULT_ASPECT_RATIO,
                      }}
                      className="h-24 flex-shrink-0 bg-gray-200 rounded overflow-hidden flex items-center justify-center"
                    >
                      {src ? (
                        <img src={src} alt={photo.label} className="w-full h-full object-cover" />
                      ) : (
                        <span className="text-xs text-gray-600">{photo.label}</span>
                      )}
                    </div>
                    <div className="flex-1 flex flex-col gap-2">
                      <p className="text-sm text-gray-600">
                        {index + 1}. {photo.label}
                        {DIRECTION_LABELS[photo.directionKbn] && (
                          <span
                            data-testid="portfolio-photo-direction"
                            className="ml-2 inline-block px-1.5 py-0.5 text-xs border border-gray-400 rounded"
                          >
                            {DIRECTION_LABELS[photo.directionKbn]}
                          </span>
                        )}
                      </p>
                      {templateKbn === "feature" && (
                        <>
                          <input
                            type="text"
                            aria-label={`${photo.label}のテーマ`}
                            placeholder="テーマ（公開時は必須）"
                            value={photo.settings.theme}
                            maxLength={PORTFOLIO_THEME_MAX_LENGTH}
                            onChange={(e) => updatePhotoSetting(photo.photoNo, "theme", e.target.value)}
                            className={inputClass}
                          />
                          <input
                            type="text"
                            aria-label={`${photo.label}のサブテーマ`}
                            placeholder="サブテーマ"
                            value={photo.settings.subTheme}
                            maxLength={PORTFOLIO_THEME_MAX_LENGTH}
                            onChange={(e) =>
                              updatePhotoSetting(photo.photoNo, "subTheme", e.target.value)
                            }
                            className={inputClass}
                          />
                          <textarea
                            aria-label={`${photo.label}の説明`}
                            placeholder="説明"
                            value={photo.settings.description}
                            maxLength={PORTFOLIO_DESCRIPTION_MAX_LENGTH}
                            rows={2}
                            onChange={(e) =>
                              updatePhotoSetting(photo.photoNo, "description", e.target.value)
                            }
                            className={inputClass}
                          />
                        </>
                      )}
                    </div>
                    <div className="flex flex-col gap-1">
                      <button
                        type="button"
                        aria-label={`${photo.label}を上へ移動`}
                        disabled={index === 0}
                        onClick={() => movePhoto(index, -1)}
                        className="px-2 py-1 text-sm border rounded disabled:opacity-40"
                      >
                        ↑
                      </button>
                      <button
                        type="button"
                        aria-label={`${photo.label}を下へ移動`}
                        disabled={index === photos.length - 1}
                        onClick={() => movePhoto(index, 1)}
                        className="px-2 py-1 text-sm border rounded disabled:opacity-40"
                      >
                        ↓
                      </button>
                      <button
                        type="button"
                        aria-label={`${photo.label}を外す`}
                        onClick={() => removePhoto(photo.photoNo)}
                        className="px-2 py-1 text-sm border border-red-400 text-red-700 rounded"
                      >
                        ×
                      </button>
                    </div>
                  </li>
                );
              })}
            </ol>
          )}
          {photos.length > 0 && photoActions}
        </section>

        {/*
          ライブリージョンはメッセージと同時にDOMへ挿入すると読み上げられないため常設し、中身だけを差し替える
        */}
        <p role="alert" className="text-red-700 text-sm empty:sr-only">
          {submitError}
        </p>
        <p role="status" className="text-green-700 text-sm empty:sr-only">
          {successMessage}
        </p>

        {/* 保存・公開状態の切り替え（中央揃え） */}
        <div className="flex flex-col items-center gap-3">
          <div className="flex flex-wrap justify-center gap-3">
            <button
              type="button"
              onClick={handleSave}
              disabled={isSubmitting}
              className="px-6 py-2 bg-blue-700 text-white rounded hover:bg-blue-800 disabled:opacity-50"
            >
              {portfolioNo === undefined ? "下書きとして登録" : "保存する"}
            </button>
            {portfolioNo !== undefined &&
              (statusKbn === "draft" ? (
                <button
                  type="button"
                  onClick={() => handleChangeStatus("published")}
                  disabled={isSubmitting || isDirty}
                  className="px-6 py-2 bg-green-700 text-white rounded hover:bg-green-800 disabled:opacity-50"
                >
                  公開する
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => handleChangeStatus("draft")}
                  disabled={isSubmitting || isDirty}
                  className="px-6 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:opacity-50"
                >
                  下書きに戻す
                </button>
              ))}
          </div>
          {portfolioNo !== undefined && isDirty && (
            <p className="text-sm text-gray-600 text-center">
              公開状態を切り替えるには、先に変更を保存してください
            </p>
          )}
          {/* 公開中だけ、「保存する」「下書きに戻す」の下に公開ページへのリンクを表示する */}
          {portfolioNo !== undefined && statusKbn === "published" && (
            <Link
              href={`/portfolio/${accountId}/${portfolioNo}`}
              target="_blank"
              rel="noopener"
              className="text-blue-700 hover:underline"
            >
              公開ページを見る
            </Link>
          )}
        </div>
      </div>

      {showPicker && (
        <PhotoPickerDialog
          accountId={accountId}
          initialSelected={photos.map(({ photoNo, imageFilePath, label, directionKbn }) => ({
            photoNo,
            imageFilePath,
            label,
            directionKbn,
          }))}
          onComplete={handleCompletePick}
          onCancel={() => setShowPicker(false)}
        />
      )}

      {showPreview && (
        <ModalDialog
          onClose={() => setShowPreview(false)}
          labelledBy="grid-preview-heading"
          initialFocusSelector="[data-dialog-initial-focus]"
          testId="grid-preview-dialog"
          overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
          containerClassName="bg-white rounded shadow-lg w-full max-w-[900px] max-h-[90vh] flex flex-col"
        >
          <div className="flex justify-between items-center px-6 py-4 border-b border-gray-200">
            <h2 id="grid-preview-heading" className="font-bold">
              プレビュー
            </h2>
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => setShowPreview(false)}
              className="px-4 py-2 text-sm border rounded"
            >
              閉じる
            </button>
          </div>
          {/* 写真・行が多いと縦に長くなるため、ダイアログの中だけをスクロールさせる。
              中にフォーカスできる要素がないため、キーボードでもスクロールできるようにする */}
          <div className="overflow-y-auto px-6 py-4" tabIndex={0} aria-label="プレビューの内容">

            {templateKbn === "grid" ? (
              <GridPreview photos={photos} rows={gridRows} />
            ) : (
              <FeaturePreview photos={photos} />
            )}
          </div>
        </ModalDialog>
      )}

      {showDeleteConfirm && (
        <ModalDialog
          onClose={isSubmitting ? undefined : () => setShowDeleteConfirm(false)}
          label="ポートフォリオの削除確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
          containerClassName="bg-white rounded shadow-lg p-6 w-full max-w-[400px]"
        >
          <p className="mb-2">ポートフォリオを削除してもよろしいですか？</p>
          <p className="mb-4 text-sm text-red-700">削除したポートフォリオは復元できません。</p>
          <p role="alert" className="text-red-700 text-sm mb-2 empty:sr-only">
            {deleteError}
          </p>
          <div className="flex justify-end gap-3">
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => setShowDeleteConfirm(false)}
              disabled={isSubmitting}
              className="px-4 py-2 border rounded disabled:opacity-50"
            >
              キャンセル
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={isSubmitting}
              className="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800 disabled:opacity-50"
            >
              {isSubmitting ? "削除中..." : "削除する"}
            </button>
          </div>
        </ModalDialog>
      )}
    </div>
  );
}
