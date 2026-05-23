/* Drop Tables */
DROP TABLE IF EXISTS common.location_mst;


/* Create Tables */
CREATE TABLE common.location_mst
(
	-- ID
	id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- ロケーション番号
	location_no bigint NOT NULL,
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
	-- ロケーション名
	location_name text NOT NULL,
	-- 住所
	address text DEFAULT '' NOT NULL,
	-- 緯度
	latitude decimal(11,4) NOT NULL,
	-- 経度
	longitude decimal(11,4) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT location_no_unique UNIQUE (account_no, location_no),
	CONSTRAINT location_name_unique UNIQUE (account_no, location_name)
) WITHOUT OIDS;


/* Create Foreign Keys */
ALTER TABLE common.location_mst
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Comments */
COMMENT ON TABLE common.location_mst IS 'ロケーションマスタ';
COMMENT ON COLUMN common.location_mst.id IS 'ID';
COMMENT ON COLUMN common.location_mst.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.location_mst.location_no IS 'ロケーション番号';
COMMENT ON COLUMN common.location_mst.created_by IS '作成者';
COMMENT ON COLUMN common.location_mst.created_at IS '作成日時';
COMMENT ON COLUMN common.location_mst.updated_by IS '更新者';
COMMENT ON COLUMN common.location_mst.updated_at IS '更新日時';
COMMENT ON COLUMN common.location_mst.is_deleted IS '削除フラグ';
COMMENT ON COLUMN common.location_mst.location_name IS 'ロケーション名 : 空文字不可';
COMMENT ON COLUMN common.location_mst.address IS '住所';
COMMENT ON COLUMN common.location_mst.latitude IS '緯度';
COMMENT ON COLUMN common.location_mst.longitude IS '経度';