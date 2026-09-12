/* Drop Tables */
DROP TABLE IF EXISTS photo.photo_list_filter_log;


/* Create Tables */
CREATE TABLE photo.photo_list_filter_log
(
	-- 写真一覧絞り込みログNo
	photo_list_filter_log_no bigserial NOT NULL,
	-- 写真アカウント番号（閲覧対象ギャラリーの所有者）
	photo_account_no bigint NOT NULL,
	-- 向き区分コード
	direction_kbn photo.direction_enum NOT NULL,
	-- お気に入り写真のみ絞り込みフラグ
	is_favorite boolean NOT NULL,
	-- タグリスト（リクエストされた生文字列、空文字可）
	tag_list varchar(500) NOT NULL DEFAULT '',
	-- 並び順
	sort_by photo.sort_photo_enum NOT NULL,
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
	PRIMARY KEY (photo_list_filter_log_no)
);


/* Create Foreign Keys */
ALTER TABLE photo.photo_list_filter_log
	ADD FOREIGN KEY (photo_account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- アカウントID単位の絞り込み・並び替え利用状況分析で使用する
CREATE INDEX idx_photo_list_filter_log_account ON photo.photo_list_filter_log (photo_account_no, created_at);


/* Comments */
COMMENT ON TABLE photo.photo_list_filter_log IS '写真一覧絞り込みログ（「もっと見る」による追加取得は対象外、初回検索のみ記録）';
COMMENT ON COLUMN photo.photo_list_filter_log.photo_list_filter_log_no IS '写真一覧絞り込みログNo';
COMMENT ON COLUMN photo.photo_list_filter_log.photo_account_no IS '写真アカウント番号（閲覧対象ギャラリーの所有者）';
COMMENT ON COLUMN photo.photo_list_filter_log.direction_kbn IS '向き区分: vertical(縦)、horizontal(横)、square(正方形)、none(未設定)';
COMMENT ON COLUMN photo.photo_list_filter_log.is_favorite IS 'お気に入り写真のみ絞り込みフラグ';
COMMENT ON COLUMN photo.photo_list_filter_log.tag_list IS 'タグリスト（リクエストされた生文字列、空文字可）';
COMMENT ON COLUMN photo.photo_list_filter_log.sort_by IS '並び順: photo_at(撮影日順)、favorite(お気に入り数順)、season(季節順)';
COMMENT ON COLUMN photo.photo_list_filter_log.referer IS 'リファラ（遷移元URL、取得できない場合は空文字）';
COMMENT ON COLUMN photo.photo_list_filter_log.ip_address IS '送信元IPアドレス';
COMMENT ON COLUMN photo.photo_list_filter_log.country IS '国: ISO 3166-1 alpha-2コード。未解決時は空文字';
COMMENT ON COLUMN photo.photo_list_filter_log.region IS '地域: 都道府県等のサブディビジョン名。未解決時は空文字';
COMMENT ON COLUMN photo.photo_list_filter_log.created_by IS '作成者（photo_account_noと同一）';
COMMENT ON COLUMN photo.photo_list_filter_log.created_at IS '作成日時';
