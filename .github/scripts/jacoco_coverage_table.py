#!/usr/bin/env python3
"""JaCoCoのXMLレポートからカバレッジ集計表（Markdown）を生成する。

引数は ``ラベル=XMLパス`` の形式で1つ以上指定する。
指定されたXMLが存在しない・対象が0件の場合はセルに ``-`` を出力する
（該当テストが実行されなかったケースを許容するため）。

生成したMarkdownは標準出力に書き出す。PRへの単一コメント投稿や
GitHub Actionsのジョブサマリーへ、そのままリダイレクトして利用できる。
"""
import os
import sys
import xml.etree.ElementTree as ET

# 表示するカウンター種別（JaCoCoの ``type`` 属性 -> 表示ラベル）
COUNTERS = [
    ("INSTRUCTION", "命令"),
    ("BRANCH", "分岐"),
    ("LINE", "行"),
    ("METHOD", "メソッド"),
    ("CLASS", "クラス"),
]


def parse_counters(path):
    """レポート直下のカウンター集計を ``{type: (covered, missed)}`` で返す。"""
    root = ET.parse(path).getroot()
    counters = {}
    for counter in root.findall("counter"):
        covered = int(counter.get("covered", "0"))
        missed = int(counter.get("missed", "0"))
        counters[counter.get("type")] = (covered, missed)
    return counters


def format_cell(counter):
    """``(covered, missed)`` を ``85.3% (100/117)`` 形式の文字列に整形する。"""
    if counter is None:
        return "-"
    covered, missed = counter
    total = covered + missed
    if total == 0:
        return "-"
    return f"{covered / total * 100:.1f}% ({covered}/{total})"


def build_table(entries):
    """``[(ラベル, XMLパス), ...]`` からMarkdownの表を組み立てる。"""
    header = "| テスト種別 | " + " | ".join(label for _, label in COUNTERS) + " |"
    separator = "|" + "---|" * (len(COUNTERS) + 1)

    lines = ["## バックエンドカバレッジ (JaCoCo)", "", header, separator]
    for label, path in entries:
        counters = parse_counters(path) if os.path.isfile(path) else {}
        cells = [format_cell(counters.get(ctype)) for ctype, _ in COUNTERS]
        lines.append(f"| {label} | " + " | ".join(cells) + " |")
    lines += [
        "",
        "各セルは `カバレッジ率 (カバー済み/全体)`。命令=Instruction、分岐=Branch。",
    ]
    return "\n".join(lines)


def main(argv):
    if not argv:
        print("usage: jacoco_coverage_table.py 'ラベル=path.xml' ...", file=sys.stderr)
        return 1
    entries = []
    for arg in argv:
        label, sep, path = arg.partition("=")
        if not sep:
            print(f"不正な引数（'ラベル=パス' 形式で指定してください）: {arg}", file=sys.stderr)
            return 1
        entries.append((label, path))
    print(build_table(entries))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
