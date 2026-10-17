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
	Env Env
	// Port は HTTP サーバーの待ち受けポート。
	Port string
	// DatabaseURL は PostgreSQL の接続文字列（読み取り専用ロール portfolio_reader で接続する）。
	DatabaseURL string
	Storage     storage.Config
	// MaxQueryComplexity は GraphQL クエリの複雑度の上限。
	MaxQueryComplexity int
	// MaxQueryDepth は GraphQL クエリのネストの深さの上限。
	MaxQueryDepth int
}

// IntrospectionEnabled は GraphQL のイントロスペクションを許可するかどうか（ローカル環境のみ許可する）。
func (c Config) IntrospectionEnabled() bool {
	return c.Env == EnvLocal
}

// Load は環境変数から設定を読み込む。
func Load() (Config, error) {
	return load(os.Getenv)
}

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

func valueOr(value, fallback string) string {
	if value == "" {
		return fallback
	}
	return value
}

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
