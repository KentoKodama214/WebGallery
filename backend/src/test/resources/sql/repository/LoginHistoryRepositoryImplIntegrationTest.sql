insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;

insert into common.login_history (login_history_no, account_no, is_success, ip_address, country, region, created_by, created_at) values (1, 2, true, '198.51.100.1', 'US', 'California', 2, '2024-01-01 00:00:00 Asia/Tokyo');
ALTER SEQUENCE common.login_history_login_history_no_seq RESTART 2;
