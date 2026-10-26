package domain

import "testing"

func TestIsValidPortfolioPage(t *testing.T) {
	cases := []struct {
		name          string
		limit, offset int
		want          bool
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
