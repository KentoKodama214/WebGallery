#!/usr/bin/env python3
"""Goのカバレッジプロファイル（go test -coverprofile）からカバレッジ集計表（Markdown）を生成する。

引数は ``ラベル=プロファイルパス`` の形式で1つ以上指定する。パスをカンマ区切りで
複数指定すると、それらを統合したカバレッジを1行として集計する（単体＋結合の集約用）。
指定されたプロファイルが存在しない・対象が0件の場合はセルに ``-`` を出力する
（該当テストが実行されなかったケースを許容するため）。

``-coverpkg`` 指定時は、テストバイナリ（パッケージ）ごとに同じブロックが重複して
出力されるため、同一ブロックはいずれかでカバーされていればカバー済みとして統合する。

関数カバレッジは、統合したプロファイルを ``go tool cover -func`` に渡して算出する
（ソースの解析が必要なため、``--module-dir`` のGoモジュールで実行する）。
1文でも実行された関数をカバー済みとみなす。

生成したMarkdownは標準出力に書き出す。PRへの単一コメント投稿や
GitHub Actionsのジョブサマリーへ、そのままリダイレクトして利用できる。
"""
import argparse
import os
import subprocess
import sys
import tempfile


def read_module_path(module_dir):
    """``go.mod`` の ``module`` ディレクティブからモジュールパスを返す。"""
    with open(os.path.join(module_dir, "go.mod"), encoding="utf-8") as f:
        for line in f:
            if line.startswith("module "):
                return line.split()[1]
    raise ValueError(f"go.mod に module ディレクティブがありません: {module_dir}")


def parse_profiles(paths, module_path, excludes):
    """プロファイルを読み込み、``{ブロック: (文数, カバー済みか)}`` に統合して返す。

    ``excludes`` にはモジュールルートからの相対パスを指定し、該当ファイルのブロックを除外する
    （gqlgenの生成コード等、テスト対象外のコードを分母から外すため）。
    """
    blocks = {}
    prefix = module_path + "/"
    for path in paths:
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("mode:"):
                    continue
                # 形式: <ファイル>:<開始行>.<開始列>,<終了行>.<終了列> <文数> <実行回数>
                block, num_stmt, count = line.rsplit(" ", 2)
                file_path = block.rsplit(":", 1)[0]
                if file_path.removeprefix(prefix) in excludes:
                    continue
                covered = int(count) > 0
                _, prev_covered = blocks.get(block, (0, False))
                blocks[block] = (int(num_stmt), prev_covered or covered)
    return blocks


def statement_counter(blocks):
    """ステートメントの ``(covered, total)`` を返す。"""
    total = sum(num_stmt for num_stmt, _ in blocks.values())
    covered = sum(num_stmt for num_stmt, is_covered in blocks.values() if is_covered)
    return covered, total


def function_counter(blocks, module_dir):
    """``go tool cover -func`` で関数の ``(covered, total)`` を返す。実行できない場合は ``None``。"""
    with tempfile.NamedTemporaryFile("w", suffix=".out", delete=False, encoding="utf-8") as f:
        f.write("mode: set\n")
        for block, (num_stmt, is_covered) in blocks.items():
            f.write(f"{block} {num_stmt} {1 if is_covered else 0}\n")
        merged_path = f.name
    try:
        result = subprocess.run(
            ["go", "tool", "cover", f"-func={merged_path}"],
            cwd=module_dir,
            capture_output=True,
            text=True,
            check=True,
        )
    except (OSError, subprocess.CalledProcessError) as e:
        print(f"go tool cover -func の実行に失敗しました: {e}", file=sys.stderr)
        return None
    finally:
        os.remove(merged_path)

    covered = total = 0
    # 形式: <ファイル>:<行>:<TAB><関数名><TAB...><カバレッジ率>%（最終行は total: の合計）
    for line in result.stdout.splitlines():
        if not line or line.startswith("total:"):
            continue
        total += 1
        if float(line.split()[-1].rstrip("%")) > 0:
            covered += 1
    return covered, total


def format_cell(counter):
    """``(covered, total)`` を ``85.3% (100/117)`` 形式の文字列に整形する。"""
    if counter is None:
        return "-"
    covered, total = counter
    if total == 0:
        return "-"
    return f"{covered / total * 100:.1f}% ({covered}/{total})"


def build_table(entries, module_dir, excludes):
    """``[(ラベル, [プロファイルパス, ...]), ...]`` からMarkdownの表を組み立てる。"""
    module_path = read_module_path(module_dir)
    lines = [
        "## バックエンドカバレッジ: portfolio-api (Go)",
        "",
        "| テスト種別 | ステートメント | 関数 |",
        "|---|---|---|",
    ]
    rows = []
    for label, paths in entries:
        existing = [path for path in paths if os.path.isfile(path)]
        rows.append((label, parse_profiles(existing, module_path, excludes) if existing else {}))
    # 結合テストのようにテスト対象のパッケージが限られると、テストが依存しないパッケージの
    # ブロックがプロファイルに出力されない。全行の分母をモジュール全体に揃えるため、
    # いずれかのプロファイルに含まれるブロックの和集合を共通の分母とし、出現しないブロックは未カバーとする
    universe = {}
    for _, blocks in rows:
        for block, (num_stmt, _) in blocks.items():
            universe[block] = (num_stmt, False)
    for label, blocks in rows:
        if blocks:
            blocks = {**universe, **blocks}
            cells = [format_cell(statement_counter(blocks)), format_cell(function_counter(blocks, module_dir))]
        else:
            cells = ["-", "-"]
        lines.append(f"| {label} | " + " | ".join(cells) + " |")
    lines += [
        "",
        "各セルは `カバレッジ率 (カバー済み/全体)`。gqlgenの生成コードは集計から除外している。",
    ]
    return "\n".join(lines)


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--module-dir", required=True, help="Goモジュールのディレクトリ（go.mod の配置場所）")
    parser.add_argument(
        "--exclude",
        action="append",
        default=[],
        help="集計から除外するファイル（モジュールルートからの相対パス）。複数指定可",
    )
    parser.add_argument("entries", nargs="+", help="'ラベル=プロファイルパス[,プロファイルパス...]'")
    args = parser.parse_args(argv)

    entries = []
    for arg in args.entries:
        label, sep, paths = arg.partition("=")
        if not sep:
            print(f"不正な引数（'ラベル=パス' 形式で指定してください）: {arg}", file=sys.stderr)
            return 1
        entries.append((label, paths.split(",")))
    print(build_table(entries, args.module_dir, set(args.exclude)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
