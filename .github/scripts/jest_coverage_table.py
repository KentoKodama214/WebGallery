#!/usr/bin/env python3
"""JestのIstanbulカバレッジサマリー（coverage-summary.json）から
カバレッジ集計表（Markdown）を生成する。

引数にはcoverage-summary.jsonのパスを1つ指定する。
ファイルが存在しない・totalが取得できない場合はセルに ``-`` を出力する
（単体テストが実行されなかったケースを許容するため）。

生成したMarkdownは標準出力に書き出す。PRへの単一コメント投稿や
GitHub Actionsのジョブサマリーへ、そのままリダイレクトして利用できる。
"""
import json
import os
import sys

# 表示するカウンター種別（coverage-summary.jsonのキー -> 表示ラベル）
COUNTERS = [
    ("statements", "ステートメント"),
    ("branches", "分岐"),
    ("functions", "関数"),
    ("lines", "行"),
]


def load_total(path):
    """``coverage-summary.json`` の ``total`` を ``{type: (covered, total)}`` で返す。"""
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    total = data.get("total", {})
    counters = {}
    for ctype, _ in COUNTERS:
        entry = total.get(ctype)
        if entry is None:
            continue
        counters[ctype] = (entry.get("covered", 0), entry.get("total", 0))
    return counters


def format_cell(counter):
    """``(covered, total)`` を ``85.3% (100/117)`` 形式の文字列に整形する。"""
    if counter is None:
        return "-"
    covered, total = counter
    if total == 0:
        return "-"
    return f"{covered / total * 100:.1f}% ({covered}/{total})"


def build_table(path):
    """``coverage-summary.json`` からMarkdownの表を組み立てる。"""
    header = "| テスト種別 | " + " | ".join(label for _, label in COUNTERS) + " |"
    separator = "|" + "---|" * (len(COUNTERS) + 1)

    counters = load_total(path) if os.path.isfile(path) else {}
    cells = [format_cell(counters.get(ctype)) for ctype, _ in COUNTERS]
    lines = [
        "## フロントエンドカバレッジ (Jest)",
        "",
        header,
        separator,
        f"| 単体テスト | " + " | ".join(cells) + " |",
        "",
        "各セルは `カバレッジ率 (カバー済み/全体)`。",
    ]
    return "\n".join(lines)


def main(argv):
    if len(argv) != 1:
        print("usage: jest_coverage_table.py coverage-summary.json", file=sys.stderr)
        return 1
    print(build_table(argv[0]))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
