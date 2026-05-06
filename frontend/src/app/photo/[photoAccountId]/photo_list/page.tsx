import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PhotoList } from "./PhotoList";
import "./photo-list-page.css";

export const metadata: Metadata = {
  title: "写真一覧 - WebGallary",
};

/**
 * 写真一覧ページ（SSR）
 */
export default async function PhotoListPage({
  params,
}: {
  params: Promise<{ photoAccountId: string }>;
}) {
  const { photoAccountId } = await params;

  return (
    <div style={{ backgroundColor: "black", minHeight: "100vh" }}>
      <Header />
      <PhotoList photoAccountId={photoAccountId} />
      <Footer />
    </div>
  );
}
