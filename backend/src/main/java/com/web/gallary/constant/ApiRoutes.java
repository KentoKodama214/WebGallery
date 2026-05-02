package com.web.gallary.constant;

/**
 * エンドポイントを管理するクラス
 */
public final class ApiRoutes {
	// APIバージョンプレフィックス
	/** APIバージョンプレフィックス */
	public static final String API_PREFIX = "/api/v1";

	// 共通
	/** ログインページ */
	public static final String LOGIN = "/login";
	/** ログアウト */
	public static final String LOGOUT = "/logout";
	/** ヘッダー */
	public static final String HEADER = "/header";
	/** フッター */
	public static final String FOOTER = "/footer";
	/** エラー */
	public static final String ERROR = "/error";
	/** エラーページ */
	public static final String ERROR_PAGE = "/error_page";

	// アカウント関連
	/** アカウントID */
	public static final String ACCOUNT_ID = "{accountId}";
	/** アカウント登録ページ */
	public static final String REGISTER = "/register";
	/** アカウント設定ページ */
	public static final String ACCOUNT_SETTING = "/" + ACCOUNT_ID + "/account_setting";
	/** アカウント一覧ページ */
	public static final String ACCOUNT_LIST = "/account_list";

	// REST API - アカウント関連
	/** アカウントAPI（POST=登録） */
	public static final String API_ACCOUNTS = API_PREFIX + "/accounts";
	/** 個別アカウントAPI（PUT=更新） */
	public static final String API_ACCOUNT = API_ACCOUNTS + "/" + ACCOUNT_ID;

	// 写真関連（ページ）
	/** 写真 */
	public static final String PHOTO = "/photo";
	/** 写真アカウントID */
	public static final String PHOTO_ACCOUNT_ID = "{photoAccountId}";
	/** 写真一覧ページ */
	public static final String PHOTO_LIST = PHOTO + "/" + PHOTO_ACCOUNT_ID + "/photo_list";
	/** 写真詳細ページ */
	public static final String PHOTO_DETAIL = PHOTO + "/" + PHOTO_ACCOUNT_ID + "/photo_detail";
	/** 写真登録・編集ページ */
	public static final String PHOTO_SETTING = PHOTO + "/" + PHOTO_ACCOUNT_ID + "/photo_setting";

	// REST API - 写真関連
	/** 写真API（GET=一覧取得, POST=新規登録, PUT=更新, DELETE=削除） */
	public static final String API_PHOTOS = API_PREFIX + "/accounts/" + PHOTO_ACCOUNT_ID + "/photos";

	// REST API - お気に入り関連
	/** お気に入りAPI（POST=登録, DELETE=解除） */
	public static final String API_FAVORITES = API_PREFIX + "/photos/favorites";
}
