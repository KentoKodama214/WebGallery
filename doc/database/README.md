# WebGallery データベース定義書

## 概要

WebGalleryはPostgreSQLを使用し、以下の2つのスキーマで構成されています。

| スキーマ | 用途 |
|----------|------|
| `common` | アカウント管理、区分マスタ、ロケーションマスタ |
| `photo`  | 写真メタデータ、タグ、お気に入り |

## ドキュメント一覧

- テーブル定義・ER図: SchemaSpyで自動生成（[commonスキーマ](./common/index.html) / [photoスキーマ](./photo/index.html)）
- [データ辞書](./data-dictionary.md)

SchemaSpyドキュメントの生成方法:

```bash
docker compose up -d
docker compose --profile docs run --rm schemaspy
```

## カスタム型

### common スキーマ

| 型名 | 値 | 説明 |
|------|----|------|
| `common.sex_enum` | `man`, `woman`, `none` | 性別区分 |
| `common.authority_enum` | `mini-user`, `normal-user`, `special-user`, `administrator` | 権限区分 |
| `common.inquiry_status_enum` | `unreplied`, `replied` | お問い合わせステータス区分 |

### photo スキーマ

| 型名 | 値 | 説明 |
|------|----|------|
| `photo.direction_enum` | `vertical`, `horizontal`, `square`, `none` | 写真の向き |
| `photo.sort_photo_enum` | `photo_at`, `favorite`, `season` | 写真一覧の並び順 |

## テーブル一覧

| No | スキーマ | テーブル名 | 説明 |
|----|----------|------------|------|
| 1 | common | account | アカウント |
| 2 | common | account_authority | アカウント権限 |
| 3 | common | kbn_mst | 区分マスタ |
| 4 | common | location_mst | ロケーションマスタ |
| 5 | common | refresh_token | リフレッシュトークン |
| 6 | common | login_history | ログイン履歴 |
| 7 | common | inquiry_mst | お問い合わせマスタ |
| 8 | common | inquiry_reply_mst | お問い合わせ返信マスタ |
| 9 | photo | photo_mst | 写真マスタ |
| 10 | photo | photo_tag_mst | 写真タグマスタ |
| 11 | photo | photo_favorite | 写真お気に入り |
| 12 | photo | photo_list_filter_log | 写真一覧絞り込みログ |
| 13 | photo | photo_view_log | 写真詳細閲覧ログ |

## DB初期化

データベースの初期化は `db/init/init-db.sh` によって以下の順序で実行されます。

1. `common/common.type.sql` - カスタム型定義
2. `common/account.sql` - アカウントテーブル
3. `common/inquiry_mst.sql` - お問い合わせマスタテーブル
4. `common/inquiry_reply_mst.sql` - お問い合わせ返信マスタテーブル
5. `common/account_authority.sql` - アカウント権限テーブル
6. `common/kbn_mst.sql` - 区分マスタテーブル
7. `common/location_mst.sql` - ロケーションマスタテーブル
8. `common/refresh_token.sql` - リフレッシュトークンテーブル
9. `common/login_history.sql` - ログイン履歴テーブル
10. `photo/photo.type.sql` - カスタム型定義
11. `photo/photo_mst.sql` - 写真マスタテーブル
12. `photo/photo_tag_mst.sql` - 写真タグマスタテーブル
13. `photo/photo_favorite.sql` - 写真お気に入りテーブル
14. `photo/photo_list_filter_log.sql` - 写真一覧絞り込みログテーブル
15. `photo/photo_view_log.sql` - 写真詳細閲覧ログテーブル
