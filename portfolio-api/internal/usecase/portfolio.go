// Package usecase は、公開ページ向けにポートフォリオを取得するユースケースを定義する。
// DB・ストレージへのアクセスはインターフェース（ポート）越しに行い、実装は infrastructure 層に置く。
package usecase

import (
	"context"
	"fmt"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
)

// PortfolioRepository は公開中のポートフォリオを読み取るリポジトリ（読み取り専用）。
type PortfolioRepository interface {
	// FindAccountNo はアカウントIDからアカウント番号を取得する。存在しない場合は found=false を返す。
	FindAccountNo(ctx context.Context, accountID string) (accountNo int64, found bool, err error)
	// ListPublished はアカウントの公開中のポートフォリオ一覧を、表示順に取得する。
	// keyword が空でない場合は、テーマ・タグ名のいずれかに部分一致するものに絞り込む。
	ListPublished(ctx context.Context, accountNo int64, keyword string) ([]domain.PortfolioSummary, error)
	// FindPublished は公開中のポートフォリオを取得する。存在しない・公開されていない場合は nil を返す。
	FindPublished(ctx context.Context, accountNo, portfolioNo int64) (*domain.Portfolio, error)
}

// URLSigner は画像のオブジェクトキーから、ブラウザが直接取得できる署名付きURLを発行する。
type URLSigner interface {
	Sign(ctx context.Context, objectKey string) (string, error)
}

// PublishedPortfolio は公開中のポートフォリオと、テンプレートごとのレイアウト。
type PublishedPortfolio struct {
	Portfolio domain.Portfolio
	Layout    domain.Layout
}

// PortfolioQuery は公開ページ向けにポートフォリオを取得するユースケース。
type PortfolioQuery struct {
	repository PortfolioRepository
	signer     URLSigner
}

// NewPortfolioQuery は PortfolioQuery を生成する。
func NewPortfolioQuery(repository PortfolioRepository, signer URLSigner) *PortfolioQuery {
	return &PortfolioQuery{repository: repository, signer: signer}
}

// ListPublishedPortfolios はアカウントの公開中のポートフォリオ一覧を取得する。
//
// keyword（前後の空白は除く）が空でない場合は、テーマ・タグ名のいずれかに部分一致するものに絞り込む。
// アカウントIDの形式が不正な場合・アカウントが存在しない場合・検索語が長すぎる場合は、エラーではなく空の一覧を返す
// （公開ページで「ポートフォリオがない」場合と区別せず、アカウントの存在有無を推測させない）。
func (q *PortfolioQuery) ListPublishedPortfolios(ctx context.Context, accountID, keyword string) ([]domain.PortfolioSummary, error) {
	normalizedKeyword, ok := domain.NormalizeKeyword(keyword)
	if !domain.IsValidAccountID(accountID) || !ok {
		return []domain.PortfolioSummary{}, nil
	}
	accountNo, found, err := q.repository.FindAccountNo(ctx, accountID)
	if err != nil {
		return nil, fmt.Errorf("find account: %w", err)
	}
	if !found {
		return []domain.PortfolioSummary{}, nil
	}

	summaries, err := q.repository.ListPublished(ctx, accountNo, normalizedKeyword)
	if err != nil {
		return nil, fmt.Errorf("list published portfolios: %w", err)
	}
	for i := range summaries {
		if summaries[i].CoverPhoto == nil {
			continue
		}
		if err := q.signPhoto(ctx, summaries[i].CoverPhoto); err != nil {
			return nil, err
		}
	}
	return summaries, nil
}

// GetPublishedPortfolio は公開中のポートフォリオを、テンプレートごとのレイアウトとあわせて取得する。
//
// アカウントIDの形式・ポートフォリオ番号が不正な場合、存在しない・公開されていない場合は nil を返す。
func (q *PortfolioQuery) GetPublishedPortfolio(ctx context.Context, accountID string, portfolioNo int64) (*PublishedPortfolio, error) {
	if !domain.IsValidAccountID(accountID) || portfolioNo <= 0 {
		return nil, nil
	}
	accountNo, found, err := q.repository.FindAccountNo(ctx, accountID)
	if err != nil {
		return nil, fmt.Errorf("find account: %w", err)
	}
	if !found {
		return nil, nil
	}

	portfolio, err := q.repository.FindPublished(ctx, accountNo, portfolioNo)
	if err != nil {
		return nil, fmt.Errorf("find published portfolio: %w", err)
	}
	if portfolio == nil {
		return nil, nil
	}
	for i := range portfolio.Photos {
		if err := q.signPhoto(ctx, &portfolio.Photos[i].Photo); err != nil {
			return nil, err
		}
	}

	layout, err := domain.BuildLayout(*portfolio)
	if err != nil {
		return nil, err
	}
	return &PublishedPortfolio{Portfolio: *portfolio, Layout: layout}, nil
}

func (q *PortfolioQuery) signPhoto(ctx context.Context, photo *domain.Photo) error {
	url, err := q.signer.Sign(ctx, photo.ImageFilePath)
	if err != nil {
		return fmt.Errorf("sign photo url (photoNo=%d): %w", photo.PhotoNo, err)
	}
	photo.URL = url
	return nil
}
