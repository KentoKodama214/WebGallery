/* Drop Tables */
DROP TABLE IF EXISTS common.webauthn_challenge;


/* Create Tables */
CREATE TABLE common.webauthn_challenge
(
	-- チャレンジNo
	challenge_no bigserial NOT NULL,
	-- チャレンジ: サーバ生成の32バイト乱数（base64url）。検証時に1度だけ消費（削除）する
	challenge varchar(100) NOT NULL UNIQUE,
	-- 用途区分:
	-- registration:パスキー登録
	-- authentication:パスキー認証
	purpose_kbn common.webauthn_challenge_purpose_enum NOT NULL,
	-- アカウント番号: 登録用は登録するアカウントの番号。認証用は認証前でアカウントが未確定のため0
	account_no bigint NOT NULL,
	-- 有効期限
	expires_at timestamp with time zone NOT NULL,
	-- 作成者: 登録用はアカウント番号、認証用は0
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL DEFAULT NOW(),
	PRIMARY KEY (challenge_no)
);


/* Create Indexes */
-- 期限切れチャレンジを削除する定期実行タスク（expires_at < NOW()）の全走査を回避する
CREATE INDEX idx_webauthn_challenge_expires_at ON common.webauthn_challenge (expires_at);


/* Comments */
-- account_noは認証用チャレンジで0（未確定）を取るため外部キーを張らない（photo_view_log.account_noと同じ扱い）。
-- 登録用チャレンジの残骸は退会時にAccountAggregateRepositoryImpl#deleteで明示的に削除する
COMMENT ON TABLE common.webauthn_challenge IS 'WebAuthnチャレンジ（パスキーの登録・認証で発行する使い捨ての乱数）';
COMMENT ON COLUMN common.webauthn_challenge.challenge_no IS 'チャレンジNo';
COMMENT ON COLUMN common.webauthn_challenge.challenge IS 'チャレンジ: サーバ生成の32バイト乱数（base64url）。検証時に1度だけ消費（削除）する';
COMMENT ON COLUMN common.webauthn_challenge.purpose_kbn IS '用途区分: registration(パスキー登録)、authentication(パスキー認証)';
COMMENT ON COLUMN common.webauthn_challenge.account_no IS 'アカウント番号: 登録用は登録するアカウントの番号。認証用は認証前でアカウントが未確定のため0（外部キーなし）';
COMMENT ON COLUMN common.webauthn_challenge.expires_at IS '有効期限';
COMMENT ON COLUMN common.webauthn_challenge.created_by IS '作成者: 登録用はアカウント番号、認証用は0';
COMMENT ON COLUMN common.webauthn_challenge.created_at IS '作成日時';
