# 写真登録

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `POST /api/v1/accounts/{photoAccountId}/photos` |
| 概要 | 新しい写真を登録する |
| 認証 | 必要（自分自身の写真のみ登録可能） |
| Content-Type | `multipart/form-data` |

## パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| photoAccountId | String | Yes | 写真を登録するアカウントのID |

## リクエストボディ

| パラメータ | 型 | 必須 | バリデーション | 説明 |
|-----------|-----|------|--------------|------|
| accountNo | Integer | Yes | 正の整数 | アカウント番号 |
| photoNo | Integer | No | 正の整数 | 写真番号 |
| favoriteCount | Integer | No | - | お気に入り数 |
| isFavorite | Boolean | No | - | お気に入りフラグ |
| photoAt | String | No | 過去日時、`yyyy-MM-ddTHH:mm:ss`形式 | 撮影日時 |
| locationNo | Integer | No | - | ロケーション番号 |
| address | String | No | - | 住所 |
| latitude | Decimal | No | - | 緯度 |
| longitude | Decimal | No | - | 経度 |
| locationName | String | No | - | ロケーション名 |
| imageFile | File | 条件付き | `imageFilePath`が未指定の場合は必須、最大5MB | 画像ファイル |
| imageFilePath | String | 条件付き | `imageFile`が未指定の場合は必須 | 画像ファイルパス |
| photoJapaneseTitle | String | No | - | 写真タイトル（日本語） |
| photoEnglishTitle | String | No | - | 写真タイトル（英語） |
| caption | String | No | - | キャプション |
| directionKbn | String | Yes | `none` / `vertical` / `horizontal` / `square` | 写真の向き |
| focalLength | Integer | No | 正の整数 | 焦点距離 |
| fValue | Decimal | No | 正の小数 | F値 |
| shutterSpeed | Decimal | No | 正の小数 | シャッタースピード |
| iso | Integer | No | 正の整数 | ISO感度 |
| photoTagRegistRequestList | Array | No | - | タグ情報の配列 |

### photoTagRegistRequestListの各要素

| パラメータ | 型 | 必須 | バリデーション | 説明 |
|-----------|-----|------|--------------|------|
| accountNo | Integer | Yes | 正の整数 | アカウント番号 |
| photoNo | Integer | Yes | 正の整数 | 写真番号 |
| tagNo | Integer | Yes | 正の整数 | タグ番号 |
| tagJapaneseName | String | Yes | スペース不可（半角・全角） | タグ名（日本語） |
| tagEnglishName | String | No | - | タグ名（英語） |

## 成功レスポンス

**ステータスコード: 200 OK**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| httpStatus | Integer | HTTPステータスコード |
| isSuccess | Boolean | 処理成功フラグ |
| message | String | メッセージ |
| photoNo | Integer | 登録された写真番号 |
| imageFilePath | String | 保存された画像ファイルパス |

```json
{
  "httpStatus": 200,
  "isSuccess": true,
  "message": "写真登録が完了しました。",
  "photoNo": 1,
  "imageFilePath": "/image/testuser01/photo01.jpg"
}
```

## エラーレスポンス

| ステータスコード | エラーコード | 説明 |
|-----------------|-------------|------|
| 400 Bad Request | E-C-0000 | 入力内容に誤りがある |
| 400 Bad Request | E-P-0010 | 写真登録上限に達している |
| 403 Forbidden | E-P-0008 | 写真を登録する権限がない |
| 409 Conflict | E-P-0007 | 画像ファイルが重複している |
| 409 Conflict | E-P-0001 | 写真登録に失敗 |
