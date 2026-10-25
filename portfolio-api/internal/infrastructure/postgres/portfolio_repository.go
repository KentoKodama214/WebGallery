// Package postgres は PostgreSQL からポートフォリオを読み取るリポジトリの実装。
//
// テーブル定義（DDL）は db/ 配下で管理し、書き込みは Java のバックエンドだけが行う。
// このパッケージは読み取り専用のロール（portfolio_reader）で接続する前提で、SELECT のみを発行する。
package postgres

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
)

// PortfolioRepository は usecase.PortfolioRepository の PostgreSQL 実装。
type PortfolioRepository struct {
	pool *pgxpool.Pool
}

// NewPortfolioRepository は PortfolioRepository を生成する。
func NewPortfolioRepository(pool *pgxpool.Pool) *PortfolioRepository {
	return &PortfolioRepository{pool: pool}
}

// FindAccountNo はアカウントIDからアカウント番号を取得する。
func (r *PortfolioRepository) FindAccountNo(ctx context.Context, accountID string) (int64, bool, error) {
	var accountNo int64
	err := r.pool.QueryRow(ctx,
		`SELECT account_no FROM common.account WHERE account_id = $1`, accountID,
	).Scan(&accountNo)
	if errors.Is(err, pgx.ErrNoRows) {
		return 0, false, nil
	}
	if err != nil {
		return 0, false, err
	}
	return accountNo, true, nil
}

