#!/bin/bash
# ローカル動作確認用のダミーデータ投入スクリプト
#
# 【注意】実行するとローカルDB・MinIOのアカウント関連データを完全にリセットする
#   - account、account_authority、location_mst、refresh_token、login_history、
#     photo_mst、photo_tag_mst、photo_favorite、photo_view_log、photo_list_filter_log、
#     portfolio_mst、portfolio_photo、portfolio_tag_mst、inquiry_mst、inquiry_reply_mst を全件削除
#   - MinIO（web-gallery-local バケット）内の画像ファイルも全件削除
# その上で、画面の全パターンを手動でのデータ追加なしに確認できるよう、以下を新規作成する
#   - アカウント25件（権限4種、ロック状態・退会済み、一覧のページング用のダミー）
#   - localuser01・04・05 の写真（縦・横・正方形、位置情報の公開/非公開、タグ0〜20件、論理削除済み等）とロケーション・タグ・お気に入り
#   - localuser01・04・05 のポートフォリオ（フィーチャー型/グリッド型 × 公開/下書き、公開不可の状態、上限値の文字列・タグ、一覧のページング等）
#   - localuser01・04・05 のお問い合わせと管理者（localuser03）の返信（未対応/回答済み（既読・未読）/取り下げ、複数返信、上限値の文字列等）
#   - ダミー画像（ImageMagickで生成）をMinIOへアップロード
set -euo pipefail

cd "$(dirname "$0")/.."

DB_CONTAINER="web-gallery-db"
MINIO_CONTAINER="web-gallery-minio"
DB_NAME="web_gallery"
DB_USER="postgres"
BUCKET="web-gallery-local"
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

echo "既存のアカウント関連データを全件削除します"
docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<'SQL'
TRUNCATE TABLE
  common.inquiry_reply_mst,
  common.inquiry_mst,
  photo.portfolio_tag_mst,
  photo.portfolio_photo,
  photo.portfolio_mst,
  photo.photo_list_filter_log,
  photo.photo_view_log,
  photo.photo_favorite,
  photo.photo_tag_mst,
  photo.photo_mst,
  common.location_mst,
  common.login_history,
  common.refresh_token,
  common.account_authority,
  common.account
RESTART IDENTITY CASCADE;
SQL

echo "既存のアップロード済み画像を全件削除します（バケット: ${BUCKET}）"
docker run --rm --network "$NETWORK" --entrypoint /bin/sh minio/mc:latest -c "
  mc alias set local http://minio:9000 minioadmin minioadmin >/dev/null &&
  mc rm --recursive --force --dangerous local/${BUCKET}/ >/dev/null 2>&1 || true
"

PASSWORD_HASH=$(htpasswd -bnBC 10 "" "$PASSWORD_PLAIN" | cut -d: -f2)

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT
SQL_FILE="$TMP_DIR/seed.sql"
IMAGE_DIR="$TMP_DIR/images"

# SQLの文字列リテラルとして出力する（シングルクォートをエスケープする）
q() {
  local sq="'"
  printf "'%s'" "${1//$sq/$sq$sq}"
}

# 文字列を指定回数繰り返す（上限文字数の確認用）
repeat_str() {
  local result=""
  for _ in $(seq 1 "$2"); do
    result+="$1"
  done
  printf '%s' "$result"
}

# --- アカウント定義 ---------------------------------------------------------------
# キー|アカウントID|アカウント名|権限|削除フラグ|ログイン失敗回数|管理者ロック|更新日時（now()からの経過）|最終ログイン（now()からの経過）|生年月日|性別|出身|在住|フリーメモ
# account_no は全件削除（RESTART IDENTITY）直後に定義順で採番されるため、キー u{N} の account_no は N になる
# （画像のオブジェクトキーのプレフィックスに使うため、投入後に一致を検証する）
# ログインのロックは「失敗回数がしきい値（3回）以上、かつ更新日時から30分以内」で判定されるため、
# localuser06 は更新日時を現在にしてロック中、localuser07 は1時間前にして自動解除済みの状態にする
read -r -d '' ACCOUNT_ROWS <<'ROWS' || true
u1|localuser01|ローカル太郎|normal-user|false|0|false|0 minutes|0 minutes|1990-05-01|man|Tokyo|Kanagawa|週末に風景写真を撮っています。
u2|localuser02|ローカル花子|mini-user|false|0|false|0 minutes|3 days|1900-01-01|none|none|none|
u3|localuser03|ローカル次郎|administrator|false|0|false|0 minutes|1 day|1900-01-01|none|none|none|
u4|localuser04|ミニ上限子|mini-user|false|0|false|0 minutes|2 days|1985-11-01|woman|Osaka|Osaka|写真・ポートフォリオともに作成数の上限に達しています。
u5|localuser05|スペシャル三郎|special-user|false|0|false|0 minutes|5 days|2000-02-01|man|Hokkaido|Okinawa|全国の灯台と砂丘を巡っています。
u6|localuser06|ロック中六郎|normal-user|false|3|false|0 minutes|10 days|1900-01-01|none|none|none|ログイン失敗回数によりロック中
u7|localuser07|自動解除七子|normal-user|false|3|false|1 hour|10 days|1900-01-01|none|none|none|ロック期間（30分）経過済みのため次回ログインで自動解除
u8|localuser08|管理ロック八郎|normal-user|false|0|true|0 minutes|20 days|1900-01-01|none|none|none|管理者によりロック中
u9|localuser09|失敗一回九子|normal-user|false|1|false|0 minutes|1 day|1900-01-01|none|none|none|ログイン失敗1回（ロック前）
u10|localuser10|退会済み十郎|normal-user|true|0|false|0 minutes|90 days|1900-01-01|none|none|none|
ROWS
for n in $(seq 11 25); do
  if [ $((n % 2)) -eq 0 ]; then
    authority="mini-user"
  else
    authority="normal-user"
  fi
  ACCOUNT_ROWS+=$'\n'"u${n}|localuser${n}|ダミーユーザー${n}|${authority}|false|0|false|0 minutes|${n} days|1900-01-01|none|none|none|"
