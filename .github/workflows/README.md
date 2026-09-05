# GitHub Actions ワークフロー

## ワークフロー一覧

| ワークフロー | ファイル | トリガー |
|---|---|---|
| Javadocチェック | `checkstyle.yml` | `main`へのPR |
| フォーマットチェック | `spotless.yml` | `main`へのPR |
| アーキテクチャチェック | `architecture-check.yml` | `main`へのPR |
| テスト実行 | `test.yml` | `main`へのPR |
| カバレッジレポート | `test.yml`（`coverage-report`ジョブ） | `main`へのPR |

## 実行順序と依存関係

```
checkstyle.yml:
  Javadocチェック ──────────────────────→ (独立)

spotless.yml:
  フォーマットチェック ──────────────────→ (独立)

architecture-check.yml:
  アーキテクチャチェック ──────────────→ (独立)

test.yml:
  フロントエンド単体テスト ─────────────→ (独立)
  本番CSPスモークテスト ────────────────→ (独立)
  単体テスト ──→ (成功時のみ) 結合テスト ──┐
             ├→ (成功時のみ) E2Eテスト     │
             └───────────────────────────┴→ (両方成功時) カバレッジレポート
```

- Javadocチェック、フォーマットチェック、アーキテクチャチェック、テスト実行は別ワークフローのため、**並列に実行**される
- フロントエンド単体テストはバックエンドの単体テストとは独立して**並列に実行**される
- 単体テストが失敗した場合、結合テスト・E2Eテストは**スキップ**される
- 結合テストとE2Eテストは互いに依存せず**並列に実行**される
- Javadocチェックの成否はテスト実行に**影響しない**
- カバレッジレポートは単体テストと結合テストの両方が成功した場合のみ実行される

## 各ジョブの詳細

### Javadocチェック (`checkstyle.yml`)

Checkstyleを使用して、`src/main/java`配下の全クラス・全メソッドにJavadocが記載されているかをチェックする。

**チェック内容:**
- クラス・インターフェース・EnumにJavadocがあるか
- 全メソッド（public/protected/package/private）にJavadocがあるか
- `@param`、`@return`、`@throws`タグが正しく記載されているか
- Javadocの説明文が空でないか

**失敗時:** チェック結果レポートがアーティファクトとしてアップロードされる

### フォーマットチェック (`spotless.yml`)

Spotless（Google Java Format）を使用して、`src/main/java`・`src/test/java`配下の全Javaファイルが整形済みかをチェックする（`spotlessCheck`）。未整形の場合はローカルで`./backend/gradlew -p backend spotlessApply`（または`just format`）を実行して修正する。

**チェック内容:**
- Google Java Formatによるコードスタイル（インデント・改行位置等）
- 未使用importの削除、import順序
- 行末の余分な空白、ファイル末尾の改行

### アーキテクチャチェック (`architecture-check.yml`)

`scripts/check-architecture.sh`を実行し、レイヤードアーキテクチャ（Controller → Service → Repository → Mapper）に違反する依存関係がないかをチェックする。

**チェック内容:**
- Controller → Repository の直接参照がないか（スキップ違反）
- Service → Controller、Repository → Controller/Service の参照がないか（逆方向の依存）
- Controller同士、Service同士、Repository同士の呼び出しがないか（同レイヤー間の依存）

### フロントエンド単体テスト (`test.yml` - `frontend-unit-test`)

`frontend`ディレクトリで`pnpm lint`（ESLint）と`pnpm test`（Jest）を実行する。バックエンドの単体テストとは独立して並列に実行される。

### 単体テスト (`test.yml` - `unit-test`)

`./gradlew unitTest`を実行し、結合テスト(`*IntegrationTest*`)とMapperテスト(`mapper/*Test*`)を除く単体テストを実行する。

### 結合テスト (`test.yml` - `integration-test`)

PostgreSQLサービスコンテナを起動し、`./gradlew integrationTest`を実行する。単体テストが成功した場合のみ実行される。

### E2Eテスト (`test.yml` - `e2e-test`)

PostgreSQLサービスコンテナを起動し、バックエンド（`bootRun`）をバックグラウンドで起動した状態でフロントエンドのPlaywright E2Eテスト（`frontend/e2e/`）を実行する。単体テストが成功した場合のみ実行される。失敗時はバックエンドログとPlaywrightレポートがアーティファクトとしてアップロードされる。

E2Eテストは `next dev` で起動するため、本番でのみ付与されるCSPディレクティブ（`src/proxy.ts` の `style-src-elem` 等）は検証されない。その検証は下記「本番CSPスモークテスト」で行う。

### 本番CSPスモークテスト (`test.yml` - `e2e-prod-smoke`)

`next build` + `next start` で本番ビルドを起動し、`frontend/e2e/prod-smoke/` のPlaywrightテスト（`playwright.prod.config.ts`）を実行する。公開ページでCSP違反が発生しないこと・Tailwindのスタイルが適用されることを確認する。バックエンド・DBは不要で、他ジョブと独立して並列実行される。

### カバレッジレポート (`test.yml` - `coverage-report`)

単体テスト・結合テストの各ジョブがアップロードしたJaCoCoの実行データ（`unitTest.exec` / `integrationTest.exec`）をダウンロードし、以下の3種類のレポート（XML/HTML）を生成する。

| レポート | Gradleタスク | 対象 |
|---|---|---|
| 単体テスト | `jacocoUnitReport` | `unitTest.exec` のみ |
| 結合テスト | `jacocoIntegrationReport` | `integrationTest.exec` のみ |
| 単体＋結合 | `jacocoAggregateReport` | `build/jacoco/*.exec` 全体 |

生成した3つのXMLを `.github/scripts/jacoco_coverage_table.py` で解析し、3行（単体＋結合／単体／結合）×各カバレッジ指標（命令・分岐・行・メソッド・クラス）のMarkdown表を作成する。その表を **1つのPRコメント**として投稿し（`<!-- jacoco-coverage-report -->` マーカーで既存コメントを検索し、あればGitHub API経由で更新、なければ新規作成）、同じ内容をジョブサマリーにも出力する。

単体テストと結合テストの両方が成功した場合のみ実行される。しきい値による失敗は設定していない（可視化のみ）。外部Actionは使用せず、`gh` CLI と Python 標準ライブラリのみで完結する。
