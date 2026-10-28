package graph

import (
	"fmt"

	"github.com/KentoKodama214/WebGallery/portfolio-api/graph/model"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/usecase"
)

// ドメインモデルを GraphQL のモデルへ変換する。
// 未知のテンプレート・向き・行パターンは、表示側で誤った型として扱われないようエラーにする。

// toPortfolioSummary はポートフォリオ一覧の1件分を GraphQL のモデルへ変換する。
//
// 引数:
//   - s: 公開中のポートフォリオ一覧の1件分（先頭の写真には署名付きURLを設定済み）
//
// 戻り値:
//   - GraphQL のポートフォリオ一覧の1件分（写真がない場合は CoverPhoto が nil）
//   - テンプレート・先頭の写真の向きが未知の場合はエラー
func toPortfolioSummary(s domain.PortfolioSummary) (*model.PortfolioSummary, error) {
	template, err := toTemplate(s.Template)
	if err != nil {
		return nil, err
	}
	result := &model.PortfolioSummary{
		PortfolioNo: int(s.PortfolioNo),
		Theme:       s.Theme,
		Description: s.Description,
		Template:    template,
		Tags:        toTags(s.Tags),
		PhotoCount:  s.PhotoCount,
		UpdatedAt:   s.UpdatedAt,
	}
	if s.CoverPhoto != nil {
		cover, err := toPhoto(*s.CoverPhoto)
		if err != nil {
			return nil, err
		}
		result.CoverPhoto = cover
	}
	return result, nil
}

// toPortfolio は公開中のポートフォリオを GraphQL のモデルへ変換する。
//
// 引数:
//   - p: 公開中のポートフォリオとレイアウト
//
// 戻り値:
//   - GraphQL のポートフォリオ
//   - テンプレート・レイアウトが変換できない場合はエラー
func toPortfolio(p usecase.PublishedPortfolio) (*model.Portfolio, error) {
	template, err := toTemplate(p.Portfolio.Template)
	if err != nil {
		return nil, err
	}
	layout, err := toLayout(p.Layout)
	if err != nil {
		return nil, err
	}
	return &model.Portfolio{
		PortfolioNo: int(p.Portfolio.PortfolioNo),
		Theme:       p.Portfolio.Theme,
		Description: p.Portfolio.Description,
		Template:    template,
		Tags:        toTags(p.Portfolio.Tags),
		UpdatedAt:   p.Portfolio.UpdatedAt,
		Layout:      layout,
	}, nil
}

// toLayout はテンプレートごとのレイアウトを GraphQL のユニオン型（PortfolioLayout）へ変換する。
//
// 引数:
//   - layout: domain.FeatureLayout または domain.GridLayout
//
// 戻り値:
//   - model.FeatureLayout または model.GridLayout
//   - 未知のレイアウトの型・行パターン・写真の向きの場合はエラー
func toLayout(layout domain.Layout) (model.PortfolioLayout, error) {
	switch l := layout.(type) {
	case domain.FeatureLayout:
		sections, err := mapSlice(l.Sections, func(section domain.FeatureSection) (*model.FeatureSection, error) {
			photo, err := toPhoto(section.Photo)
			if err != nil {
				return nil, err
			}
			return &model.FeatureSection{
				Photo:       photo,
				Theme:       section.Theme,
				SubTheme:    section.SubTheme,
				Description: section.Description,
			}, nil
		})
		if err != nil {
			return nil, err
		}
		return model.FeatureLayout{Sections: sections}, nil
	case domain.GridLayout:
		rows, err := mapSlice(l.Rows, toGridRow)
		if err != nil {
			return nil, err
		}
		return model.GridLayout{Rows: rows}, nil
	default:
		return nil, fmt.Errorf("unknown layout type: %T", layout)
	}
}

