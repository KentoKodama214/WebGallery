#!/bin/bash
# フロントエンドのPlaywright E2Eテストを一括実行するスクリプト
# DB（docker-compose）とバックエンド（gradlew bootRun）を必要に応じて自動起動し、
# frontend/e2e配下のテストを実行する
set -euo pipefail

cd "$(dirname "$0")/.."

BACKEND_URL="http://localhost:8080/v3/api-docs"
BACKEND_LOG="/tmp/webgallery-backend-e2e.log"
BACKEND_PID=""

cleanup() {
  if [ -n "$BACKEND_PID" ]; then
    echo "バックエンドを停止します (PID: $BACKEND_PID)"
    kill -- -"$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

echo "PostgreSQLコンテナを起動します"
docker compose up -d postgres-db

echo "PostgreSQLの起動を待機します"
for _ in $(seq 1 30); do
  if docker compose exec -T postgres-db pg_isready -U postgres >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if curl -sf "$BACKEND_URL" >/dev/null 2>&1; then
  echo "バックエンドは既に起動しています。新規起動はスキップします"
else
  echo "バックエンドを起動します (SPRING_PROFILES_ACTIVE=local)"
  # docker-compose.ymlのpostgres-db設定に合わせて明示的に指定する
  # （シェルの環境変数でDB_URL等が上書きされていても、E2E実行時はdocker-composeのDBに接続する）
  # E2E実行時限りのJWTシークレット（本番用ではない）。application-local.ymlは
  # デフォルト値を持たないため、ここで明示的に与える
  E2E_JWT_SECRET="${JWT_SECRET:-e2e-only-secret-not-for-production-must-be-256-bits-long}"
  set -m
  SPRING_PROFILES_ACTIVE=local \
  DB_URL="jdbc:postgresql://localhost:5432/web_gallery" \
  DB_USERNAME="postgres" \
  DB_PASSWORD="postgres" \
  JWT_SECRET="$E2E_JWT_SECRET" \
    ./backend/gradlew -p backend bootRun --no-daemon \
    >"$BACKEND_LOG" 2>&1 &
  BACKEND_PID=$!
  set +m

  echo "バックエンドの起動を待機します"
  READY=false
  for _ in $(seq 1 60); do
    if curl -sf "$BACKEND_URL" >/dev/null 2>&1; then
      READY=true
      break
    fi
    sleep 2
  done

  if [ "$READY" != "true" ]; then
    echo "バックエンドの起動がタイムアウトしました。ログ: $BACKEND_LOG"
    tail -n 50 "$BACKEND_LOG" || true
    exit 1
  fi
  echo "バックエンドが起動しました"
fi

echo "Playwright E2Eテストを実行します"
cd frontend
pnpm exec playwright test "$@"
