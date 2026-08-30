/**
 * フロントエンド全体で共有する定数
 *
 * バックエンドと対応する値は、バックエンドの定義（`Consts` 等）と乖離しないよう
 * ここで一元管理する。
 */

/**
 * ログイン失敗によりアカウントがロックされる失敗回数のしきい値
 *
 * バックエンドのロック判定（`loginFailureCount` がこの値以上でロック）と一致させる。
 * バックエンドは `application.yml` の `login.failCount`（現在 3、全プロファイル共通）で判定し、
 * `doc/architecture/security.md` にも「ログイン失敗3回でロック」と明記されている。
 * 管理画面の状態表示・操作ボタンの活性制御に使用する。
 */
export const LOGIN_FAILURE_LOCK_THRESHOLD = 3;
