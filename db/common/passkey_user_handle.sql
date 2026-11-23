/* Drop Tables */
DROP TABLE IF EXISTS common.passkey_user_handle;


/* Create Tables */
CREATE TABLE common.passkey_user_handle
(
	-- パスキーユーザーハンドルNo
	passkey_user_handle_no bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL UNIQUE,
	-- ユーザーハンドル: WebAuthnのuser.id。サーバ生成の乱数（base64url）。アカウントIDのような変更されうる値や個人情報を含めない
	user_handle varchar(100) NOT NULL UNIQUE,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL DEFAULT NOW(),
	PRIMARY KEY (passkey_user_handle_no)
);


/* Create Foreign Keys */
ALTER TABLE common.passkey_user_handle
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Comments */
COMMENT ON TABLE common.passkey_user_handle IS 'パスキーユーザーハンドル（アカウントごとに1件。最初のパスキー登録時に発行する）';
COMMENT ON COLUMN common.passkey_user_handle.passkey_user_handle_no IS 'パスキーユーザーハンドルNo';
COMMENT ON COLUMN common.passkey_user_handle.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.passkey_user_handle.user_handle IS 'ユーザーハンドル: WebAuthnのuser.id。サーバ生成の乱数（base64url）。アカウントIDのような変更されうる値や個人情報を含めない';
COMMENT ON COLUMN common.passkey_user_handle.created_by IS '作成者';
COMMENT ON COLUMN common.passkey_user_handle.created_at IS '作成日時';
