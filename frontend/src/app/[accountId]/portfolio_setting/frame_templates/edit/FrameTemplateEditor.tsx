"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import { deleteFrameTemplate, getFrameTemplate, saveFrameTemplate } from "@/lib/api/client";
import { ModalDialog } from "@/components/ui/ModalDialog";
import {
  FRAME_SLOT_MAX_SIZE,
  FRAME_TEMPLATE_NAME_MAX_LENGTH,
  FRAME_TEXT_MAX_SIZE,
  createDefaultDefinition,
  createSlot,
  createText,
  parseFrameDefinition,
  toFrameDefinitionJson,
  type FrameDefinition,
  type FrameSlot,
  type FrameText,
} from "@/lib/frame/definition";
import { FrameEditorCanvas, type FrameBoxChange, type FrameSelection } from "./FrameEditorCanvas";
import { FramePropertyPanel } from "./FramePropertyPanel";

interface FrameTemplateEditorProps {
  /** URLのアカウントID */
  accountId: string;
  /** 編集対象の額縁テンプレート番号（新規作成の場合は未指定） */
  initialFrameTemplateNo?: number;
}

/** 複製した写真枠・メッセージ枠を元の位置からずらす量（%） */
const DUPLICATE_OFFSET = 3;

/**
 * 額縁テンプレートの作成・編集画面
 *
 * 写真展の額のように、決まったサイズの1枚の額縁に、写真枠（大きさ・傾き・枠線・影）とメッセージ枠（位置・フォント）を
 * 自由に配置する。作成した額縁テンプレートはポートフォリオ設定の「額縁」テンプレートで選択でき、選択時に額縁定義が
 * ポートフォリオへコピーされる（後からこの画面で変更・削除しても、選択済みのポートフォリオには反映されない）。
 */
