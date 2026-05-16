# CLAUDE.md - WebGallary AIアシスタントガイド

## プロジェクト概要

WebGallaryは、Spring Bootで構築されたフォトギャラリーWebアプリケーションです。ユーザーはアカウント登録、メタデータ/EXIFデータ付きの写真アップロード、フォトギャラリーの閲覧、写真のタグ付け、お気に入り管理が可能です。コードベースおよびすべてのドキュメント・コメントは日本語で記述されています。

## 技術スタック

| コンポーネント   | 技術                              |
|-----------------|-----------------------------------|
| 言語            | Java 21                           |
| ビルドツール     | Gradle 8.14（wrapper同梱）         |
| フレームワーク   | Spring Boot 4.0.6                 |
| セキュリティ     | Spring Security 7.0.5 (BCrypt + JWT) |
| フロントエンド   | Next.js (React, TypeScript)       |
| ORM             | MyBatis 4.0.1                     |
| データベース     | PostgreSQL (driver 42.7.11)       |
| コード生成      | Lombok 1.18.42                    |
| オブジェクトマッピング | ModelMapper 3.2.6            |
| JWT             | jjwt 0.13.0                       |
| テスト          | JUnit Jupiter 6.0.3, Mockito 5.20.0|
| パッケージング   | WAR (Tomcatデプロイ)              |

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

## プロジェクト構造

