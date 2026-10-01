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

/** お問い合わせ件名の最大文字数（バックエンドの Consts.INQUIRY_SUBJECT_MAX_LENGTH と一致させること） */
export const INQUIRY_SUBJECT_MAX_LENGTH = 100;

/** お問い合わせ本文・返信本文の最大文字数（バックエンドの Consts.INQUIRY_BODY_MAX_LENGTH と一致させること） */
export const INQUIRY_BODY_MAX_LENGTH = 2000;

/**
 * アカウント名の最大文字数（バックエンドの `AccountRegistRequest` / `AccountUpdateRequest` の
 * `accountName` に付与された `@Size(max = 50)` と一致させること）
 */
export const ACCOUNT_NAME_MAX_LENGTH = 50;

/**
 * メモ（自由記述）の最大文字数（バックエンドの `freeMemo` の `@Size(max = 1000)` と一致させること）
 */
export const FREE_MEMO_MAX_LENGTH = 1000;

/**
 * 写真タイトル（日本語・英語）の最大文字数
 *
 * バックエンドの `PhotoSaveRequest` / `PhotoBulkSaveRequest` の
 * `photoJapaneseTitle` / `photoEnglishTitle` の `@Size(max = 100)` と一致させること。
 */
export const PHOTO_TITLE_MAX_LENGTH = 100;

/**
 * 写真キャプションの最大文字数（バックエンドの `caption` の `@Size(max = 1000)` と一致させること）
 */
export const PHOTO_CAPTION_MAX_LENGTH = 1000;

/**
 * ロケーション名（管理名・表示名）の最大文字数
 *
 * バックエンドの `managementName` / `displayName` の `@Size(max = 100)` と一致させること。
 */
export const LOCATION_NAME_MAX_LENGTH = 100;

/**
 * ロケーション住所の最大文字数（バックエンドの `address` の `@Size(max = 255)` と一致させること）
 */
export const LOCATION_ADDRESS_MAX_LENGTH = 255;

/**
 * タグ名（日本語・英語）の最大文字数
 *
 * バックエンドの `Consts.TAG_NAME_MAX_LENGTH`（`PhotoTagSaveRequest` の `@Size`）と一致させること。
 */
export const TAG_NAME_MAX_LENGTH = 20;

/**
 * 1枚の写真に登録できるタグの最大件数
 *
 * バックエンドの `Consts.PHOTO_TAG_MAX_SIZE`（`photoTagRegistRequestList` の `@Size`）と
 * 一致させること。
 */
export const PHOTO_TAG_MAX_SIZE = 20;

/**
 * 分析ログ用に送信する遷移元URL（`document.referrer`）の最大文字数
 *
 * バックエンドの `PhotoListRequest#referer` / `PhotoDetailRequest#referer` に付与された
 * `@Size(max = 2048)`（DBの `photo.photo_view_log.referer` / `photo_list_filter_log.referer`
 * が `varchar(2048)`）と一致させること。
 */
export const REFERER_MAX_LENGTH = 2048;

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
