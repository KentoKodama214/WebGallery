-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- photo.photo_list_filter_log
insert into photo.photo_list_filter_log values(1, 1, 1, 'vertical', false, '風景', 'photo_at', 'https://example.com/', '203.0.113.1', 'JP', 'Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo');
insert into photo.photo_list_filter_log values(2, 1, 2, 'horizontal', true, '', 'favorite', '', '203.0.113.2', 'JP', 'Osaka', 1, '2000-01-02 09:00:00 Asia/Tokyo');
insert into photo.photo_list_filter_log values(3, 2, 0, 'none', false, '', 'season', '', '203.0.113.3', '', '', 2, '2000-01-03 09:00:00 Asia/Tokyo');
