-- フィクスチャで投入した行（アカウント番号 900001〜900002）だけを、外部キーの参照元から順に削除する
delete from photo.portfolio_photo where account_no in (900001, 900002);
delete from photo.portfolio_tag_mst where account_no in (900001, 900002);
delete from photo.portfolio_mst where account_no in (900001, 900002);
delete from photo.photo_mst where account_no in (900001, 900002);
delete from common.account where account_no in (900001, 900002);
