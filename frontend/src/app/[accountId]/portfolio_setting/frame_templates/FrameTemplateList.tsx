"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import { deleteFrameTemplate, getFrameTemplateList, type FrameTemplateItem } from "@/lib/api/client";
import { ModalDialog } from "@/components/ui/ModalDialog";
import { FrameCanvas } from "@/components/frame/FrameCanvas";
import { FRAME_TEMPLATE_MAX_SIZE, parseFrameDefinition } from "@/lib/frame/definition";

interface FrameTemplateListProps {
  /** URLのアカウントID */
  accountId: string;
}

/**
 * 額縁テンプレート一覧
 *
 * 本人の額縁テンプレートを、額縁の見本（写真枠は当てはめる順の番号で表示）とあわせて一覧表示する。
 * 作成数が上限に達している場合は新規作成できない（バックエンドでも上限を検証する）。
 */
export function FrameTemplateList({ accountId }: FrameTemplateListProps) {
  const { user } = useAuth();
  const isOwner = !!user && user.accountId === accountId;

  const [templates, setTemplates] = useState<FrameTemplateItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<FrameTemplateItem | null>(null);
  const [deleteError, setDeleteError] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!isOwner) return;
    let cancelled = false;
    getFrameTemplateList(accountId)
      .then((data) => {
        if (!cancelled) setTemplates(data.frameTemplateList);
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
  }, [isOwner, accountId]);

  const handleDelete = async () => {
    if (!deleteTarget || isDeleting) return;
    setDeleteError("");
    setIsDeleting(true);
    try {
      await deleteFrameTemplate(accountId, deleteTarget.frameTemplateNo);
      setTemplates((prev) => prev.filter((t) => t.frameTemplateNo !== deleteTarget.frameTemplateNo));
      setDeleteTarget(null);
    } catch (err) {
      // 削除失敗時は確認ダイアログを開いたままエラーを通知する
      setDeleteError(err instanceof Error ? err.message : "エラーが発生しました");
    } finally {
      setIsDeleting(false);
    }
  };

  if (!isOwner) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <p className="text-red-700">この操作を行う権限がありません</p>
      </div>
    );
  }

  const isReachedUpperLimit = templates.length >= FRAME_TEMPLATE_MAX_SIZE;

  return (
    <div className="flex flex-col items-center py-8 gap-4">
      <div className="w-full max-w-[1000px] px-4 flex flex-col gap-2">
        <Link href={`/${accountId}/portfolio_setting`} className="text-blue-700 hover:underline text-sm self-start">
          &larr; ポートフォリオ設定へ戻る
        </Link>
        <div className="flex justify-between items-center">
          <h1 className="text-xl font-bold">額縁テンプレート</h1>
          {isReachedUpperLimit ? (
            <button
              type="button"
              disabled
              aria-describedby="frame-template-upper-limit-message"
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded disabled:opacity-50"
            >
              新規作成
            </button>
          ) : (
            <Link
              href={`/${accountId}/portfolio_setting/frame_templates/edit`}
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
            >
              新規作成
            </Link>
          )}
        </div>
        {isReachedUpperLimit && (
          <p id="frame-template-upper-limit-message" className="text-sm text-red-700 text-right">
            作成できる額縁テンプレートの数（{FRAME_TEMPLATE_MAX_SIZE}個）が上限に達しています
          </p>
        )}
        <p className="text-sm text-gray-700">
          写真展の額のように、1枚の額縁に写真枠とメッセージを自由に配置したテンプレートを作れます。
          ポートフォリオ設定でテンプレート「額縁」を選ぶと、ここで作ったテンプレートを使えます。
        </p>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p>読み込み中...</p>
        </div>
      ) : loadError ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p className="text-red-700">{loadError}</p>
        </div>
      ) : templates.length === 0 ? (
        <div className="flex justify-center items-center min-h-[200px]">
          <p className="text-gray-700">額縁テンプレートがありません</p>
        </div>
      ) : (
        <ul className="w-full max-w-[1000px] px-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {templates.map((template) => {
            const definition = parseFrameDefinition(template.frameDefinition);
            return (
              <li
                key={template.frameTemplateNo}
                data-testid="frame-template-item"
                className="bg-white rounded shadow p-4 flex flex-col gap-3"
              >
                <div className="px-2 pt-2 pb-1">
                  <FrameCanvas
                    size={definition.size}
                    backgroundColor={definition.background.color}
                    borderColor={definition.border.color}
                    borderWidth={definition.border.width}
                    slots={definition.slots.map((slot) => ({ ...slot, photo: null }))}
                    texts={definition.texts.map((text, index) => ({ ...text, text: `メッセージ${index + 1}` }))}
                    emptySlotLabel={(index) => `${index + 1}`}
                    label={`${template.templateName}の見本`}
                  />
                </div>
                <div className="flex flex-col gap-1">
                  <p className="font-bold break-all">{template.templateName}</p>
                  <p className="text-xs text-gray-600">
                    写真枠 {definition.slots.length}・メッセージ枠 {definition.texts.length}・
                    {definition.size.width}×{definition.size.height}mm
                  </p>
                </div>
                <div className="flex gap-2 justify-end">
                  <Link
                    href={`/${accountId}/portfolio_setting/frame_templates/edit?frameTemplateNo=${template.frameTemplateNo}`}
                    aria-label={`${template.templateName}を編集`}
                    className="px-3 py-1 text-sm border border-blue-700 text-blue-700 rounded hover:bg-blue-50"
                  >
                    編集
                  </Link>
                  <button
                    type="button"
                    aria-label={`${template.templateName}を削除`}
                    onClick={() => {
                      setDeleteError("");
                      setDeleteTarget(template);
                    }}
                    className="px-3 py-1 text-sm border border-red-500 text-red-700 rounded hover:bg-red-50"
                  >
                    削除
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}

      {deleteTarget && (
        <ModalDialog
          onClose={isDeleting ? undefined : () => setDeleteTarget(null)}
          label="額縁テンプレートの削除確認"
          initialFocusSelector="[data-dialog-initial-focus]"
          overlayClassName="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4"
          containerClassName="bg-white rounded shadow-lg p-6 w-full max-w-[400px]"
        >
          <p className="mb-2 break-all">「{deleteTarget.templateName}」を削除してもよろしいですか？</p>
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
              onClick={() => setDeleteTarget(null)}
              disabled={isDeleting}
              className="px-4 py-2 border rounded disabled:opacity-50"
            >
              キャンセル
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={isDeleting}
              className="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800 disabled:opacity-50"
            >
              {isDeleting ? "削除中..." : "削除する"}
            </button>
          </div>
        </ModalDialog>
      )}
    </div>
  );
}
