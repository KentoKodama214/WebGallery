# WebGallary API設計書

## 概要

WebGallaryのREST APIは、認証・アカウント管理・写真管理・お気に入り管理・都道府県取得の機能を提供します。

| 項目 | 内容 |
|------|------|
| ベースURL | `/api/v1` |
| データ形式 | JSON（`Content-Type: application/json`） |
| 文字コード | UTF-8 |
| 認証方式 | JWT（Bearer Token）認証 |

## ドキュメント一覧

### 認証API（Auth）

- [ログイン](./Auth/login.md)
- [トークンリフレッシュ](./Auth/refresh.md)
- [ログアウト](./Auth/logout.md)

### アカウント管理API（Account）

- [アカウント詳細取得](./Account/get-account-detail.md)
- [アカウント一覧取得](./Account/get-account-list.md)
- [アカウント登録](./Account/regist-account.md)
- [アカウント更新](./Account/update-account.md)

### 写真管理API（Photo）

- [写真一覧取得](./Photo/get-photo-list.md)
- [写真詳細取得](./Photo/get-photo-detail.md)
- [写真登録](./Photo/regist-photo.md)
- [写真更新](./Photo/update-photo.md)
- [写真削除](./Photo/delete-photo.md)

### お気に入りAPI（PhotoFavorite）

- [お気に入り登録](./PhotoFavorite/regist-favorite.md)
- [お気に入り解除](./PhotoFavorite/delete-favorite.md)

### 都道府県API（Prefecture）

- [都道府県一覧取得](./Prefecture/get-prefectures.md)

### 共通

- [エラー定義](./error-definition.md)

## API一覧

| No | Controllerクラス | HTTPメソッド | エンドポイント | 概要 | 認証 |
|----|------------------|-------------|---------------|------|------|
| 1 | AuthRestController | POST | [`/api/v1/auth/login`](./Auth/login.md) | ログイン | 不要 |
| 2 | AuthRestController | POST | [`/api/v1/auth/refresh`](./Auth/refresh.md) | トークンリフレッシュ | 不要（リフレッシュトークンcookie必要） |
| 3 | AuthRestController | POST | [`/api/v1/auth/logout`](./Auth/logout.md) | ログアウト | 不要 |
| 4 | AccountRestController | POST | [`/api/v1/accounts`](./Account/regist-account.md) | アカウント登録 | 不要 |
| 5 | AccountRestController | PUT | [`/api/v1/accounts/{accountId}`](./Account/update-account.md) | アカウント更新 | 必要 |
| 6 | KbnMstRestController | GET | [`/api/v1/prefectures`](./Prefecture/get-prefectures.md) | 都道府県一覧取得 | 不要 |
| 7 | PhotoRestController | GET | [`/api/v1/accounts/{photoAccountId}/photos`](./Photo/get-photo-list.md) | 写真一覧取得 | 不要 |
| 8 | PhotoRestController | GET | [`/api/v1/accounts/{photoAccountId}/photos/{photoNo}`](./Photo/get-photo-detail.md) | 写真詳細取得 | 不要 |
| 9 | PhotoRestController | POST | [`/api/v1/accounts/{photoAccountId}/photos`](./Photo/regist-photo.md) | 写真登録 | 必要 |
| 10 | PhotoRestController | PUT | [`/api/v1/accounts/{photoAccountId}/photos`](./Photo/update-photo.md) | 写真更新 | 必要 |
| 11 | PhotoRestController | DELETE | [`/api/v1/accounts/{photoAccountId}/photos`](./Photo/delete-photo.md) | 写真削除 | 必要 |
| 12 | PhotoFavoriteController | POST | [`/api/v1/photos/favorites`](./PhotoFavorite/regist-favorite.md) | お気に入り登録 | 必要 |
| 13 | PhotoFavoriteController | DELETE | [`/api/v1/photos/favorites`](./PhotoFavorite/delete-favorite.md) | お気に入り解除 | 必要 |

## 認証・認可

### 認証

- JWT（JSON Web Token）によるステートレス認証を使用
- ログイン成功時にアクセストークン（レスポンスボディ）とリフレッシュトークン（HttpOnly cookie）を発行
- APIリクエストには `Authorization: Bearer {accessToken}` ヘッダーを付与
- パスワードはBCryptでハッシュ化

### 認可ルール

| 操作 | ルール |
|------|--------|
| アカウント更新 | 自分自身のアカウントのみ更新可能 |
| 写真登録・更新・削除 | 自分自身の写真のみ操作可能 |
| お気に入り登録・解除 | 認証済みユーザーは任意の写真に対して操作可能 |
| 写真一覧・詳細取得 | 認証不要（公開アクセス可能） |

### 権限レベルと写真登録上限

| 権限レベル | 写真登録上限 |
|-----------|-------------|
| MINI | 10枚 |
| NORMAL | 1,000枚 |
| SPECIAL | 無制限 |
| ADMINISTRATOR | 無制限 |

## 共通仕様

### ファイルアップロード

| 項目 | 制限値 |
|------|--------|
| 最大ファイルサイズ | 5MB |
| サーブレットレベル最大サイズ | 6MB |

### 日時フォーマット

| 型 | フォーマット | 例 |
|----|-------------|-----|
| 日付 | `yyyy-MM-dd` | `2024-01-15` |
| 日時 | `yyyy-MM-ddTHH:mm:ss` | `2024-01-15T12:30:00` |

### 区分値

#### 性別区分（sexKbn）

| 値 | 説明 |
|----|------|
| `none` | 未設定 |
| `man` | 男性 |
| `woman` | 女性 |

#### 写真の向き（directionKbn）

| 値 | 説明 |
|----|------|
| `none` | 未設定 |
| `vertical` | 縦向き |
| `horizontal` | 横向き |
| `square` | 正方形 |

#### ソート順（sortBy）

| 値 | 説明 |
|----|------|
| `photoAt` | 撮影日順（デフォルト） |
| `favorite` | お気に入り数順 |
| `season` | 季節順 |
