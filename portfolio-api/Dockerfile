# portfolio-api（公開ページ向けのポートフォリオ表示API）の本番用イメージ
# ビルド: docker build -t portfolio-api ./portfolio-api
FROM golang:1.27.2 AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
# 静的リンクしたバイナリにし、シェル・パッケージマネージャを含まない distroless イメージで動かす
RUN CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o /out/portfolio-api ./cmd/server

FROM gcr.io/distroless/static-debian12:nonroot
COPY --from=build /out/portfolio-api /portfolio-api
EXPOSE 8081
USER nonroot:nonroot
ENTRYPOINT ["/portfolio-api"]
