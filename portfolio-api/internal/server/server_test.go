package server

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"io"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"
	"time"

	"github.com/KentoKodama214/WebGallery/portfolio-api/graph"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/domain"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/usecase"
)

// fakeQuery はテスト用の graph.PortfolioQuery。あらかじめ設定した値を返し、呼び出し回数と受け取った引数を記録する。
type fakeQuery struct {
	// summaries は ListPublishedPortfolios が返すポートフォリオ一覧。
	summaries []domain.PortfolioSummary
	// published はポートフォリオ番号ごとに GetPublishedPortfolio が返すポートフォリオ（ないものは nil を返す）。
	published map[int64]*usecase.PublishedPortfolio
	// err は各メソッドが返すエラー（nil の場合はエラーにしない）。
	err error
	// hasNext は ListPublishedPortfolios が返す続きの有無。
	hasNext bool
	// calls はリゾルバからユースケースが呼び出された回数（クエリが実行されなかったことの検証に使う）。
	calls int
	// keyword は ListPublishedPortfolios が最後に受け取った検索語。
	keyword string
	// limit は ListPublishedPortfolios が最後に受け取った取得件数。
	limit int
	// offset は ListPublishedPortfolios が最後に受け取った取得開始位置。
	offset int
}

// ListPublishedPortfolios は呼び出し回数と受け取った引数を記録し、summaries・hasNext・err を返す。
func (f *fakeQuery) ListPublishedPortfolios(_ context.Context, _ string, keyword string, limit, offset int) (usecase.PortfolioSummaryPage, error) {
	f.calls++
	f.keyword = keyword
	f.limit = limit
	f.offset = offset
	return usecase.PortfolioSummaryPage{Portfolios: f.summaries, HasNext: f.hasNext}, f.err
}

// GetPublishedPortfolio は呼び出し回数を記録し、err が設定されていればそれを、なければ published から引いたポートフォリオを返す。
func (f *fakeQuery) GetPublishedPortfolio(_ context.Context, _ string, portfolioNo int64) (*usecase.PublishedPortfolio, error) {
	f.calls++
	if f.err != nil {
		return nil, f.err
	}
	return f.published[portfolioNo], nil
}

// fakeHealth はテスト用の HealthChecker。
type fakeHealth struct {
	// err は Ping が返すエラー（nil の場合は疎通できたものとする）。
	err error
}

// Ping は err を返す。
func (f fakeHealth) Ping(context.Context) error { return f.err }

// updatedAt はテスト用のポートフォリオの最終更新日時。
var updatedAt = time.Date(2026, 10, 1, 9, 0, 0, 0, time.UTC)

// signedPhoto は署名付きURLを設定済みのテスト用の写真を作る。
//
// 引数:
//   - no: 写真番号（0〜9。URL の末尾に使う）
//
// 戻り値:
//   - URL が "https://signed/{写真番号}" の写真（正方形）
func signedPhoto(no int64) domain.Photo {
	return domain.Photo{PhotoNo: no, URL: "https://signed/" + string(rune('0'+no)), JapaneseTitle: "金閣寺", Direction: domain.DirectionSquare}
}

// newTestServer は fakeQuery を使う GraphQL ハンドラでテスト用の HTTP サーバーを起動する（テスト終了時に停止する）。
//
// 引数:
//   - t: テストの状態
//   - query: リゾルバが使うユースケース
//   - opts: GraphQL ハンドラの設定（複雑度・深さの上限が 0 の場合は本番の既定値 300・8 を使い、ログは捨てる）
//
// 戻り値:
//   - 起動したテスト用の HTTP サーバー
func newTestServer(t *testing.T, query *fakeQuery, opts Options) *httptest.Server {
	t.Helper()
	if opts.MaxQueryComplexity == 0 {
		opts.MaxQueryComplexity = 300
	}
	if opts.MaxQueryDepth == 0 {
		opts.MaxQueryDepth = 8
	}
	opts.Logger = slog.New(slog.NewTextHandler(io.Discard, nil))
	handler := NewGraphQLHandler(&graph.Resolver{PortfolioQuery: query}, opts)
	server := httptest.NewServer(NewMux(handler, fakeHealth{}))
	t.Cleanup(server.Close)
	return server
}

