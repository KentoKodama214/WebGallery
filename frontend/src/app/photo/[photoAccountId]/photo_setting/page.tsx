import type { Metadata } from "next";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { AuthGuard } from "@/lib/auth/AuthGuard";
import { isValidAccountId } from "@/lib/validation";
import { PhotoSettingForm } from "./PhotoSettingForm";

export const metadata: Metadata = {
  title: "写真設定 - WebGallery",
};

/**
 * クエリパラメータを正の整数として解釈する
 *
 * 指数表記（`1e3`）や16進表記（`0x10`）・前後空白を含む値を弾くため、
 * まず10進数字のみで構成されているかを確認してから数値化する。
 *
 * @param value クエリパラメータの値
 * @returns 正の整数。解釈できない場合はundefined
 */
function parsePositiveInt(
  value: string | string[] | undefined
): number | undefined {
  if (typeof value !== "string" || !/^\d+$/.test(value)) return undefined;
  const num = Number(value);
  return Number.isInteger(num) && num > 0 ? num : undefined;
}

/**
 * 写真設定ページ
 */
export default async function PhotoSettingPage({
  params,
  searchParams,
}: {
  params: Promise<{ photoAccountId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { photoAccountId } = await params;
  const { accountNo, photoNo } = await searchParams;

  const parsedAccountNo = parsePositiveInt(accountNo);
  const parsedPhotoNo = parsePositiveInt(photoNo);

  // 編集は accountNo / photoNo の両方が必要。片方だけ指定された URL は不正扱いにする
  // （新規登録は両方とも無し）
  const isPartialEditParams =
    (parsedAccountNo === undefined) !== (parsedPhotoNo === undefined);

  // photoAccountId はURLの動的セグメントで細工可能なため、APIパスに使う前に形式を検証する
  const isInvalidParams = !isValidAccountId(photoAccountId) || isPartialEditParams;

  return (
    <>
      <Header />
      {isInvalidParams ? (
        <div className="min-h-screen bg-black text-white flex justify-center items-center">
          <p className="text-red-500">写真が見つかりません</p>
        </div>
      ) : (
        <AuthGuard>
          <PhotoSettingForm
            photoAccountId={photoAccountId}
            accountNo={parsedAccountNo}
            photoNo={parsedPhotoNo}
          />
        </AuthGuard>
      )}
      <Footer />
    </>
  );
}
