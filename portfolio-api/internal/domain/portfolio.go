// Package domain は、公開ページに表示するポートフォリオのドメインモデルと、
// テンプレートごとのレイアウトを組み立てるルールを定義する。外部ライブラリに依存しない。
package domain

import "time"

// TemplateKbn はポートフォリオのテンプレート区分（DB の photo.portfolio_template_enum の値）。
type TemplateKbn string

const (
	// TemplateFeature はフィーチャー型（写真ごとにテーマ・サブテーマ・説明を持つセクションを縦に並べる）。
	TemplateFeature TemplateKbn = "feature"
	// TemplateGrid はグリッド型（1行に2〜3枚の写真を組み合わせて並べる）。
	TemplateGrid TemplateKbn = "grid"
)

// Direction は写真の向き（DB の photo.direction_enum の値）。
type Direction string

const (
	DirectionVertical   Direction = "vertical"
	DirectionHorizontal Direction = "horizontal"
	DirectionSquare     Direction = "square"
	DirectionNone       Direction = "none"
)

// Photo は公開ページに表示する写真。
type Photo struct {
	PhotoNo int64
	// ImageFilePath は S3 のオブジェクトキー。公開ページへは URL（署名付き）だけを渡す。
	ImageFilePath string
	// URL は ImageFilePath から発行した署名付きURL（ユースケース層で設定する）。
	URL           string
	JapaneseTitle string
	EnglishTitle  string
	Direction     Direction
}

// PortfolioPhoto はポートフォリオに設定した写真と、写真単位のテンプレート設定。
type PortfolioPhoto struct {
	Photo        Photo
	DisplayOrder int
	// Settings は写真単位のテンプレート設定（portfolio_photo.photo_settings の JSON）。
	Settings map[string]any
}

// Tag はポートフォリオのタグ。
type Tag struct {
	JapaneseName string
	EnglishName  string
}

// Portfolio は公開中のポートフォリオ。
type Portfolio struct {
	AccountNo   int64
	PortfolioNo int64
	Theme       string
	Description string
	Template    TemplateKbn
	// TemplateSettings はポートフォリオ単位のテンプレート設定（portfolio_mst.template_settings の JSON）。
	TemplateSettings map[string]any
	UpdatedAt        time.Time
	// Photos は表示順に並べた写真（削除済みの写真は含まない）。
	Photos []PortfolioPhoto
	Tags   []Tag
}

// PortfolioSummary は公開中のポートフォリオ一覧の1件分。
type PortfolioSummary struct {
	PortfolioNo int64
	Theme       string
	Description string
	Template    TemplateKbn
	UpdatedAt   time.Time
	Tags        []Tag
	PhotoCount  int
	// CoverPhoto は表示順の先頭の写真。写真が1枚もない場合は nil。
	CoverPhoto *Photo
}
