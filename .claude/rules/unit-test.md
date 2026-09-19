---
paths:
  - backend/src/test/**
  - "!backend/src/test/**/integration/**"
  - "!backend/src/test/java/com/web/gallery/WebGalleryApplicationTests.java"
  - "!backend/src/test/java/com/web/gallery/ArchitectureTest.java"
  - "!backend/src/test/java/com/web/gallery/ModulithDocumentationTest.java"
  - "!backend/src/test/java/com/web/gallery/architecture/**"
---

# ユニットテストのアーキテクチャルール

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- 依存関係（DI対象のフィールド）を持つテスト対象クラスは、`@ExtendWith(MockitoExtension.class)`を付与し、`@Mock`でモック化した依存関係と`@InjectMocks`を使用する
- 依存関係を持たないテスト対象クラス（`aggregate/`の集約ルート、`model/`の値オブジェクト・コレクション等）は、Mockitoを使用せず対象クラスを直接インスタンス化してテストする
- `mapper/`配下のMapperインターフェースのテストは例外とし、`@MybatisTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`を付与し、`@Sql`でDBフィクスチャ（`backend/src/test/resources/sql/mapper/`）を投入した上で実DBに対して検証する（Mockitoは使用しない）
- `controller/`配下のControllerのテストは、`@WebMvcTest`を使用せず、コントローラー本体と依存を`@ExtendWith(MockitoExtension.class)`でモック化し、`MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new CommonControllerAdvice())`で構築した`MockMvc`を使用する

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class getPhotoList { ... }`）とする。PascalCaseや`Test`サフィックスは付与しない
  - 単一メソッド内の複数シナリオを分けて整理する場合も、シナリオを表すcamelCase名とする（PascalCase不可）
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `getPhotoList_not_found`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
- 正常系・異常系の区別が本質的でないテスト（設定値検証、フィルタ・ルーティング等の分岐挙動の検証等）は、上記接頭辞を省略した説明文でもよい

## モック・アサーションの記法

- モックのスタブ設定は`doReturn(...).when(mock).method(...)`を基本形とする
- 複数の`@Nested`クラスをまたいで共通に使うスタブを`@BeforeEach`で設定する場合は、未使用時にMockitoのstrict stubbing警告が出ないよう`lenient()`を付与する
- 「呼ばれないこと」の検証には`verify(mock, never())`または`verifyNoInteractions(mock)`を使用する
- Repository・Serviceに渡されるModelの内容を検証する場合は`ArgumentCaptor`を使用する

## 対象外

- `WebGalleryApplicationTests`はSpring Boot標準生成のアプリケーションコンテキストロード確認テストであり、本ルールの対象外とする
- `ArchitectureTest`、および`architecture/`パッケージ配下の`*ArchitectureTest`はArchUnitによるクラスパス解析ベースのアーキテクチャ検証テストであり、Mockitoによるモック化を前提としないため本ルールの対象外とする
- `ModulithDocumentationTest`はSpring Modulithによるドキュメント生成テストであり、Mockitoによるモック化を前提としないため本ルールの対象外とする
