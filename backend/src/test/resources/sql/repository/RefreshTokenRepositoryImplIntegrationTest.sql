insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;

-- アカウント1の有効なトークン
insert into common.refresh_token (token_id, account_no, token_hash, expires_at, created_at, updated_by, updated_at, is_revoked) values(1, 1, 'valid_token_hash_1', '2099-12-31 00:00:00 Asia/Tokyo', '2024-01-01 00:00:00 Asia/Tokyo', 1, '2024-01-01 00:00:00 Asia/Tokyo', false);
-- アカウント1の無効化済みトークン
insert into common.refresh_token (token_id, account_no, token_hash, expires_at, created_at, updated_by, updated_at, is_revoked) values(2, 1, 'revoked_token_hash_1', '2099-12-31 00:00:00 Asia/Tokyo', '2024-01-01 00:00:00 Asia/Tokyo', 1, '2024-01-01 00:00:00 Asia/Tokyo', true);
-- アカウント1の有効期限切れトークン
insert into common.refresh_token (token_id, account_no, token_hash, expires_at, created_at, updated_by, updated_at, is_revoked) values(3, 1, 'expired_token_hash_1', '2020-01-01 00:00:00 Asia/Tokyo', '2019-01-01 00:00:00 Asia/Tokyo', 1, '2019-01-01 00:00:00 Asia/Tokyo', false);
-- アカウント2の有効なトークン
insert into common.refresh_token (token_id, account_no, token_hash, expires_at, created_at, updated_by, updated_at, is_revoked) values(4, 2, 'valid_token_hash_2', '2099-12-31 00:00:00 Asia/Tokyo', '2024-01-01 00:00:00 Asia/Tokyo', 2, '2024-01-01 00:00:00 Asia/Tokyo', false);
-- アカウント1の有効なトークン（2つ目）
insert into common.refresh_token (token_id, account_no, token_hash, expires_at, created_at, updated_by, updated_at, is_revoked) values(5, 1, 'valid_token_hash_1b', '2099-12-31 00:00:00 Asia/Tokyo', '2024-01-02 00:00:00 Asia/Tokyo', 1, '2024-01-02 00:00:00 Asia/Tokyo', false);
ALTER SEQUENCE common.refresh_token_token_id_seq RESTART 6;
