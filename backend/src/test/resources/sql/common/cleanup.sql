TRUNCATE TABLE
	photo.photo_favorite,
	photo.photo_tag_mst,
	photo.photo_mst,
	common.refresh_token,
	common.location_mst,
	common.account,
	common.kbn_mst
CASCADE;