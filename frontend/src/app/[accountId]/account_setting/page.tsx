import type { Metadata } from "next";
import { AccountSettingForm } from "./AccountSettingForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
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

  return (
    <>
      <Header />
      {/* accountId はURLの動的セグメントで細工可能なため、
          APIパス・画面表示に使う前にアカウントID形式を検証する
          （他の動的セグメントページ（photo_list 等）と揃える多層防御） */}
      {isValidAccountId(accountId) ? (
        <AccountSettingForm accountId={accountId} />
      ) : (
        <div className="min-h-screen bg-[whitesmoke] flex items-center justify-center">
          <p className="text-red-500">ページが見つかりません</p>
        </div>
      )}
      <Footer />
    </>
  );
}
