insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;

insert into common.location_mst values(DEFAULT, 1, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション1', '住所1', 38.100, 115.100);
insert into common.location_mst values(DEFAULT, 2, 1, 2, '2000-01-05 09:00:00 Asia/Tokyo', 2, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション2', '住所2', 38.200, 115.200);

insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC111.jpg', 'DSC111.jpg', 'タイトル11', 'title11', 'キャプション11', 'vertical', 24, 8.0, 1, 100, true);
insert into photo.photo_mst values(DEFAULT, 2, 1, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 2, 'https://www.xxx.com/DSC222.jpg', 'DSC222.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 50, 4.0, 2, 200, true);

insert into photo.photo_view_log (photo_view_log_no, photo_account_no, photo_no, referer, ip_address, country, region, created_by, created_at) values (1, 2, 1, 'https://example.com/', '198.51.100.1', 'US', 'California', 2, '2024-01-01 00:00:00 Asia/Tokyo');
ALTER SEQUENCE photo.photo_view_log_photo_view_log_no_seq RESTART 2;
