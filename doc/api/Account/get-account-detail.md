# アカウント詳細取得

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `GET /api/v1/accounts/{accountId}` |
| 概要 | 指定したアカウントの詳細情報を取得する |
| 認証 | 必要（認証ユーザー自身のアカウントのみ取得可能） |

## リクエスト

### パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| accountId | String | Yes | アカウントID |

## 成功レスポンス

**ステータスコード: 200 OK**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accountId | String | アカウントID |
| accountName | String | アカウント名 |
| birthdate | String | 生年月日（`yyyy-MM-dd`形式、未設定の場合は`null`） |
| sexKbn | String | 性別区分（`none` / `man` / `woman`） |
| birthplacePrefectureKbnCode | String | 出身地の都道府県コード |
| residentPrefectureKbnCode | String | 居住地の都道府県コード |
| freeMemo | String | フリーメモ |

```json
{
  "accountId": "testuser01",
  "accountName": "テストユーザー",
  "birthdate": "1990-01-15",
  "sexKbn": "man",
  "birthplacePrefectureKbnCode": "Hokkaido",
  "residentPrefectureKbnCode": "Aomori",
  "freeMemo": "よろしくお願いします"
}
```

## エラーレスポンス

| ステータスコード | エラーコード | 説明 |
|-----------------|-------------|------|
| 403 Forbidden | E-C-0003 | 認証ユーザーと異なるアカウントIDを指定した |
