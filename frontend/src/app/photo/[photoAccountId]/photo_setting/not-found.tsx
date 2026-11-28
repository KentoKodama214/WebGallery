import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 写真設定の 404 表示（`photoAccountId` の形式不正・編集用クエリが片方だけ指定された場合）
 */
export default function PhotoSettingNotFound() {
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
