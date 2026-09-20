---
paths:
  - backend/src/test/**/service/**
  - "!backend/src/test/**/integration/**"
---

# Serviceパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し、`@Mock`でモック化した依存関係と`@InjectMocks`を使用する
- `@Mock`で注入する依存は、`repository/impl/`の実装クラス（インターフェースでなく実装型）、`config/`の設定値クラス、`policy/`、`ApplicationEventPublisher`、`Clock`を対象とする
- `Clock`に依存するクラスは、`@BeforeEach`で`clock.instant()`と`clock.getZone()`を`lenient()`付きでスタブすること

## ドメインイベント・例外系の検証

- ドメインイベント発行を伴う処理は、`ApplicationEventPublisher`を`@Mock`化し、`ArgumentCaptor`でイベント内容をフィールド単位で検証すること
- 例外発生時は`assertThrows(XxxException.class, () -> service.method(...))`で例外クラスを直接検証すること
- 例外発生時は、Repository未呼び出し・イベント未発行を`verify(mock, times(0))`で併せて確認すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class getPhotoList { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `getPhotoList_not_found`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする

## モック・アサーションの記法

- モックのスタブ設定は`doReturn(...).when(mock).method(...)`を基本形とする
- 複数の`@Nested`クラスをまたいで共通に使うスタブを`@BeforeEach`で設定する場合は、未使用時にMockitoのstrict stubbing警告が出ないよう`lenient()`を付与する
- 「呼ばれないこと」の検証には`verify(mock, never())`または`verifyNoInteractions(mock)`を使用する
- Repositoryに渡されるModelの内容を検証する場合は`ArgumentCaptor`を使用する
