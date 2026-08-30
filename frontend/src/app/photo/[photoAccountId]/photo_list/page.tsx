import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { isValidAccountId } from "@/lib/validation";
import { PhotoList } from "./PhotoList";
import "./photo-list-page.css";

export const metadata: Metadata = {
  title: "写真一覧 - WebGallery",
};

/**
 * 写真一覧ページ
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
      {/* photoAccountId はURLの動的セグメントで細工可能なため、
          Cookie名・APIパスに使う前にアカウントID形式を検証する */}
      {isValidAccountId(photoAccountId) ? (
        <PhotoList photoAccountId={photoAccountId} />
      ) : (
        <div
          style={{
            minHeight: "100vh",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            color: "#9ca3af",
          }}
        >
          <p>ギャラリーが見つかりません</p>
        </div>
      )}
      <Footer />
    </div>
  );
}