// gqlResponse は GraphQL のレスポンス。
type gqlResponse struct {
	// Data はクエリの結果（エラーで実行されなかった場合は nil）。
	Data map[string]any `json:"data"`
	// Errors はクエリのエラー（エラーがない場合は空）。
	Errors []struct {
		// Message はエラーメッセージ。
		Message string `json:"message"`
	} `json:"errors"`
}

// post は GraphQL のクエリを POST で送信し、レスポンスをデコードして返す。
//
// 引数:
//   - t: テストの状態（送信・デコードに失敗した場合はテストを失敗させる）
//   - server: 送信先のテスト用の HTTP サーバー
//   - query: 送信する GraphQL のクエリ（変数なし）
//
// 戻り値:
//   - デコードした GraphQL のレスポンス
func post(t *testing.T, server *httptest.Server, query string) gqlResponse {
	t.Helper()
	body, _ := json.Marshal(map[string]any{"query": query})
	res, err := http.Post(server.URL+"/graphql", "application/json", bytes.NewReader(body))
	if err != nil {
		t.Fatalf("request failed: %v", err)
	}
	defer res.Body.Close()
	var decoded gqlResponse
	if err := json.NewDecoder(res.Body).Decode(&decoded); err != nil {
		t.Fatalf("decode failed: %v", err)
	}
	return decoded
}

// toJSON は値を JSON の文字列にする（マップのキーが整列されるため、レスポンスの比較に使う）。
//
// 引数:
//   - v: JSON にする値
//
// 戻り値:
//   - JSON の文字列（変換に失敗した場合は空文字）
func toJSON(v any) string {
	b, _ := json.Marshal(v)
	return string(b)
}

// portfolioQuery は publishedPortfolio をレイアウト（FeatureLayout・GridLayout の両方）まで取得するクエリ。変数 no にポートフォリオ番号を渡す。
const portfolioQuery = `query($no: Int!) {
  publishedPortfolio(accountId: "owner123", portfolioNo: $no) {
    portfolioNo theme template tags { japaneseName englishName }
    layout {
      __typename
      ... on FeatureLayout { sections { theme subTheme description photo { photoNo url direction } } }
      ... on GridLayout { rows { pattern photos { photo { photoNo } } } }
    }
  }
}`

// TestPublishedPortfolio_LayoutUnion は、publishedPortfolio のレイアウトが、テンプレートに応じて
// FeatureLayout・GridLayout のいずれかの型（__typename）で返り、各型のフィールド・タグ・列挙値が正しく変換されることを検証する。
func TestPublishedPortfolio_LayoutUnion(t *testing.T) {
	query := &fakeQuery{published: map[int64]*usecase.PublishedPortfolio{
		1: {
			Portfolio: domain.Portfolio{PortfolioNo: 1, Theme: "日本の自然", Template: domain.TemplateFeature, UpdatedAt: updatedAt,
				Tags: []domain.Tag{{JapaneseName: "自然", EnglishName: "nature"}}},
			Layout: domain.FeatureLayout{Sections: []domain.FeatureSection{{Photo: signedPhoto(5), Theme: "京都", SubTheme: "金閣寺"}}},
		},
		2: {
			Portfolio: domain.Portfolio{PortfolioNo: 2, Theme: "光と影", Template: domain.TemplateGrid, UpdatedAt: updatedAt, Tags: []domain.Tag{}},
			Layout: domain.GridLayout{Rows: []domain.GridRow{{Pattern: domain.GridRowWideNarrow,
				Photos: []domain.GridPhoto{{Photo: signedPhoto(1)}, {Photo: signedPhoto(2)}}}}},
		},
	}}
	server := newTestServer(t, query, Options{})

	for no, want := range map[int]string{
		1: `{"portfolioNo":1,"theme":"日本の自然","template":"FEATURE","tags":[{"japaneseName":"自然","englishName":"nature"}],` +
			`"layout":{"__typename":"FeatureLayout","sections":[{"theme":"京都","subTheme":"金閣寺","description":"","photo":{"photoNo":5,"url":"https://signed/5","direction":"SQUARE"}}]}}`,
		2: `{"portfolioNo":2,"theme":"光と影","template":"GRID","tags":[],` +
			`"layout":{"__typename":"GridLayout","rows":[{"pattern":"WIDE_NARROW","photos":[{"photo":{"photoNo":1}},{"photo":{"photoNo":2}}]}]}}`,
	} {
		body, _ := json.Marshal(map[string]any{"query": portfolioQuery, "variables": map[string]any{"no": no}})
		res, err := http.Post(server.URL+"/graphql", "application/json", bytes.NewReader(body))
		if err != nil {
			t.Fatal(err)
		}
		var decoded struct {
			Data struct {
				PublishedPortfolio json.RawMessage `json:"publishedPortfolio"`
			} `json:"data"`
		}
		_ = json.NewDecoder(res.Body).Decode(&decoded)
		res.Body.Close()
		var got, expected any
		_ = json.Unmarshal(decoded.Data.PublishedPortfolio, &got)
		_ = json.Unmarshal([]byte(want), &expected)
		if toJSON(got) != toJSON(expected) {
			t.Errorf("portfolio %d = %s\nwant %s", no, decoded.Data.PublishedPortfolio, want)
		}
	}
}

