import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { FrameTemplateEditor } from "./FrameTemplateEditor";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";

export const metadata: Metadata = {
  title: "額縁テンプレート編集 - WebGallery",
};

/**
 * クエリパラメータを正の整数として解釈する
 *
 * 指数表記（`1e3`）や16進表記（`0x10`）・前後空白を含む値を弾くため、
 * まず10進数字のみで構成されているかを確認してから数値化する。
 *
 * @param value クエリパラメータの値
 * @returns 正の整数。解釈できない場合はnull
 */
function parsePositiveInt(value: string | string[] | undefined): number | null {
  if (typeof value !== "string" || !/^\d+$/.test(value)) return null;
  const num = Number(value);
  return Number.isInteger(num) && num > 0 ? num : null;
}

/**
 * 額縁テンプレートの作成・編集ページ
 *
 * `frameTemplateNo` クエリを指定した場合は既存の額縁テンプレートの編集、未指定の場合は新規作成となる。
 */
export default async function FrameTemplateEditPage({
  params,
  searchParams,
}: {
  params: Promise<{ accountId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { accountId } = await params;
  const { frameTemplateNo } = await searchParams;

  if (!isValidAccountId(accountId)) {
    notFound();
  }

  // 未指定は新規作成。指定されているのに正の整数でない場合は 404 として扱う
  const parsedNo = frameTemplateNo === undefined ? null : parsePositiveInt(frameTemplateNo);
  if (frameTemplateNo !== undefined && parsedNo === null) {
    notFound();
  }

  return (
    <>
      <Header />
      <AuthGuard>
        <FrameTemplateEditor accountId={accountId} initialFrameTemplateNo={parsedNo ?? undefined} />
      </AuthGuard>
      <Footer variant="light" />
    </>
  );
}
