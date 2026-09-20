---
paths:
  - backend/src/test/**/dto/**
  - "!backend/src/test/**/integration/**"
---

# Dtoパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする

## 変換ファクトリメソッド（`from`）の検証

- Modelから変換する`from`系ファクトリメソッドは、境界値・null分岐となる観点ごとに1つの`@Test`メソッドを用意し、漏れなくカバーすること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class from { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `from_optionalFieldsNull`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
