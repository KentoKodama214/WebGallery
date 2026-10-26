package domain

import (
	"strings"
	"testing"
)

func TestNormalizeKeyword(t *testing.T) {
	cases := []struct {
		name   string
		input  string
		want   string
		wantOK bool
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
