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
	// Region は S3 のリージョン（AWS_REGION）。
	Region string
	// Bucket は写真を保存しているバケット名（APP_S3_BUCKET）。
	Bucket string
	// AccessKey は署名に使うアクセスキー（APP_S3_ACCESS_KEY）。
	AccessKey string
	// SecretKey は署名に使うシークレットキー（APP_S3_SECRET_KEY）。
	SecretKey string
	// PathStyleAccess はパス形式（エンドポイント/バケット/キー）の URL を使うかどうか（MinIO では true）。
	PathStyleAccess bool
	// Endpoint は S3 互換ストレージのエンドポイント（AWS の S3 を使う場合は空）。
	Endpoint string
	// PublicBaseURL はブラウザから到達できるエンドポイント。署名付きURLのホストに使う
	// （Java の S3ClientConfig#resolvePresignEndpoint と同じく、未設定なら Endpoint を使う）。
	PublicBaseURL string
	// Expiry は署名付きURLの有効期限（APP_S3_PRESIGN_EXPIRY_SECONDS）。
	Expiry time.Duration
}

// Presigner は usecase.URLSigner の S3 実装。
type Presigner struct {
	// client は署名付きURLを発行する S3 のクライアント。
	client *s3.PresignClient
	// bucket は写真を保存しているバケット名。
	bucket string
	// expiry は署名付きURLの有効期限。
	expiry time.Duration
}

// NewPresigner は Presigner を生成する。署名はローカルで計算するため、生成時に通信は発生しない。
//
// 引数:
//   - cfg: 署名付きURLの発行に必要な設定
//
// 戻り値:
//   - 生成した Presigner（PublicBaseURL が空の場合は Endpoint をホストに使い、両方空の場合は AWS の S3 を使う）
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
//
// 引数:
//   - ctx: キャンセル・タイムアウトを伝えるコンテキスト
//   - objectKey: 署名付きURLを発行する画像のオブジェクトキー（photo_mst.image_file_path）
//
// 戻り値:
//   - ブラウザが直接画像を取得できる署名付きURL
//   - 署名の計算に失敗した場合はエラー
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

// emptyToNil は空文字を nil に置き換える（AWS SDK の省略可能な設定項目へ渡すため）。
//
// 引数:
//   - value: 変換する文字列
//
// 戻り値:
//   - value が空文字の場合は nil、それ以外は value へのポインタ
func emptyToNil(value string) *string {
	if value == "" {
		return nil
	}
	return &value
}
