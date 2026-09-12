#!/bin/bash
# ローカル動作確認用のダミーデータ投入スクリプト
#
# 以下を新規作成する（既存データは削除しない。account_id が重複する場合のみ再作成する）
#   - アカウント3件（localuser01: normal-user, localuser02: mini-user, localuser03: administrator）
#   - localuser01 の写真10枚（縦6枚・横4枚）とロケーション・タグ
#   - localuser01 の写真に対する localuser01/02/03 からのお気に入り（お気に入り数順ソートの検証用に件数を分散）
#   - ダミー画像（ImageMagickで生成）をMinIO（web-gallery-local バケット）へアップロード
#
# 再実行しても安全（localuser01/02/03 に紐づく既存データ・画像は投入前に削除する）
set -euo pipefail

cd "$(dirname "$0")/.."

DB_CONTAINER="web-gallery-db"
MINIO_CONTAINER="web-gallery-minio"
DB_NAME="web_gallery"
DB_USER="postgres"
BUCKET="web-gallery-local"
ACCOUNT_IDS=("localuser01" "localuser02" "localuser03")
PASSWORD_PLAIN="password01"

echo "前提条件を確認します"
for name in "$DB_CONTAINER" "$MINIO_CONTAINER"; do
  if [ "$(docker inspect -f '{{.State.Running}}' "$name" 2>/dev/null || true)" != "true" ]; then
    echo "コンテナ $name が起動していません。先に 'just db-up' を実行してください" >&2
    exit 1
  fi
done
for cmd in convert htpasswd openssl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "コマンド '$cmd' が見つかりません。インストールしてから再実行してください" >&2
    exit 1
  fi
done

NETWORK=$(docker inspect -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}' "$MINIO_CONTAINER")
echo "MinIOのDockerネットワーク: $NETWORK"

PASSWORD_HASH=$(htpasswd -bnBC 10 "" "$PASSWORD_PLAIN" | cut -d: -f2)

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT
SQL_FILE="$TMP_DIR/seed.sql"

# --- 写真データ定義（No.1〜10、配列は0始まりなので添字はNo-1） -----------------
DIRECTIONS=(horizontal vertical horizontal vertical horizontal vertical vertical horizontal vertical vertical)
PHOTO_AT=(
  "2021-01-10 08:30:00+09"
  "2024-01-14 07:45:00+09"
  "2021-04-05 10:15:00+09"
  "2024-04-09 09:00:00+09"
  "2021-07-20 15:00:00+09"
  "2024-07-24 19:30:00+09"
  "2021-10-12 11:20:00+09"
  "2024-10-16 13:40:00+09"
  "2022-11-01 18:50:00+09"
  "2023-08-01 14:10:00+09"
)
LOCATION_NAMES=("白銀岳展望台" "雪華並木通り" "千本桜堤" "桜守公園" "紺碧ビーチ" "港見台" "紅葉渓谷" "古社参道" "臨海展望タワー" "洋館庭園")
ADDRESSES=("北海道虻田郡ニセコ町" "北海道札幌市中央区" "奈良県吉野郡吉野町" "京都府京都市右京区" "沖縄県国頭郡恩納村" "神奈川県横浜市中区" "山梨県南都留郡富士河口湖町" "京都府京都市左京区" "東京都港区" "兵庫県神戸市中央区")
LATS=("42.8048" "43.0621" "34.3641" "35.0116" "26.5041" "35.4437" "35.5019" "35.0281" "35.6586" "34.6913")
LONS=("140.6874" "141.3544" "135.8631" "135.6762" "127.8557" "139.6380" "138.7469" "135.7808" "139.7454" "135.1830")
IS_PUBLIC=(true false true true true false true false true false)
TITLES_JA=("雪山の朝" "粉雪の並木道" "満開の桜並木" "一輪の桜" "夏の海岸線" "港町の夜景" "渓谷の紅葉" "古都の紅葉と社殿" "夜の展望タワー" "花と洋館")
TITLES_EN=("Snowy Mountain Morning" "Powder Snow Path" "Cherry Blossom Avenue" "Single Cherry Blossom" "Summer Coastline" "Night View of Port Town" "Autumn Leaves in the Valley" "Autumn Leaves and Shrine" "Night Observation Tower" "Flowers and Western House")
CAPTIONS=("澄んだ空気の中に広がる雪山の稜線" "静かに降り積もる粉雪と街路樹" "河川敷を彩る満開の桜並木" "枝先にひっそりと咲く一輪の桜" "青く輝く夏の海岸線" "海沿いに広がる港町の灯り" "谷を染める鮮やかな紅葉" "紅葉に囲まれた歴史ある社殿" "夜空にそびえる展望タワーの灯り" "庭園に咲く花と洋館の外観")
FOCALS=(24 35 50 85 16 24 70 35 24 50)
FVALS=(8.00 5.60 4.00 2.80 11.00 2.80 5.60 8.00 4.00 2.80)
SHUTTERS=(0.00400 0.00313 0.00200 0.00100 0.00400 0.01667 0.00313 0.00400 0.03333 0.00200)
ISOS=(100 200 100 100 100 800 200 100 1600 200)
# タグは「日本語名:英語名」を","区切りで複数指定
TAGS=("雪:snow,山:mountain" "雪:snow" "桜:sakura,花:flower" "桜:sakura" "海:sea" "海:sea,夜景:night-view" "紅葉:autumn-leaves" "紅葉:autumn-leaves,建築:architecture" "建築:architecture" "花:flower,建築:architecture")
# お気に入りしたアカウント（u1/u2/u3のカンマ区切り、空なら誰もお気に入りしていない）
FAVORITED_BY=("u1,u2,u3" "" "" "u1,u2" "u1,u3" "" "u1" "" "" "")
# ダミー画像の配色（背景色, 文字色）
BG_COLORS=("#eef4fb" "#e7eef7" "#fde3ec" "#fce8f1" "#e0f6f7" "#16233f" "#fbe6d4" "#f7dcc0" "#1c1f2b" "#eef7e3")
TEXT_COLORS=("#1b3a5c" "#1b3a5c" "#7a2d4d" "#7a2d4d" "#0b4f57" "#eaf2ff" "#6b3410" "#6b3410" "#f5f1e6" "#2f4a1e")

