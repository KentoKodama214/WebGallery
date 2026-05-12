# 都道府県一覧取得

## 基本情報

| 項目 | 内容 |
|------|------|
| エンドポイント | `GET /api/v1/prefectures` |
| 概要 | 都道府県の一覧を地方グループ別に取得する |
| 認証 | 不要 |

## リクエスト

パラメータはありません。

## 成功レスポンス

**ステータスコード: 200 OK**

レスポンスは地方グループの配列です。

### 配列の各要素

| フィールド | 型 | 説明 |
|-----------|-----|------|
| groupName | String | 地方名（例: 北海道・東北） |
| prefectures | Array | 都道府県の配列 |

### prefecturesの各要素

| フィールド | 型 | 説明 |
|-----------|-----|------|
| kbnCode | String | 都道府県コード |
| kbnJapaneseName | String | 都道府県名 |

```json
[
  {
    "groupName": "北海道・東北",
    "prefectures": [
      {
        "kbnCode": "Hokkaido",
        "kbnJapaneseName": "北海道"
      },
      {
        "kbnCode": "Aomori",
        "kbnJapaneseName": "青森県"
      }
    ]
  }
]
```
