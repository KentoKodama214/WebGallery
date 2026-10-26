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
func NormalizeKeyword(keyword string) (normalized string, ok bool) {
	normalized = strings.TrimSpace(keyword)
	if utf8.RuneCountInString(normalized) > KeywordMaxLength {
		return "", false
	}
	return normalized, true
}
