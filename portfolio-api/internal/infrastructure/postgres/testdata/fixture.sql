-- portfolio-api の PostgreSQL リポジトリ結合テスト用フィクスチャ。
-- Java のテストデータと衝突しないよう、アカウント番号 900001〜900002 の範囲だけを使う。
-- account_no=900001（goitowner1）：
--   ポートフォリオ1：公開中のグリッド型（写真2→写真1→論理削除済みの写真3の順。タグ2件）
--   ポートフォリオ2：公開中のフィーチャー型（写真なし、更新日時が最も新しい）
--   ポートフォリオ3：下書き、ポートフォリオ4：論理削除済み（いずれも公開ページには出さない）
-- account_no=900002（goitother1）：公開中のポートフォリオ1（他アカウントのデータが混ざらないことの確認用）
insert into common.account values(900001, 900001, '2026-01-01 09:00:00 Asia/Tokyo', 900001, '2026-01-01 09:00:00 Asia/Tokyo', false, 'goitowner1', 'Go Owner', '$2a$10$password1', '1900-01-01', 'none', 'none', 'none', '', '2026-01-01 09:00:00 Asia/Tokyo', 0, false);
insert into common.account values(900002, 900002, '2026-01-01 09:00:00 Asia/Tokyo', 900002, '2026-01-01 09:00:00 Asia/Tokyo', false, 'goitother1', 'Go Other', '$2a$10$password2', '1900-01-01', 'none', 'none', 'none', '', '2026-01-01 09:00:00 Asia/Tokyo', 0, false);

insert into photo.photo_mst (account_no, photo_no, created_by, created_at, updated_by, updated_at, is_deleted, photo_at, location_no, image_file_path, image_file_name, photo_japanese_title, photo_english_title, caption, direction_kbn, focal_length, f_value, shutter_speed, iso, is_location_public) values
  (900001, 1, 900001, now(), 900001, now(), false, now(), 0, '900001/1-aaa.jpg', 'a.jpg', '金閣寺', 'Kinkakuji', '', 'horizontal', 24, 8.0, 1, 100, false),
  (900001, 2, 900001, now(), 900001, now(), false, now(), 0, '900001/2-bbb.jpg', 'b.jpg', '茶畑', '', '', 'square', 24, 8.0, 1, 100, false),
  (900001, 3, 900001, now(), 900001, now(), true, now(), 0, '900001/3-ccc.jpg', 'c.jpg', '削除済み', '', '', 'vertical', 24, 8.0, 1, 100, false),
  (900002, 1, 900002, now(), 900002, now(), false, now(), 0, '900002/1-ddd.jpg', 'd.jpg', '橋杭岩', '', '', 'horizontal', 24, 8.0, 1, 100, false);

insert into photo.portfolio_mst (account_no, portfolio_no, created_by, created_at, updated_by, updated_at, theme, description, template_kbn, template_settings, settings_version, status_kbn) values
  (900001, 1, 900001, now(), 900001, '2026-02-01 09:00:00+09', '光と影', '説明1', 'grid', '{"rows": [{"pattern": "TWO_EQUAL"}]}', 1, 'published'),
  (900001, 2, 900001, now(), 900001, '2026-03-01 09:00:00+09', '日本の自然', '', 'feature', '{}', 1, 'published'),
  (900001, 3, 900001, now(), 900001, '2026-04-01 09:00:00+09', '下書き', '', 'feature', '{}', 1, 'draft'),
  (900002, 1, 900002, now(), 900002, '2026-06-01 09:00:00+09', '他人', '', 'feature', '{}', 1, 'published');

insert into photo.portfolio_photo (account_no, portfolio_no, photo_no, created_by, created_at, display_order, photo_settings) values
  (900001, 1, 2, 900001, now(), 1, '{"caption": "茶畑"}'),
  (900001, 1, 1, 900001, now(), 2, '{}'),
  (900001, 1, 3, 900001, now(), 3, '{}'),
  (900001, 3, 1, 900001, now(), 1, '{}'),
  (900002, 1, 1, 900002, now(), 1, '{"theme": "海"}');

insert into photo.portfolio_tag_mst (account_no, portfolio_no, tag_no, created_by, created_at, tag_japanese_name, tag_english_name) values
  (900001, 1, 2, 900001, now(), '風景', ''),
  (900001, 1, 1, 900001, now(), '自然', 'nature'),
  (900002, 1, 1, 900002, now(), '海', 'sea');
