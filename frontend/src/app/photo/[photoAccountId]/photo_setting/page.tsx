import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PhotoSettingForm } from "./PhotoSettingForm";

export const metadata: Metadata = {
  title: "写真設定 - WebGallary",
};

/**
 * 写真設定ページ（SSR）
 */
export default async function PhotoSettingPage({
  params,
  searchParams,
}: {
  params: Promise<{ photoAccountId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { photoAccountId } = await params;
  const { accountNo, photoNo } = await searchParams;

  return (
    <>
      <Header />
      <PhotoSettingForm
        photoAccountId={photoAccountId}
        accountNo={accountNo ? Number(accountNo) : undefined}
        photoNo={photoNo ? Number(photoNo) : undefined}
      />
      <Footer />
    </>
  );
}
