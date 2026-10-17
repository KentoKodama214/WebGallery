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
  publishedPortfolios(accountId: ID!): [PortfolioSummary!]!
  publishedPortfolio(accountId: ID!, portfolioNo: Int!): Portfolio
}

union PortfolioLayout = FeatureLayout | GridLayout
```

- 公開状態（`published`）かつ未削除のポートフォリオだけを返します。下書き・削除済み・存在しないポートフォリオは
  `publishedPortfolio` が `null`、`publishedPortfolios` は空の一覧になります（アカウントの存在有無も区別しない）
- 削除済みの写真は返しません
- テンプレートごとのレイアウトは `layout` の union 型で返し、`__typename` で判別します

## テンプレートを追加する手順

1. Java 側：`PortfolioTemplateEnum` に値を追加し、DB の `photo.portfolio_template_enum` 型にも値を追加する（`ALTER TYPE ... ADD VALUE`）。
   テンプレート固有の設定を検証する Policy を追加し、`PortfolioTemplateSettingsPolicy` の振り分けに加える
2. portfolio-api：
   1. `internal/domain` にレイアウトの型を追加し、`BuildLayout` の分岐に加える
   2. `graph/schema.graphqls` にレイアウトの型を追加して `PortfolioLayout` の union に加え、`just go-generate` で再生成する
   3. `graph/mapper.go` の `toLayout`・`toTemplate` に変換を加える
3. Next.js：設定画面の入力と、公開ページのテンプレートのコンポーネントを追加する

## セキュリティ

| 対策 | 内容 |
|---|---|
| 公開範囲 | 公開中のポートフォリオだけを返す（クエリの WHERE 句で常に `status_kbn = 'published' AND is_deleted = false`） |
| DB 権限 | 読み取り専用ロール `portfolio_reader`。`common.account` は `account_no`・`account_id` の列だけ、`photo.photo_mst` は表示に必要な列だけ SELECT を許可し、パスワード・撮影場所等は読めない |
| クエリの複雑度 | `PORTFOLIO_API_MAX_QUERY_COMPLEXITY`（既定 300）を超えるクエリは実行しない |
| クエリの深さ | `PORTFOLIO_API_MAX_QUERY_DEPTH`（既定 8）を超えるクエリは実行しない（フラグメントは展開して数える） |
| イントロスペクション | ローカル環境（`PORTFOLIO_API_ENV=local`）以外では無効 |
| リクエストサイズ | 64KB を超えるリクエストボディは受け付けない（申告サイズ超過は 413） |
| エラー | リゾルバの内部エラー（DB・ストレージの障害等）は詳細を返さず `internal server error` とし、詳細はログにだけ残す |
| Mutation | 持たない（GET でも問い合わせできるが、状態は変化しない） |

## キャッシュ

GET（`/graphql?query=...`）でも問い合わせできるため、Next.js 側で URL 単位にキャッシュできます。
レスポンスに含む画像の署名付きURLには有効期限（`APP_S3_PRESIGN_EXPIRY_SECONDS`、既定 900 秒）があるため、
キャッシュの再検証間隔はこれより短くしてください。

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
