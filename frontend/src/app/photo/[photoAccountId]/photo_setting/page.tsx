import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PhotoSettingForm } from "./PhotoSettingForm";

export const metadata: Metadata = {
  title: "写真設定 - WebGallery",
};

/**
 * クエリパラメータを正の整数として解釈する
 *
 * @param value クエリパラメータの値
 * @returns 正の整数。解釈できない場合はundefined
 */
function parsePositiveInt(
  value: string | string[] | undefined
): number | undefined {
  if (typeof value !== "string") return undefined;
  const num = Number(value);
  return Number.isInteger(num) && num > 0 ? num : undefined;
}

/**
 * 写真設定ページ
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
        accountNo={parsePositiveInt(accountNo)}
        photoNo={parsePositiveInt(photoNo)}
      />
      <Footer />
    </>
  );
}
