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
  - [アーキテクチャ](#アーキテクチャ)
  - [データベース構成](#データベース構成)
  - [画面設計](#画面設計)
  - [API](#api)
  - [プロジェクト構成](#プロジェクト構成)

## 主な機能

- **アカウント管理** - ユーザー登録・ログイン・プロフィール編集
- **写真管理** - 写真のアップロード・編集・削除（EXIF情報の自動取得に対応）
- **タグ機能** - 写真への日本語・英語タグ付け
- **お気に入り** - 他ユーザーの写真をお気に入り登録
- **写真一覧** - フィルタリング（方向・タグ）やソート（撮影日・お気に入り数・季節）に対応
- **権限管理** - 4段階のユーザー権限によるアップロード枚数制限

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
docker-compose up -d
```

開発用データベース（`web_gallery`、ポート5432）とテスト用データベース（`web_gallery_test`、ポート5433）が起動します。データベースの初期化は `db/` 配下のSQLスクリプトにより自動的に行われます。

### 2. 環境変数の設定

```bash
./backend/set-env.sh
```

デフォルト値でもアプリケーションの起動は可能ですが、必要に応じて環境変数を設定してください。

### 3. フロントエンドのセットアップ

```bash
just setup
```

このコマンドで以下が実行されます:
1. miseによるNode.js（`.mise.toml`で定義されたバージョン）のインストール
2. Corepack経由でのpnpmの有効化
3. `frontend/` の依存パッケージインストール

### 4. アプリケーションの起動

```bash
# バックエンド
./backend/gradlew -p backend bootRun

# フロントエンド
just dev
```

バックエンドは `http://localhost:8080`、フロントエンドは `http://localhost:3000` でアクセスできます。

## ビルド・テスト

```bash
# ビルド
./backend/gradlew -p backend build

# 単体テストの実行
./backend/gradlew -p backend test

# 統合テストの実行（要Docker）
./backend/gradlew -p backend integrationTest

# 全テストの実行（要Docker）
./backend/gradlew -p backend allTest

# WARファイルの生成
./backend/gradlew -p backend war

# クリーンビルド
./backend/gradlew -p backend clean build
```

## アーキテクチャ

アーキテクチャの詳細は [`doc/architecture/`](doc/architecture/) を参照してください。

## データベース構成

データベースの詳細は [`doc/database/`](doc/database/) を参照してください。

## 画面設計

画面遷移図・画面一覧の詳細は [`doc/view/`](doc/view/) を参照してください。

## API

REST APIの詳細は [`doc/api/`](doc/api/) を参照してください。

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
│       ├── architecture-check.yml  # アーキテクチャチェックのGithub Action
│       ├── checkstyle.yml          # CheckstyleによるJavadocチェックのGithub Action
│       └── test.yml                # テスト実行のGithub Action
├── docker-compose.yml
├── docker/db/                      # DBイメージ用Dockerfile
├── db/                             # DB初期化スクリプト
│   ├── init/                       # 初期化エントリポイント
│   ├── common/                     # commonスキーマSQL
│   └── photo/                      # photoスキーマSQL
├── doc/
│   ├── api/                        # API設計書
│   ├── architecture/               # アーキテクチャ設計書
│   ├── database/                   # データベース設計書
│   └── view/                       # 画面設計書
├── scripts/
│   └── check-architecture.sh       # アーキテクチャチェックスクリプト
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
│   ├── public/
│   │   └── image/                  # 静的画像（アイコン等）
│   └── src/
│       ├── proxy.ts                     # APIプロキシ設定
│       ├── app/
│       │   ├── favicon.ico              # ファビコン
│       │   ├── globals.css              # グローバルCSS
│       │   ├── layout.tsx               # ルートレイアウト
│       │   ├── page.tsx                 # トップページ
│       │   ├── login/                   # ログインページ
│       │   ├── register/               # アカウント登録ページ
│       │   ├── account_list/           # アカウント一覧ページ
│       │   ├── [accountId]/
│       │   │   └── account_setting/    # アカウント設定ページ
│       │   ├── photo/[photoAccountId]/
│       │   │   ├── photo_list/         # 写真一覧ページ（PhotoSwipe統合）
│       │   │   ├── photo_detail/       # 写真詳細ページ
│       │   │   └── photo_setting/      # 写真設定ページ
│       │   └── api/v1/                  # Next.js APIルート（プロキシ）
│       ├── components/
│       │   └── layout/                  # 共通レイアウト
│       │       ├── Header.tsx
│       │       ├── Header.module.css
│       │       ├── Footer.tsx
│       │       └── Navigation.tsx
│       └── lib/
│           ├── api/
│           │   └── client.ts            # APIクライアント
│           └── auth/
│               └── AuthProvider.tsx      # 認証プロバイダー
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
│       │   │   ├── config/             # 設定クラス
│       │   │   ├── constant/           # 定数（APIルート・メッセージ）
│       │   │   ├── controller/         # コントローラ
│       │   │   │   ├── request/        # リクエストDTO
│       │   │   │   └── response/       # レスポンスDTO
│       │   │   ├── entity/             # エンティティ
│       │   │   ├── enumuration/        # 列挙型
│       │   │   ├── exception/          # カスタム例外
│       │   │   ├── helper/             # ヘルパーユーティリティ
│       │   │   ├── mapper/             # MyBatisマッパー
│       │   │   ├── model/              # モデルオブジェクト
│       │   │   ├── repository/         # リポジトリ
│       │   │   │   └── impl/
│       │   │   ├── service/            # サービス
│       │   │   │   └── impl/
│       │   │   └── type_handler/       # MyBatis型ハンドラ
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── messages.properties
│       │       └── com/web/gallery/mapper/  # MyBatis XMLマッパー
│       └── test/
│           ├── java/com/web/gallery/   # テストクラス
│           └── resources/
│               ├── application-test.yml
│               ├── json/controller     # テスト用リクエストjson
│               └── sql/                # テスト用SQL
```
