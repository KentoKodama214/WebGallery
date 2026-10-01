import type { Metadata } from "next";
import { AdminAccountManagement } from "./AdminAccountManagement";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";

export const metadata: Metadata = {
  title: "アカウント管理 - WebGallery",
};

/**
 * 管理者用アカウント管理ページ
 */
export default function AdminAccountManagementPage() {
  return (
    <>
      <Header />
      {/* 未ログイン時は他の保護ページと同様に /login へ誘導する（配下は管理者権限の判定に専念する） */}
      <AuthGuard>
        <AdminAccountManagement />
      </AuthGuard>
      <Footer />
    </>
  );
}
