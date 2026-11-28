# データ辞書

全テーブルのカラムを物理名・論理名・データ型で重複排除し、物理名の昇順で一覧化したものです。

## カラム辞書

| No | 物理名 | 論理名 | データ型 | デフォルト値 | コメント | 使用テーブル |
|----|--------|--------|----------|-------------|----------|-------------|
| 1 | account_id | アカウントID | varchar(20) | - | 8〜20文字の英数字。アカウントを識別する一意のID（ログインはパスキーで行うため入力不要。リカバリーコードによる復旧時に入力する） | account |
| 2 | account_name | アカウント名 | varchar(50) | - | ユーザーの表示名 | account |
| 1 | account_no | アカウント番号 | bigserial / bigint | (自動採番) / - | アカウントを一意に特定するための番号。accountテーブルではPK（自動採番）、他テーブルでは概ねFK。account_authorityではPK兼FK（accountと1対1）。ただしphoto_list_filter_log・photo_view_logのaccount_noは閲覧者のアカウント番号を表し、未ログインの場合は0（センチネル値のためFKなし）。webauthn_challengeのaccount_noは、認証用・新規登録用チャレンジ（アカウント未確定・未作成）で0を取るためFKなし | account, account_authority, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, inquiry_mst, portfolio_mst, portfolio_photo, portfolio_tag_mst, passkey_credential, passkey_user_handle, webauthn_challenge, recovery_code, frame_template_mst |
| 2 | address | 住所 | text | '' | 撮影場所の住所 | location_mst |
| 3 | admin_account_no | 管理者アカウント番号 | bigint | - | 返信した管理者のアカウント番号。common.account(account_no)へのFK。退会可否判定（管理者返信の存在確認）用インデックスあり。photo_moderation_logでは非公開・解除を操作した管理者のアカウント番号（管理者の退会を妨げないようFKなし） | inquiry_reply_mst, photo_moderation_log |
| 4 | attested_credential_data | 認証器データ | bytea | - | 公開鍵（COSE）・AAGUID・クレデンシャルIDを含むattestedCredentialData（webauthn4jのバイナリ表現）。署名の検証に使用する | passkey_credential |
| 5 | authority_kbn | 権限区分 | common.authority_enum | - | mini-user/normal-user/special-user/administratorの4段階。写真アップロード上限に影響 | account_authority |
| 6 | birthdate | 生年月日 | date | '1900-01-01' | 個人情報管理の観点で、必須入力なし、かつ年月まで。データ登録時にすべて1日に変換する | account |
| 7 | birthplace_prefecture_kbn_code | 出身地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 8 | body | 本文 | text | - | お問い合わせの本文、または返信の本文。空文字不可 | inquiry_mst, inquiry_reply_mst |
| 9 | caption | キャプション | text | '""' | 写真の説明文 | photo_mst |
| 10 | challenge | チャレンジ | varchar(100) | - | サーバ生成の32バイト乱数（base64url）。UNIQUE。パスキーの登録・認証・本人確認の検証時に1度だけ消費（削除）する | webauthn_challenge |
| 11 | challenge_no | チャレンジNo | bigserial | (自動採番) | WebAuthnチャレンジのPK（自動採番） | webauthn_challenge |
| 12 | code_hash | コードハッシュ | varchar(64) | - | リカバリーコード（80ビットの乱数）の正規化後の値のSHA-256（16進数）。平文は発行時に一度だけ利用者へ表示し、保存しない。使用時に行ごと削除する。(account_no, code_hash)でUNIQUE | recovery_code |
| 13 | country | 国 | varchar(2) | '' | IPアドレスから解決したISO 3166-1 alpha-2コード。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 14 | created_at | 作成日時 | timestamp with time zone | - / NOW() | レコード作成日時（タイムゾーン付き） | account, account_authority, kbn_mst, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, photo_moderation_log, inquiry_mst, inquiry_reply_mst, portfolio_mst, portfolio_photo, portfolio_tag_mst, passkey_credential, passkey_user_handle, webauthn_challenge, recovery_code, frame_template_mst |
| 15 | created_by | 作成者 | bigint（kbn_mstのみint） | - | レコードを作成したアカウント番号。システム側が作成した場合は'0'を入れる | account, account_authority, kbn_mst, location_mst, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, photo_moderation_log, inquiry_mst, inquiry_reply_mst, portfolio_mst, portfolio_photo, portfolio_tag_mst, passkey_credential, passkey_user_handle, webauthn_challenge, recovery_code, frame_template_mst |
| 16 | credential_id | クレデンシャルID | varchar(1400) | - | 認証器が発行したクレデンシャルID（base64url）。UNIQUE。パスキーログイン時の引き当てに使用する | passkey_credential |
| 17 | description | 説明文 | text | '' | ポートフォリオの説明文 | portfolio_mst |
| 18 | direction_kbn | 写真の向き | photo.direction_enum | 'none' | vertical（縦）/horizontal（横）/square（正方形）/none（未設定） | photo_mst, photo_list_filter_log |
| 19 | display_name | 表示名 | text | - | 撮影場所の表示用名称。写真詳細等での表示に使用し、重複可（一意制約はmanagement_name側） | location_mst |
| 20 | display_order | 表示順 | int | - | portfolio_photoではポートフォリオ内の写真の表示順（1始まりの連番）で、account_no, portfolio_noとの複合UNIQUEを構成。portfolio_mstではアカウント内のポートフォリオの並び順（1始まり。新規登録時は末尾＝最大値+1。削除による欠番を許容し、並び替え時に1から振り直す） | portfolio_mst, portfolio_photo |
| 21 | expires_at | 有効期限 | timestamp with time zone | - | refresh_tokenではリフレッシュトークンの有効期限、webauthn_challengeではチャレンジの有効期限。いずれも期限切れ行を削除する定期実行タスク用インデックスあり | refresh_token, webauthn_challenge |
| 22 | explanation | 説明 | text | '""' | 区分コードの補足説明 | kbn_mst |
| 23 | f_value | F値 | decimal(5,2) | - | EXIF情報から取得した絞り値 | photo_mst |
| 24 | favorite_photo_account_no | 写真所有者のアカウント番号 | bigint | - | お気に入り対象の写真を所有するアカウント番号。photo_mst(account_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 25 | favorite_photo_no | お気に入り写真番号 | bigint | - | お気に入り対象の写真番号。photo_mst(photo_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 26 | focal_length | 焦点距離 | int | - | EXIF情報から取得した焦点距離（mm単位） | photo_mst |
| 27 | frame_definition | 額縁定義 | jsonb | '{}' | 額縁のサイズ（size.width/height）・背景（background.color）・外枠（border.color/width）・写真枠（slots。位置・大きさ・傾き・枠線・影）・メッセージ枠（texts。位置・幅・傾き・フォント・文字サイズ・色・揃え・太字・斜体）の定義（JSON）。長さはすべてmmで、写真枠・メッセージ枠の位置は台紙（額縁から外枠を除いた部分）の左上からの距離。ポートフォリオで選択するとportfolio_mst.template_settingsにコピーされるため、テンプレートの変更・削除は既存のポートフォリオに影響しない | frame_template_mst |
| 28 | frame_template_no | 額縁テンプレート番号 | bigint | - | アカウント単位の額縁テンプレート連番（登録済みの最大値+1で採番）。account_noとの複合UNIQUEを構成 | frame_template_mst |
| 29 | free_memo | フリーメモ | text | '""' | ユーザーが自由に入力できるメモ欄 | account |
| 30 | id | ID | bigserial | (自動採番) | サロゲートキー（自動採番） | location_mst, photo_mst, photo_tag_mst, photo_favorite, inquiry_mst, inquiry_reply_mst, portfolio_mst, portfolio_photo, portfolio_tag_mst, frame_template_mst |
| 31 | image_file_name | 画像ファイル名 | text | - | アップロード時のクライアント送信ファイル名（ベース名）。表示用・写真登録時の重複存在チェックの等価検索に使用。オブジェクトキー（image_file_path）とは独立 | photo_mst |
| 32 | image_file_path | 画像ファイルパス | text | - | S3オブジェクトキー（サーバ生成の不透明値 `{アカウント番号}/{写真番号}-{ランダム}.{拡張子}`）。閲覧時はこのキーから署名付きURLを発行する | photo_mst |
| 33 | inquiry_id | お問い合わせID | bigint | - | inquiry_mst(id)へのFK | inquiry_reply_mst |
| 34 | inquiry_no | お問い合わせ番号 | bigint | - | アカウント単位のお問い合わせ連番。account_noとの複合UNIQUEを構成 | inquiry_mst |
| 35 | ip_address | 送信元IPアドレス | varchar(45) | - | アクセス元のIPアドレス（IPv6を考慮した最大長） | login_history, photo_list_filter_log, photo_view_log |
| 36 | is_admin_locked | 管理者ロックフラグ | boolean | false | 管理者が強制ロックした場合にtrue。ログイン失敗回数による自動ロック解除の対象外 | account |
| 37 | is_backed_up | バックアップ済みフラグ | boolean | false | 認証器データのBSフラグ（クラウド等にバックアップ済みか）。認証のたびに更新する | passkey_credential |
| 38 | is_backup_eligible | バックアップ可能フラグ | boolean | false | 認証器データのBEフラグ（複数端末に同期されるパスキーか） | passkey_credential |
| 39 | is_deleted | 削除フラグ | boolean | false | 論理削除フラグ。trueの場合は削除済み | account, location_mst, photo_mst |
| 40 | is_favorite | お気に入り写真のみ絞り込みフラグ | boolean | - | 写真一覧の検索条件として「お気に入りのみ」が指定されたか | photo_list_filter_log |
| 41 | is_hidden | 非公開フラグ | boolean | - | 管理者による操作の種別。trueなら写真を非公開にした操作、falseなら非公開を解除した操作 | photo_moderation_log |
| 42 | is_hidden_by_admin | 管理者非公開フラグ | boolean | false | trueなら管理者により非公開にされており、写真一覧・写真詳細・公開ポートフォリオで所有者本人以外には表示しない。解除できるのは管理者のみ | photo_mst |
| 43 | is_location_public | 位置情報公開フラグ | boolean | false | trueなら撮影場所（緯度経度・住所・表示名）を写真所有者以外にも公開する。falseの場合、写真詳細APIは所有者以外へこれらを返さない | photo_mst |
| 44 | is_read_by_user | ユーザー既読フラグ | boolean | true | 管理者の返信をユーザーが確認済みかどうか。返信投稿時にfalseへ更新される | inquiry_mst |
| 45 | is_revoked | 無効化フラグ | boolean | false | リフレッシュトークンの無効化フラグ。ログアウト時やトークンローテーション時にtrueに設定 | refresh_token |
| 46 | is_uv_initialized | ユーザー検証済みフラグ | boolean | false | 登録時にユーザー検証（生体認証・PIN）が行われたか | passkey_credential |
| 47 | iso | ISO感度 | int | - | EXIF情報から取得したISO感度 | photo_mst |
| 48 | kbn_class_code | 区分分類コード | varchar(20) | - | 区分の大分類コード（例: prefecture） | kbn_mst |
| 49 | kbn_class_english_name | 区分分類英語名 | varchar(20) | '""' | 区分分類の英語表記 | kbn_mst |
| 50 | kbn_class_japanese_name | 区分分類日本語名（空文字不可） | varchar(20) | - | 区分分類の日本語表記。kbn_japanese_nameとの複合UNIQUEを構成 | kbn_mst |
| 51 | kbn_code | 区分コード | varchar(20) | - | 区分の個別コード。kbn_class_codeとの複合PKを構成 | kbn_mst |
| 52 | kbn_english_name | 区分英語名 | varchar(20) | '""' | 区分の英語表記 | kbn_mst |
| 53 | kbn_group_code | 区分グループコード | varchar(20) | '""' | 区分のグループ分類コード（例: 地域コード） | kbn_mst |
| 54 | kbn_group_english_name | 区分グループ英語名 | varchar(20) | '""' | 区分グループの英語表記 | kbn_mst |
| 55 | kbn_group_japanese_name | 区分グループ日本語名 | varchar(20) | '""' | 区分グループの日本語表記 | kbn_mst |
| 56 | kbn_japanese_name | 区分日本語名 | varchar(20) | - | 区分の日本語表記。kbn_class_japanese_nameとの複合UNIQUEを構成（空文字不可） | kbn_mst |
| 57 | last_login_datetime | 最終ログイン日時 | timestamp with time zone | - | ユーザーが最後にログインした日時 | account |
| 58 | last_used_at | 最終使用日時 | timestamp with time zone | NOW() | パスキーで最後にログインした日時。登録直後は登録日時 | passkey_credential |
| 59 | latitude | 緯度 | decimal(11,4) | - | 撮影場所の緯度座標 | location_mst |
| 60 | location_no | ロケーション番号 | bigint | - | アカウント単位のロケーション連番 | location_mst, photo_mst |
| 61 | login_failure_count | ログイン失敗回数 | smallint | 0 | 連続ログイン失敗回数。ログイン成功時にリセット | account |
| 62 | login_history_no | ログイン履歴No | bigserial | (自動採番) | ログイン履歴のPK（自動採番） | login_history |
| 63 | login_method_kbn | ログイン方式区分 | common.login_method_enum | 'passkey' | passkey（パスキー）/recovery_code（リカバリーコードによる復旧） | login_history |
| 64 | longitude | 経度 | decimal(11,4) | - | 撮影場所の経度座標 | location_mst |
| 65 | management_name | 管理名 | text | - | 撮影場所の管理用名称。同一アカウント内で一意（既存マスタからの選択・新規登録時の重複判定に使用） | location_mst |
| 66 | passkey_name | パスキー名 | varchar(50) | - | 利用者が管理画面で識別するための名前 | passkey_credential |
| 67 | passkey_no | パスキー番号 | bigserial | (自動採番) | パスキーのPK（自動採番） | passkey_credential |
| 68 | passkey_user_handle_no | パスキーユーザーハンドルNo | bigserial | (自動採番) | パスキーユーザーハンドルのPK（自動採番） | passkey_user_handle |
| 69 | photo_account_no | 写真アカウント番号 | bigint | - | 閲覧対象ギャラリー（写真）の所有者アカウント番号。common.account(account_no)へのFK（photo_view_log・photo_moderation_logではphoto_noとの組でphoto_mst(account_no, photo_no)へのFK） | photo_list_filter_log, photo_view_log, photo_moderation_log |
| 70 | photo_at | 撮影日時 | timestamp with time zone | - | 写真を撮影した日時。ギャラリー一覧の既定ソート用に(account_no, photo_at DESC)の部分インデックス（is_deleted = false）あり | photo_mst |
| 71 | photo_english_title | 写真タイトル（英語） | varchar(100) | '""' | 写真の英語タイトル（任意入力） | photo_mst |
| 72 | photo_japanese_title | 写真タイトル（日本語） | varchar(100) | - | 写真の日本語タイトル（必須入力） | photo_mst |
| 73 | photo_list_filter_log_no | 写真一覧絞り込みログNo | bigserial | (自動採番) | 写真一覧絞り込みログのPK（自動採番） | photo_list_filter_log |
| 74 | photo_moderation_log_no | 写真モデレーションログNo | bigserial | (自動採番) | 写真モデレーションログのPK（自動採番） | photo_moderation_log |
| 75 | photo_no | 写真番号 | bigint | - | アカウント単位の写真連番。account_noとの複合UNIQUEを構成。portfolio_photoではポートフォリオに設定した写真（photo_mst(account_no, photo_no)へのFK）。写真削除時の参照用に(account_no, photo_no)のインデックスあり | photo_mst, photo_tag_mst, photo_view_log, photo_moderation_log, portfolio_photo |
| 76 | photo_settings | 写真設定 | jsonb | '{}' | テンプレート固有の写真単位の設定（JSON）。feature: theme/subTheme/description、grid・frame: なし（空） | portfolio_photo |
| 77 | photo_view_log_no | 写真閲覧ログNo | bigserial | (自動採番) | 写真詳細閲覧ログのPK（自動採番） | photo_view_log |
| 78 | portfolio_no | ポートフォリオ番号 | bigint | - | アカウント単位のポートフォリオ連番（登録済みの最大値+1で採番。ポートフォリオは物理削除するため、最大の番号を削除した後は同じ番号が再び使われる）。portfolio_mstではaccount_noとの複合UNIQUEを構成し、portfolio_photo・portfolio_tag_mstでは(account_no, portfolio_no)でportfolio_mstへのFK | portfolio_mst, portfolio_photo, portfolio_tag_mst |
| 79 | purpose_kbn | 用途区分 | common.webauthn_challenge_purpose_enum | - | registration（パスキー追加登録）/authentication（パスキー認証）/signup（アカウント新規登録）/reauthentication（重要操作前の本人確認）/recovery（リカバリーコードによる復旧） | webauthn_challenge |
| 80 | reason | 理由 | varchar(500) | - | 管理者が写真を非公開・解除した理由。空文字不可。管理者のみが参照し、写真所有者には表示しない | photo_moderation_log |
| 81 | recovery_code_no | リカバリーコードNo | bigserial | (自動採番) | リカバリーコードのPK（自動採番） | recovery_code |
| 82 | referer | リファラ | varchar(2048) | '' | 遷移元URL（Refererヘッダー）。取得できない場合は空文字 | photo_list_filter_log, photo_view_log |
| 83 | region | 地域 | varchar(100) | '' | IPアドレスから解決した都道府県等のサブディビジョン名。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 84 | reply_no | 返信番号 | bigint | - | お問い合わせ単位の返信連番。inquiry_idとの複合UNIQUEを構成 | inquiry_reply_mst |
| 85 | resident_prefecture_kbn_code | 居住地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 86 | settings_version | 設定バージョン | int | 1 | template_settings・photo_settingsのJSON構造のバージョン | portfolio_mst |
| 87 | sex_kbn | 性別区分 | common.sex_enum | 'none' | man（男性）/woman（女性）/none（未設定） | account |
| 88 | shutter_speed | シャッタースピード | decimal(10,5) | - | EXIF情報から取得したシャッタースピード（秒単位） | photo_mst |
| 89 | sign_count | 署名カウンタ | bigint | 0 | 直近の認証で認証器が返した値。巻き戻りはクローンの疑いとして認証を拒否する（同期パスキーは常に0） | passkey_credential |
| 90 | sort_by | 並び順 | photo.sort_photo_enum | - | photo_at（撮影日順）/favorite（お気に入り数順）/season（季節順） | photo_list_filter_log |
| 91 | sort_order | 表示順 | int | - | 画面表示時のソート順序 | kbn_mst |
| 92 | status_kbn | ステータス区分 / 公開状態区分 | common.inquiry_status_enum / photo.portfolio_status_enum | 'unreplied' / 'draft' | inquiry_mstではunreplied（未対応）/replied（回答済み）/withdrawn（取り下げ）。portfolio_mstではdraft（下書き）/published（公開）。公開ページは公開のポートフォリオのみ表示し、写真削除時は公開から下書きへ自動で戻す。(account_no, status_kbn)のインデックスあり | inquiry_mst, portfolio_mst |
| 93 | subject | 件名 | varchar(100) | - | お問い合わせの件名。空文字不可 | inquiry_mst |
| 94 | tag_english_name | タグ名（英語） | varchar(20) | '""' | 写真に付与するタグの英語名（任意入力） | photo_tag_mst, portfolio_tag_mst |
| 95 | tag_japanese_name | タグ名（日本語） | varchar(20) | - | 写真に付与するタグの日本語名（必須入力） | photo_tag_mst, portfolio_tag_mst |
| 96 | tag_list | タグリスト | varchar(500) | '' | 写真一覧の検索条件として指定されたタグの生文字列 | photo_list_filter_log |
| 97 | tag_no | タグ番号 | bigint | - | 写真単位のタグ連番。account_no, photo_noとの複合UNIQUEを構成。portfolio_tag_mstではポートフォリオ単位のタグ連番で、account_no, portfolio_noとの複合UNIQUEを構成 | photo_tag_mst, portfolio_tag_mst |
| 98 | template_kbn | テンプレート区分 | photo.portfolio_template_enum | - | feature（フィーチャー型）/grid（グリッド型）/frame（額縁型） | portfolio_mst |
| 99 | template_name | テンプレート名 | varchar(50) | - | 額縁テンプレートの名前。空文字不可 | frame_template_mst |
| 100 | template_settings | テンプレート設定 | jsonb | '{}' | テンプレート固有のポートフォリオ単位の設定（JSON）。feature: なし、grid: rows（各行のpatternにTWO_EQUAL（2枚均等）/THREE_EQUAL（3枚均等）/WIDE_NARROW（横長1枚+縦長1枚。縦長が右）/NARROW_WIDE（縦長1枚+横長1枚。縦長が左））、frame: frameTemplateNo（コピー元の額縁テンプレート番号）/frame（額縁定義のコピー。frame_template_mst.frame_definitionと同じ構造）/messages（メッセージ枠ごとの文面） | portfolio_mst |
| 101 | theme | テーマ | varchar(100) | - | ポートフォリオのテーマ。空文字不可 | portfolio_mst |
| 102 | token_hash | トークンハッシュ | varchar(256) | - | リフレッシュトークンをSHA-256でハッシュ化した値。検索用インデックスあり | refresh_token |
| 103 | token_id | トークンID | bigserial | (自動採番) | リフレッシュトークンのPK（自動採番） | refresh_token |
| 104 | transports | 認証器の通信手段 | varchar(200) | '' | internal/hybrid/usb等をカンマ区切りで保持する。未取得の場合は空文字 | passkey_credential |
| 105 | updated_at | 更新日時 | timestamp with time zone | - / NOW() | レコード最終更新日時（タイムゾーン付き） | account, account_authority, location_mst, refresh_token, photo_mst, inquiry_mst, portfolio_mst, passkey_credential, frame_template_mst |
| 106 | updated_by | 更新者 | bigint | - | レコードを最後に更新したアカウント番号。refresh_tokenでは自身のアカウント番号（本人のトークンのみ無効化操作が発生するため） | account, account_authority, location_mst, refresh_token, photo_mst, inquiry_mst, portfolio_mst, passkey_credential, frame_template_mst |
| 107 | user_handle | ユーザーハンドル | varchar(100) | - | WebAuthnのuser.id。サーバ生成の乱数（base64url）。passkey_user_handleではUNIQUE。アカウントIDのような変更されうる値や個人情報を含めない。webauthn_challengeでは新規登録用チャレンジのみ、登録オプションで払い出した値を保持し、それ以外は空文字（既定値''） | passkey_user_handle, webauthn_challenge |

## カスタム型辞書

| No | 型名 | 定義値 | 説明 | 使用カラム |
|----|------|--------|------|-----------|
| 1 | common.sex_enum | man, woman, none | 性別区分 | account.sex_kbn |
| 2 | common.authority_enum | mini-user, normal-user, special-user, administrator | 権限区分 | account_authority.authority_kbn |
| 3 | common.inquiry_status_enum | unreplied, replied, withdrawn | お問い合わせステータス区分 | inquiry_mst.status_kbn |
| 4 | common.login_method_enum | passkey, recovery_code | ログイン方式区分 | login_history.login_method_kbn |
| 5 | common.webauthn_challenge_purpose_enum | registration, authentication, signup, reauthentication, recovery | WebAuthnチャレンジの用途区分 | webauthn_challenge.purpose_kbn |
| 6 | photo.direction_enum | vertical, horizontal, square, none | 写真の向き | photo_mst.direction_kbn, photo_list_filter_log.direction_kbn |
| 7 | photo.sort_photo_enum | photo_at, favorite, season | 写真一覧の並び順 | photo_list_filter_log.sort_by |
| 8 | photo.portfolio_template_enum | feature, grid, frame | ポートフォリオのテンプレート | portfolio_mst.template_kbn |
| 9 | photo.portfolio_status_enum | draft, published | ポートフォリオの公開状態 | portfolio_mst.status_kbn |
