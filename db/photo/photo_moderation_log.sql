/* Drop Tables */
DROP TABLE IF EXISTS photo.photo_moderation_log;


/* Create Tables */
CREATE TABLE photo.photo_moderation_log
(
	-- 写真モデレーションログNo
	photo_moderation_log_no bigserial NOT NULL,
	-- 写真アカウント番号
	photo_account_no bigint NOT NULL,
	-- 写真番号
	photo_no bigint NOT NULL,
	-- 操作した管理者のアカウント番号（管理者の退会を妨げないよう外部キー制約なし）
	admin_account_no bigint NOT NULL,
	-- 非公開フラグ: trueなら非公開にした操作、falseなら非公開を解除した操作
	is_hidden boolean NOT NULL,
	-- 理由: 空文字不可
	reason varchar(500) NOT NULL,
	-- 作成者（admin_account_noと同一）
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	PRIMARY KEY (photo_moderation_log_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.photo_moderation_log
	ADD FOREIGN KEY (photo_account_no, photo_no)
	REFERENCES photo.photo_mst (account_no, photo_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- 写真単位の操作履歴の参照、および退会時の削除で使用する
CREATE INDEX idx_photo_moderation_log_photo ON photo.photo_moderation_log (photo_account_no, photo_no);


/* Comments */
COMMENT ON TABLE photo.photo_moderation_log IS '写真モデレーションログ';
COMMENT ON COLUMN photo.photo_moderation_log.photo_moderation_log_no IS '写真モデレーションログNo';
COMMENT ON COLUMN photo.photo_moderation_log.photo_account_no IS '写真アカウント番号';
COMMENT ON COLUMN photo.photo_moderation_log.photo_no IS '写真番号';
COMMENT ON COLUMN photo.photo_moderation_log.admin_account_no IS '操作した管理者のアカウント番号（管理者の退会を妨げないよう外部キー制約なし）';
COMMENT ON COLUMN photo.photo_moderation_log.is_hidden IS '非公開フラグ : trueなら非公開にした操作、falseなら非公開を解除した操作';
COMMENT ON COLUMN photo.photo_moderation_log.reason IS '理由 : 空文字不可';
COMMENT ON COLUMN photo.photo_moderation_log.created_by IS '作成者（admin_account_noと同一）';
COMMENT ON COLUMN photo.photo_moderation_log.created_at IS '作成日時';
