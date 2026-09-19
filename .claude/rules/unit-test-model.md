---
paths:
  - backend/src/test/**/model/**
  - "!backend/src/test/**/integration/**"
---

# Modelパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする
- Model・値オブジェクトの組み立ては`@Builder`を使用し、組み立てた値をアサートする形式で統一すること

## 変換ファクトリメソッド（`from`）の検証

- Entity/DTO/Requestから変換する`from`系ファクトリメソッドは、全項目を設定した場合にそのまま値が転写されること（`from_allFieldsSet`）と、任意項目を未設定にした場合にnull・デフォルト値になること（`from_optionalFieldsNull`）の双方を検証すること
- 空文字が`null`に変換される、EXIF値の`0`が「未設定」扱いになる等、ドメイン固有の暗黙変換ルールがある場合は、専用のテストケースを追加すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `XxxModelList`のフィルタ・ソート等のインスタンスメソッドも、メソッド単位で`@Nested`クラスにグルーピングし、正常系＋境界ケースを検証すること
  - `from`等の変換ファクトリメソッドも、メソッド単位で`@Nested`クラスにグルーピングすること
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class from { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `from_optionalFieldsNull`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
