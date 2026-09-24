---
paths:
  - backend/src/test/**/controller/request/**
  - "!backend/src/test/**/integration/**"
---

# Requestパッケージの単体テスト規約

`controller/request/`配下のRequestクラスのテストを対象とする。標準のバリデーションアノテーション（`@NotNull`、`@Size`等）自体の付与は`.claude/rules/request.md`の`RequestArchitectureTest`で機械的に検証されるため、本パッケージの単体テストはカスタムバリデーションメソッドを対象とする。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする

## テスト対象

- `@AssertTrue`/`@AssertFalse`等が付与された、複数プロパティ・複合条件を検証するカスタムバリデーションメソッド（`isXxx()`形式）を持つRequestクラスのみ、テストクラスを作成すること
- 標準のバリデーションアノテーションのみを持ち、カスタムバリデーションメソッドを持たないRequestクラスは、単体テストの作成対象外とする（Controller層のMockMvcベーステストで間接的に検証される）

## カスタムバリデーションメソッドの検証

- 正常系（trueを返すケース）と異常系（falseを返すケース）の双方を検証すること
- 上限・下限等の閾値を持つ場合は、境界値ちょうどで結果が切り替わることを正常系・異常系のペアテストとして検証すること
- 複数プロパティの組み合わせで判定するメソッドは、プロパティの取りうる値のパターン（null、空文字、区切り文字の違い等）を網羅すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class isTagListSizeValid { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
