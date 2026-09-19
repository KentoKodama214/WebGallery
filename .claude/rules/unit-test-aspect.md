---
paths:
  - backend/src/test/**/aspect/**
  - "!backend/src/test/**/integration/**"
---

# Aspectパッケージの単体テスト規約

`aspect/`配下のAOPクラス（管理者権限チェック等）のテストを対象とする。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し、`@Mock`でモック化した依存関係（`ProceedingJoinPoint`、`SessionHelper`等）と`@InjectMocks`を使用する

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド（Adviceメソッド）単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase名とする
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする

## モック・アサーションの記法

- モックのスタブ設定は`doReturn(...).when(mock).method(...)`を基本形とする
- Adviceの処理継続・中断の検証には、`ProceedingJoinPoint#proceed()`の呼び出し有無を`verify`で確認すること
