---
paths:
  - backend/src/test/**/config/**
  - "!backend/src/test/**/integration/**"
---

# Configパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- 依存関係を持つ`@Configuration`クラス・フィルタクラスは`@ExtendWith(MockitoExtension.class)`を付与し`@Mock`でモック化した依存関係を使用する
- テスト対象は`@InjectMocks`ではなく、コンストラクタへ明示的に依存を渡す`new`でインスタンス化すること
  - Bean生成メソッドが同一クラスの他メソッドを呼び出す場合は、対象を`spy()`でラップし、内部呼び出しをスタブしてよい
- Servletフィルタ（`JwtAuthenticationFilter`、`RateLimitFilter`等）は、`HttpServletRequest`等を素朴に`@Mock`化するのではなく、`MockHttpServletRequest`/`MockHttpServletResponse`/`MockFilterChain`等のSpring Mock実装を活用してよい

## 起動時バリデーションの検証

- 起動時に設定値を検証するメソッド（`validateXxx`系）を持つConfigクラスは、正常系1ケースに対して異常系パターンを網羅的に列挙し、いずれの異常系も同一の例外クラス（例：`IllegalStateException`）でスローされることを検証すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class validateXxx { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## `@DisplayName`

- 正常系・異常系の区別が本質的でない設定値検証・分岐挙動の検証が中心のため、接頭辞を省略した説明文でよい。区別が本質的な場合は「正常系：」「異常系：」を付与する