```
WebGallary/
├── docker-compose.yml              # PostgreSQLコンテナ設定
├── docker/db/                      # DB用Dockerfile
├── db/                             # データベース初期化スクリプト
│   ├── init/init-db.sh             # DB初期化エントリーポイント
│   ├── common/                     # 共通スキーマSQL (account, kbn_mst, location_mst)
│   └── photo/                      # 写真スキーマSQL (photo_mst, photo_tag_mst, photo_favorite)
├── scripts/                        # CI/CDスクリプト
│   └── check-architecture.sh       # アーキテクチャ違反チェッカー
├── frontend/                       # Next.jsフロントエンド (React)
│   ├── package.json                # 依存関係とスクリプト
│   ├── next.config.ts              # Next.js設定
│   ├── tsconfig.json               # TypeScript設定
│   ├── eslint.config.mjs           # ESLint設定
│   ├── jest.config.js              # Jestテスト設定
│   ├── playwright.config.ts        # Playwright E2Eテスト設定
│   ├── public/image/               # 静的画像アセット
│   ├── e2e/                        # Playwright E2Eテスト
│   └── src/
│       ├── app/                    # Next.js App Routerページ
│       │   ├── layout.tsx          # ルートレイアウト
│       │   ├── page.tsx            # ホームページ
│       │   ├── globals.css         # グローバルスタイル
│       │   ├── login/              # ログインページ
│       │   ├── register/           # アカウント登録ページ
│       │   ├── account_list/       # アカウント一覧ページ
│       │   ├── [accountId]/
│       │   │   └── account_setting/  # アカウント設定ページ
│       │   ├── photo/[photoAccountId]/
│       │   │   ├── photo_list/     # フォトギャラリーページ
│       │   │   ├── photo_detail/   # 写真詳細ページ
│       │   │   └── photo_setting/  # 写真アップロード・編集ページ
│       │   └── api/v1/             # Next.js APIルート（プロキシ）
│       ├── components/layout/      # 共有レイアウトコンポーネント (Header, Footer, Navigation)
│       └── lib/
│           ├── api/client.ts       # バックエンドAPIクライアント
│           └── auth/AuthProvider.tsx  # 認証コンテキストプロバイダー
├── backend/                        # Spring Bootバックエンド (REST API)
│   ├── build.gradle                # Gradleビルド設定
│   ├── settings.gradle             # Gradle設定
│   ├── gradlew / gradlew.bat      # Gradleラッパースクリプト
│   ├── gradle/                     # GradleラッパーJAR
│   ├── config/checkstyle/          # Checkstyle設定
│   ├── set-env.sh                  # 環境変数設定スクリプト
│   └── src/
│       ├── main/
│       │   ├── java/com/web/gallary/
│       │   │   ├── WebGallaryApplication.java   # Bootメインクラス
│       │   │   ├── ServletInitializer.java      # WARデプロイ初期化クラス
│       │   │   ├── AccountPrincipal.java        # Spring Security UserDetails
│       │   │   ├── config/                      # 設定クラス (Security, JWT, CORS等)
│       │   │   ├── constant/                    # 定数 (ApiRoutes, Consts, MessageConst)
│       │   │   ├── controller/                  # RESTコントローラー (JSON APIのみ)
│       │   │   │   ├── request/                 # リクエストDTO
│       │   │   │   └── response/                # レスポンスDTO
│       │   │   ├── dto/                         # データ転送オブジェクト (マッパー層)
│       │   │   ├── entity/                      # データベースエンティティ
│       │   │   ├── enumuration/                 # 列挙型 (パッケージ名のtypoは意図的)
│       │   │   ├── exception/                   # カスタム例外クラス
│       │   │   ├── helper/                      # ヘルパーユーティリティ (Session, Kbn, JwtTokenProvider)
│       │   │   ├── mapper/                      # MyBatisマッパーインターフェース
│       │   │   ├── model/                       # 転送・ビジネスモデルオブジェクト
│       │   │   ├── repository/                  # リポジトリインターフェース
│       │   │   │   └── impl/                    # リポジトリ実装
│       │   │   ├── service/                     # サービスインターフェース
│       │   │   │   └── impl/                    # サービス実装
│       │   │   └── type_handler/                # MyBatis列挙型タイプハンドラー
│       │   └── resources/
│       │       ├── application.yml              # アプリケーション設定
│       │       ├── application-*.yml            # プロファイル別設定 (local, development, prod)
│       │       ├── messages.properties          # メッセージ文字列
│       │       └── com/web/gallary/mapper/      # MyBatis XMLマッパーファイル
│       └── test/
│           ├── java/com/web/gallary/            # テストクラス（main構造のミラー）
│           │   ├── controller/                  # RESTコントローラーユニットテスト
│           │   │   └── integration/             # RESTコントローラー統合テスト
│           │   ├── mapper/                      # マッパーユニットテスト
│           │   ├── repository/impl/
│           │   │   └── integration/             # リポジトリ統合テスト
│           │   ├── service/impl/
│           │   │   └── integration/             # サービス統合テスト
│           │   └── helper/                      # ヘルパーユニットテスト
│           └── resources/
│               ├── application-test.yml         # テスト設定
│               ├── json/                        # テスト用JSONフィクスチャ
│               │   └── controller/              # コントローラーテスト用リクエストボディ
│               └── sql/                         # テスト用SQLフィクスチャ
│                   ├── common/                  # 共有テストデータ
│                   ├── controller/              # コントローラーテストデータ
│                   ├── mapper/                  # マッパーテストデータ
│                   ├── repository/              # リポジトリテストデータ
│                   └── service/                 # サービステストデータ
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
   - SQLは`resources/com/web/gallary/mapper/*.xml`のXMLファイルで定義
   - `type_handler/`の列挙型からDB変換用カスタムタイプハンドラー

### 主要パターン

- **Interface + Impl**: サービスとリポジトリは常にインターフェースと別の`impl/`実装を持つ
- **Request/Response DTO**: コントローラーは専用のリクエスト/レスポンスオブジェクトを使用し、エンティティを直接使用しない
- **Modelオブジェクト**: サービス層とリポジトリ層間の転送オブジェクトとして使用
- **ModelMapper**: エンティティ、モデル、DTO間のマッピングに使用
- **Lombok**: すべてのエンティティ、モデル、DTOで`@Getter`、`@Setter`、`@Builder`、`@AllArgsConstructor`等を使用
- **定数の一元管理**: ルートは`ApiRoutes`、デフォルト値は`Consts`、メッセージは`MessageConst`

### セキュリティモデル

- Spring SecurityによるJWT認証（ステートレス）
- BCryptパスワードエンコーディング
- `JwtAuthenticationFilter`がBearerトークンを検証しSecurityContextを設定
- `/api/**`配下のAPIエンドポイントは保護対象。認証・アカウント・都道府県エンドポイントは公開
- 写真の閲覧は公開アクセス可能。編集とお気に入りは認証が必要

### ユーザー権限レベル

| レベル         | 説明                                | 写真アップロード上限 |
|---------------|--------------------------------------|---------------------|
| MINI          | 基本ユーザー                          | 10枚               |
| NORMAL        | 標準ユーザー                          | 1,000枚            |
| SPECIAL       | プレミアムユーザー                     | 無制限              |
| ADMINISTRATOR | サイト管理者                          | 無制限              |

### エラーコード規約

- `E-C-xxxx` - 共通・アカウントエラー（例：`E-C-0001` = アカウント登録失敗）
- `E-P-xxxx` - 写真関連エラー（例：`E-P-0001` = 写真登録失敗）
- すべてのエラーコードは`enumuration/ErrorEnum.java`で定義し、メッセージは`MessageConst`に記載

### データベーススキーマ

PostgreSQLの2つのスキーマ：
- **`common`スキーマ**: `account`、`kbn_mst`（区分マスタ）、`location_mst`
- **`photo`スキーマ**: `photo_mst`（写真メタデータ + EXIF）、`photo_tag_mst`、`photo_favorite`

初期化スクリプトは`db/`ディレクトリに配置し、`db/init/init-db.sh`で実行される。

## テスト規約

### テスト種別

- **ユニットテスト**: `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用
- **統合テスト**: クラス名に`IntegrationTest`サフィックスを付与し、`integration/`サブディレクトリに配置
  - `@SpringBootTest`と`@ActiveProfiles("test")`を使用
  - `@Transactional`による自動ロールバック
  - `@Sql("/sql/...")`アノテーションでテストフィクスチャデータを読み込み

### テストデータベース

- 専用データベース：`web_gallary_test`（`application-test.yml`で設定）
- テスト用SQLフィクスチャは`backend/src/test/resources/sql/`にレイヤー別に整理
- 統合テストの実行にはDocker PostgreSQLの起動が必要

### テスト実行

```bash
# 全テスト実行
./backend/gradlew -p backend test

# 特定のテストクラスを実行
./backend/gradlew -p backend test --tests "com.web.gallary.service.impl.PhotoServiceImplTest"
```

## 開発環境セットアップ

1. DockerでPostgreSQLを起動：
   ```bash
   docker-compose up -d
   ```
2. `db/`のスクリプトによりデータベースが自動初期化される
3. アプリケーションを実行：
   ```bash
   ./backend/gradlew -p backend bootRun
   ```
4. バックエンドは`http://localhost:8080`、フロントエンドは`http://localhost:3000`でアクセス

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
- Responseクラスには `static from()` ファクトリメソッドを定義し、Model/Entity→Responseの変換ロジックをResponseクラス側に集約する。Controllerでは `from()` を呼び出すだけにする
- 明示的なリンティング・フォーマットツールは未設定。既存のコードスタイルに従うこと

### 新機能追加の手順

1. `ApiRoutes.java`にルートを定義
2. `controller/request/`と`controller/response/`にリクエスト/レスポンスDTOを作成
3. 新しいテーブルが必要な場合は`entity/`にエンティティを作成
4. レイヤー間転送用のモデルオブジェクトを`model/`に作成
5. `mapper/`にMyBatisマッパーインターフェース、`resources/com/web/gallary/mapper/`にXMLを作成
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
- `backend/build.gradle`のgroupは`com.official`、ベースパッケージは`com.web.gallary`
