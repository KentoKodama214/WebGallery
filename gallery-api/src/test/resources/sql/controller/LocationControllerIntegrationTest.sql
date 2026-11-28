-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.location_mst
insert into common.location_mst values(DEFAULT, 1, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, '渋谷スクランブル交差点_管理用', '渋谷スクランブル交差点', '東京都渋谷区', 35.6812, 139.7671);
insert into common.location_mst values(DEFAULT, 1, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, '削除済みロケーション_管理用', '削除済みロケーション', '東京都新宿区', 35.6850, 139.7100);
insert into common.location_mst values(DEFAULT, 2, 1, 2, '2000-01-06 09:00:00 Asia/Tokyo', 2, '2000-01-06 09:00:00 Asia/Tokyo', false, '他人のロケーション_管理用', '他人のロケーション', '大阪府大阪市', 34.6937, 135.5023);