done

# --- ロケーション定義 -------------------------------------------------------------
# 所有者キー|ロケーション番号|表示名|住所|緯度|経度
read -r -d '' LOCATION_ROWS <<'ROWS' || true
u1|1|白銀岳展望台|北海道虻田郡ニセコ町|42.8048|140.6874
u1|2|雪華並木通り|北海道札幌市中央区|43.0621|141.3544
u1|3|千本桜堤|奈良県吉野郡吉野町|34.3641|135.8631
u1|4|桜守公園|京都府京都市右京区|35.0116|135.6762
u1|5|紺碧ビーチ|沖縄県国頭郡恩納村|26.5041|127.8557
u1|6|港見台|神奈川県横浜市中区|35.4437|139.6380
u1|7|紅葉渓谷|山梨県南都留郡富士河口湖町|35.5019|138.7469
u1|8|古社参道|京都府京都市左京区|35.0281|135.7808
u1|9|臨海展望タワー|東京都港区|35.6586|139.7454
u1|10|洋館庭園|兵庫県神戸市中央区|34.6913|135.1830
u1|11|あじさい寺|神奈川県鎌倉市|35.3317|139.5526
u1|12|星峠の棚田|新潟県十日町市|37.1306|138.6553
u1|13|港の大観覧車|大阪府大阪市港区|34.6553|135.4300
u1|14|花火河川敷|東京都江戸川区|35.7067|139.8683
u1|15|高原牧場||36.0500|138.2833
u1|16|梅林公園|茨城県水戸市|36.3758|140.4524
u1|17|湖畔キャンプ場|北海道川上郡弟子屈町|43.6167|144.3500
u1|18|彼岸花の里|埼玉県日高市|35.8934|139.3110
u4|1|ミニの散歩道|東京都世田谷区|35.6464|139.6533
u5|1|岬の灯台|千葉県銚子市|35.6951|140.8681
u5|2|砂丘|鳥取県鳥取市|35.5408|134.2290
u5|3|火口湖|宮城県刈田郡蔵王町|38.1361|140.4486
ROWS

