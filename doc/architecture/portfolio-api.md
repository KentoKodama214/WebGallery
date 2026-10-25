# portfolio-api（公開ページ向けのポートフォリオ表示API）

## 概要

`portfolio-api/` は、公開中のポートフォリオを公開ページ（Next.js）へ返す、読み取り専用の GraphQL API です（Go）。
ポートフォリオの設定（登録・更新・公開状態の変更）は Java のバックエンド（`/api/v1/accounts/{accountId}/portfolios`）が担い、
portfolio-api は書き込みを一切行いません。

```
[設定系：要ログイン・本人のみ]  Next.js(設定画面) ─/api/*─▶ Java/Spring Boot ─▶ PostgreSQL（書き込み・スキーマ所有）
[表示系：公開・未ログイン可]    Next.js(公開ページ, Server Component) ─server-side─▶ portfolio-api(GraphQL) ─▶ PostgreSQL（読み取り専用ロール）
                                                                                              └▶ S3（署名付きURL発行）
```

- DB は Java のバックエンドと共有し、読み取り専用ロール `portfolio_reader`（`db/photo/portfolio_reader.sql`）で接続します。
  テーブル定義（DDL）の所有者は `db/` と Java 側に一本化します
- 画像は S3（ローカル/E2E は MinIO）の署名付きURLを portfolio-api 自身が発行します。環境変数名は Java（`app.s3.*`）と揃えています
- Next.js のサーバーからだけ呼び出し、ブラウザには公開しません（CORS は設定しない）

## レイヤー構成

Java のバックエンドと同じく、依存は内側（domain）へ向かいます。

| パッケージ | 役割 |
|---|---|
| `internal/domain` | ドメインモデル（ポートフォリオ・写真・タグ）と、テンプレートごとのレイアウトを組み立てるルール（`BuildLayout`）。外部ライブラリに依存しない |
| `internal/usecase` | 公開中のポートフォリオを取得するユースケース。リポジトリ・署名付きURL発行はインターフェース（ポート）越しに使う |
| `internal/infrastructure/postgres` | リポジトリの実装（pgx。SELECT のみ）。一覧のタグは一括で取得し N+1 を避ける |
| `internal/infrastructure/storage` | 署名付きURL発行の実装（aws-sdk-go-v2） |
| `graph` | GraphQL スキーマ（`schema.graphqls`）・gqlgen の生成コード・リゾルバ・ドメインモデルから GraphQL モデルへの変換 |
| `internal/server` | HTTP ハンドラ（GraphQL・ヘルスチェック）と、クエリの深さの上限などの防御 |
| `internal/config` | 環境変数からの設定の読み込み |
| `cmd/server` | エントリポイント |

## GraphQL スキーマ

スキーマの正本は `portfolio-api/graph/schema.graphqls` です。ローカル環境（`PORTFOLIO_API_ENV=local`）ではイントロスペクションが有効です。

```graphql
type Query {
  publishedPortfolios(accountId: ID!, keyword: String): [PortfolioSummary!]!
  publishedPortfolio(accountId: ID!, portfolioNo: Int!): Portfolio
}

union PortfolioLayout = FeatureLayout | GridLayout
```

- 公開状態（`published`）のポートフォリオだけを返します（ポートフォリオは削除すると物理削除される）。下書き・存在しないポートフォリオは
  `publishedPortfolio` が `null`、`publishedPortfolios` は空の一覧になります（アカウントの存在有無も区別しない）
- 削除済みの写真は返しません
- `publishedPortfolios` は所有者が設定画面で並べた表示順（`portfolio_mst.display_order`）に返します。
  `keyword` を指定すると、テーマ・タグ名（日本語・英語）のいずれかに部分一致（大文字・小文字を区別しない）するものに絞り込みます
- テンプレートごとのレイアウトは `layout` の union 型で返し、`__typename` で判別します

## テンプレートを追加する手順

1. Java 側：`PortfolioTemplateEnum` に値を追加し、DB の `photo.portfolio_template_enum` 型にも値を追加する（`ALTER TYPE ... ADD VALUE`）。
   テンプレート固有の設定を検証する Policy を追加し、`PortfolioTemplateSettingsPolicy` の振り分けに加える
2. portfolio-api：
   1. `internal/domain` にレイアウトの型を追加し、`BuildLayout` の分岐に加える
   2. `graph/schema.graphqls` にレイアウトの型を追加して `PortfolioLayout` の union に加え、`just go-generate` で再生成する
   3. `graph/mapper.go` の `toLayout`・`toTemplate` に変換を加える
3. Next.js：
   1. 設定画面（`src/app/[accountId]/portfolio_setting/edit/`）に、テンプレート固有の設定の入力を追加する
   2. `src/lib/portfolio-api/types.ts` にレイアウトの型を追加して `PortfolioLayout` に加え、
      `queries.ts` の `layout` にインラインフラグメント（`... on XxxLayout { ... }`）を加える。型・クエリは手書きのため、スキーマと揃えること
   3. `src/components/portfolio/templates/` にテンプレートのコンポーネントを追加し、`registry.tsx` の `TEMPLATE_RENDERERS` に加える
      （`PortfolioLayout` の全ての `__typename` を網羅しないと型エラーになる）。未対応の型を受け取った場合は「このポートフォリオは表示できません」を表示する

