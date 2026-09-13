#!/bin/bash
# JMeterでの「写真一覧取得API」パフォーマンス検証用に、1アカウントへ大量の写真ダミーデータを投入するスクリプト
#
# 既存のダミーデータ（scripts/seed-local-data.sh）とは独立して動作する。
# 既存のaccount・photo_mst等は一切削除せず、本スクリプト専用のテストアカウント（デフォルト: perftestuser01）
# のデータのみを対象にする（既に存在する場合は、そのアカウントに紐づく写真データのみ削除してから作り直す）。
#
# 一覧取得APIは画像実体（S3/MinIO）には触れず署名付きURLを発行するだけなので、画像ファイルの実アップロードは行わない。
#
# 使い方:
#   ./scripts/perf/seed-perf-photos.sh                  # デフォルト10,000件を投入
#   PHOTO_COUNT=50000 ./scripts/perf/seed-perf-photos.sh # 件数を指定
set -euo pipefail

cd "$(dirname "$0")/../.."

DB_CONTAINER="web-gallery-db"
DB_NAME="web_gallery"
DB_USER="postgres"
ACCOUNT_ID="perftestuser01"
PHOTO_COUNT="${PHOTO_COUNT:-10000}"

echo "前提条件を確認します"
if [ "$(docker inspect -f '{{.State.Running}}' "$DB_CONTAINER" 2>/dev/null || true)" != "true" ]; then
  echo "コンテナ ${DB_CONTAINER} が起動していません。先に 'just db-up' を実行してください" >&2
  exit 1
fi

echo "アカウント ${ACCOUNT_ID} に写真 ${PHOTO_COUNT} 件を投入します"

docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 \
  -v account_id="'${ACCOUNT_ID}'" -v photo_count="${PHOTO_COUNT}" <<'SQL'
-- 既存の同名テストアカウントがあれば、紐づくデータごと削除して作り直す（再実行時の冪等性のため）
DO $$
DECLARE
  existing_account_no bigint;
BEGIN
  SELECT account_no INTO existing_account_no FROM common.account WHERE account_id = :account_id;
  IF existing_account_no IS NOT NULL THEN
    DELETE FROM photo.photo_favorite WHERE account_no = existing_account_no OR photo_account_no = existing_account_no;
    DELETE FROM photo.photo_tag_mst WHERE account_no = existing_account_no;
    DELETE FROM photo.photo_mst WHERE account_no = existing_account_no;
    DELETE FROM common.location_mst WHERE account_no = existing_account_no;
    DELETE FROM common.refresh_token WHERE account_no = existing_account_no;
    DELETE FROM common.account WHERE account_no = existing_account_no;
  END IF;
END $$;

-- テスト専用アカウントを作成（ログインしない前提なのでパスワードはダミー値）
INSERT INTO common.account
  (account_no, created_by, created_at, updated_by, updated_at, is_deleted,
   account_id, account_name, password, authority_kbn, last_login_datetime)
VALUES
  (DEFAULT, 1, now(), 1, now(), false,
   :account_id, 'パフォーマンステスト用アカウント', 'no-login-dummy-hash', 'normal-user', now())
RETURNING account_no \gset target_

-- 写真の紐付け先ロケーションを1件だけ用意（全写真で共用）
INSERT INTO common.location_mst
  (id, account_no, location_no, created_by, created_at, updated_by, updated_at, is_deleted,
   location_name, address, latitude, longitude)
VALUES
  (DEFAULT, :target_account_no, 1, :target_account_no, now(), :target_account_no, now(), false,
   'パフォーマンステスト地点', '', 0, 0);

-- generate_seriesで一括INSERT（1件ずつのループより大幅に高速）
INSERT INTO photo.photo_mst
  (account_no, photo_no, created_by, created_at, updated_by, updated_at, is_deleted,
   photo_at, location_no, image_file_path, image_file_name,
   photo_japanese_title, photo_english_title, caption, direction_kbn,
   focal_length, f_value, shutter_speed, iso, is_location_public)
SELECT
  :target_account_no,
  gs,
  :target_account_no,
  now(),
  :target_account_no,
  now(),
  false,
  -- 撮影日時を過去へ分散させ、既定ソート（photo_at DESC）に意味を持たせる
  now() - (gs || ' minutes')::interval,
  1,
  :account_id || '/' || gs || '-dummy.jpg',
  gs || '-dummy.jpg',
  'パフォーマンステスト写真 No.' || gs,
  'Perf Test Photo No.' || gs,
  '',
  (ARRAY['none', 'vertical', 'horizontal', 'square']::photo.direction_enum[])[1 + (gs % 4)],
  50,
  2.80,
  0.01,
  100,
  false
FROM generate_series(1, :photo_count) AS gs;
SQL

echo "投入完了: アカウント ${ACCOUNT_ID} に写真 ${PHOTO_COUNT} 件"
echo "削除する場合は、DB上で account_id = '${ACCOUNT_ID}' のアカウントを本スクリプト同様に削除してください"
