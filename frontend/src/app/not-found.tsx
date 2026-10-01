import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

/**
 * 既定の 404 表示
 *
 * ページ側で `notFound()` を呼んだとき、より近い `not-found.tsx` が無ければこれが描画される。
 * 未マッチのURL（ルーティングに一致しないパス）でも使われる。
 */
export default function NotFound() {
  return (
    <>
      <Header />
      <div className="min-h-[60vh] bg-[whitesmoke] flex flex-col items-center justify-center gap-2 px-4">
        <h1 className="text-[#444] text-lg font-bold">ページが見つかりません</h1>
        <p className="text-[#444] text-sm">URLが正しいかご確認ください。</p>
      </div>
      <Footer variant="light" />
    </>
  );
}
