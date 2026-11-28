package domain

import (
	"strings"
	"testing"
)

// TestNormalizeKeyword は、検索語の前後の空白（全角空白を含む）が除かれ、空白だけの場合は空文字になること、
// 最大文字数ちょうどは受け付け、超える場合は ok=false になることを検証する。
func TestNormalizeKeyword(t *testing.T) {
	cases := []struct {
		name   string // テストケースの説明
		input  string // 入力する検索語
		want   string // 期待する正規化後の検索語
		wantOK bool   // 期待する ok の値
	}{
		{"前後の空白を除く", "  自然\t", "自然", true},
		{"空白だけは空文字", "　 ", "", true},
		{"最大文字数ちょうど", strings.Repeat("あ", KeywordMaxLength), strings.Repeat("あ", KeywordMaxLength), true},
		{"最大文字数を超える", strings.Repeat("あ", KeywordMaxLength+1), "", false},
	}
	for _, c := range cases {
		got, ok := NormalizeKeyword(c.input)
		if got != c.want || ok != c.wantOK {
			t.Errorf("%s: NormalizeKeyword(%q) = (%q, %v), want (%q, %v)", c.name, c.input, got, ok, c.want, c.wantOK)
		}
	}
}
