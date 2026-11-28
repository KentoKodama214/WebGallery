# DBの起動
db-up:
    docker-compose up -d

# DBの停止
db-down:
    docker-compose down

# ローカル動作確認用のダミーデータ（アカウント3件・写真10枚・タグ・お気に入り）をDB・MinIOへ投入
# 注意: 実行するとDB・MinIO上の既存のアカウント・写真関連データを全件削除してから投入する
seed-local:
    ./scripts/seed-local-data.sh

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

# gallery-apiアプリケーションの実行（localプロファイル）
gallery-api-run:
    ./gallery-api/gradlew -p gallery-api bootRun --args='--spring.profiles.active=local'

# gallery-apiのビルド
gallery-api-build:
    ./gallery-api/gradlew -p gallery-api build

# gallery-apiの単体テスト実行
gallery-api-unitTest:
    ./gallery-api/gradlew -p gallery-api unitTest

# gallery-apiの結合テスト実行（要Docker）
gallery-api-integrationTest:
    ./gallery-api/gradlew -p gallery-api integrationTest

# gallery-apiの単体・結合テストをすべて実行（要Docker）
gallery-api-allTest:
    ./gallery-api/gradlew -p gallery-api allTest

# gallery-apiのクリーンビルド
gallery-api-clean-build:
    ./gallery-api/gradlew -p gallery-api clean build

# DBドキュメント生成（SchemaSpy、要DB起動）
db-doc:
    docker compose --profile docs run --rm schemaspy

# JIGによるコード構造ドキュメント生成
jig:
    ./gallery-api/gradlew -p gallery-api jigReports

# Spring Modulithのモジュール構成図・モジュールキャンバス生成
modulith-doc:
    ./gallery-api/gradlew -p gallery-api generateModulithDocs

# gallery-apiのコードフォーマットチェック（Spotless）
format-check:
    ./gallery-api/gradlew -p gallery-api spotlessCheck

# gallery-apiのコードフォーマット整形（Spotless）
format:
    ./gallery-api/gradlew -p gallery-api spotlessApply

# portfolio-api（Go。公開ページ向けのポートフォリオ表示API）をローカル環境で起動（要DB・MinIO起動）
portfolio-api-run:
    cd portfolio-api && PORTFOLIO_API_ENV=local mise exec -- go run ./cmd/server

# portfolio-apiの単体テスト
portfolio-api-test:
    cd portfolio-api && mise exec -- go test ./...

# portfolio-apiの結合テスト（要テスト用DB起動。just db-up）
portfolio-api-integrationTest:
    cd portfolio-api && mise exec -- go test -tags integration ./...

# portfolio-apiの静的解析（gofmtの整形漏れ・go vet）
portfolio-api-lint:
    cd portfolio-api && test -z "$(mise exec -- gofmt -l .)" || (mise exec -- gofmt -l . && exit 1)
    cd portfolio-api && mise exec -- go vet -tags integration ./...

# portfolio-apiのGraphQLコード生成（graph/schema.graphqls を変更したら実行する）
portfolio-api-generate:
    cd portfolio-api && mise exec -- go generate ./...
