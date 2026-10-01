import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 写真詳細の 404 表示（`photoAccountId` の形式不正・`photoNo` が正の整数でない場合）
 */
export default function PhotoDetailNotFound() {
  return (
    <>
      <Header />
      <div className="min-h-screen bg-black text-white flex flex-col justify-center items-center gap-2 px-4">
        <h1 className="text-lg font-bold text-red-500">写真が見つかりません</h1>
        <p className="text-sm text-gray-400">URLが正しいかご確認ください。</p>
      </div>
      <Footer />
    </>
  );
}
