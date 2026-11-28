import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 管理者用お問い合わせ詳細の 404 表示（`inquiryId` が正の整数でない場合）
 */
export default function AdminInquiryDetailNotFound() {
  return (
    <>
      <Header />
      <div className="min-h-[60vh] flex flex-col justify-center items-center gap-2 px-4">
        <h1 className="text-lg font-bold text-red-500">お問い合わせが見つかりません</h1>
        <p className="text-sm text-[#444]">URLが正しいかご確認ください。</p>
      </div>
      <Footer />
    </>
  );
}
