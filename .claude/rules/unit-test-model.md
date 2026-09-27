---
paths:
  - backend/src/test/**/application/model/**
  - "!backend/src/test/**/integration/**"
---

# Modelパッケージの単体テスト規約

`application/model/`のModelクラス（Entity/DTO/Requestからの変換ロジックを持たない、純粋なデータ保持クラス）を対象とする。Request→Modelの変換テストは`presentation/controller/converter/`のConverterクラスに対して書く（`unit-test-controller.md`の対象外、Converter自体は本ファイルと同様Mockitoを使わず直接インスタンス化してテストする）。Dto/Entity→Modelの変換テストは、変換ロジックを持つ`infrastructure/persistence/repository/`のRepository実装のテストでカバーする（`unit-test-repository.md`）。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする
- Model・値オブジェクトの組み立ては`@Builder`を使用し、組み立てた値をアサートする形式で統一すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `XxxModelList`のフィルタ・ソート等のインスタンスメソッドも、メソッド単位で`@Nested`クラスにグルーピングし、正常系＋境界ケースを検証すること
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class sortBySortOrder { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `sortBySortOrder_empty`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
