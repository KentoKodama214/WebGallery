import type { Metadata } from "next";
import { AccountList } from "./AccountList";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "アカウント一覧 - WebGallery",
};

/**
 * アカウント一覧ページ
 */
export default function AccountListPage() {
  return (
    // アカウント設定ページと同じ背景色にする（body の背景色に依存させない）
    <div className="min-h-screen bg-[whitesmoke]">
      <Header />
      <AccountList />
      <Footer variant="light" />
    </div>
  );
}