# --- 写真定義 ---------------------------------------------------------------------
# 所有者キー|写真番号|向き|撮影日時|ロケーション番号|位置情報公開|タイトル（日本語）|タイトル（英語）|キャプション|焦点距離|F値|シャッタースピード|ISO|タグ|お気に入りしたアカウント|削除フラグ
# タグは「日本語名:英語名」を","区切りで複数指定（英語名は空でもよい）。お気に入りはアカウントキーの","区切り
# localuser01 は表示対象25枚（1ページ20件のため2ページ目が出る）＋論理削除済み1枚
TAGS_MAX=$(for n in $(seq -w 1 20); do printf 'タグ%s:tag%s,' "$n" "$n"; done)
TAGS_MAX=${TAGS_MAX%,}
LONG_TITLE_JA=$(repeat_str "長" 100)
LONG_TITLE_EN=$(repeat_str "L" 100)
LONG_CAPTION=$(repeat_str "キャプションの文字数が多い場合の表示を確認するための文章です。" 10)
read -r -d '' PHOTO_ROWS <<ROWS || true
u1|1|horizontal|2021-01-10 08:30:00+09|1|true|雪山の朝|Snowy Mountain Morning|澄んだ空気の中に広がる雪山の稜線|24|8.00|0.00400|100|雪:snow,山:mountain|u1,u2,u3|false
u1|2|vertical|2024-01-14 07:45:00+09|2|false|粉雪の並木道|Powder Snow Path|静かに降り積もる粉雪と街路樹|35|5.60|0.00313|200|雪:snow||false
u1|3|horizontal|2021-04-05 10:15:00+09|3|true|満開の桜並木|Cherry Blossom Avenue|河川敷を彩る満開の桜並木|50|4.00|0.00200|100|桜:sakura,花:flower||false
u1|4|vertical|2024-04-09 09:00:00+09|4|true|一輪の桜|Single Cherry Blossom|枝先にひっそりと咲く一輪の桜|85|2.80|0.00100|100|桜:sakura|u1,u2|false
u1|5|horizontal|2021-07-20 15:00:00+09|5|true|夏の海岸線|Summer Coastline|青く輝く夏の海岸線|16|11.00|0.00400|100|海:sea|u1,u3|false
u1|6|vertical|2024-07-24 19:30:00+09|6|false|港町の夜景|Night View of Port Town|海沿いに広がる港町の灯り|24|2.80|0.01667|800|海:sea,夜景:night-view||false
u1|7|vertical|2021-10-12 11:20:00+09|7|true|渓谷の紅葉|Autumn Leaves in the Valley|谷を染める鮮やかな紅葉|70|5.60|0.00313|200|紅葉:autumn-leaves|u1|false
u1|8|horizontal|2024-10-16 13:40:00+09|8|false|古都の紅葉と社殿|Autumn Leaves and Shrine|紅葉に囲まれた歴史ある社殿|35|8.00|0.00400|100|紅葉:autumn-leaves,建築:architecture||false
u1|9|vertical|2022-11-01 18:50:00+09|9|true|夜の展望タワー|Night Observation Tower|夜空にそびえる展望タワーの灯り|24|4.00|0.03333|1600|建築:architecture||false
u1|10|vertical|2023-08-01 14:10:00+09|10|false|花と洋館|Flowers and Western House|庭園に咲く花と洋館の外観|50|2.80|0.00200|200|花:flower,建築:architecture||false
u1|11|square|2022-03-28 10:00:00+09|3|true|桜と青空|Cherry Blossoms and Blue Sky|青空に映える満開の桜|35|5.60|0.00200|100|桜:sakura,空:sky|u1,u2,u3,u4,u5|false
u1|12|horizontal|2023-06-18 16:30:00+09|11|true|雨上がりの紫陽花|Hydrangeas After Rain|雨粒をまとった色とりどりの紫陽花|90|2.80|0.00500|400|花:flower,紫陽花:hydrangea,雨:rain|u2,u4|false
u1|13|vertical|2025-02-11 06:50:00+09|1|false|樹氷の朝焼け|||28|8.00|0.00800|200|||false
u1|14|square|2022-05-05 12:00:00+09|12|true|新緑の棚田|Green Rice Terraces|水を張った棚田に映る新緑|24|11.00|0.00250|100|${TAGS_MAX}|u1,u3|false
u1|15|horizontal|2024-09-15 18:20:00+09|13|true|夕焼けの観覧車|Ferris Wheel at Sunset|夕焼け空に浮かぶ大観覧車|35|8.00|0.00400|200|夕景:,建築:architecture|u1,u2,u3,u4|false
u1|16|vertical|2023-12-24 20:00:00+09|6|false|聖夜のイルミネーション|Christmas Illumination|港を彩るクリスマスのイルミネーション|50|1.80|0.01250|3200|夜景:night-view,冬:winter|u2,u3,u4|false
u1|17|horizontal|2020-08-12 21:15:00+09|14|true|夏の花火|Summer Fireworks|夜空に咲く大輪の花火|35|11.00|30.00000|100|花火:fireworks,夏:summer,夜景:night-view|u1,u2,u3|false
u1|18|vertical|2025-05-20 09:30:00+09|15|true|${LONG_TITLE_JA}|${LONG_TITLE_EN}|${LONG_CAPTION}|105|5.60|0.00200|100|山:mountain|u5|false
u1|19|square|2024-03-03 14:00:00+09|16|true|満開の梅|Plum Blossoms in Full Bloom|甘い香りが漂う梅林|100|2.80|0.00100|100|梅:plum,花:flower|u2|false
u1|20|horizontal|2021-12-31 23:50:00+09|8|true|年越しの参道|New Year's Eve Approach|灯籠に照らされた年越しの参道|24|2.00|0.06667|6400|建築:architecture,冬:winter||false
u1|21|horizontal|2022-06-21 04:40:00+09|17|true|夏至の朝霧|Morning Mist on Summer Solstice|湖面に立ちこめる朝霧|200|1.40|0.00013|6400|湖:lake|u4|false
u1|22|vertical|2023-09-23 17:45:00+09|18|true|彼岸花の群生|Red Spider Lilies|里山を赤く染める彼岸花|85|4.00|0.00200|200|花:flower,秋:autumn|u1,u2|false
u1|23|square|2024-11-23 11:00:00+09|7|true|紅葉のトンネル|Tunnel of Autumn Leaves|頭上を覆う紅葉のトンネル|16|8.00|0.00400|200|紅葉:autumn-leaves|u3|false
u1|24|horizontal|2025-08-08 10:10:00+09|5|true|シュノーケリング日和|Perfect Day for Snorkeling|透き通る海と白い砂浜|16|8.00|0.00100|100|海:sea,夏:summer||false
u1|25|vertical|2020-02-14 19:00:00+09|2|false|雪あかりの小径|Snow Lantern Path|雪灯りが続く冬の小径|35|2.00|0.03333|1600|雪:snow,夜景:night-view,冬:winter|u1|false
u1|26|horizontal|2022-10-10 12:00:00+09|9|true|削除済みの写真|Deleted Photo|論理削除済みのため一覧に表示されない写真|24|8.00|0.00400|100|建築:architecture||true
u5|1|horizontal|2023-01-01 06:40:00+09|1|true|初日の出と灯台|First Sunrise and Lighthouse|水平線から昇る初日の出|70|8.00|0.00200|100|海:sea,日の出:sunrise|u1,u2,u4|false
u5|2|horizontal|2022-08-20 17:00:00+09|2|false|風紋の砂丘|Sand Ripples|風が描いた砂丘の模様|50|11.00|0.00400|100|砂丘:dunes|u1|false
u5|3|vertical|2024-10-05 13:30:00+09|3|true|エメラルドの火口湖|Emerald Crater Lake|山頂に広がるエメラルドグリーンの湖|24|8.00|0.00250|100|山:mountain,湖:lake||false
u5|4|square|2025-04-12 15:00:00+09|1|false|灯台の螺旋階段|Spiral Staircase|灯台内部の螺旋階段を見下ろす|14|4.00|0.01667|800|建築:architecture|u3|false
u5|5|vertical|2021-12-05 08:00:00+09|2|true|雪の砂丘|Snow on the Dunes|うっすらと雪化粧した砂丘|35|8.00|0.00400|200|雪:snow,砂丘:dunes|u1,u5|false
ROWS
# localuser04（mini-user）は写真の作成数上限（ローカルは10枚）に達した状態にする
MINI_DIRECTIONS=(horizontal vertical square)
for n in $(seq 1 10); do
  PHOTO_ROWS+=$'\n'"u4|${n}|${MINI_DIRECTIONS[$(((n - 1) % 3))]}|2025-0$(((n - 1) % 9 + 1))-15 12:00:00+09|1|true|ミニユーザーの写真${n}|Mini User Photo ${n}|上限確認用の写真|50|5.60|0.00400|200|散歩:walk||false"
