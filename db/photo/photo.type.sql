/* Create Type */
-- 向き
CREATE TYPE photo.direction_enum AS ENUM ('vertical', 'horizontal', 'square', 'none');
COMMENT ON TYPE photo.direction_enum IS '向きを管理するEnum型: vertical(縦)、horizontal(横)、square(正方形)、none(未設定)';

-- 写真一覧の並び順
CREATE TYPE photo.sort_photo_enum AS ENUM ('photo_at', 'favorite', 'season');
COMMENT ON TYPE photo.sort_photo_enum IS '写真一覧の並び順を管理するEnum型: photo_at(撮影日順)、favorite(お気に入り数順)、season(季節順)';

-- ポートフォリオのテンプレート
CREATE TYPE photo.portfolio_template_enum AS ENUM ('feature', 'grid');
COMMENT ON TYPE photo.portfolio_template_enum IS 'ポートフォリオのテンプレートを管理するEnum型: feature(フィーチャー型。写真ごとにテーマ・サブテーマ・説明を持つセクションを縦に並べる)、grid(グリッド型。1行に2〜3枚の写真を組み合わせて並べる)';

-- ポートフォリオの公開状態
CREATE TYPE photo.portfolio_status_enum AS ENUM ('draft', 'published');
COMMENT ON TYPE photo.portfolio_status_enum IS 'ポートフォリオの公開状態を管理するEnum型: draft(下書き)、published(公開)';
