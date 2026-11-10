package domain

import (
	"math"
	"regexp"
)

// FrameLayout は額縁型のレイアウト（1枚の額縁に写真枠・メッセージ枠を配置した組写真）。
//
// 位置・大きさ・太さ・文字サイズは、表示する画面の幅によらず同じ比率で描画できるよう、額縁に対する % で持つ。
type FrameLayout struct {
	// Width は額縁の横幅（mm）。表示では縦横比の算出にだけ使う。
	Width float64
	// Height は額縁の縦幅（mm）。
	Height float64
	// BackgroundColor は額縁の背景（台紙）の色（#RRGGBB）。
	BackgroundColor string
	// BorderColor は額縁の外枠の色（#RRGGBB）。
	BorderColor string
	// BorderWidth は額縁の外枠の太さ（額縁の横幅に対する %。0 の場合は外枠なし）。
	BorderWidth float64
	// Slots は写真枠（額縁定義の順。写真を表示順に1枚ずつ割り当てる）。
	Slots []FrameSlot
	// Texts は文面を入力したメッセージ枠（額縁定義の順）。
	Texts []FrameText
}

// FrameSlot は額縁型の写真枠。
type FrameSlot struct {
	// X・Y は写真枠の左上の位置（額縁に対する %）。
	X, Y float64
	// Width・Height は写真枠の大きさ（額縁に対する %）。
	Width, Height float64
	// Rotation は写真枠の傾き（度。時計回りが正）。
	Rotation float64
	// BorderColor は写真枠の枠線の色（#RRGGBB）。
	BorderColor string
	// BorderWidth は写真枠の枠線の太さ（額縁の横幅に対する %。0 の場合は枠線なし）。
	BorderWidth float64
	// Shadow は写真枠に影を付けるかどうか。
	Shadow bool
	// Photo は写真枠に当てはめる写真。管理者により非公開にされた写真の枠・写真が足りない枠は nil（空き枠として表示する）。
	Photo *Photo
}

// FrameText は額縁型のメッセージ枠。
type FrameText struct {
	// X・Y はメッセージ枠の左上の位置（額縁に対する %）。
	X, Y float64
	// Width はメッセージ枠の横幅（額縁に対する %。文面はこの幅で折り返す）。
	Width float64
	// Rotation はメッセージ枠の傾き（度。時計回りが正）。
	Rotation float64
	// FontFamily はフォント（FrameFontFamilies のいずれか）。
	FontFamily string
	// FontSize は文字サイズ（額縁の横幅に対する %）。
	FontSize float64
	// Color は文字の色（#RRGGBB）。
	Color string
	// Align は文面の揃え（FrameTextAligns のいずれか）。
	Align string
	// Bold は太字にするかどうか。
	Bold bool
	// Italic は斜体にするかどうか。
	Italic bool
	// Text はポートフォリオごとに入力した文面。
	Text string
}

// FrameFontFamilies はメッセージ枠に指定できるフォント（Java 側の FrameDefinitionPolicy と一致させる）。
var FrameFontFamilies = []string{"gothic", "mincho", "maru", "handwriting", "serif", "script"}

// FrameTextAligns はメッセージ枠に指定できる揃え（Java 側の FrameDefinitionPolicy と一致させる）。
var FrameTextAligns = []string{"left", "center", "right"}

// ポートフォリオ単位の設定・額縁定義の項目名（Java 側の FrameTemplateSettingsPolicy・FrameDefinitionPolicy と一致させる）
const (
	settingFrame     = "frame"
	settingMessages  = "messages"
	frameSize        = "size"
	frameBackground  = "background"
	frameBorder      = "border"
	frameSlots       = "slots"
	frameTexts       = "texts"
	frameColor       = "color"
	frameWidth       = "width"
	frameHeight      = "height"
	frameX           = "x"
	frameY           = "y"
	frameRotation    = "rotation"
	frameBorderColor = "borderColor"
	frameBorderWidth = "borderWidth"
	frameShadow      = "shadow"
	frameFontFamily  = "fontFamily"
	frameFontSize    = "fontSize"
	frameAlign       = "align"
	frameBold        = "bold"
	frameItalic      = "italic"
)

