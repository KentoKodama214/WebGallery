/* Create Type */
-- 向き
CREATE TYPE photo.direction_enum AS ENUM ('vertical', 'horizontal', 'square', 'none');
COMMENT ON TYPE photo.direction_enum IS '向きを管理するEnum型: vertical(縦)、horizontal(横)、square(正方形)、none(未設定)';

-- 写真一覧の並び順
CREATE TYPE photo.sort_photo_enum AS ENUM ('photo_at', 'favorite', 'season');
COMMENT ON TYPE photo.sort_photo_enum IS '写真一覧の並び順を管理するEnum型: photo_at(撮影日順)、favorite(お気に入り数順)、season(季節順)';
