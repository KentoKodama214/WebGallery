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
}

func (f *fakeRepository) FindAccountNo(_ context.Context, accountID string) (int64, bool, error) {
	f.findCalled = true
	if f.err != nil {
		return 0, false, f.err
	}
	no, ok := f.accounts[accountID]
	return no, ok, nil
}

func (f *fakeRepository) ListPublished(_ context.Context, _ int64, keyword string) ([]domain.PortfolioSummary, error) {
	f.keyword = keyword
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

	got, err := query.ListPublishedPortfolios(context.Background(), "owner123", "")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(got) != 2 {
		t.Fatalf("len = %d, want 2", len(got))
	}
	if got[0].CoverPhoto.URL != "https://signed/1/5.jpg" {
		t.Errorf("cover url = %q", got[0].CoverPhoto.URL)
	}
	if got[1].CoverPhoto != nil {
		t.Errorf("cover photo should be nil for portfolio without photos")
	}
}

func TestListPublishedPortfolios_EmptyWhenAccountMissingOrInvalid(t *testing.T) {
	for _, accountID := range []string{"missing1", "bad"} {
		repo := &fakeRepository{accounts: map[string]int64{}}
		got, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), accountID, "")
		if err != nil {
			t.Fatalf("%s: unexpected error: %v", accountID, err)
		}
		if got == nil || len(got) != 0 {
			t.Errorf("%s: got %v, want empty slice", accountID, got)
		}
	}
	// 形式が不正なアカウントIDは DB へ問い合わせない
	repo := &fakeRepository{}
	_, _ = NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "a", "")
	if repo.findCalled {
		t.Error("repository should not be called for invalid account id")
	}
}

func TestListPublishedPortfolios_Keyword(t *testing.T) {
	// 前後の空白を除いてリポジトリへ渡す。空白だけの場合は絞り込まない
	for input, want := range map[string]string{"  自然 ": "自然", "   ": "", "": ""} {
		repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}}
		if _, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(context.Background(), "owner123", input); err != nil {
			t.Fatalf("%q: unexpected error: %v", input, err)
		}
		if repo.keyword != want {
			t.Errorf("%q: keyword = %q, want %q", input, repo.keyword, want)
		}
	}

	// 最大文字数を超える検索語は DB へ問い合わせず空の一覧を返す
	repo := &fakeRepository{accounts: map[string]int64{"owner123": 1}, summaries: []domain.PortfolioSummary{{PortfolioNo: 1}}}
	got, err := NewPortfolioQuery(repo, fakeSigner{}).ListPublishedPortfolios(
		context.Background(), "owner123", strings.Repeat("あ", domain.KeywordMaxLength+1))
	if err != nil || len(got) != 0 || repo.findCalled {
		t.Errorf("got %v, %v (findCalled=%v); want empty slice without query", got, err, repo.findCalled)
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
