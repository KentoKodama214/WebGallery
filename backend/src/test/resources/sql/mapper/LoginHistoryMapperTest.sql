-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.login_history
insert into common.login_history values(1, 1, '203.0.113.1', 'JP', 'Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo');
insert into common.login_history values(2, 1, '203.0.113.2', 'JP', 'Osaka', 1, '2000-01-02 09:00:00 Asia/Tokyo');
insert into common.login_history values(3, 2, '203.0.113.3', '', '', 2, '2000-01-03 09:00:00 Asia/Tokyo');
