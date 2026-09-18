import type { Metadata } from "next";
import { AdminInquiryDetail } from "./AdminInquiryDetail";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "お問い合わせ詳細 - WebGallery",
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
 * 管理者用お問い合わせ詳細ページ
 */
export default async function AdminInquiryDetailPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { inquiryId } = await searchParams;
  const parsedInquiryId = parsePositiveInt(inquiryId);

  return (
    <>
      <Header />
      {parsedInquiryId === null ? (
        <div className="flex justify-center items-center min-h-[60vh]">
          <p className="text-red-500">お問い合わせが見つかりません</p>
        </div>
      ) : (
        <AdminInquiryDetail inquiryId={parsedInquiryId} />
      )}
      <Footer />
    </>
  );
}
