# 写真一覧取得

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `GET /api/v1/accounts/{photoAccountId}/photos` |
| 概要 | 指定アカウントの写真一覧を取得する |
| 認証 | 不要（お気に入り状態は認証済みユーザーのみ反映） |

## パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| photoAccountId | String | Yes | 写真を所有するアカウントのID |

## クエリパラメータ

| パラメータ | 型 | 必須 | デフォルト | バリデーション | 説明 |
|-----------|-----|------|-----------|--------------|------|
| directionKbn | String | No | `none` | `none` / `vertical` / `horizontal` / `square` | 写真の向きでフィルタ |
| isFavorite | Boolean | No | `false` | `true` / `false` | お気に入りのみ表示 |
| tagList | String | No | - | スペース区切り（半角・全角対応） | タグ名でフィルタ |
| sortBy | String | No | `photoAt` | `photoAt` / `favorite` / `season` | ソート順 |
| pageNo | Integer | No | `1` | 正の整数 | ページ番号 |

## リクエスト例

```
GET /api/v1/accounts/testuser01/photos?directionKbn=horizontal&sortBy=favorite&pageNo=1
```

## 成功レスポンス

**ステータスコード: 200 OK**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| isLast | Boolean | 最終ページかどうか |
| photoList | Array | 写真情報の配列 |

### photoListの各要素

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accountNo | Integer | アカウント番号 |
| photoNo | Integer | 写真番号 |
| isFavorite | Boolean | お気に入り登録済みか |
| imageFilePath | String | 画像ファイルパス |
| caption | String | キャプション |
| directionKbn | String | 写真の向き |

```json
{
  "isLast": false,
  "photoList": [
    {
      "accountNo": 1,
      "photoNo": 1,
      "isFavorite": true,
      "imageFilePath": "/image/testuser01/photo01.jpg",
      "caption": "東京タワーの夜景",
      "directionKbn": "horizontal"
    },
    {
      "accountNo": 1,
      "photoNo": 2,
      "isFavorite": false,
      "imageFilePath": "/image/testuser01/photo02.jpg",
      "caption": "富士山",
      "directionKbn": "horizontal"
    }
  ]
}
```
