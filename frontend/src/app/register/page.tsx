import type { Metadata } from "next";
import { RegisterForm } from "./RegisterForm";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "アカウント登録 - WebGallary",
};

/**
 * アカウント登録ページ（SSR）
 */
export default function RegisterPage() {
  return (
    <>
      <Header />
      <RegisterForm />
      <Footer />
    </>
  );
}
