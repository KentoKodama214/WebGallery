package com.web.gallery.dto;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumuration.DirectionEnum;

import lombok.Data;

/**
 * 写真の基本データを保持するDtoクラス
 */
@Data
public class PhotoDto {
	/** アカウントNo */
	private AccountNo accountNo;

	/** 写真番号 */
	private PhotoNo photoNo;

	/** お気に入り数 */
	private FavoriteCount favoriteCount;

	/** お気に入り */
	private IsFavorite isFavorite;

	/** 撮影日時 */
	private PhotoAt photoAt;

	/** 画像ファイルパス */
	private ImageFilePath imageFilePath;

	/** キャプション */
	private Caption caption;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	private DirectionEnum directionKbn;
}
