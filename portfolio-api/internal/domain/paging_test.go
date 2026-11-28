package domain

import "testing"

// TestIsValidPortfolioPage は、取得件数・取得開始位置が下限・上限ちょうどの場合は true、
// 範囲外（limit が0・上限超え、offset が負・上限超え）の場合は false になることを検証する。
func TestIsValidPortfolioPage(t *testing.T) {
	cases := []struct {
		name          string // テストケースの説明
		limit, offset int    // 判定する取得件数・取得開始位置
		want          bool   // 期待する判定結果
	}{
		{"下限", 1, 0, true},
		{"上限", PortfolioPageMaxLimit, PortfolioPageMaxOffset, true},
		{"limitが0", 0, 0, false},
		{"limitが上限超え", PortfolioPageMaxLimit + 1, 0, false},
		{"offsetが負", 20, -1, false},
		{"offsetが上限超え", 20, PortfolioPageMaxOffset + 1, false},
	}
	for _, c := range cases {
		if got := IsValidPortfolioPage(c.limit, c.offset); got != c.want {
			t.Errorf("%s: IsValidPortfolioPage(%d, %d) = %v, want %v", c.name, c.limit, c.offset, got, c.want)
		}
	}
}
