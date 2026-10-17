-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- photo.photo_mst (account_no=1の写真3件（写真番号3は論理削除済み）、account_no=2の写真1件)
insert into photo.photo_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 0, '1/1-aaa.jpg', 'DSC11.jpg', '金閣寺', 'Kinkakuji', '', 'horizontal', 24, 8.0, 1, 100, false);
insert into photo.photo_mst values(2, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-02-01 09:00:00 Asia/Tokyo', 0, '1/2-bbb.jpg', 'DSC12.jpg', '原山の茶畑', 'Harayama', '', 'square', 36, 9.0, 2, 200, false);
insert into photo.photo_mst values(3, 1, 3, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true, '2021-03-01 09:00:00 Asia/Tokyo', 0, '1/3-ccc.jpg', 'DSC13.jpg', '削除済み', 'deleted', '', 'vertical', 50, 2.8, 1, 400, false);
insert into photo.photo_mst values(4, 2, 1, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 0, '2/1-ddd.jpg', 'DSC21.jpg', '橋杭岩', 'Hashiguiiwa', '', 'horizontal', 24, 8.0, 1, 100, false);
select setval('photo.photo_mst_id_seq', (select max(id) from photo.photo_mst));

-- photo.portfolio_mst
-- account_no=1：公開中のフィーチャー型（番号1。写真1・写真2）、下書きのグリッド型（番号2。行の枚数2に対し写真1枚のため公開できない）
-- account_no=2：下書きのフィーチャー型（番号1）
insert into photo.portfolio_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, '日本の自然', '日本各地の風景', 'feature', '{}', 1, 'published');
insert into photo.portfolio_mst values(2, 1, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '2001-02-01 09:00:00 Asia/Tokyo', false, '光と影', '', 'grid', '{"rows": [{"pattern": "TWO_EQUAL"}]}', 1, 'draft');
insert into photo.portfolio_mst values(3, 2, 1, 2, '2000-01-03 09:00:00 Asia/Tokyo', 2, '2001-01-03 09:00:00 Asia/Tokyo', false, '海の風景', '', 'feature', '{}', 1, 'draft');
select setval('photo.portfolio_mst_id_seq', (select max(id) from photo.portfolio_mst));

-- photo.portfolio_photo
insert into photo.portfolio_photo values(1, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '{"theme": "日本の自然", "subTheme": "京都", "description": "金色に輝く"}');
insert into photo.portfolio_photo values(2, 1, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 2, '{"theme": "日本の動物・植物"}');
insert into photo.portfolio_photo values(3, 1, 2, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '{"caption": "原山の茶畑"}');
insert into photo.portfolio_photo values(4, 2, 1, 1, 2, '2000-01-03 09:00:00 Asia/Tokyo', 1, '{}');
select setval('photo.portfolio_photo_id_seq', (select max(id) from photo.portfolio_photo));

-- photo.portfolio_tag_mst
insert into photo.portfolio_tag_mst values(1, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', '自然', 'nature');
select setval('photo.portfolio_tag_mst_id_seq', (select max(id) from photo.portfolio_tag_mst));
