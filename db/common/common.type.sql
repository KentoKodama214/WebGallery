/* Create Type */
-- 性別
CREATE TYPE common.sex_enum AS ENUM ('man', 'woman', 'none');
COMMENT ON TYPE common.sex_enum IS '性別を管理するEnum型: man(男性)、woman(女性)、none(未設定)';

-- 権限
CREATE TYPE common.authority_enum AS ENUM ('mini-user', 'normal-user', 'special-user', 'administrator');
COMMENT ON TYPE common.authority_enum IS '権限を管理するEnum型: mini-user(簡易ユーザー)、normal-user(一般ユーザー)、special-user(特別ユーザー)、administrator(管理者)';

-- お問い合わせステータス
CREATE TYPE common.inquiry_status_enum AS ENUM ('unreplied', 'replied', 'withdrawn');
COMMENT ON TYPE common.inquiry_status_enum IS 'お問い合わせステータスを管理するEnum型: unreplied(未対応)、replied(回答済み)、withdrawn(取り下げ)';

-- ログイン方式
CREATE TYPE common.login_method_enum AS ENUM ('passkey', 'recovery_code');
COMMENT ON TYPE common.login_method_enum IS 'ログイン方式を管理するEnum型: passkey(パスキー)、recovery_code(リカバリーコードによる復旧)';

-- WebAuthnチャレンジの用途
CREATE TYPE common.webauthn_challenge_purpose_enum AS ENUM ('registration', 'authentication', 'signup', 'reauthentication', 'recovery');
COMMENT ON TYPE common.webauthn_challenge_purpose_enum IS 'WebAuthnチャレンジの用途を管理するEnum型: registration(パスキー追加登録)、authentication(パスキー認証)、signup(アカウント新規登録)、reauthentication(重要操作前の本人確認)、recovery(リカバリーコードによる復旧)';
