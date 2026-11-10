import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { FrameTemplateList } from "./FrameTemplateList";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";

export const metadata: Metadata = {
  title: "額縁テンプレート - WebGallery",
};

/**
 * 額縁テンプレート（一覧）ページ
 */
export default async function FrameTemplateListPage({
  params,
}: {
  params: Promise<{ accountId: string }>;
}) {
  const { accountId } = await params;

  // accountId はURLの動的セグメントで細工可能なため、APIパス・画面表示に使う前に形式を検証する
  if (!isValidAccountId(accountId)) {
    notFound();
  }

  return (
    <div className="min-h-screen bg-[whitesmoke]">
      <Header />
      <AuthGuard>
        <FrameTemplateList accountId={accountId} />
      </AuthGuard>
      <Footer variant="light" />
    </div>
  );
}
