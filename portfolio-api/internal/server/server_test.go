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

type fakeQuery struct {
	summaries []domain.PortfolioSummary
	published map[int64]*usecase.PublishedPortfolio
	err       error
	calls     int
}

func (f *fakeQuery) ListPublishedPortfolios(context.Context, string) ([]domain.PortfolioSummary, error) {
	f.calls++
	return f.summaries, f.err
}

func (f *fakeQuery) GetPublishedPortfolio(_ context.Context, _ string, portfolioNo int64) (*usecase.PublishedPortfolio, error) {
	f.calls++
	if f.err != nil {
		return nil, f.err
	}
	return f.published[portfolioNo], nil
}

type fakeHealth struct{ err error }

func (f fakeHealth) Ping(context.Context) error { return f.err }

var updatedAt = time.Date(2026, 10, 1, 9, 0, 0, 0, time.UTC)

func signedPhoto(no int64) domain.Photo {
	return domain.Photo{PhotoNo: no, URL: "https://signed/" + string(rune('0'+no)), JapaneseTitle: "金閣寺", Direction: domain.DirectionSquare}
}

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

type gqlResponse struct {
	Data   map[string]any `json:"data"`
	Errors []struct {
		Message string `json:"message"`
	} `json:"errors"`
}

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

func toJSON(v any) string {
	b, _ := json.Marshal(v)
	return string(b)
}

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

func TestPublishedPortfolio_NullWhenNotPublished(t *testing.T) {
	server := newTestServer(t, &fakeQuery{}, Options{})

	res := post(t, server, `{ publishedPortfolio(accountId: "owner123", portfolioNo: 9) { portfolioNo } }`)

	if len(res.Errors) != 0 || res.Data["publishedPortfolio"] != nil {
		t.Errorf("response = %+v, want null without errors", res)
	}
}

func TestPublishedPortfolios(t *testing.T) {
	cover := signedPhoto(3)
	server := newTestServer(t, &fakeQuery{summaries: []domain.PortfolioSummary{
		{PortfolioNo: 2, Theme: "光と影", Template: domain.TemplateGrid, UpdatedAt: updatedAt, PhotoCount: 4, CoverPhoto: &cover, Tags: []domain.Tag{}},
		{PortfolioNo: 1, Theme: "日本の自然", Template: domain.TemplateFeature, UpdatedAt: updatedAt, Tags: []domain.Tag{}},
	}}, Options{})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolioNo template photoCount updatedAt coverPhoto { photoNo url } } }`)

	want := `[{"coverPhoto":{"photoNo":3,"url":"https://signed/3"},"photoCount":4,"portfolioNo":2,"template":"GRID","updatedAt":"2026-10-01T09:00:00Z"},` +
		`{"coverPhoto":null,"photoCount":0,"portfolioNo":1,"template":"FEATURE","updatedAt":"2026-10-01T09:00:00Z"}]`
	if got := toJSON(res.Data["publishedPortfolios"]); got != want {
		t.Errorf("publishedPortfolios = %s\nwant %s", got, want)
	}
}

func TestGetTransport(t *testing.T) {
	server := newTestServer(t, &fakeQuery{summaries: []domain.PortfolioSummary{}}, Options{})

	q := url.Values{"query": {`{ publishedPortfolios(accountId: "owner123") { portfolioNo } }`}}
	res, err := http.Get(server.URL + "/graphql?" + q.Encode())
	if err != nil {
		t.Fatal(err)
	}
	defer res.Body.Close()
	body, _ := io.ReadAll(res.Body)
	if res.StatusCode != http.StatusOK || string(body) != `{"data":{"publishedPortfolios":[]}}` {
		t.Errorf("status=%d body=%s", res.StatusCode, body)
	}
}

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

func TestComplexityLimit(t *testing.T) {
	query := &fakeQuery{}
	server := newTestServer(t, query, Options{MaxQueryComplexity: 3})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolioNo theme description template } }`)

	if len(res.Errors) == 0 || !strings.Contains(res.Errors[0].Message, "complexity") {
		t.Errorf("errors = %+v", res.Errors)
	}
	if query.calls != 0 {
		t.Error("resolver should not be called when complexity limit is exceeded")
	}
}

func TestInternalErrorIsHidden(t *testing.T) {
	server := newTestServer(t, &fakeQuery{err: errors.New("dial tcp 10.0.0.1:5432: connection refused")}, Options{})

	res := post(t, server, `{ publishedPortfolios(accountId: "owner123") { portfolioNo } }`)

	if len(res.Errors) != 1 || res.Errors[0].Message != "internal server error" {
		t.Errorf("errors = %+v, want only the generic message", res.Errors)
	}
}

func TestRequestBodyLimit(t *testing.T) {
	query := &fakeQuery{}
	server := newTestServer(t, query, Options{})
	body := `{"query":"{ publishedPortfolios(accountId: \"owner123\") { portfolioNo } }","variables":{"pad":"` +
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

func TestHealthz(t *testing.T) {
	handler := NewGraphQLHandler(&graph.Resolver{PortfolioQuery: &fakeQuery{}}, Options{
		MaxQueryComplexity: 300, MaxQueryDepth: 8, Logger: slog.New(slog.NewTextHandler(io.Discard, nil)),
	})
	for health, want := range map[fakeHealth]int{
		{}:                        http.StatusOK,
		{err: errors.New("down")}: http.StatusServiceUnavailable,
	} {
		rec := httptest.NewRecorder()
		NewMux(handler, health).ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/healthz", nil))
		if rec.Code != want {
			t.Errorf("healthz status = %d, want %d", rec.Code, want)
		}
	}
}