## セキュリティ

| 対策 | 内容 |
|---|---|
| 公開範囲 | 公開中のポートフォリオだけを返す（クエリの WHERE 句で常に `status_kbn = 'published'`。写真は `photo_mst.is_deleted = false` のものだけを結合する） |
| DB 権限 | 読み取り専用ロール `portfolio_reader`。`common.account` は `account_no`・`account_id` の列だけ、`photo.photo_mst` は表示に必要な列だけ SELECT を許可し、パスワード・撮影場所等は読めない |
| クエリの複雑度 | `PORTFOLIO_API_MAX_QUERY_COMPLEXITY`（既定 300）を超えるクエリは実行しない |
| クエリの深さ | `PORTFOLIO_API_MAX_QUERY_DEPTH`（既定 8）を超えるクエリは実行しない（フラグメントは展開して数える） |
| イントロスペクション | ローカル環境（`PORTFOLIO_API_ENV=local`）以外では無効 |
| リクエストサイズ | 64KB を超えるリクエストボディは受け付けない（申告サイズ超過は 413） |
| エラー | リゾルバの内部エラー（DB・ストレージの障害等）は詳細を返さず `internal server error` とし、詳細はログにだけ残す |
| Mutation | 持たない（GET でも問い合わせできるが、状態は変化しない） |

## キャッシュ

公開ページ（`frontend/src/lib/portfolio-api/client.ts`）は GET（`/graphql?query=...`）で問い合わせ、
**Next.js 側ではキャッシュしません**（`cache: "no-store"`）。

レスポンスに含む画像の署名付きURLには有効期限（`APP_S3_PRESIGN_EXPIRY_SECONDS`、既定 900 秒）があります。
Next.js のデータキャッシュは stale-while-revalidate のため、再検証間隔を有効期限より短くしても、
しばらくアクセスのなかったページでは期限切れの URL を一度返してしまい、画像が表示されません。
そのため毎リクエストで問い合わせて URL を発行し直します。公開状態の変更（下書きへの戻し等）も即座に反映されます。

キャッシュを導入する場合は、署名付きURLを含まない形（画像のキーだけをキャッシュし、URL はリクエストごとに発行する等）にしてください。

## 環境変数

| 環境変数 | 既定値（`PORTFOLIO_API_ENV=local` の場合） | 説明 |
|---|---|---|
| `PORTFOLIO_API_ENV` | `prod` | `local` の場合だけ、下記のローカル用の既定値の適用とイントロスペクションを有効にする |
| `PORTFOLIO_API_PORT` | `8081` | 待ち受けポート |
| `PORTFOLIO_API_DB_URL` | `postgres://portfolio_reader:portfolio_reader@localhost:5432/web_gallery?sslmode=disable` | PostgreSQL の接続文字列（必須） |
| `APP_S3_BUCKET` | `web-gallery-local` | バケット（必須） |
| `APP_S3_ACCESS_KEY` / `APP_S3_SECRET_KEY` | `minioadmin` | 認証情報（必須） |
| `APP_S3_ENDPOINT` | `http://localhost:9000` | S3 互換ストレージのエンドポイント（AWS の S3 では空） |
| `APP_S3_PUBLIC_BASE_URL` | `http://localhost:9000` | 署名付きURLに使う、ブラウザから到達できるエンドポイント（未設定時は `APP_S3_ENDPOINT`） |
| `APP_S3_PATH_STYLE_ACCESS` | `true` | パス形式でアクセスするか |
| `AWS_REGION` | `ap-northeast-1` | リージョン |
| `APP_S3_PRESIGN_EXPIRY_SECONDS` | `900` | 署名付きURLの有効期限（秒） |
| `PORTFOLIO_API_MAX_QUERY_COMPLEXITY` | `300` | クエリの複雑度の上限 |
| `PORTFOLIO_API_MAX_QUERY_DEPTH` | `8` | クエリの深さの上限 |

本番では `portfolio_reader` ロールのパスワードを、`db/photo/portfolio_reader.sql` のローカル用の値から変更してください。

`db/photo/portfolio_reader.sql` はDBの初期化時（ボリュームが空のとき）にだけ実行されます。
このファイルの追加より前に作成したローカルのDBボリュームでは、`psql` で手動実行するか、ボリュームを作り直してください。

Next.js（公開ページ）からの接続先は `PORTFOLIO_API_URL`（既定 `http://localhost:8081/graphql`）で設定します。

## エンドポイント

| パス | 説明 |
|---|---|
| `GET /graphql`・`POST /graphql` | GraphQL |
| `GET /healthz` | ヘルスチェック（DB に接続できない場合は 503） |

## 開発コマンド

```bash
just go-run              # ローカル環境で起動（要 just db-up）
just go-test             # 単体テスト
just go-integrationTest  # 結合テスト（テスト用DB 5433 に読み取り専用ロールで接続）
just go-lint             # gofmt・go vet
just go-generate         # GraphQL のコード生成（schema.graphqls を変更したら実行）
```

本番用のイメージは `docker build -t portfolio-api ./portfolio-api` でビルドできます（distroless、非root）。
