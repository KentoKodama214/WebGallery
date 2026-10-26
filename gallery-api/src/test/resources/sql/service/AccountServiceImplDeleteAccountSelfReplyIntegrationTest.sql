-- AccountServiceImplDeleteAccountIntegrationTest.sql に重ねて使う追加フィクスチャ。
-- account_no=1が「管理者として」自分自身のお問い合わせへ投稿した返信を1件作る。
-- 自スレッドの返信は退会時にスレッドごと削除されるため第三者への影響がなく、削除はブロックされない
insert into common.inquiry_reply_mst values(DEFAULT, (select id from common.inquiry_mst where account_no=1 and inquiry_no=2), 1, 1, 1, '2000-01-02 10:00:00 Asia/Tokyo', '自分のお問い合わせへの自己返信');
