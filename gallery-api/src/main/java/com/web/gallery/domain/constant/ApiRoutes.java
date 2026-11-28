package com.web.gallery.domain.constant;

/** エンドポイントを管理するクラス */
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

  /** 認証リフレッシュAPI */
  public static final String API_AUTH_REFRESH = API_AUTH_PREFIX + "/refresh";

  /** 認証ログアウトAPI */
  public static final String API_AUTH_LOGOUT = API_AUTH_PREFIX + "/logout";

  /** パスキー認証オプション発行API（POST=チャレンジ等の発行。未ログインで呼び出す） */
  public static final String API_AUTH_PASSKEY_OPTIONS = API_AUTH_PREFIX + "/passkey/options";

  /** パスキーログインAPI（POST=認証レスポンスの検証とトークン発行） */
  public static final String API_AUTH_PASSKEY_LOGIN = API_AUTH_PREFIX + "/passkey/login";

  /** 復旧オプション発行API（POST=リカバリーコードで本人確認し、新しいパスキーの登録オプションを発行。未ログインで呼び出す） */
  public static final String API_AUTH_RECOVERY_OPTIONS = API_AUTH_PREFIX + "/recovery/options";

  /** 復旧ログインAPI（POST=新しいパスキーの登録レスポンスを検証して登録し、トークンを発行） */
  public static final String API_AUTH_RECOVERY_LOGIN = API_AUTH_PREFIX + "/recovery/login";

  // アカウント関連
  /** アカウントAPI（POST=登録） */
  public static final String API_ACCOUNTS = API_PREFIX + "/accounts";

  /**
   * アカウント新規登録オプション発行API（POST=最初のパスキーを作成するためのチャレンジ等を発行。未ログインで呼び出す）
   *
   * <p>アカウントIDは半角英数字のみのため、{@code {accountId}}のパスとは衝突しない
   */
  public static final String API_ACCOUNT_SIGNUP_OPTIONS = API_ACCOUNTS + "/signup-options";

  /** 個別アカウントAPI（PUT=更新） */
  public static final String API_ACCOUNT = API_ACCOUNTS + "/" + ACCOUNT_ID;

  /**
   * アカウント削除API（POST=パスキーによる再認証のうえ物理削除）
   *
   * <p>本人確認情報をリクエストボディで安全に受け取れるよう、DELETEではなくPOSTのサブリソースとする
   * （DELETEのボディは一部の中継経路で破棄されうるため。カスタムヘッダーはアクセスログ等に 記録されやすいため用いない）
   */
  public static final String API_ACCOUNT_DELETION = API_ACCOUNT + "/deletion";

  /** ロケーション一覧API（GET=本人のロケーションマスタ一覧取得） */
  public static final String API_LOCATIONS = API_ACCOUNT + "/locations";

  /**
   * 本人確認オプション発行API（POST=重要操作の前にパスキーで本人確認するためのチャレンジ等を発行）
   *
   * <p>発行したチャレンジへの署名（認証レスポンス）を、パスキーの登録・削除、退会、リカバリーコードの再発行のリクエストに添える
   */
  public static final String API_REAUTHENTICATION_OPTIONS =
      API_ACCOUNT + "/reauthentication-options";

  /**
   * リカバリーコードAPI（GET=未使用のリカバリーコードの件数取得、POST=パスキーによる再認証のうえ再発行）
   *
   * <p>再発行は本人確認情報をリクエストボディで受け取るためPOSTとする
   */
  public static final String API_RECOVERY_CODES = API_ACCOUNT + "/recovery-codes";

  /** パスキー一覧・登録API（GET=本人のパスキー一覧取得、POST=登録レスポンスの検証と登録） */
  public static final String API_PASSKEYS = API_ACCOUNT + "/passkeys";

  /**
   * パスキー登録オプション発行API（POST=パスキーによる再認証のうえチャレンジ等を発行）
   *
   * <p>本人確認情報をリクエストボディで受け取るため、GETではなくPOSTとする
   */
  public static final String API_PASSKEY_REGISTRATION_OPTIONS =
      API_PASSKEYS + "/registration-options";

  /** パスキー番号 */
  public static final String PASSKEY_NO = "{passkeyNo}";

  /** 個別パスキーAPI（PUT=パスキー名の変更） */
  public static final String API_PASSKEY = API_PASSKEYS + "/" + PASSKEY_NO;

  /**
   * パスキー削除API（POST=パスキーによる再認証のうえ削除）
   *
   * <p>アカウント削除APIと同じく、本人確認情報をリクエストボディで受け取るためPOSTのサブリソースとする
   */
  public static final String API_PASSKEY_DELETION = API_PASSKEY + "/deletion";

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

  // ポートフォリオ関連
  /** ポートフォリオAPI（GET=本人の一覧取得（下書きを含む。検索語でテーマ・タグを絞り込み可）, POST=下書きとして新規登録） */
  public static final String API_PORTFOLIOS = API_ACCOUNT + "/portfolios";

  /** ポートフォリオ番号 */
  public static final String PORTFOLIO_NO = "{portfolioNo}";

  /** 個別ポートフォリオAPI（GET=本人の詳細取得, PUT=更新, DELETE=物理削除） */
  public static final String API_PORTFOLIO_DETAIL = API_PORTFOLIOS + "/" + PORTFOLIO_NO;

  /** ポートフォリオ公開状態API（PUT=公開・下書きへの切り替え） */
  public static final String API_PORTFOLIO_STATUS = API_PORTFOLIO_DETAIL + "/status";

  /** ポートフォリオ表示順API（PUT=表示順で隣のポートフォリオと入れ替えて1つ上・下へ移動） */
  public static final String API_PORTFOLIO_DISPLAY_ORDER = API_PORTFOLIO_DETAIL + "/display-order";

  /** ポートフォリオ作成上限チェックAPI（GET=作成数の上限到達チェック） */
  public static final String API_PORTFOLIO_UPPER_LIMIT = API_PORTFOLIOS + "/upper-limit";

  /** ポートフォリオ用の写真検索API（GET=本人の写真をタイトル・撮影場所・タグで検索） */
  public static final String API_PORTFOLIO_PHOTOS = API_ACCOUNT + "/portfolio-photos";

  /** ポートフォリオテンプレート一覧API（GET=テンプレート・グリッド型の行パターンの一覧取得） */
  public static final String API_PORTFOLIO_TEMPLATES = API_PREFIX + "/portfolio-templates";

  // 額縁テンプレート関連
  /** 額縁テンプレートAPI（GET=本人の一覧取得, POST=新規登録） */
  public static final String API_FRAME_TEMPLATES = API_ACCOUNT + "/frame-templates";

  /** 額縁テンプレート番号 */
  public static final String FRAME_TEMPLATE_NO = "{frameTemplateNo}";

  /** 額縁テンプレート詳細API（GET=詳細取得, PUT=更新, DELETE=削除） */
  public static final String API_FRAME_TEMPLATE_DETAIL =
      API_FRAME_TEMPLATES + "/" + FRAME_TEMPLATE_NO;

  // お気に入り関連
  /** お気に入りAPI（POST=登録, DELETE=解除） */
  public static final String API_FAVORITES = API_PREFIX + "/photos/favorites";

  // お問い合わせ関連
  /** お問い合わせAPI（GET=自分の一覧取得, POST=新規登録） */
  public static final String API_INQUIRIES = API_PREFIX + "/inquiries";

  /** お問い合わせ番号 */
  public static final String INQUIRY_NO = "{inquiryNo}";

  /** お問い合わせ詳細API（GET=自分の詳細取得） */
  public static final String API_INQUIRY_DETAIL = API_INQUIRIES + "/" + INQUIRY_NO;

  /** お問い合わせ既読化API（POST=既読化実行） */
  public static final String API_INQUIRY_READ = API_INQUIRY_DETAIL + "/read";

  /** お問い合わせ取り下げAPI（POST=取り下げ実行） */
  public static final String API_INQUIRY_WITHDRAWAL = API_INQUIRY_DETAIL + "/withdrawal";

  // 管理者関連
  /** アカウント番号 */
  public static final String ACCOUNT_NO = "{accountNo}";

  /** 管理者APIプレフィックス */
  public static final String API_ADMIN_PREFIX = API_PREFIX + "/admin";

  /** 管理者用アカウント一覧API */
  public static final String API_ADMIN_ACCOUNTS = API_ADMIN_PREFIX + "/accounts";

  /** 管理者用アカウントロック解除API */
  public static final String API_ADMIN_ACCOUNT_UNLOCK =
      API_ADMIN_ACCOUNTS + "/" + ACCOUNT_NO + "/unlock";

  /** 管理者用アカウント強制ロックAPI */
  public static final String API_ADMIN_ACCOUNT_LOCK =
      API_ADMIN_ACCOUNTS + "/" + ACCOUNT_NO + "/lock";

  /** 管理者用アカウント権限変更API */
  public static final String API_ADMIN_ACCOUNT_AUTHORITY =
      API_ADMIN_ACCOUNTS + "/" + ACCOUNT_NO + "/authority";

  // 管理者用お問い合わせ関連
  /** お問い合わせID */
  public static final String INQUIRY_ID = "{inquiryId}";

  /** 管理者用お問い合わせ一覧API（GET=全件一覧取得） */
  public static final String API_ADMIN_INQUIRIES = API_ADMIN_PREFIX + "/inquiries";

  /** 管理者用お問い合わせ詳細API（GET=詳細取得） */
  public static final String API_ADMIN_INQUIRY_DETAIL = API_ADMIN_INQUIRIES + "/" + INQUIRY_ID;

  /** 管理者用お問い合わせ返信API（POST=返信登録） */
  public static final String API_ADMIN_INQUIRY_REPLY = API_ADMIN_INQUIRY_DETAIL + "/replies";

  // 管理者用写真関連
  /** 管理者用写真一覧API（GET=全アカウントの写真一覧取得） */
  public static final String API_ADMIN_PHOTOS = API_ADMIN_PREFIX + "/photos";

  /** 管理者用写真公開状態変更API（PUT=非公開・非公開解除） */
  public static final String API_ADMIN_PHOTO_VISIBILITY =
      API_ADMIN_PHOTOS + "/" + ACCOUNT_NO + "/" + PHOTO_NO + "/visibility";
}