done

# ダミー画像の配色（背景色:文字色）。写真番号に応じて順番に使う
COLORS=("#eef4fb:#1b3a5c" "#fde3ec:#7a2d4d" "#e0f6f7:#0b4f57" "#16233f:#eaf2ff" "#fbe6d4:#6b3410" "#1c1f2b:#f5f1e6" "#eef7e3:#2f4a1e" "#f3e8fb:#4b2a6b")

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

echo "ダミー画像と投入用SQLを生成します"
{
  # created_by/updated_by は監査用の作成者IDとして1を使う（全件クリア直後のためlocaluser01自身のaccount_noと一致する）
  while IFS='|' read -r key account_id account_name authority is_deleted failure_count admin_locked updated_ago login_ago birthdate sex birthplace resident memo; do
    echo "INSERT INTO common.account (created_by, created_at, updated_by, updated_at, is_deleted, account_id, account_name, password, birthdate, sex_kbn, birthplace_prefecture_kbn_code, resident_prefecture_kbn_code, free_memo, last_login_datetime, login_failure_count, is_admin_locked) VALUES (1, now() - interval '90 days', 1, now() - interval '${updated_ago}', ${is_deleted}, '${account_id}', $(q "$account_name"), '${PASSWORD_HASH}', '${birthdate}', '${sex}', '${birthplace}', '${resident}', $(q "$memo"), now() - interval '${login_ago}', ${failure_count}, ${admin_locked}) RETURNING account_no \\gset ${key}_"
    echo "INSERT INTO common.account_authority VALUES (:${key}_account_no, 1, now(), 1, now(), '${authority}');"
  done <<< "$ACCOUNT_ROWS"

  while IFS='|' read -r owner location_no display_name address lat lon; do
    echo "INSERT INTO common.location_mst (account_no, location_no, created_by, created_at, updated_by, updated_at, management_name, display_name, address, latitude, longitude) VALUES (:${owner}_account_no, ${location_no}, :${owner}_account_no, now(), :${owner}_account_no, now(), $(q "${display_name}_管理用"), $(q "$display_name"), $(q "$address"), ${lat}, ${lon});"
  done <<< "$LOCATION_ROWS"

  while IFS='|' read -r owner photo_no direction photo_at location_no is_public title_ja title_en caption focal fval shutter iso tags favorited_by is_deleted; do
    account_no="${owner#u}"
    case "$direction" in
      vertical) size="900x1200" ;;
      horizontal) size="1200x900" ;;
      *) size="1000x1000" ;;
    esac
    color="${COLORS[$(((photo_no - 1) % ${#COLORS[@]}))]}"
    # オブジェクトキーはアプリと同じく「{アカウント番号}/{写真番号}-{ランダム}.{拡張子}」とする
    file_name="${photo_no}-$(openssl rand -hex 16).jpg"
    mkdir -p "$IMAGE_DIR/$account_no"
    convert -size "$size" "xc:${color%%:*}" \
      -gravity center -fill "${color##*:}" -pointsize 40 "${FONT_ARGS[@]}" \
      -annotate 0 "localuser$(printf '%02d' "$account_no") No.${photo_no}\n${title_en:0:30}\n(${direction})" \
      "$IMAGE_DIR/$account_no/$file_name"

    echo "INSERT INTO photo.photo_mst (account_no, photo_no, created_by, created_at, updated_by, updated_at, is_deleted, photo_at, location_no, image_file_path, image_file_name, photo_japanese_title, photo_english_title, caption, direction_kbn, focal_length, f_value, shutter_speed, iso, is_location_public) VALUES (:${owner}_account_no, ${photo_no}, :${owner}_account_no, now(), :${owner}_account_no, now(), ${is_deleted}, '${photo_at}', ${location_no}, '${account_no}/${file_name}', '${file_name}', $(q "$title_ja"), $(q "$title_en"), $(q "$caption"), '${direction}', ${focal}, ${fval}, ${shutter}, ${iso}, ${is_public});"

    if [ -n "$tags" ]; then
      IFS=',' read -ra tag_list <<< "$tags"
      tag_no=1
      for tag in "${tag_list[@]}"; do
        echo "INSERT INTO photo.photo_tag_mst (account_no, photo_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) VALUES (:${owner}_account_no, ${photo_no}, ${tag_no}, :${owner}_account_no, now(), $(q "${tag%%:*}"), $(q "${tag#*:}"));"
        tag_no=$((tag_no + 1))
      done
    fi

    if [ -n "$favorited_by" ]; then
      IFS=',' read -ra favoriters <<< "$favorited_by"
      for who in "${favoriters[@]}"; do
        echo "INSERT INTO photo.photo_favorite (account_no, favorite_photo_account_no, favorite_photo_no, created_by, created_at) VALUES (:${who}_account_no, :${owner}_account_no, ${photo_no}, :${who}_account_no, now());"
      done
    fi
  done <<< "$PHOTO_ROWS"

  # --- ポートフォリオ ---------------------------------------------------------------
  # feature: template_settings は {}、写真ごとの photo_settings に theme/subTheme/description（公開にはtheme必須）
  # grid: template_settings の rows に行パターン（行の枚数の合計＝写真の枚数で公開可能）、photo_settings は {}
  cat <<'SQL'
-- localuser01 P1: フィーチャー型・公開中（写真7は P3 と共有し、削除時に両方が下書きへ戻ることを確認する）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 1, :u1_account_no, now() - interval '30 days', :u1_account_no, now() - interval '2 days', '日本の四季', '春夏秋冬の風景を、季節の移ろいに沿って紹介します。', 'feature', '{}', 1, 'published', 1);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u1_account_no, 1, 3, :u1_account_no, now(), 1, '{"theme": "春", "subTheme": "桜の季節", "description": "吉野の山を埋め尽くす桜並木。"}'),
  (:u1_account_no, 1, 11, :u1_account_no, now(), 2, '{"theme": "春の空", "subTheme": "", "description": ""}'),
  (:u1_account_no, 1, 5, :u1_account_no, now(), 3, '{"theme": "夏", "subTheme": "海辺の午後"}'),
  (:u1_account_no, 1, 12, :u1_account_no, now(), 4, '{"theme": "梅雨", "description": "雨上がりにだけ見られる色の深さ。"}'),
  (:u1_account_no, 1, 7, :u1_account_no, now(), 5, '{"theme": "秋", "subTheme": "渓谷の紅葉", "description": "谷を染める紅葉と澄んだ空気。"}');
