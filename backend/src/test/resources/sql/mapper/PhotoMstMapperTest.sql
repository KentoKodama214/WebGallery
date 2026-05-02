-- common.account
insert into common.account values(1,  1,  '2000-01-01 09:00:00 Asia/Tokyo', 1,  '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2,  2,  '2000-01-02 09:00:00 Asia/Tokyo', 2,  '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',  'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC111.jpg', 'タイトル11', 'title11', 'キャプション11', 'vertical',   24, 8.0,  1,  100);
insert into photo.photo_mst values(DEFAULT, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true,  '2021-02-01 09:00:00 Asia/Tokyo', 2, 'https://www.xxx.com/DSC222.jpg', 'タイトル12', 'title12', 'キャプション12', 'horizontal', 36, 9.0,  2,  200);
insert into photo.photo_mst values(DEFAULT, 1, 3, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true,  '2021-03-01 09:00:00 Asia/Tokyo', 3, 'https://www.xxx.com/DSC333.jpg', 'タイトル13', 'title13', 'キャプション13', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2022-01-01 09:00:00 Asia/Tokyo', 4, 'https://www.xxx.com/DSC444.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 80, 12.0, 5,  800);
insert into photo.photo_mst values(DEFAULT, 2, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2022-02-01 09:00:00 Asia/Tokyo', 5, 'https://www.xxx.com/DSC555.jpg', 'タイトル22', 'title22', 'キャプション22', 'horizontal', 90, 16.0, 10, 1200);
insert into photo.photo_mst values(DEFAULT, 2, 3, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true,  '2022-03-01 09:00:00 Asia/Tokyo', 6, 'https://www.xxx.com/DSC555.jpg', 'タイトル23', 'title23', 'キャプション23', 'horizontal', 50, 10.0, 3,  400);
