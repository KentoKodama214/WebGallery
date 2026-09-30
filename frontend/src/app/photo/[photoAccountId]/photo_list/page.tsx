import type { Metadata } from "next";
import { notFound } from "next/navigation";
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

  // photoAccountId はURLの動的セグメントで細工可能なため、
  // Cookie名・APIパスに使う前にアカウントID形式を検証する。
  // 存在しないリソースは 404 として扱う（`not-found.tsx` が描画される）
  if (!isValidAccountId(photoAccountId)) {
    notFound();
  }

  return (
    <div style={{ backgroundColor: "black", minHeight: "100vh" }}>
      <Header />
      <PhotoList photoAccountId={photoAccountId} />
      <Footer />
    </div>
  );
}
