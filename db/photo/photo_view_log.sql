/* Drop Tables */
DROP TABLE IF EXISTS photo.photo_view_log;


/* Create Tables */
CREATE TABLE photo.photo_view_log
(
	-- 写真閲覧ログNo
	photo_view_log_no bigserial NOT NULL,
	-- 写真アカウント番号
	photo_account_no bigint NOT NULL,
	-- 写真番号
	photo_no bigint NOT NULL,
	-- 閲覧者のアカウント番号（ログイン中の場合のみ設定。未ログインの場合は0。センチネル値のため外部キー制約なし）
	account_no bigint NOT NULL,
	-- リファラ（遷移元URL、取得できない場合は空文字）
	referer varchar(2048) NOT NULL DEFAULT '',
	-- 送信元IPアドレス
	ip_address varchar(45) NOT NULL,
	-- 国（ISO 3166-1 alpha-2コード、未解決時は空文字）
	country varchar(2) NOT NULL DEFAULT '',
	-- 地域（都道府県等のサブディビジョン名、未解決時は空文字）
	region varchar(100) NOT NULL DEFAULT '',
	-- 作成者（photo_account_noと同一）
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamptz NOT NULL DEFAULT NOW(),
	PRIMARY KEY (photo_view_log_no)
);


/* Create Foreign Keys */
ALTER TABLE photo.photo_view_log
	ADD FOREIGN KEY (photo_account_no, photo_no)
	REFERENCES photo.photo_mst (account_no, photo_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- 写真単位の閲覧回数集計で使用する
CREATE INDEX idx_photo_view_log_photo ON photo.photo_view_log (photo_account_no, photo_no);


/* Comments */
COMMENT ON TABLE photo.photo_view_log IS '写真詳細閲覧ログ';
COMMENT ON COLUMN photo.photo_view_log.photo_view_log_no IS '写真閲覧ログNo';
COMMENT ON COLUMN photo.photo_view_log.photo_account_no IS '写真アカウント番号';
COMMENT ON COLUMN photo.photo_view_log.photo_no IS '写真番号';
COMMENT ON COLUMN photo.photo_view_log.account_no IS '閲覧者のアカウント番号（ログイン中の場合のみ設定。未ログインの場合は0。センチネル値のため外部キー制約なし）';
COMMENT ON COLUMN photo.photo_view_log.referer IS 'リファラ（遷移元URL、取得できない場合は空文字）';
COMMENT ON COLUMN photo.photo_view_log.ip_address IS '送信元IPアドレス';
COMMENT ON COLUMN photo.photo_view_log.country IS '国: ISO 3166-1 alpha-2コード。未解決時は空文字';
COMMENT ON COLUMN photo.photo_view_log.region IS '地域: 都道府県等のサブディビジョン名。未解決時は空文字';
COMMENT ON COLUMN photo.photo_view_log.created_by IS '作成者（photo_account_noと同一）';
COMMENT ON COLUMN photo.photo_view_log.created_at IS '作成日時';
