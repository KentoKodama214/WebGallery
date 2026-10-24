//go:build integration

package postgres

import (
	"context"
	"os"
	"reflect"
	"strings"
	"testing"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
)

// 結合テストはテスト用DB（docker-compose の postgres-test-db。CI では services の postgres）に対して実行する。
// リポジトリは本番と同じ読み取り専用ロール（portfolio_reader）で接続し、フィクスチャの投入・削除だけを
// 管理者ユーザーで行う（ロールに付与した権限で読み取れることも併せて検証するため）。
const (
	defaultReaderURL = "postgres://portfolio_reader:portfolio_reader@localhost:5433/web_gallery_test?sslmode=disable"
	defaultAdminURL  = "postgres://postgres:postgres@localhost:5433/web_gallery_test?sslmode=disable"
)

func envOr(name, fallback string) string {
	if value := os.Getenv(name); value != "" {
		return value
	}
	return fallback
}

func execFile(t *testing.T, pool *pgxpool.Pool, path string) {
	t.Helper()
	sql, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	if _, err := pool.Exec(context.Background(), string(sql)); err != nil {
		t.Fatalf("exec %s: %v", path, err)
	}
}

// setup はフィクスチャを投入し、読み取り専用ロールで接続したリポジトリを返す。
func setup(t *testing.T) (*PortfolioRepository, *pgxpool.Pool) {
	t.Helper()
	ctx := context.Background()
	admin, err := pgxpool.New(ctx, envOr("PORTFOLIO_API_TEST_ADMIN_DB_URL", defaultAdminURL))
	if err != nil {
		t.Fatal(err)
	}
	reader, err := pgxpool.New(ctx, envOr("PORTFOLIO_API_TEST_DB_URL", defaultReaderURL))
	if err != nil {
		t.Fatal(err)
	}
	execFile(t, admin, "testdata/cleanup.sql")
	execFile(t, admin, "testdata/fixture.sql")
	t.Cleanup(func() {
		execFile(t, admin, "testdata/cleanup.sql")
		reader.Close()
		admin.Close()
	})
	return NewPortfolioRepository(reader), reader
}

func TestFindAccountNo(t *testing.T) {
	repo, _ := setup(t)
	ctx := context.Background()

	no, found, err := repo.FindAccountNo(ctx, "goitowner1")
	if err != nil || !found || no != 900001 {
		t.Errorf("FindAccountNo(goitowner1) = %d, %v, %v", no, found, err)
	}
	_, found, err = repo.FindAccountNo(ctx, "goitmissing")
	if err != nil || found {
		t.Errorf("FindAccountNo(goitmissing) found=%v err=%v, want not found", found, err)
	}
}

func TestListPublished(t *testing.T) {
	repo, _ := setup(t)

	got, err := repo.ListPublished(context.Background(), 900001)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	// 公開中のものだけを、更新日時の新しい順に返す
	if len(got) != 2 || got[0].PortfolioNo != 2 || got[1].PortfolioNo != 1 {
		t.Fatalf("portfolios = %+v", got)
	}
	feature, grid := got[0], got[1]

	if feature.Template != domain.TemplateFeature || feature.PhotoCount != 0 || feature.CoverPhoto != nil {
		t.Errorf("feature summary = %+v", feature)
	}
	if len(feature.Tags) != 0 || feature.Tags == nil {
		t.Errorf("feature tags = %#v, want empty slice", feature.Tags)
	}

	// 論理削除済みの写真は枚数に含めない。先頭の写真は表示順の先頭
	if grid.Template != domain.TemplateGrid || grid.Theme != "光と影" || grid.Description != "説明1" || grid.PhotoCount != 2 {
		t.Errorf("grid summary = %+v", grid)
	}
	wantCover := &domain.Photo{PhotoNo: 2, ImageFilePath: "900001/2-bbb.jpg", JapaneseTitle: "茶畑", Direction: domain.DirectionSquare}
	if !reflect.DeepEqual(grid.CoverPhoto, wantCover) {
		t.Errorf("cover = %+v, want %+v", grid.CoverPhoto, wantCover)
	}
	// タグはタグ番号順
	wantTags := []domain.Tag{{JapaneseName: "自然", EnglishName: "nature"}, {JapaneseName: "風景"}}
	if !reflect.DeepEqual(grid.Tags, wantTags) {
		t.Errorf("tags = %+v, want %+v", grid.Tags, wantTags)
	}
	if !grid.UpdatedAt.Equal(time.Date(2026, 2, 1, 0, 0, 0, 0, time.UTC)) {
		t.Errorf("updatedAt = %v", grid.UpdatedAt)
	}
}