INSERT INTO photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) VALUES
  (:u1_account_no, 1, 1, :u1_account_no, now(), '四季', 'seasons'),
  (:u1_account_no, 1, 2, :u1_account_no, now(), '風景', 'landscape'),
  (:u1_account_no, 1, 3, :u1_account_no, now(), '日本', '');

-- localuser01 P2: フィーチャー型・下書き（テーマ未入力の写真があるため公開できない）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 2, :u1_account_no, now() - interval '20 days', :u1_account_no, now() - interval '1 day', '冬の北海道', '', 'feature', '{}', 1, 'draft', 2);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u1_account_no, 2, 1, :u1_account_no, now(), 1, '{"theme": "雪山", "subTheme": "ニセコの朝"}'),
  (:u1_account_no, 2, 13, :u1_account_no, now(), 2, '{"theme": "", "subTheme": "テーマ未入力"}'),
  (:u1_account_no, 2, 25, :u1_account_no, now(), 3, '{}');
INSERT INTO photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) VALUES
  (:u1_account_no, 2, 1, :u1_account_no, now(), '冬', 'winter');

-- localuser01 P3: グリッド型・公開中（全行パターンを使用。2 + 3 + 2 + 2 = 9枚）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 3, :u1_account_no, now() - interval '15 days', :u1_account_no, now() - interval '3 days', '色彩のコレクション', '色をテーマに、構図の異なる写真を組み合わせました。', 'grid',
  '{"rows": [{"pattern": "TWO_EQUAL"}, {"pattern": "THREE_EQUAL"}, {"pattern": "WIDE_NARROW"}, {"pattern": "NARROW_WIDE"}]}', 1, 'published', 3);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u1_account_no, 3, 11, :u1_account_no, now(), 1, '{}'),
  (:u1_account_no, 3, 14, :u1_account_no, now(), 2, '{}'),
  (:u1_account_no, 3, 19, :u1_account_no, now(), 3, '{}'),
  (:u1_account_no, 3, 23, :u1_account_no, now(), 4, '{}'),
  (:u1_account_no, 3, 1, :u1_account_no, now(), 5, '{}'),
  (:u1_account_no, 3, 15, :u1_account_no, now(), 6, '{}'),
  (:u1_account_no, 3, 22, :u1_account_no, now(), 7, '{}'),
  (:u1_account_no, 3, 7, :u1_account_no, now(), 8, '{}'),
  (:u1_account_no, 3, 8, :u1_account_no, now(), 9, '{}');
