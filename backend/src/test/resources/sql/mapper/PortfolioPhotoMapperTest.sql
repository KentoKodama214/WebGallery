-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(3, 3, '2000-01-03 09:00:00 Asia/Tokyo', 3, '2001-01-03 09:00:00 Asia/Tokyo', false, 'cccccccc', 'CCCCCCCC', '$2a$10$password3', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- photo.photo_mst (account_no=1の写真3件（写真番号3は論理削除済み）、account_no=2の写真1件)
insert into photo.photo_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 0, '1/1-aaa.jpg', 'DSC11.jpg', 'タイトル11', 'title11', 'キャプション11', 'horizontal', 24, 8.0, 1, 100, false);
insert into photo.photo_mst values(2, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-02-01 09:00:00 Asia/Tokyo', 0, '1/2-bbb.jpg', 'DSC12.jpg', 'タイトル12', 'title12', 'キャプション12', 'square', 36, 9.0, 2, 200, false);
insert into photo.photo_mst values(3, 1, 3, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2000-01-01 09:00:00 Asia/Tokyo', true, '2021-03-01 09:00:00 Asia/Tokyo', 0, '1/3-ccc.jpg', 'DSC13.jpg', 'タイトル13', 'title13', 'キャプション13', 'vertical', 50, 2.8, 1, 400, false);
insert into photo.photo_mst values(4, 2, 1, 2, '2000-01-01 09:00:00 Asia/Tokyo', 2, '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 0, '2/1-ddd.jpg', 'DSC21.jpg', 'タイトル21', 'title21', 'キャプション21', 'horizontal', 24, 8.0, 1, 100, false);
select setval('photo.photo_mst_id_seq', (select max(id) from photo.photo_mst));

-- photo.portfolio_mst
-- account_no=1：公開中のグリッド型（番号1）、下書きのフィーチャー型（番号2）、下書きのフィーチャー型（番号3、更新日時が最も新しい）
-- account_no=2：下書きのフィーチャー型（番号1）
insert into photo.portfolio_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', 'ポートフォリオ11', '説明11', 'grid', '{"rows": [{"pattern": "TWO_EQUAL"}]}', 1, 'published');
insert into photo.portfolio_mst values(2, 1, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '2001-02-01 09:00:00 Asia/Tokyo', 'ポートフォリオ12', '', 'feature', '{}', 1, 'draft');
insert into photo.portfolio_mst values(3, 1, 3, 1, '2000-01-03 09:00:00 Asia/Tokyo', 1, '2001-03-01 09:00:00 Asia/Tokyo', 'ポートフォリオ13', '説明13', 'feature', '{}', 1, 'draft');
insert into photo.portfolio_mst values(4, 2, 1, 2, '2000-01-04 09:00:00 Asia/Tokyo', 2, '2001-01-04 09:00:00 Asia/Tokyo', 'ポートフォリオ21', '説明21', 'feature', '{}', 1, 'draft');
select setval('photo.portfolio_mst_id_seq', (select max(id) from photo.portfolio_mst));

-- photo.portfolio_photo
-- account_no=1のポートフォリオ1：写真2→写真1の順、ポートフォリオ2：写真1、ポートフォリオ3：写真1
-- account_no=2のポートフォリオ1：写真1（account_no=2の写真）
insert into photo.portfolio_photo values(1, 1, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '{"caption": "キャプション12"}');
insert into photo.portfolio_photo values(2, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 2, '{}');
insert into photo.portfolio_photo values(3, 1, 2, 1, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '{"theme": "テーマ11"}');
insert into photo.portfolio_photo values(4, 1, 3, 1, 1, '2000-01-03 09:00:00 Asia/Tokyo', 1, '{}');
insert into photo.portfolio_photo values(5, 2, 1, 1, 2, '2000-01-04 09:00:00 Asia/Tokyo', 1, '{}');
select setval('photo.portfolio_photo_id_seq', (select max(id) from photo.portfolio_photo));

-- photo.portfolio_tag_mst
insert into photo.portfolio_tag_mst values(1, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', '自然', 'nature');
insert into photo.portfolio_tag_mst values(2, 1, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', '風景', '');
insert into photo.portfolio_tag_mst values(3, 2, 1, 1, 2, '2000-01-04 09:00:00 Asia/Tokyo', '海', 'sea');
select setval('photo.portfolio_tag_mst_id_seq', (select max(id) from photo.portfolio_tag_mst));

-- ポートフォリオ用の写真検索（selectAvailablePhotos）の検証用
-- common.location_mst：account_no=1のロケーション1（京都・住所は北区金閣寺町）・ロケーション2（論理削除済みの大阪）、account_no=2のロケーション1（京都）
insert into common.location_mst values(DEFAULT, 1, 1, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', false, '金閣寺_管理', '京都・金閣寺', '北区金閣寺町1', 35.039, 135.729);
insert into common.location_mst values(DEFAULT, 1, 2, 1, '2000-01-05 09:00:00 Asia/Tokyo', 1, '2000-01-05 09:00:00 Asia/Tokyo', true, '大阪城_管理', '大阪城', '中央区大阪城1-1', 34.687, 135.525);
insert into common.location_mst values(DEFAULT, 2, 1, 2, '2000-01-05 09:00:00 Asia/Tokyo', 2, '2000-01-05 09:00:00 Asia/Tokyo', false, '京都駅_管理', '京都駅', '住所3', 34.985, 135.758);
-- 写真の撮影場所：account_no=1の写真1は論理削除済みのロケーション2、写真2はロケーション1。account_no=2の写真1はロケーション1
update photo.photo_mst set location_no = 2 where account_no = 1 and photo_no = 1;
update photo.photo_mst set location_no = 1 where account_no = 1 and photo_no = 2;
update photo.photo_mst set location_no = 1 where account_no = 2 and photo_no = 1;
-- photo.photo_tag_mst：「紅葉」をaccount_no=1の写真1・論理削除済みの写真3、account_no=2の写真1に付与
insert into photo.photo_tag_mst values(DEFAULT, 1, 1, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '紅葉', 'autumn');
insert into photo.photo_tag_mst values(DEFAULT, 1, 3, 1, 1, '2000-01-01 10:00:00 Asia/Tokyo', '紅葉', 'autumn');
insert into photo.photo_tag_mst values(DEFAULT, 2, 1, 1, 2, '2000-01-01 10:00:00 Asia/Tokyo', '紅葉', 'autumn');
