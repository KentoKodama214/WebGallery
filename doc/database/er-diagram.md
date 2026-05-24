# ER図

## 全体ER図

```mermaid
erDiagram
    %% ========== common スキーマ ==========

    account {
        bigserial account_no PK "アカウント番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        varchar account_id UK "アカウントID"
        varchar account_name "アカウント名"
        text password "パスワード"
        date birthdate "生年月日"
        sex_enum sex_kbn "性別区分"
        varchar birthplace_prefecture_kbn_code "出身地都道府県"
        varchar resident_prefecture_kbn_code "居住地都道府県"
        text free_memo "フリーメモ"
        authority_enum authority_kbn "権限区分"
        timestamptz last_login_datetime "最終ログイン日時"
        smallint login_failure_count "ログイン失敗回数"
    }

    kbn_mst {
        varchar kbn_class_code PK "区分クラスコード"
        varchar kbn_code PK "区分コード"
        int created_by "作成者"
        timestamptz created_at "作成日時"
        int sort_order "表示順"
        varchar kbn_group_code "区分グループコード"
        varchar kbn_class_japanese_name UK "区分クラス日本語名"
        varchar kbn_group_japanese_name "区分グループ日本語名"
        varchar kbn_japanese_name UK "区分日本語名"
        varchar kbn_class_english_name "区分クラス英語名"
        varchar kbn_group_english_name "区分グループ英語名"
        varchar kbn_english_name "区分英語名"
        text explanation "説明"
    }

    location_mst {
        bigserial id PK "ロケーションID"
        bigint account_no FK,UK "アカウント番号"
        bigint location_no UK "ロケーション番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        text location_name UK "ロケーション名"
        text address "住所"
        decimal latitude "緯度"
        decimal longitude "経度"
    }

    refresh_token {
        bigserial token_id PK "トークンID"
        bigint account_no FK "アカウント番号"
        varchar token_hash "トークンハッシュ"
        timestamptz expires_at "有効期限"
        timestamptz created_at "作成日時"
        boolean is_revoked "無効化フラグ"
    }

    %% ========== photo スキーマ ==========

    photo_mst {
        bigserial id PK "写真ID"
        bigint account_no FK,UK "アカウント番号"
        bigint photo_no UK "写真番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        timestamptz photo_at "撮影日時"
        bigint location_no "ロケーション番号"
        text image_file_path "画像ファイルパス"
        varchar photo_japanese_title "写真タイトル(日本語)"
        varchar photo_english_title "写真タイトル(英語)"
        text caption "キャプション"
        direction_enum direction_kbn "写真の向き"
        int focal_length "焦点距離"
        decimal f_value "F値"
        decimal shutter_speed "シャッタースピード"
        int iso "ISO感度"
    }

    photo_tag_mst {
        bigserial id PK "タグID"
        bigint account_no FK,UK "アカウント番号"
        bigint photo_no FK,UK "写真番号"
        bigint tag_no UK "タグ番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        varchar tag_japanese_name "タグ名(日本語)"
        varchar tag_english_name "タグ名(英語)"
    }

    photo_favorite {
        bigserial id PK "お気に入りID"
        bigint account_no FK,UK "アカウント番号"
        bigint favorite_photo_account_no FK,UK "写真所有者アカウント番号"
        bigint favorite_photo_no FK,UK "写真番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
    }

    %% ========== リレーションシップ ==========

    account ||--o{ location_mst : "所有する"
    account ||--o{ refresh_token : "トークンを持つ"
    account ||--o{ photo_mst : "投稿する"
    account ||--o{ photo_favorite : "お気に入りする"
    photo_mst ||--o{ photo_tag_mst : "タグ付けされる"
    photo_mst ||--o{ photo_favorite : "お気に入りされる"
```

## スキーマ別ER図

### common スキーマ