INSERT INTO photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) VALUES
  (:u1_account_no, 3, 1, :u1_account_no, now(), '色彩', 'color'),
  (:u1_account_no, 3, 2, :u1_account_no, now(), 'グリッド', 'grid');

-- localuser01 P4: グリッド型・下書き（行の枚数の合計5枚と写真4枚が一致しないため公開できない）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 4, :u1_account_no, now() - interval '10 days', :u1_account_no, now() - interval '10 days', '夜と光', '', 'grid',
  '{"rows": [{"pattern": "TWO_EQUAL"}, {"pattern": "THREE_EQUAL"}]}', 1, 'draft', 4);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u1_account_no, 4, 17, :u1_account_no, now(), 1, '{}'),
  (:u1_account_no, 4, 20, :u1_account_no, now(), 2, '{}'),
  (:u1_account_no, 4, 21, :u1_account_no, now(), 3, '{}'),
  (:u1_account_no, 4, 24, :u1_account_no, now(), 4, '{}');

-- localuser01 P5: フィーチャー型・下書き（写真0枚・タグ0件・説明文なし）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 5, :u1_account_no, now() - interval '5 days', :u1_account_no, now() - interval '5 days', '作成途中のポートフォリオ', '', 'feature', '{}', 1, 'draft', 5);

-- localuser01 P6: グリッド型・下書き（公開可能な状態。テーマ100文字・説明文2000文字・タグ30件の上限値）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u1_account_no, 6, :u1_account_no, now() - interval '1 day', :u1_account_no, now() - interval '1 hour', repeat('長', 100), repeat(E'説明文の行です。\n', 222) || repeat('説', 2), 'grid',
  '{"rows": [{"pattern": "TWO_EQUAL"}, {"pattern": "TWO_EQUAL"}]}', 1, 'draft', 6);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u1_account_no, 6, 2, :u1_account_no, now(), 1, '{}'),
  (:u1_account_no, 6, 4, :u1_account_no, now(), 2, '{}'),
  (:u1_account_no, 6, 6, :u1_account_no, now(), 3, '{}'),
  (:u1_account_no, 6, 9, :u1_account_no, now(), 4, '{}');
INSERT INTO photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name)
SELECT :u1_account_no, 6, n, :u1_account_no, now(), 'タグ' || lpad(n::text, 2, '0'), 'tag' || lpad(n::text, 2, '0')
FROM generate_series(1, 30) AS n;

-- localuser04（mini-user）: ポートフォリオの作成数上限（ローカルは3件）に達した状態
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order) VALUES
  (:u4_account_no, 1, :u4_account_no, now() - interval '9 days', :u4_account_no, now() - interval '9 days', 'ミニの散歩', '近所の散歩道で撮った写真です。', 'feature', '{}', 1, 'published', 1),
  (:u4_account_no, 2, :u4_account_no, now() - interval '8 days', :u4_account_no, now() - interval '8 days', '下書きグリッド', '', 'grid', '{"rows": []}', 1, 'draft', 2),
  (:u4_account_no, 3, :u4_account_no, now() - interval '7 days', :u4_account_no, now() - interval '7 days', '下書きフィーチャー', '', 'feature', '{}', 1, 'draft', 3);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u4_account_no, 1, 1, :u4_account_no, now(), 1, '{"theme": "朝の散歩道"}'),
  (:u4_account_no, 1, 2, :u4_account_no, now(), 2, '{"theme": "夕方の散歩道", "subTheme": "帰り道"}'),
  (:u4_account_no, 3, 3, :u4_account_no, now(), 1, '{"theme": "途中"}');

-- localuser05（special-user）: グリッド型・公開中（2 + 3 = 5枚）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
VALUES (:u5_account_no, 1, :u5_account_no, now() - interval '12 days', :u5_account_no, now() - interval '12 days', '灯台と砂丘', '海辺の風景を巡る旅の記録です。', 'grid',
  '{"rows": [{"pattern": "TWO_EQUAL"}, {"pattern": "THREE_EQUAL"}]}', 1, 'published', 1);
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) VALUES
  (:u5_account_no, 1, 1, :u5_account_no, now(), 1, '{}'),
  (:u5_account_no, 1, 2, :u5_account_no, now(), 2, '{}'),
  (:u5_account_no, 1, 3, :u5_account_no, now(), 3, '{}'),
  (:u5_account_no, 1, 4, :u5_account_no, now(), 4, '{}'),
  (:u5_account_no, 1, 5, :u5_account_no, now(), 5, '{}');