// ListPublished はアカウントの公開中のポートフォリオ一覧を、表示順に取得する。
//
// keyword が空でない場合は、テーマ・タグ名（日本語・英語）のいずれかに部分一致（大文字・小文字を区別しない）
// するものに絞り込む。写真の枚数・先頭の写真は、削除済みの写真を除いて数える。タグはポートフォリオごとに問い合わせず
// 一括で取得する（一覧の件数に比例してクエリが増える N+1 を避ける）。
func (r *PortfolioRepository) ListPublished(ctx context.Context, accountNo int64, keyword string) ([]domain.PortfolioSummary, error) {
	// 検索語がない場合は NULL を渡し、絞り込み条件全体を無効にする
	var pattern *string
	if keyword != "" {
		p := toContainsPattern(keyword)
		pattern = &p
	}
	rows, err := r.pool.Query(ctx, `
		SELECT
			pm.portfolio_no,
			pm.theme,
			pm.description,
			pm.template_kbn::text,
			pm.updated_at,
			(
				SELECT COUNT(*)
				FROM photo.portfolio_photo pp
				INNER JOIN photo.photo_mst ph
					ON ph.account_no = pp.account_no AND ph.photo_no = pp.photo_no AND ph.is_deleted = false
				WHERE pp.account_no = pm.account_no AND pp.portfolio_no = pm.portfolio_no
			) AS photo_count,
			cover.photo_no,
			cover.image_file_path,
			cover.photo_japanese_title,
			cover.photo_english_title,
			cover.direction_kbn::text
		FROM photo.portfolio_mst pm
		LEFT JOIN LATERAL (
			SELECT ph.photo_no, ph.image_file_path, ph.photo_japanese_title, ph.photo_english_title, ph.direction_kbn
			FROM photo.portfolio_photo pp
			INNER JOIN photo.photo_mst ph
				ON ph.account_no = pp.account_no AND ph.photo_no = pp.photo_no AND ph.is_deleted = false
			WHERE pp.account_no = pm.account_no AND pp.portfolio_no = pm.portfolio_no
			ORDER BY pp.display_order
			LIMIT 1
		) cover ON true
		WHERE pm.account_no = $1
			AND pm.status_kbn = 'published'
			AND (
				$2::text IS NULL
				OR pm.theme ILIKE $2 ESCAPE '\'
				OR EXISTS (
					SELECT 1
					FROM photo.portfolio_tag_mst pt
					WHERE pt.account_no = pm.account_no
						AND pt.portfolio_no = pm.portfolio_no
						AND (pt.tag_japanese_name ILIKE $2 ESCAPE '\' OR pt.tag_english_name ILIKE $2 ESCAPE '\')
				)
			)
		ORDER BY pm.display_order, pm.portfolio_no`, accountNo, pattern)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	summaries := make([]domain.PortfolioSummary, 0)
	portfolioNos := make([]int64, 0)
	for rows.Next() {
		var (
			s          domain.PortfolioSummary
			template   string
			photoCount int64
			coverNo    *int64
			coverPath  *string
			coverJa    *string
			coverEn    *string
			coverDir   *string
		)
		if err := rows.Scan(&s.PortfolioNo, &s.Theme, &s.Description, &template, &s.UpdatedAt,
			&photoCount, &coverNo, &coverPath, &coverJa, &coverEn, &coverDir); err != nil {
			return nil, err
		}
		s.Template = domain.TemplateKbn(template)
		s.PhotoCount = int(photoCount)
		if coverNo != nil {
			s.CoverPhoto = &domain.Photo{
				PhotoNo:       *coverNo,
				ImageFilePath: *coverPath,
				JapaneseTitle: *coverJa,
				EnglishTitle:  *coverEn,
				Direction:     domain.Direction(*coverDir),
			}
		}
		summaries = append(summaries, s)
		portfolioNos = append(portfolioNos, s.PortfolioNo)
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	tags, err := r.findTags(ctx, accountNo, portfolioNos)
	if err != nil {
		return nil, err
	}
	for i := range summaries {
		summaries[i].Tags = tagsOrEmpty(tags[summaries[i].PortfolioNo])
	}
	return summaries, nil
}

// FindPublished は公開中のポートフォリオを、表示順の写真（削除済みを除く）・タグとあわせて取得する。
func (r *PortfolioRepository) FindPublished(ctx context.Context, accountNo, portfolioNo int64) (*domain.Portfolio, error) {
	var (
		p        domain.Portfolio
		template string
		settings map[string]any
	)
	err := r.pool.QueryRow(ctx, `
		SELECT account_no, portfolio_no, theme, description, template_kbn::text, template_settings, updated_at
		FROM photo.portfolio_mst
		WHERE account_no = $1
			AND portfolio_no = $2
			AND status_kbn = 'published'`, accountNo, portfolioNo,
	).Scan(&p.AccountNo, &p.PortfolioNo, &p.Theme, &p.Description, &template, &settings, &p.UpdatedAt)
	if errors.Is(err, pgx.ErrNoRows) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	p.Template = domain.TemplateKbn(template)
	p.TemplateSettings = settingsOrEmpty(settings)

	photos, err := r.findPhotos(ctx, accountNo, portfolioNo)
	if err != nil {
		return nil, err
	}
	p.Photos = photos

	tags, err := r.findTags(ctx, accountNo, []int64{portfolioNo})
	if err != nil {
		return nil, err
	}
	p.Tags = tagsOrEmpty(tags[portfolioNo])
	return &p, nil
}

func (r *PortfolioRepository) findPhotos(ctx context.Context, accountNo, portfolioNo int64) ([]domain.PortfolioPhoto, error) {
	rows, err := r.pool.Query(ctx, `
		SELECT
			pp.display_order,
			pp.photo_settings,
			ph.photo_no,
			ph.image_file_path,
			ph.photo_japanese_title,
			ph.photo_english_title,
			ph.direction_kbn::text
		FROM photo.portfolio_photo pp
		INNER JOIN photo.photo_mst ph
			ON ph.account_no = pp.account_no AND ph.photo_no = pp.photo_no AND ph.is_deleted = false
		WHERE pp.account_no = $1 AND pp.portfolio_no = $2
		ORDER BY pp.display_order`, accountNo, portfolioNo)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	photos := make([]domain.PortfolioPhoto, 0)
	for rows.Next() {
		var (
			pp        domain.PortfolioPhoto
			settings  map[string]any
			direction string
		)
		if err := rows.Scan(&pp.DisplayOrder, &settings, &pp.Photo.PhotoNo, &pp.Photo.ImageFilePath,
			&pp.Photo.JapaneseTitle, &pp.Photo.EnglishTitle, &direction); err != nil {
			return nil, err
		}
		pp.Settings = settingsOrEmpty(settings)
		pp.Photo.Direction = domain.Direction(direction)
		photos = append(photos, pp)
	}
	return photos, rows.Err()
}

// findTags は複数のポートフォリオのタグを一括で取得し、ポートフォリオ番号ごとにタグ番号順でまとめる。
func (r *PortfolioRepository) findTags(ctx context.Context, accountNo int64, portfolioNos []int64) (map[int64][]domain.Tag, error) {
	result := make(map[int64][]domain.Tag)
	if len(portfolioNos) == 0 {
		return result, nil
	}
	rows, err := r.pool.Query(ctx, `
		SELECT portfolio_no, tag_japanese_name, tag_english_name
		FROM photo.portfolio_tag_mst
		WHERE account_no = $1 AND portfolio_no = ANY($2)
		ORDER BY portfolio_no, tag_no`, accountNo, portfolioNos)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	for rows.Next() {
		var (
			portfolioNo int64
			tag         domain.Tag
		)
		if err := rows.Scan(&portfolioNo, &tag.JapaneseName, &tag.EnglishName); err != nil {
			return nil, err
		}
		result[portfolioNo] = append(result[portfolioNo], tag)
	}
	return result, rows.Err()
}

// toContainsPattern は検索語を部分一致の LIKE パターンに変換する。
//
// 検索語に含まれる LIKE のワイルドカード文字（% と _）とエスケープ文字（\）は、文字そのものとして一致させるためエスケープする
// （Java 側の PortfolioMstRepositoryImpl#toContainsPattern と同じ規則）。
func toContainsPattern(keyword string) string {
	escaped := strings.NewReplacer(`\`, `\\`, `%`, `\%`, `_`, `\_`).Replace(keyword)
	return "%" + escaped + "%"
}

func tagsOrEmpty(tags []domain.Tag) []domain.Tag {
	if tags == nil {
		return []domain.Tag{}
	}
	return tags
}

func settingsOrEmpty(settings map[string]any) map[string]any {
	if settings == nil {
		return map[string]any{}
	}
	return settings
}

// Ping は DB への接続を確認する（ヘルスチェック用）。
func (r *PortfolioRepository) Ping(ctx context.Context) error {
	ctx, cancel := context.WithTimeout(ctx, 2*time.Second)
	defer cancel()
	if err := r.pool.Ping(ctx); err != nil {
		return fmt.Errorf("ping database: %w", err)
	}
	return nil
}
