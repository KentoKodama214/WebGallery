-- パスワード: password123 をBCryptハッシュ化した値
-- アカウント1: 正常なアカウント（ログイン失敗回数0）
insert into common.account values(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'testuser01', 'テストユーザー01', '$2a$10$gIBKBmG5bPPn/YoXFnn5k.VGkAe1VqAmagfNgCOgBhhPaFfziOQUa', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0);
-- アカウント2: ログイン失敗回数が上限に達したアカウント（ロック状態）
insert into common.account values(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'lockeduser', 'ロックユーザー', '$2a$10$gIBKBmG5bPPn/YoXFnn5k.VGkAe1VqAmagfNgCOgBhhPaFfziOQUa', '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 3);
ALTER SEQUENCE common.account_account_no_seq RESTART 3;
