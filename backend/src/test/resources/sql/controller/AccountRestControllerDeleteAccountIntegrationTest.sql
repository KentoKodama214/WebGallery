-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',  'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- photo.photo_mst
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/aaaaaaaa/DSC11.jpg', 'DSC11.jpg', 'タイトル11', 'title11', 'キャプション11', 'horizontal', 24, 8.0, 1, 100);

-- photo.photo_tag_mst
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '太陽', 'sun');

-- photo.photo_favorite
insert into photo.photo_favorite values(DEFAULT, 1, 1, 1, 1, now());
insert into photo.photo_favorite values(DEFAULT, 2, 1, 1, 1, now());
