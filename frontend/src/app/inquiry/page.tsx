import type { Metadata } from "next";
import { InquiryForm } from "./InquiryForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";

export const metadata: Metadata = {
  title: "お問い合わせ - WebGallery",
};

/**
 * お問い合わせ投稿ページ
 */
export default function InquiryPage() {
  return (
    <>
      <Header />
      <AuthGuard>
        <InquiryForm />
      </AuthGuard>
      <Footer />
    </>
  );
}
