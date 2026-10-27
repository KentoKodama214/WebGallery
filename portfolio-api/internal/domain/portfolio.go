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
	// PhotoNo は写真番号（photo_mst.photo_no）。
	PhotoNo int64
	// ImageFilePath は S3 のオブジェクトキー。公開ページへは URL（署名付き）だけを渡す。
	ImageFilePath string
	// URL は ImageFilePath から発行した署名付きURL（ユースケース層で設定する）。
	URL string
	// JapaneseTitle は写真の日本語タイトル（photo_mst.photo_japanese_title）。
	JapaneseTitle string
	// EnglishTitle は写真の英語タイトル（photo_mst.photo_english_title）。
	EnglishTitle string
	// Direction は写真の向き（photo_mst.direction_kbn）。
	Direction Direction
}

// PortfolioPhoto はポートフォリオに設定した写真と、写真単位のテンプレート設定。
type PortfolioPhoto struct {
	// Photo はポートフォリオに設定した写真。
	Photo Photo
	// DisplayOrder はポートフォリオ内の表示順（portfolio_photo.display_order。小さいほど先に表示する）。
	DisplayOrder int
	// Settings は写真単位のテンプレート設定（portfolio_photo.photo_settings の JSON）。
	Settings map[string]any
}

// Tag はポートフォリオのタグ。
type Tag struct {
	// JapaneseName はタグの日本語名（portfolio_tag_mst.tag_japanese_name）。
	JapaneseName string
	// EnglishName はタグの英語名（portfolio_tag_mst.tag_english_name）。
	EnglishName string
}

// Portfolio は公開中のポートフォリオ。
type Portfolio struct {
	// AccountNo はポートフォリオを所有するアカウントのアカウント番号。
	AccountNo int64
	// PortfolioNo はポートフォリオ番号（アカウント番号との組で一意）。
	PortfolioNo int64
	// Theme はポートフォリオのテーマ（一覧・詳細の見出し）。
	Theme string
	// Description はポートフォリオの説明文。
	Description string
	// Template は表示に使うテンプレート。Layout の具象型はこの値で決まる。
	Template TemplateKbn
	// TemplateSettings はポートフォリオ単位のテンプレート設定（portfolio_mst.template_settings の JSON）。
	TemplateSettings map[string]any
	// UpdatedAt はポートフォリオの最終更新日時。
	UpdatedAt time.Time
	// Photos は表示順に並べた写真（削除済みの写真は含まない）。
	Photos []PortfolioPhoto
	// Tags はタグ番号順に並べたタグ（タグがない場合は空のスライス）。
	Tags []Tag
}

// PortfolioSummary は公開中のポートフォリオ一覧の1件分。
type PortfolioSummary struct {
	// PortfolioNo はポートフォリオ番号（アカウント番号との組で一意）。
	PortfolioNo int64
	// Theme はポートフォリオのテーマ。
	Theme string
	// Description はポートフォリオの説明文。
	Description string
	// Template は表示に使うテンプレート。
	Template TemplateKbn
	// UpdatedAt はポートフォリオの最終更新日時。
	UpdatedAt time.Time
	// Tags はタグ番号順に並べたタグ（タグがない場合は空のスライス）。
	Tags []Tag
	// PhotoCount はポートフォリオに設定した写真の枚数（削除済みの写真は数えない）。
	PhotoCount int
	// CoverPhoto は表示順の先頭の写真。写真が1枚もない場合は nil。
	CoverPhoto *Photo
}
