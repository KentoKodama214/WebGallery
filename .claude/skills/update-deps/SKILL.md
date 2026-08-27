---
name: update-deps
description: Next.js (pnpm/npm) と Java Spring Boot (Gradle) の依存ライブラリを安全に更新します。
---

# Update dependencies

## 1. 引数の解析
   - `--target`: `frontend` | `backend` | `all`（指定がない場合はユーザーに確認）
   - `--scope`: `minor`（Patch/Minorのみ） | `major`（Major含む） | `package-name`（個別指定）

## 2. フロントエンドの更新手順（target: frontend）
   - ディレクトリ移動: `cd frontend`
   - 更新確認: `npx npm-check-updates --output json` を実行して更新対象を把握。
   - フィルタリング:
     - `--scope minor` の場合は、メジャーバージョンが上がらないものだけを抽出。
   - 更新適用: `npx npm-check-updates -u <更新対象のパッケージ>` を実行して package.json を更新。
   - インストール: `pnpm install` または `npm install` を実行。
   - 検証:
     - `pnpm build`（または `npm run build`）を実行。
     - `pnpm test` を実行。

## 3. バックエンドの更新手順（target: backend）
   - ディレクトリ移動: `cd backend`
   - 更新確認: `./gradlew dependencyUpdates -DoutputFormatter=json` を実行。
   - レポート読み込み: `build/dependencyUpdates/report.json` を読み込み、`outdated.dependencies` を解析。
   - 編集: `build.gradle` のバージョン記述を更新。
   - 検証:
     - `./gradlew compileJava`
     - `./gradlew test` を実行。

4. 失敗時の回復処理（Self-Correction）
   - テストやビルドが失敗した場合:
     - エラーログを解析する。
     - 変更を `git checkout` でロールバックし、エラーの原因となったライブラリのみを特定する。
     - ユーザーに「どのライブラリ更新によってビルドが壊れたか」と「対応案」を報告する。

5. 完了報告
   - 更新されたライブラリの一覧（Before -> After）をテーブル形式で提示する。
