package domain

const (
	// PortfolioPageMaxLimit は公開ページの一覧で一度に取得できる件数の上限。
	// 公開ページは「もっと見る」で先頭から読み込み済みの件数をまとめて取り直すため、1ページ分（20件）より大きくとる。
	PortfolioPageMaxLimit = 1000
	// PortfolioPageMaxOffset は取得開始位置の上限（深いオフセットページングによる負荷増大を抑える）。
	PortfolioPageMaxOffset = 200000
)

// IsValidPortfolioPage は一覧の取得件数・取得開始位置が許容範囲内かどうかを判定する。
func IsValidPortfolioPage(limit, offset int) bool {
	return limit >= 1 && limit <= PortfolioPageMaxLimit && offset >= 0 && offset <= PortfolioPageMaxOffset
}
