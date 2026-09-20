---
paths:
  - backend/src/test/**/mapper/**
  - "!backend/src/test/**/integration/**"
---

# Mapperパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@MybatisTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`を付与し、`@Sql`でDBフィクスチャを投入した上で実DBに対して検証すること（Mockitoは使用しない）

## SQLフィクスチャの配置・投入

- フィクスチャは1マッパー1ファイルとし、`backend/src/test/resources/sql/mapper/{テストクラス名}.sql`に配置すること
- `@Sql`はクラスレベルではなく`@Nested`クラスごとに付与し、`/sql/common/cleanup.sql` → `/sql/mapper/{テストクラス名}.sql`の順に重ねて指定すること
- insert系のテストで採番シーケンスに依存する場合は、上記に加えてシーケンスリセット用のSQL（例：`ResetAccountNoSeq.sql`）を付与すること

## 期待値・検証パターン

- フィクスチャの実データを決め打ちでハードコードし、期待するDTO/Entityを構築して`assertEquals(expected, actual)`で比較するか、個別フィールドを`assertEquals`で検証すること
- select/count/update系メソッドの検索条件は、条件カラムごとに1テストメソッドを対応させ、全条件カラムを網羅すること
- 複数条件を組み合わせる検索メソッドは、AND条件で正しく絞り込まれ、意図せず0件・全件にならないことを検証するテストを含めること
- `created_at`/`updated_at`等の日時カラムを検証する場合は、`jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class)`でトランザクション内の現在時刻を取得し、その値と比較すること

## テストの構造

- `@Test`メソッドは対象クラスの公開メソッド単位で`@Nested`クラスにグルーピングする
  - `@Nested`クラス名は、対象メソッド名と同一のcamelCase（例: `class selectByCondition { ... }`）とする
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する
- テストメソッド名は`対象メソッド名_条件`の形式（camelCase＋アンダースコア区切り、例: `selectByCondition_narrows_to_zero`）とする

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする。検索条件の絞り込み挙動の検証等、正常系・異常系の区別が本質的でない場合は接頭辞を省略してよい
