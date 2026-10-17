// Package storage は S3（ローカル/E2E は MinIO）の画像に対して、署名付きURLを発行する。
package storage

import (
	"context"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
)

// Config は署名付きURLの発行に必要な設定。環境変数名は Java のバックエンド（app.s3.*）と揃える。
type Config struct {
	Region          string
	Bucket          string
	AccessKey       string
	SecretKey       string
	PathStyleAccess bool
	// Endpoint は S3 互換ストレージのエンドポイント（AWS の S3 を使う場合は空）。
	Endpoint string
	// PublicBaseURL はブラウザから到達できるエンドポイント。署名付きURLのホストに使う
	// （Java の S3ClientConfig#resolvePresignEndpoint と同じく、未設定なら Endpoint を使う）。
	PublicBaseURL string
	Expiry        time.Duration
}

// Presigner は usecase.URLSigner の S3 実装。
type Presigner struct {
	client *s3.PresignClient
	bucket string
	expiry time.Duration
}

// NewPresigner は Presigner を生成する。署名はローカルで計算するため、生成時に通信は発生しない。
func NewPresigner(cfg Config) *Presigner {
	endpoint := cfg.PublicBaseURL
	if endpoint == "" {
		endpoint = cfg.Endpoint
	}
	client := s3.New(s3.Options{
		Region:       cfg.Region,
		Credentials:  aws.NewCredentialsCache(credentials.NewStaticCredentialsProvider(cfg.AccessKey, cfg.SecretKey, "")),
		UsePathStyle: cfg.PathStyleAccess,
		BaseEndpoint: emptyToNil(endpoint),
	})
	return &Presigner{client: s3.NewPresignClient(client), bucket: cfg.Bucket, expiry: cfg.Expiry}
}

// Sign はオブジェクトキーから、有効期限付きの署名付きURL（GET）を発行する。
func (p *Presigner) Sign(ctx context.Context, objectKey string) (string, error) {
	request, err := p.client.PresignGetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(p.bucket),
		Key:    aws.String(objectKey),
	}, s3.WithPresignExpires(p.expiry))
	if err != nil {
		return "", err
	}
	return request.URL, nil
}

func emptyToNil(value string) *string {
	if value == "" {
		return nil
	}
	return &value
}
