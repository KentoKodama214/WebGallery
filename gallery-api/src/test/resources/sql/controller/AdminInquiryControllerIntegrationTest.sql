-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;

-- common.inquiry_mst
insert into common.inquiry_mst values(1, 1, 1, 1, '2024-01-01 09:00:00 Asia/Tokyo', 1, '2024-01-01 09:00:00 Asia/Tokyo', '写真が表示されない', 'アップロードした写真がギャラリーに表示されません。', 'unreplied', true);
insert into common.inquiry_mst values(2, 2, 1, 2, '2024-01-02 09:00:00 Asia/Tokyo', 2, '2024-01-02 09:00:00 Asia/Tokyo', 'アカウント削除方法', 'アカウントを削除したいです。', 'unreplied', true);
insert into common.inquiry_mst values(3, 1, 2, 1, '2024-01-03 09:00:00 Asia/Tokyo', 1, '2024-01-03 09:00:00 Asia/Tokyo', '取り下げ済みのお問い合わせ', 'これは取り下げ済みです。', 'withdrawn', true);
ALTER SEQUENCE common.inquiry_mst_id_seq RESTART 4;
