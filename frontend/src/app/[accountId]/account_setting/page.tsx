import type { Metadata } from "next";
import { AccountSettingForm } from "./AccountSettingForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

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
      <AccountSettingForm accountId={accountId} />
      <Footer />
    </>
  );
}
