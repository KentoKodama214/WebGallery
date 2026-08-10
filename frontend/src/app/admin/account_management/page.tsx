import type { Metadata } from "next";
import { AdminAccountManagement } from "./AdminAccountManagement";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

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
      <AdminAccountManagement />
      <Footer />
    </>
  );
}
