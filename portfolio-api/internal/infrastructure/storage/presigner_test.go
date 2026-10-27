package storage

import (
	"context"
	"net/url"
	"testing"
	"time"
)

// TestPresigner_Sign は、PublicBaseURL を設定した場合に、署名付きURLのホストが PublicBaseURL になり、
// パス形式（/バケット/キー）で発行されること、有効期限（X-Amz-Expires）が設定値になり、署名が付くことを検証する。
func TestPresigner_Sign(t *testing.T) {
	presigner := NewPresigner(Config{
		Region:          "ap-northeast-1",
		Bucket:          "web-gallery-local",
		AccessKey:       "minioadmin",
		SecretKey:       "minioadmin",
		PathStyleAccess: true,
		Endpoint:        "http://minio:9000",
		PublicBaseURL:   "http://localhost:9000",
		Expiry:          900 * time.Second,
	})

	signed, err := presigner.Sign(context.Background(), "1/5-abc.jpg")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	u, err := url.Parse(signed)
	if err != nil {
		t.Fatalf("invalid url: %v", err)
	}
	// ブラウザから到達できるエンドポイント（PublicBaseURL）を、パス形式（/バケット/キー）で使う
	if u.Host != "localhost:9000" || u.Path != "/web-gallery-local/1/5-abc.jpg" {
		t.Errorf("url = %s", signed)
	}
	if got := u.Query().Get("X-Amz-Expires"); got != "900" {
		t.Errorf("X-Amz-Expires = %q, want 900", got)
	}
	if u.Query().Get("X-Amz-Signature") == "" {
		t.Error("signature is missing")
	}
}

// TestPresigner_FallsBackToEndpoint は、PublicBaseURL が未設定の場合に、署名付きURLのホストが Endpoint になることを検証する。
func TestPresigner_FallsBackToEndpoint(t *testing.T) {
	presigner := NewPresigner(Config{
		Region: "ap-northeast-1", Bucket: "bucket", AccessKey: "k", SecretKey: "s",
		PathStyleAccess: true, Endpoint: "http://minio:9000", Expiry: time.Minute,
	})

	signed, err := presigner.Sign(context.Background(), "key.jpg")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if u, _ := url.Parse(signed); u.Host != "minio:9000" {
		t.Errorf("url = %s, want host minio:9000", signed)
	}
}
