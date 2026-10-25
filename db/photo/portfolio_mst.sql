/* Drop Tables */
DROP TABLE IF EXISTS photo.portfolio_mst;


/* Create Tables */
CREATE TABLE photo.portfolio_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- ポートフォリオ番号
	portfolio_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- テーマ: 空文字不可
	theme varchar(100) NOT NULL,
	-- 説明文
	description text DEFAULT '' NOT NULL,
	-- テンプレート区分:
	-- feature: フィーチャー型
	-- grid: グリッド型
	template_kbn photo.portfolio_template_enum NOT NULL,
	-- テンプレート設定: テンプレート固有のポートフォリオ単位の設定（JSON）
	template_settings jsonb DEFAULT '{}' NOT NULL,
	-- 設定バージョン: template_settings・portfolio_photo.photo_settingsのJSON構造のバージョン
	settings_version int DEFAULT 1 NOT NULL,
	-- 公開状態区分:
	-- draft: 下書き
	-- published: 公開
	status_kbn photo.portfolio_status_enum DEFAULT 'draft' NOT NULL,
	-- 表示順: アカウント内のポートフォリオの並び順（1始まり。昇順に表示する）
	display_order int NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT portfolio_no_unique UNIQUE (account_no, portfolio_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.portfolio_mst
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- 公開ページでのアカウント単位の公開中ポートフォリオ取得で使用する
CREATE INDEX idx_portfolio_mst_account_no_status_kbn ON photo.portfolio_mst (account_no, status_kbn);


/* Comments */
COMMENT ON TABLE photo.portfolio_mst IS 'ポートフォリオマスタ';
COMMENT ON COLUMN photo.portfolio_mst.id IS 'ID';
COMMENT ON COLUMN photo.portfolio_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN photo.portfolio_mst.portfolio_no IS 'ポートフォリオ番号';
COMMENT ON COLUMN photo.portfolio_mst.created_by IS '作成者';
COMMENT ON COLUMN photo.portfolio_mst.created_at IS '作成日時';
COMMENT ON COLUMN photo.portfolio_mst.updated_by IS '更新者';
COMMENT ON COLUMN photo.portfolio_mst.updated_at IS '更新日時';
COMMENT ON COLUMN photo.portfolio_mst.theme IS 'テーマ : 空文字不可';
COMMENT ON COLUMN photo.portfolio_mst.description IS '説明文';
COMMENT ON COLUMN photo.portfolio_mst.template_kbn IS 'テンプレート区分: feature(フィーチャー型)、grid(グリッド型)';
COMMENT ON COLUMN photo.portfolio_mst.template_settings IS 'テンプレート設定 : テンプレート固有のポートフォリオ単位の設定（JSON）';
COMMENT ON COLUMN photo.portfolio_mst.settings_version IS '設定バージョン : template_settings・portfolio_photo.photo_settingsのJSON構造のバージョン';
COMMENT ON COLUMN photo.portfolio_mst.status_kbn IS '公開状態区分: draft(下書き)、published(公開)';
COMMENT ON COLUMN photo.portfolio_mst.display_order IS '表示順 : アカウント内のポートフォリオの並び順（1始まり。昇順に表示する）';
