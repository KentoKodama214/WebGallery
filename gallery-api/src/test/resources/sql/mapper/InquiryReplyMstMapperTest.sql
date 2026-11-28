-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(3, 3, '2000-01-03 09:00:00 Asia/Tokyo', 3, '2001-01-03 09:00:00 Asia/Tokyo', false, 'cccccccc', 'CCCCCCCC', '$2a$10$password3', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(4, 4, '2000-01-04 09:00:00 Asia/Tokyo', 4, '2001-01-04 09:00:00 Asia/Tokyo', false, 'dddddddd', 'DDDDDDDD', '$2a$10$password4', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.inquiry_mst
insert into common.inquiry_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', '件名1', '本文1', 'unreplied', true);
insert into common.inquiry_mst values(2, 2, 1, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', '件名2', '本文2', 'unreplied', true);
insert into common.inquiry_mst values(3, 3, 1, 3, '2000-01-03 09:00:00 Asia/Tokyo', 3, '2001-01-03 09:00:00 Asia/Tokyo', '件名3', '本文3', 'unreplied', true);
insert into common.inquiry_mst values(4, 4, 1, 4, '2000-01-04 09:00:00 Asia/Tokyo', 4, '2001-01-04 09:00:00 Asia/Tokyo', '件名4', '本文4', 'unreplied', true);

-- common.inquiry_reply_mst
-- 返信1・2：アカウント1が起票したお問い合わせへの、アカウント1自身による返信（自己返信）
insert into common.inquiry_reply_mst values(1, 1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', '返信本文1');
insert into common.inquiry_reply_mst values(2, 1, 2, 1, 1, '2000-01-02 09:00:00 Asia/Tokyo', '返信本文2');
-- 返信3：アカウント3が起票したお問い合わせへの、アカウント1による返信（他ユーザーのスレッドへの返信）
insert into common.inquiry_reply_mst values(3, 3, 1, 1, 1, '2000-01-03 09:00:00 Asia/Tokyo', '返信本文3');
-- 返信4：アカウント4が起票したお問い合わせへの、アカウント4自身による返信（自己返信のみを持つ管理者）
insert into common.inquiry_reply_mst values(4, 4, 1, 4, 4, '2000-01-04 09:00:00 Asia/Tokyo', '返信本文4');
