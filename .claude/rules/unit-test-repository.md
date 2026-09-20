---
paths:
  - backend/src/test/**/repository/**
  - "!backend/src/test/**/integration/**"
---

# Repositoryパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し、`@Mock`でモック化した依存関係と`@InjectMocks`を使用する
- `@Mock`で注入する依存は、対象の`mapper/`インターフェース、または`PasswordEncoder`・`S3Client`・`S3Presigner`等の外部SDK/技術コンポーネントに限ること

## Entity変換の検証

- insert/update対象のEntity（`Condition`/`UpdateTarget`含む）は`ArgumentCaptor`で捕捉し、全フィールドを`assertEquals`で検証すること
- Modelの必須項目のみ指定したケースと、全項目を指定したケースの最低2パターンを用意し、デフォルト値・センチネル値への変換ロジックを網羅すること
- update系で未指定項目が「変更なし」としてnullで渡ることを検証する場合は、その旨をコメントで明示した上で`assertNull`すること

## 例外・異常系の検証

- Mapperの戻り値が0件の場合に`UpdateFailureException`を、`DuplicateKeyException`がスローされた場合に`RegistFailureException`/`UpdateFailureException`をスローすることを、それぞれセットで検証すること

## 集約Repository固有のルール

- 複数テーブルへの操作順序に意味がある場合（外部キー制約等）、`InOrder`で呼び出し順序を検証し、各ステップの理由をコメントで明示すること
- 整合性が崩れる異常系（例：親テーブルの登録失敗時に子テーブルへの登録が行われないこと）は、`verify(mapper, times(0))`で確認すること

## 外部I/O（S3等）の検証

- SDKからスローされる例外はラップせずそのまま伝播することを検証すること
- `IOException`等を独自例外へラップする処理は、その境界を明示的にテストすること

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
