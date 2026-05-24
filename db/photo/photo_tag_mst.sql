/* Drop Tables */
DROP TABLE IF EXISTS photo.photo_tag_mst;


/* Create Tables */
CREATE TABLE photo.photo_tag_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- 写真番号
	photo_no bigint NOT NULL,
	-- タグ番号
	tag_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- タグ日本語名: 空文字不可
	tag_japanese_name varchar(20) NOT NULL,
	-- タグ英語名
	tag_english_name varchar(20) DEFAULT '""' NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT photo_tag_no_unique UNIQUE (account_no, photo_no, tag_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.photo_tag_mst
	ADD FOREIGN KEY (account_no, photo_no)
	REFERENCES photo.photo_mst (account_no, photo_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Comments */
COMMENT ON TABLE photo.photo_tag_mst IS '写真タグマスタ';
COMMENT ON COLUMN photo.photo_tag_mst.id IS 'ID';
COMMENT ON COLUMN photo.photo_tag_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN photo.photo_tag_mst.photo_no IS '写真番号';
COMMENT ON COLUMN photo.photo_tag_mst.tag_no IS 'タグ番号';
COMMENT ON COLUMN photo.photo_tag_mst.created_by IS '作成者';
COMMENT ON COLUMN photo.photo_tag_mst.created_at IS '作成日時';
COMMENT ON COLUMN photo.photo_tag_mst.tag_japanese_name IS 'タグ日本語名 : 空文字不可';
COMMENT ON COLUMN photo.photo_tag_mst.tag_english_name IS 'タグ英語名';