export function FrameTemplateEditor({ accountId, initialFrameTemplateNo }: FrameTemplateEditorProps) {
  const router = useRouter();
  const { user } = useAuth();
  const isOwner = !!user && user.accountId === accountId;

  const [frameTemplateNo, setFrameTemplateNo] = useState<number | undefined>(initialFrameTemplateNo);
  const [templateName, setTemplateName] = useState("");
  const [definition, setDefinition] = useState<FrameDefinition>(createDefaultDefinition);
  const [selection, setSelection] = useState<FrameSelection | null>(null);
  const [sampleTexts, setSampleTexts] = useState<string[]>([]);

  const [isLoading, setIsLoading] = useState(initialFrameTemplateNo !== undefined);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  // このフォームで新規登録した額縁テンプレート番号。登録後の編集用URLへの切り替えでは読み込み直さない
  const registeredNoRef = useRef<number | undefined>(undefined);

  // 編集時は登録済みの内容を読み込む
  useEffect(() => {
    if (!isOwner || initialFrameTemplateNo === undefined) return;
    if (initialFrameTemplateNo === registeredNoRef.current) return;
    let cancelled = false;
    getFrameTemplate(accountId, initialFrameTemplateNo)
      .then((template) => {
        if (cancelled) return;
        setFrameTemplateNo(template.frameTemplateNo);
        setTemplateName(template.templateName);
        setDefinition(parseFrameDefinition(template.frameDefinition));
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err instanceof Error ? err.message : "エラーが発生しました");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [isOwner, accountId, initialFrameTemplateNo]);

  const updateSlot = (index: number, change: Partial<FrameSlot>) => {
    setDefinition((prev) => ({
      ...prev,
      slots: prev.slots.map((slot, i) => (i === index ? { ...slot, ...change } : slot)),
    }));
  };

  const updateText = (index: number, change: Partial<FrameText>) => {
    setDefinition((prev) => ({
      ...prev,
      texts: prev.texts.map((text, i) => (i === index ? { ...text, ...change } : text)),
    }));
  };

  const changeBox = (target: FrameSelection, change: FrameBoxChange) => {
    if (target.kind === "slot") {
      updateSlot(target.index, change);
    } else {
      // メッセージ枠は高さを持たない（文字サイズと文面で決まる）ため、キャンバスは高さを変更しない
      updateText(target.index, change);
    }
  };

  const addSlot = () => {
    if (definition.slots.length >= FRAME_SLOT_MAX_SIZE) return;
    const index = definition.slots.length;
    setDefinition((prev) => ({ ...prev, slots: [...prev.slots, createSlot(index)] }));
    setSelection({ kind: "slot", index });
    setErrors((prev) => {
      const next = { ...prev };
      delete next.slots;
      return next;
    });
  };

  const addText = () => {
    if (definition.texts.length >= FRAME_TEXT_MAX_SIZE) return;
    const index = definition.texts.length;
    setDefinition((prev) => ({ ...prev, texts: [...prev.texts, createText(index)] }));
    setSelection({ kind: "text", index });
  };

  const duplicate = (target: FrameSelection) => {
    if (target.kind === "slot") {
      if (definition.slots.length >= FRAME_SLOT_MAX_SIZE) return;
      const source = definition.slots[target.index];
      setDefinition((prev) => ({
        ...prev,
        slots: [
          ...prev.slots,
          { ...source, x: Math.min(source.x + DUPLICATE_OFFSET, 100), y: Math.min(source.y + DUPLICATE_OFFSET, 100) },
        ],
      }));
      setSelection({ kind: "slot", index: definition.slots.length });
    } else {
      if (definition.texts.length >= FRAME_TEXT_MAX_SIZE) return;
      const source = definition.texts[target.index];
      setDefinition((prev) => ({
        ...prev,
        texts: [
          ...prev.texts,
          { ...source, x: Math.min(source.x + DUPLICATE_OFFSET, 100), y: Math.min(source.y + DUPLICATE_OFFSET, 100) },
        ],
      }));
      setSampleTexts((prev) => {
        const next = [...prev];
        next[definition.texts.length] = prev[target.index] ?? "";
        return next;
      });
      setSelection({ kind: "text", index: definition.texts.length });
    }
  };

  const remove = (target: FrameSelection) => {
    if (target.kind === "slot") {
      setDefinition((prev) => ({ ...prev, slots: prev.slots.filter((_, i) => i !== target.index) }));
    } else {
      setDefinition((prev) => ({ ...prev, texts: prev.texts.filter((_, i) => i !== target.index) }));
      setSampleTexts((prev) => prev.filter((_, i) => i !== target.index));
    }
    setSelection(null);
  };

  const validate = (): boolean => {
    const nextErrors: Record<string, string> = {};
    if (!templateName.trim()) {
      nextErrors.templateName = "テンプレート名を入力してください";
    }
    if (definition.slots.length === 0) {
      nextErrors.slots = "写真枠を1つ以上配置してください";
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSave = async () => {
    if (isSubmitting) return;
    setSubmitError("");
    setSuccessMessage("");
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const result = await saveFrameTemplate(
        accountId,
        { templateName: templateName.trim(), frameDefinition: toFrameDefinitionJson(definition) },
        frameTemplateNo
      );
      setSuccessMessage(result.message);
      if (frameTemplateNo === undefined) {
        // 新規登録後は、以降の保存が同じ額縁テンプレートに対して行われるよう編集用URLへ切り替える
        registeredNoRef.current = result.frameTemplateNo;
        setFrameTemplateNo(result.frameTemplateNo);
        router.replace(
          `/${accountId}/portfolio_setting/frame_templates/edit?frameTemplateNo=${result.frameTemplateNo}`
        );
      }
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (isSubmitting || frameTemplateNo === undefined) return;
    setDeleteError("");
    setIsSubmitting(true);
    try {
      await deleteFrameTemplate(accountId, frameTemplateNo);
      router.push(`/${accountId}/portfolio_setting/frame_templates`);
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : "エラーが発生しました");
      setIsSubmitting(false);
    }
  };

  // ログイン済みだが他人の額縁テンプレートを開こうとした場合（認可はバックエンドでも実施）
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
        <Link href={`/${accountId}/portfolio_setting/frame_templates`} className="text-blue-700 hover:underline">
          額縁テンプレート一覧へ戻る
        </Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center py-8 px-4">
      <div className="w-full max-w-[1200px] flex flex-col gap-6">
        <Link
          href={`/${accountId}/portfolio_setting/frame_templates`}
          className="text-blue-700 hover:underline text-sm self-start"
        >
          &larr; 額縁テンプレート一覧へ戻る
        </Link>
        <div className="flex flex-wrap justify-between items-center gap-2">
          <h1 className="text-xl font-bold">
            {frameTemplateNo === undefined ? "額縁テンプレート作成" : "額縁テンプレート編集"}
          </h1>
          {frameTemplateNo !== undefined && (
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

        <div>
          <label htmlFor="frame-template-name" className="block font-bold mb-1">
            テンプレート名<span className="text-red-700">*</span>
          </label>
          <input
            id="frame-template-name"
            type="text"
            value={templateName}
            maxLength={FRAME_TEMPLATE_NAME_MAX_LENGTH}
            aria-invalid={!!errors.templateName}
            aria-describedby={errors.templateName ? "frame-template-name-error" : undefined}
            onChange={(e) => setTemplateName(e.target.value)}
            className="w-full max-w-[480px] border border-gray-300 rounded px-3 py-2"
          />
          {errors.templateName && (
            <p id="frame-template-name-error" className="text-red-700 text-sm mt-1">
              {errors.templateName}
            </p>
          )}
        </div>

        <div className="flex flex-col lg:flex-row gap-6 items-start">
          <div className="w-full lg:flex-1 flex flex-col gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={addSlot}
                disabled={definition.slots.length >= FRAME_SLOT_MAX_SIZE}
                className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800 disabled:opacity-50"
              >
                ＋写真枠
              </button>
              <button
                type="button"
                onClick={addText}
                disabled={definition.texts.length >= FRAME_TEXT_MAX_SIZE}
                className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800 disabled:opacity-50"
              >
                ＋メッセージ枠
              </button>
              <span className="text-sm text-gray-700" data-testid="frame-element-count">
                写真枠 {definition.slots.length}/{FRAME_SLOT_MAX_SIZE}・メッセージ枠 {definition.texts.length}/
                {FRAME_TEXT_MAX_SIZE}
              </span>
            </div>
            {errors.slots && <p className="text-red-700 text-sm">{errors.slots}</p>}
            {/* ハンドルが額縁の上端・外枠の外へはみ出しても操作できるよう、余白を取る */}
            <div className="bg-gray-100 rounded p-8">
              <FrameEditorCanvas
                definition={definition}
                sampleTexts={sampleTexts}
                selection={selection}
                onSelect={setSelection}
                onChangeBox={changeBox}
                onDelete={remove}
              />
            </div>
            <p className="text-xs text-gray-600">
              写真枠の番号は、ポートフォリオで写真を当てはめる順番です。額縁テンプレートはポートフォリオで選択したときにコピーされるため、
              ここで変更・削除しても選択済みのポートフォリオには反映されません。
            </p>
          </div>
          <aside className="w-full lg:w-80 bg-white border border-gray-200 rounded p-4">
            <FramePropertyPanel
              definition={definition}
              selection={selection}
              sampleTexts={sampleTexts}
              onChangeDefinition={setDefinition}
              onChangeSlot={updateSlot}
              onChangeText={updateText}
              onChangeSampleText={(index, value) =>
                setSampleTexts((prev) => {
                  const next = [...prev];
                  next[index] = value;
                  return next;
                })
              }
              onDuplicate={duplicate}
              onDelete={remove}
            />
            {selection && (
              <button
                type="button"
                onClick={() => setSelection(null)}
                className="mt-4 text-sm text-blue-700 hover:underline"
              >
                額縁全体の設定に戻る
              </button>
            )}
          </aside>
        </div>

        <p role="alert" className="text-red-700 text-sm empty:sr-only">
          {submitError}
        </p>
        <div className="flex justify-center">
          <button
            type="button"
            onClick={handleSave}
            disabled={isSubmitting}
            className="px-6 py-2 bg-blue-700 text-white rounded hover:bg-blue-800 disabled:opacity-50"
          >
            {frameTemplateNo === undefined ? "登録する" : "保存する"}
          </button>
        </div>
      </div>

      {successMessage && (
        <ModalDialog
          testId="frame-template-result-dialog"
          label="額縁テンプレートの保存結果"
          initialFocusSelector="[data-dialog-initial-focus]"
          onClose={() => setSuccessMessage("")}
          overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
          containerClassName="bg-white rounded shadow-lg p-6 w-full max-w-[400px]"
        >
          <p className="mb-4 text-center">{successMessage}</p>
          <div className="flex justify-center">
            <button
              type="button"
              data-dialog-initial-focus
              onClick={() => setSuccessMessage("")}
              className="px-6 py-2 bg-blue-700 text-white rounded hover:bg-blue-800"
            >
              閉じる
            </button>
          </div>
        </ModalDialog>
      )}

      {showDeleteConfirm && (
        <ModalDialog
          onClose={isSubmitting ? undefined : () => setShowDeleteConfirm(false)}
          label="額縁テンプレートの削除確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
          containerClassName="bg-white rounded shadow-lg p-6 w-full max-w-[400px]"
        >
          <p className="mb-2">額縁テンプレートを削除してもよろしいですか？</p>
          <p className="mb-4 text-sm text-gray-700">
            このテンプレートを選択済みのポートフォリオは、選択時にコピーした額縁で表示され続けます。
          </p>
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
