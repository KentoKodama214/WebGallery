import type { NextConfig } from "next";

const isDev = process.env.NODE_ENV === "development";

/**
 * 写真の配信元オリジン（`NEXT_PUBLIC_IMAGE_BASE_URL` 例: `https://cdn.example.com/`）。
 * 設定されていれば CSP の `img-src` をそのオリジンに限定し、
 * XSS 時に任意の外部ホストへ画像リクエストでデータを持ち出す経路を塞ぐ。
 * 未設定の場合は後方互換のため従来どおり `https:` 全体を許可する。
 */
const imageBaseOrigin = (() => {
  const base = process.env.NEXT_PUBLIC_IMAGE_BASE_URL;
  if (!base) return null;
  try {
    return new URL(base).origin;
  } catch {
    return null;
  }
})();

/**
 * バックエンド API のオリジン（`NEXT_PUBLIC_API_BASE_URL` 例: `https://api.example.com`）。
 * 別オリジンを指定した場合、`connect-src 'self'` のままだとブラウザの CSP で
 * すべての API 通信がブロックされるため、そのオリジンを `connect-src` に追加する。
 * 未設定（＝同一オリジンの `/api` プロキシ経由）の場合は追加不要。
 */
const apiBaseOrigin = (() => {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL;
  if (!base) return null;
  try {
    const origin = new URL(base).origin;
    return origin === "null" ? null : origin;
  } catch {
    return null;
  }
})();

/**
 * Content-Security-Policy
 *
 * - `script-src` に `'unsafe-inline'` を含むのは Next.js のハイドレーション用
 *   インラインスクリプトのため（nonce 運用に移行する場合はここを見直す）
 * - 開発時は React Fast Refresh（eval）と HMR（WebSocket）を許可する
 * - 写真は外部ホスト（CDN 等）から配信されうるため `img-src` に配信元を許可する。
 *   `NEXT_PUBLIC_IMAGE_BASE_URL` 設定時はそのオリジンに限定、未設定時は `https:` 全体
 * - Web フォントは `next/font`（ビルド時セルフホスト）で配信するため外部ホスト許可は不要
 * - `NEXT_PUBLIC_API_BASE_URL` で別オリジンの API を指す場合は `connect-src` に追加する
 */
const contentSecurityPolicy = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${isDev ? " 'unsafe-eval'" : ""}`,
  "style-src 'self' 'unsafe-inline'",
  `img-src 'self' data: blob: ${imageBaseOrigin ?? "https:"}`,
  "font-src 'self' data:",
  `connect-src 'self'${apiBaseOrigin ? ` ${apiBaseOrigin}` : ""}${isDev ? " ws:" : ""}`,
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
