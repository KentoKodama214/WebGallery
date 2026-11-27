"use client";

import { useState } from "react";

interface RecoveryCodeListProps {
  /** 表示するリカバリーコード（4文字ごとにハイフンで区切った表示用の形） */
  codes: string[];
  /** ダウンロードするテキストファイルに記載するアカウントID */
  accountId: string;
}

/**
 * 発行したリカバリーコードの一覧
 *
 * コードはサーバーにハッシュしか保存されず、この画面を閉じると二度と表示できないため、
 * コピー・ダウンロードの手段を添えて保管を促す
 */
export function RecoveryCodeList({ codes, accountId }: RecoveryCodeListProps) {
  const [copyStatus, setCopyStatus] = useState("");

  const text = codes.join("\n");

  /** すべてのコードをクリップボードへコピーする */
  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(text);
      setCopyStatus("コピーしました");
    } catch {
      setCopyStatus("コピーできませんでした。手動で書き写してください");
    }
  };

  /** すべてのコードをテキストファイルとしてダウンロードする */
  const handleDownload = () => {
    const content = [
      "Web Gallery リカバリーコード",
      `アカウントID: ${accountId}`,
      "",
      "パスキーをすべて失ったときに、新しいパスキーを登録するために使います。",
      "各コードは1回だけ使えます。他人に見られない場所に保管してください。",
      "",
      text,
      "",
    ].join("\n");
    const url = URL.createObjectURL(new Blob([content], { type: "text/plain" }));
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `webgallery-recovery-codes-${accountId}.txt`;
    anchor.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <ol
        aria-label="リカバリーコード"
        className="grid grid-cols-2 gap-x-3 gap-y-1 bg-[#f5f5f5] border border-[#ddd] rounded-sm p-3 mb-2 font-mono text-sm text-[#222]"
      >
        {codes.map((code) => (
          <li key={code} className="tracking-wider">
            {code}
          </li>
        ))}
      </ol>
      <div className="flex gap-2 mb-1">
        <button
          type="button"
          onClick={handleCopy}
          className="flex-1 h-[36px] text-xs border border-[#1565C0] text-[#1565C0] bg-white rounded-sm cursor-pointer hover:bg-[#E3F2FD]"
        >
          コピー
        </button>
        <button
          type="button"
          onClick={handleDownload}
          className="flex-1 h-[36px] text-xs border border-[#1565C0] text-[#1565C0] bg-white rounded-sm cursor-pointer hover:bg-[#E3F2FD]"
        >
          ダウンロード
        </button>
      </div>
      {copyStatus && (
        <p role="status" className="text-[#1565C0] text-xs mb-1">
          {copyStatus}
        </p>
      )}
    </div>
  );
}
