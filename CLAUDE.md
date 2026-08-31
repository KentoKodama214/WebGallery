# CLAUDE.md - WebGallery AIアシスタントガイド

## プロジェクト概要

WebGalleryは、Spring Bootで構築されたフォトギャラリーWebアプリケーションです。ユーザーはアカウント登録、メタデータ/EXIFデータ付きの写真アップロード、フォトギャラリーの閲覧、写真のタグ付け、お気に入り管理が可能です。コードベースおよびすべてのドキュメント・コメントは日本語で記述されています。

## 技術スタック・プロジェクト構造

詳しくは、[README.md](README.md)を参照。

## ビルド・実行コマンド

```bash
# PostgreSQLデータベースの起動（アプリ実行前に必要）
docker-compose up -d

# backendのビルド
./backend/gradlew -p backend build

# backendのテストのみ実行
./backend/gradlew -p backend test

# backendアプリケーションの実行
./backend/gradlew -p backend bootRun --args='--spring.profiles.active=local'

# WARファイルのビルド
./backend/gradlew -p backend war

# クリーンビルド
./backend/gradlew -p backend clean build

# frontendの依存パッケージインストール・開発サーバー起動・ビルド・lint（just経由）
just setup
just dev
just build
just lint

# E2Eテスト一括実行（DB・backendを自動起動）
just e2e
```

## アーキテクチャ

### レイヤードアーキテクチャ (Controller -> Service -> Repository -> Mapper)

1. **Controller層** (`controller/`) - RESTコントローラー。`/model/`のModelオブジェクトでService層と転送する
2. **Service層** (`service/` + `service/impl/`) - ビジネスロジックとバリデーション。`/model/`のModelオブジェクトでController層・Repository層と転送する
3. **Repository層** (`repository/` + `repository/impl/`) - データベースアクセス。`/model/`でService層と、`/entity/`や`/dto/`でMapper層と転送する
4. **MyBatis Mapper層** (`mapper/`) - SQLは`resources/com/web/gallery/mapper/*.xml`のXMLファイルで定義

上記に加え、`domain/`にはプロパティ単位の値オブジェクト（`record`）が定義されており、Model・Repository・Serviceのメソッド引数・返り値として利用される。単一のビジネスルールを判定するドメインサービスは`policy/`に配置し、Service層からのみ利用する。また、管理者権限チェックには`annotation/`のカスタムアノテーションと`aspect/`のAOPを使用する。写真登録・削除などのドメインイベントとそのリスナーは`event/`に配置し、通知やログ集計等の副次的な処理をService層のビジネスロジックから疎結合にする。`@Scheduled`による定期実行タスク（期限切れリフレッシュトークンの削除等）は`scheduler/`に配置し、Service層のメソッドを呼び出す（有効化は`config/SchedulingConfig`）。JWT生成・検証やセッション取得などの技術的ユーティリティは`helper/`に配置する。

書き込みユースケース（登録・更新・削除）で複数テーブルにまたがる整合性・ライフサイクルの管理が必要な場合、`aggregate/`に集約ルートクラスを定義し、Service層とRepository層の間に位置づける（例：`Photo`集約）。詳細は`.claude/rules/aggregate.md`を参照。

各レイヤーの詳細なルール（依存関係、命名規則、アノテーション規約等）は `.claude/rules/` 配下のルールファイルを参照。

### セキュリティモデル

詳しくは、[セキュリティ](doc/architecture/security.md)を参照。

### ユーザー権限レベル・エラーコード

API仕様はアプリケーション起動後、Scalar UI（`/scalar`）またはOpenAPI JSON（`/v3/api-docs`）で確認できる。

### データベーススキーマ

詳しくは、[データベース定義書](doc/database/README.md)を参照。テーブル定義・ER図はSchemaSpyで自動生成する。

## テスト規約

テストクラスの種別・命名規則・配置ルールは `.claude/rules/unit-test.md` と `.claude/rules/integration-test.md` を参照。

### テストデータベース

- 専用データベース：`web_gallery_test`（`application-test.yml`で設定）
- テスト用SQLフィクスチャは`backend/src/test/resources/sql/`にレイヤー別に整理
- 統合テストの実行にはDocker PostgreSQLの起動が必要

### テスト実行

```bash
# backendの全テスト実行
./backend/gradlew -p backend test

# backendの特定のテストクラスを実行
./backend/gradlew -p backend test --tests "com.web.gallery.service.impl.PhotoServiceImplTest"

# frontendの単体テスト（Jest）
cd frontend && pnpm test

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

1. `ApiRoutes.java`にルートを定義
2. `controller/request/`と`controller/response/`にリクエスト/レスポンスDTOを作成
3. テーブルの追加やカラムの追加が必要な場合は`db/`の対象スキーマのフォルダ配下にSQLファイルを作成または既存ファイルを修正
   プライマリキーは`bigserial`型、日時は`timestamp with time zone`型で、すべてのカラムに必ず`NOT NULL`制約を付与する
4. テーブルを追加した場合
   1. `db/init`の`init-db.sh`と`init-test-db.sh`のSQL_FILESに追加したテーブルのSQLファイルを追加する
   2. `doc/database/README.md`に追加したテーブルを追記
   3. `doc/database/data-dictionary.md`に追加したカラムがなければ追記
   4. `entity/`にエンティティを作成
5. カラムを追加・修正した場合
   1. `doc/database/data-dictionary.md`にカラムを追加・修正
   2. `entity/`の該当テーブルのエンティティを追加・修正
6. `mapper/`にMyBatisマッパーインターフェース、`resources/com/web/gallery/mapper/`にXMLを作成
7. 3以外でテーブルと同等ではないプロパティや複数テーブルを結合してプロパティを取得する場合、または特殊な条件で抽出する場合は`dto/`にDTOクラスを作成
8. `repository/`と`repository/impl/`にリポジトリインターフェースと実装を作成
9.  レイヤー間転送用のモデルオブジェクトを`model/`に作成
10. `service/`と`service/impl/`にサービスインターフェースと実装を作成
11. `controller/`にコントローラーを作成
12. `backend/src/test/resources/sql/`にテスト用SQLフィクスチャを追加
13. `backend/src/test/resources/json/controller`にテスト用APIリクエストのjsonを作成
14. 既存パターンに従って`backend/src/test`にユニットテストと統合テスト、対象コンポーネントの`__tests__/`にfrontendのユニットテスト（Jest）、`frontend/e2e/pages/`または`frontend/e2e/scenarios/`にE2Eテストを追加
15. すべてのユニットテスト・統合テスト・E2Eテストを実行して、成功することを確認

### 重要事項

- ファイルアップロード上限は1ファイルあたり5MB（`application.yml`の`app.photo.maxFileSizeMb`。サーブレットレベルの`spring.servlet.multipart`は6MB）
- 写真の出力パスは各プロファイルの`application-{profile}.yml`（例：`application-local.yml`）の`app.photo.outputPath`（環境変数`OUTPUT_PATH`）で設定する
- プロジェクトはTomcatデプロイ用のWARパッケージング（実行可能JARではない）
- `backend/build.gradle`のgroupは`com.official`、ベースパッケージは`com.web.gallery`
