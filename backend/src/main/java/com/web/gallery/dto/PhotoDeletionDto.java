package com.web.gallery.dto;

import lombok.Data;

/**
 * 写真マスタ物理削除結果を保持するDtoクラス
 */
@Data
public class PhotoDeletionDto {
	/** 写真番号 */
	private Long photoNo;

	/** 物理削除前の論理削除フラグ */
	private Boolean isDeleted;
}
