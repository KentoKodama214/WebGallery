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
	// ListPublished はアカウントの公開中のポートフォリオ一覧を、表示順に offset 件目から最大 limit 件取得する。
	// keyword が空でない場合は、テーマ・タグ名のいずれかに部分一致するものに絞り込む。
	ListPublished(ctx context.Context, accountNo int64, keyword string, limit, offset int) ([]domain.PortfolioSummary, error)
	// FindPublished は公開中のポートフォリオを取得する。存在しない・公開されていない場合は nil を返す。
	FindPublished(ctx context.Context, accountNo, portfolioNo int64) (*domain.Portfolio, error)
}

// URLSigner は画像のオブジェクトキーから、ブラウザが直接取得できる署名付きURLを発行する。
type URLSigner interface {
	// Sign はオブジェクトキーから署名付きURLを発行する。発行できない場合はエラーを返す。
	Sign(ctx context.Context, objectKey string) (string, error)
}

// PortfolioSummaryPage は公開中のポートフォリオ一覧の1ページ分。
type PortfolioSummaryPage struct {
	// Portfolios は表示順に並べたポートフォリオ（最大 limit 件。該当がない場合は空のスライス）。
	Portfolios []domain.PortfolioSummary
	// HasNext は、このページより後ろに続きのポートフォリオがあるかどうか。
	HasNext bool
}

// PublishedPortfolio は公開中のポートフォリオと、テンプレートごとのレイアウト。
type PublishedPortfolio struct {
	// Portfolio は公開中のポートフォリオ（写真には署名付きURLを設定済み）。
	Portfolio domain.Portfolio
	// Layout は Portfolio のテンプレート・設定から組み立てた表示用のレイアウト。
	Layout domain.Layout
}

// PortfolioQuery は公開ページ向けにポートフォリオを取得するユースケース。
type PortfolioQuery struct {
	// repository は公開中のポートフォリオを読み取るリポジトリ。
	repository PortfolioRepository
	// signer は写真の署名付きURLを発行する。
	signer URLSigner
}

// NewPortfolioQuery は PortfolioQuery を生成する。
//
// 引数:
//   - repository: 公開中のポートフォリオを読み取るリポジトリ
//   - signer: 写真の署名付きURLを発行する実装
//
// 戻り値:
//   - 生成した PortfolioQuery
func NewPortfolioQuery(repository PortfolioRepository, signer URLSigner) *PortfolioQuery {
	return &PortfolioQuery{repository: repository, signer: signer}
}

// ListPublishedPortfolios はアカウントの公開中のポートフォリオ一覧を、表示順に offset 件目から最大 limit 件取得する。
//
// keyword（前後の空白は除く）が空でない場合は、テーマ・タグ名のいずれかに部分一致するものに絞り込む。
// アカウントIDの形式が不正な場合・アカウントが存在しない場合・検索語が長すぎる場合・取得件数や取得開始位置が範囲外の場合は、
// エラーではなく空の一覧を返す（公開ページで「ポートフォリオがない」場合と区別せず、アカウントの存在有無を推測させない）。
//
// 引数:
//   - ctx: キャンセル・タイムアウトを伝えるコンテキスト
//   - accountID: ポートフォリオを所有するアカウントのアカウントID
//   - keyword: 絞り込みの検索語（空文字の場合は絞り込まない）
//   - limit: 一度に取得する件数
//   - offset: 取得開始位置（先頭から読み飛ばす件数）
//
// 戻り値:
//   - 表示順に並べたポートフォリオ（先頭の写真には署名付きURLを設定済み）と、続きの有無
//   - DB への問い合わせ・署名付きURLの発行に失敗した場合はエラー
func (q *PortfolioQuery) ListPublishedPortfolios(ctx context.Context, accountID, keyword string, limit, offset int) (PortfolioSummaryPage, error) {
	empty := PortfolioSummaryPage{Portfolios: []domain.PortfolioSummary{}}
	normalizedKeyword, ok := domain.NormalizeKeyword(keyword)
	if !domain.IsValidAccountID(accountID) || !ok || !domain.IsValidPortfolioPage(limit, offset) {
		return empty, nil
	}
	accountNo, found, err := q.repository.FindAccountNo(ctx, accountID)
	if err != nil {
		return PortfolioSummaryPage{}, fmt.Errorf("find account: %w", err)
	}
	if !found {
		return empty, nil
	}

	// 続きの有無を判定するため、1件多く取得する
	summaries, err := q.repository.ListPublished(ctx, accountNo, normalizedKeyword, limit+1, offset)
	if err != nil {
		return PortfolioSummaryPage{}, fmt.Errorf("list published portfolios: %w", err)
	}
	hasNext := len(summaries) > limit
	if hasNext {
		summaries = summaries[:limit]
	}
	for i := range summaries {
		if summaries[i].CoverPhoto == nil {
			continue
		}
		if err := q.signPhoto(ctx, summaries[i].CoverPhoto); err != nil {
			return PortfolioSummaryPage{}, err
		}
	}
	return PortfolioSummaryPage{Portfolios: summaries, HasNext: hasNext}, nil
}

// GetPublishedPortfolio は公開中のポートフォリオを、テンプレートごとのレイアウトとあわせて取得する。
//
// アカウントIDの形式・ポートフォリオ番号が不正な場合、存在しない・公開されていない場合は nil を返す。
//
// 引数:
//   - ctx: キャンセル・タイムアウトを伝えるコンテキスト
//   - accountID: ポートフォリオを所有するアカウントのアカウントID
//   - portfolioNo: 取得するポートフォリオのポートフォリオ番号
//
// 戻り値:
//   - 公開中のポートフォリオ（写真には署名付きURLを設定済み）とレイアウト。取得できない場合は nil
//   - DB への問い合わせ・署名付きURLの発行・レイアウトの組み立てに失敗した場合はエラー
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

// signPhoto は写真のオブジェクトキーから署名付きURLを発行し、写真の URL に設定する。
//
// 引数:
//   - ctx: キャンセル・タイムアウトを伝えるコンテキスト
//   - photo: 署名付きURLを設定する写真（ImageFilePath を設定済みのもの）
//
// 戻り値:
//   - 署名付きURLの発行に失敗した場合はエラー（写真番号を付けて返す）
func (q *PortfolioQuery) signPhoto(ctx context.Context, photo *domain.Photo) error {
	url, err := q.signer.Sign(ctx, photo.ImageFilePath)
	if err != nil {
		return fmt.Errorf("sign photo url (photoNo=%d): %w", photo.PhotoNo, err)
	}
	photo.URL = url
	return nil
}
