# DBの起動
db-up:
    docker-compose up -d

# DBの停止
db-down:
    docker-compose down

# フロントエンドの初期セットアップ（mise + corepack + pnpm install）
front-setup:
    mise install
    mise exec -- corepack enable pnpm
    mise reshim
    cd frontend && mise exec -- pnpm install

# フロントエンドの開発サーバー起動
front-run:
    cd frontend && mise exec -- pnpm dev

# フロントエンドのビルド
front-build:
    cd frontend && mise exec -- pnpm build

# フロントエンドのlint
lint:
    cd frontend && mise exec -- pnpm lint

# フロントエンドの単体テスト（Jest）
front-test:
    cd frontend && mise exec -- pnpm test

# E2Eテスト実行（DB・バックエンドを未起動の場合は自動起動）
e2e:
    ./scripts/e2e.sh

# 本番ビルドに対するCSPスモークテスト（next build + next start）
e2e-prod:
    cd frontend && mise exec -- pnpm test:e2e:prod

# backendアプリケーションの実行（localプロファイル）
backend-run:
    ./backend/gradlew -p backend bootRun --args='--spring.profiles.active=local'

# backendのビルド
backend-build:
    ./backend/gradlew -p backend build

# backendの単体テスト実行
backend-unitTest:
    ./backend/gradlew -p backend unitTest

# backendの結合テスト実行（要Docker）
backend-integrationTest:
    ./backend/gradlew -p backend integrationTest

# backendの単体・結合テストをすべて実行（要Docker）
backend-allTest:
    ./backend/gradlew -p backend allTest

# backendのクリーンビルド
backend-clean-build:
    ./backend/gradlew -p backend clean build

# DBドキュメント生成（SchemaSpy、要DB起動）
db-doc:
    docker compose --profile docs run --rm schemaspy

# JIGによるコード構造ドキュメント生成
jig:
    ./backend/gradlew -p backend jigReports

# Spring Modulithのモジュール構成図・モジュールキャンバス生成
modulith-doc:
    ./backend/gradlew -p backend generateModulithDocs

# レイヤードアーキテクチャ違反チェック
check-arch:
    bash scripts/check-architecture.sh backend
