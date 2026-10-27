package domain

import (
	"strings"
	"unicode/utf8"
)

// KeywordMaxLength は公開ページの絞り込みの検索語の最大文字数（ポートフォリオのテーマの最大文字数と一致させる）。
const KeywordMaxLength = 100

// NormalizeKeyword は絞り込みの検索語の前後の空白を除く。
//
// 空白だけの場合は空文字（絞り込まない）を返す。最大文字数を超える場合は ok=false を返す。
//
// 引数:
//   - keyword: 利用者が入力した検索語
//
// 戻り値:
//   - normalized: 前後の空白（全角空白を含む）を除いた検索語。ok=false の場合は空文字
//   - ok: 前後の空白を除いた文字数が KeywordMaxLength 以下の場合は true、超える場合は false
func NormalizeKeyword(keyword string) (normalized string, ok bool) {
	normalized = strings.TrimSpace(keyword)
	if utf8.RuneCountInString(normalized) > KeywordMaxLength {
		return "", false
	}
	return normalized, true
}
