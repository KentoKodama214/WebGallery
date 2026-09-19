-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'man', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;

-- common.inquiry_mst
insert into common.inquiry_mst values(1, 1, 1, 1, '2024-01-01 09:00:00 Asia/Tokyo', 1, '2024-01-01 09:00:00 Asia/Tokyo', '既読の問い合わせ', '既読状態の問い合わせ本文です。', 'replied', true);
insert into common.inquiry_mst values(2, 1, 2, 1, '2024-01-02 09:00:00 Asia/Tokyo', 1, '2024-01-02 09:00:00 Asia/Tokyo', '未読の問い合わせ', '未読状態の問い合わせ本文です。', 'replied', false);
insert into common.inquiry_mst values(3, 1, 3, 1, '2024-01-03 09:00:00 Asia/Tokyo', 1, '2024-01-03 09:00:00 Asia/Tokyo', '返信前の問い合わせ', '返信前の問い合わせ本文です。', 'unreplied', true);
insert into common.inquiry_mst values(4, 1, 4, 1, '2024-01-04 09:00:00 Asia/Tokyo', 1, '2024-01-04 09:00:00 Asia/Tokyo', '取り下げ済みの問い合わせ', '取り下げ済みの問い合わせ本文です。', 'withdrawn', true);
insert into common.inquiry_mst values(5, 1, 5, 1, '2024-01-05 09:00:00 Asia/Tokyo', 1, '2024-01-05 09:00:00 Asia/Tokyo', '問い合わせ5', '問い合わせ5の本文です。', 'unreplied', true);
insert into common.inquiry_mst values(6, 1, 6, 1, '2024-01-06 09:00:00 Asia/Tokyo', 1, '2024-01-06 09:00:00 Asia/Tokyo', '問い合わせ6', '問い合わせ6の本文です。', 'unreplied', true);
insert into common.inquiry_mst values(7, 1, 7, 1, '2024-01-07 09:00:00 Asia/Tokyo', 1, '2024-01-07 09:00:00 Asia/Tokyo', '問い合わせ7', '問い合わせ7の本文です。', 'unreplied', true);
ALTER SEQUENCE common.inquiry_mst_id_seq RESTART 8;

-- common.inquiry_reply_mst
insert into common.inquiry_reply_mst values(1, 1, 1, 1, 1, '2024-01-01 10:00:00 Asia/Tokyo', '既読済みの返信です。');
insert into common.inquiry_reply_mst values(2, 2, 1, 1, 1, '2024-01-02 10:00:00 Asia/Tokyo', '未読の返信です。');
ALTER SEQUENCE common.inquiry_reply_mst_id_seq RESTART 3;
