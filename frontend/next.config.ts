import type { NextConfig } from "next";

const isDev = process.env.NODE_ENV === "development";

/**
 * Content-Security-Policy
 *
 * - `script-src` に `'unsafe-inline'` を含むのは Next.js のハイドレーション用
 *   インラインスクリプトのため（nonce 運用に移行する場合はここを見直す）
 * - 開発時は React Fast Refresh（eval）と HMR（WebSocket）を許可する
 * - 写真は外部ホスト（CDN 等）から配信されうるため `img-src` に `https:` を許可
 */
const contentSecurityPolicy = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${isDev ? " 'unsafe-eval'" : ""}`,
  // Google Fonts のスタイルシート（Header.module.css の @import）を許可
  "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
  "img-src 'self' data: blob: https:",
  "font-src 'self' data: https://fonts.gstatic.com",
  `connect-src 'self'${isDev ? " ws:" : ""}`,
  "worker-src 'self' blob:",
  "object-src 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "frame-ancestors 'none'",
]
  .join("; ")
  .concat(";");

/**
 * 全ルート共通で付与するセキュリティレスポンスヘッダー
 * （Next.js が返す HTML にはバックエンドの Spring Security 設定が効かないため）
 */
const securityHeaders = [
  { key: "Content-Security-Policy", value: contentSecurityPolicy },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  {
    key: "Permissions-Policy",
    value: "camera=(), microphone=(), geolocation=()",
  },
  // HSTS は HTTPS 配信される本番のみ付与する
  ...(isDev
    ? []
    : [
        {
          key: "Strict-Transport-Security",
          value: "max-age=63072000; includeSubDomains",
        },
      ]),
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
