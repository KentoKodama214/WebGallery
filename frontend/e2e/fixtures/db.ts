/**
 * E2E実行環境のPostgreSQL接続情報。
 *
 * `docker-compose.yml`のpostgres-dbサービス（ローカル）・CIの`postgres`サービスコンテナは
 * いずれも `localhost:5432/web_gallery`（postgres/postgres）に固定されているため、
 * `scripts/e2e.sh`のバックエンド起動設定と同じ既定値を用いる。
 */
export const DB_CONFIG = {
  host: "localhost",
  port: 5432,
  database: "web_gallery",
  user: "postgres",
  password: "postgres",
};