// TestPublishedPortfolio_NullWhenNotPublished は、ユースケースがポートフォリオを返さない（公開されていない等）場合に、
// エラーなしで publishedPortfolio が null になることを検証する。
func TestPublishedPortfolio_NullWhenNotPublished(t *testing.T) {
	server := newTestServer(t, &fakeQuery{}, Options{})

	res := post(t, server, `{ publishedPortfolio(accountId: "owner123", portfolioNo: 9) { portfolioNo } }`)

	if len(res.Errors) != 0 || res.Data["publishedPortfolio"] != nil {
		t.Errorf("response = %+v, want null without errors", res)
	}
}

// TestPublishedPortfolios は、publishedPortfolios がユースケースの返した順にポートフォリオを返し、
// 先頭の写真がない場合は coverPhoto が null になること、hasNext・更新日時（RFC 3339）が正しく返ることを検証する。
func TestPublishedPortfolios(t *testing.T) {
	cover := signedPhoto(3)
	server := newTestServer(t, &fakeQuery{summaries: []domain.PortfolioSummary{
		{PortfolioNo: 2, Theme: "光と影", Template: domain.TemplateGrid, UpdatedAt: updatedAt, PhotoCount: 4, CoverPhoto: &cover, Tags: []domain.Tag{}},
		{PortfolioNo: 1, Theme: "日本の自然", Template: domain.TemplateFeature, UpdatedAt: updatedAt, Tags: []domain.Tag{}},
	}, hasNext: true}, Options{})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolios { portfolioNo template photoCount updatedAt coverPhoto { photoNo url } } hasNext } }`)

	want := `{"hasNext":true,"portfolios":[{"coverPhoto":{"photoNo":3,"url":"https://signed/3"},"photoCount":4,"portfolioNo":2,"template":"GRID","updatedAt":"2026-10-01T09:00:00Z"},` +
		`{"coverPhoto":null,"photoCount":0,"portfolioNo":1,"template":"FEATURE","updatedAt":"2026-10-01T09:00:00Z"}]}`
	if got := toJSON(res.Data["publishedPortfolios"]); got != want {
		t.Errorf("publishedPortfolios = %s\nwant %s", got, want)
	}
}

// TestPublishedPortfolios_Keyword は、publishedPortfolios の keyword 引数がそのままユースケースへ渡されることを検証する。
func TestPublishedPortfolios_Keyword(t *testing.T) {
	query := &fakeQuery{summaries: []domain.PortfolioSummary{}}
	server := newTestServer(t, query, Options{})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123", keyword: "自然") { portfolios { portfolioNo } } }`)

	if len(res.Errors) != 0 || query.keyword != "自然" {
		t.Errorf("errors = %+v, keyword = %q; want keyword passed through", res.Errors, query.keyword)
	}
}

