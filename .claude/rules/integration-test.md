---
paths:
  - backend/src/test/**/integration/**
---

# 統合テストのアーキテクチャルール

## 命名規則

- クラス名に`IntegrationTest`サフィックスを付与すること
- `@Nested`内部クラスには、検証対象メソッド名と同一の名前（小文字始まり）を付与すること（例：`class getPhotoList`、`class addFavorite`）

## テスト種別

- `@SpringBootTest`と`@ActiveProfiles("test")`を使用
- `@Transactional`による自動ロールバック
- `@Sql("/sql/...")`アノテーションでテストフィクスチャデータを読み込み
- `created_at`/`updated_at`等の日時カラムを検証する場合は、`jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class)`でトランザクション内の現在時刻を取得し、その値と比較すること

## 配置ルール

- 統合テストクラスは`integration/`サブディレクトリに配置すること
- `integration/`ディレクトリにあるのに`IntegrationTest`サフィックスがない、またはその逆のケースは違反

## SQLフィクスチャの配置規則

- テスト用SQLフィクスチャは`backend/src/test/resources/sql/{controller|repository|service}/{テストクラス名}.sql`に、対象クラスのレイヤーと同名のディレクトリへ配置すること
- 1クラス内で機能（`@Nested`クラス）ごとにフィクスチャを分ける場合は`{テストクラス名}{機能名}.sql`のように命名すること（例：`AccountControllerDeleteAccountIntegrationTest.sql`）
- `@Sql`は共通クリーンアップ用SQL（`/sql/common/cleanup.sql`）→クラス固有SQLの順に重ねて指定すること

## モック化の方針

- repository層・service層の統合テストは、原則としてMockitoによるモック化を行わず、実際のDB・実装をAutowireして検証すること
- S3ストレージ等、統合テストで実接続させるべきでない外部I/O依存のみ`@MockitoBean`でモック化すること（例：`FileRepository`）

## 異常系検証の型

- controller層：HTTPステータス（`status().isXxx()`）と、レスポンスの`errorCode`/`errorMessage`を`ErrorEnum`の値でjsonPath検証すること
- repository層・service層：`assertThrows(XxxException.class, () -> ...)`で例外クラスを直接検証すること

## REQUIRES_NEWトランザクションを伴う処理のテスト

- 絞り込みログ・閲覧ログ・リフレッシュトークン削除等、`REQUIRES_NEW`で別コネクションのトランザクションを使用する処理を検証する場合、フィクスチャ投入後に`TestTransaction.flagForCommit()` → `TestTransaction.end()` → `TestTransaction.start()`で一度物理コミットしてから新しいテスト用トランザクションを開始すること
- テスト終了後は、物理コミットした内容が他のテストクラスへ残留しないよう、`@AfterEach`で明示的に`TRUNCATE ... CASCADE`して物理コミットすること

## セットアップの分割

- フィクスチャのコミットとモックスタブの設定など、役割が異なる初期化処理は`@BeforeEach`メソッドを分けて、処理内容が分かる名前を付与すること（例：`commitFixtures`、`setUpFileRepositoryStub`）
