"use client";

import { useEffect } from "react";

/**
 * ルートレイアウトを含む最上位のエラーバウンダリ
 *
 * `app/error.tsx` では捕捉できないルートレイアウト自体のエラーに対応する。
 * 独自の `<html>` / `<body>` を持つ必要がある。
 *
 * `unstable_retry` は Next.js 16 の現行 API（詳細は `app/error.tsx` のコメント参照）。
 */
export default function GlobalError({
  error,
  unstable_retry,
}: {
  error: Error & { digest?: string };
  unstable_retry: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <html lang="ja">
      <body
        style={{
          minHeight: "100vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "16px",
          padding: "0 16px",
          background: "whitesmoke",
          fontFamily: "'Open Sans', sans-serif",
        }}
      >
        <p style={{ color: "#444", textAlign: "center" }}>
          予期しないエラーが発生しました。
          <br />
          時間をおいて再度お試しください。
        </p>
        <button
          type="button"
          onClick={() => unstable_retry()}
          style={{
            padding: "8px 16px",
            background: "#2196F3",
            color: "#fff",
            border: "none",
            borderRadius: "2px",
            cursor: "pointer",
          }}
        >
          再試行
        </button>
      </body>
    </html>
  );
}
