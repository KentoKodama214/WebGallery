---
paths:
  - backend/src/test/**/controller/**
  - "!backend/src/test/**/integration/**"
---

# Controllerパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@WebMvcTest`は使用せず、`@ExtendWith(MockitoExtension.class)`でController本体と依存を`@Mock`化し、`@InjectMocks`でController本体を構築する
- `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new CommonControllerAdvice())`を基本形として`MockMvc`を構築すること
  - リクエストボディにJSONを使用するクラスは、`JacksonJsonHttpMessageConverter`を明示的に設定すること

## リクエストボディの構築

- リクエストボディはJavaオブジェクトの組み立てやインラインのJSON文字列ではなく、`backend/src/test/resources/json/controller/{テストクラス名}/{ファイル名}.json`に配置したJSONファイルを`readJsonFile`ヘルパーで読み込んで使用すること

## レスポンス検証

- HTTPステータスは`status().isXxx()`、レスポンスボディは`jsonPath("$.フィールド名").value(...)`で検証すること
- 一覧系レスポンスは`jsonPath("$.xxxList[n].field")`のインデックスアクセスで検証すること

## 認可の検証

- `SessionHelper`を`@Mock`化し、`getAccountId()`/`getAccountNo()`をスタブすること
- パスパラメータの`accountId`と`SessionHelper`が返すアカウント情報が不一致の場合に403を返すテストを設けること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class getPhotoList { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- `@Nested`クラス内は「正常系 → バリデーション異常系（400） → 業務例外系（409/404等）」の順に並べること
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `getPhotoList_not_found`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする

## モック・アサーションの記法

- モックのスタブ設定は`doReturn(...).when(mock).method(...)`を基本形とする
- バリデーション違反・認可失敗により400/403を返すテストでは、対象のServiceメソッドが呼ばれていないことを`verify(service, times(0))`で検証すること
- 「呼ばれないこと」の検証には`verify(mock, never())`または`verifyNoInteractions(mock)`を使用する
