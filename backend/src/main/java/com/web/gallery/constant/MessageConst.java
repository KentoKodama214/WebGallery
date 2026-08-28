package com.web.gallery.constant;

/**
 * メッセージを管理するクラス
 */
public final class MessageConst {
	// Info
	public static final String REGIST_FAVORITE = "お気に入りに追加しました。";
	public static final String CANCEL_FAVORITE = "お気に入りを解除しました。";
	public static final String REGIST_PHOTO = "写真登録が完了しました。";
	public static final String DELETE_PHOTO = "写真削除が完了しました。";
	
	// Warning
	public static final String USER_NOT_FOUND = "ユーザーが見つかりません。";
	
	// Error
	public static final String ERR_INVALID_INPUT = "入力内容に誤りがあります。再度入力してください。";
	public static final String ERR_FAIL_TO_REGIST_ACCOUNT = "アカウント登録でエラーが発生しました。登録をやり直してください。";
	public static final String ERR_FAIL_TO_UPDATE_ACCOUNT = "アカウント更新でエラーが発生しました。更新をやり直してください。";
	public static final String ERR_NOT_AUTHORIZED_TO_EDIT_ACCOUNT = "アカウントを編集する権限がありません。";
	public static final String ERR_FAIL_TO_REGIST_PHOTO = "写真登録でエラーが発生しました。登録をやり直してください。";
	public static final String ERR_FAIL_TO_UPDATE_PHOTO = "写真更新でエラーが発生しました。更新をやり直してください。";
	public static final String ERR_FAIL_TO_DELETE_PHOTO = "写真削除でエラーが発生しました。削除をやり直してください。";
	public static final String ERR_NOT_AUTHORIZED_TO_EDIT_PHOTO = "写真を登録・編集する権限がありません。";
	public static final String ERR_FAIL_TO_REGIST_PHOTO_TAG = "写真タグ登録でエラー発生しました。登録をやり直してください。";
	public static final String ERR_FAIL_TO_REGIST_FAVORITE = "お気に入り登録でエラー発生しました。登録をやり直してください。";
	public static final String ERR_FAIL_TO_CANCEL_FAVORITE = "お気に入り解除でエラー発生しました。解除をやり直してください。";
	public static final String ERR_DUPLICATE_PHOTO_FILE = "写真登録でエラーが発生しました。（既に同じファイル名でアップロード済みです）";
	public static final String ERR_PHOTO_NOT_FOUND = "写真が存在しません。";
	public static final String ERR_REACHED_REGISTRATION_LIMIT = "写真の登録枚数が上限に達しています。";
	public static final String ERR_NOT_AUTHORIZED_TO_ADMIN = "管理者権限がありません。";
	public static final String ERR_FAIL_TO_DELETE_ACCOUNT = "アカウント削除でエラーが発生しました。削除をやり直してください。";
	public static final String ERR_INVALID_REFRESH_TOKEN = "無効なリフレッシュトークンです。";
	public static final String ERR_REFRESH_TOKEN_EXPIRED = "リフレッシュトークンの有効期限が切れています。";
	public static final String ERR_BAD_CREDENTIALS = "アカウントIDまたはパスワードが間違っています。";
	public static final String ERR_ACCOUNT_LOCKED = "アカウントがロックされています。";
	public static final String ERR_IMAGE_FILE_REQUIRED = "新規登録時は画像ファイルの指定が必須です。";
	public static final String ERR_UNSUPPORTED_IMAGE_CONTENT_TYPE = "許可されていないファイル形式です。";
	public static final String ERR_INVALID_IMAGE_SIGNATURE = "画像ファイルの内容が不正です。";
	public static final String ERR_IMAGE_FILE_SIZE_EXCEEDED = "画像ファイルのサイズが上限を超えています。";

	// Admin
	public static final String UNLOCK_ACCOUNT = "アカウントのロックを解除しました。";
	public static final String LOCK_ACCOUNT = "アカウントをロックしました。";
}