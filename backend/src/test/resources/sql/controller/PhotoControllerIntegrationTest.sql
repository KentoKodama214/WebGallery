-- common.account
insert into common.account values(1,  1,  '2000-01-01 09:00:00 Asia/Tokyo', 1,  '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'mini-user', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2,  2,  '2000-01-02 09:00:00 Asia/Tokyo', 2,  '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',  'none', 'none', '', 'mini-user', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(3,  3,  '2000-01-03 09:00:00 Asia/Tokyo', 3,  '2001-01-03 09:00:00 Asia/Tokyo', false, 'cccccccc', 'CCCCCCCC', '$2a$10$password3', '1900-01-01', 'man',  'none', 'none', '', 'normal-user', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(4,  4,  '2000-01-04 09:00:00 Asia/Tokyo', 4,  '2001-01-04 09:00:00 Asia/Tokyo', false, 'dddddddd', 'DDDDDDDD', '$2a$10$password4', '1900-01-01', 'man',  'none', 'none', '', 'normal-user', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(5,  5,  '2000-01-05 09:00:00 Asia/Tokyo', 5,  '2001-01-05 09:00:00 Asia/Tokyo', false, 'eeeeeeee', 'EEEEEEEE', '$2a$10$password5', '1900-01-01', 'man',  'none', 'none', '', 'special-user', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(6,  6,  '2000-01-06 09:00:00 Asia/Tokyo', 6,  '2001-01-06 09:00:00 Asia/Tokyo', false, 'ffffffff', 'FFFFFFFF', '$2a$10$password6', '1900-01-01', 'man',  'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- common.location_mst
insert into common.location_mst values(DEFAULT, 1, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション1', '住所1', 38.100, 115.100);
insert into common.location_mst values(DEFAULT, 1, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション2', '住所2', 38.200, 115.200);
insert into common.location_mst values(DEFAULT, 1, 3, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, 'ロケーション3', '住所3', 38.300, 115.300);
insert into common.location_mst values(DEFAULT, 2, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, 'ロケーション4', '住所4', 38.400, 115.400);
insert into common.location_mst values(DEFAULT, 2, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, 'ロケーション5', '住所5', 38.500, 115.500);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1,  'https://www.xxx.com/aaaaaaaa/DSC11.jpg', 'タイトル11', 'title11', 'キャプション11', 'horizontal', 24, 8.0,  1,  100);
insert into photo.photo_mst values(DEFAULT, 1, 2,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '1900-01-01 00:00:00 Asia/Tokyo', 0,  'https://www.xxx.com/aaaaaaaa/DSC12.jpg', '', '', '', 'horizontal', 0, 0, 0, 0);
insert into photo.photo_mst values(DEFAULT, 1, 3,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-03-01 09:00:00 Asia/Tokyo', 3,  'https://www.xxx.com/aaaaaaaa/DSC13.jpg', 'タイトル13', 'title13', 'キャプション13', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 4,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2022-04-01 09:00:00 Asia/Tokyo', 4,  'https://www.xxx.com/aaaaaaaa/DSC14.jpg', 'タイトル14', 'title14', 'キャプション14', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 5,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2022-05-01 09:00:00 Asia/Tokyo', 5,  'https://www.xxx.com/aaaaaaaa/DSC15.jpg', 'タイトル15', 'title15', 'キャプション15', 'vertical', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 6,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2022-06-01 09:00:00 Asia/Tokyo', 6,  'https://www.xxx.com/aaaaaaaa/DSC16.jpg', 'タイトル16', 'title16', 'キャプション16', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 7,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2023-07-01 09:00:00 Asia/Tokyo', 7,  'https://www.xxx.com/aaaaaaaa/DSC17.jpg', 'タイトル17', 'title17', 'キャプション17', 'vertical', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 8,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2023-08-01 09:00:00 Asia/Tokyo', 8,  'https://www.xxx.com/aaaaaaaa/DSC18.jpg', 'タイトル18', 'title18', 'キャプション18', 'vertical', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 9,  1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2023-09-01 09:00:00 Asia/Tokyo', 9,  'https://www.xxx.com/aaaaaaaa/DSC19.jpg', 'タイトル19', 'title19', 'キャプション19', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 1, 10, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-10-01 09:00:00 Asia/Tokyo', 10, 'https://www.xxx.com/aaaaaaaa/DSC20.jpg', 'タイトル20', 'title20', 'キャプション20', 'horizontal', 50, 10.0, 3,  400);

insert into photo.photo_mst values(DEFAULT, 2, 1,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1,  'https://www.xxx.com/DSC21.jpg', 'タイトル11', 'title11', 'キャプション11', 'horizontal', 24, 8.0,  1,  100);
insert into photo.photo_mst values(DEFAULT, 2, 2,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-02-01 09:00:00 Asia/Tokyo', 2,  'https://www.xxx.com/DSC22.jpg', 'タイトル12', 'title12', 'キャプション12', 'horizontal', 36, 9.0,  2,  200);
insert into photo.photo_mst values(DEFAULT, 2, 3,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-03-01 09:00:00 Asia/Tokyo', 3,  'https://www.xxx.com/DSC23.jpg', 'タイトル13', 'title13', 'キャプション13', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 4,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-04-01 09:00:00 Asia/Tokyo', 4,  'https://www.xxx.com/DSC24.jpg', 'タイトル14', 'title14', 'キャプション14', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 5,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-05-01 09:00:00 Asia/Tokyo', 5,  'https://www.xxx.com/DSC25.jpg', 'タイトル15', 'title15', 'キャプション15', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 6,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-06-01 09:00:00 Asia/Tokyo', 6,  'https://www.xxx.com/DSC26.jpg', 'タイトル16', 'title16', 'キャプション16', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 7,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-07-01 09:00:00 Asia/Tokyo', 7,  'https://www.xxx.com/DSC27.jpg', 'タイトル17', 'title17', 'キャプション17', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 8,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-08-01 09:00:00 Asia/Tokyo', 8,  'https://www.xxx.com/DSC28.jpg', 'タイトル18', 'title18', 'キャプション18', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 9,  2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-09-01 09:00:00 Asia/Tokyo', 9,  'https://www.xxx.com/DSC29.jpg', 'タイトル19', 'title19', 'キャプション19', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 10, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', true,   '2021-10-01 09:00:00 Asia/Tokyo', 10, 'https://www.xxx.com/DSC30.jpg', 'タイトル20', 'title20', 'キャプション20', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 2, 11, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-11-01 09:00:00 Asia/Tokyo', 11, 'https://www.xxx.com/DSC31.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 50, 10.0, 3,  400);

insert into photo.photo_mst values(DEFAULT, 3, 1,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3001.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 24, 8.0,  1,  100);
insert into photo.photo_mst values(DEFAULT, 3, 2,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3002.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 36, 9.0,  2,  200);
insert into photo.photo_mst values(DEFAULT, 3, 3,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3003.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 4,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3004.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 5,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3005.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 6,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3006.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 7,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3007.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 8,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3008.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 9,  3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,  '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3009.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 10, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3010.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 11, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3011.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 12, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3012.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 13, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3013.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 14, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3014.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 15, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3015.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 16, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3016.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 17, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3017.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 18, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3018.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 19, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3019.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);
insert into photo.photo_mst values(DEFAULT, 3, 20, 3, '2000-01-01 09:00:00 Asia/Tokyo', 3, '2000-01-01 09:00:00 Asia/Tokyo', false,   '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/DSC3020.jpg', 'タイトル', 'title', 'キャプション', 'horizontal', 50, 10.0, 3,  400);

-- photo.photo_favorite
insert into photo.photo_favorite values(DEFAULT, 1, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 1, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 1, 2, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 3, 1, now());
insert into photo.photo_favorite values(DEFAULT, 3, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 3, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 3, 1, 3, 1, now());
insert into photo.photo_favorite values(DEFAULT, 4, 1, 2, 1, now());
insert into photo.photo_favorite values(DEFAULT, 4, 1, 3, 1, now());

-- photo.photo_tag
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '太陽', 'sun');
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 2, 1, '2000-01-01 11:00:00 Asia/Tokyo', '青空', 'bluesky');
insert into photo.photo_tag_mst values(DEFAULT, 1, 2, 1, 1, '2000-02-01 10:00:00 Asia/Tokyo', '太陽', 'sun');
insert into photo.photo_tag_mst values(DEFAULT, 1, 2, 2, 1, '2000-02-01 11:00:00 Asia/Tokyo', '曇天', 'cloudy');
insert into photo.photo_tag_mst values(DEFAULT, 1, 2, 3, 1, '2000-02-01 12:00:00 Asia/Tokyo', '花',   'flower');
insert into photo.photo_tag_mst values(DEFAULT, 1, 3, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '太陽', 'sun');
insert into photo.photo_tag_mst values(DEFAULT, 2, 1, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '太陽', 'sun');
