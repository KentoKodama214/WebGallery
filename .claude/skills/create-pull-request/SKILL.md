---
name: create-pull-request
description: プルリクエストを作成するスキル。「PRを作成して」「プルリクを作って」「/create-pull-request」などの指示があったときに使用する。テンプレート（.github/PULL_REQUEST_TEMPLATE.md）に沿って記載する
---

# Create Pull Request

## 1. 事前準備と情報収集
   - `git branch --show-current` で現在のブランチ名を取得する。
   - `main` の場合は PR を作成できないため警告して終了する。
   - `git log origin/main..HEAD --oneline` で差分コミット一覧を取得する。
   - `git diff origin/main...HEAD` でコードの全差分を取得する。
   - リポジトリ内に `.github/PULL_REQUEST_TEMPLATE.md` があるか確認し、あれば読み込む。

## 2. タイトルと本文の生成
   - タイトル: Conventional Commitsに基づいた簡潔な1行（例: `feat: YOLOv8による背景判定機能の追加`）
   - 本文: テンプレートの構造に従って記述

## 3. 実行
   - `pwd` コマンドで作業ディレクトリが `WebGallery` であることを確認する
   - `git status` で未プッシュのコミットがあるか確認し、必要に応じて `/commit-push` のスキルを実行する。
   - 生成した PR タイトルと本文、および実行する `gh pr create` コマンドをユーザーに提示し、実行してよいか確認する。
   - 承認が得られたら `gh pr create --title "..." --body "..."` を実行する。
   - 作成された PR の URL を出力する。