```mermaid
erDiagram
    account {
        bigserial account_no PK "アカウント番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        varchar account_id UK "アカウントID"
        varchar account_name "アカウント名"
        text password "パスワード"
        date birthdate "生年月日"
        sex_enum sex_kbn "性別区分"
        varchar birthplace_prefecture_kbn_code "出身地都道府県"
        varchar resident_prefecture_kbn_code "居住地都道府県"
        text free_memo "フリーメモ"
        authority_enum authority_kbn "権限区分"
        timestamptz last_login_datetime "最終ログイン日時"
        smallint login_failure_count "ログイン失敗回数"
    }

    kbn_mst {
        varchar kbn_class_code PK "区分クラスコード"
        varchar kbn_code PK "区分コード"
        int created_by "作成者"
        timestamptz created_at "作成日時"
        int sort_order "表示順"
        varchar kbn_group_code "区分グループコード"
        varchar kbn_class_japanese_name UK "区分クラス日本語名"
        varchar kbn_group_japanese_name "区分グループ日本語名"
        varchar kbn_japanese_name UK "区分日本語名"
        varchar kbn_class_english_name "区分クラス英語名"
        varchar kbn_group_english_name "区分グループ英語名"
        varchar kbn_english_name "区分英語名"
        text explanation "説明"
    }

    location_mst {
        bigserial id PK "ロケーションID"
        bigint account_no FK,UK "アカウント番号"
        bigint location_no UK "ロケーション番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        text location_name UK "ロケーション名"
        text address "住所"
        decimal latitude "緯度"
        decimal longitude "経度"
    }

    refresh_token {
        bigserial token_id PK "トークンID"
        bigint account_no FK "アカウント番号"
        varchar token_hash "トークンハッシュ"
        timestamptz expires_at "有効期限"
        timestamptz created_at "作成日時"
        boolean is_revoked "無効化フラグ"
    }

    account ||--o{ location_mst : "所有する"
    account ||--o{ refresh_token : "トークンを持つ"
```

### photo スキーマ

```mermaid
erDiagram
    account {
        bigserial account_no PK "アカウント番号"
        varchar account_id UK "アカウントID"
        varchar account_name "アカウント名"
    }

    photo_mst {
        bigserial id PK "写真ID"
        bigint account_no FK,UK "アカウント番号"
        bigint photo_no UK "写真番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        bigint updated_by "更新者"
        timestamptz updated_at "更新日時"
        boolean is_deleted "削除フラグ"
        timestamptz photo_at "撮影日時"
        bigint location_no "ロケーション番号"
        text image_file_path "画像ファイルパス"
        varchar photo_japanese_title "写真タイトル(日本語)"
        varchar photo_english_title "写真タイトル(英語)"
        text caption "キャプション"
        direction_enum direction_kbn "写真の向き"
        int focal_length "焦点距離"
        decimal f_value "F値"
        decimal shutter_speed "シャッタースピード"
        int iso "ISO感度"
    }

    photo_tag_mst {
        bigserial id PK "タグID"
        bigint account_no FK,UK "アカウント番号"
        bigint photo_no FK,UK "写真番号"
        bigint tag_no UK "タグ番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
        varchar tag_japanese_name "タグ名(日本語)"
        varchar tag_english_name "タグ名(英語)"
    }

    photo_favorite {
        bigserial id PK "お気に入りID"
        bigint account_no FK,UK "アカウント番号"
        bigint favorite_photo_account_no FK,UK "写真所有者アカウント番号"
        bigint favorite_photo_no FK,UK "写真番号"
        bigint created_by "作成者"
        timestamptz created_at "作成日時"
    }

    account ||--o{ photo_mst : "投稿する"
    account ||--o{ photo_favorite : "お気に入りする"
    photo_mst ||--o{ photo_tag_mst : "タグ付けされる"
    photo_mst ||--o{ photo_favorite : "お気に入りされる"
```

## リレーションシップ一覧

| No | 親テーブル | 子テーブル | 外部キー | 関係 | 説明 |
|----|-----------|-----------|----------|------|------|
| 1 | common.account | common.location_mst | account_no → account_no | 1:N | アカウントが複数のロケーションを所有 |
| 2 | common.account | common.refresh_token | account_no → account_no | 1:N | アカウントが複数のリフレッシュトークンを持つ |
| 3 | common.account | photo.photo_mst | account_no → account_no | 1:N | アカウントが複数の写真を投稿 |
| 4 | common.account | photo.photo_favorite | account_no → account_no | 1:N | アカウントが複数の写真をお気に入り |
| 5 | photo.photo_mst | photo.photo_tag_mst | (account_no, photo_no) → (account_no, photo_no) | 1:N | 写真に複数のタグを付与 |
| 6 | photo.photo_mst | photo.photo_favorite | (account_no, photo_no) → (favorite_photo_account_no, favorite_photo_no) | 1:N | 写真が複数のユーザーにお気に入りされる |
