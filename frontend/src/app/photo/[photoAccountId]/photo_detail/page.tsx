import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PhotoDetail } from "./PhotoDetail";

export const metadata: Metadata = {
  title: "写真詳細 - WebGallery",
};

/**
 * クエリパラメータを正の整数として解釈する
 *
 * @param value クエリパラメータの値
 * @returns 正の整数。解釈できない場合はnull
 */
function parsePositiveInt(value: string | string[] | undefined): number | null {
  if (typeof value !== "string") return null;
  const num = Number(value);
  return Number.isInteger(num) && num > 0 ? num : null;
}

/**
 * 写真詳細ページ
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

  const parsedAccountNo = parsePositiveInt(accountNo);
  const parsedPhotoNo = parsePositiveInt(photoNo);

  return (
    <>
      <Header />
      {parsedAccountNo === null || parsedPhotoNo === null ? (
        <div className="min-h-screen bg-black text-white flex justify-center items-center">
          <p className="text-red-500">写真が見つかりません</p>
        </div>
      ) : (
        <PhotoDetail
          photoAccountId={photoAccountId}
          accountNo={parsedAccountNo}
          photoNo={parsedPhotoNo}
        />
      )}
      <Footer />
    </>
  );
}
