-- common.account
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'aaaaaaaa', 'AAAAAAAA', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'bbbbbbbb', 'BBBBBBBB', '1900-01-01', 'none', 'none', 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false);

-- common.recovery_code（account_no=1に2件、account_no=2に1件）
insert into common.recovery_code (recovery_code_no, account_no, code_hash, created_by, created_at) values (1, 1, 'hash-1a', 1, '2003-01-01 09:00:00 Asia/Tokyo');
insert into common.recovery_code (recovery_code_no, account_no, code_hash, created_by, created_at) values (2, 1, 'hash-1b', 1, '2003-01-01 09:00:00 Asia/Tokyo');
insert into common.recovery_code (recovery_code_no, account_no, code_hash, created_by, created_at) values (3, 2, 'hash-2a', 2, '2003-01-01 09:00:00 Asia/Tokyo');
SELECT setval('common.recovery_code_recovery_code_no_seq', 3);
