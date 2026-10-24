package graph

import (
	"fmt"

	"github.com/KentoKodama214/WebGallery/portfolio-api/graph/model"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/usecase"
)

// ドメインモデルを GraphQL のモデルへ変換する。
// 未知のテンプレート・向き・行パターンは、表示側で誤った型として扱われないようエラーにする。

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

func toLayout(layout domain.Layout) (model.PortfolioLayout, error) {
	switch l := layout.(type) {
	case domain.FeatureLayout:
		sections := make([]*model.FeatureSection, 0, len(l.Sections))
		for _, section := range l.Sections {
			photo, err := toPhoto(section.Photo)
			if err != nil {
				return nil, err
			}
			sections = append(sections, &model.FeatureSection{
				Photo:       photo,
				Theme:       section.Theme,
				SubTheme:    section.SubTheme,
				Description: section.Description,
			})
		}
		return model.FeatureLayout{Sections: sections}, nil
	case domain.GridLayout:
		rows := make([]*model.GridRow, 0, len(l.Rows))
		for _, row := range l.Rows {
			pattern := model.GridRowPattern(row.Pattern)
			if !pattern.IsValid() {
				return nil, fmt.Errorf("unknown grid row pattern: %q", row.Pattern)
			}
			photos := make([]*model.GridPhoto, 0, len(row.Photos))
			for _, gridPhoto := range row.Photos {
				photo, err := toPhoto(gridPhoto.Photo)
				if err != nil {
					return nil, err
				}
				photos = append(photos, &model.GridPhoto{Photo: photo})
			}
			rows = append(rows, &model.GridRow{Pattern: pattern, Photos: photos})
		}
		return model.GridLayout{Rows: rows}, nil
	default:
		return nil, fmt.Errorf("unknown layout type: %T", layout)
	}
}

func toPhoto(p domain.Photo) (*model.Photo, error) {
	direction, ok := map[domain.Direction]model.PhotoDirection{
		domain.DirectionVertical:   model.PhotoDirectionVertical,
		domain.DirectionHorizontal: model.PhotoDirectionHorizontal,
		domain.DirectionSquare:     model.PhotoDirectionSquare,
		domain.DirectionNone:       model.PhotoDirectionNone,
	}[p.Direction]
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

func toTags(tags []domain.Tag) []*model.Tag {
	result := make([]*model.Tag, 0, len(tags))
	for _, tag := range tags {
		result = append(result, &model.Tag{JapaneseName: tag.JapaneseName, EnglishName: tag.EnglishName})
	}
	return result
}
