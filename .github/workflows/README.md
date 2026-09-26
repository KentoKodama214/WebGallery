# GitHub Actions ワークフロー

## ワークフロー一覧

| ワークフロー | ファイル | トリガー |
|---|---|---|
| Javadocチェック | `checkstyle.yml` | `development`・`staging`・`master`へのPR、手動実行 |
| フォーマットチェック | `spotless.yml` | `development`・`staging`・`master`へのPR、手動実行 |
| テスト実行 | `test.yml` | `development`・`staging`・`master`へのPR、手動実行 |
| 依存関係の脆弱性スキャン | `test.yml`（`dependency-scan`ジョブ） | `development`・`staging`・`master`へのPR、手動実行 |
| カバレッジレポート | `test.yml`（`coverage-report`ジョブ） | `development`・`staging`・`master`へのPR、手動実行 |
| 環境昇格PR自動作成・リリースタグ発行 | `promote-branch.yml` | `development`・`staging`へのpush（PRマージ含む） |

セキュリティレビューはAnthropic APIの従量課金コストがかかるため、CIワークフロー化はせず、Claude Codeの`/security-review`スキルでローカルから都度実行する運用とする。

## 実行順序と依存関係

```
checkstyle.yml:
  Javadocチェック ──────────────────────→ (独立)

spotless.yml:
  フォーマットチェック ──────────────────→ (独立)

test.yml:
  フロントエンド単体テスト ─────────────→ (成功時のみ) ─┐
  本番CSPスモークテスト ────────────────→ (独立)         │
  依存関係の脆弱性スキャン ─────────────→ (独立)         │
  単体テスト ──→ (成功時のみ) 結合テスト ──┐             │
             ├→ (成功時のみ) E2Eテスト     │             │
             └───────────────────────────┴→ (全て成功時) カバレッジレポート
```

- Javadocチェック、フォーマットチェック、テスト実行は別ワークフローのため、**並列に実行**される
- フロントエンド単体テストはバックエンドの単体テストとは独立して**並列に実行**される
- 依存関係の脆弱性スキャンは他のジョブと依存関係を持たず**並列に実行**される
- 単体テストが失敗した場合、結合テスト・E2Eテストは**スキップ**される
- 結合テストとE2Eテストは互いに依存せず**並列に実行**される
- Javadocチェックの成否はテスト実行に**影響しない**
- カバレッジレポートはフロントエンド単体テスト・バックエンド単体テスト・結合テストがすべて成功した場合のみ実行される

## 手動実行

`checkstyle.yml`・`spotless.yml`・`test.yml`はGitHub Actionsの画面（Actions → 対象ワークフロー → Run workflow）から任意のブランチに対して手動実行できる（`workflow_dispatch`）。手動実行時は`coverage-report`ジョブのPRコメント投稿ステップのみスキップされ（PRに紐付かないため）、それ以外のジョブ・ジョブサマリーへの出力は通常のPR実行と同様に行われる。

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

### フロントエンド単体テスト (`test.yml` - `frontend-unit-test`)

`frontend`ディレクトリで`pnpm lint`（ESLint）と`pnpm test --coverage`（Jest）を実行する。バックエンドの単体テストとは独立して並列に実行される。カバレッジ集計データ（`coverage/coverage-summary.json`）はアーティファクト（`jest-coverage-summary`）としてアップロードされ、`coverage-report`ジョブで使用される。

### 単体テスト (`test.yml` - `unit-test`)

`./gradlew unitTest`を実行し、結合テスト(`*IntegrationTest*`)とMapperテスト(`mapper/*Test*`)を除く単体テストを実行する。

レイヤードアーキテクチャ（Controller → Service → Repository → Mapper）の依存方向違反は、`ArchitectureTest`（ArchUnit）としてこの単体テストの一部で検証される。

### 依存関係の脆弱性スキャン (`test.yml` - `dependency-scan`)

