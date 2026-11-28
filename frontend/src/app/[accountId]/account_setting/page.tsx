import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { AccountSettingForm } from "./AccountSettingForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";

export const metadata: Metadata = {
  title: "アカウント設定 - WebGallery",
};

/**
 * アカウント設定ページ
 */
export default async function AccountSettingPage({
  params,
}: {
  params: Promise<{ accountId: string }>;
}) {
  const { accountId } = await params;

  // accountId はURLの動的セグメントで細工可能なため、
  // APIパス・画面表示に使う前にアカウントID形式を検証する
  // （他の動的セグメントページ（photo_list 等）と揃える多層防御）。
  // 不正なURLは 404 として扱う（ルートの `not-found.tsx` が描画される）
  if (!isValidAccountId(accountId)) {
    notFound();
  }

  return (
    <>
      <Header />
      <AuthGuard>
        <AccountSettingForm accountId={accountId} />
      </AuthGuard>
      <Footer variant="light" />
    </>
  );
}
