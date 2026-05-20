import type { Metadata } from "next";
import { LoginForm } from "./LoginForm";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "ログイン - WebGallery",
};

/**
 * ログインページ
 */
export default function LoginPage() {
  return (
    <>
      <LoginForm />
      <Footer />
    </>
  );
}