// 額縁定義の値が未設定・不正な場合に使う既定値
const (
	defaultFrameWidth      = 364.0
	defaultFrameHeight     = 257.0
	defaultBackgroundColor = "#ffffff"
	defaultBorderColor     = "#000000"
	defaultSlotBorderColor = "#ffffff"
	defaultTextColor       = "#333333"
	defaultFontFamily      = "gothic"
	defaultFontSize        = 3.0
	defaultAlign           = "left"
)

// colorPattern は額縁定義の色（#RRGGBB）の形式。
var colorPattern = regexp.MustCompile(`^#[0-9a-fA-F]{6}$`)

// buildFrameLayout は額縁型のレイアウトを組み立てる。
//
// 公開中のポートフォリオは Java 側で額縁定義を検証済みで、写真枠の数と写真の枚数も一致しているが、表示側でも
// 設定の不整合で処理が止まらないよう防御的に組み立てる（未設定・不正な値は既定値にし、範囲外の値は範囲内に収める）。
// 写真は表示順に写真枠へ1枚ずつ割り当て、管理者により非公開にされた写真の枠・写真が足りない枠は空き枠にする
// （後ろの写真を詰めて組写真の構成を崩さないため）。写真枠より多い写真は表示しない。
// メッセージ枠は、文面が空のものを表示しない。
//
// 引数:
//   - p: 額縁型のポートフォリオ
//
// 戻り値:
//   - 額縁定義の順に写真枠・メッセージ枠を並べたレイアウト
func buildFrameLayout(p Portfolio) FrameLayout {
	frame, _ := p.TemplateSettings[settingFrame].(map[string]any)
	size, _ := frame[frameSize].(map[string]any)
	background, _ := frame[frameBackground].(map[string]any)
	border, _ := frame[frameBorder].(map[string]any)

	layout := FrameLayout{
		Width:           positiveNumberSetting(size, frameWidth, defaultFrameWidth),
		Height:          positiveNumberSetting(size, frameHeight, defaultFrameHeight),
		BackgroundColor: colorSetting(background, frameColor, defaultBackgroundColor),
		BorderColor:     colorSetting(border, frameColor, defaultBorderColor),
		BorderWidth:     clampedNumberSetting(border, frameWidth, 0, 0, 20),
		Slots:           make([]FrameSlot, 0),
		Texts:           make([]FrameText, 0),
	}

	for i, slot := range objectList(frame[frameSlots]) {
		frameSlot := FrameSlot{
			X:           clampedNumberSetting(slot, frameX, 0, 0, 100),
			Y:           clampedNumberSetting(slot, frameY, 0, 0, 100),
			Width:       clampedNumberSetting(slot, frameWidth, 100, 0, 100),
			Height:      clampedNumberSetting(slot, frameHeight, 100, 0, 100),
			Rotation:    clampedNumberSetting(slot, frameRotation, 0, -180, 180),
			BorderColor: colorSetting(slot, frameBorderColor, defaultSlotBorderColor),
			BorderWidth: clampedNumberSetting(slot, frameBorderWidth, 0, 0, 10),
			Shadow:      boolSetting(slot, frameShadow),
		}
		if i < len(p.Photos) && !p.Photos[i].HiddenByAdmin {
			photo := p.Photos[i].Photo
			frameSlot.Photo = &photo
		}
		layout.Slots = append(layout.Slots, frameSlot)
	}

	messages, _ := p.TemplateSettings[settingMessages].([]any)
	for i, text := range objectList(frame[frameTexts]) {
		message := ""
		if i < len(messages) {
			message, _ = messages[i].(string)
		}
		if message == "" {
			continue
		}
		layout.Texts = append(layout.Texts, FrameText{
			X:          clampedNumberSetting(text, frameX, 0, 0, 100),
			Y:          clampedNumberSetting(text, frameY, 0, 0, 100),
			Width:      clampedNumberSetting(text, frameWidth, 100, 0, 100),
			Rotation:   clampedNumberSetting(text, frameRotation, 0, -180, 180),
			FontFamily: choiceSetting(text, frameFontFamily, FrameFontFamilies, defaultFontFamily),
			FontSize:   clampedNumberSetting(text, frameFontSize, defaultFontSize, 0.5, 30),
			Color:      colorSetting(text, frameColor, defaultTextColor),
			Align:      choiceSetting(text, frameAlign, FrameTextAligns, defaultAlign),
			Bold:       boolSetting(text, frameBold),
			Italic:     boolSetting(text, frameItalic),
			Text:       message,
		})
	}
	return layout
}

