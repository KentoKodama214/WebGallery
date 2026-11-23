-- common.webauthn_challenge
-- challenge-reg：account_no=1の登録用（有効期限内）
-- challenge-auth：未ログインで発行した認証用（有効期限内、account_no=0）
-- challenge-expired：有効期限切れの認証用
insert into common.webauthn_challenge (challenge_no, challenge, purpose_kbn, account_no, expires_at, created_by, created_at) values (1, 'challenge-reg', 'registration', 1, '2099-01-01 09:00:00 Asia/Tokyo', 1, '2003-01-01 09:00:00 Asia/Tokyo');
insert into common.webauthn_challenge (challenge_no, challenge, purpose_kbn, account_no, expires_at, created_by, created_at) values (2, 'challenge-auth', 'authentication', 0, '2099-01-01 09:00:00 Asia/Tokyo', 0, '2003-01-01 09:00:00 Asia/Tokyo');
insert into common.webauthn_challenge (challenge_no, challenge, purpose_kbn, account_no, expires_at, created_by, created_at) values (3, 'challenge-expired', 'authentication', 0, '2000-01-01 09:00:00 Asia/Tokyo', 0, '2000-01-01 08:55:00 Asia/Tokyo');
SELECT setval('common.webauthn_challenge_challenge_no_seq', 3);
