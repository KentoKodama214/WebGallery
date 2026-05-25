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

### photo スキーマ

| 型名 | 値 | 説明 |
|------|----|------|
| `photo.direction_enum` | `vertical`, `horizontal`, `square`, `none` | 写真の向き |

## テーブル一覧

| No | スキーマ | テーブル名 | 説明 |
|----|----------|------------|------|
| 1 | common | account | アカウント |
| 2 | common | kbn_mst | 区分マスタ |
| 3 | common | location_mst | ロケーションマスタ |
| 4 | common | refresh_token | リフレッシュトークン |
| 5 | photo | photo_mst | 写真マスタ |
| 6 | photo | photo_tag_mst | 写真タグマスタ |
| 7 | photo | photo_favorite | 写真お気に入り |

## DB初期化

データベースの初期化は `db/init/init-db.sh` によって以下の順序で実行されます。

1. `common/type.sql` - カスタム型定義
2. `common/account.sql` - アカウントテーブル
3. `common/kbn_mst.sql` - 区分マスタテーブル
4. `common/location_mst.sql` - ロケーションマスタテーブル
5. `common/refresh_token.sql` - リフレッシュトークンテーブル
6. `photo/type.sql` - カスタム型定義
7. `photo/photo_mst.sql` - 写真マスタテーブル
8. `photo/photo_tag_mst.sql` - 写真タグマスタテーブル
9. `photo/photo_favorite.sql` - 写真お気に入りテーブル
