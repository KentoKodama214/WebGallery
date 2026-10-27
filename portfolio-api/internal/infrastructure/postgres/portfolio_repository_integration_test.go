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

// envOr は環境変数の値を返し、未設定の場合は代わりの値を返す。
//
// 引数:
//   - name: 環境変数名
//   - fallback: 環境変数が未設定の場合に使う値
//
// 戻り値:
//   - 環境変数の値（未設定の場合は fallback）
func envOr(name, fallback string) string {
	if value := os.Getenv(name); value != "" {
		return value
	}
	return fallback
}

// execFile は SQL ファイルを読み込んで実行する（失敗した場合はテストを中断する）。
//
// 引数:
//   - t: テストの状態
//   - pool: SQL を実行する接続（フィクスチャの投入・削除は管理者ユーザーの接続を使う）
//   - path: 実行する SQL ファイルのパス
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
//
// 投入前に前回のフィクスチャを削除し、テスト終了時にも削除して接続を閉じる。
//
// 引数:
//   - t: テストの状態
//
// 戻り値:
//   - 読み取り専用ロールで接続したリポジトリ
//   - リポジトリと同じ読み取り専用ロールの接続（権限の検証に使う）
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

// TestFindAccountNo は、存在するアカウントIDの場合はアカウント番号と found=true を返し、
// 存在しないアカウントIDの場合はエラーにせず found=false を返すことを検証する。
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

// TestListPublished は、アカウントの公開中のポートフォリオだけを表示順に返し、
// テンプレート・テーマ・説明文・更新日時、写真の枚数（論理削除済みの写真を除く）・先頭の写真（表示順の先頭）、
// タグ（タグ番号順。タグがない場合は空のスライス）が正しく取得できることを検証する。
func TestListPublished(t *testing.T) {
	repo, _ := setup(t)

	got, err := repo.ListPublished(context.Background(), 900001, "", 100, 0)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	// 公開中のものだけを、表示順に返す（更新日時の順ではない）
	if len(got) != 2 || got[0].PortfolioNo != 1 || got[1].PortfolioNo != 2 {
		t.Fatalf("portfolios = %+v", got)
	}
	grid, feature := got[0], got[1]

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

// TestListPublished_NoPortfolios は、公開中のポートフォリオがないアカウントの場合に、エラーにせず空のスライス（nil ではない）を返すことを検証する。
func TestListPublished_NoPortfolios(t *testing.T) {
	repo, _ := setup(t)

	got, err := repo.ListPublished(context.Background(), 999999, "", 100, 0)
	if err != nil || got == nil || len(got) != 0 {
		t.Errorf("ListPublished = %#v, %v; want empty slice", got, err)
	}
}

// TestListPublished_LimitOffset は、表示順で offset 件目から最大 limit 件を返し、
// offset が件数以上の場合は空のスライスを返すことを検証する。
func TestListPublished_LimitOffset(t *testing.T) {
	repo, _ := setup(t)

	// 表示順（ポートフォリオ1→2）で offset 件目から最大 limit 件を返す
	for _, c := range []struct {
		limit, offset int     // 取得件数・取得開始位置
		want          []int64 // 期待するポートフォリオ番号（表示順）
	}{
		{1, 0, []int64{1}}, // 先頭の1件
		{1, 1, []int64{2}}, // 2件目から1件
		{10, 2, []int64{}}, // 件数を超える取得開始位置は空
	} {
		got, err := repo.ListPublished(context.Background(), 900001, "", c.limit, c.offset)
		if err != nil {
			t.Fatalf("limit=%d offset=%d: unexpected error: %v", c.limit, c.offset, err)
		}
		gotNos := make([]int64, 0, len(got))
		for _, s := range got {
			gotNos = append(gotNos, s.PortfolioNo)
		}
		if !reflect.DeepEqual(gotNos, c.want) {
			t.Errorf("limit=%d offset=%d: portfolioNos = %v, want %v", c.limit, c.offset, gotNos, c.want)
		}
	}
}

// TestListPublished_Keyword は、検索語がテーマ・タグ名（日本語・英語）のいずれかに部分一致（大文字・小文字を区別しない）する
// ポートフォリオだけを返し、下書き・他アカウントのポートフォリオは一致しても返さないこと、
// LIKE のワイルドカード文字を文字そのものとして扱うことを検証する。
func TestListPublished_Keyword(t *testing.T) {
	repo, _ := setup(t)

	cases := []struct {
		name    string  // テストケースの説明
		keyword string  // 絞り込みの検索語
		want    []int64 // 期待するポートフォリオ番号（表示順）
	}{
		{"テーマに部分一致", "光", []int64{1}},
		{"テーマ・タグ名のいずれかに一致（OR条件）", "自然", []int64{1, 2}},
		{"タグ英語名に大文字・小文字を区別せず一致", "NATURE", []int64{1}},
		{"下書きは対象外", "下書き", []int64{}},
		{"他アカウントは対象外", "海", []int64{}},
		{"LIKEのワイルドカードは文字そのものとして扱う", "%", []int64{}},
	}
	for _, c := range cases {
		got, err := repo.ListPublished(context.Background(), 900001, c.keyword, 100, 0)
		if err != nil {
			t.Fatalf("%s: unexpected error: %v", c.name, err)
		}
		gotNos := make([]int64, 0, len(got))
		for _, s := range got {
			gotNos = append(gotNos, s.PortfolioNo)
		}
		if !reflect.DeepEqual(gotNos, c.want) {
			t.Errorf("%s: portfolioNos = %v, want %v", c.name, gotNos, c.want)
		}
	}
}

// TestFindPublished は、公開中のポートフォリオを取得でき、テーマ・テンプレート・ポートフォリオ単位の設定、
// 表示順に並べた写真（論理削除済みの写真を除き、写真単位の設定は空のマップ）、タグが正しく取得できることを検証する。
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

// TestFindPublished_NotPublished は、下書きの場合・存在しない場合・他アカウントのポートフォリオ番号を指定した場合に、
// エラーにせず nil を返すことを検証する。
func TestFindPublished_NotPublished(t *testing.T) {
	repo, _ := setup(t)
	for _, c := range []struct {
		name                   string // テストケースの説明
		accountNo, portfolioNo int64  // 取得するアカウント番号・ポートフォリオ番号
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

// TestReaderRoleCannotReadPrivateColumns は、読み取り専用ロールが非公開の列（パスワード・住所・位置情報の公開設定）を読み取れず
// permission denied になること、テーブルを更新できないことを検証する。
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
