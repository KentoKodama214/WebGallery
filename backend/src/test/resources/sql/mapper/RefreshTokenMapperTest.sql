-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.refresh_token
insert into common.refresh_token values(1, 1, 'hash1', '2099-01-01 09:00:00 Asia/Tokyo', '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false);
insert into common.refresh_token values(2, 1, 'hash2', '2099-01-01 09:00:00 Asia/Tokyo', '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false);
insert into common.refresh_token values(3, 2, 'hash3', '2099-01-01 09:00:00 Asia/Tokyo', '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false);
insert into common.refresh_token values(4, 1, 'hash4', '2000-01-01 09:00:00 Asia/Tokyo', '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false);
insert into common.refresh_token values(5, 1, 'hash5', '2099-01-01 09:00:00 Asia/Tokyo', '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', true);
