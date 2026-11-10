/* Drop Tables */
DROP TABLE IF EXISTS photo.frame_template_mst;


/* Create Tables */
CREATE TABLE photo.frame_template_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- 額縁テンプレート番号
	frame_template_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- テンプレート名: 空文字不可
	template_name varchar(50) NOT NULL,
	-- 額縁定義: 額縁のサイズ・背景・外枠・写真枠・メッセージ枠の配置とデザイン（JSON）
	frame_definition jsonb DEFAULT '{}' NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT frame_template_no_unique UNIQUE (account_no, frame_template_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.frame_template_mst
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Comments */
COMMENT ON TABLE photo.frame_template_mst IS '額縁テンプレートマスタ';
COMMENT ON COLUMN photo.frame_template_mst.id IS 'ID';
COMMENT ON COLUMN photo.frame_template_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN photo.frame_template_mst.frame_template_no IS '額縁テンプレート番号';
COMMENT ON COLUMN photo.frame_template_mst.created_by IS '作成者';
COMMENT ON COLUMN photo.frame_template_mst.created_at IS '作成日時';
COMMENT ON COLUMN photo.frame_template_mst.updated_by IS '更新者';
COMMENT ON COLUMN photo.frame_template_mst.updated_at IS '更新日時';
COMMENT ON COLUMN photo.frame_template_mst.template_name IS 'テンプレート名 : 空文字不可';
COMMENT ON COLUMN photo.frame_template_mst.frame_definition IS '額縁定義 : 額縁のサイズ・背景・外枠・写真枠・メッセージ枠の配置とデザイン（JSON）。ポートフォリオで選択すると portfolio_mst.template_settings にコピーされる';
