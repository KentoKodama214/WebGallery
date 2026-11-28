/* Drop Tables */
DROP TABLE IF EXISTS photo.portfolio_photo;


/* Create Tables */
CREATE TABLE photo.portfolio_photo
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- ポートフォリオ番号
	portfolio_no bigint NOT NULL,
	-- 写真番号
	photo_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 表示順: 1始まりの連番
	display_order int NOT NULL,
	-- 写真設定: テンプレート固有の写真単位の設定（JSON）
	photo_settings jsonb DEFAULT '{}' NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT portfolio_photo_unique UNIQUE (account_no, portfolio_no, photo_no),
	CONSTRAINT portfolio_photo_display_order_unique UNIQUE (account_no, portfolio_no, display_order)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE photo.portfolio_photo
	ADD FOREIGN KEY (account_no, portfolio_no)
	REFERENCES photo.portfolio_mst (account_no, portfolio_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;
ALTER TABLE photo.portfolio_photo
	ADD FOREIGN KEY (account_no, photo_no)
	REFERENCES photo.photo_mst (account_no, photo_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- 写真削除時に、その写真を使用しているポートフォリオを取得するために使用する
CREATE INDEX idx_portfolio_photo_account_no_photo_no ON photo.portfolio_photo (account_no, photo_no);


/* Comments */
COMMENT ON TABLE photo.portfolio_photo IS 'ポートフォリオ写真';
COMMENT ON COLUMN photo.portfolio_photo.id IS 'ID';
COMMENT ON COLUMN photo.portfolio_photo.account_no IS 'アカウント番号';
COMMENT ON COLUMN photo.portfolio_photo.portfolio_no IS 'ポートフォリオ番号';
COMMENT ON COLUMN photo.portfolio_photo.photo_no IS '写真番号';
COMMENT ON COLUMN photo.portfolio_photo.created_by IS '作成者';
COMMENT ON COLUMN photo.portfolio_photo.created_at IS '作成日時';
COMMENT ON COLUMN photo.portfolio_photo.display_order IS '表示順 : 1始まりの連番';
COMMENT ON COLUMN photo.portfolio_photo.photo_settings IS '写真設定 : テンプレート固有の写真単位の設定（JSON）';
