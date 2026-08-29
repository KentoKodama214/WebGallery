/* Drop Tables */
DROP TABLE IF EXISTS photo.photo_mst;


/* Create Tables */
CREATE TABLE photo.photo_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- 写真番号
	photo_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- 削除フラグ
	is_deleted boolean DEFAULT 'false' NOT NULL,
	-- 撮影日時
	photo_at timestamp with time zone NOT NULL,
	-- ロケーション番号
	location_no bigint NOT NULL,
	-- 画像ファイルパス: 空文字不可
	image_file_path text NOT NULL,
	-- 画像ファイル名: 空文字不可、image_file_pathの末尾のファイル名部分。存在チェックの等価検索用
	image_file_name text NOT NULL,
	-- 写真タイトル日本語名: 空文字不可
	photo_japanese_title varchar(100) NOT NULL,
	-- 写真タイトル英語名
	photo_english_title varchar(100) DEFAULT '""' NOT NULL,
	-- キャプション
	caption text DEFAULT '""' NOT NULL,
	-- 向き区分コード:
	-- vertical: 縦
	-- horizontal: 横
	-- square: 正方形
	direction_kbn photo.direction_enum DEFAULT 'none' NOT NULL,
	-- 焦点距離: 単位：mm
	focal_length int NOT NULL,
	-- F値
	f_value decimal(5,2) NOT NULL,
	-- シャッタースピード: 単位：秒
	-- 1日が86400秒
	-- 1/4000秒が0.00025秒
	shutter_speed decimal(10,5) NOT NULL,
	-- ISO
	iso int NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT photo_no_unique UNIQUE (account_no, photo_no)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.photo_mst
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
CREATE INDEX idx_photo_mst_account_no_image_file_name ON photo.photo_mst (account_no, image_file_name);


/* Comments */
COMMENT ON TABLE photo.photo_mst IS '写真マスタ';
COMMENT ON COLUMN photo.photo_mst.id IS 'ID';
COMMENT ON COLUMN photo.photo_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN photo.photo_mst.photo_no IS '写真番号';
COMMENT ON COLUMN photo.photo_mst.created_by IS '作成者';
COMMENT ON COLUMN photo.photo_mst.created_at IS '作成日時';
COMMENT ON COLUMN photo.photo_mst.updated_by IS '更新者';
COMMENT ON COLUMN photo.photo_mst.updated_at IS '更新日時';
COMMENT ON COLUMN photo.photo_mst.is_deleted IS '削除フラグ';
COMMENT ON COLUMN photo.photo_mst.photo_at IS '撮影日時';
COMMENT ON COLUMN photo.photo_mst.location_no IS 'ロケーション番号';
COMMENT ON COLUMN photo.photo_mst.image_file_path IS '画像ファイルパス : 空文字不可';
COMMENT ON COLUMN photo.photo_mst.image_file_name IS '画像ファイル名 : 空文字不可、image_file_pathの末尾のファイル名部分。存在チェックの等価検索用';
COMMENT ON COLUMN photo.photo_mst.photo_japanese_title IS '写真タイトル日本語名 : 空文字不可';
COMMENT ON COLUMN photo.photo_mst.photo_english_title IS '写真タイトル英語名';
COMMENT ON COLUMN photo.photo_mst.caption IS 'キャプション';
COMMENT ON COLUMN photo.photo_mst.direction_kbn IS '向き区分: vertical(縦)、horizontal(横)、square(正方形)、none(未設定)';
COMMENT ON COLUMN photo.photo_mst.focal_length IS '焦点距離 : 単位：mm';
COMMENT ON COLUMN photo.photo_mst.f_value IS 'F値';
COMMENT ON COLUMN photo.photo_mst.shutter_speed IS 'シャッタースピード : 単位：秒 1日が86400秒 1/4000秒が0.00025秒';
COMMENT ON COLUMN photo.photo_mst.iso IS 'ISO';