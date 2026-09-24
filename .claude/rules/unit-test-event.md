---
paths:
  - backend/src/test/**/event/**
  - "!backend/src/test/**/integration/**"
---

# Eventパッケージの単体テスト規約

`event/`配下のリスナークラス（`Listener`サフィックス）のテストを対象とする。

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し`@InjectMocks`のみを使用すること。リスナークラスは`domain/`のみに依存する設計のため、`@Mock`でモック化する依存関係を持たない

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `handle`メソッドはイベント型ごとにオーバーロードされているため、イベント型単位で`@Nested`クラスに分離すること
  - `@Nested`クラス名は`handle{イベントクラス名}`の形式（例: `class handleAccountRegisteredEvent { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- 各イベントに対し、`assertDoesNotThrow(() -> listener.handle(event))`で例外が発生しないことを検証する1テストを配置すること
- テストメソッド名は`handle_{イベントクラス名の先頭を小文字にした名前}_success`の形式とする

## `@DisplayName`

- 正常系・異常系の区別が本質的でないため、接頭辞を省略した説明文でよい
