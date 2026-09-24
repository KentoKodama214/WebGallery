---
paths:
  - backend/src/test/**/domain/**
  - "!backend/src/test/**/integration/**"
---

# Domainパッケージの単体テスト規約

`domain/`配下の値オブジェクト（`record`）のテストを対象とする。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする

## コンパクトコンストラクタの検証

- 正常系「有効な値でインスタンスが生成されること」と、異常系「`null`で`IllegalArgumentException`をスローすること」を必ず作成すること
- 文字数上限を持つ値オブジェクトは、正常系に上限文字数ちょうどのテスト（`constructor_maxLength`）、異常系に上限超過のテスト（`constructor_tooLong`）を追加すること。下限がある場合も同様に`constructor_tooShort`を追加すること
- 数値型で0または負値が不正となる値オブジェクトは、`constructor_zero`・`constructor_negative`をそれぞれ追加すること
- 文字列型でトリム後空文字が不正となる値オブジェクトは、`constructor_blank`を追加すること
- 上記に該当しない、フォーマット不正等その他のバリデーション観点がある場合も、観点ごとに1つの`@Test`メソッドを追加し漏れなくカバーすること
- `@ParameterizedTest`は使用せず、1バリデーション観点につき1つの`@Test`メソッドとすること

## ファクトリメソッドの検証

- `getOrDefault`等のデフォルト値補完ファクトリメソッドを持つ場合、非null入力時は`assertSame`で同一インスタンスが返ることを、null入力時は`assertEquals`でデフォルト値が返ることを検証すること

## 対象外のメソッド

- `equals`/`hashCode`は`record`の自動生成メソッドであるため個別のテスト対象としない
- `toString()`は、マスキング等の意図的なロジックを持つ場合のみテスト対象とする

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - コンパクトコンストラクタの検証テストは`class constructor { ... }`にグルーピングすること
  - `getOrDefault`等の追加のファクトリメソッドや`toString()`のオーバーライドがある場合も、メソッド単位で`@Nested`クラスにグルーピングすること
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class getOrDefault { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `constructor_tooLong`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
