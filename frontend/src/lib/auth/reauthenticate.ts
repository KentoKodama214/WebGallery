import { getReauthenticationOptions } from "@/lib/api/client";
import { getPasskeyCredential } from "@/lib/auth/passkey";

/**
 * 重要操作（パスキーの登録・削除、退会、リカバリーコードの再発行）の前に、パスキーで本人確認する
 *
 * サーバーが発行した本人確認用のチャレンジ（本人のパスキーに限定）に端末のパスキーで署名させ、
 * その認証レスポンスを返す。返り値は各操作のリクエストにそのまま添える（1度しか使えない）。
 *
 * @param accountId ログイン中のアカウントID
 * @returns 本人確認用の認証レスポンス（`PublicKeyCredential#toJSON()` の形式の JSON 文字列）
 * @throws {PasskeyCancelledError} 利用者がパスキーの操作をキャンセルした場合
 */
export async function reauthenticateWithPasskey(accountId: string): Promise<string> {
  const options = await getReauthenticationOptions(accountId);
  return getPasskeyCredential(options);
}
