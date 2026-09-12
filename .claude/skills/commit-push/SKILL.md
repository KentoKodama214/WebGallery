---
name: commit-push
description: Githubへコミット・プッシュを実行するスキル。「コミットして」「プッシュして」「/commit-push」などの指示があった時に使用する。mainブランチの保護、メッセージ付きのコミットまで行う
---

# Githubコミット&プッシュ

## 1. 事前チェック
   - `git status` および `git diff --staged`（未ステージングがある場合は `git diff`）を確認する。
   - 現在のブランチ名を取得する。`main` または `master` の場合は警告を出し、ブランチ作成を促して処理を停止する。

## 2. ドキュメント整合性チェック
   - 差分に以下のいずれかが含まれる場合、対応するドキュメントも差分に含まれているか確認する。
     - `db/**` の追加・変更 → `doc/database/README.md` / `doc/database/data-dictionary.md`
     - セキュリティ関連（認証・認可、JWT、`annotation/`・`aspect/`等）の変更 → `doc/architecture/security.md`
     - レイヤー構成やアーキテクチャ全体に影響する変更 → `doc/architecture/layered-architecture.md` / `CLAUDE.md`
     - frontendの画面追加・ルーティング変更 → `doc/view/screen-transition.md`
     - ビルド・実行コマンド（justfile等）の変更 → `README.md` / `CLAUDE.md`
   - 該当する変更があるにもかかわらず対応ドキュメントが差分に含まれていない場合は、コミット前にユーザーへ「〇〇の変更がありますが、△△の更新は不要ですか？」と確認する（このスキル内での自動修正は行わない。ユーザーが必要と判断したら、更新してから改めてコミット対象に含める）。

## 3. コミットメッセージ生成
   - 差分（diff）を解析し、変更理由（Why）と変更内容（What）を把握する。
   - Conventional Commits形式（feat, fix, refactor, docs, test, chore など）でメッセージを作成する。

## 4. ユーザー確認
   - 変更ファイル一覧
   - 現在のブランチ
   - 生成したコミットメッセージ
   上記を提示し、ユーザーにコミット・プッシュを実行してよいか確認する。

## 5. 実行
   - 承認が得られたら `git commit` を実行する。
   - 続いて `git push origin <current-branch>` を実行する。