// objectList は設定値の配列から、オブジェクトの要素だけを取り出す。
//
// 引数:
//   - value: 設定値（配列でない場合は要素なしとして扱う）
//
// 戻り値:
//   - オブジェクトの要素の一覧（オブジェクトでない要素は取り除く）
func objectList(value any) []map[string]any {
	list, _ := value.([]any)
	result := make([]map[string]any, 0, len(list))
	for _, item := range list {
		if object, ok := item.(map[string]any); ok {
			result = append(result, object)
		}
	}
	return result
}

// clampedNumberSetting は設定から数値の項目を取り出し、範囲内に収める。
//
// 引数:
//   - settings: 設定（nil 可）
//   - key: 取り出す項目名
//   - fallback: 未設定・数値以外・有限でない場合の値
//   - min・max: 値の範囲
//
// 戻り値:
//   - 範囲内に収めた値
func clampedNumberSetting(settings map[string]any, key string, fallback, min, max float64) float64 {
	value, ok := settings[key].(float64)
	if !ok || math.IsNaN(value) || math.IsInf(value, 0) {
		return fallback
	}
	return math.Min(math.Max(value, min), max)
}

// positiveNumberSetting は設定から正の数値の項目を取り出す。
//
// 引数:
//   - settings: 設定（nil 可）
//   - key: 取り出す項目名
//   - fallback: 未設定・数値以外・0以下の場合の値
//
// 戻り値:
//   - 正の数値
func positiveNumberSetting(settings map[string]any, key string, fallback float64) float64 {
	value, ok := settings[key].(float64)
	if !ok || !(value > 0) || math.IsInf(value, 0) {
		return fallback
	}
	return value
}

// colorSetting は設定から色（#RRGGBB）の項目を取り出す。
//
// 引数:
//   - settings: 設定（nil 可）
//   - key: 取り出す項目名
//   - fallback: 未設定・形式が不正な場合の値
//
// 戻り値:
//   - #RRGGBB 形式の色（CSS にそのまま埋め込むため、形式に合わない値は返さない）
func colorSetting(settings map[string]any, key string, fallback string) string {
	value, _ := settings[key].(string)
	if !colorPattern.MatchString(value) {
		return fallback
	}
	return value
}

// choiceSetting は設定から、選択肢のいずれかである文字列の項目を取り出す。
//
// 引数:
//   - settings: 設定（nil 可）
//   - key: 取り出す項目名
//   - choices: 選択肢
//   - fallback: 未設定・選択肢にない場合の値
//
// 戻り値:
//   - 選択肢のいずれか
func choiceSetting(settings map[string]any, key string, choices []string, fallback string) string {
	value, _ := settings[key].(string)
	for _, choice := range choices {
		if value == choice {
			return value
		}
	}
	return fallback
}

// boolSetting は設定から真偽値の項目を取り出す。
//
// 引数:
//   - settings: 設定（nil 可）
//   - key: 取り出す項目名
//
// 戻り値:
//   - 項目の値。未設定・真偽値以外の場合は false
func boolSetting(settings map[string]any, key string) bool {
	value, _ := settings[key].(bool)
	return value
}

// isLayout は FrameLayout を Layout として扱えるようにするためのマーカー。
func (FrameLayout) isLayout() {}
