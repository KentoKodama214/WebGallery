#!/bin/bash
#
# レイヤードアーキテクチャ違反チェックスクリプト
#
# 許可される依存方向: Controller -> Service -> Repository -> Mapper
# 以下を検出してエラーとする:
#   1. Controller -> Repository（スキップ違反）
#   2. Service -> Controller（逆方向）
#   3. Repository -> Controller（逆方向）
#   4. Repository -> Service（逆方向）
#   5. Controller同士の呼び出し（同レイヤー）
#   6. Service同士の呼び出し（同レイヤー）
#   7. Repository同士の呼び出し（同レイヤー）
#

set -euo pipefail

BACKEND_DIR="${1:-backend}"
BASE_DIR="${BACKEND_DIR}/src/main/java/com/web/gallery"
CONTROLLER_DIR="${BASE_DIR}/controller"
SERVICE_IMPL_DIR="${BASE_DIR}/service/impl"
REPOSITORY_IMPL_DIR="${BASE_DIR}/repository/impl"

VIOLATIONS=0

# 違反を報告する関数
report_violation() {
  local rule="$1"
  local file="$2"
  local line="$3"
  echo "  [違反] ${rule}"
  echo "    ファイル: ${file}"
  echo "    該当行: ${line}"
  echo ""
  VIOLATIONS=$((VIOLATIONS + 1))
}

echo "===== レイヤードアーキテクチャチェック ====="
echo ""

# ----------------------------------------
# ルール1: Controller -> Repository 禁止
# ----------------------------------------
echo "--- ルール1: Controller -> Repository 禁止 ---"
while IFS= read -r file; do
  while IFS= read -r line; do
    report_violation "Controller から Repository を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.repository" "$file" 2>/dev/null || true)
done < <(find "${CONTROLLER_DIR}" -maxdepth 1 -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール2: Service -> Controller 禁止
# ----------------------------------------
echo "--- ルール2: Service -> Controller 禁止 ---"
while IFS= read -r file; do
  while IFS= read -r line; do
    report_violation "Service から Controller を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.controller" "$file" 2>/dev/null || true)
done < <(find "${BASE_DIR}/service" -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール3: Repository -> Controller 禁止
# ----------------------------------------
echo "--- ルール3: Repository -> Controller 禁止 ---"
while IFS= read -r file; do
  while IFS= read -r line; do
    report_violation "Repository から Controller を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.controller" "$file" 2>/dev/null || true)
done < <(find "${BASE_DIR}/repository" -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール4: Repository -> Service 禁止
# ----------------------------------------
echo "--- ルール4: Repository -> Service 禁止 ---"
while IFS= read -r file; do
  while IFS= read -r line; do
    report_violation "Repository から Service を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.service" "$file" 2>/dev/null || true)
done < <(find "${BASE_DIR}/repository" -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール5: Controller同士の呼び出し禁止
# Controller同士は同パッケージのためimportではなくフィールド宣言・extends句で判定
# ----------------------------------------
echo "--- ルール5: Controller同士の呼び出し禁止 ---"
while IFS= read -r file; do
  # Controller/RestController型のフィールド注入を検出
  while IFS= read -r line; do
    report_violation "Controller から他の Controller を注入しています" "$file" "$line"
  done < <(grep -n "\(Controller\|RestController\) " "$file" | grep -i "private\|protected\|public" | grep -v "class " 2>/dev/null || true)

  # extends句でController系クラスの継承を検出
  while IFS= read -r line; do
    report_violation "Controller が他の Controller を継承しています" "$file" "$line"
  done < <(grep -n "extends.*\(Controller\|RestController\)" "$file" 2>/dev/null || true)
done < <(find "${CONTROLLER_DIR}" -maxdepth 1 -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール6: Service同士の呼び出し禁止
# - service.impl パッケージ内の他Implクラスのimportを検出
# - 自インタフェース以外のserviceパッケージからのimportを検出
# ----------------------------------------
echo "--- ルール6: Service同士の呼び出し禁止 ---"
while IFS= read -r file; do
  filename=$(basename "$file" .java)

  # 自インタフェース名を推定（例: PhotoServiceImpl -> PhotoService）
  own_interface="${filename%Impl}"

  # 他のServiceImplクラスのimportを検出
  while IFS= read -r line; do
    report_violation "Service から他の ServiceImpl を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.service\.impl\." "$file" 2>/dev/null || true)

  # 自インタフェース以外のServiceインタフェースのimportを検出
  while IFS= read -r line; do
    report_violation "Service から他の Service を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.service\." "$file" | grep -v "import com\.web\.gallery\.service\.impl\." | grep -v "import com\.web\.gallery\.service\.${own_interface};" 2>/dev/null || true)
done < <(find "${SERVICE_IMPL_DIR}" -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# ルール7: Repository同士の呼び出し禁止
# - repository.impl パッケージ内の他Implクラスのimportを検出
# - 自インタフェース以外のrepositoryパッケージからのimportを検出
# ----------------------------------------
echo "--- ルール7: Repository同士の呼び出し禁止 ---"
while IFS= read -r file; do
  filename=$(basename "$file" .java)

  # 自インタフェース名を推定（例: PhotoMstRepositoryImpl -> PhotoMstRepository）
  own_interface="${filename%Impl}"

  # 他のRepositoryImplクラスのimportを検出
  while IFS= read -r line; do
    report_violation "Repository から他の RepositoryImpl を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.repository\.impl\." "$file" 2>/dev/null || true)

  # 自インタフェース以外のRepositoryインタフェースのimportを検出
  while IFS= read -r line; do
    report_violation "Repository から他の Repository を参照しています" "$file" "$line"
  done < <(grep -n "import com\.web\.gallery\.repository\." "$file" | grep -v "import com\.web\.gallery\.repository\.impl\." | grep -v "import com\.web\.gallery\.repository\.${own_interface};" 2>/dev/null || true)
done < <(find "${REPOSITORY_IMPL_DIR}" -name "*.java" -type f 2>/dev/null)

# ----------------------------------------
# 結果出力
# ----------------------------------------
echo "====================================="
if [ "${VIOLATIONS}" -eq 0 ]; then
  echo "チェック結果: 違反なし"
  exit 0
else
  echo "チェック結果: ${VIOLATIONS} 件の違反が見つかりました"
  exit 1
fi
