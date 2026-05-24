/* Drop Tables */
DROP TABLE IF EXISTS common.refresh_token;


/* Create Tables */
CREATE TABLE common.refresh_token
(
	-- トークンID
	token_id bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- トークンハッシュ
	token_hash varchar(256) NOT NULL,
	-- 有効期限
	expires_at timestamptz NOT NULL,
	-- 作成日時
	created_at timestamptz NOT NULL DEFAULT NOW(),
	-- 無効化フラグ
	is_revoked boolean NOT NULL DEFAULT FALSE,
	PRIMARY KEY (token_id)
);


/* Create Foreign Keys */
ALTER TABLE common.refresh_token
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE CASCADE;


/* Create Indexes */
CREATE INDEX idx_refresh_token_account ON common.refresh_token (account_no);
CREATE INDEX idx_refresh_token_hash ON common.refresh_token (token_hash);
