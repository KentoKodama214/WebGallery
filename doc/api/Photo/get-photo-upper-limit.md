# 写真登録上限チェック

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `GET /api/v1/accounts/{photoAccountId}/photos/upper-limit` |
| 概要 | 指定アカウントの写真登録枚数が上限に達しているかをチェックする |
| 認証 | 不要（認証ユーザー自身のアカウント以外は常にfalseを返却） |

## パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| photoAccountId | String | Yes | チェック対象のアカウントID |

## リクエスト例

```
GET /api/v1/accounts/testuser01/photos/upper-limit
```

## 成功レスポンス

**ステータスコード: 200 OK**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| isReachedUpperLimit | Boolean | 写真の登録枚数が上限に達しているか（認証ユーザー自身の場合のみ判定、それ以外はfalse） |

```json
{
  "isReachedUpperLimit": false
}
```