INSERT INTO photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) VALUES
  (:u5_account_no, 1, 1, :u5_account_no, now(), '旅', 'travel');

-- localuser05 P2〜P22: 一覧のページング確認用（1ページ20件。P1と合わせて公開中22件。special-userのため作成数上限なし）
-- 表示順は番号順とし、更新日時は番号が大きいほど新しくする（更新日時の新しい順ではなく表示順に並ぶことを確認できるようにする）
INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn, display_order)
SELECT :u5_account_no, n, :u5_account_no, now() - interval '11 days', :u5_account_no, now() - ((23 - n) || ' hours')::interval,
  'ページング確認用のポートフォリオ' || n, 'ポートフォリオ一覧のページング確認用のポートフォリオです。', 'grid',
  '{"rows": [{"pattern": "TWO_EQUAL"}]}', 1, 'published', n
FROM generate_series(2, 22) AS n;
INSERT INTO photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings)
SELECT :u5_account_no, n, (n + d) % 5 + 1, :u5_account_no, now(), d + 1, '{}'
FROM generate_series(2, 22) AS n, generate_series(0, 1) AS d;
SQL

  # --- お問い合わせ -------------------------------------------------------------------
  # 返信は管理者 localuser03 が投稿したものとする（返信が残るため localuser03 は退会できない状態になる）
  # 管理者の返信投稿時はステータスが replied、ユーザー既読フラグが false になる
  cat <<'SQL'
-- localuser01 No.1〜15: 一覧のページング確認用（1ページ20件。No.16〜22と合わせて22件）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
SELECT :u1_account_no, n, :u1_account_no, now() - ((76 - n) || ' days')::interval, :u1_account_no,
  now() - ((76 - n) || ' days')::interval + CASE WHEN n % 3 = 0 THEN interval '1 day' ELSE interval '0' END,
  'ページング確認用のお問い合わせ' || n, 'お問い合わせ一覧のページング確認用のお問い合わせです。',
  (CASE n % 3 WHEN 0 THEN 'replied' WHEN 1 THEN 'unreplied' ELSE 'withdrawn' END)::common.inquiry_status_enum, true
FROM generate_series(1, 15) AS n;
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
SELECT id, 1, :u3_account_no, :u3_account_no, created_at + interval '1 day', 'お問い合わせありがとうございます。内容を確認いたしました。'
FROM common.inquiry_mst
WHERE account_no = :u1_account_no AND inquiry_no <= 15 AND status_kbn = 'replied';

-- localuser01 No.16: 回答済み・既読
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 16, :u1_account_no, now() - interval '10 days', :u3_account_no, now() - interval '9 days', '写真に登録できるタグの上限について', E'1枚の写真に登録できるタグは何件までですか？\nまとめて登録したいと考えています。', 'replied', true)
RETURNING id \gset q16_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
VALUES (:q16_id, 1, :u3_account_no, :u3_account_no, now() - interval '9 days', '1枚の写真に登録できるタグは20件までです。');

-- localuser01 No.17: 取り下げ（返信あり）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 17, :u1_account_no, now() - interval '8 days', :u1_account_no, now() - interval '6 days', '退会方法について', '退会の手順を教えてください。', 'withdrawn', true)
RETURNING id \gset q17_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
VALUES (:q17_id, 1, :u3_account_no, :u3_account_no, now() - interval '7 days', 'アカウント設定画面の「退会」から手続きできます。');

-- localuser01 No.18: 取り下げ（返信なし）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 18, :u1_account_no, now() - interval '7 days', :u1_account_no, now() - interval '7 days', 'ログインできません（自己解決）', 'パスワードを思い出したため取り下げます。', 'withdrawn', true);

-- localuser01 No.19: 回答済み・管理者の返信3件（複数回のやり取り）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 19, :u1_account_no, now() - interval '6 days', :u3_account_no, now() - interval '3 days', '写真のアップロードに失敗します', E'5MBを超える写真をアップロードするとエラーになります。\n対処方法を教えてください。', 'replied', true)
RETURNING id \gset q19_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body) VALUES
  (:q19_id, 1, :u3_account_no, :u3_account_no, now() - interval '5 days', 'アップロードできる写真は1ファイルあたり5MBまでです。'),
  (:q19_id, 2, :u3_account_no, :u3_account_no, now() - interval '4 days', E'画像を縮小してから再度お試しください。\n縮小後も失敗する場合はお知らせください。'),
  (:q19_id, 3, :u3_account_no, :u3_account_no, now() - interval '3 days', 'その後の状況はいかがでしょうか。');

-- localuser01 No.20: 回答済み・未読（一覧に未読バッジが表示される）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 20, :u1_account_no, now() - interval '5 days', :u3_account_no, now() - interval '1 day', 'ギャラリーの並び順について', 'お気に入り数順に並べ替えた際の、同数の写真の並び順を教えてください。', 'replied', false)
RETURNING id \gset q20_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
VALUES (:q20_id, 1, :u3_account_no, :u3_account_no, now() - interval '1 day', 'お気に入り数が同じ場合は、撮影日時の新しい順に表示されます。');

