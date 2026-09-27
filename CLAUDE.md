# CLAUDE.md - WebGallery AIアシスタントガイド

## プロジェクト概要

WebGalleryは、Spring Bootで構築されたフォトギャラリーWebアプリケーションです。ユーザーはアカウント登録、メタデータ/EXIFデータ付きの写真アップロード、フォトギャラリーの閲覧、写真のタグ付け、お気に入り管理が可能です。コードベースおよびすべてのドキュメント・コメントは日本語で記述されています。

## 技術スタック・プロジェクト構造

詳しくは、[README.md](README.md)を参照。

## ビルド・実行コマンド

```bash
# PostgreSQLデータベースの起動・停止（アプリ実行前に必要）
just db-up
just db-down

# backendのビルド
just backend-build

# backendの単体テスト実行
just backend-unitTest

# backendアプリケーションの実行
just backend-run

# WARファイルのビルド
./backend/gradlew -p backend war

# クリーンビルド
just backend-clean-build

# backendのコードフォーマットチェック・整形（Spotless + Google Java Format）
just format-check
just format

# frontendの依存パッケージインストール・開発サーバー起動・ビルド・lint（just経由）
just front-setup
just front-run
just front-build
just lint

# E2Eテスト一括実行（DB・backendを自動起動）
just e2e

# 本番ビルドに対するCSPスモークテスト（next build + next start）
just e2e-prod
```

## アーキテクチャ

### オニオンアーキテクチャ (domain → application → infrastructure/presentation)

`com.web.gallery`直下に4つのレイヤーをトップレベルパッケージとして持ち、依存は常に内側（domain）へ向かう。

1. **domain層**（最内層・外部依存なし） - プロパティ単位の値オブジェクト（`record`、`domain/model/{機能}/`）、単一のビジネスルールを判定するドメインサービス（`Policy`サフィックス、`domain/service/`）、ドメインイベント・リスナー（`domain/event/`）、カスタム例外（`domain/exception/`）、ビジネス区分値Enum（`domain/enumeration/`）、定数（`domain/constant/`、`ApiRoutes`/`Consts`/`MessageConst`）
2. **application層**（ユースケース。domainのみに依存） - レイヤー間転送用のModelオブジェクト（`application/model/{機能}/`）、Service（`application/service/{機能}/` + `application/service/impl/{機能}/`）、Repositoryインターフェース（`application/repository/{機能}/`）、書き込みユースケースの集約ルート（`application/aggregate/`）、技術詳細へのポート（`application/helper/`、`application/config/`）
3. **infrastructure層**（技術詳細。domain・applicationに依存可） - Entity/Dto/Mapper/Repository実装（`infrastructure/persistence/{entity,dto,mapper,repository}/{機能}/`）、定期実行タスク（`infrastructure/scheduler/`、有効化は`infrastructure/config/SchedulingConfig`）、セキュリティ実装（`infrastructure/security/`、`infrastructure/web/`）、上記ポートの実装（`infrastructure/security・web・config`の`*Impl`クラス）
4. **presentation層**（domain・applicationに加えinfrastructureにも一方向で依存可） - RESTコントローラー（`presentation/controller/{機能}/`）、Request/Response DTO（`presentation/request/{機能}/`、`presentation/response/{機能}/`）、Request→ModelのConverter（`presentation/converter/{機能}/`）

Service層が本来infrastructure層の技術（JWT生成・GeoIP解決・`application.yml`の設定値等）を必要とする場合は、`application/helper/`・`application/config/`にポート（インターフェース）を定義し、`infrastructure/`側に実装を置く（DIP）。書き込みユースケースで複数テーブルにまたがる整合性・ライフサイクルの管理が必要な場合は`application/aggregate/`に集約ルートクラスを定義し、Service層とRepository層の間に位置づける（例：`Photo`集約）。詳細は`.claude/rules/aggregate.md`を参照。

依存方向・パッケージ構成の全体像は`.claude/rules/architecture-overview.md`を、各レイヤーの詳細なルール（依存関係、命名規則、アノテーション規約等）は `.claude/rules/` 配下のルールファイルを参照。

### セキュリティモデル

詳しくは、[セキュリティ](doc/architecture/security.md)を参照。

### ユーザー権限レベル・エラーコード

API仕様はアプリケーション起動後、Scalar UI（`/scalar`）またはOpenAPI JSON（`/v3/api-docs`）で確認できる。

### データベーススキーマ

詳しくは、[データベース定義書](doc/database/README.md)を参照。テーブル定義・ER図はSchemaSpyで自動生成する。

## テスト規約

テストクラスの種別・命名規則・配置ルールは `.claude/rules/unit-test-*.md`（パッケージ単位）と `.claude/rules/integration-test.md` を参照。

### テストデータベース

- 専用データベース：`web_gallery_test`（`application-test.yml`で設定）
- テスト用SQLフィクスチャは`backend/src/test/resources/sql/`にレイヤー別に整理
- 統合テストの実行にはDocker PostgreSQLの起動が必要

### テスト実行

```bash
# backendの単体テスト実行
just backend-unitTest

# backendの特定のテストクラスを実行
./backend/gradlew -p backend unitTest --tests "com.web.gallery.application.service.impl.photo.PhotoServiceImplTest"

# frontendの単体テスト（Jest）
just front-test

# frontendのE2Eテスト（Playwright、DB・backendを自動起動）
just e2e
```

