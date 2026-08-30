/* Drop Tables */
DROP TABLE IF EXISTS common.account;


/* Create Tables */
CREATE TABLE common.account
(
	-- アカウント番号
	account_no bigserial NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- 削除フラグ
	is_deleted boolean DEFAULT 'false' NOT NULL,
	-- アカウントID: 8文字以上20文字以下、半角英数字
	account_id varchar(20) NOT NULL UNIQUE,
	-- アカウント名
	account_name varchar(50) NOT NULL,
	-- パスワード
	password text NOT NULL,
	-- 生年月日: 個人情報管理の観点で、必須入力なし、かつ年月まで。データ登録時にすべて1日に変換する
	birthdate date DEFAULT '1900-01-01' NOT NULL,
	-- 性別区分:
	-- none:未設定
	-- man:男
	-- woman:女
	sex_kbn common.sex_enum DEFAULT 'none' NOT NULL,
	-- 出身都道府県区分コード:
	-- none:未設定（画面上、任意入力）
	-- 北海道　〜　沖縄
	birthplace_prefecture_kbn_code varchar(20) DEFAULT 'none' NOT NULL,
	-- 在住都道府県区分コード:
	-- none:未設定（画面上、任意入力）
	-- 北海道　〜　沖縄
	resident_prefecture_kbn_code varchar(20) DEFAULT 'none' NOT NULL,
	-- フリーメモ
	free_memo text DEFAULT '""' NOT NULL,
	-- 権限区分:
	-- mini-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は10枚（上限50MB）
	-- normal-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は1000枚（上限5GB）
	-- special-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は無制限
	-- administrator: サイトを管理・運営する人。写真登録の上限は無制限
	authority_kbn common.authority_enum NOT NULL,
	-- 最終ログイン日時
	last_login_datetime timestamp with time zone NOT NULL,
	-- ログイン失敗回数
	login_failure_count smallint DEFAULT 0 NOT NULL,
	PRIMARY KEY (account_no)
) WITHOUT OIDS;


/* Comments */
COMMENT ON TABLE common.account IS 'アカウント';
COMMENT ON COLUMN common.account.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.account.created_by IS '作成者';
COMMENT ON COLUMN common.account.created_at IS '作成日時';
COMMENT ON COLUMN common.account.updated_by IS '更新者';
COMMENT ON COLUMN common.account.updated_at IS '更新日時';
COMMENT ON COLUMN common.account.is_deleted IS '削除フラグ';
COMMENT ON COLUMN common.account.account_id IS 'アカウントID : 8文字以上20文字以下、半角英数字';
COMMENT ON COLUMN common.account.account_name IS 'アカウント名';
COMMENT ON COLUMN common.account.password IS 'パスワード';
COMMENT ON COLUMN common.account.birthdate IS '生年月日 : 個人情報管理の観点で、必須入力なし、かつ年月まで。データ登録時にすべて1日に変換する';
COMMENT ON COLUMN common.account.sex_kbn IS '性別区分: man(男性)、woman(女性)、none(未設定)';
COMMENT ON COLUMN common.account.birthplace_prefecture_kbn_code IS '出身都道府県区分コード : none:未設定（画面上、任意入力）北海道　〜　沖縄';
COMMENT ON COLUMN common.account.resident_prefecture_kbn_code IS '在住都道府県区分コード : none:未設定（画面上、任意入力）北海道　〜　沖縄';
COMMENT ON COLUMN common.account.free_memo IS 'フリーメモ';
COMMENT ON COLUMN common.account.authority_kbn IS '権限区分: mini-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は10枚（上限50MB）)、normal-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は1000枚（上限5GB）)、special-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は無制限)、administrator(サイトを管理・運営する人。写真登録の上限は無制限)';
COMMENT ON COLUMN common.account.last_login_datetime IS '最終ログイン日時';
