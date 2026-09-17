/* Drop Tables */
DROP TABLE IF EXISTS common.inquiry_mst;


/* Create Tables */
CREATE TABLE common.inquiry_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- お問い合わせ番号
	inquiry_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- 件名: 空文字不可
	subject varchar(100) NOT NULL,
	-- 本文: 空文字不可
	body text NOT NULL,
	-- ステータス区分:
	-- unreplied: 未対応
	-- replied: 回答済み
	status_kbn common.inquiry_status_enum DEFAULT 'unreplied' NOT NULL,
	-- ユーザー既読フラグ: 管理者の返信をユーザーが確認済みかどうか。返信投稿時にfalseへ更新される
	is_read_by_user boolean DEFAULT 'true' NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT inquiry_no_unique UNIQUE (account_no, inquiry_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE common.inquiry_mst
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
CREATE INDEX idx_inquiry_mst_account_no_inquiry_no ON common.inquiry_mst (account_no, inquiry_no DESC);
-- 管理者一覧のステータス絞り込み・新着順ソートを高速化する
CREATE INDEX idx_inquiry_mst_status_kbn_created_at ON common.inquiry_mst (status_kbn, created_at DESC);


/* Comments */
COMMENT ON TABLE common.inquiry_mst IS 'お問い合わせマスタ';
COMMENT ON COLUMN common.inquiry_mst.id IS 'ID';
COMMENT ON COLUMN common.inquiry_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.inquiry_mst.inquiry_no IS 'お問い合わせ番号 : アカウント単位の連番';
COMMENT ON COLUMN common.inquiry_mst.created_by IS '作成者';
COMMENT ON COLUMN common.inquiry_mst.created_at IS '作成日時';
COMMENT ON COLUMN common.inquiry_mst.updated_by IS '更新者';
COMMENT ON COLUMN common.inquiry_mst.updated_at IS '更新日時';
COMMENT ON COLUMN common.inquiry_mst.subject IS '件名 : 空文字不可';
COMMENT ON COLUMN common.inquiry_mst.body IS '本文 : 空文字不可';
COMMENT ON COLUMN common.inquiry_mst.status_kbn IS 'ステータス区分: unreplied(未対応)、replied(回答済み)';
COMMENT ON COLUMN common.inquiry_mst.is_read_by_user IS 'ユーザー既読フラグ : 管理者の返信をユーザーが確認済みかどうか。返信投稿時にfalseへ更新される';
