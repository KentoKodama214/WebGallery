-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man',  'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);

-- photo.photo_mst (account_no=1の写真2件、account_no=2の写真1件)
insert into photo.photo_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/aaaaaaaa/DSC11.jpg', 'DSC11.jpg', 'タイトル11', 'title11', 'キャプション11', 'horizontal', 24, 8.0, 1, 100);
insert into photo.photo_mst values(DEFAULT, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-02-01 09:00:00 Asia/Tokyo', 2, 'https://www.xxx.com/aaaaaaaa/DSC12.jpg', 'DSC12.jpg', 'タイトル12', 'title12', 'キャプション12', 'horizontal', 36, 9.0, 2, 200);
insert into photo.photo_mst values(DEFAULT, 2, 1, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1, 'https://www.xxx.com/bbbbbbbb/DSC21.jpg', 'DSC21.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 24, 8.0, 1, 100);

-- photo.photo_tag_mst (account_no=1の写真タグ)
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '太陽', 'sun');
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 2, 1, '2000-01-01 11:00:00 Asia/Tokyo', '青空', 'bluesky');
insert into photo.photo_tag_mst values(DEFAULT, 1, 2, 1, 1, '2000-02-01 10:00:00 Asia/Tokyo', '太陽', 'sun');

-- photo.photo_favorite
-- account_no=1が自分の写真をお気に入り
insert into photo.photo_favorite values(DEFAULT, 1, 1, 1, 1, now());
-- account_no=2がaccount_no=1の写真をお気に入り
insert into photo.photo_favorite values(DEFAULT, 2, 1, 1, 1, now());
-- account_no=1がaccount_no=2の写真をお気に入り
insert into photo.photo_favorite values(DEFAULT, 1, 2, 1, 1, now());
-- account_no=2が自分の写真をお気に入り
insert into photo.photo_favorite values(DEFAULT, 2, 2, 1, 1, now());

-- common.refresh_token (account_no=1、account_no=2それぞれ有効なリフレッシュトークン)
insert into common.refresh_token values(DEFAULT, 1, 'hash-account1', now() + interval '7 days', now(), 1, now(), false);
insert into common.refresh_token values(DEFAULT, 2, 'hash-account2', now() + interval '7 days', now(), 2, now(), false);
