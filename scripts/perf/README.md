# 写真一覧取得APIのパフォーマンス検証（JMeter）

写真一覧取得API（`GET /api/v1/accounts/{accountId}/photos`）の、1ページあたりの取得パフォーマンスをJMeterで計測するための手順。

- `seed-perf-photos.sh` : 検証用の写真ダミーデータを大量投入するスクリプト
- `photo-list-page.jmx` : JMeterテスト計画

## 対象APIについて

- エンドポイント: `GET /api/v1/accounts/{accountId}/photos`（**認証不要の公開API**）
- クエリパラメータ: `directionKbn`（向き） / `isFavorite` / `tagList` / `sortBy` / `pageNo`
- 1ページの件数は`application.yml`の`app.photo.photoCountPerPage`で固定（既定20件）
- `pageNo`はオフセットページングのため、深いページほど遅くなりうる実装（`PhotoListRequest`のコメント参照）。本手順は浅いページ（`pageNo=1`）の計測を対象とする

## 事前準備

### 1. JMeterのインストール

```bash
brew install jmeter
```

### 2. DB起動

```bash
just db-up
```

### 3. レート制限を無効化してbackendを起動

`RateLimitFilter`により`/api/**`（GENERALカテゴリ）は既定300回/60秒/IPの上限があり、JMeterの負荷計測では即座に429で弾かれる。検証時は無効化して起動する。

```bash
RATE_LIMIT_ENABLED=false just backend-run
```

### 4. テストデータ投入

専用テストアカウント（`perftestuser01`）へ写真ダミーデータを投入する。既存のアカウント・写真データには影響しない。

```bash
./scripts/perf/seed-perf-photos.sh
```

デフォルトは10,000件。件数を変えたい場合は環境変数で指定する。

```bash
PHOTO_COUNT=50000 ./scripts/perf/seed-perf-photos.sh
```

再実行すると、同名テストアカウントのデータを削除してから作り直す（冪等）。

## JMeterの実行

### GUIで内容を確認・調整する場合

```bash
jmeter -t scripts/perf/photo-list-page.jmx
```

Test Plan直下のUser Defined Variables（`HOST` / `PORT` / `ACCOUNT_ID` / `PAGE_NO`）や、Thread Groupの同時ユーザー数・ランプアップ時間・ループ回数（既定: 50ユーザー、ランプアップ10秒、ループ100回 = 計5,000リクエスト）を調整できる。

### CLIモードで負荷を計測する場合

GUIモードは負荷生成には使わず、実測定は必ずCLIモードで行う。

```bash
jmeter -n -t scripts/perf/photo-list-page.jmx -l scripts/perf/result.jtl -e -o scripts/perf/report
```

- `-l` : 生の結果を`.jtl`に出力
- `-e -o` : 実行後にHTMLレポートを`report/`ディレクトリへ生成

### 結果の確認

```bash
open scripts/perf/report/index.html
```

レスポンスタイムの平均・90/95/99パーセンタイル、スループット、エラー率を確認する。サーバー側のボトルネック調査には、Spring Boot Actuatorのメトリクスや PostgreSQL のスロークエリログも併せて確認する。

## 後片付け

検証用データが不要になったら、DB上で`account_id = 'perftestuser01'`のアカウントを`seed-perf-photos.sh`と同様の手順（`photo_favorite` → `photo_tag_mst` → `photo_mst` → `location_mst` → `refresh_token` → `account`の順）で削除する。
