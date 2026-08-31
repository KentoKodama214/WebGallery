import type { NextConfig } from "next";

const isDev = process.env.NODE_ENV === "development";

/**
 * Content-Security-Policy は `src/proxy.ts` でリクエストごとに nonce 付きで生成する
 * （`script-src` から `'unsafe-inline'` を排除するため。詳細は proxy.ts のコメント参照）。
 * ここではリクエストに依存しない静的なセキュリティヘッダーのみを付与する。
 *
 * これらは Next.js が返す HTML に対して付与する（バックエンドの Spring Security 設定は
 * Next.js のレスポンスには効かないため）。
 */
const securityHeaders = [
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
  experimental: {
    // proxy（旧 middleware）使用時、Next.js はリクエストボディをメモリへ
    // バッファリングする。バックエンドのアップロード上限（サーブレット 6MB）に
    // 合わせて上限を設定し、過大なボディでメモリを消費しないようにする。
    proxyClientMaxBodySize: "6mb",
  },
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
