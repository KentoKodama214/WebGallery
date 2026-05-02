-- common.account
insert into common.account values(1,  1,  '2000-01-01 09:00:00 Asia/Tokyo', 1,  '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2,  2,  '2000-01-02 09:00:00 Asia/Tokyo', 2,  '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',  'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, now(), 1, now(), false, '2021-01-01 00:00:00', 1, 'https://www.xxx.com/DSC111.jpg', 'タイトル1', '', 'キャプション1', 'horizontal', 24, 8.0, 5, 100);
insert into photo.photo_mst values(DEFAULT, 1, 2, 1, now(), 1, now(), false, '2021-02-01 00:00:00', 1, 'https://www.xxx.com/DSC222.jpg', 'タイトル2', '', 'キャプション2', 'horizontal', 24, 8.0, 5, 100);
insert into photo.photo_mst values(DEFAULT, 2, 1, 1, now(), 1, now(), false, '2021-01-01 00:00:00', 1, 'https://www.xxx.com/DSC333.jpg', 'タイトル1', '', 'キャプション3', 'horizontal', 24, 8.0, 5, 100);
insert into photo.photo_mst values(DEFAULT, 2, 2, 1, now(), 1, now(), false, '2021-02-01 00:00:00', 1, 'https://www.xxx.com/DSC444.jpg', 'タイトル2', '', 'キャプション4', 'horizontal', 24, 8.0, 5, 100);

-- photo.photo_favorite
insert into photo.photo_favorite values(DEFAULT, 1, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 1, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 2, 1, 1, now());