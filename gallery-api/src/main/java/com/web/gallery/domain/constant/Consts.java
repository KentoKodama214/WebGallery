package com.web.gallery.domain.constant;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/** デフォルト値を管理するクラス */
public final class Consts {
  // String
  /** 空文字（""） */
  public static final String STRING_EMPTY = "";

  /** 半角スペース */
  public static final String HALF_SPACE = " ";

  /** 全角スペース */
  public static final String FULL_SPACE = "　";

  /** none */
  public static final String STRING_NONE = "none";

  /** mini-user */
  public static final String AUTHORITY_MINI = "mini-user";

  /** normal-user */
  public static final String AUTHORITY_NORMAL = "normal-user";

  /** special-user */
  public static final String AUTHORITY_SPECIAL = "special-user";

  /** administrator */
  public static final String ADMINISTRATOR = "administrator";

  /** prefecture */
  public static final String PREFECTURE = "prefecture";

  // ZoneOffset
  public static final ZoneOffset JST = ZoneOffset.ofHours(9);

  /** タイムゾーンID（日本標準時）。{@code @Scheduled} の zone 属性等で使用する */
  public static final String ZONE_ID_ASIA_TOKYO = "Asia/Tokyo";

  // LocalDate
  /** 1900-01-01 */
  public static final LocalDate MIN_LOCAL_DATE = LocalDate.of(1900, 1, 1);

  // OffsetDateTime
  /** 1900-01-01 00:00:00 (+9:00) */
  public static final OffsetDateTime MIN_OFFSET_DATE_TIME =
      OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, JST);

  // Hash Algorithm
  public static final String SHA_256 = "SHA-256";

  // Integer
  /** 写真一覧のタグリストで指定できるタグ数の上限 */
  public static final Integer TAG_LIST_MAX_SIZE = 20;

  /** 1枚の写真に登録できるタグ数の上限（アノテーション属性で参照するためint型で定義する） */
  public static final int PHOTO_TAG_MAX_SIZE = 20;

  /** タグ名（日本語・英語）の最大文字数（DBの varchar(20) に合わせる） */
  public static final int TAG_NAME_MAX_LENGTH = 20;

  /** 写真の新規一括登録で1リクエストにつき送信できる写真枚数の上限（アノテーション属性で参照するためint型で定義する） */
  public static final int PHOTO_BULK_REGIST_MAX_SIZE = 10;

  /** お問い合わせ件名の最大文字数（アノテーション属性で参照するためint型で定義する） */
  public static final int INQUIRY_SUBJECT_MAX_LENGTH = 100;

  /** お問い合わせ本文・返信本文の最大文字数（アノテーション属性で参照するためint型で定義する） */
  public static final int INQUIRY_BODY_MAX_LENGTH = 2000;

  /** 管理者が写真を非公開・非公開解除する理由の最大文字数（DBの varchar(500) に合わせる。アノテーション属性で参照するためint型で定義する） */
  public static final int MODERATION_REASON_MAX_LENGTH = 500;

  /** ポートフォリオのテーマ・写真ごとのテーマ・サブテーマの最大文字数（アノテーション属性で参照するためint型で定義する） */
  public static final int PORTFOLIO_THEME_MAX_LENGTH = 100;

  /** ポートフォリオの説明文・写真ごとの説明の最大文字数（アノテーション属性で参照するためint型で定義する） */
  public static final int PORTFOLIO_DESCRIPTION_MAX_LENGTH = 2000;

  /** 1つのポートフォリオに設定できる写真枚数の上限（アノテーション属性で参照するためint型で定義する） */
  public static final int PORTFOLIO_PHOTO_MAX_SIZE = 200;

  /** 1つのポートフォリオに登録できるタグ数の上限（アノテーション属性で参照するためint型で定義する） */
  public static final int PORTFOLIO_TAG_MAX_SIZE = 30;

  /** ポートフォリオのテンプレート設定・写真設定のJSON構造の現行バージョン */
  public static final int PORTFOLIO_SETTINGS_VERSION = 1;

  /** 1アカウントに登録できるパスキー数の上限 */
  public static final int PASSKEY_MAX_COUNT = 10;

  /** パスキー名の最大文字数（アノテーション属性で参照するためint型で定義する） */
  public static final int PASSKEY_NAME_MAX_LENGTH = 50;

  /**
   * パスキーの登録・認証レスポンスJSONの最大文字数（アノテーション属性で参照するためint型で定義する）
   *
   * <p>クレデンシャルIDは最大1023バイト、公開鍵・attestationを含めても通常は数KBに収まるため、余裕を持たせた上限とする
   */
  public static final int PASSKEY_RESPONSE_JSON_MAX_LENGTH = 32768;

  /** WebAuthnチャレンジの乱数のバイト数（W3C WebAuthnの推奨は16バイト以上） */
  public static final int WEBAUTHN_CHALLENGE_BYTES = 32;

  /** パスキーのユーザーハンドル（WebAuthnのuser.id）の乱数のバイト数（仕様上の上限は64バイト） */
  public static final int PASSKEY_USER_HANDLE_BYTES = 32;

  /**
   * パスキーで受け付ける公開鍵の署名アルゴリズム（COSEアルゴリズム識別子、優先順）
   *
   * <p>-7=ES256（ほぼすべての認証器が対応）、-8=EdDSA（一部のセキュリティキー）、-257=RS256（Windows Hello等）。
   * 登録オプション（pubKeyCredParams）と登録レスポンスの検証の双方でこの一覧を使う
   */
  public static final List<Long> PASSKEY_PUBLIC_KEY_ALGORITHMS = List.of(-7L, -8L, -257L);

  /** 1回に発行するリカバリーコードの個数 */
  public static final int RECOVERY_CODE_COUNT = 10;

  /**
   * リカバリーコードの文字数（区切りのハイフンを除く）
   *
   * <p>32種類の文字（{@link #RECOVERY_CODE_ALPHABET}）×16文字＝80ビットの乱数とし、オンラインでの総当たりを現実的に不可能にする
   */
  public static final int RECOVERY_CODE_LENGTH = 16;

  /** リカバリーコードに使う文字（読み間違えやすいI・L・O・Uを除いたCrockfordのBase32） */
  public static final String RECOVERY_CODE_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

  /** リカバリーコード入力欄の最大文字数（ハイフン・空白の混入を許容した上限。アノテーション属性で参照するためint型で定義する） */
  public static final int RECOVERY_CODE_INPUT_MAX_LENGTH = 64;

  /** 認証前でアカウントが未確定であることを表すアカウント番号（WebAuthnの認証用・新規登録用チャレンジで使用する） */
  public static final long ACCOUNT_NO_UNKNOWN = 0L;

  /** 額縁テンプレートのテンプレート名の最大文字数（DBの varchar(50) に合わせる。アノテーション属性で参照するためint型で定義する） */
  public static final int FRAME_TEMPLATE_NAME_MAX_LENGTH = 50;

  /** 1つのアカウントが作成できる額縁テンプレートの数の上限 */
  public static final int FRAME_TEMPLATE_MAX_SIZE = 50;

  /** 1つの額縁に配置できる写真枠の数の上限 */
  public static final int FRAME_SLOT_MAX_SIZE = 30;

  /** 1つの額縁に配置できるメッセージ枠の数の上限 */
  public static final int FRAME_TEXT_MAX_SIZE = 10;

  /** 額縁に添えるメッセージ1件あたりの最大文字数 */
  public static final int FRAME_MESSAGE_MAX_LENGTH = 200;
}
