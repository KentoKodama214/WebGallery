import type { Metadata } from "next";
import { AdminInquiryManagement } from "./AdminInquiryManagement";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

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
      <AdminInquiryManagement />
      <Footer />
    </>
  );
}
