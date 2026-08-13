---
name: commit-push
description: Githubへコミット・プッシュを実行するスキル。「コミットして」「プッシュして」「/commit-push」などの指示があった時に使用する。mainブランチの保護、メッセージ付きのコミットまで行う
---

# Githubコミット&プッシュ

## 1. 事前チェック
   - `git status` および `git diff --staged`（未ステージングがある場合は `git diff`）を確認する。
   - 現在のブランチ名を取得する。`main` または `master` の場合は警告を出し、ブランチ作成を促して処理を停止する。

## 2. コミットメッセージ生成
   - 差分（diff）を解析し、変更理由（Why）と変更内容（What）を把握する。
   - Conventional Commits形式（feat, fix, refactor, docs, test, chore など）でメッセージを作成する。

## 3. ユーザー確認
   - 変更ファイル一覧
   - 現在のブランチ
   - 生成したコミットメッセージ
   上記を提示し、ユーザーにコミット・プッシュを実行してよいか確認する。

## 4. 実行
   - 承認が得られたら `git commit` を実行する。
   - 続いて `git push origin <current-branch>` を実行する。
