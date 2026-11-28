import { deleteE2eTestAccounts } from "./fixtures/cleanup";

/**
 * テスト実行後に、実行中に登録したE2E生成アカウントとその関連データを削除する
 *
 * `scripts/e2e.sh`のローカル実行はdocker-composeの永続的なPostgreSQLコンテナを使うため、
 * 削除しないとテストデータが次回の実行まで残り、ローカルでの動作確認時にも一覧へ表示されてしまう。
 *
 * 実行前のクリーンアップ（`global-setup.ts`）は残しておく。テスト実行を中断した場合等、
 * 本処理が走らずにデータが残ったときの保険になる。
 */
export default async function globalTeardown(): Promise<void> {
  await deleteE2eTestAccounts();
}
