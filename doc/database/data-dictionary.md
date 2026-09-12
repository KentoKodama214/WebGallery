# データ辞書

全テーブルのカラムを物理名・論理名・データ型で重複排除し、物理名の昇順で一覧化したものです。

## カラム辞書

| No | 物理名 | 論理名 | データ型 | デフォルト値 | コメント | 使用テーブル |
|----|--------|--------|----------|-------------|----------|-------------|
| 1 | account_id | アカウントID | varchar(20) | - | 8〜20文字の英数字。ログイン時に使用する一意の識別子 | account |
| 2 | account_name | アカウント名 | varchar(50) | - | ユーザーの表示名 | account |
| 3 | account_no | アカウント番号 | bigserial / bigint | (自動採番) / - | アカウントを一意に特定するための番号。accountテーブルではPK（自動採番）、他テーブルではFK | account, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history |
| 4 | address | 住所 | text | '' | 撮影場所の住所 | location_mst |
| 5 | authority_kbn | 権限区分 | common.authority_enum | - | mini-user/normal-user/special-user/administratorの4段階。写真アップロード上限に影響 | account |
| 6 | birthdate | 生年月日 | date | '1900-01-01' | 個人情報管理の観点で、必須入力なし、かつ年月まで。データ登録時にすべて1日に変換する | account |
| 7 | birthplace_prefecture_kbn_code | 出身地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 8 | caption | キャプション | text | '""' | 写真の説明文 | photo_mst |
| 9 | country | 国 | varchar(2) | '' | IPアドレスから解決したISO 3166-1 alpha-2コード。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 10 | created_at | 作成日時 | timestamp with time zone | - / NOW() | レコード作成日時（タイムゾーン付き） | account, kbn_mst, location_mst, refresh_token, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log |
| 11 | created_by | 作成者 | bigint（kbn_mstのみint） | - | レコードを作成したアカウント番号。システム側が作成した場合は'0'を入れる | account, kbn_mst, location_mst, photo_mst, photo_tag_mst, photo_favorite, login_history, photo_list_filter_log, photo_view_log |
| 12 | direction_kbn | 写真の向き | photo.direction_enum | 'none' | vertical（縦）/horizontal（横）/square（正方形）/none（未設定） | photo_mst, photo_list_filter_log |
| 13 | explanation | 説明 | text | '""' | 区分コードの補足説明 | kbn_mst |
| 14 | expires_at | 有効期限 | timestamp with time zone | - | リフレッシュトークンの有効期限。期限切れトークン削除の定期実行タスク用インデックスあり | refresh_token |
| 15 | f_value | F値 | decimal(5,2) | - | EXIF情報から取得した絞り値 | photo_mst |
| 16 | favorite_photo_account_no | 写真所有者のアカウント番号 | bigint | - | お気に入り対象の写真を所有するアカウント番号。photo_mst(account_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 17 | favorite_photo_no | お気に入り写真番号 | bigint | - | お気に入り対象の写真番号。photo_mst(photo_no)へのFK。(favorite_photo_account_no, favorite_photo_no)の複合インデックスあり | photo_favorite |
| 18 | focal_length | 焦点距離 | int | - | EXIF情報から取得した焦点距離（mm単位） | photo_mst |
| 19 | free_memo | フリーメモ | text | '""' | ユーザーが自由に入力できるメモ欄 | account |
| 20 | id | ID | bigserial | (自動採番) | サロゲートキー（自動採番） | location_mst, photo_mst, photo_tag_mst, photo_favorite |
| 21 | image_file_name | 画像ファイル名 | text | - | アップロード時のクライアント送信ファイル名（ベース名）。表示用・写真登録時の重複存在チェックの等価検索に使用。オブジェクトキー（image_file_path）とは独立 | photo_mst |
| 22 | image_file_path | 画像ファイルパス | text | - | S3オブジェクトキー（サーバ生成の不透明値 `{accountId}/{写真番号}-{ランダム}.{拡張子}`）。閲覧時はこのキーから署名付きURLを発行する | photo_mst |
| 23 | ip_address | 送信元IPアドレス | varchar(45) | - | アクセス元のIPアドレス（IPv6を考慮した最大長） | login_history, photo_list_filter_log, photo_view_log |
| 24 | is_admin_locked | 管理者ロックフラグ | boolean | false | 管理者が強制ロックした場合にtrue。ログイン失敗回数による自動ロック解除の対象外 | account |
| 25 | is_deleted | 削除フラグ | boolean | false | 論理削除フラグ。trueの場合は削除済み | account, location_mst, photo_mst |
| 26 | is_favorite | お気に入り写真のみ絞り込みフラグ | boolean | - | 写真一覧の検索条件として「お気に入りのみ」が指定されたか | photo_list_filter_log |
| 27 | is_location_public | 位置情報公開フラグ | boolean | false | trueなら撮影場所（緯度経度・住所・ロケーション名）を写真所有者以外にも公開する。falseの場合、写真詳細APIは所有者以外へこれらを返さない | photo_mst |
| 28 | is_revoked | 無効化フラグ | boolean | false | リフレッシュトークンの無効化フラグ。ログアウト時やトークンローテーション時にtrueに設定 | refresh_token |
| 29 | is_success | ログイン成功フラグ | boolean | - | ログイン試行の成否 | login_history |
| 30 | iso | ISO感度 | int | - | EXIF情報から取得したISO感度 | photo_mst |
| 31 | kbn_class_code | 区分分類コード | varchar(20) | - | 区分の大分類コード（例: prefecture） | kbn_mst |
| 32 | kbn_class_english_name | 区分分類英語名 | varchar(20) | '""' | 区分分類の英語表記 | kbn_mst |
| 33 | kbn_class_japanese_name | 区分分類日本語名（空文字不可） | varchar(20) | - | 区分分類の日本語表記。kbn_japanese_nameとの複合UNIQUEを構成 | kbn_mst |
| 34 | kbn_code | 区分コード | varchar(20) | - | 区分の個別コード。kbn_class_codeとの複合PKを構成 | kbn_mst |
| 35 | kbn_english_name | 区分英語名 | varchar(20) | '""' | 区分の英語表記 | kbn_mst |
| 36 | kbn_group_code | 区分グループコード | varchar(20) | '""' | 区分のグループ分類コード（例: 地域コード） | kbn_mst |
| 37 | kbn_group_english_name | 区分グループ英語名 | varchar(20) | '""' | 区分グループの英語表記 | kbn_mst |
| 38 | kbn_group_japanese_name | 区分グループ日本語名 | varchar(20) | '""' | 区分グループの日本語表記 | kbn_mst |
| 39 | kbn_japanese_name | 区分日本語名 | varchar(20) | - | 区分の日本語表記。kbn_class_japanese_nameとの複合UNIQUEを構成（空文字不可） | kbn_mst |
| 40 | last_login_datetime | 最終ログイン日時 | timestamp with time zone | - | ユーザーが最後にログインした日時 | account |
| 41 | latitude | 緯度 | decimal(11,4) | - | 撮影場所の緯度座標 | location_mst |
| 42 | location_name | ロケーション名 | text | - | 撮影場所の名称。同一アカウント内で一意 | location_mst |
| 43 | location_no | ロケーション番号 | bigint | - | アカウント単位のロケーション連番 | location_mst, photo_mst |
| 44 | login_failure_count | ログイン失敗回数 | smallint | 0 | 連続ログイン失敗回数。ログイン成功時にリセット | account |
| 45 | login_history_no | ログイン履歴No | bigserial | (自動採番) | ログイン履歴のPK（自動採番） | login_history |
| 46 | longitude | 経度 | decimal(11,4) | - | 撮影場所の経度座標 | location_mst |
| 47 | password | パスワード | text | - | BCryptでハッシュ化されたパスワード | account |
| 48 | photo_account_no | 写真アカウント番号 | bigint | - | 閲覧対象ギャラリー（写真）の所有者アカウント番号。common.account(account_no)へのFK | photo_list_filter_log, photo_view_log |
| 49 | photo_at | 撮影日時 | timestamp with time zone | - | 写真を撮影した日時。ギャラリー一覧の既定ソート用に(account_no, photo_at DESC)の部分インデックス（is_deleted = false）あり | photo_mst |
| 50 | photo_english_title | 写真タイトル（英語） | varchar(100) | '""' | 写真の英語タイトル（任意入力） | photo_mst |
| 51 | photo_japanese_title | 写真タイトル（日本語） | varchar(100) | - | 写真の日本語タイトル（必須入力） | photo_mst |
| 52 | photo_list_filter_log_no | 写真一覧絞り込みログNo | bigserial | (自動採番) | 写真一覧絞り込みログのPK（自動採番） | photo_list_filter_log |
| 53 | photo_no | 写真番号 | bigint | - | アカウント単位の写真連番。account_noとの複合UNIQUEを構成 | photo_mst, photo_tag_mst, photo_view_log |
| 54 | photo_view_log_no | 写真閲覧ログNo | bigserial | (自動採番) | 写真詳細閲覧ログのPK（自動採番） | photo_view_log |
| 55 | referer | リファラ | varchar(2048) | '' | 遷移元URL（Refererヘッダー）。取得できない場合は空文字 | photo_list_filter_log, photo_view_log |
| 56 | region | 地域 | varchar(100) | '' | IPアドレスから解決した都道府県等のサブディビジョン名。未解決時は空文字 | login_history, photo_list_filter_log, photo_view_log |
| 57 | resident_prefecture_kbn_code | 居住地都道府県区分コード | varchar(20) | 'none' | kbn_mstの都道府県区分コードを参照。未設定時は'none' | account |
| 58 | sex_kbn | 性別区分 | common.sex_enum | 'none' | man（男性）/woman（女性）/none（未設定） | account |
| 59 | shutter_speed | シャッタースピード | decimal(10,5) | - | EXIF情報から取得したシャッタースピード（秒単位） | photo_mst |
| 60 | sort_by | 並び順 | photo.sort_photo_enum | - | photo_at（撮影日順）/favorite（お気に入り数順）/season（季節順） | photo_list_filter_log |
| 61 | sort_order | 表示順 | int | - | 画面表示時のソート順序 | kbn_mst |
| 62 | tag_english_name | タグ名（英語） | varchar(20) | '""' | 写真に付与するタグの英語名（任意入力） | photo_tag_mst |
| 63 | tag_japanese_name | タグ名（日本語） | varchar(20) | - | 写真に付与するタグの日本語名（必須入力） | photo_tag_mst |
| 64 | tag_list | タグリスト | varchar(500) | '' | 写真一覧の検索条件として指定されたタグの生文字列 | photo_list_filter_log |
| 65 | tag_no | タグ番号 | bigint | - | 写真単位のタグ連番。account_no, photo_noとの複合UNIQUEを構成 | photo_tag_mst |
| 66 | token_hash | トークンハッシュ | varchar(256) | - | リフレッシュトークンをSHA-256でハッシュ化した値。検索用インデックスあり | refresh_token |
| 67 | token_id | トークンID | bigserial | (自動採番) | リフレッシュトークンのPK（自動採番） | refresh_token |
| 68 | updated_at | 更新日時 | timestamp with time zone | - / NOW() | レコード最終更新日時（タイムゾーン付き） | account, location_mst, refresh_token, photo_mst |
| 69 | updated_by | 更新者 | bigint | - | レコードを最後に更新したアカウント番号。refresh_tokenでは自身のアカウント番号（本人のトークンのみ無効化操作が発生するため） | account, location_mst, refresh_token, photo_mst |

## カスタム型辞書

| No | 型名 | 定義値 | 説明 | 使用カラム |
|----|------|--------|------|-----------|
| 2 | common.sex_enum | man, woman, none | 性別区分 | account.sex_kbn |
| 3 | common.authority_enum | mini-user, normal-user, special-user, administrator | 権限区分 | account.authority_kbn |
| 4 | photo.direction_enum | vertical, horizontal, square, none | 写真の向き | photo_mst.direction_kbn, photo_list_filter_log.direction_kbn |
| 5 | photo.sort_photo_enum | photo_at, favorite, season | 写真一覧の並び順 | photo_list_filter_log.sort_by |
