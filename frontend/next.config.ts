import type { NextConfig } from "next";

/**
 * 全ルート共通で付与するセキュリティレスポンスヘッダー
 * （Next.js が返す HTML にはバックエンドの Spring Security 設定が効かないため）
 */
const securityHeaders = [
  { key: "X-Frame-Options", value: "DENY" },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  {
    key: "Permissions-Policy",
    value: "camera=(), microphone=(), geolocation=()",
  },
];

const nextConfig: NextConfig = {
  devIndicators: false,
  async headers() {
    return [
      {
        source: "/:path*",
        headers: securityHeaders,
      },
    ];
  },
  // バックエンドAPIへの中継は src/app/api/[...path]/route.ts で行う。
  // rewrites はダイナミックルートより先に評価され multipart アップロードを
  // 扱えないため使用しない。
};

export default nextConfig;
