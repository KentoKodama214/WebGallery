/* Drop Tables */
DROP TABLE IF EXISTS common.account_authority;


/* Create Tables */
CREATE TABLE common.account_authority
(
	-- アカウント番号
	account_no bigint NOT NULL,
	-- 作成者
	created_by bigint NOT NULL,
	-- 作成日時
	created_at timestamp with time zone NOT NULL,
	-- 更新者
	updated_by bigint NOT NULL,
	-- 更新日時
	updated_at timestamp with time zone NOT NULL,
	-- 権限区分:
	-- mini-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は10枚（上限50MB）
	-- normal-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は1000枚（上限5GB）
	-- special-user: サイトを閲覧したり、サービスを利用する人。写真登録の上限は無制限
	-- administrator: サイトを管理・運営する人。写真登録の上限は無制限
	authority_kbn common.authority_enum NOT NULL,
	PRIMARY KEY (account_no),
	FOREIGN KEY (account_no) REFERENCES common.account (account_no)
) WITHOUT OIDS;


/* Comments */
COMMENT ON TABLE common.account_authority IS 'アカウント権限';
COMMENT ON COLUMN common.account_authority.account_no IS 'アカウント番号';
COMMENT ON COLUMN common.account_authority.created_by IS '作成者';
COMMENT ON COLUMN common.account_authority.created_at IS '作成日時';
COMMENT ON COLUMN common.account_authority.updated_by IS '更新者';
COMMENT ON COLUMN common.account_authority.updated_at IS '更新日時';
COMMENT ON COLUMN common.account_authority.authority_kbn IS '権限区分: mini-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は10枚（上限50MB）)、normal-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は1000枚（上限5GB）)、special-user(サイトを閲覧したり、サービスを利用する人。写真登録の上限は無制限)、administrator(サイトを管理・運営する人。写真登録の上限は無制限)';
