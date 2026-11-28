package domain

const (
	// PortfolioPageMaxLimit は公開ページの一覧で一度に取得できる件数の上限。
	// 公開ページは「もっと見る」で先頭から読み込み済みの件数をまとめて取り直すため、1ページ分（20件）より大きくとる。
	PortfolioPageMaxLimit = 1000
	// PortfolioPageMaxOffset は取得開始位置の上限（深いオフセットページングによる負荷増大を抑える）。
	PortfolioPageMaxOffset = 200000
)

// IsValidPortfolioPage は一覧の取得件数・取得開始位置が許容範囲内かどうかを判定する。
//
// 引数:
//   - limit: 一度に取得する件数
//   - offset: 取得開始位置（先頭から読み飛ばす件数）
//
// 戻り値:
//   - limit が 1〜PortfolioPageMaxLimit、かつ offset が 0〜PortfolioPageMaxOffset の場合は true、それ以外は false
func IsValidPortfolioPage(limit, offset int) bool {
	return limit >= 1 && limit <= PortfolioPageMaxLimit && offset >= 0 && offset <= PortfolioPageMaxOffset
}
