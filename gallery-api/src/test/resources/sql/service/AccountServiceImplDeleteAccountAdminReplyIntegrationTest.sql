-- AccountServiceImplDeleteAccountIntegrationTest.sql に重ねて使う追加フィクスチャ。
-- account_no=1が「管理者として」account_no=2のお問い合わせへ投稿した返信を1件作る。
-- この状態ではアカウント削除がブロックされる（ErrorEnum.CANNOT_DELETE_ACCOUNT_WITH_ADMIN_REPLY）
insert into common.inquiry_reply_mst values(DEFAULT, (select id from common.inquiry_mst where account_no=2 and inquiry_no=1), 1, 1, 1, '2000-01-03 10:00:00 Asia/Tokyo', '返信本文21');
