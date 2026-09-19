---
paths:
  - backend/src/test/**/scheduler/**
  - "!backend/src/test/**/integration/**"
---

# Schedulerパッケージの単体テスト規約

`scheduler/`配下の`@Scheduled`クラスの検証内容（`SchedulerLock`をモックし、正しい`SchedulerLockNameEnum`とともにService層メソッドへ委譲していることの検証）は`.claude/rules/scheduler.md`の「## テスト」セクションを参照。本ファイルはテストクラスの構造に関する規約のみを定める。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し、`@Mock`でモック化した`SchedulerLock`等の依存関係と`@InjectMocks`を使用する

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド（定期実行メソッド）単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase名とする
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
