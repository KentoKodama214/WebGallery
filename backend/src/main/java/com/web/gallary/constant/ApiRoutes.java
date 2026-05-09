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

	// アカウント関連
	/** アカウントID */
	public static final String ACCOUNT_ID = "{accountId}";
	/** アカウント登録ページ */
	public static final String REGISTER = "/register";

	// REST API - 認証関連
	/** 認証ログインAPI */
	public static final String API_AUTH_LOGIN = API_PREFIX + "/auth/login";
	/** 認証リフレッシュAPI */
	public static final String API_AUTH_REFRESH = API_PREFIX + "/auth/refresh";
	/** 認証ログアウトAPI */
	public static final String API_AUTH_LOGOUT = API_PREFIX + "/auth/logout";

	// REST API - アカウント関連
	/** アカウントAPI（POST=登録） */
	public static final String API_ACCOUNTS = API_PREFIX + "/accounts";
	/** 個別アカウントAPI（PUT=更新） */
	public static final String API_ACCOUNT = API_ACCOUNTS + "/" + ACCOUNT_ID;

	// 写真関連（ページ）
	/** 写真アカウントID */
	public static final String PHOTO_ACCOUNT_ID = "{photoAccountId}";
	/** 写真一覧ページ */
	public static final String PHOTO_LIST = "/photo/" + PHOTO_ACCOUNT_ID + "/photo_list";
	/** 写真詳細ページ */
	public static final String PHOTO_DETAIL = "/photo/" + PHOTO_ACCOUNT_ID + "/photo_detail";
	/** 写真登録・編集ページ */
	public static final String PHOTO_SETTING = "/photo/" + PHOTO_ACCOUNT_ID + "/photo_setting";

	// REST API - 都道府県関連
	/** 都道府県一覧API */
	public static final String API_PREFECTURES = API_PREFIX + "/prefectures";

	// REST API - 写真関連
	/** 写真API（GET=一覧取得, POST=新規登録, PUT=更新, DELETE=削除） */
	public static final String API_PHOTOS = API_PREFIX + "/accounts/" + PHOTO_ACCOUNT_ID + "/photos";
	/** 写真番号 */
	public static final String PHOTO_NO = "{photoNo}";
	/** 写真詳細API（GET=詳細取得） */
	public static final String API_PHOTO_DETAIL = API_PHOTOS + "/" + PHOTO_NO;

	// REST API - お気に入り関連
	/** お気に入りAPI（POST=登録, DELETE=解除） */
	public static final String API_FAVORITES = API_PREFIX + "/photos/favorites";
}
