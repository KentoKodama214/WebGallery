import type { Metadata } from "next";
import { AdminInquiryManagement } from "./AdminInquiryManagement";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";

export const metadata: Metadata = {
  title: "お問い合わせ管理 - WebGallery",
};

/**
 * 管理者用お問い合わせ管理ページ
 */
export default function AdminInquiryManagementPage() {
  return (
    <>
      <Header />
      {/* 未ログイン時は他の保護ページと同様に /login へ誘導する（配下は管理者権限の判定に専念する） */}
      <AuthGuard>
        <AdminInquiryManagement />
      </AuthGuard>
      <Footer />
    </>
  );
}