// toGridRow はグリッド型の行を GraphQL のモデルへ変換する。
//
// 引数:
//   - row: グリッド型の行
//
// 戻り値:
//   - GraphQL のグリッド型の行
//   - 行パターン・写真の向きが未知の場合はエラー
func toGridRow(row domain.GridRow) (*model.GridRow, error) {
	pattern := model.GridRowPattern(row.Pattern)
	if !pattern.IsValid() {
		return nil, fmt.Errorf("unknown grid row pattern: %q", row.Pattern)
	}
	photos, err := mapSlice(row.Photos, func(gridPhoto domain.GridPhoto) (*model.GridPhoto, error) {
		photo, err := toPhoto(gridPhoto.Photo)
		if err != nil {
			return nil, err
		}
		return &model.GridPhoto{Photo: photo}, nil
	})
	if err != nil {
		return nil, err
	}
	return &model.GridRow{Pattern: pattern, Photos: photos}, nil
}

// photoDirections は写真の向き（DB の値）と GraphQL の列挙型の対応。
var photoDirections = map[domain.Direction]model.PhotoDirection{
	domain.DirectionVertical:   model.PhotoDirectionVertical,
	domain.DirectionHorizontal: model.PhotoDirectionHorizontal,
	domain.DirectionSquare:     model.PhotoDirectionSquare,
	domain.DirectionNone:       model.PhotoDirectionNone,
}

// toPhoto は写真を GraphQL のモデルへ変換する（オブジェクトキーは渡さず、署名付きURLだけを渡す）。
//
// 引数:
//   - p: 署名付きURLを設定済みの写真
//
// 戻り値:
//   - GraphQL の写真
//   - 写真の向きが未知の場合はエラー
func toPhoto(p domain.Photo) (*model.Photo, error) {
	direction, ok := photoDirections[p.Direction]
	if !ok {
		return nil, fmt.Errorf("unknown photo direction: %q", p.Direction)
	}
	return &model.Photo{
		PhotoNo:       int(p.PhotoNo),
		URL:           p.URL,
		JapaneseTitle: p.JapaneseTitle,
		EnglishTitle:  p.EnglishTitle,
		Direction:     direction,
	}, nil
}

// toTemplate はテンプレート区分を GraphQL の列挙型へ変換する。
//
// 引数:
//   - template: テンプレート区分（DB の値）
//
// 戻り値:
//   - GraphQL のテンプレート（FEATURE・GRID）
//   - テンプレートが未知の場合はエラー
func toTemplate(template domain.TemplateKbn) (model.PortfolioTemplate, error) {
	switch template {
	case domain.TemplateFeature:
		return model.PortfolioTemplateFeature, nil
	case domain.TemplateGrid:
		return model.PortfolioTemplateGrid, nil
	default:
		return "", fmt.Errorf("unknown portfolio template: %q", template)
	}
}

// toTags はタグの一覧を GraphQL のモデルへ変換する。
//
// 引数:
//   - tags: ポートフォリオのタグ
//
// 戻り値:
//   - 同じ順に並べた GraphQL のタグ（tags が空・nil の場合は空のスライス）
func toTags(tags []domain.Tag) []*model.Tag {
	result := make([]*model.Tag, 0, len(tags))
	for _, tag := range tags {
		result = append(result, &model.Tag{JapaneseName: tag.JapaneseName, EnglishName: tag.EnglishName})
	}
	return result
}

// mapSlice はスライスの要素を順に変換する。1件でも変換に失敗した場合は、そこで打ち切ってエラーを返す。
//
// 引数:
//   - in: 変換元のスライス
//   - convert: 要素1件分の変換
//
// 戻り値:
//   - 同じ順に並べた変換後のスライス（in が空・nil の場合は空のスライス。GraphQL で null ではなく [] を返すため）
//   - 変換に失敗した場合は最初のエラー
func mapSlice[T, U any](in []T, convert func(T) (U, error)) ([]U, error) {
	out := make([]U, 0, len(in))
	for _, v := range in {
		converted, err := convert(v)
		if err != nil {
			return nil, err
		}
		out = append(out, converted)
	}
	return out, nil
}
