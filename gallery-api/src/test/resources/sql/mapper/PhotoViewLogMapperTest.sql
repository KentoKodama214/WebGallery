-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC111.jpg', 'DSC111.jpg', 'タイトル11', 'title11', 'キャプション11', 'vertical', 24, 8.0, 1, 100, true);
insert into photo.photo_mst values(DEFAULT, 2, 1, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2000-01-02 09:00:00 Asia/Tokyo', false, '2021-02-01 09:00:00 Asia/Tokyo', 2, 'https://www.xxx.com/DSC222.jpg', 'DSC222.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 36, 9.0, 2, 200, true);

-- photo.photo_view_log
insert into photo.photo_view_log values(1, 1, 1, 1, 'https://example.com/', '203.0.113.1', 'JP', 'Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo');
insert into photo.photo_view_log values(2, 1, 1, 0, '', '203.0.113.2', '', '', 1, '2000-01-02 09:00:00 Asia/Tokyo');
insert into photo.photo_view_log values(3, 2, 1, 2, '', '203.0.113.3', '', '', 2, '2000-01-03 09:00:00 Asia/Tokyo');