// TestPublishedPortfolios_LimitOffset は、publishedPortfolios の limit・offset を省略した場合はスキーマの既定値（20・0）が、
// 指定した場合はその値がユースケースへ渡されることを検証する。
func TestPublishedPortfolios_LimitOffset(t *testing.T) {
	// 省略した場合はスキーマの既定値（20件・先頭から）を渡す
	query := &fakeQuery{summaries: []domain.PortfolioSummary{}}
	server := newTestServer(t, query, Options{})
	post(t, server, `{ publishedPortfolios(accountId: "owner123") { hasNext } }`)
	if query.limit != 20 || query.offset != 0 {
		t.Errorf("default limit/offset = %d/%d, want 20/0", query.limit, query.offset)
	}

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123", limit: 40, offset: 20) { hasNext } }`)
	if len(res.Errors) != 0 || query.limit != 40 || query.offset != 20 {
		t.Errorf("errors = %+v, limit/offset = %d/%d; want 40/20 passed through", res.Errors, query.limit, query.offset)
	}
}

// TestGetTransport は、GET でクエリ（URL の query パラメータ）を送信した場合も、200 でクエリの結果が返ることを検証する。
func TestGetTransport(t *testing.T) {
	server := newTestServer(t, &fakeQuery{summaries: []domain.PortfolioSummary{}}, Options{})

	q := url.Values{"query": {`{ publishedPortfolios(accountId: "owner123") { portfolios { portfolioNo } } }`}}
	res, err := http.Get(server.URL + "/graphql?" + q.Encode())
	if err != nil {
		t.Fatal(err)
	}
	defer res.Body.Close()
	body, _ := io.ReadAll(res.Body)
	if res.StatusCode != http.StatusOK || string(body) != `{"data":{"publishedPortfolios":{"portfolios":[]}}}` {
		t.Errorf("status=%d body=%s", res.StatusCode, body)
	}
}

// TestIntrospection は、イントロスペクションが無効の場合はエラーになり、有効の場合はエラーなしで実行できることを検証する。
func TestIntrospection(t *testing.T) {
	const introspection = `{ __schema { queryType { name } } }`

	disabled := post(t, newTestServer(t, &fakeQuery{}, Options{IntrospectionEnabled: false}), introspection)
	if len(disabled.Errors) == 0 {
		t.Error("introspection should be rejected when disabled")
	}

	enabled := post(t, newTestServer(t, &fakeQuery{}, Options{IntrospectionEnabled: true}), introspection)
	if len(enabled.Errors) != 0 {
		t.Errorf("introspection should be allowed when enabled: %+v", enabled.Errors)
	}
}

// TestDepthLimit は、フラグメントを展開した深さが上限を超えるクエリは、深さと上限を示すエラーになりリゾルバが呼び出されないこと、
// 上限以内の深さのクエリは実行できることを検証する。
func TestDepthLimit(t *testing.T) {
	query := &fakeQuery{}
	server := newTestServer(t, query, Options{MaxQueryDepth: 4})

	// publishedPortfolio > layout > sections > photo > photoNo で深さ5
	res := post(t, server, `
		fragment SectionPhoto on FeatureSection { photo { photoNo } }
		{ publishedPortfolio(accountId: "owner123", portfolioNo: 1) {
			layout { ... on FeatureLayout { sections { ...SectionPhoto } } }
		} }`)

	if len(res.Errors) == 0 || !strings.Contains(res.Errors[0].Message, "query depth 5 exceeds the limit of 4") {
		t.Errorf("errors = %+v", res.Errors)
	}
	if query.calls != 0 {
		t.Error("resolver should not be called when depth limit is exceeded")
	}

	// 上限ちょうどの深さは実行できる
	ok := post(t, server, `{ publishedPortfolio(accountId: "owner123", portfolioNo: 1) {
		layout { ... on GridLayout { rows { pattern } } } } }`)
	if len(ok.Errors) != 0 {
		t.Errorf("depth 3 query should succeed: %+v", ok.Errors)
	}
}

