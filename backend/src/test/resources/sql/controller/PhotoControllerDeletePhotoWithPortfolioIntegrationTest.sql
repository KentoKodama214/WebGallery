-- PhotoControllerIntegrationTest.sql に重ねて投入し、削除対象の写真（account_no=1の写真番号1）を使用するポートフォリオを用意する
-- photo.portfolio_mst
-- 番号1：公開中（写真1・写真2）→ 写真1の削除で下書きに戻る
-- 番号2：下書き（写真1）→ 写真1は除外されるが、下書きのまま
-- 番号3：公開中（写真2のみ）→ 写真1を使用していないため変更されない
insert into photo.portfolio_mst values(DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', '日本の自然', '', 'feature', '{}', 1, 'published');
insert into photo.portfolio_mst values(DEFAULT, 1, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '2001-02-01 09:00:00 Asia/Tokyo', '光と影', '', 'grid', '{"rows": [{"pattern": "TWO_EQUAL"}]}', 1, 'draft');
insert into photo.portfolio_mst values(DEFAULT, 1, 3, 1, '2000-01-03 09:00:00 Asia/Tokyo', 1, '2001-03-01 09:00:00 Asia/Tokyo', '日本の動物・植物', '', 'feature', '{}', 1, 'published');

-- photo.portfolio_photo
insert into photo.portfolio_photo values(DEFAULT, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '{"theme": "テーマ1"}');
insert into photo.portfolio_photo values(DEFAULT, 1, 1, 2, 1, '2000-01-01 09:00:00 Asia/Tokyo', 2, '{"theme": "テーマ2"}');
insert into photo.portfolio_photo values(DEFAULT, 1, 2, 1, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '{}');
insert into photo.portfolio_photo values(DEFAULT, 1, 3, 2, 1, '2000-01-03 09:00:00 Asia/Tokyo', 1, '{"theme": "テーマ2"}');
