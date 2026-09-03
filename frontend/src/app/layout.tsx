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
 *
 * これは nonce 方式 CSP に内在する制約であり（Next.js 公式ドキュメント
 * "Static vs Dynamic Rendering with CSP" 参照）、回避策ではない。静的生成・ISR・CDN
 * キャッシュを併用する場合は nonce を諦めてハッシュ方式（experimental な `sri`）へ
 * 移行する必要がある。トレードオフの詳細は `doc/architecture/security.md` を参照。
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
