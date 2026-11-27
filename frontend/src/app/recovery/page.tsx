import type { Metadata } from "next";
import { RecoveryForm } from "./RecoveryForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "リカバリーコードで復旧 - WebGallery",
};

/**
 * リカバリーコードによる復旧ページ
 *
 * パスキーをすべて失った利用者が、リカバリーコードで新しいパスキーを登録してログインする
 */
export default function RecoveryPage() {
  return (
    <>
      <Header />
      <RecoveryForm />
      <Footer variant="light" />
    </>
  );
}
