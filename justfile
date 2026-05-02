# 初期セットアップ（mise + corepack + pnpm install）
setup:
    mise install
    corepack enable pnpm
    cd frontend && pnpm install

# 開発サーバー起動
dev:
    cd frontend && pnpm dev

# ビルド
build:
    cd frontend && pnpm build

# lint
lint:
    cd frontend && pnpm lint
