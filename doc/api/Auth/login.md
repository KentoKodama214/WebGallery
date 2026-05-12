# ログイン

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `POST /api/v1/auth/login` |
| 概要 | アカウントID・パスワードで認証し、JWTトークンを発行する |
| 認証 | 不要 |
| Content-Type | `application/json` |

## リクエストボディ

| パラメータ | 型 | 必須 | バリデーション | 説明 |
|-----------|-----|------|--------------|------|
| accountId | String | Yes | 空白不可 | アカウントID |
| password | String | Yes | 空白不可 | パスワード |

## リクエスト例

```json
{
  "accountId": "testuser01",
  "password": "password01"
}
```

## 成功レスポンス

**ステータスコード: 200 OK**

レスポンスヘッダーに `Set-Cookie` でリフレッシュトークン（HttpOnly cookie）が付与されます。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accessToken | String | アクセストークン（JWT） |
| expiresIn | Long | アクセストークン有効期限（秒） |

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

### リフレッシュトークンcookie

| 属性 | 値 |
|------|-----|
| 名前 | `refreshToken` |
| HttpOnly | true |
| Secure | true |
| SameSite | Strict |
| Path | `/api/v1/auth` |

## エラーレスポンス

| ステータスコード | 説明 |
|-----------------|------|
| 400 Bad Request | 入力内容に誤りがある |
| 401 Unauthorized | アカウントIDまたはパスワードが間違っている |
| 423 Locked | アカウントがロックされている |
