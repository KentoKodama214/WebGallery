---
paths:
  - backend/src/test/**/aggregate/**
  - "!backend/src/test/**/integration/**"
---

# Aggregateパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- Mockitoは使用せず、対象クラスを直接インスタンス化してテストする

## テストの構造

- 静的ファクトリメソッド（`forRegist`/`forUpdate`/`forDelete`等）単位で`@Nested`クラスにグルーピングし、生成された集約の全プロパティを検証すること
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class forRegist { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- 番号の振り直し（連番採番）等、ファクトリメソッドが保証するライフサイクル上の不変条件も、正常系テストの一部としてまとめて検証すること

## セキュリティ観点のテスト

- 子エンティティの所有者情報（アカウント番号等）を親から強制するファクトリメソッド・更新メソッドには、入力値の改ざんが無視され親の所有者情報が優先されることを検証する専用テストを設けること
- 当該テストの`@DisplayName`は「セキュリティ：」で始める

## `@DisplayName`

- 「セキュリティ：」で始まるテストを除き、原則「正常系：」または「異常系：」で始まる日本語の説明文とする
