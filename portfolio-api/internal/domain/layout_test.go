package domain

import (
	"reflect"
	"testing"
)

func photo(no int64) Photo {
	return Photo{PhotoNo: no, ImageFilePath: "1/key", JapaneseTitle: "写真", Direction: DirectionHorizontal}
}

func portfolioPhotos(settings ...map[string]any) []PortfolioPhoto {
	photos := make([]PortfolioPhoto, 0, len(settings))
	for i, s := range settings {
		photos = append(photos, PortfolioPhoto{Photo: photo(int64(i + 1)), DisplayOrder: i + 1, Settings: s})
	}
	return photos
}

func rows(patterns ...string) map[string]any {
	list := make([]any, 0, len(patterns))
	for _, p := range patterns {
		list = append(list, map[string]any{"pattern": p})
	}
	return map[string]any{"rows": list}
}

func TestBuildLayout_Feature(t *testing.T) {
	p := Portfolio{
		Template: TemplateFeature,
		Photos: portfolioPhotos(
			map[string]any{"theme": "日本の自然", "subTheme": "京都", "description": "金色に輝く"},
			map[string]any{"theme": "光と影"},
			map[string]any{"theme": 1, "caption": "他テンプレートの項目"},
		),
	}

	layout, err := BuildLayout(p)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	want := FeatureLayout{Sections: []FeatureSection{
		{Photo: photo(1), Theme: "日本の自然", SubTheme: "京都", Description: "金色に輝く"},
		// 未設定の項目は空文字にする
		{Photo: photo(2), Theme: "光と影"},
		// 文字列以外の値・他テンプレートの項目は無視する
		{Photo: photo(3)},
	}}
	if !reflect.DeepEqual(layout, want) {
		t.Errorf("layout = %#v, want %#v", layout, want)
	}
}

func TestBuildLayout_Grid(t *testing.T) {
	tests := []struct {
		name     string
		settings map[string]any
		photos   int
		want     []GridRowPattern
		wantNos  [][]int64
	}{
		{
			name:     "行パターンの枚数ずつ、写真を表示順に各行へ割り当てる",
			settings: rows("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW", "NARROW_WIDE"),
			photos:   9,
			want:     []GridRowPattern{GridRowTwoEqual, GridRowThreeEqual, GridRowWideNarrow, GridRowNarrowWide},
			wantNos:  [][]int64{{1, 2}, {3, 4, 5}, {6, 7}, {8, 9}},
		},
		{
			name:     "写真が足りない行以降は表示しない",
			settings: rows("TWO_EQUAL", "THREE_EQUAL"),
			photos:   4,
			want:     []GridRowPattern{GridRowTwoEqual},
			wantNos:  [][]int64{{1, 2}},
		},
		{
			name:     "行に割り当てられない写真は表示しない",
			settings: rows("TWO_EQUAL"),
			photos:   3,
			want:     []GridRowPattern{GridRowTwoEqual},
			wantNos:  [][]int64{{1, 2}},
		},
		{
			name:     "未知の行パターン以降は表示しない",
			settings: rows("TWO_EQUAL", "FOUR_EQUAL", "TWO_EQUAL"),
			photos:   6,
			want:     []GridRowPattern{GridRowTwoEqual},
			wantNos:  [][]int64{{1, 2}},
		},
		{
			name:     "行の一覧が未設定の場合は行なし",
			settings: map[string]any{},
			photos:   2,
			want:     []GridRowPattern{},
			wantNos:  [][]int64{},
		},
		{
			name:     "行がオブジェクトでない場合は、その行以降を表示しない",
			settings: map[string]any{"rows": []any{"TWO_EQUAL"}},
			photos:   2,
			want:     []GridRowPattern{},
			wantNos:  [][]int64{},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			settings := make([]map[string]any, tt.photos)
			for i := range settings {
				settings[i] = map[string]any{}
			}
			layout, err := BuildLayout(Portfolio{Template: TemplateGrid, TemplateSettings: tt.settings, Photos: portfolioPhotos(settings...)})
			if err != nil {
				t.Fatalf("unexpected error: %v", err)
			}
			grid, ok := layout.(GridLayout)
			if !ok {
				t.Fatalf("layout type = %T, want GridLayout", layout)
			}
			gotPatterns := make([]GridRowPattern, 0)
			gotNos := make([][]int64, 0)
			for _, row := range grid.Rows {
				gotPatterns = append(gotPatterns, row.Pattern)
				nos := make([]int64, 0)
				for _, p := range row.Photos {
					nos = append(nos, p.Photo.PhotoNo)
				}
				gotNos = append(gotNos, nos)
			}
			if !reflect.DeepEqual(gotPatterns, tt.want) {
				t.Errorf("patterns = %v, want %v", gotPatterns, tt.want)
			}
			if !reflect.DeepEqual(gotNos, tt.wantNos) {
				t.Errorf("photo nos = %v, want %v", gotNos, tt.wantNos)
			}
		})
	}
}

func TestBuildLayout_UnknownTemplate(t *testing.T) {
	if _, err := BuildLayout(Portfolio{Template: "slideshow"}); err == nil {
		t.Error("expected error for unknown template")
	}
}

func TestIsValidAccountID(t *testing.T) {
	tests := map[string]bool{
		"abcd1234":              true,
		"abcdefghij1234567890":  true,
		"abc1234":               false, // 7文字
		"abcdefghij12345678901": false, // 21文字
		"abcd-1234":             false,
		"":                      false,
		"ａｂｃｄ１２３４":              false, // 全角
	}
	for accountID, want := range tests {
		if got := IsValidAccountID(accountID); got != want {
			t.Errorf("IsValidAccountID(%q) = %v, want %v", accountID, got, want)
		}
	}
}
