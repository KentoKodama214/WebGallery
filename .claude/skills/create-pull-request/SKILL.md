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

## 2. 検証事項の実施
   - テンプレートの「検証事項」に記載された各コマンドを、上から順にすべて実行する。
   - いずれかが失敗した場合は、その時点で処理を中断し、失敗したコマンドと出力内容をユーザーに報告する（PRは作成しない。自動修正は行わない）。
   - 各項目の実行方法:
     1. `just backend-build` を実行し、成功を確認する。
     2. `just backend-unitTest` を実行し、成功を確認する。
     3. Docker PostgreSQLが未起動なら `just db-up` を実行してから、`just backend-integrationTest` を実行し、成功を確認する。
     4. `just backend-run` の起動確認: バックグラウンドで `./backend/gradlew -p backend bootRun --args='--spring.profiles.active=local'` を起動し、`curl -sf http://localhost:8080/v3/api-docs` を数秒間隔でポーリングして起動完了を確認する（タイムアウト目安60秒）。確認できたらプロセスを停止する（gradlewは子プロセスを起動するため、プロセスグループごと停止する）。
     5. `just front-run` の起動確認: バックグラウンドで `cd frontend && mise exec -- pnpm dev` を起動し、`curl -sf http://localhost:3000` などで応答があることを確認する。確認できたらプロセスを停止する。
     6. `just e2e` を実行し、成功を確認する（DB・backendが未起動の場合は自動起動される）。
   - すべて成功したら、テンプレートの「検証事項」チェックボックスをすべて `[x]` にする。

## 3. タイトルと本文の生成
   - タイトル: Conventional Commitsに基づいた簡潔な1行（例: `feat: YOLOv8による背景判定機能の追加`）
   - 本文: テンプレートの構造に従って記述する。「検証事項」は手順2の実施結果を反映する。

## 4. 実行
   - `pwd` コマンドで作業ディレクトリが `WebGallery` であることを確認する
   - `git status` で未プッシュのコミットがあるか確認し、必要に応じて `/commit-push` のスキルを実行する。
   - 生成した PR タイトルと本文、および実行する `gh pr create` コマンドをユーザーに提示し、実行してよいか確認する。
   - 承認が得られたら `gh pr create --title "..." --body "..."` を実行する。
   - 作成された PR の URL を出力する。
