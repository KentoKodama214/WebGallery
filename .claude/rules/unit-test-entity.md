---
paths:
  - backend/src/test/**/entity/**
  - "!backend/src/test/**/integration/**"
---

# Entityパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- 依存関係（`PasswordEncoder`等）を持つファクトリメソッドを検証する場合のみ`@ExtendWith(MockitoExtension.class)`を付与し`@Mock`を使用する
- 依存関係を持たない場合はMockitoを使用せず、対象クラスを直接インスタンス化する

## ファクトリメソッド（`fromForXxx`/`from`）の検証

- 「全項目が設定されている場合、そのまま値が反映されること」と「未設定項目のみの場合、デフォルト値が反映されること」の最低2ケースを検証すること
- `BigDecimal`型プロパティの検証は`assertEquals(0, expected.compareTo(actual))`を用い、スケール差異による`assertEquals`の不一致を避けること

## テストの構造

- ファクトリメソッド単位で`@Nested`クラスにグルーピングすること
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class fromForRegist { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
