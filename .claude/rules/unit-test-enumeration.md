---
paths:
  - backend/src/test/**/enumeration/**
  - "!backend/src/test/**/integration/**"
---

# Enumerationパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化・呼び出ししてテストする

## `getOrDefault`系メソッドの検証

- DB保存値・クエリパラメータ値等からEnumを解決する`getOrDefault`系静的メソッドは、以下4パターンを網羅すること
  - 値が一致する場合、対応するEnumが返ること
  - Enum名（`name()`）に一致する場合、対応するEnumが返ること（該当するメソッドがある場合のみ）
  - `null`を指定した場合、デフォルトのEnumが返ること
  - どの値にも一致しない場合、デフォルトのEnumが返ること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class getOrDefault { ... }`）とする
  - オーバーロードされたメソッド（例：`String`版と`Enum`版）が存在する場合は、オーバーロード単位で`@Nested`クラスに分離し、引数型等が分かるcamelCase名（例: `class getOrDefaultByString { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
