// Package server は GraphQL の HTTP ハンドラを組み立てる。
//
// このサーバーは Next.js のサーバーからだけ呼び出す前提で、ブラウザには公開しない（CORS は設定しない）。
// それでも不特定のクエリを受け付ける GraphQL の性質上、クエリの複雑度・深さの上限、本番での
// イントロスペクションの無効化、リクエストサイズの上限、内部エラーの詳細を返さないことで攻撃面を抑える。
package server

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"time"

	"github.com/99designs/gqlgen/graphql"
	"github.com/99designs/gqlgen/graphql/handler"
	"github.com/99designs/gqlgen/graphql/handler/extension"
	"github.com/99designs/gqlgen/graphql/handler/lru"
	"github.com/99designs/gqlgen/graphql/handler/transport"
	"github.com/vektah/gqlparser/v2/ast"
	"github.com/vektah/gqlparser/v2/gqlerror"

	"github.com/KentoKodama214/WebGallery/portfolio-api/graph"
)

// maxRequestBodyBytes は GraphQL リクエストボディの上限（クエリ文字列と変数だけのため小さくてよい）。
const maxRequestBodyBytes = 64 * 1024

// internalErrorMessage は内部エラー（DB・ストレージの障害等）をクライアントへ返すときの固定文言。
const internalErrorMessage = "internal server error"

// Options は GraphQL ハンドラの設定。
type Options struct {
	MaxQueryComplexity   int
	MaxQueryDepth        int
	IntrospectionEnabled bool
	Logger               *slog.Logger
}

// HealthChecker は依存先（DB）の疎通を確認する。
type HealthChecker interface {
	Ping(ctx context.Context) error
}

// NewGraphQLHandler は GraphQL の HTTP ハンドラを生成する。
//
// GET でもクエリを受け付ける（Next.js 側で URL 単位にキャッシュできるようにするため）。
// 書き込み操作（Mutation）を持たないスキーマのため、GET を許可しても状態は変化しない。
func NewGraphQLHandler(resolver *graph.Resolver, opts Options) http.Handler {
	srv := handler.New(graph.NewExecutableSchema(graph.Config{Resolvers: resolver}))
	srv.AddTransport(transport.GET{})
	srv.AddTransport(transport.POST{})
	srv.SetQueryCache(lru.New[*ast.QueryDocument](1000))

	if opts.IntrospectionEnabled {
		srv.Use(extension.Introspection{})
	}
	srv.Use(extension.FixedComplexityLimit(opts.MaxQueryComplexity))
	srv.Use(DepthLimit{Max: opts.MaxQueryDepth})

	logger := opts.Logger
	srv.SetErrorPresenter(func(ctx context.Context, err error) *gqlerror.Error {
		var gqlErr *gqlerror.Error
		// クエリの構文・検証エラー（gqlparser が生成し、元のエラーを持たないもの）はそのまま返す
		if errors.As(err, &gqlErr) && gqlErr.Err == nil {
			return graphql.DefaultErrorPresenter(ctx, err)
		}
		// リゾルバの内部エラーは詳細（SQL・接続先等）を返さず、ログにだけ残す
		logger.ErrorContext(ctx, "resolver error", "path", graphql.GetPath(ctx).String(), "error", err)
		return &gqlerror.Error{Message: internalErrorMessage, Path: graphql.GetPath(ctx)}
	})
	srv.SetRecoverFunc(func(ctx context.Context, recovered any) error {
		logger.ErrorContext(ctx, "panic in resolver", "panic", recovered)
		return errors.New(internalErrorMessage)
	})

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// 申告サイズが上限を超えるリクエストは読み込まずに 413 を返す。申告を省略・詐称した
		// リクエストも、読み込み時に上限で打ち切る（gqlgen が読み込みエラーとして実行せずに返す）
		if r.ContentLength > maxRequestBodyBytes {
			http.Error(w, "request body too large", http.StatusRequestEntityTooLarge)
			return
		}
		r.Body = http.MaxBytesReader(w, r.Body, maxRequestBodyBytes)
		srv.ServeHTTP(w, r)
	})
}

// NewMux は GraphQL（/graphql）とヘルスチェック（/healthz）のルーティングを組み立てる。
func NewMux(graphqlHandler http.Handler, health HealthChecker) *http.ServeMux {
	mux := http.NewServeMux()
	mux.Handle("GET /graphql", graphqlHandler)
	mux.Handle("POST /graphql", graphqlHandler)
	mux.HandleFunc("GET /healthz", func(w http.ResponseWriter, r *http.Request) {
		ctx, cancel := context.WithTimeout(r.Context(), 2*time.Second)
		defer cancel()
		if err := health.Ping(ctx); err != nil {
			http.Error(w, "unhealthy", http.StatusServiceUnavailable)
			return
		}
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})
	return mux
}
