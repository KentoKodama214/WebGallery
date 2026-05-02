-- common.account
insert into common.account values(1,  1,  '2000-01-01 09:00:00 Asia/Tokyo', 1,  '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none',  'none',     'none',    '',         'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2,  2,  '2000-01-02 09:00:00 Asia/Tokyo', 2,  '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',   'none',     'none',    '',         'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(3,  3,  '2000-01-03 09:00:00 Asia/Tokyo', 3,  '2001-01-03 09:00:00 Asia/Tokyo', false, 'cccccccc', 'CCCCCCCC', '$2a$10$password3', '1900-01-01', 'none',  'Hokkaido', 'none',    '',         'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(4,  4,  '2000-01-04 09:00:00 Asia/Tokyo', 4,  '2001-01-04 09:00:00 Asia/Tokyo', false, 'dddddddd', 'DDDDDDDD', '$2a$10$password4', '1900-01-01', 'none',  'none',     'Okinawa', '',         'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(5,  5,  '2000-01-05 09:00:00 Asia/Tokyo', 5,  '2001-01-05 09:00:00 Asia/Tokyo', false, 'eeeeeeee', 'EEEEEEEE', '$2a$10$password5', '1900-01-01', 'none',  'none',     'none',    'フリーメモ', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- common.location_mst
insert into common.location_mst values(DEFAULT, 1, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション1', '住所1', 38.100, 115.100);
insert into common.location_mst values(DEFAULT, 1, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション2', '住所2', 38.200, 115.200);
insert into common.location_mst values(DEFAULT, 1, 3, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, 'ロケーション3', '住所3', 38.300, 115.300);
insert into common.location_mst values(DEFAULT, 2, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション4', '住所4', 38.400, 115.400);
insert into common.location_mst values(DEFAULT, 2, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, 'ロケーション5', '住所5', 38.500, 115.500);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC111.jpg', 'タイトル11', 'title11', 'キャプション11', 'vertical',   24, 8.0,  1,  100);
insert into photo.photo_mst values(DEFAULT, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-02-01 09:00:00 Asia/Tokyo', 2, 'https://www.xxx.com/DSC222.jpg', 'タイトル12', 'title12', 'キャプション12', 'horizontal', 36, 9.0,  2,  200);
insert into photo.photo_mst values(DEFAULT, 1, 3, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true,  '2021-03-01 09:00:00 Asia/Tokyo', 3, 'https://www.xxx.com/DSC333.jpg', 'タイトル13', 'title13', 'キャプション13', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2022-01-01 09:00:00 Asia/Tokyo', 4, 'https://www.xxx.com/DSC444.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 80, 12.0, 5,  800);
insert into photo.photo_mst values(DEFAULT, 2, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2022-02-01 09:00:00 Asia/Tokyo', 5, 'https://www.xxx.com/DSC555.jpg', 'タイトル22', 'title22', 'キャプション22', 'horizontal', 90, 16.0, 10, 1200);

-- photo.photo_favorite
insert into photo.photo_favorite values(DEFAULT, 1, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 2, 1, 1, now());