---
paths:
  - frontend/e2e/**
---

# E2Eテスト（Playwright）のアーキテクチャルール

## 配置ルール

- ページ単位の検証は`pages/`に、複数ページにまたがるシナリオ（ログイン→操作→別ページでの確認等）は`scenarios/`に配置すること
- 本番ビルド専用のスモークテスト（CSP等、`next dev`では検証できないもの）は`prod-smoke/`に配置し、`playwright.prod.config.ts`からのみ実行すること（通常の`playwright.config.ts`は`testIgnore`で除外する）
- 共通のヘルパー・フィクスチャは`fixtures/`に配置すること（`auth.ts`＝一般アカウント認証、`admin.ts`＝管理者アカウント認証、`db.ts`＝DB接続情報、`a11y.ts`＝アクセシビリティ検証）
- テスト用画像・不正ファイルは`fixtures/images/`に配置すること

## 命名規則

- ファイル名は検証対象のページ・シナリオが分かるスネークケース（例：`photo_setting_validation.spec.ts`）とし、対象が広い場合は関心事ごとにファイルを分割すること（例：`photo_setting_fields.spec.ts`と`photo_setting_validation.spec.ts`）
- `test.describe`・`test`のタイトルは日本語で、検証内容が結果だけで分かる文（「〜こと」で終える）にすること

## フィクスチャ設計方針

- バックエンドはログイン時に同一アカウントの既存セッションを全て失効させるため（`AuthServiceImpl#login`）、認証必須のテストで単一の共有アカウントを複数ワーカーが使い回すと並列実行時にセッションを奪い合って401になる。ワーカーごとに使い捨てアカウントを1つだけ登録・ログインし、そのワーカー内の全テストで同一の`workerPage`/`adminPage`（同一ブラウザコンテキスト）を使い回すこと（`fixtures/auth.ts`の`workerPage`、`fixtures/admin.ts`の`adminPage`）
- 「写真0件の状態を検証したい」等、共有の`workerPage`の状態（他テストが写真を登録済み等）を前提にできない検証は、`generateTestAccountId`で個別に使い捨てアカウントを登録すること
- UI操作だけでは到達できない状態（管理者権限の初期付与、撮影場所の紐付け等）に限り、`fixtures/db.ts`の`DB_CONFIG`を用いてDBを直接更新してよい。UI経由で再現できる状態をDB直接更新で代替しないこと
- アカウント一覧のページング境界値等、ソート順への依存があるテストは`generateSortEarlyTestAccountId`のように既存データより辞書順で先頭に来るIDを使い、ローカルでのE2E生成アカウント蓄積の影響を受けないようにすること（蓄積分は`global-setup.ts`が実行前に一括削除する）

## クロスブラウザ・視覚回帰・アクセシビリティ検証の適用範囲

- `playwright.config.ts`の`webkit-smoke`・`mobile-smoke`プロジェクトは実行時間を抑えるため、レイアウト崩れの影響が大きい主要導線（`CROSS_BROWSER_SPECS`で指定するspecのみ）に限定している。対象を増やす場合はCIの実行時間増加とのトレードオフを踏まえること
- WebKit（`webkit-smoke`・`mobile-smoke`。iPhone等のモバイルプリセットもWebKitエンジン）は、認証Cookie（`AuthController`が発行する`Secure`付きリフレッシュトークンCookie）を`http://localhost`（非HTTPS）では保存しない。ログイン後に`page.goto`でフルページ遷移するテストはクライアント側メモリのアクセストークンが失われ再認証に失敗するため、`CROSS_BROWSER_SPECS`にはフルページ遷移を伴う認証必須フローを含めないこと（本番はHTTPSのため実害はない、ローカル/CI検証環境固有の制約）
- CI上のWebKitはChromiumより応答待ちのタイムアウトに引っかかりやすい（共有ランナーでの起動・処理が相対的に遅い）。`CROSS_BROWSER_SPECS`対象specのアサーションは`timeout: 5000`のような短い値を避け、`fixtures/auth.ts`の`login`/`registerAccount`と同じ`timeout: 10000`以上を基準にすること
- 視覚回帰テスト（`toHaveScreenshot`）・アクセシビリティ検証（`expectNoAccessibilityViolations`）は、複数プロジェクトで重複実行して時間を浪費しないよう、`test.skip(testInfo.project.name !== "chromium", ...)`でchromiumプロジェクトのみに限定すること
- 視覚回帰テストのスクリーンショットはOS（フォントレンダリング等）に依存するため、プラットフォームごとに別ファイルとして保存される（Playwrightの既定の`snapshotPathTemplate`）。ローカル（macOS等）で生成したベースラインはCI（Linux）では通らない。CIのベースラインはCI実行結果のartifact（失敗時にアップロードされる実際のスクリーンショット）から取得して`*-snapshots/`配下にコミットすること
- 視覚回帰テストには`maxDiffPixelRatio`で許容誤差を設定し、アンチエイリアシング等の微小差分で恒常的に失敗しないようにすること

## テスト実行環境

- ローカル実行（`just e2e`）・CI実行のいずれも、レート制限（`RATE_LIMIT_ENABLED`）を無効化している。多数のワーカーが同一IPからアカウント登録・ログインを繰り返すE2Eの性質上、本番向けのレート制限が誤検知するため。レート制限機能自体の検証はE2Eではなく結合テストで行うこと
- 写真アップロードを伴うテストは実際のS3互換ストレージ（ローカル/CIともにMinIO）に接続する。ストレージ自体をモック化しないこと
