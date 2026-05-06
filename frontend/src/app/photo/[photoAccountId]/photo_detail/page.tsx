import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PhotoDetail } from "./PhotoDetail";

export const metadata: Metadata = {
  title: "写真詳細 - WebGallary",
};

/**
 * 写真詳細ページ（SSR）
 */
export default async function PhotoDetailPage({
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
      <PhotoDetail
        photoAccountId={photoAccountId}
        accountNo={Number(accountNo)}
        photoNo={Number(photoNo)}
      />
      <Footer />
    </>
  );
}
