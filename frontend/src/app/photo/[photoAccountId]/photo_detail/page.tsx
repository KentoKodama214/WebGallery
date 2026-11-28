import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { isValidAccountId } from "@/lib/validation";
import { PhotoDetail } from "./PhotoDetail";

export const metadata: Metadata = {
  title: "写真詳細 - WebGallery",
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
  const { photoNo } = await searchParams;

  const parsedPhotoNo = parsePositiveInt(photoNo);

  // photoAccountId はURLの動的セグメントで細工可能なため、APIパスに使う前に形式を検証する。
  // 写真の所有者は photoAccountId（パス）で解決するため accountNo クエリは参照しない。
  // 存在しないリソースは 404 として扱う（`not-found.tsx` が描画される）
  if (!isValidAccountId(photoAccountId) || parsedPhotoNo === null) {
    notFound();
  }

  return (
    <>
      <Header />
      <PhotoDetail photoAccountId={photoAccountId} photoNo={parsedPhotoNo} />
      <Footer />
    </>
  );
}