PHOTO_COUNT=10

# ImageMagickはfontconfig未設定だと文字が描画できないため、既知のフォントファイルを直接指定する
FONT=""
for candidate in \
  "/System/Library/Fonts/Supplemental/Arial.ttf" \
  "/System/Library/Fonts/Helvetica.ttc" \
  "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" \
  "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"; do
  if [ -f "$candidate" ]; then
    FONT="$candidate"
    break
  fi
done
FONT_ARGS=()
if [ -n "$FONT" ]; then
  FONT_ARGS=(-font "$FONT")
fi

echo "ダミー画像を生成します"
IMAGE_KEYS=()
for i in $(seq 0 $((PHOTO_COUNT - 1))); do
  no=$((i + 1))
  rand=$(openssl rand -hex 4)
  if [ "${DIRECTIONS[$i]}" = "vertical" ]; then
    size="900x1200"
  else
    size="1200x900"
  fi
  file_name="${no}-${rand}.jpg"
  IMAGE_KEYS+=("$file_name")
  convert -size "$size" "xc:${BG_COLORS[$i]}" \
    -gravity center -fill "${TEXT_COLORS[$i]}" -pointsize 40 "${FONT_ARGS[@]}" \
    -annotate 0 "No.${no}\n${TITLES_EN[$i]}\n(${DIRECTIONS[$i]})" \
    "$TMP_DIR/$file_name"
done

echo "既存の localuser01/02/03 データを削除します（再実行対応）"
docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<SQL
DELETE FROM photo.photo_favorite
  WHERE account_no IN (SELECT account_no FROM common.account WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}'))
     OR favorite_photo_account_no IN (SELECT account_no FROM common.account WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}'));
DELETE FROM photo.photo_tag_mst
  WHERE account_no IN (SELECT account_no FROM common.account WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}'));
DELETE FROM photo.photo_mst
  WHERE account_no IN (SELECT account_no FROM common.account WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}'));
DELETE FROM common.location_mst
  WHERE account_no IN (SELECT account_no FROM common.account WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}'));
DELETE FROM common.account
  WHERE account_id IN ('${ACCOUNT_IDS[0]}', '${ACCOUNT_IDS[1]}', '${ACCOUNT_IDS[2]}');
SQL

