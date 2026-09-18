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
