---
paths:
  - backend/src/test/**/policy/**
  - "!backend/src/test/**/integration/**"
---

# Policyパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- 依存の有無にかかわらず`@ExtendWith(MockitoExtension.class)`を付与すること
  - 依存を持つPolicyは`@Mock`でモック化した依存関係と`@InjectMocks`を使用する
  - 依存を持たないPolicyも`@ExtendWith(MockitoExtension.class)`は付与するが、対象クラスは`new`で直接インスタンス化し、`@InjectMocks`は使用しない

## 判定ロジックの検証

- 閾値判定を行うメソッドは、境界値ちょうどで判定結果が切り替わることを正常系のペアテスト（例：`isSizeExceeded_exactlyAtLimit`と超過ケース）として検証すること
- ファイル検証等セキュリティに関わるPolicyは、拡張子偽装・Content-Type偽装・マジックバイト不一致・パストラバーサル等の偽装パターンを異常系として明示的にテストすること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class isSizeExceeded { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `isSizeExceeded_exactlyAtLimit`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする

## モック・アサーションの記法

- モックのスタブ設定は`doReturn(...).when(mock).method(...)`を基本形とする
