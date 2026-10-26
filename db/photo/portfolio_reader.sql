/* Create Role */
-- 公開ページ向けのポートフォリオ表示API（portfolio-api。Go）が接続する読み取り専用ロール。
-- 書き込みは Java のバックエンドだけが行うため、表示に必要なテーブル・列の SELECT だけを許可する。
-- ロールはクラスタ単位のため、既に存在する場合は作成しない（権限の付与は毎回やり直す）。
-- パスワードはローカル開発・テスト用の値。本番では別の値で作成・変更すること。
DO $$
BEGIN
	IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'portfolio_reader') THEN
		CREATE ROLE portfolio_reader LOGIN PASSWORD 'portfolio_reader';
	END IF;
END
$$;


/* Grant Privileges */
GRANT USAGE ON SCHEMA common, photo TO portfolio_reader;

-- アカウントIDからアカウント番号を引くためだけに使う（パスワード等の個人情報の列は読ませない）
GRANT SELECT (account_no, account_id) ON common.account TO portfolio_reader;

-- 写真は表示に必要な列だけを読ませる（撮影場所等の非公開になりうる情報の列は読ませない）
GRANT SELECT (account_no, photo_no, is_deleted, image_file_path, photo_japanese_title, photo_english_title, direction_kbn)
	ON photo.photo_mst TO portfolio_reader;

GRANT SELECT ON photo.portfolio_mst, photo.portfolio_photo, photo.portfolio_tag_mst TO portfolio_reader;