func TestListPublished_NoPortfolios(t *testing.T) {
	repo, _ := setup(t)

	got, err := repo.ListPublished(context.Background(), 999999)
	if err != nil || got == nil || len(got) != 0 {
		t.Errorf("ListPublished = %#v, %v; want empty slice", got, err)
	}
}

func TestFindPublished(t *testing.T) {
	repo, _ := setup(t)

	got, err := repo.FindPublished(context.Background(), 900001, 1)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if got == nil {
		t.Fatal("portfolio should be found")
	}
	if got.Theme != "光と影" || got.Template != domain.TemplateGrid {
		t.Errorf("portfolio = %+v", got)
	}
	wantSettings := map[string]any{"rows": []any{map[string]any{"pattern": "TWO_EQUAL"}}}
	if !reflect.DeepEqual(got.TemplateSettings, wantSettings) {
		t.Errorf("template settings = %#v", got.TemplateSettings)
	}
	// 表示順に並べ、論理削除済みの写真3は含めない
	wantPhotos := []domain.PortfolioPhoto{
		{DisplayOrder: 1, Settings: map[string]any{},
			Photo: domain.Photo{PhotoNo: 2, ImageFilePath: "900001/2-bbb.jpg", JapaneseTitle: "茶畑", Direction: domain.DirectionSquare}},
		{DisplayOrder: 2, Settings: map[string]any{},
			Photo: domain.Photo{PhotoNo: 1, ImageFilePath: "900001/1-aaa.jpg", JapaneseTitle: "金閣寺", EnglishTitle: "Kinkakuji", Direction: domain.DirectionHorizontal}},
	}
	if !reflect.DeepEqual(got.Photos, wantPhotos) {
		t.Errorf("photos = %+v\nwant %+v", got.Photos, wantPhotos)
	}
	if len(got.Tags) != 2 || got.Tags[0].JapaneseName != "自然" {
		t.Errorf("tags = %+v", got.Tags)
	}
}

func TestFindPublished_NotPublished(t *testing.T) {
	repo, _ := setup(t)
	for _, c := range []struct {
		name                   string
		accountNo, portfolioNo int64
	}{
		{"下書き", 900001, 3},
		{"存在しない", 900001, 99},
		{"他アカウントのポートフォリオ番号（アカウント番号との AND 条件）", 900002, 2},
	} {
		got, err := repo.FindPublished(context.Background(), c.accountNo, c.portfolioNo)
		if err != nil || got != nil {
			t.Errorf("%s: got %+v, %v; want nil", c.name, got, err)
		}
	}
}

func TestReaderRoleCannotReadPrivateColumns(t *testing.T) {
	_, reader := setup(t)
	ctx := context.Background()

	for _, query := range []string{
		`SELECT password FROM common.account WHERE account_no = 900001`,
		`SELECT address FROM common.location_mst LIMIT 1`,
		`SELECT is_location_public FROM photo.photo_mst LIMIT 1`,
	} {
		_, err := reader.Exec(ctx, query)
		if err == nil || !strings.Contains(err.Error(), "permission denied") {
			t.Errorf("%q: err = %v, want permission denied", query, err)
		}
	}
	if _, err := reader.Exec(ctx, `UPDATE photo.portfolio_mst SET theme = 'x' WHERE account_no = 900001`); err == nil {
		t.Error("reader role must not be able to update")
	}
}
