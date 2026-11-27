/* Drop Tables */
DROP TABLE IF EXISTS common.recovery_code;


/* Create Tables */
CREATE TABLE common.recovery_code
(
	-- リカバリーコードNo
	recovery_code_no bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- コードハッシュ: リカバリーコード（80ビットの乱数）のSHA-256（16進数）。平文は発行時に一度だけ利用者へ表示し、保存しない
	code_hash varchar(64) NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL DEFAULT NOW(),
	PRIMARY KEY (recovery_code_no),
	UNIQUE (account_no, code_hash)
);


/* Create Foreign Keys */
ALTER TABLE common.recovery_code
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Comments */
-- 使用済みのコードは使用時に削除する（使用済みフラグは持たない）
COMMENT ON TABLE common.recovery_code IS 'リカバリーコード（パスキーをすべて失ったときに新しいパスキーを登録するための使い捨てのコード）';
COMMENT ON COLUMN common.recovery_code.recovery_code_no IS 'リカバリーコードNo';
COMMENT ON COLUMN common.recovery_code.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.recovery_code.code_hash IS 'コードハッシュ: リカバリーコード（80ビットの乱数）のSHA-256（16進数）。平文は発行時に一度だけ利用者へ表示し、保存しない';
COMMENT ON COLUMN common.recovery_code.created_by IS '作成者';
COMMENT ON COLUMN common.recovery_code.created_at IS '作成日時';
