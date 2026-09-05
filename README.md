# WebGallery

写真ギャラリーWebアプリケーションです。ユーザー登録・写真のアップロード・EXIF情報の管理・タグ付け・お気に入り機能などを備えています。

## 目次

- [WebGallery](#webgallery)
  - [目次](#目次)
  - [主な機能](#主な機能)
    - [ユーザー権限](#ユーザー権限)
  - [技術スタック](#技術スタック)
    - [バックエンド](#バックエンド)
    - [フロントエンド](#フロントエンド)
  - [前提条件](#前提条件)
  - [セットアップ](#セットアップ)
    - [0. miseの信頼](#0-miseの信頼)
    - [1. データベースの起動](#1-データベースの起動)
    - [2. 環境変数の設定](#2-環境変数の設定)
    - [3. フロントエンドのセットアップ](#3-フロントエンドのセットアップ)
    - [4. アプリケーションの起動](#4-アプリケーションの起動)
  - [ビルド・テスト](#ビルドテスト)
    - [E2Eテストの実行（要Docker）](#e2eテストの実行要docker)
  - [アーキテクチャ](#アーキテクチャ)
  - [データベース構成](#データベース構成)
  - [コード構造の可視化（JIG / Spring Modulith）](#コード構造の可視化jig--spring-modulith)
  - [画面設計](#画面設計)
  - [API](#api)
  - [プロジェクト構成](#プロジェクト構成)

## 主な機能

- **アカウント管理** - ユーザー登録・ログイン・プロフィール編集・アカウント削除
- **写真管理** - 写真のアップロード・編集・削除（EXIF情報の自動取得に対応）
- **タグ機能** - 写真への日本語・英語タグ付け
- **お気に入り** - 他ユーザーの写真をお気に入り登録
- **写真一覧** - フィルタリング（方向・タグ）やソート（撮影日・お気に入り数・季節）に対応
- **権限管理** - 4段階のユーザー権限によるアップロード枚数制限
- **管理者機能** - アカウント一覧管理・アカウントロック/ロック解除

### ユーザー権限

| 権限 | 説明 | アップロード上限 |
|------|------|------------------|
| MINI | ミニユーザー | 10枚 |
| NORMAL | 一般ユーザー | 1,000枚 |
| SPECIAL | 特別ユーザー | 無制限 |
| ADMINISTRATOR | 管理者 | 無制限 |

## 技術スタック

### バックエンド

| コンポーネント | 技術 |
|----------------|------|
| 言語 | Java 21 |
| ビルドツール | Gradle 8.14 |
| フレームワーク | Spring Boot 4.0.6 |
| セキュリティ | Spring Security 7.0.5（BCrypt + JWT） |
| ORM | MyBatis 4.0.1 |
| データベース | PostgreSQL（ドライバ 42.7.11） |
| コード生成 | Lombok 1.18.42 |
| オブジェクトマッピング | ModelMapper 3.2.6 |
| JWT | jjwt 0.13.0 |
| テスト | JUnit Jupiter 6.0.3 / Mockito 5.20.0 |
| パッケージング | WAR（Tomcatデプロイ） |

### フロントエンド

| コンポーネント | 技術 |
|----------------|------|
| 言語 | TypeScript |
| フレームワーク | Next.js 16 (App Router) |
| UIライブラリ | React 19 |
| スタイリング | Tailwind CSS 4 |
| パッケージマネージャー | pnpm（Corepack経由） |
| Node.jsバージョン管理 | mise |
| タスクランナー | just |

## 前提条件

- Java 21
- Docker / Docker Compose
- [mise](https://mise.jdx.dev/)（Node.jsバージョン管理）
- [just](https://github.com/casey/just)（タスクランナー）

```bash
# mise / just のインストール（macOS）
brew install mise just
```

miseのインストール後、シェルにmiseを有効化する設定を追加してください。

```bash
# bashの場合
echo 'eval "$(mise activate bash)"' >> ~/.bashrc

# zshの場合
echo 'eval "$(mise activate zsh)"' >> ~/.zshrc
```

設定後、シェルを再起動するか `source ~/.bashrc`（または `source ~/.zshrc`）を実行してください。

## セットアップ

### 0. miseの信頼
クローン後、以下のようなメッセージが表示される。
```bash
mise ERROR error parsing config file: ~/MainDevelopment/WebGallery/.mise.toml
mise ERROR Config files in ~/MainDevelopment/WebGallery/.mise.toml are not trusted.
Trust them with `mise trust`. See https://mise.en.dev/cli/trust.html for more information.
mise ERROR Run with --verbose or MISE_VERBOSE=1 for more information
```
その解消のため、プロジェクトルートで以下のコマンドを実行してください。
```bash
mise trust
```


### 1. データベースの起動

```bash
just db-up
```

開発用データベース（`web_gallery`、ポート5432）とテスト用データベース（`web_gallery_test`、ポート5433）が起動します。データベースの初期化は `db/` 配下のSQLスクリプトにより自動的に行われます。

### 2. 環境変数の設定

```bash
./backend/set-env.sh
```

このスクリプトはシェルの設定ファイル（`~/.zshrc` / `~/.bashrc`）に `export` 文を追記します。設定後はシェルを再起動するか `source` してください。

#### バックエンドの環境変数

バックエンドは以下の環境変数を参照します。`local` プロファイルでは DB 接続情報などにデフォルト値がありますが、**`JWT_SECRET` はデフォルト値を持たず、未設定だと起動に失敗します**（既知のシークレットが混入すると JWT を偽造できるため）。`set-env.sh` は未入力時に安全なランダム値を生成して設定します。

| 変数名 | 用途 | `local` での挙動 |
| --- | --- | --- |
| `JWT_SECRET` | JWT アクセストークンの署名鍵（**256bit / 32バイト以上必須**） | **必須。未設定なら起動失敗** |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | DB 接続情報 | `jdbc:postgresql://localhost:5432/web_gallery` / `postgres` / `postgres` |
| `OUTPUT_PATH` | 写真の出力先パス | `https://localhost:8080/image/` |
| `MINI_USER_UPPER_LIMIT` / `NORMAL_USER_UPPER_LIMIT` | 権限別の写真登録上限 | `10` / `1000` |
| `FRONTEND_ORIGIN` | CORS 許可オリジン | `http://localhost:3000` |

> **IntelliJ IDEA で起動する場合**
> Dock やランチャーから起動した IntelliJ はシェルの `export` を引き継がないため、`JWT_SECRET` を渡す必要があります。共有の実行構成 `backend/.run/WebGalleryApplication_local.run.xml`（実行構成名「WebGalleryApplication (local)」、プロファイル `local` ＋ ローカル用 `JWT_SECRET` を設定済み）を選択して実行してください。独自の実行構成を使う場合は「Environment variables」に `JWT_SECRET` を追加してください。

#### フロントエンドの環境変数

フロントエンド（`frontend/`）は以下の環境変数を参照する（`frontend/.env.local` 等に設定。未設定でも起動は可能）。

| 変数名 | 用途 | 未設定時の挙動 |
| --- | --- | --- |
| `BACKEND_URL` | APIプロキシ（`/api/*`）の転送先バックエンドオリジン | `http://localhost:8080` |
| `NEXT_PUBLIC_API_BASE_URL` | 別オリジンのバックエンドを直接叩く場合のベースURL | 同一オリジンの `/api` プロキシを使用 |
| `NEXT_PUBLIC_IMAGE_BASE_URL` | 写真の配信元オリジン（例: `https://cdn.example.com/`）。CSP の `img-src` と `sanitizeImageUrl` の許可オリジンに反映される | **外部ホストからの画像読み込みを一切許可しない**（`img-src 'self' data: blob:`）。外部の画像配信元を使う構成では必ず設定すること |

##### 構成上の注意

- **アップロード写真の配信経路**: フロントの API プロキシ（`src/app/api/[...path]/route.ts`）が中継するのは `/api/*` のみで、
  バックエンドの画像配信パス（`/image/*`、`app.photo.outputPath`）は中継しない。`public/image/` には UI アセットのみが置かれる。
  したがって実写真を表示するには次のいずれかが必要:
  - `NEXT_PUBLIC_IMAGE_BASE_URL` に画像配信元オリジンを設定する（推奨。CDN / バックエンドの `/image/` 等）。
  - フロントの前段のリバースプロキシ（nginx 等）で `/image/*` をバックエンドへルーティングする。
  ローカル開発では backend の `application-local.yml` が `app.photo.outputPath: https://localhost:8080/image/` を使うため、
  `NEXT_PUBLIC_IMAGE_BASE_URL=https://localhost:8080/` を `frontend/.env.local` に設定する。
- **アップロードのボディサイズ / 同時接続**: `/api/*` プロキシはリクエストボディを最大 6MB までメモリにバッファしてから
  バックエンドへ転送する（1 リクエストあたりは 6MB で頭打ちだが同時実行数の上限は持たない）。本番では前段の
  リバースプロキシで `client_max_body_size`（6MB 程度）と同時接続数の制限をかけること。
- **CSRF 対策のスコープ**: `/api/*` プロキシの Origin / `Sec-Fetch-Site` 検証は同一オリジンプロキシ経由でのみ機能する。
  `NEXT_PUBLIC_API_BASE_URL` で別オリジンのバックエンドを直接叩く構成にした場合、この検証はバイパスされるため、
  バックエンド側の CSRF 対策（SameSite Cookie 等）に完全に依存する。

### 3. フロントエンドのセットアップ

```bash
just front-setup
```

このコマンドで以下が実行されます:
1. miseによるNode.js（`.mise.toml`で定義されたバージョン）のインストール
2. Corepack経由でのpnpmの有効化
3. `frontend/` の依存パッケージインストール

### 4. アプリケーションの起動

```bash
# バックエンド
just backend-run

# フロントエンド
just front-run
```

バックエンドは `http://localhost:8080`、フロントエンドは `http://localhost:3000` でアクセスできます。

## ビルド・テスト

```bash
# ビルド
just backend-build

# 単体テストの実行
just backend-unitTest

# 統合テストの実行（要Docker）
just backend-integrationTest

# 全テストの実行（要Docker）
just backend-allTest

# カバレッジレポートの生成（要Docker）
#   単体のみ:   build/reports/jacoco/jacocoUnitReport/html/index.html
#   結合のみ:   build/reports/jacoco/jacocoIntegrationReport/html/index.html
#   単体＋結合: build/reports/jacoco/jacocoAggregateReport/html/index.html
./backend/gradlew -p backend unitTest integrationTest jacocoUnitReport jacocoIntegrationReport jacocoAggregateReport

# WARファイルの生成
./backend/gradlew -p backend war

# クリーンビルド
just backend-clean-build
```

```bash
# フロントエンドのビルド
just front-build

# フロントエンドのlint
just lint

# フロントエンドの単体テスト（Jest）
just front-test
```

### E2Eテストの実行（要Docker）

フロントエンドの画面操作を通したE2Eテスト（Playwright）を一括実行できます。DB（docker-compose）とバックエンドが未起動の場合は自動的に起動し、終了後に自動起動したバックエンドは停止します。

```bash
just e2e
# もしくは
./scripts/e2e.sh
```

DB・バックエンドを自分で起動済みの場合は、フロントエンドのみで直接実行できます。

```bash
cd frontend && pnpm test:e2e
```

## アーキテクチャ

アーキテクチャの詳細は [`doc/architecture/`](doc/architecture/) を参照してください。

## データベース構成

データベースドキュメント（ER図・テーブル定義・データディクショナリ）はSchemaSpyで自動生成します。

```bash
# DBが起動している状態で実行
just db-doc
```

生成後、`doc/database/common/index.html`（commonスキーマ）と`doc/database/photo/index.html`（photoスキーマ）をブラウザで開いてください。

## コード構造の可視化（JIG / Spring Modulith）

[JIG（Java Integration Graph）](https://github.com/dddjava/jig)によるコード構造のドキュメントを自動生成できます。パッケージ関連図・ユースケース図・ドメインモデルなどが出力されます。

```bash
just jig
```

生成後、`backend/build/jig/index.html` をブラウザで開いてください。

また、Spring Modulithによるモジュール構成図（PlantUML）とモジュールキャンバス（AsciiDoc）を生成できます。

```bash
just modulith-doc
```

生成後、`doc/modulith/` 配下のファイルを参照してください。

## 画面設計

画面遷移図・画面一覧の詳細は [`doc/view/`](doc/view/) を参照してください。

## API

REST APIの詳細は `http://localhost:8080/scalar` を参照してください。

## プロジェクト構成

```
WebGallery/
├── .mise.toml                     # Node.jsバージョン定義（mise）
├── justfile                       # タスクランナー定義（just）
├── .github
│   ├── ISSUE_TEMPLATE
│   │   ├── テストissue.md          # テスト用Issueのテンプレート
│   │   └── 開発issue.md            # 開発用Issueのテンプレート
│   └── workflows
│       ├── checkstyle.yml          # CheckstyleによるJavadocチェックのGithub Action
│       └── test.yml                # テスト実行・カバレッジレポート（JaCoCo）のGithub Action
├── docker-compose.yml
├── docker/
│   ├── db/                        # DBイメージ用Dockerfile
│   └── schemaspy/                 # SchemaSpy設定ファイル
├── db/                             # DB初期化スクリプト
│   ├── init/                       # 初期化エントリポイント
│   ├── common/                     # commonスキーマSQL
│   └── photo/                      # photoスキーマSQL
├── doc/
│   ├── architecture/               # アーキテクチャ設計書
│   ├── database/                   # データベースドキュメント（SchemaSpy自動生成）
│   ├── modulith/                   # モジュールドキュメント（Spring Modulith自動生成）
│   └── view/                       # 画面設計書
├── scripts/
│   └── e2e.sh                      # E2Eテスト一括実行スクリプト（DB・バックエンド自動起動）
├── frontend/                       # フロントエンド（Next.js）
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── pnpm-workspace.yaml
│   ├── next.config.ts              # Next.js設定（APIプロキシ等）
│   ├── next-env.d.ts               # Next.js TypeScript型定義
│   ├── tsconfig.json
│   ├── eslint.config.mjs
│   ├── postcss.config.mjs
│   ├── jest.config.js              # Jestテスト設定
│   ├── playwright.config.ts        # E2Eテスト設定
│   ├── e2e/                        # Playwright E2Eテスト
│   │   ├── pages/                  # ページ単位のE2Eテスト
│   │   └── scenarios/              # ページ間シナリオのE2Eテスト
│   ├── public/
│   │   └── image/                  # 静的画像（アイコン等）
│   └── src/
│       ├── proxy.ts                     # ルーティング制御（Next.js proxy。`/` を `/login` へリダイレクト）
│       ├── __tests__/                   # proxy.ts のJestテスト
│       ├── app/
│       │   ├── favicon.ico              # ファビコン
│       │   ├── globals.css              # グローバルCSS
│       │   ├── layout.tsx               # ルートレイアウト
│       │   ├── error.tsx                # ページ単位のエラーバウンダリ
│       │   ├── global-error.tsx         # ルートレイアウトのエラーバウンダリ
│       │   ├── page.tsx                 # トップページ
│       │   ├── login/                   # ログインページ（__tests__/にJestテスト）
│       │   ├── register/               # アカウント登録ページ（__tests__/にJestテスト）
│       │   ├── account_list/           # アカウント一覧ページ（__tests__/にJestテスト）
│       │   ├── [accountId]/
│       │   │   └── account_setting/    # アカウント設定ページ（__tests__/にJestテスト）
│       │   ├── photo/[photoAccountId]/
│       │   │   ├── photo_list/         # 写真一覧ページ（PhotoSwipe統合、__tests__/にJestテスト）
│       │   │   ├── photo_detail/       # 写真詳細ページ（__tests__/にJestテスト）
│       │   │   └── photo_setting/      # 写真設定ページ（__tests__/にJestテスト）
│       │   ├── admin/
│       │   │   └── account_management/ # 管理者アカウント管理ページ（__tests__/にJestテスト）
│       │   └── api/[...path]/
│       │       ├── route.ts            # `/api/*` をバックエンドへ中継するプロキシ（CSRF検証・ボディ上限・multipart対応）
│       │       └── __tests__/          # Jestテスト
│       ├── components/
│       │   ├── layout/                  # 共通レイアウト
│       │   │   ├── Header.tsx
│       │   │   ├── Header.module.css
│       │   │   ├── Footer.tsx
│       │   │   └── fonts/               # セルフホストフォント（woff2）
│       │   └── ui/                      # 汎用UIコンポーネント
│       │       └── ModalDialog.tsx      # モーダルダイアログ（__tests__/にJestテスト）
│       └── lib/
│           ├── api/
│           │   └── client.ts            # APIクライアント（__tests__/にJestテスト）
│           ├── auth/
│           │   ├── AuthProvider.tsx      # 認証プロバイダー
│           │   └── __tests__/            # Jestテスト
│           ├── a11y.ts                  # アクセシビリティユーティリティ
│           ├── consts.ts                # フロントエンド共有定数（バックエンドの値と同期）
│           ├── cookie.ts                # Cookie操作ユーティリティ（__tests__/にJestテスト）
│           ├── url.ts                   # URL操作ユーティリティ（__tests__/にJestテスト）
│           └── validation.ts            # 入力バリデーション（__tests__/にJestテスト）
├── backend/                        # バックエンド（Spring Boot）
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew / gradlew.bat      # Gradleラッパー
│   ├── gradle/                     # Gradleラッパー JAR
│   ├── config/
│   │   └── checkstyle/
│   │       └── checkstyle.xml      # Checkstyle設定ファイル
│   ├── set-env.sh                  # 環境変数設定スクリプト
│   └── src/
│       ├── main/
│       │   ├── java/com/web/gallery/
│       │   │   ├── aggregate/          # 集約ルート（複数テーブルにまたがる整合性・ライフサイクル管理）
│       │   │   ├── annotation/         # カスタムアノテーション
│       │   │   ├── aspect/             # AOP（管理者権限チェック等）
│       │   │   ├── config/             # 設定クラス
│       │   │   ├── constant/           # 定数（APIルート・メッセージ）
│       │   │   ├── controller/         # コントローラ
│       │   │   │   ├── request/        # リクエストDTO
│       │   │   │   └── response/       # レスポンスDTO
│       │   │   ├── domain/             # ドメインオブジェクト（値オブジェクト）
│       │   │   ├── dto/                # DBアクセス層の複合データ転送オブジェクト
│       │   │   ├── entity/             # エンティティ
│       │   │   ├── enumeration/        # 列挙型
│       │   │   ├── event/              # ドメインイベント・リスナー
│       │   │   ├── exception/          # カスタム例外
│       │   │   ├── helper/             # ヘルパーユーティリティ
│       │   │   ├── mapper/             # MyBatisマッパー
│       │   │   ├── model/              # モデルオブジェクト
│       │   │   ├── policy/             # ドメインサービス（単一のビジネスルールを判定）
│       │   │   ├── repository/         # リポジトリ
│       │   │   │   └── impl/
│       │   │   ├── scheduler/          # 定期実行タスク（期限切れリフレッシュトークンの削除等）
│       │   │   ├── service/            # サービス
│       │   │   │   └── impl/
│       │   │   └── type_handler/       # MyBatis型ハンドラ
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-local.yml       # ローカル開発用プロファイル
│       │       ├── application-development.yml # 開発環境用プロファイル
│       │       ├── application-prod.yml        # 本番環境用プロファイル
│       │       ├── messages.properties
│       │       └── com/web/gallery/mapper/  # MyBatis XMLマッパー
│       └── test/
│           ├── java/com/web/gallery/   # テストクラス
│           └── resources/
│               ├── application-test.yml
│               ├── json/controller     # テスト用リクエストjson
│               └── sql/                # テスト用SQL
```
