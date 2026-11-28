// Package config は環境変数から設定を読み込む。
//
// S3 関連の環境変数名は Java のバックエンド（application-*.yml の app.s3.*）と揃え、同じ値を共有できるようにする。
// ローカル開発用の既定値（docker-compose の PostgreSQL・MinIO への接続情報）は、
// PORTFOLIO_API_ENV=local の場合にだけ適用する（本番で既定値のまま起動してしまうのを防ぐ）。
package config

import (
	"fmt"
	"os"
	"strconv"
	"time"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/infrastructure/storage"
)

// Env は実行環境。
type Env string

const (
	// EnvLocal はローカル開発・E2E 環境。ローカル用の既定値の適用と、GraphQL のイントロスペクションを許可する。
	EnvLocal Env = "local"
	// EnvProd は本番環境。
	EnvProd Env = "prod"
)

// Config はアプリケーションの設定。
type Config struct {
	// Env は実行環境（PORTFOLIO_API_ENV。未設定の場合は本番）。
	Env Env
	// Port は HTTP サーバーの待ち受けポート。
	Port string
	// DatabaseURL は PostgreSQL の接続文字列（読み取り専用ロール portfolio_reader で接続する）。
	DatabaseURL string
	// Storage は署名付きURLの発行に使う S3 の設定。
	Storage storage.Config
	// MaxQueryComplexity は GraphQL クエリの複雑度の上限。
	MaxQueryComplexity int
	// MaxQueryDepth は GraphQL クエリのネストの深さの上限。
	MaxQueryDepth int
}

// IntrospectionEnabled は GraphQL のイントロスペクションを許可するかどうか（ローカル環境のみ許可する）。
//
// 戻り値:
//   - 実行環境がローカルの場合は true、本番の場合は false
func (c Config) IntrospectionEnabled() bool {
	return c.Env == EnvLocal
}

// Load は環境変数から設定を読み込む。
//
// 戻り値:
//   - 読み込んだ設定
//   - 環境変数の値が不正な場合・必須の設定がない場合はエラー
func Load() (Config, error) {
	return load(os.Getenv)
}

// load は環境変数を取得する関数を使って設定を読み込む（テストで環境変数を差し替えられるよう Load から切り出している）。
//
// 未設定の項目には既定値を使い、ローカル用の既定値は実行環境がローカルの場合にだけ適用する。
//
// 引数:
//   - getenv: 環境変数名から値を取得する関数（未設定の場合は空文字を返すもの）
//
// 戻り値:
//   - 読み込んだ設定
//   - 実行環境が未知の場合・整数の設定値が正の整数でない場合・必須の設定（DB の接続文字列、S3 のバケット・認証情報）がない場合はエラー
func load(getenv func(string) string) (Config, error) {
	env := Env(valueOr(getenv("PORTFOLIO_API_ENV"), string(EnvProd)))
	if env != EnvLocal && env != EnvProd {
		return Config{}, fmt.Errorf("PORTFOLIO_API_ENV must be %q or %q: %q", EnvLocal, EnvProd, env)
	}
	// ローカル環境の既定値（docker-compose の PostgreSQL・MinIO。backend の application-local.yml と同じ値）
	localDefault := func(value string) string {
		if env == EnvLocal {
			return value
		}
		return ""
	}

	expirySeconds, err := intValue(getenv, "APP_S3_PRESIGN_EXPIRY_SECONDS", 900)
	if err != nil {
		return Config{}, err
	}
	maxComplexity, err := intValue(getenv, "PORTFOLIO_API_MAX_QUERY_COMPLEXITY", 300)
	if err != nil {
		return Config{}, err
	}
	maxDepth, err := intValue(getenv, "PORTFOLIO_API_MAX_QUERY_DEPTH", 8)
	if err != nil {
		return Config{}, err
	}

	cfg := Config{
		Env:  env,
		Port: valueOr(getenv("PORTFOLIO_API_PORT"), "8081"),
		DatabaseURL: valueOr(getenv("PORTFOLIO_API_DB_URL"),
			localDefault("postgres://portfolio_reader:portfolio_reader@localhost:5432/web_gallery?sslmode=disable")),
		Storage: storage.Config{
			Region:          valueOr(getenv("AWS_REGION"), "ap-northeast-1"),
			Bucket:          valueOr(getenv("APP_S3_BUCKET"), localDefault("web-gallery-local")),
			AccessKey:       valueOr(getenv("APP_S3_ACCESS_KEY"), localDefault("minioadmin")),
			SecretKey:       valueOr(getenv("APP_S3_SECRET_KEY"), localDefault("minioadmin")),
			PathStyleAccess: valueOr(getenv("APP_S3_PATH_STYLE_ACCESS"), localDefault("true")) == "true",
			Endpoint:        valueOr(getenv("APP_S3_ENDPOINT"), localDefault("http://localhost:9000")),
			PublicBaseURL:   valueOr(getenv("APP_S3_PUBLIC_BASE_URL"), localDefault("http://localhost:9000")),
			Expiry:          time.Duration(expirySeconds) * time.Second,
		},
		MaxQueryComplexity: maxComplexity,
		MaxQueryDepth:      maxDepth,
	}

	for name, value := range map[string]string{
		"PORTFOLIO_API_DB_URL": cfg.DatabaseURL,
		"APP_S3_BUCKET":        cfg.Storage.Bucket,
		"APP_S3_ACCESS_KEY":    cfg.Storage.AccessKey,
		"APP_S3_SECRET_KEY":    cfg.Storage.SecretKey,
	} {
		if value == "" {
			return Config{}, fmt.Errorf("%s is required", name)
		}
	}
	return cfg, nil
}

// valueOr は値が空文字の場合に代わりの値を返す。
//
// 引数:
//   - value: 環境変数から取得した値
//   - fallback: value が空文字の場合に使う値
//
// 戻り値:
//   - value が空文字の場合は fallback、それ以外は value
func valueOr(value, fallback string) string {
	if value == "" {
		return fallback
	}
	return value
}

// intValue は環境変数から正の整数の設定値を読み込む。
//
// 引数:
//   - getenv: 環境変数名から値を取得する関数
//   - name: 読み込む環境変数名
//   - fallback: 環境変数が未設定の場合に使う値
//
// 戻り値:
//   - 環境変数の値（未設定の場合は fallback）
//   - 値が整数でない・0以下の場合はエラー
func intValue(getenv func(string) string, name string, fallback int) (int, error) {
	raw := getenv(name)
	if raw == "" {
		return fallback, nil
	}
	value, err := strconv.Atoi(raw)
	if err != nil || value <= 0 {
		return 0, fmt.Errorf("%s must be a positive integer: %q", name, raw)
	}
	return value, nil
}
