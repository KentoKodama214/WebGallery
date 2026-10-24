import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 公開ページの 404 表示（アカウントIDの形式が不正、またはポートフォリオが存在しない・公開されていない場合）
 */
export default function PublicPortfolioNotFound() {
  return (
    <>
      <Header />
      <div className="min-h-[60vh] flex flex-col justify-center items-center gap-2 px-4">
        <h1 className="text-lg font-bold text-red-700">ポートフォリオが見つかりません</h1>
        <p className="text-sm text-[#444]">URLが正しいかご確認ください。</p>
      </div>
      <Footer variant="light" />
    </>
  );
}
