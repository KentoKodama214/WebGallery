package domain

import (
	"math"
	"reflect"
	"testing"
)

// frameSettings は額縁型のポートフォリオ単位のテンプレート設定を作る。
//
// 引数:
//   - frame: 額縁定義
//   - messages: メッセージ枠ごとの文面
//
// 戻り値:
//   - frame・messages を持つテンプレート設定
func frameSettings(frame map[string]any, messages ...any) map[string]any {
	return map[string]any{"frameTemplateNo": 1.0, "frame": frame, "messages": messages}
}

// TestBuildLayout_Frame は、額縁型のポートフォリオから、額縁定義の値をそのまま持つレイアウトが組み立てられ、
// 写真が表示順に写真枠へ1枚ずつ割り当てられ、メッセージ枠に文面が設定されることを検証する。
func TestBuildLayout_Frame(t *testing.T) {
	p := Portfolio{
		Template: TemplateFrame,
		TemplateSettings: frameSettings(map[string]any{
			"size":       map[string]any{"width": 254.0, "height": 305.0},
			"background": map[string]any{"color": "#F5F0E6"},
			"border":     map[string]any{"color": "#3b2a1a", "width": 4.0},
			"slots": []any{
				map[string]any{"x": 5.0, "y": 10.0, "width": 40.0, "height": 60.0, "rotation": -3.5,
					"borderColor": "#ffffff", "borderWidth": 1.0, "shadow": true},
				map[string]any{"x": 55.0, "y": 10.0, "width": 40.0, "height": 60.0},
			},
			"texts": []any{
				map[string]any{"x": 10.0, "y": 80.0, "width": 80.0, "rotation": 2.0, "fontFamily": "script",
					"fontSize": 4.5, "color": "#000000", "align": "right", "bold": true},
			},
		}, "春の訪れ"),
		Photos: portfolioPhotos(map[string]any{}, map[string]any{}),
	}

	layout, err := BuildLayout(p)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	photo1, photo2 := photo(1), photo(2)
	want := FrameLayout{
		Width: 254, Height: 305, BackgroundColor: "#F5F0E6", BorderColor: "#3b2a1a", BorderWidth: 4,
		Slots: []FrameSlot{
			{X: 5, Y: 10, Width: 40, Height: 60, Rotation: -3.5, BorderColor: "#ffffff", BorderWidth: 1, Shadow: true, Photo: &photo1},
			// 未設定の項目は既定値（傾き0・枠線なし・影なし。枠線の色は白）にする
			{X: 55, Y: 10, Width: 40, Height: 60, BorderColor: defaultSlotBorderColor, Photo: &photo2},
		},
		Texts: []FrameText{
			{X: 10, Y: 80, Width: 80, Rotation: 2, FontFamily: "script", FontSize: 4.5, Color: "#000000", Align: "right", Bold: true, Text: "春の訪れ"},
		},
	}
	if !reflect.DeepEqual(layout, want) {
		t.Errorf("layout = %#v, want %#v", layout, want)
	}
}

// TestBuildLayout_FramePhotoAssignment は、額縁型で、管理者により非公開にされた写真の枠・写真が足りない枠を空き枠
// （Photo が nil）にし、後ろの写真を詰めないこと、写真枠より多い写真は表示しないことを検証する。
func TestBuildLayout_FramePhotoAssignment(t *testing.T) {
	slot := map[string]any{"x": 0.0, "y": 0.0, "width": 30.0, "height": 30.0}
	frame := map[string]any{"size": map[string]any{"width": 300.0, "height": 300.0}, "slots": []any{slot, slot, slot}}

	t.Run("管理者により非公開にされた写真の枠は空き枠にする", func(t *testing.T) {
		photos := portfolioPhotos(map[string]any{}, map[string]any{}, map[string]any{})
		photos[1].HiddenByAdmin = true
		layout := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: frameSettings(frame), Photos: photos})

		got := []*Photo{layout.Slots[0].Photo, layout.Slots[1].Photo, layout.Slots[2].Photo}
		if got[0] == nil || got[0].PhotoNo != 1 || got[1] != nil || got[2] == nil || got[2].PhotoNo != 3 {
			t.Errorf("slot photos = %v, want [1 nil 3]", got)
		}
	})

	t.Run("写真が足りない枠は空き枠にし、写真枠より多い写真は表示しない", func(t *testing.T) {
		short := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: frameSettings(frame),
			Photos: portfolioPhotos(map[string]any{})})
		if len(short.Slots) != 3 || short.Slots[0].Photo == nil || short.Slots[1].Photo != nil || short.Slots[2].Photo != nil {
			t.Errorf("slots = %#v, want only the first slot filled", short.Slots)
		}

		long := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: frameSettings(frame),
			Photos: portfolioPhotos(map[string]any{}, map[string]any{}, map[string]any{}, map[string]any{})})
		if len(long.Slots) != 3 || long.Slots[2].Photo == nil || long.Slots[2].Photo.PhotoNo != 3 {
			t.Errorf("slots = %#v, want 3 slots filled with photos 1-3", long.Slots)
		}
	})
}

