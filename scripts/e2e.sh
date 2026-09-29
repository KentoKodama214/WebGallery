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

echo "PostgreSQL・MinIOコンテナを起動します"
docker compose up -d postgres-db minio minio-setup

echo "PostgreSQLの起動を待機します"
for _ in $(seq 1 30); do
  if docker compose exec -T postgres-db pg_isready -U postgres >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "MinIOのバケット作成を待機します"
for _ in $(seq 1 30); do
  if [ "$(docker inspect -f '{{.State.Status}}' web-gallery-minio-setup 2>/dev/null)" = "exited" ]; then
    break
  fi
  sleep 1
done

if curl -sf "$BACKEND_URL" >/dev/null 2>&1; then
  # 既にポート8080でバックエンドが応答している。ただしそのインスタンスにE2E専用の設定
  # （とくに RATE_LIMIT_ENABLED=false）が適用されているとは限らないため、既定では再利用しない。
  # 手動起動したインスタンス（RATE_LIMIT_ENABLED は既定 true）を再利用すると、
  # app.rate-limit.register（10件 / 3600秒）に引っかかって11件目以降のアカウント登録が
  # 429 になり、アカウントを登録する多数のテストがまとめて失敗する。
  # 原因がレート制限だと気づきにくいため、静かに再利用せずここで止める
  if [ "${E2E_REUSE_BACKEND:-0}" = "1" ]; then
    echo "警告: 起動済みのバックエンドを再利用します (E2E_REUSE_BACKEND=1)"
    echo "      RATE_LIMIT_ENABLED=false で起動したインスタンスでない場合、"
    echo "      アカウント登録が429になり多数のテストが失敗します"
  else
    cat >&2 <<'REUSE_ERROR'
エラー: ポート8080で既にバックエンドが起動しています。

E2Eはアカウントを多数登録するため、レート制限を無効化した専用設定
(RATE_LIMIT_ENABLED=false) で起動したバックエンドが必要です。
手動起動したインスタンス (既定は RATE_LIMIT_ENABLED=true) をそのまま使うと、
11件目以降のアカウント登録が429になり、多数のテストがまとめて失敗します。

対処:
  1. 起動中のバックエンドを停止してから実行し直す (推奨)
       lsof -ti:8080 | xargs kill -9 && just e2e
  2. レート制限を無効化して起動済みであることが確実な場合のみ、再利用する
       E2E_REUSE_BACKEND=1 just e2e
REUSE_ERROR
    exit 1
  fi
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
  RATE_LIMIT_ENABLED="false" \
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
