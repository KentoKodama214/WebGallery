import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/lib/auth/AuthProvider";

export const metadata: Metadata = {
  title: "WebGallery",
};

/**
 * nonce 方式の CSP（`src/proxy.ts`）は SSR 時にリクエストの CSP ヘッダーから
 * nonce を読み取って各 <script> へ適用するため、全ルートを動的レンダリングにする。
 * 静的生成されたページはリクエストヘッダーを持たず nonce を適用できない。
 */
export const dynamic = "force-dynamic";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ja">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
