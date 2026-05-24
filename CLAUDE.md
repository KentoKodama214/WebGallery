# CLAUDE.md - WebGallery AIアシスタントガイド

## プロジェクト概要

WebGalleryは、Spring Bootで構築されたフォトギャラリーWebアプリケーションです。ユーザーはアカウント登録、メタデータ/EXIFデータ付きの写真アップロード、フォトギャラリーの閲覧、写真のタグ付け、お気に入り管理が可能です。コードベースおよびすべてのドキュメント・コメントは日本語で記述されています。

## 技術スタック・プロジェクト構造

詳しくは、[README.md](README.md)を参照。

## ビルド・実行コマンド

```bash
# プロジェクトのビルド
./backend/gradlew -p backend build

# テストのみ実行
./backend/gradlew -p backend test

# アプリケーションの実行
./backend/gradlew -p backend bootRun

# WARファイルのビルド
./backend/gradlew -p backend war

# クリーンビルド
./backend/gradlew -p backend clean build

# PostgreSQLデータベースの起動（アプリ実行前に必要）
docker-compose up -d
```

## アーキテクチャ

### レイヤードアーキテクチャ (Controller -> Service -> Repository -> Mapper)

1. **Controller層** (`controller/`)
   - RESTコントローラーはJSONレスポンスを返す（REST APIのみ、サーバーサイドレンダリングなし）
   - `CommonRestControllerAdvice`による例外ハンドリング
   - `controller/request/`のリクエストDTOに`@Valid`を使用したリクエストバリデーション
   - すべてのAPIルートは`constant/ApiRoutes.java`に一元定義

2. **Service層** (`service/` + `service/impl/`)
   - インターフェースベース設計：`service/`にインターフェース、`service/impl/`に実装
   - 必要に応じて`@Service`と`@Transactional`を付与
   - ビジネスロジックとバリデーションはここに配置

3. **Repository層** (`repository/` + `repository/impl/`)
   - インターフェースベース設計：`repository/`にインターフェース、`repository/impl/`に実装
   - `@Repository`を付与
   - MyBatisマッパーへのデータベースアクセスを委譲
   - `FileRepository`がファイルシステム操作を担当

4. **MyBatis Mapper層** (`mapper/`)
   - Javaインターフェースでメソッドシグネチャを定義
   - SQLは`resources/com/web/gallery/mapper/*.xml`のXMLファイルで定義
   - `type_handler/`の列挙型からDB変換用カスタムタイプハンドラー

### 主要パターン

- **Interface + Impl**: サービスとリポジトリは常にインターフェースと別の`impl/`実装を持つ
- **Request/Response DTO**: コントローラーは専用のリクエスト/レスポンスオブジェクトを使用し、エンティティを直接使用しない
- **Modelオブジェクト**: サービス層とリポジトリ層間の転送オブジェクトとして使用
- **ModelMapper**: エンティティ、モデル、DTO間のマッピングに使用
- **Lombok**: すべてのエンティティ、モデル、DTOで`@Getter`、`@Setter`、`@Builder`、`@AllArgsConstructor`等を使用
- **定数の一元管理**: ルートは`ApiRoutes`、デフォルト値は`Consts`、メッセージは`MessageConst`

### セキュリティモデル

詳しくは、[セキュリティ](doc/architecture/security.md)を参照。

### ユーザー権限レベル・エラーコード

API仕様はアプリケーション起動後、Scalar UI（`/scalar`）またはOpenAPI JSON（`/v3/api-docs`）で確認できる。

### データベーススキーマ

SchemaSpyで自動生成したドキュメントを参照。生成手順は以下の通り。

```bash
docker compose up -d
docker compose --profile docs run --rm schemaspy
```

生成後、`doc/database/common/index.html`（commonスキーマ）と`doc/database/photo/index.html`（photoスキーマ）をブラウザで開くと、ER図・テーブル定義・データディクショナリを確認できる。

## テスト規約

### テスト種別

- **ユニットテスト**: `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用
- **統合テスト**: クラス名に`IntegrationTest`サフィックスを付与し、`integration/`サブディレクトリに配置
  - `@SpringBootTest`と`@ActiveProfiles("test")`を使用
  - `@Transactional`による自動ロールバック
  - `@Sql("/sql/...")`アノテーションでテストフィクスチャデータを読み込み

### テストデータベース

- 専用データベース：`web_gallery_test`（`application-test.yml`で設定）
- テスト用SQLフィクスチャは`backend/src/test/resources/sql/`にレイヤー別に整理
- 統合テストの実行にはDocker PostgreSQLの起動が必要

### テスト実行

```bash
# 全テスト実行
./backend/gradlew -p backend test

# 特定のテストクラスを実行
./backend/gradlew -p backend test --tests "com.web.gallery.service.impl.PhotoServiceImplTest"
```

## 遵守すべき規約

### 命名規則

- パッケージ名：小文字、アンダースコア区切り（例：`type_handler`）
- クラス名：PascalCaseで説明的なサフィックスを付与（`Controller`、`RestController`、`Service`、`ServiceImpl`、`Repository`、`RepositoryImpl`、`Mapper`）
- 統合テストクラス：`IntegrationTest`サフィックスを付与
- 定数：`UPPER_SNAKE_CASE`

### コードスタイル

- すべてのpublicクラスとメソッドに日本語のJavaDocコメントを記述
- Lombokアノテーションでボイラープレートを削減（`@Builder`、`@Getter`、`@Setter`を優先）
- Entityクラスには `@Data` と `@Builder` のみを使用する（`@NoArgsConstructor` や `@AllArgsConstructor` は使用しない）
- サービスとリポジトリはインターフェースベース設計
- Responseクラスにはファクトリメソッドを定義し、レスポンス生成ロジックをResponseクラス側に集約する。Controllerではファクトリメソッドを呼び出すだけにする
  - `static from(Model)`: Model/Entity→Responseの変換に使用する
  - `static of(...)`: 固定値や少数のパラメータから直接生成する場合に使用する
- 明示的なリンティング・フォーマットツールは未設定。既存のコードスタイルに従うこと

### 新機能追加の手順

1. `ApiRoutes.java`にルートを定義
2. `controller/request/`と`controller/response/`にリクエスト/レスポンスDTOを作成
3. 新しいテーブルが必要な場合は`entity/`にエンティティを作成
4. レイヤー間転送用のモデルオブジェクトを`model/`に作成
5. `mapper/`にMyBatisマッパーインターフェース、`resources/com/web/gallery/mapper/`にXMLを作成
6. `repository/`と`repository/impl/`にリポジトリインターフェースと実装を作成
7. `service/`と`service/impl/`にサービスインターフェースと実装を作成
8. `controller/`にコントローラーを作成
9. 既存パターンに従ってユニットテストと統合テストを追加
10. `backend/src/test/resources/sql/`にテスト用SQLフィクスチャを追加

### 重要事項

- パッケージ名`enumuration`（`enumeration`ではない）はプロジェクトの意図的な規約であり、リネームしないこと
- ファイルアップロード上限は1ファイルあたり5MB（サーブレットレベルでは6MB）
- 写真の出力パスは`backend/src/main/resources/application.yml`の`app.photo.outputPath`で設定可能
- プロジェクトはTomcatデプロイ用のWARパッケージング（実行可能JARではない）
- `backend/build.gradle`のgroupは`com.official`、ベースパッケージは`com.web.gallery`
