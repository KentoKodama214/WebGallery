import type { Metadata } from "next";
import { InquiryList } from "./InquiryList";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";

export const metadata: Metadata = {
  title: "お問い合わせ一覧 - WebGallery",
};

/**
 * 自分のお問い合わせ一覧ページ
 */
export default function InquiryListPage() {
  return (
    <div className="min-h-screen bg-[whitesmoke]">
      <Header />
      <AuthGuard>
        <InquiryList />
      </AuthGuard>
      <Footer variant="light" />
    </div>
  );
}
