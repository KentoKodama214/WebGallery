package com.web.gallary.entity;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 写真お気に入りテーブルのEntityクラス
 */
@Data
@Builder
public class PhotoFavorite {
	/** ID */
	private Integer id;

	/** アカウント番号 */
	private Integer accountNo;

	/** お気に入り写真アカウント番号 */
	private Integer favoritePhotoAccountNo;

	/** お気に入り写真番号 */
	private Integer favoritePhotoNo;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;
}