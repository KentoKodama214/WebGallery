-- common.account
-- account_no=1（aaaaaaaa）：パスワードは password01。パスキーの登録・ログインに使う
-- account_no=2（bbbbbbbb）：パスワードは password01。他人のチャレンジ・パスキーの検証に使う
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$k19cLX6F2brrOLYp74GstejFJfeGjm52TMk..ELwoJeTcBcUnBVM2', '1991-02-14', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '$2a$10$k19cLX6F2brrOLYp74GstejFJfeGjm52TMk..ELwoJeTcBcUnBVM2', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
SELECT setval('common.account_account_no_seq', 2);

-- common.account_authority
insert into common.account_authority values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', 'normal-user');
insert into common.account_authority values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', 'normal-user');
