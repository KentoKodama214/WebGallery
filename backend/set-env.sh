#!/bin/bash

# ローカル開発用の環境変数をbashrcまたはzshrcに書き込むスクリプト
# 使用方法: bash set-env.sh

# 現在のシェルに応じてrcファイルを判定
CURRENT_SHELL=$(basename "$SHELL")
if [ "$CURRENT_SHELL" = "zsh" ]; then
  RC_FILE="$HOME/.zshrc"
elif [ "$CURRENT_SHELL" = "bash" ]; then
  RC_FILE="$HOME/.bashrc"
else
  echo "未対応のシェルです: $CURRENT_SHELL"
  exit 1
fi

echo "環境変数を $RC_FILE に書き込みます"
echo

# データベース接続情報
read -p "DB_URL [jdbc:postgresql://localhost:5432/web_gallery]: " DB_URL
DB_URL=${DB_URL:-jdbc:postgresql://localhost:5432/web_gallery}

read -p "DB_USERNAME [postgres]: " DB_USERNAME
DB_USERNAME=${DB_USERNAME:-postgres}

read -sp "DB_PASSWORD [postgres]: " DB_PASSWORD
echo
DB_PASSWORD=${DB_PASSWORD:-postgres}

# 写真関連設定
read -p "MINI_USER_UPPER_LIMIT [10]: " MINI_USER_UPPER_LIMIT
MINI_USER_UPPER_LIMIT=${MINI_USER_UPPER_LIMIT:-10}

read -p "NORMAL_USER_UPPER_LIMIT [1000]: " NORMAL_USER_UPPER_LIMIT
NORMAL_USER_UPPER_LIMIT=${NORMAL_USER_UPPER_LIMIT:-1000}

# 画像ストレージ（S3 / MinIO）は local プロファイルで docker-compose の MinIO を指す
# デフォルト値を持つため、通常はここで設定不要（別のバケット等を使う場合のみ APP_S3_* を export する）

# JWT設定
# 既知の固定文字列をデフォルトにするとJWT偽造のリスクがあるため、未入力時はランダム生成する
DEFAULT_JWT_SECRET=$(openssl rand -base64 48 2>/dev/null || head -c 48 /dev/urandom | base64)
read -p "JWT_SECRET [ランダム生成]: " JWT_SECRET
JWT_SECRET=${JWT_SECRET:-$DEFAULT_JWT_SECRET}

# rcファイルに書き込み
{
  echo ""
  echo "# WebGallery ローカル開発用環境変数"
  echo "export DB_URL=\"$DB_URL\""
  echo "export DB_USERNAME=\"$DB_USERNAME\""
  echo "export DB_PASSWORD=\"$DB_PASSWORD\""
  echo "export MINI_USER_UPPER_LIMIT=\"$MINI_USER_UPPER_LIMIT\""
  echo "export NORMAL_USER_UPPER_LIMIT=\"$NORMAL_USER_UPPER_LIMIT\""
  echo "export JWT_SECRET=\"$JWT_SECRET\""
} >> "$RC_FILE"

echo
echo "$RC_FILE に環境変数を書き込みました"
echo "反映するには以下を実行してください:"
echo "  source $RC_FILE"
