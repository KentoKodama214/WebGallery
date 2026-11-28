// Command server は、公開ページ向けのポートフォリオ表示API（GraphQL）を起動する。
package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/KentoKodama214/WebGallery/portfolio-api/graph"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/config"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/infrastructure/postgres"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/infrastructure/storage"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/server"
	"github.com/KentoKodama214/WebGallery/portfolio-api/internal/usecase"
)

// main はサーバーを起動し、異常終了した場合はエラーをログに出力して終了コード 1 で終了する。
func main() {
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	if err := run(logger); err != nil {
		logger.Error("server stopped with error", "error", err)
		os.Exit(1)
	}
}

// run は設定の読み込み・依存関係の組み立てを行って HTTP サーバーを起動し、停止するまで待つ。
//
// SIGINT・SIGTERM を受け取った場合は、処理中のリクエストを最大10秒待ってから停止する。
//
// 引数:
//   - logger: 起動・停止を記録し、GraphQL の内部エラーの記録にも使うロガー
//
// 戻り値:
//   - 設定の読み込み・DB の接続設定・サーバーの起動・停止に失敗した場合はエラー。正常に停止した場合は nil
func run(logger *slog.Logger) error {
	cfg, err := config.Load()
	if err != nil {
		return err
	}

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	pool, err := pgxpool.New(ctx, cfg.DatabaseURL)
	if err != nil {
		return err
	}
	defer pool.Close()

	repository := postgres.NewPortfolioRepository(pool)
	query := usecase.NewPortfolioQuery(repository, storage.NewPresigner(cfg.Storage))
	graphqlHandler := server.NewGraphQLHandler(&graph.Resolver{PortfolioQuery: query}, server.Options{
		MaxQueryComplexity:   cfg.MaxQueryComplexity,
		MaxQueryDepth:        cfg.MaxQueryDepth,
		IntrospectionEnabled: cfg.IntrospectionEnabled(),
		Logger:               logger,
	})

	httpServer := &http.Server{
		Addr:              ":" + cfg.Port,
		Handler:           server.NewMux(graphqlHandler, pool),
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       10 * time.Second,
		WriteTimeout:      30 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	serverErr := make(chan error, 1)
	go func() {
		logger.Info("portfolio-api started", "port", cfg.Port, "env", cfg.Env)
		serverErr <- httpServer.ListenAndServe()
	}()

	select {
	case err := <-serverErr:
		if !errors.Is(err, http.ErrServerClosed) {
			return err
		}
		return nil
	case <-ctx.Done():
		logger.Info("shutting down portfolio-api")
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		return httpServer.Shutdown(shutdownCtx)
	}
}
