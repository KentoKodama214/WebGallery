/* Drop Tables */
DROP TABLE IF EXISTS common.passkey_credential;


/* Create Tables */
CREATE TABLE common.passkey_credential
(
	-- パスキー番号
	passkey_no bigserial NOT NULL,
	-- アカウント番号
	account_no bigint NOT NULL,
	-- クレデンシャルID: 認証器が発行したクレデンシャルID（base64url）
	credential_id varchar(1400) NOT NULL UNIQUE,
	-- 認証器データ: 公開鍵（COSE）・AAGUID・クレデンシャルIDを含むattestedCredentialData（webauthn4jのバイナリ表現）
	attested_credential_data bytea NOT NULL,
	-- 署名カウンタ: 直近の認証で認証器が返した値。巻き戻りはクローンの疑いとして認証を拒否する（同期パスキーは常に0）
	sign_count bigint NOT NULL DEFAULT 0,
	-- 認証器の通信手段: internal/hybrid/usb等をカンマ区切りで保持する。未取得の場合は空文字
	transports varchar(200) NOT NULL DEFAULT '',
	-- バックアップ可能フラグ: 認証器データのBEフラグ（同期パスキーかどうか）
	is_backup_eligible boolean NOT NULL DEFAULT FALSE,
	-- バックアップ済みフラグ: 認証器データのBSフラグ。認証のたびに更新する
	is_backed_up boolean NOT NULL DEFAULT FALSE,
	-- ユーザー検証済みフラグ: 登録時にユーザー検証（生体認証・PIN）が行われたかどうか
	is_uv_initialized boolean NOT NULL DEFAULT FALSE,
	-- パスキー名: 利用者が管理画面で識別するための名前
	passkey_name varchar(50) NOT NULL,
	-- 最終使用日時: 登録直後は登録日時
	last_used_at timestamp with time zone NOT NULL DEFAULT NOW(),
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL DEFAULT NOW(),
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL DEFAULT NOW(),
	PRIMARY KEY (passkey_no)
);


/* Create Foreign Keys */
ALTER TABLE common.passkey_credential
	ADD FOREIGN KEY (account_no)
	REFERENCES common.account (account_no)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


/* Create Indexes */
-- アカウント単位のパスキー一覧（管理画面）・登録時のexcludeCredentialsの組み立てで使用する
CREATE INDEX idx_passkey_credential_account ON common.passkey_credential (account_no);


/* Comments */
COMMENT ON TABLE common.passkey_credential IS 'パスキー（WebAuthnクレデンシャル）';
COMMENT ON COLUMN common.passkey_credential.passkey_no IS 'パスキー番号';
COMMENT ON COLUMN common.passkey_credential.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.passkey_credential.credential_id IS 'クレデンシャルID: 認証器が発行したクレデンシャルID（base64url）';
COMMENT ON COLUMN common.passkey_credential.attested_credential_data IS '認証器データ: 公開鍵（COSE）・AAGUID・クレデンシャルIDを含むattestedCredentialData（webauthn4jのバイナリ表現）';
COMMENT ON COLUMN common.passkey_credential.sign_count IS '署名カウンタ: 直近の認証で認証器が返した値。巻き戻りはクローンの疑いとして認証を拒否する（同期パスキーは常に0）';
COMMENT ON COLUMN common.passkey_credential.transports IS '認証器の通信手段: internal/hybrid/usb等をカンマ区切りで保持する。未取得の場合は空文字';
COMMENT ON COLUMN common.passkey_credential.is_backup_eligible IS 'バックアップ可能フラグ: 認証器データのBEフラグ（同期パスキーかどうか）';
COMMENT ON COLUMN common.passkey_credential.is_backed_up IS 'バックアップ済みフラグ: 認証器データのBSフラグ。認証のたびに更新する';
COMMENT ON COLUMN common.passkey_credential.is_uv_initialized IS 'ユーザー検証済みフラグ: 登録時にユーザー検証（生体認証・PIN）が行われたかどうか';
COMMENT ON COLUMN common.passkey_credential.passkey_name IS 'パスキー名: 利用者が管理画面で識別するための名前';
COMMENT ON COLUMN common.passkey_credential.last_used_at IS '最終使用日時: 登録直後は登録日時';
COMMENT ON COLUMN common.passkey_credential.created_by IS '作成者';
COMMENT ON COLUMN common.passkey_credential.created_at IS '作成日時';
COMMENT ON COLUMN common.passkey_credential.updated_by IS '更新者';
COMMENT ON COLUMN common.passkey_credential.updated_at IS '更新日時';
