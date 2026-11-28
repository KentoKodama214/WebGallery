import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PortfolioEditForm } from "./PortfolioEditForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";

export const metadata: Metadata = {
  title: "ポートフォリオ編集 - WebGallery",
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
 * ポートフォリオの作成・編集ページ
 *
 * `portfolioNo` クエリを指定した場合は既存のポートフォリオの編集、未指定の場合は新規作成となる。
 */
export default async function PortfolioEditPage({
  params,
  searchParams,
}: {
  params: Promise<{ accountId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { accountId } = await params;
  const { portfolioNo } = await searchParams;

  if (!isValidAccountId(accountId)) {
    notFound();
  }

  // 未指定は新規作成。指定されているのに正の整数でない場合は 404 として扱う
  const parsedPortfolioNo = portfolioNo === undefined ? null : parsePositiveInt(portfolioNo);
  if (portfolioNo !== undefined && parsedPortfolioNo === null) {
    notFound();
  }

  return (
    <>
      <Header />
      <AuthGuard>
        <PortfolioEditForm
          accountId={accountId}
          initialPortfolioNo={parsedPortfolioNo ?? undefined}
        />
      </AuthGuard>
      <Footer variant="light" />
    </>
  );
}
