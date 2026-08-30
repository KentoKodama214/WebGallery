/**
 * フォーム入力の共通バリデーションユーティリティ
 */

/** アカウントIDの形式（半角英数字8〜20文字） */
export const ACCOUNT_ID_PATTERN = /^[a-zA-Z0-9]{8,20}$/;

/**
 * 値がアカウントIDの形式（半角英数字8〜20文字）を満たすか判定する
 *
 * URL の動的セグメント（`photoAccountId` / `accountId`）は利用者が自由に細工できるため、
 * Cookie 名・API パス・画面表示に用いる前にこの関数で形式を確認する。
 *
 * @param value 検証対象（通常は URL パスパラメータ）
 * @returns 形式を満たす場合 true
 */
export function isValidAccountId(value: string | null | undefined): value is string {
  return typeof value === "string" && ACCOUNT_ID_PATTERN.test(value);
}

/**
 * パスワードの形式（半角8〜72文字、英字と数字を各1文字以上含む、記号可）
 *
 * バックエンドの制約（AccountRegistRequest / AccountUpdateRequest）と一致させること。
 */
export const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)[\x21-\x7E]{8,72}$/;

/** パスワードが形式を満たさない場合のエラーメッセージ */
export const PASSWORD_ERROR_MESSAGE =
  "英字と数字を含む半角8〜72文字で入力してください";

/** パスワード入力欄のプレースホルダー */
export const PASSWORD_PLACEHOLDER = "英字と数字を含む半角8〜72文字";

/**
 * エラーメッセージのマップから指定キーを取り除いた新しいマップを返す
 *
 * @param errors 現在のエラーマップ
 * @param key 取り除くキー
 * @returns キーを除いた新しいマップ（対象キーが無い場合は元のマップ）
 */
export function clearError(
  errors: Record<string, string>,
  key: string
): Record<string, string> {
  if (!(key in errors)) return errors;
  const next = { ...errors };
  delete next[key];
  return next;
}

/**
 * `yyyy-MM-dd` 形式の日付文字列が過去日かどうかを判定する
 *
 * `<input type="date">` の値をローカルタイムの暦日として解釈し、
 * 同じくローカルの本日と比較する（タイムゾーン起因のずれを避けるため）。
 *
 * @param value `yyyy-MM-dd` 形式の日付文字列
 * @returns 過去日の場合true。空文字や不正な形式の場合はfalse
 */
export function isPastDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const [year, month, day] = value.split("-").map(Number);
  const input = new Date(year, month - 1, day);
  if (
    input.getFullYear() !== year ||
    input.getMonth() !== month - 1 ||
    input.getDate() !== day
  ) {
    return false;
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return input.getTime() < today.getTime();
}