// TestBuildLayout_FrameMessages は、額縁型で、文面が空・未入力・文字列以外のメッセージ枠を表示しないことを検証する。
func TestBuildLayout_FrameMessages(t *testing.T) {
	text := func(x float64) map[string]any {
		return map[string]any{"x": x, "y": 0.0, "width": 50.0, "fontFamily": "gothic", "fontSize": 3.0}
	}
	frame := map[string]any{
		"size":  map[string]any{"width": 300.0, "height": 300.0},
		"slots": []any{map[string]any{"x": 0.0, "y": 0.0, "width": 30.0, "height": 30.0}},
		"texts": []any{text(1), text(2), text(3), text(4)},
	}

	// 1つ目は空文字、2つ目は文字列以外、3つ目だけ文面あり、4つ目は文面の一覧より後ろ（未入力）
	layout := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: frameSettings(frame, "", 1.0, "3つ目")})

	if len(layout.Texts) != 1 || layout.Texts[0].X != 3 || layout.Texts[0].Text != "3つ目" {
		t.Errorf("texts = %#v, want only the third text", layout.Texts)
	}
}

// TestBuildLayout_FrameDefensive は、額縁型で、額縁定義が未設定・不正な値の場合に処理を止めず、既定値・範囲内の値で
// 組み立てることを検証する（CSS に埋め込む色は #RRGGBB 形式以外を返さない）。
func TestBuildLayout_FrameDefensive(t *testing.T) {
	t.Run("額縁定義がない場合は既定のサイズ・色で、写真枠・メッセージ枠なしにする", func(t *testing.T) {
		layout := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: map[string]any{},
			Photos: portfolioPhotos(map[string]any{})})

		want := FrameLayout{Width: defaultFrameWidth, Height: defaultFrameHeight, BackgroundColor: defaultBackgroundColor,
			BorderColor: defaultBorderColor, Slots: []FrameSlot{}, Texts: []FrameText{}}
		if !reflect.DeepEqual(layout, want) {
			t.Errorf("layout = %#v, want %#v", layout, want)
		}
	})

	t.Run("不正な値は既定値に、範囲外の値は範囲内に収める", func(t *testing.T) {
		frame := map[string]any{
			"size":       map[string]any{"width": -1.0, "height": "257"},
			"background": map[string]any{"color": "red; background-image: url(x)"},
			"border":     map[string]any{"color": "#3b2a1a", "width": 99.0},
			"slots": []any{
				"オブジェクトでない写真枠",
				map[string]any{"x": -10.0, "y": 150.0, "width": math.Inf(1), "height": 0.5, "rotation": 400.0, "borderWidth": 50.0, "shadow": "true"},
			},
			"texts": []any{
				map[string]any{"x": 10.0, "y": 10.0, "width": 50.0, "fontFamily": "Comic Sans", "fontSize": 100.0, "align": "justify", "color": "#00f"},
			},
		}
		layout := buildFrameLayout(Portfolio{Template: TemplateFrame, TemplateSettings: frameSettings(frame, "文面"),
			Photos: portfolioPhotos(map[string]any{})})

		if layout.Width != defaultFrameWidth || layout.Height != defaultFrameHeight {
			t.Errorf("size = %v x %v, want default", layout.Width, layout.Height)
		}
		if layout.BackgroundColor != defaultBackgroundColor || layout.BorderWidth != 20 {
			t.Errorf("background = %q, borderWidth = %v", layout.BackgroundColor, layout.BorderWidth)
		}
		wantSlot := FrameSlot{X: 0, Y: 100, Width: 100, Height: 0.5, Rotation: 180, BorderColor: defaultSlotBorderColor, BorderWidth: 10}
		if len(layout.Slots) != 1 {
			t.Fatalf("slots = %#v, want 1 slot (non-object slot is skipped)", layout.Slots)
		}
		gotSlot := layout.Slots[0]
		gotSlot.Photo = nil
		if !reflect.DeepEqual(gotSlot, wantSlot) {
			t.Errorf("slot = %#v, want %#v", gotSlot, wantSlot)
		}
		wantText := FrameText{X: 10, Y: 10, Width: 50, FontFamily: defaultFontFamily, FontSize: 30, Color: defaultTextColor, Align: defaultAlign, Text: "文面"}
		if len(layout.Texts) != 1 || !reflect.DeepEqual(layout.Texts[0], wantText) {
			t.Errorf("texts = %#v, want %#v", layout.Texts, wantText)
		}
	})
}
