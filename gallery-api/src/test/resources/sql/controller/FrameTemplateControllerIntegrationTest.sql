-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.account_authority
insert into common.account_authority values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', 'normal-user');
insert into common.account_authority values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', 'normal-user');

-- photo.frame_template_mst
-- account_no=1：額縁テンプレート番号1（写真枠2・メッセージ枠1）・番号2（写真枠1）
-- account_no=2：額縁テンプレート番号1
insert into photo.frame_template_mst values(1, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', '横長 2枚組', '{"size": {"width": 364, "height": 257}, "background": {"color": "#f5f0e6"}, "slots": [{"x": 5, "y": 10, "width": 40, "height": 60}, {"x": 55, "y": 10, "width": 40, "height": 60, "rotation": 3}], "texts": [{"x": 10, "y": 80, "width": 80, "fontFamily": "mincho", "fontSize": 3}]}');
insert into photo.frame_template_mst values(2, 1, 2, 1, '2000-01-02 09:00:00 Asia/Tokyo', 1, '2001-01-02 09:00:00 Asia/Tokyo', '四つ切 1枚', '{"size": {"width": 254, "height": 305}, "slots": [{"x": 10, "y": 10, "width": 80, "height": 80}]}');
insert into photo.frame_template_mst values(3, 2, 1, 2, '2000-01-03 09:00:00 Asia/Tokyo', 2, '2001-01-03 09:00:00 Asia/Tokyo', '正方形', '{"size": {"width": 300, "height": 300}, "slots": [{"x": 10, "y": 10, "width": 80, "height": 80}]}');
select setval('photo.frame_template_mst_id_seq', (select max(id) from photo.frame_template_mst));
