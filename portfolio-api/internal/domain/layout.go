package domain

import "fmt"

// Layout はテンプレートごとのレイアウト。具象型は FeatureLayout・GridLayout のいずれか。
//
// テンプレートを追加する場合は、レイアウトの型を追加して BuildLayout の分岐に加える。
type Layout interface {
	isLayout()
}

// FeatureLayout はフィーチャー型のレイアウト。
type FeatureLayout struct {
	Sections []FeatureSection
}

// FeatureSection はフィーチャー型のセクション（写真1枚分）。
type FeatureSection struct {
	Photo       Photo
	Theme       string
	SubTheme    string
	Description string
}

// GridLayout はグリッド型のレイアウト。
type GridLayout struct {
	Rows []GridRow
}

// GridRowPattern はグリッド型の行パターン（テンプレート設定の rows[].pattern の値）。
type GridRowPattern string

const (
	GridRowTwoEqual   GridRowPattern = "TWO_EQUAL"
	GridRowThreeEqual GridRowPattern = "THREE_EQUAL"
	// GridRowWideNarrow は横長1枚（左）と縦長1枚（右）。
	GridRowWideNarrow GridRowPattern = "WIDE_NARROW"
	// GridRowNarrowWide は縦長1枚（左）と横長1枚（右）。
	GridRowNarrowWide GridRowPattern = "NARROW_WIDE"
)

// photoCount は行パターンごとの1行あたりの写真の枚数。未知の行パターンは 0 を返す。
func (p GridRowPattern) photoCount() int {
	switch p {
	case GridRowTwoEqual, GridRowWideNarrow, GridRowNarrowWide:
		return 2
	case GridRowThreeEqual:
		return 3
	default:
		return 0
	}
}

// GridRow はグリッド型の行。
type GridRow struct {
	Pattern GridRowPattern
	Photos  []GridPhoto
}

// GridPhoto はグリッド型の写真（グリッド型は写真単位の設定を持たない）。
type GridPhoto struct {
	Photo Photo
}

func (FeatureLayout) isLayout() {}
func (GridLayout) isLayout()    {}

// 写真単位・ポートフォリオ単位のテンプレート設定の項目名（Java 側の各テンプレートの Policy と一致させる）
const (
	settingTheme       = "theme"
	settingSubTheme    = "subTheme"
	settingDescription = "description"
	settingRows        = "rows"
	settingPattern     = "pattern"
)

// BuildLayout はポートフォリオのテンプレートと設定から、表示用のレイアウトを組み立てる。
//
// 公開中のポートフォリオは Java 側で公開できる設定であることを検証済みだが、表示側でも
// 設定の不整合で処理が止まらないよう防御的に組み立てる（グリッド型で写真が足りない行は
// その行で打ち切り、行に割り当てられない写真は表示しない）。
func BuildLayout(p Portfolio) (Layout, error) {
	switch p.Template {
	case TemplateFeature:
		return buildFeatureLayout(p), nil
	case TemplateGrid:
		return buildGridLayout(p), nil
	default:
		return nil, fmt.Errorf("unknown portfolio template: %q", p.Template)
	}
}

func buildFeatureLayout(p Portfolio) FeatureLayout {
	sections := make([]FeatureSection, 0, len(p.Photos))
	for _, photo := range p.Photos {
		sections = append(sections, FeatureSection{
			Photo:       photo.Photo,
			Theme:       stringSetting(photo.Settings, settingTheme),
			SubTheme:    stringSetting(photo.Settings, settingSubTheme),
			Description: stringSetting(photo.Settings, settingDescription),
		})
	}
	return FeatureLayout{Sections: sections}
}

func buildGridLayout(p Portfolio) GridLayout {
	rows := make([]GridRow, 0)
	cursor := 0
	for _, pattern := range gridRowPatterns(p.TemplateSettings) {
		count := pattern.photoCount()
		if count == 0 || cursor+count > len(p.Photos) {
			// 未知の行パターン、または写真が足りない行以降は表示しない
			break
		}
		photos := make([]GridPhoto, 0, count)
		for _, photo := range p.Photos[cursor : cursor+count] {
			photos = append(photos, GridPhoto{Photo: photo.Photo})
		}
		rows = append(rows, GridRow{Pattern: pattern, Photos: photos})
		cursor += count
	}
	return GridLayout{Rows: rows}
}

// gridRowPatterns はポートフォリオ単位の設定から行パターンの一覧を取り出す。
func gridRowPatterns(settings map[string]any) []GridRowPattern {
	rows, ok := settings[settingRows].([]any)
	if !ok {
		return nil
	}
	patterns := make([]GridRowPattern, 0, len(rows))
	for _, row := range rows {
		rowMap, ok := row.(map[string]any)
		if !ok {
			patterns = append(patterns, "")
			continue
		}
		pattern, _ := rowMap[settingPattern].(string)
		patterns = append(patterns, GridRowPattern(pattern))
	}
	return patterns
}

// stringSetting は設定から文字列の項目を取り出す。未設定・文字列以外の場合は空文字を返す。
func stringSetting(settings map[string]any, key string) string {
	value, _ := settings[key].(string)
	return value
}
