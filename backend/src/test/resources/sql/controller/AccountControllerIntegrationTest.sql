-- common.kbn_mst
insert into common.kbn_mst values('prefecture', 'Hokkaido',     0, '2000-01-01 09:00:00 Asia/Tokyo', 1,  'Hokkaido_Tohoku', '都道府県', '北海道・東北', '北海道', 'prefecture', 'Hokkaido_Tohoku', 'Hokkaido', '');
insert into common.kbn_mst values('prefecture', 'Aomori',       0, '2000-01-01 09:00:00 Asia/Tokyo', 2,  'Hokkaido_Tohoku', '都道府県', '北海道・東北', '青森',   'prefecture', 'Hokkaido_Tohoku', 'Aomori', '');
insert into common.kbn_mst values('prefecture', 'Kagoshima',    0, '2000-01-01 09:00:00 Asia/Tokyo', 46, 'Kyushu_Okinawa',  '都道府県', '九州・沖縄',   '鹿児島', 'prefecture', 'Kyushu_Okinawa',  'Kagoshima', '');
insert into common.kbn_mst values('prefecture', 'Okinawa',      0, '2000-01-01 09:00:00 Asia/Tokyo', 47, 'Kyushu_Okinawa',  '都道府県', '九州・沖縄',   '沖縄',   'prefecture', 'Kyushu_Okinawa',  'Okinawa', '');

-- common.account
insert into common.account values(1,  1,  '2000-01-01 09:00:00 Asia/Tokyo', 1,  '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1',  '1991-02-14', 'none', 'none',     'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2,  2,  '2000-01-02 09:00:00 Asia/Tokyo', 2,  '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2',  '1900-01-01', 'man',  'none',     'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(3,  3,  '2000-01-03 09:00:00 Asia/Tokyo', 3,  '2001-01-03 09:00:00 Asia/Tokyo', false, 'cccccccc', 'CCCCCCCC', '$2a$10$password3',  '1900-01-01', 'none', 'Hokkaido', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);