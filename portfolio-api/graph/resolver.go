// Package graph は GraphQL のスキーマ（schema.graphqls）と、gqlgen が生成したコード・リゾルバを持つ。
//
// スキーマを変更したら `go generate ./...` でコードを再生成する（generated.go・model/models_gen.go は手で編集しない）。
package graph

//go:generate go tool gqlgen generate

import (
	"context"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/usecase"
)

// PortfolioQuery はリゾルバが使うユースケース（テストで差し替えられるようインターフェースで受け取る）。
type PortfolioQuery interface {
	ListPublishedPortfolios(ctx context.Context, accountID, keyword string, limit, offset int) (usecase.PortfolioSummaryPage, error)
	GetPublishedPortfolio(ctx context.Context, accountID string, portfolioNo int64) (*usecase.PublishedPortfolio, error)
}

// Resolver はリゾルバの依存関係を保持する。
type Resolver struct {
	PortfolioQuery PortfolioQuery
}
