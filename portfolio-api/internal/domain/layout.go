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
	// Sections は写真の表示順に並べたセクション（写真1枚につき1セクション）。
	Sections []FeatureSection
}

// FeatureSection はフィーチャー型のセクション（写真1枚分）。
type FeatureSection struct {
	// Photo はセクションに表示する写真。
	Photo Photo
	// Theme はセクションのテーマ（写真単位の設定 theme。未設定の場合は空文字）。
	Theme string
	// SubTheme はセクションのサブテーマ（写真単位の設定 subTheme。未設定の場合は空文字）。
	SubTheme string
	// Description はセクションの説明文（写真単位の設定 description。未設定の場合は空文字）。
	Description string
}

// GridLayout はグリッド型のレイアウト。
type GridLayout struct {
	// Rows は上から順に並べた行（ポートフォリオ単位の設定 rows の順）。
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

// photoCount は行パターンごとの1行あたりの写真の枚数を返す。
//
// 戻り値:
//   - 行パターンの1行あたりの写真の枚数（2 または 3）。未知の行パターンは 0
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
	// Pattern は行パターン（1行に並べる写真の枚数と配置）。
	Pattern GridRowPattern
	// Photos は行に左から順に並べる写真（枚数は行パターンの枚数と一致する）。
	Photos []GridPhoto
}

// GridPhoto はグリッド型の写真（グリッド型は写真単位の設定を持たない）。
type GridPhoto struct {
	// Photo は行に並べる写真。
	Photo Photo
}

// isLayout は FeatureLayout を Layout として扱えるようにするためのマーカー（他パッケージの型が Layout を実装できないようにする）。
func (FeatureLayout) isLayout() {}

// isLayout は GridLayout を Layout として扱えるようにするためのマーカー。
func (GridLayout) isLayout() {}

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
//
// 引数:
//   - p: 公開中のポートフォリオ（Photos は表示順に並べたもの。各写真はそのままレイアウトへ引き継ぐ）
//
// 戻り値:
//   - テンプレートに対応するレイアウト（FeatureLayout または GridLayout）
//   - テンプレートが未知の場合はエラー
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

// buildFeatureLayout はフィーチャー型のレイアウトを組み立てる。
//
// 写真1枚につき1セクションとし、写真単位の設定からテーマ・サブテーマ・説明文を取り出す。
//
// 引数:
//   - p: フィーチャー型のポートフォリオ
//
// 戻り値:
//   - 写真の表示順にセクションを並べたレイアウト（写真がない場合はセクションなし）
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

// buildGridLayout はグリッド型のレイアウトを組み立てる。
//
// ポートフォリオ単位の設定の行パターンを先頭から順に見て、写真を表示順に行パターンの枚数ずつ各行へ割り当てる。
//
// 引数:
//   - p: グリッド型のポートフォリオ
//
// 戻り値:
//   - 割り当てられた行だけを並べたレイアウト（未知の行パターン・写真が足りない行以降は含めない）
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
//
// 引数:
//   - settings: ポートフォリオ単位のテンプレート設定（rows に行の一覧を持つ）
//
// 戻り値:
//   - 行の順に並べた行パターン。rows が未設定・配列でない場合は nil。
//     オブジェクトでない行・pattern が文字列でない行は空文字（未知の行パターン）にする
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

// stringSetting は設定から文字列の項目を取り出す。
//
// 引数:
//   - settings: テンプレート設定
//   - key: 取り出す項目名
//
// 戻り値:
//   - 項目の値。未設定・文字列以外の場合は空文字
func stringSetting(settings map[string]any, key string) string {
	value, _ := settings[key].(string)
	return value
}
