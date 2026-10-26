package usecase

import (
	"context"
	"errors"
	"strings"
	"testing"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
)

type fakeRepository struct {
	accounts   map[string]int64
	summaries  []domain.PortfolioSummary
	portfolio  *domain.Portfolio
	err        error
	findCalled bool
	keyword    string
	limit      int
	offset     int
}

func (f *fakeRepository) FindAccountNo(_ context.Context, accountID string) (int64, bool, error) {
	f.findCalled = true
	if f.err != nil {
		return 0, false, f.err
	}
	no, ok := f.accounts[accountID]
	return no, ok, nil
}

func (f *fakeRepository) ListPublished(_ context.Context, _ int64, keyword string, limit, offset int) ([]domain.PortfolioSummary, error) {
	f.keyword = keyword
	f.limit = limit
	f.offset = offset
	if len(f.summaries) > limit {
		return f.summaries[:limit], nil
	}
	return f.summaries, nil
}

func (f *fakeRepository) FindPublished(_ context.Context, accountNo, portfolioNo int64) (*domain.Portfolio, error) {
	if f.portfolio == nil || f.portfolio.AccountNo != accountNo || f.portfolio.PortfolioNo != portfolioNo {
		return nil, nil
	}
	copied := *f.portfolio
	copied.Photos = append([]domain.PortfolioPhoto(nil), f.portfolio.Photos...)
	return &copied, nil
}

type fakeSigner struct{ err error }

func (f fakeSigner) Sign(_ context.Context, key string) (string, error) {
	if f.err != nil {
		return "", f.err
	}
	return "https://signed/" + key, nil
}

func TestListPublishedPortfolios(t *testing.T) {
	repo := &fakeRepository{
		accounts: map[string]int64{"owner123": 1},
		summaries: []domain.PortfolioSummary{
			{PortfolioNo: 2, CoverPhoto: &domain.Photo{PhotoNo: 5, ImageFilePath: "1/5.jpg"}},
			{PortfolioNo: 1},
		},
	}
	query := NewPortfolioQuery(repo, fakeSigner{})

	page, err := query.ListPublishedPortfolios(context.Background(), "owner123", "", 20, 0)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	got := page.Portfolios
	if len(got) != 2 || page.HasNext {
		t.Fatalf("len = %d, hasNext = %v; want 2, false", len(got), page.HasNext)
	}
	if got[0].CoverPhoto.URL != "https://signed/1/5.jpg" {
		t.Errorf("cover url = %q", got[0].CoverPhoto.URL)
	}
	if got[1].CoverPhoto != nil {
		t.Errorf("cover photo should be nil for portfolio without photos")
	}
}

func TestListPublishedPortfolios_Paging(t *testing.T) {
	repo := &fakeRepository{
		accounts:  map[string]int64{"owner123": 1},
		summaries: []domain.PortfolioSummary{{PortfolioNo: 3}, {PortfolioNo: 1}, {PortfolioNo: 2}},
	}

	// 続きの有無を判定するため、リポジトリからは1件多く取得し、limit 件に切り詰めて返す
	page, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "owner123", "", 2, 40)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if repo.limit != 3 || repo.offset != 40 {
		t.Errorf("repository limit/offset = %d/%d, want 3/40", repo.limit, repo.offset)
	}
	if len(page.Portfolios) != 2 || page.Portfolios[1].PortfolioNo != 1 || !page.HasNext {
		t.Errorf("page = %+v, want first 2 portfolios with hasNext", page)
	}

	// limit・offset が範囲外の場合は DB へ問い合わせず空の一覧を返す
	for _, c := range [][2]int{{0, 0}, {domain.PortfolioPageMaxLimit + 1, 0}, {20, -1}, {20, domain.PortfolioPageMaxOffset + 1}} {
		repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}, summaries: []domain.PortfolioSummary{{PortfolioNo: 1}}}
		page, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "owner123", "", c[0], c[1])
		if err != nil || page.Portfolios == nil || len(page.Portfolios) != 0 || page.HasNext || repo.findCalled {
			t.Errorf("limit/offset %v: page = %+v, err = %v, findCalled = %v; want empty without query", c, page, err, repo.findCalled)
		}
	}
}

