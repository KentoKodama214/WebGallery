import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PortfolioList } from "./PortfolioList";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";

export const metadata: Metadata = {
  title: "ポートフォリオ設定 - WebGallery",
};

/**
 * ポートフォリオ設定（一覧）ページ
 */
export default async function PortfolioSettingPage({
  params,
}: {
  params: Promise<{ accountId: string }>;
}) {
  const { accountId } = await params;

  // accountId はURLの動的セグメントで細工可能なため、APIパス・画面表示に使う前に形式を検証する。
  // 不正なURLは 404 として扱う（ルートの `not-found.tsx` が描画される）
  if (!isValidAccountId(accountId)) {
    notFound();
  }

  return (
    <>
      <Header />
      <AuthGuard>
        <PortfolioList accountId={accountId} />
      </AuthGuard>
      <Footer variant="light" />
    </>
  );
}
