package com.web.gallery.constant;

/**
 * エンドポイントを管理するクラス
 */
public final class ApiRoutes {
	// APIバージョンプレフィックス
	/** APIバージョンプレフィックス */
	public static final String API_PREFIX = "/api/v1";

	// アカウント関連
	/** アカウントID */
	public static final String ACCOUNT_ID = "{accountId}";

	// 認証関連
	/** 認証APIプレフィックス */
	public static final String API_AUTH_PREFIX = API_PREFIX + "/auth";
	/** 認証ログインAPI */
	public static final String API_AUTH_LOGIN = API_AUTH_PREFIX + "/login";
	/** 認証リフレッシュAPI */
	public static final String API_AUTH_REFRESH = API_AUTH_PREFIX + "/refresh";
	/** 認証ログアウトAPI */
	public static final String API_AUTH_LOGOUT = API_AUTH_PREFIX + "/logout";

	// アカウント関連
	/** アカウントAPI（POST=登録） */
	public static final String API_ACCOUNTS = API_PREFIX + "/accounts";
	/** 個別アカウントAPI（PUT=更新） */
	public static final String API_ACCOUNT = API_ACCOUNTS + "/" + ACCOUNT_ID;
	/**
	 * アカウント削除API（POST=現在のパスワードによる再認証のうえ物理削除）<p>
	 * 本人確認情報をリクエストボディで安全に受け取れるよう、DELETEではなくPOSTのサブリソースとする
	 * （DELETEのボディは一部の中継経路で破棄されうるため。カスタムヘッダーはアクセスログ等に
	 * 記録されやすいため用いない）
	 */
	public static final String API_ACCOUNT_DELETION = API_ACCOUNT + "/deletion";

	/** 写真アカウントID */
	public static final String PHOTO_ACCOUNT_ID = "{photoAccountId}";

	// 都道府県関連
	/** 都道府県一覧API */
	public static final String API_PREFECTURES = API_PREFIX + "/prefectures";

	// 写真関連
	/** 写真API（GET=一覧取得, POST=新規登録, PUT=更新, DELETE=削除） */
	public static final String API_PHOTOS = API_PREFIX + "/accounts/" + PHOTO_ACCOUNT_ID + "/photos";
	/** 写真番号 */
	public static final String PHOTO_NO = "{photoNo}";
	/** 写真詳細API（GET=詳細取得） */
	public static final String API_PHOTO_DETAIL = API_PHOTOS + "/" + PHOTO_NO;
	/** 写真登録上限チェックAPI（GET=上限到達チェック） */
	public static final String API_PHOTO_UPPER_LIMIT = API_PHOTOS + "/upper-limit";

	// お気に入り関連
	/** お気に入りAPI（POST=登録, DELETE=解除） */
	public static final String API_FAVORITES = API_PREFIX + "/photos/favorites";

	// 管理者関連
	/** アカウント番号 */
	public static final String ACCOUNT_NO = "{accountNo}";
	/** 管理者APIプレフィックス */
	public static final String API_ADMIN_PREFIX = API_PREFIX + "/admin";
	/** 管理者用アカウント一覧API */
	public static final String API_ADMIN_ACCOUNTS = API_ADMIN_PREFIX + "/accounts";
	/** 管理者用アカウントロック解除API */
	public static final String API_ADMIN_ACCOUNT_UNLOCK = API_ADMIN_ACCOUNTS + "/" + ACCOUNT_NO + "/unlock";
	/** 管理者用アカウント強制ロックAPI */
	public static final String API_ADMIN_ACCOUNT_LOCK = API_ADMIN_ACCOUNTS + "/" + ACCOUNT_NO + "/lock";
}