[OSV-Scanner](https://github.com/google/osv-scanner)を使用して、backend・frontendの依存関係（推移的依存を含む）に既知の脆弱性がないかをスキャンする。他のジョブと依存関係を持たず並列に実行される。

- backend: `./gradlew cyclonedxBom`（CycloneDXプラグイン）でランタイム依存関係全体のSBOM（Software Bill of Materials）を生成し、それをスキャン対象にする
- frontend: `pnpm-lock.yaml`を直接スキャン対象にする（依存パッケージのインストールは不要）

OSV-Scanner CLIはGitHub Releaseからバイナリを直接ダウンロードして使用する（外部Actionは不使用）。以前はOWASP Dependency-Checkを使用していたが、NVD（米国の脆弱性データベース）データベース全体の同期が必要でCI実行時間が長時間化する問題があったため、OSVデータベースをAPI照会するOSV-Scannerに置き換えた。脆弱性が1件でも見つかった場合はジョブが失敗する（重大度による絞り込みは行わない）。スキャン結果はMarkdown形式でジョブサマリーに出力し、同じ内容をアーティファクトとしてもアップロードする。

### 結合テスト (`test.yml` - `integration-test`)

PostgreSQLサービスコンテナを起動し、`./gradlew integrationTest`を実行する。単体テストが成功した場合のみ実行される。

### E2Eテスト (`test.yml` - `e2e-test`)

PostgreSQLサービスコンテナを起動し、バックエンド（`bootRun`）をバックグラウンドで起動した状態でフロントエンドのPlaywright E2Eテスト（`frontend/e2e/`）を実行する。単体テストが成功した場合のみ実行される。失敗時はバックエンドログとPlaywrightレポートがアーティファクトとしてアップロードされる。

E2Eテストは `next dev` で起動するため、本番でのみ付与されるCSPディレクティブ（`src/proxy.ts` の `style-src-elem` 等）は検証されない。その検証は下記「本番CSPスモークテスト」で行う。

### 本番CSPスモークテスト (`test.yml` - `e2e-prod-smoke`)

`next build` + `next start` で本番ビルドを起動し、`frontend/e2e/prod-smoke/` のPlaywrightテスト（`playwright.prod.config.ts`）を実行する。公開ページでCSP違反が発生しないこと・Tailwindのスタイルが適用されることを確認する。バックエンド・DBは不要で、他ジョブと独立して並列実行される。

### カバレッジレポート (`test.yml` - `coverage-report`)

**バックエンド（JaCoCo）:** 単体テスト・結合テストの各ジョブがアップロードしたJaCoCoの実行データ（`unitTest.exec` / `integrationTest.exec`）をダウンロードし、以下の3種類のレポート（XML/HTML）を生成する。

| レポート | Gradleタスク | 対象 |
|---|---|---|
| 単体テスト | `jacocoUnitReport` | `unitTest.exec` のみ |
| 結合テスト | `jacocoIntegrationReport` | `integrationTest.exec` のみ |
| 単体＋結合 | `jacocoAggregateReport` | `build/jacoco/*.exec` 全体 |

生成した3つのXMLを `.github/scripts/jacoco_coverage_table.py` で解析し、3行（単体＋結合／単体／結合）×各カバレッジ指標（命令・分岐・行・メソッド・クラス）のMarkdown表を作成する。

**フロントエンド（Jest）:** フロントエンド単体テストジョブがアップロードしたIstanbulのカバレッジ集計データ（`coverage-summary.json`）をダウンロードし、`.github/scripts/jest_coverage_table.py` で解析して各カバレッジ指標（ステートメント・分岐・関数・行）のMarkdown表を作成する。`jest.config.js`の`collectCoverageFrom`で`src/**/*.{ts,tsx}`を対象にしているため、テストが一度もimportしないファイルも未カバーとして集計に含まれる。

バックエンド・フロントエンド双方の表を連結し、**1つのPRコメント**として投稿し（`<!-- jacoco-coverage-report -->` マーカーで既存コメントを検索し、あればGitHub API経由で更新、なければ新規作成）、同じ内容をジョブサマリーにも出力する。

フロントエンド単体テスト・バックエンド単体テスト・結合テストがすべて成功した場合のみ実行される。しきい値による失敗は設定していない（可視化のみ）。外部Actionは使用せず、`gh` CLI と Python 標準ライブラリのみで完結する。

### 環境昇格PR自動作成・リリースタグ発行 (`promote-branch.yml`)

`development`へのpush（PRマージによるものを含む）で`development`→`staging`、`staging`へのpushで`staging`→`master`のマージPRを`gh pr create`で自動作成する。同じhead/baseの組み合わせでオープンなPRが既に存在する場合は作成をスキップする。レビュワーの自動アサインは行わないため、マージ先のRulesetで必須となっているコードオーナーレビューの依頼は手動で行う。外部Actionは使用せず`gh` CLIのみで完結する。

`staging`へのpush（＝本番リリース対象の確定）のタイミングでのみ、追加でセマンティックバージョニング（`vX.Y.Z`）のリリースタグを発行する。既存タグの最新patchバージョンを`git tag --list`と`sort -V`で取得し、patchを+1して`staging`のHEADコミットにタグ付け・pushする（タグが1つも無い場合は`v1.0.0`から開始）。minor/majorバージョンの更新は運用者が別途手動でタグを打つ想定で、このワークフローはpatchの自動インクリメントのみを行う。発行したタグ名は昇格PRの本文に記載される。
