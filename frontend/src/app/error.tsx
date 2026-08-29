"use client";

import { useEffect } from "react";

/**
 * ルートセグメントのエラーバウンダリ
 *
 * 配下のクライアントコンポーネントで捕捉されない例外が発生した場合に、
 * 画面が真っ白になるのを防ぎ、再試行の導線を表示する。
 *
 * `unstable_retry` は Next.js 16 の error.js が提供する現行の復旧 API
 * （`node_modules/next/dist/docs/01-app/03-api-reference/03-file-conventions/error.md` 参照）。
 * 将来のバージョンで名称が変わる可能性があるためアップデート時は確認すること。
 */
export default function Error({
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
    <div className="min-h-screen bg-[whitesmoke] flex flex-col items-center justify-center gap-4 px-4">
      <p className="text-[#444] text-center">
        予期しないエラーが発生しました。
        <br />
        時間をおいて再度お試しください。
      </p>
      <button
        type="button"
        onClick={() => unstable_retry()}
        className="px-4 py-2 bg-[#2196F3] text-white rounded-sm cursor-pointer"
      >
        再試行
      </button>
    </div>
  );
}
