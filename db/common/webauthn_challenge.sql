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
	-- registration:パスキー追加登録
	-- authentication:パスキー認証
	-- signup:アカウント新規登録
	-- reauthentication:重要操作前の本人確認
	-- recovery:リカバリーコードによる復旧
	purpose_kbn common.webauthn_challenge_purpose_enum NOT NULL,
	-- アカウント番号: 対象アカウントの番号。認証用・新規登録用はアカウントが未確定（未作成）のため0
	account_no bigint NOT NULL,
	-- ユーザーハンドル: 新規登録用のみ、登録オプションで払い出したWebAuthnのuser.id（base64url）。それ以外は空文字
	user_handle varchar(100) NOT NULL DEFAULT '',
	-- 有効期限
	expires_at timestamp with time zone NOT NULL,
	-- 作成者: アカウント番号（認証用・新規登録用は0）
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL DEFAULT NOW(),
	PRIMARY KEY (challenge_no)
);


/* Create Indexes */
-- 期限切れチャレンジを削除する定期実行タスク（expires_at < NOW()）の全走査を回避する
CREATE INDEX idx_webauthn_challenge_expires_at ON common.webauthn_challenge (expires_at);


/* Comments */
-- account_noは認証用・新規登録用チャレンジで0（未確定）を取るため外部キーを張らない（photo_view_log.account_noと同じ扱い）。
-- アカウントに紐づくチャレンジの残骸は退会時にAccountAggregateRepositoryImpl#deleteで明示的に削除する
COMMENT ON TABLE common.webauthn_challenge IS 'WebAuthnチャレンジ（パスキーの登録・認証・本人確認で発行する使い捨ての乱数）';
COMMENT ON COLUMN common.webauthn_challenge.challenge_no IS 'チャレンジNo';
COMMENT ON COLUMN common.webauthn_challenge.challenge IS 'チャレンジ: サーバ生成の32バイト乱数（base64url）。検証時に1度だけ消費（削除）する';
COMMENT ON COLUMN common.webauthn_challenge.purpose_kbn IS '用途区分: registration(パスキー追加登録)、authentication(パスキー認証)、signup(アカウント新規登録)、reauthentication(重要操作前の本人確認)、recovery(リカバリーコードによる復旧)';
COMMENT ON COLUMN common.webauthn_challenge.account_no IS 'アカウント番号: 対象アカウントの番号。認証用・新規登録用はアカウントが未確定（未作成）のため0（外部キーなし）';
COMMENT ON COLUMN common.webauthn_challenge.user_handle IS 'ユーザーハンドル: 新規登録用のみ、登録オプションで払い出したWebAuthnのuser.id（base64url）。それ以外は空文字';
COMMENT ON COLUMN common.webauthn_challenge.expires_at IS '有効期限';
COMMENT ON COLUMN common.webauthn_challenge.created_by IS '作成者: アカウント番号（認証用・新規登録用は0）';
COMMENT ON COLUMN common.webauthn_challenge.created_at IS '作成日時';
