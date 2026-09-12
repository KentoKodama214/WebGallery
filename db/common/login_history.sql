/* Drop Tables */
DROP TABLE IF EXISTS common.login_history;


/* Create Tables */
CREATE TABLE common.login_history
(
	-- ログイン履歴No
	login_history_no bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- ログイン成功フラグ
	is_success boolean NOT NULL,
	-- 送信元IPアドレス
	ip_address varchar(45) NOT NULL,
	-- 国（ISO 3166-1 alpha-2コード、未解決時は空文字）
	country varchar(2) NOT NULL DEFAULT '',
	-- 地域（都道府県等のサブディビジョン名、未解決時は空文字）
	region varchar(100) NOT NULL DEFAULT '',
	-- 作成者（ログイン試行者自身のアカウント番号）
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamptz NOT NULL DEFAULT NOW(),
	PRIMARY KEY (login_history_no)
);


/* Create Foreign Keys */
ALTER TABLE common.login_history
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- アカウント単位のログイン状況分析（時系列）で使用する
CREATE INDEX idx_login_history_account ON common.login_history (account_no, created_at);


/* Comments */
COMMENT ON TABLE common.login_history IS 'ログイン履歴';
COMMENT ON COLUMN common.login_history.login_history_no IS 'ログイン履歴No';
COMMENT ON COLUMN common.login_history.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.login_history.is_success IS 'ログイン成功フラグ';
COMMENT ON COLUMN common.login_history.ip_address IS '送信元IPアドレス';
COMMENT ON COLUMN common.login_history.country IS '国: ISO 3166-1 alpha-2コード。未解決時は空文字';
COMMENT ON COLUMN common.login_history.region IS '地域: 都道府県等のサブディビジョン名。未解決時は空文字';
COMMENT ON COLUMN common.login_history.created_by IS '作成者（ログイン試行者自身のアカウント番号）';
COMMENT ON COLUMN common.login_history.created_at IS '作成日時';
