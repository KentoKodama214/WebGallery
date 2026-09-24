/* Drop Tables */
DROP TABLE IF EXISTS common.inquiry_reply_mst;


/* Create Tables */
CREATE TABLE common.inquiry_reply_mst
(
	-- ID
	id bigserial NOT NULL,
	-- お問い合わせID
	inquiry_id bigint NOT NULL,
	-- 返信番号
	reply_no bigint NOT NULL,
	-- 返信した管理者のアカウント番号
	admin_account_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 返信本文: 空文字不可
	body text NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT inquiry_reply_no_unique UNIQUE (inquiry_id, reply_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE common.inquiry_reply_mst
	ADD FOREIGN KEY (inquiry_id)
	REFERENCES common.inquiry_mst (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;

ALTER TABLE common.inquiry_reply_mst
	ADD FOREIGN KEY (admin_account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
CREATE INDEX idx_inquiry_reply_mst_inquiry_id ON common.inquiry_reply_mst (inquiry_id, reply_no);


/* Comments */
COMMENT ON TABLE common.inquiry_reply_mst IS 'お問い合わせ返信マスタ';
COMMENT ON COLUMN common.inquiry_reply_mst.id IS 'ID';
COMMENT ON COLUMN common.inquiry_reply_mst.inquiry_id IS 'お問い合わせID : inquiry_mst(id)へのFK';
COMMENT ON COLUMN common.inquiry_reply_mst.reply_no IS '返信番号 : お問い合わせ単位の連番';
COMMENT ON COLUMN common.inquiry_reply_mst.admin_account_no IS '返信した管理者のアカウント番号';
COMMENT ON COLUMN common.inquiry_reply_mst.created_by IS '作成者';
COMMENT ON COLUMN common.inquiry_reply_mst.created_at IS '作成日時';
COMMENT ON COLUMN common.inquiry_reply_mst.body IS '返信本文 : 空文字不可';
