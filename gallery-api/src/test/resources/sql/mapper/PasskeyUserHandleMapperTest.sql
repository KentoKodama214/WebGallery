-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.passkey_user_handle（account_no=1のみ発行済み）
insert into common.passkey_user_handle (passkey_user_handle_no, account_no, user_handle, created_by, created_at) values (1, 1, 'handle-1', 1, '2003-01-01 09:00:00 Asia/Tokyo');
SELECT setval('common.passkey_user_handle_passkey_user_handle_no_seq', 1);
