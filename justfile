# 初期セットアップ（mise + corepack + pnpm install）
setup:
    mise install
    mise exec -- corepack enable pnpm
    mise reshim
    cd frontend && mise exec -- pnpm install

# 開発サーバー起動
dev:
    cd frontend && mise exec -- pnpm dev

# ビルド
build:
    cd frontend && mise exec -- pnpm build

# lint
lint:
    cd frontend && mise exec -- pnpm lint
