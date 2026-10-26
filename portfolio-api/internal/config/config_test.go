package config

import (
	"strings"
	"testing"
	"time"
)

func getenvFrom(values map[string]string) func(string) string {
	return func(key string) string { return values[key] }
}

func TestLoad_LocalDefaults(t *testing.T) {
	cfg, err := load(getenvFrom(map[string]string{"PORTFOLIO_API_ENV": "local"}))
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if !cfg.IntrospectionEnabled() {
		t.Error("introspection should be enabled in local")
	}
	if cfg.Port != "8081" || !strings.Contains(cfg.DatabaseURL, "portfolio_reader") {
		t.Errorf("unexpected defaults: %+v", cfg)
	}
	if cfg.Storage.Bucket != "web-gallery-local" || !cfg.Storage.PathStyleAccess || cfg.Storage.PublicBaseURL != "http://localhost:9000" {
		t.Errorf("unexpected storage defaults: %+v", cfg.Storage)
	}
	if cfg.Storage.Expiry != 900*time.Second || cfg.MaxQueryComplexity != 300 || cfg.MaxQueryDepth != 8 {
		t.Errorf("unexpected limits: %+v", cfg)
	}
}

func TestLoad_ProdRequiresSettings(t *testing.T) {
	// 本番（既定）ではローカル用の既定値を使わず、必須の設定がなければ起動させない
	_, err := load(getenvFrom(map[string]string{}))
	if err == nil {
		t.Fatal("expected error when required settings are missing in prod")
	}

	cfg, err := load(getenvFrom(map[string]string{
		"PORTFOLIO_API_DB_URL":          "postgres://reader@db/web_gallery",
		"APP_S3_BUCKET":                 "bucket",
		"APP_S3_ACCESS_KEY":             "key",
		"APP_S3_SECRET_KEY":             "secret",
		"APP_S3_PRESIGN_EXPIRY_SECONDS": "300",
	}))
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if cfg.Env != EnvProd || cfg.IntrospectionEnabled() {
		t.Error("introspection should be disabled in prod")
	}
	if cfg.Storage.PathStyleAccess || cfg.Storage.Endpoint != "" || cfg.Storage.PublicBaseURL != "" {
		t.Errorf("local defaults must not be applied in prod: %+v", cfg.Storage)
	}
	if cfg.Storage.Expiry != 300*time.Second {
		t.Errorf("expiry = %v", cfg.Storage.Expiry)
	}
}

func TestLoad_InvalidValues(t *testing.T) {
	for name, values := range map[string]map[string]string{
		"未知の環境":     {"PORTFOLIO_API_ENV": "staging"},
		"整数でない有効期限": {"PORTFOLIO_API_ENV": "local", "APP_S3_PRESIGN_EXPIRY_SECONDS": "abc"},
		"0以下の複雑度上限": {"PORTFOLIO_API_ENV": "local", "PORTFOLIO_API_MAX_QUERY_COMPLEXITY": "0"},
		"負の深さの上限":   {"PORTFOLIO_API_ENV": "local", "PORTFOLIO_API_MAX_QUERY_DEPTH": "-1"},
	} {
		if _, err := load(getenvFrom(values)); err == nil {
			t.Errorf("%s: expected error", name)
		}
	}
}
