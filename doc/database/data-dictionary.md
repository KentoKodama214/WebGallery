# データ辞書

全テーブルのカラムを物理名・論理名・データ型で重複排除し、物理名の昇順で一覧化したものです。

## カラム辞書

| No | 物理名 | 論理名 | データ型 | デフォルト値 | コメント | 使用テーブル |
|----|--------|--------|----------|-------------|----------|-------------|
| 1 | account_id | アカウントID | varchar(20) | - | 8〜20文字の英数字。ログイン時に使用する一意の識別子 | account |
| 2 | account_name | アカウント名 | varchar(50) | - | ユーザーの表示名 | account |
| 3 | account_no | アカウント番号 | bigserial / bigint | (自動採番) / - | アカウントを一意に特定するための番号。accountテーブルではPK（自動採番）、他テーブルでは概ねFK。account_authorityではPK兼FK（accountと1対1）。ただしphoto_list_filter_log・photo_view_logのaccount_noは閲覧者のアカウント番号を表し、未ログインの場合は0（センチネル値のためFKなし） | account, account_authority, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, inquiry_mst |
| 4 | address | 住所 | text | '' | 撮影場所の住所 | location_mst |
| 5 | admin_account_no | 管理者アカウント番号 | bigint | - | 返信した管理者のアカウント番号。common.account(account_no)へのFK | inquiry_reply_mst |
| 6 | authority_kbn | 権限区分 | common.authority_enum | - | mini-user/normal-user/special-user/administratorの4段階。写真アップロード上限に影響 | account_authority |
| 7 | birthdate | 生年月日 | date | '1900-01-01' | 個人情報管理の観点で、必須入力なし、かつ年月まで。データ登録時にすべて1日に変換する | account |
| 8 | birthplace_prefecture_kbn_code | 出身地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 9 | body | 本文 | text | - | お問い合わせの本文、または返信の本文。空文字不可 | inquiry_mst, inquiry_reply_mst |
| 10 | caption | キャプション | text | '""' | 写真の説明文 | photo_mst |
| 11 | country | 国 | varchar(2) | '' | IPアドレスから解決したISO 3166-1 alpha-2コード。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 12 | created_at | 作成日時 | timestamp with time zone | - / NOW() | レコード作成日時（タイムゾーン付き） | account, account_authority, kbn_mst, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, inquiry_mst, inquiry_reply_mst |
| 13 | created_by | 作成者 | bigint（kbn_mstのみint） | - | レコードを作成したアカウント番号。システム側が作成した場合は'0'を入れる | account, account_authority, kbn_mst, location_mst, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log, inquiry_mst, inquiry_reply_mst |
| 14 | direction_kbn | 写真の向き | photo.direction_enum | 'none' | vertical（縦）/horizontal（横）/square（正方形）/none（未設定） | photo_mst, photo_list_filter_log |
| 15 | explanation | 説明 | text | '""' | 区分コードの補足説明 | kbn_mst |
| 16 | expires_at | 有効期限 | timestamp with time zone | - | リフレッシュトークンの有効期限。期限切れトークン削除の定期実行タスク用インデックスあり | refresh_token |
| 17 | f_value | F値 | decimal(5,2) | - | EXIF情報から取得した絞り値 | photo_mst |
| 18 | favorite_photo_account_no | 写真所有者のアカウント番号 | bigint | - | お気に入り対象の写真を所有するアカウント番号。photo_mst(account_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 19 | favorite_photo_no | お気に入り写真番号 | bigint | - | お気に入り対象の写真番号。photo_mst(photo_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 20 | focal_length | 焦点距離 | int | - | EXIF情報から取得した焦点距離（mm単位） | photo_mst |
| 21 | free_memo | フリーメモ | text | '""' | ユーザーが自由に入力できるメモ欄 | account |
| 22 | id | ID | bigserial | (自動採番) | サロゲートキー（自動採番） | location_mst, photo_mst, photo_tag_mst, photo_favorite, inquiry_mst, inquiry_reply_mst |
| 23 | image_file_name | 画像ファイル名 | text | - | アップロード時のクライアント送信ファイル名（ベース名）。表示用・写真登録時の重複存在チェックの等価検索に使用。オブジェクトキー（image_file_path）とは独立 | photo_mst |
| 24 | image_file_path | 画像ファイルパス | text | - | S3オブジェクトキー（サーバ生成の不透明値 `{accountId}/{写真番号}-{ランダム}.{拡張子}`）。閲覧時はこのキーから署名付きURLを発行する | photo_mst |
| 25 | inquiry_id | お問い合わせID | bigint | - | inquiry_mst(id)へのFK | inquiry_reply_mst |
| 26 | inquiry_no | お問い合わせ番号 | bigint | - | アカウント単位のお問い合わせ連番。account_noとの複合UNIQUEを構成 | inquiry_mst |
| 27 | ip_address | 送信元IPアドレス | varchar(45) | - | アクセス元のIPアドレス（IPv6を考慮した最大長） | login_history, photo_list_filter_log, photo_view_log |
| 28 | is_admin_locked | 管理者ロックフラグ | boolean | false | 管理者が強制ロックした場合にtrue。ログイン失敗回数による自動ロック解除の対象外 | account |
| 29 | is_deleted | 削除フラグ | boolean | false | 論理削除フラグ。trueの場合は削除済み | account, location_mst, photo_mst |
| 30 | is_favorite | お気に入り写真のみ絞り込みフラグ | boolean | - | 写真一覧の検索条件として「お気に入りのみ」が指定されたか | photo_list_filter_log |
| 31 | is_location_public | 位置情報公開フラグ | boolean | false | trueなら撮影場所（緯度経度・住所・ロケーション名）を写真所有者以外にも公開する。falseの場合、写真詳細APIは所有者以外へこれらを返さない | photo_mst |
| 32 | is_read_by_user | ユーザー既読フラグ | boolean | true | 管理者の返信をユーザーが確認済みかどうか。返信投稿時にfalseへ更新される | inquiry_mst |
| 33 | is_revoked | 無効化フラグ | boolean | false | リフレッシュトークンの無効化フラグ。ログアウト時やトークンローテーション時にtrueに設定 | refresh_token |
| 34 | iso | ISO感度 | int | - | EXIF情報から取得したISO感度 | photo_mst |
| 35 | kbn_class_code | 区分分類コード | varchar(20) | - | 区分の大分類コード（例: prefecture） | kbn_mst |
| 36 | kbn_class_english_name | 区分分類英語名 | varchar(20) | '""' | 区分分類の英語表記 | kbn_mst |
| 37 | kbn_class_japanese_name | 区分分類日本語名（空文字不可） | varchar(20) | - | 区分分類の日本語表記。kbn_japanese_nameとの複合UNIQUEを構成 | kbn_mst |
| 38 | kbn_code | 区分コード | varchar(20) | - | 区分の個別コード。kbn_class_codeとの複合PKを構成 | kbn_mst |
| 39 | kbn_english_name | 区分英語名 | varchar(20) | '""' | 区分の英語表記 | kbn_mst |
| 40 | kbn_group_code | 区分グループコード | varchar(20) | '""' | 区分のグループ分類コード（例: 地域コード） | kbn_mst |
| 41 | kbn_group_english_name | 区分グループ英語名 | varchar(20) | '""' | 区分グループの英語表記 | kbn_mst |
| 42 | kbn_group_japanese_name | 区分グループ日本語名 | varchar(20) | '""' | 区分グループの日本語表記 | kbn_mst |
| 43 | kbn_japanese_name | 区分日本語名 | varchar(20) | - | 区分の日本語表記。kbn_class_japanese_nameとの複合UNIQUEを構成（空文字不可） | kbn_mst |
| 44 | last_login_datetime | 最終ログイン日時 | timestamp with time zone | - | ユーザーが最後にログインした日時 | account |
| 45 | latitude | 緯度 | decimal(11,4) | - | 撮影場所の緯度座標 | location_mst |
| 46 | location_name | ロケーション名 | text | - | 撮影場所の名称。同一アカウント内で一意 | location_mst |
| 47 | location_no | ロケーション番号 | bigint | - | アカウント単位のロケーション連番 | location_mst, photo_mst |
| 48 | login_failure_count | ログイン失敗回数 | smallint | 0 | 連続ログイン失敗回数。ログイン成功時にリセット | account |
| 49 | login_history_no | ログイン履歴No | bigserial | (自動採番) | ログイン履歴のPK（自動採番） | login_history |
| 50 | longitude | 経度 | decimal(11,4) | - | 撮影場所の経度座標 | location_mst |
| 51 | password | パスワード | text | - | BCryptでハッシュ化されたパスワード | account |
| 52 | photo_account_no | 写真アカウント番号 | bigint | - | 閲覧対象ギャラリー（写真）の所有者アカウント番号。common.account(account_no)へのFK | photo_list_filter_log, photo_view_log |
| 53 | photo_at | 撮影日時 | timestamp with time zone | - | 写真を撮影した日時。ギャラリー一覧の既定ソート用に(account_no, photo_at DESC)の部分インデックス（is_deleted = false）あり | photo_mst |
| 54 | photo_english_title | 写真タイトル（英語） | varchar(100) | '""' | 写真の英語タイトル（任意入力） | photo_mst |
| 55 | photo_japanese_title | 写真タイトル（日本語） | varchar(100) | - | 写真の日本語タイトル（必須入力） | photo_mst |
| 56 | photo_list_filter_log_no | 写真一覧絞り込みログNo | bigserial | (自動採番) | 写真一覧絞り込みログのPK（自動採番） | photo_list_filter_log |
| 57 | photo_no | 写真番号 | bigint | - | アカウント単位の写真連番。account_noとの複合UNIQUEを構成 | photo_mst, photo_tag_mst, photo_view_log |
| 58 | photo_view_log_no | 写真閲覧ログNo | bigserial | (自動採番) | 写真詳細閲覧ログのPK（自動採番） | photo_view_log |
| 59 | referer | リファラ | varchar(2048) | '' | 遷移元URL（Refererヘッダー）。取得できない場合は空文字 | photo_list_filter_log, photo_view_log |
| 60 | region | 地域 | varchar(100) | '' | IPアドレスから解決した都道府県等のサブディビジョン名。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 61 | reply_no | 返信番号 | bigint | - | お問い合わせ単位の返信連番。inquiry_idとの複合UNIQUEを構成 | inquiry_reply_mst |
| 62 | resident_prefecture_kbn_code | 居住地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 63 | sex_kbn | 性別区分 | common.sex_enum | 'none' | man（男性）/woman（女性）/none（未設定） | account |
| 64 | shutter_speed | シャッタースピード | decimal(10,5) | - | EXIF情報から取得したシャッタースピード（秒単位） | photo_mst |
| 65 | sort_by | 並び順 | photo.sort_photo_enum | - | photo_at（撮影日順）/favorite（お気に入り数順）/season（季節順） | photo_list_filter_log |
| 66 | sort_order | 表示順 | int | - | 画面表示時のソート順序 | kbn_mst |
| 67 | status_kbn | ステータス区分 | common.inquiry_status_enum | 'unreplied' | unreplied（未対応）/replied（回答済み）/withdrawn（取り下げ） | inquiry_mst |
| 68 | subject | 件名 | varchar(100) | - | お問い合わせの件名。空文字不可 | inquiry_mst |
| 69 | tag_english_name | タグ名（英語） | varchar(20) | '""' | 写真に付与するタグの英語名（任意入力） | photo_tag_mst |
| 70 | tag_japanese_name | タグ名（日本語） | varchar(20) | - | 写真に付与するタグの日本語名（必須入力） | photo_tag_mst |
| 71 | tag_list | タグリスト | varchar(500) | '' | 写真一覧の検索条件として指定されたタグの生文字列 | photo_list_filter_log |
| 72 | tag_no | タグ番号 | bigint | - | 写真単位のタグ連番。account_no, photo_noとの複合UNIQUEを構成 | photo_tag_mst |
| 73 | token_hash | トークンハッシュ | varchar(256) | - | リフレッシュトークンをSHA-256でハッシュ化した値。検索用インデックスあり | refresh_token |
| 74 | token_id | トークンID | bigserial | (自動採番) | リフレッシュトークンのPK（自動採番） | refresh_token |
| 75 | updated_at | 更新日時 | timestamp with time zone | - / NOW() | レコード最終更新日時（タイムゾーン付き） | account, account_authority, location_mst, refresh_token, photo_mst, inquiry_mst |
| 76 | updated_by | 更新者 | bigint | - | レコードを最後に更新したアカウント番号。refresh_tokenでは自身のアカウント番号（本人のトークンのみ無効化操作が発生するため） | account, account_authority, location_mst, refresh_token, photo_mst, inquiry_mst |

## カスタム型辞書

| No | 型名 | 定義値 | 説明 | 使用カラム |
|----|------|--------|------|-----------|
| 1 | common.sex_enum | man, woman, none | 性別区分 | account.sex_kbn |
| 2 | common.authority_enum | mini-user, normal-user, special-user, administrator | 権限区分 | account_authority.authority_kbn |
| 3 | common.inquiry_status_enum | unreplied, replied, withdrawn | お問い合わせステータス区分 | inquiry_mst.status_kbn |
| 4 | photo.direction_enum | vertical, horizontal, square, none | 写真の向き | photo_mst.direction_kbn, photo_list_filter_log.direction_kbn |
| 5 | photo.sort_photo_enum | photo_at, favorite, season | 写真一覧の並び順 | photo_list_filter_log.sort_by |