### フロントエンドのテスト

- 単体テスト（Jest）は各コンポーネントと同階層の`__tests__/`に配置する（例：`frontend/src/app/login/__tests__/LoginForm.test.tsx`）
- E2Eテスト（Playwright）は`frontend/e2e/pages/`（ページ単位）と`frontend/e2e/scenarios/`（ページ間シナリオ）に分けて配置する

## 遵守すべき規約

パッケージごとの詳細ルール（レイヤー依存関係、Lombokアノテーション、命名規則、ファクトリメソッド等）は `.claude/rules/` 配下のルールファイルに定義されている。対象パッケージのファイル編集時に自動適用される。

### 全パッケージ共通の規約

- パッケージ名：小文字、アンダースコア区切り（例：`type_handler`）
- クラス名：PascalCase
- 定数：`UPPER_SNAKE_CASE`
- ルートは`ApiRoutes`、デフォルト値は`Consts`、メッセージは`MessageConst`で一元管理する
- すべてのpublicクラスとメソッドに日本語のJavaDocコメントを記述
- 明示的なリンティング・フォーマットツールは未設定。既存のコードスタイルに従うこと

### 新機能追加の手順

機能を`{機能}`（`account`/`auth`/`common`/`inquiry`/`photo`のいずれか）として、以下の順に作成する（domainからpresentationへ、内側から外側へ向かう順）。

1. `domain/constant/ApiRoutes.java`にルートを定義
2. `presentation/request/{機能}/`と`presentation/response/{機能}/`にリクエスト/レスポンスDTOを作成
3. テーブルの追加やカラムの追加が必要な場合は`db/`の対象スキーマのフォルダ配下にSQLファイルを作成または既存ファイルを修正
   プライマリキーは`bigserial`型、日時は`timestamp with time zone`型で、すべてのカラムに必ず`NOT NULL`制約を付与する
4. テーブルを追加した場合
   1. `db/init`の`init-db.sh`と`init-test-db.sh`のSQL_FILESに追加したテーブルのSQLファイルを追加する
   2. `doc/database/README.md`に追加したテーブルを追記
   3. `doc/database/data-dictionary.md`に追加したカラムがなければ追記
   4. `infrastructure/persistence/entity/{機能}/`にエンティティを作成
5. カラムを追加・修正した場合
   1. `doc/database/data-dictionary.md`にカラムを追加・修正
   2. `infrastructure/persistence/entity/{機能}/`の該当テーブルのエンティティを追加・修正
6. `infrastructure/persistence/mapper/{機能}/`にMyBatisマッパーインターフェース、`resources/com/web/gallery/infrastructure/persistence/mapper/{機能}/`にXMLを作成
7. 3以外でテーブルと同等ではないプロパティや複数テーブルを結合してプロパティを取得する場合、または特殊な条件で抽出する場合は`infrastructure/persistence/dto/{機能}/`にDTOクラスを作成
8. `application/repository/{機能}/`にリポジトリインターフェース、`infrastructure/persistence/repository/{機能}/`に実装を作成
9.  レイヤー間転送用のモデルオブジェクトを`application/model/{機能}/`に作成
10. `application/service/{機能}/`にサービスインターフェース、`application/service/impl/{機能}/`に実装を作成
11. `presentation/controller/{機能}/`にコントローラーを作成
12. RequestからModelへの変換ロジックが必要な場合は`presentation/converter/{機能}/`にConverterクラスを作成する（詳細は`.claude/rules/converter.md`）。Controller自身に変換ロジックを持たせない
13. `backend/src/test/resources/sql/`にテスト用SQLフィクスチャを追加
14. `backend/src/test/resources/json/controller`にテスト用APIリクエストのjsonを作成
15. 既存パターンに従って`backend/src/test`にユニットテストと統合テスト、対象コンポーネントの`__tests__/`にfrontendのユニットテスト（Jest）、`frontend/e2e/pages/`または`frontend/e2e/scenarios/`にE2Eテストを追加
16. すべてのユニットテスト・統合テスト・E2Eテストを実行して、成功することを確認

### 重要事項

- ファイルアップロード上限は1ファイルあたり5MB（`application.yml`の`app.photo.maxFileSizeMb`。サーブレットレベルの`spring.servlet.multipart`は6MB）
- 写真の実体はS3（ローカル/E2Eはdocker-composeのMinIO）に保存し、DBの`photo_mst.image_file_path`にはサーバ生成の不透明オブジェクトキー（`{アカウント番号}/{写真番号}-{ランダム}.{拡張子}`。変更されうるアカウントIDではなく不変のアカウント番号をプレフィックスに使う）のみを保持する。クライアント送信のファイル名はキーに含めず、表示・重複判定用に`photo_mst.image_file_name`へ別途保持する。閲覧時はService層が署名付きURL（pre-signed GET URL）を発行して返す。ストレージ設定は各プロファイルの`application-{profile}.yml`の`app.s3.*`（環境変数`APP_S3_*` / `AWS_REGION`）で行う
- プロジェクトはTomcatデプロイ用のWARパッケージング（実行可能JARではない）
- `backend/build.gradle`のgroupは`com.official`、ベースパッケージは`com.web.gallery`
