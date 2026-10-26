-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.inquiry_mst
insert into common.inquiry_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', '件名1', '本文1', 'unreplied', true);
insert into common.inquiry_mst values(2, 1, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '2001-01-02 09:00:00 Asia/Tokyo', '件名2', '本文2', 'replied', false);
insert into common.inquiry_mst values(3, 2, 1, 2, '2000-01-03 09:00:00 Asia/Tokyo', 2, '2001-01-03 09:00:00 Asia/Tokyo', '件名3', '本文3', 'unreplied', true);

select setval('common.inquiry_mst_id_seq', (select max(id) from common.inquiry_mst));
