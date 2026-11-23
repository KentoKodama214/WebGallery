# WebGallery データベース定義書

## 概要

WebGalleryはPostgreSQLを使用し、以下の2つのスキーマで構成されています。

| スキーマ | 用途 |
|----------|------|
| `common` | アカウント管理、区分マスタ、ロケーションマスタ |
| `photo`  | 写真メタデータ、タグ、お気に入り、ポートフォリオ |

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
| `common.inquiry_status_enum` | `unreplied`, `replied`, `withdrawn` | お問い合わせステータス区分 |
| `common.login_method_enum` | `password`, `passkey` | ログイン方式区分 |
| `common.webauthn_challenge_purpose_enum` | `registration`, `authentication` | WebAuthnチャレンジの用途区分 |

### photo スキーマ

| 型名 | 値 | 説明 |
|------|----|------|
| `photo.direction_enum` | `vertical`, `horizontal`, `square`, `none` | 写真の向き |
| `photo.sort_photo_enum` | `photo_at`, `favorite`, `season` | 写真一覧の並び順 |
| `photo.portfolio_template_enum` | `feature`, `grid` | ポートフォリオのテンプレート |
| `photo.portfolio_status_enum` | `draft`, `published` | ポートフォリオの公開状態 |

## テーブル一覧

| No | スキーマ | テーブル名 | 説明 |
|----|----------|------------|------|
| 1 | common | account | アカウント |
| 2 | common | account_authority | アカウント権限 |
| 3 | common | kbn_mst | 区分マスタ |
| 4 | common | location_mst | ロケーションマスタ |
| 5 | common | refresh_token | リフレッシュトークン |
| 6 | common | login_history | ログイン履歴 |
| 7 | common | passkey_user_handle | パスキーユーザーハンドル |
| 8 | common | passkey_credential | パスキー |
| 9 | common | webauthn_challenge | WebAuthnチャレンジ |
| 10 | common | inquiry_mst | お問い合わせマスタ |
| 11 | common | inquiry_reply_mst | お問い合わせ返信マスタ |
| 12 | photo | photo_mst | 写真マスタ |
| 13 | photo | photo_tag_mst | 写真タグマスタ |
| 14 | photo | photo_favorite | 写真お気に入り |
| 15 | photo | photo_list_filter_log | 写真一覧絞り込みログ |
| 16 | photo | photo_view_log | 写真詳細閲覧ログ |
| 17 | photo | photo_moderation_log | 写真モデレーションログ |
| 18 | photo | portfolio_mst | ポートフォリオマスタ |
| 19 | photo | portfolio_photo | ポートフォリオ写真 |
| 20 | photo | portfolio_tag_mst | ポートフォリオタグマスタ |

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
10. `common/passkey_user_handle.sql` - パスキーユーザーハンドルテーブル
11. `common/passkey_credential.sql` - パスキーテーブル
12. `common/webauthn_challenge.sql` - WebAuthnチャレンジテーブル
13. `photo/photo.type.sql` - カスタム型定義
14. `photo/photo_mst.sql` - 写真マスタテーブル
15. `photo/photo_tag_mst.sql` - 写真タグマスタテーブル
16. `photo/photo_favorite.sql` - 写真お気に入りテーブル
17. `photo/photo_list_filter_log.sql` - 写真一覧絞り込みログテーブル
18. `photo/photo_view_log.sql` - 写真詳細閲覧ログテーブル
19. `photo/photo_moderation_log.sql` - 写真モデレーションログテーブル
20. `photo/portfolio_mst.sql` - ポートフォリオマスタテーブル
21. `photo/portfolio_photo.sql` - ポートフォリオ写真テーブル
22. `photo/portfolio_tag_mst.sql` - ポートフォリオタグマスタテーブル
23. `photo/portfolio_reader.sql` - portfolio-api 用の読み取り専用ロール `portfolio_reader`（表示に必要なテーブル・列の SELECT のみを付与）