-- localuser01 No.21: 未対応
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 21, :u1_account_no, now() - interval '3 days', :u1_account_no, now() - interval '3 days', 'ポートフォリオを公開できません', 'グリッド型のポートフォリオで公開ボタンを押すとエラーになります。', 'unreplied', true);

-- localuser01 No.22: 未対応（件名100文字・本文2000文字の上限値）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u1_account_no, 22, :u1_account_no, now() - interval '1 hour', :u1_account_no, now() - interval '1 hour', repeat('件名', 50), repeat(E'本文の行です。\n', 250), 'unreplied', true);

-- localuser04: 未対応・回答済み（未読）・取り下げ
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user) VALUES
  (:u4_account_no, 1, :u4_account_no, now() - interval '4 days', :u4_account_no, now() - interval '4 days', '写真の上限を増やしたい', 'mini-userの写真の上限を増やす方法はありますか？', 'unreplied', true),
  (:u4_account_no, 3, :u4_account_no, now() - interval '2 days', :u4_account_no, now() - interval '2 days', '取り下げたお問い合わせ', '誤って送信しました。', 'withdrawn', true);
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u4_account_no, 2, :u4_account_no, now() - interval '3 days', :u3_account_no, now() - interval '2 hours', 'ポートフォリオを追加したい', 'ポートフォリオを4件以上作成できません。', 'replied', false)
RETURNING id \gset q4_2_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
VALUES (:q4_2_id, 1, :u3_account_no, :u3_account_no, now() - interval '2 hours', 'mini-userのポートフォリオは3件までです。不要なポートフォリオを削除してください。');

-- localuser05: 未対応・回答済み（既読）
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u5_account_no, 1, :u5_account_no, now() - interval '14 days', :u3_account_no, now() - interval '13 days', '位置情報の公開設定について', '位置情報を非公開にした写真は、他のユーザーからどのように見えますか？', 'replied', true)
RETURNING id \gset q5_1_
INSERT INTO common.inquiry_reply_mst (inquiry_id, reply_no, admin_account_no, created_by, created_at, body)
VALUES (:q5_1_id, 1, :u3_account_no, :u3_account_no, now() - interval '13 days', '非公開にした写真は、撮影場所（緯度経度・住所・表示名）が他のユーザーに表示されません。');
INSERT INTO common.inquiry_mst (account_no, inquiry_no, created_by, created_at, updated_by, updated_at, subject, body, status_kbn, is_read_by_user)
VALUES (:u5_account_no, 2, :u5_account_no, now() - interval '30 minutes', :u5_account_no, now() - interval '30 minutes', '新しいテンプレートの追加予定', 'ポートフォリオのテンプレートを追加する予定はありますか？', 'unreplied', true);
SQL
} > "$SQL_FILE"

echo "データベースへ投入します"
docker exec -i "$DB_CONTAINER" psql -q -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 < "$SQL_FILE" >/dev/null

# 画像のオブジェクトキーのプレフィックス（定義順から決めたアカウント番号）が、実際に採番されたアカウント番号と一致するか検証する
MISMATCH_COUNT=$(docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -tA -c \
  "SELECT count(*) FROM photo.photo_mst WHERE split_part(image_file_path, '/', 1) <> account_no::text;")
if [ "$MISMATCH_COUNT" != "0" ]; then
  echo "画像のオブジェクトキーとアカウント番号が一致しない写真が ${MISMATCH_COUNT} 件あります" >&2
  exit 1
fi

echo "MinIOへ画像をアップロードします"
docker run --rm --network "$NETWORK" -v "$IMAGE_DIR:/images" --entrypoint /bin/sh minio/mc:latest -c "
  mc alias set local http://minio:9000 minioadmin minioadmin >/dev/null &&
  mc mirror /images local/${BUCKET} >/dev/null
"

cat <<MSG

投入が完了しました。

ログイン情報（パスワードは全アカウント共通で ${PASSWORD_PLAIN}）:
  localuser01 normal-user    写真25枚（＋論理削除済み1枚）・ポートフォリオ6件・お問い合わせ22件
  localuser02 mini-user      データなし（空の一覧表示の確認用）
  localuser03 administrator  お問い合わせへの返信者（返信が残るため退会不可）
  localuser04 mini-user      写真10枚・ポートフォリオ3件（いずれも作成数上限に到達）・お問い合わせ3件
  localuser05 special-user   写真5枚・ポートフォリオ22件（公開ページのページング確認用）・お問い合わせ2件（作成数上限なし）
  localuser06 normal-user    ログイン失敗回数によりロック中
  localuser07 normal-user    ロック期間経過済み（ログインすると自動解除）
  localuser08 normal-user    管理者ロック中
  localuser09 normal-user    ログイン失敗1回（ロック前）
  localuser10 normal-user    退会済み（削除フラグ）
  localuser11〜25            一覧のページング確認用（normal-user / mini-user を交互）
MSG
