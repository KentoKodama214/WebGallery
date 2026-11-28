import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 写真一覧の 404 表示（`photoAccountId` がアカウントID形式でない場合）
 */
export default function PhotoListNotFound() {
  return (
    <div style={{ backgroundColor: "black", minHeight: "100vh" }}>
      <Header />
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-2 px-4">
        <h1 className="text-lg font-bold text-[#9ca3af]">ギャラリーが見つかりません</h1>
        <p className="text-sm text-[#9ca3af]">URLが正しいかご確認ください。</p>
      </div>
      <Footer />
    </div>
  );
}