// TestComplexityLimit は、複雑度が上限を超えるクエリは複雑度のエラーになり、リゾルバが呼び出されないことを検証する。
func TestComplexityLimit(t *testing.T) {
	query := &fakeQuery{}
	server := newTestServer(t, query, Options{MaxQueryComplexity: 3})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolios { portfolioNo theme description template } } }`)

	if len(res.Errors) == 0 || !strings.Contains(res.Errors[0].Message, "complexity") {
		t.Errorf("errors = %+v", res.Errors)
	}
	if query.calls != 0 {
		t.Error("resolver should not be called when complexity limit is exceeded")
	}
}

// TestInternalErrorIsHidden は、リゾルバで内部エラー（DB の接続エラー等）が発生した場合に、
// エラーの詳細（接続先等）を返さず、固定の文言 "internal server error" だけを返すことを検証する。
func TestInternalErrorIsHidden(t *testing.T) {
	server := newTestServer(t, &fakeQuery{err: errors.New("dial tcp 10.0.0.1:5432: connection refused")}, Options{})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolios { portfolioNo } } }`)

	if len(res.Errors) != 1 || res.Errors[0].Message != "internal server error" {
		t.Errorf("errors = %+v, want only the generic message", res.Errors)
	}
}

// TestRequestBodyLimit は、リクエストボディが上限を超える場合に、Content-Length で申告していれば 413 になり、
// 申告していない（chunked）場合も読み込みを上限で打ち切ってエラーを返すこと、どちらもリゾルバが呼び出されないことを検証する。
func TestRequestBodyLimit(t *testing.T) {
	query := &fakeQuery{}
	server := newTestServer(t, query, Options{})
	body := `{"query":"{ publishedPortfolios(accountId: \"owner123\") { portfolios { portfolioNo } } }","variables":{"pad":"` +
		strings.Repeat("a", maxRequestBodyBytes) + `"}}`

	// 申告サイズ（Content-Length）が上限を超える場合は 413
	res, err := http.Post(server.URL+"/graphql", "application/json", strings.NewReader(body))
	if err != nil {
		t.Fatal(err)
	}
	res.Body.Close()
	if res.StatusCode != http.StatusRequestEntityTooLarge {
		t.Errorf("status = %d, want 413", res.StatusCode)
	}

	// 申告サイズを省略した（chunked）場合も、上限で読み込みを打ち切り実行しない
	req, _ := http.NewRequest(http.MethodPost, server.URL+"/graphql", io.MultiReader(strings.NewReader(body)))
	req.Header.Set("Content-Type", "application/json")
	req.ContentLength = -1
	chunked, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatal(err)
	}
	defer chunked.Body.Close()
	var decoded gqlResponse
	_ = json.NewDecoder(chunked.Body).Decode(&decoded)
	if len(decoded.Errors) == 0 || !strings.Contains(decoded.Errors[0].Message, "request body too large") {
		t.Errorf("errors = %+v", decoded.Errors)
	}
	if query.calls != 0 {
		t.Error("resolver should not be called for oversized requests")
	}
}

// TestHealthz は、/healthz が依存先（DB）に疎通できる場合は 200、できない場合は 503 を返すことを検証する。
func TestHealthz(t *testing.T) {
	handler := NewGraphQLHandler(&graph.Resolver{PortfolioQuery: &fakeQuery{}}, Options{
		MaxQueryComplexity: 300, MaxQueryDepth: 8, Logger: slog.New(slog.NewTextHandler(io.Discard, nil)),
	})
	for health, want := range map[fakeHealth]int{
		{}:                        http.StatusOK,                 // 疎通できる
		{err: errors.New("down")}: http.StatusServiceUnavailable, // 疎通できない
	} {
		rec := httptest.NewRecorder()
		NewMux(handler, health).ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/healthz", nil))
		if rec.Code != want {
			t.Errorf("healthz status = %d, want %d", rec.Code, want)
		}
	}
}
