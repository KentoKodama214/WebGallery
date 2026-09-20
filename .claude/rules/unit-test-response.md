---
paths:
  - backend/src/test/**/controller/response/**
  - "!backend/src/test/**/integration/**"
---

# Responseパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする

## ファクトリメソッド（`from`/`of`）の検証

- Modelから変換する`from`系ファクトリメソッドは、全項目を設定した場合にそのまま値が転写されること（正常系）と、任意項目を未設定にした場合にnull・デフォルト値になること（異常系）の双方を検証すること
- 番兵値（未設定を表すデフォルト値）がnullへ変換される、コレクションがnull・空の場合に空リストへ変換される等、ドメイン固有の暗黙変換ルールがある場合は、専用のテストケースを追加すること
- 固定値や少数のパラメータから直接生成する`of`系ファクトリメソッドは、指定した値がそのまま設定されることを検証すること
- 複数の値から優先順位に従って値を採用する`of`系ファクトリメソッドは、各優先順位のパターンを網羅すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class from { ... }`）とする
  - オーバーロードされたメソッド（例：`of(String, Long, String)`と`of(PhotoSaveResultModel, PhotoSaveRequest)`）が存在する場合は、オーバーロード単位で`@Nested`クラスに分離し、引数型等が分かるcamelCase名とする
  - 変換対象のプロパティ数が多く観点ごとに整理する場合は、対象メソッドの`@Nested`クラスの内側に、観点を表すcamelCase名で`@Nested`クラスをさらにネストすること
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
