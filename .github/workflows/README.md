# GitHub Actions ワークフロー

## ワークフロー一覧

| ワークフロー | ファイル | トリガー |
|---|---|---|
| Javadocチェック | `checkstyle.yml` | `main`へのPR |
| アーキテクチャチェック | `architecture-check.yml` | `main`へのPR |
| テスト実行 | `test.yml` | `main`へのPR |
| カバレッジレポート | `test.yml`（`coverage-report`ジョブ） | `main`へのPR |

## 実行順序と依存関係

```
checkstyle.yml:
  Javadocチェック ──────────────────────→ (独立)

architecture-check.yml:
  アーキテクチャチェック ──────────────→ (独立)

test.yml:
  フロントエンド単体テスト ─────────────→ (独立)
  本番CSPスモークテスト ────────────────→ (独立)
  単体テスト ──→ (成功時のみ) 結合テスト ──┐
             ├→ (成功時のみ) E2Eテスト     │
             └───────────────────────────┴→ (両方成功時) カバレッジレポート
```

- Javadocチェック、アーキテクチャチェック、テスト実行は別ワークフローのため、**並列に実行**される
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

### アーキテクチャチェック (`architecture-check.yml`)

`scripts/check-architecture.sh`を実行し、レイヤードアーキテクチャ（Controller → Service → Repository → Mapper）に違反する依存関係がないかをチェックする。

**チェック内容:**
- Controller → Repository の直接参照がないか（スキップ違反）
- Service → Controller、Repository → Controller/Service の参照がないか（逆方向の依存）
- Controller同士、Service同士、Repository同士の呼び出しがないか（同レイヤー間の依存）

### フロントエンド単体テスト (`test.yml` - `frontend-unit-test`)

`frontend`ディレクトリで`pnpm lint`（ESLint）と`pnpm test`（Jest）を実行する。バックエンドの単体テストとは独立して並列に実行される。

### 単体テスト (`test.yml` - `unit-test`)

`./gradlew test`を実行し、結合テスト(`*IntegrationTest*`)とMapperテスト(`mapper/*Test*`)を除く単体テストを実行する。

### 結合テスト (`test.yml` - `integration-test`)

PostgreSQLサービスコンテナを起動し、`./gradlew integrationTest`を実行する。単体テストが成功した場合のみ実行される。

### E2Eテスト (`test.yml` - `e2e-test`)

PostgreSQLサービスコンテナを起動し、バックエンド（`bootRun`）をバックグラウンドで起動した状態でフロントエンドのPlaywright E2Eテスト（`frontend/e2e/`）を実行する。単体テストが成功した場合のみ実行される。失敗時はバックエンドログとPlaywrightレポートがアーティファクトとしてアップロードされる。

E2Eテストは `next dev` で起動するため、本番でのみ付与されるCSPディレクティブ（`src/proxy.ts` の `style-src-elem` 等）は検証されない。その検証は下記「本番CSPスモークテスト」で行う。

### 本番CSPスモークテスト (`test.yml` - `e2e-prod-smoke`)

`next build` + `next start` で本番ビルドを起動し、`frontend/e2e/prod-smoke/` のPlaywrightテスト（`playwright.prod.config.ts`）を実行する。公開ページでCSP違反が発生しないこと・Tailwindのスタイルが適用されることを確認する。バックエンド・DBは不要で、他ジョブと独立して並列実行される。

### カバレッジレポート (`test.yml` - `coverage-report`)

単体テスト・結合テストの各ジョブがアップロードしたJaCoCoの実行データ（`test.exec` / `integrationTest.exec`）をダウンロードし、`./gradlew jacocoAggregateReport` で1つのレポートに集約する。生成したXMLを `madrapps/jacoco-report` に渡し、バックエンド全体および変更ファイルのカバレッジ率をPRにコメントする（同一PR内では既存コメントを更新）。単体テストと結合テストの両方が成功した場合のみ実行される。しきい値による失敗は設定していない（可視化のみ）。
