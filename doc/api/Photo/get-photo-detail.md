# 写真詳細取得

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `GET /api/v1/accounts/{photoAccountId}/photos/{photoNo}` |
| 概要 | 指定した写真の詳細情報を取得する |
| 認証 | 不要（お気に入り状態は認証済みユーザーのみ反映） |

## パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| photoAccountId | String | Yes | 写真を所有するアカウントのID |
| photoNo | Integer | Yes | 写真番号 |

## クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| accountNo | Integer | No | 写真所有者のアカウント番号 |

## リクエスト例

```
GET /api/v1/accounts/testuser01/photos/1?accountNo=1
```

## 成功レスポンス

**ステータスコード: 200 OK**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accountNo | Integer | アカウント番号 |
| photoNo | Integer | 写真番号 |
| isFavorite | Boolean | お気に入り登録済みか |
| photoAt | String | 撮影日時（ISO 8601） |
| locationNo | Integer | ロケーション番号 |
| address | String | 住所 |
| latitude | Decimal | 緯度 |
| longitude | Decimal | 経度 |
| locationName | String | ロケーション名 |
| imageFilePath | String | 画像ファイルパス |
| photoJapaneseTitle | String | 写真タイトル（日本語） |
| photoEnglishTitle | String | 写真タイトル（英語） |
| caption | String | キャプション |
| directionKbn | String | 写真の向き |
| focalLength | Integer | 焦点距離 |
| fValue | Decimal | F値 |
| shutterSpeed | Decimal | シャッタースピード |
| iso | Integer | ISO感度 |
| photoTagList | Array | タグ情報の配列 |

### photoTagListの各要素

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accountNo | Integer | アカウント番号 |
| photoNo | Integer | 写真番号 |
| tagNo | Integer | タグ番号 |
| tagJapaneseName | String | タグ名（日本語） |
| tagEnglishName | String | タグ名（英語） |

```json
{
  "accountNo": 1,
  "photoNo": 1,
  "isFavorite": true,
  "photoAt": "2024-01-15T12:30:00+09:00",
  "locationNo": 1,
  "address": "東京都港区芝公園4丁目2-8",
  "latitude": 35.6586,
  "longitude": 139.7454,
  "locationName": "東京タワー",
  "imageFilePath": "/image/testuser01/photo01.jpg",
  "photoJapaneseTitle": "東京タワーの夜景",
  "photoEnglishTitle": "Tokyo Tower Night View",
  "caption": "東京タワーの美しい夜景",
  "directionKbn": "horizontal",
  "focalLength": 50,
  "fValue": 2.8,
  "shutterSpeed": 0.01,
  "iso": 800,
  "photoTagList": [
    {
      "accountNo": 1,
      "photoNo": 1,
      "tagNo": 1,
      "tagJapaneseName": "夜景",
      "tagEnglishName": "night view"
    }
  ]
}
```

## エラーレスポンス

| ステータスコード | エラーコード | 説明 |
|-----------------|-------------|------|
| 404 Not Found | E-P-0009 | 写真が存在しない |