func TestListPublishedPortfolios_EmptyWhenAccountMissingOrInvalid(t *testing.T) {
	for _, accountID := range []string{"missing1", "bad"} {
		repo := &fakeRepository{accounts: map[string]int64{}}
		page, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), accountID, "", 20, 0)
		if err != nil {
			t.Fatalf("%s: unexpected error: %v", accountID, err)
		}
		if got := page.Portfolios; got == nil || len(got) != 0 {
			t.Errorf("%s: got %v, want empty slice", accountID, got)
		}
	}
	// 形式が不正なアカウントIDは DB へ問い合わせない
	repo := &fakeRepository{}
	_, _ = NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "a", "", 20, 0)
	if repo.findCalled {
		t.Error("repository should not be called for invalid account id")
	}
}

func TestListPublishedPortfolios_Keyword(t *testing.T) {
	// 前後の空白を除いてリポジトリへ渡す。空白だけの場合は絞り込まない
	for input, want := range map[string]string{"  自然 ": "自然", "   ": "", "": ""} {
		repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}}
		if _, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "owner123", input, 20, 0); err != nil {
			t.Fatalf("%q: unexpected error: %v", input, err)
		}
		if repo.keyword != want {
			t.Errorf("%q: keyword = %q, want %q", input, repo.keyword, want)
		}
	}

	// 最大文字数を超える検索語は DB へ問い合わせず空の一覧を返す
	repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}, summaries: []domain.PortfolioSummary{{PortfolioNo: 1}}}
	page, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(
		context.Background(), "owner123", strings.Repeat("あ", domain.KeywordMaxLength+1), 20, 0)
	if err != nil || len(page.Portfolios) != 0 || repo.findCalled {
		t.Errorf("got %v, %v (findCalled=%v); want empty slice without query", page.Portfolios, err, repo.findCalled)
	}
}

func TestGetPublishedPortfolio(t *testing.T) {
	repo := &fakeRepository{
		accounts: map[string]int64{"owner123": 1},
		portfolio: &domain.Portfolio{
			AccountNo:   1,
			PortfolioNo: 3,
			Template:    domain.TemplateFeature,
			Photos: []domain.PortfolioPhoto{
				{Photo: domain.Photo{PhotoNo: 5, ImageFilePath: "1/5.jpg"}, Settings: map[string]any{"theme": "京都"}},
			},
		},
	}
	query := NewPortfolioQuery(repo, fakeSigner{})

	got, err := query.GetPublishedPortfolio(context.Background(), "owner123", 3)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if got.Portfolio.Photos[0].Photo.URL != "https://signed/1/5.jpg" {
		t.Errorf("photo url = %q", got.Portfolio.Photos[0].Photo.URL)
	}
	feature, ok := got.Layout.(domain.FeatureLayout)
	if !ok || feature.Sections[0].Theme != "京都" || feature.Sections[0].Photo.URL != "https://signed/1/5.jpg" {
		t.Errorf("layout = %#v", got.Layout)
	}
}

func TestGetPublishedPortfolio_NilWhenNotFound(t *testing.T) {
	repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}}
	query := NewPortfolioQuery(repo, fakeSigner{})
	cases := []struct {
		accountID   string
		portfolioNo int64
	}{
		{"owner123", 9}, // 公開されていない・存在しない
		{"missing1", 1}, // アカウントが存在しない
		{"bad", 1},      // アカウントIDの形式が不正
		{"owner123", 0}, // ポートフォリオ番号が不正
	}
	for _, c := range cases {
		got, err := query.GetPublishedPortfolio(context.Background(), c.accountID, c.portfolioNo)
		if err != nil || got != nil {
			t.Errorf("(%s, %d) = %v, %v; want nil, nil", c.accountID, c.portfolioNo, got, err)
		}
	}
}

func TestGetPublishedPortfolio_Errors(t *testing.T) {
	repoErr := errors.New("db down")
	_, err := NewPortfolioQuery(&fakeRepository{err: repoErr}, fakeSigner{}).
		GetPublishedPortfolio(context.Background(), "owner123", 1)
	if !errors.Is(err, repoErr) {
		t.Errorf("err = %v, want wrapping %v", err, repoErr)
	}

	signErr := errors.New("sign failed")
	repo := &fakeRepository{
		accounts: map[string]int64{"owner123": 1},
		portfolio: &domain.Portfolio{AccountNo: 1, PortfolioNo: 1, Template: domain.TemplateFeature,
			Photos: []domain.PortfolioPhoto{{Photo: domain.Photo{PhotoNo: 1}}}},
	}
	_, err = NewPortfolioQuery(repo, fakeSigner{err: signErr}).GetPublishedPortfolio(context.Background(), "owner123", 1)
	if !errors.Is(err, signErr) {
		t.Errorf("err = %v, want wrapping %v", err, signErr)
	}
}