echo "既存のアップロード済み画像を削除します（再実行対応）"
for account_id in "${ACCOUNT_IDS[@]}"; do
  docker run --rm --network "$NETWORK" --entrypoint /bin/sh minio/mc:latest -c "
    mc alias set local http://minio:9000 minioadmin minioadmin >/dev/null &&
    mc rm --recursive --force local/${BUCKET}/${account_id}/ >/dev/null 2>&1 || true
  "
done

echo "投入用SQLを生成します"
{
  # created_by/updated_by は既存の管理者アカウント(account_no=1)を監査用の作成者として使う
  cat <<SQL
INSERT INTO common.account VALUES (DEFAULT, 1, now(), 1, now(), false, '${ACCOUNT_IDS[0]}', 'ローカル太郎', '${PASSWORD_HASH}', '1900-01-01', 'none', 'none', 'none', '', 'normal-user', now(), 0, false) RETURNING account_no \gset u1_
INSERT INTO common.account VALUES (DEFAULT, 1, now(), 1, now(), false, '${ACCOUNT_IDS[1]}', 'ローカル花子', '${PASSWORD_HASH}', '1900-01-01', 'none', 'none', 'none', '', 'mini-user', now(), 0, false) RETURNING account_no \gset u2_
INSERT INTO common.account VALUES (DEFAULT, 1, now(), 1, now(), false, '${ACCOUNT_IDS[2]}', 'ローカル次郎', '${PASSWORD_HASH}', '1900-01-01', 'none', 'none', 'none', '', 'administrator', now(), 0, false) RETURNING account_no \gset u3_
SQL

  for i in $(seq 0 $((PHOTO_COUNT - 1))); do
    no=$((i + 1))
    cat <<SQL
INSERT INTO common.location_mst VALUES (DEFAULT, :u1_account_no, ${no}, 1, now(), 1, now(), false, '${LOCATION_NAMES[$i]}', '${ADDRESSES[$i]}', ${LATS[$i]}, ${LONS[$i]});
INSERT INTO photo.photo_mst VALUES (DEFAULT, :u1_account_no, ${no}, 1, now(), 1, now(), false, '${PHOTO_AT[$i]}', ${no}, '${ACCOUNT_IDS[0]}/${IMAGE_KEYS[$i]}', '${IMAGE_KEYS[$i]}', '${TITLES_JA[$i]}', '${TITLES_EN[$i]}', '${CAPTIONS[$i]}', '${DIRECTIONS[$i]}', ${FOCALS[$i]}, ${FVALS[$i]}, ${SHUTTERS[$i]}, ${ISOS[$i]}, ${IS_PUBLIC[$i]});
SQL

    IFS=',' read -ra tag_list <<< "${TAGS[$i]}"
    tag_no=1
    for tag in "${tag_list[@]}"; do
      tag_ja="${tag%%:*}"
      tag_en="${tag##*:}"
      echo "INSERT INTO photo.photo_tag_mst VALUES (DEFAULT, :u1_account_no, ${no}, ${tag_no}, 1, now(), '${tag_ja}', '${tag_en}');"
      tag_no=$((tag_no + 1))
    done

    if [ -n "${FAVORITED_BY[$i]}" ]; then
      IFS=',' read -ra favoriters <<< "${FAVORITED_BY[$i]}"
      for who in "${favoriters[@]}"; do
        echo "INSERT INTO photo.photo_favorite VALUES (DEFAULT, :${who}_account_no, :u1_account_no, ${no}, 1, now());"
      done
    fi
  done
} > "$SQL_FILE"

echo "データベースへ投入します"
docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 < "$SQL_FILE"

echo "MinIOへ画像をアップロードします"
docker run --rm --network "$NETWORK" -v "$TMP_DIR:/images" --entrypoint /bin/sh minio/mc:latest -c "
  mc alias set local http://minio:9000 minioadmin minioadmin &&
  for f in /images/*.jpg; do
    mc cp \"\$f\" local/${BUCKET}/${ACCOUNT_IDS[0]}/\"\$(basename \"\$f\")\"
  done
"

cat <<MSG

投入が完了しました。

ログイン情報（パスワードは全アカウント共通）:
  localuser01 / password01 (normal-user, 写真10枚保持)
  localuser02 / password01 (mini-user)
  localuser03 / password01 (administrator)
MSG
