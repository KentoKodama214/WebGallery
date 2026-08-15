package com.web.gallery.model;

import java.util.List;
import java.util.Objects;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumuration.DirectionEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoModel {
	/** アカウント番号 */
	@NonNull
	private AccountNo accountNo;

	/** 写真番号 */
	@NonNull
	private PhotoNo photoNo;

	/** お気に入り数 */
	private FavoriteCount favoriteCount;

	/** お気に入り */
	private IsFavorite isFavorite;

	/** 撮影日時 */
	@NonNull
	private PhotoAt photoAt;

	/** 画像ファイルパス */
	@NonNull
	private ImageFilePath imageFilePath;

	/** キャプション */
	@NonNull
	private Caption caption;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@NonNull
	private DirectionEnum directionKbn;

	/** 写真タグリスト */
	@NonNull
	private List<PhotoTagModel> photoTagModelList;

	/**
	 * PhotoDtoとタグエンティティリストからPhotoModelを生成する
	 *
	 * @param	dto				{@link PhotoDto}
	 * @param	photoTagMstList	全タグエンティティリスト（内部で該当写真のタグをフィルタリングする）
	 * @return					{@link PhotoModel}
	 */
	public static PhotoModel from(PhotoDto dto, List<PhotoTagMst> photoTagMstList) {
		List<PhotoTagModel> photoTagModelList = photoTagMstList.stream()
				.filter(tag ->
					tag.getAccountNo().value().equals(dto.getAccountNo().value()) &&
					Objects.equals(tag.getPhotoNo().value(), dto.getPhotoNo().value()))
				.map(PhotoTagModel::from)
				.toList();
		return PhotoModel.builder()
				.accountNo(dto.getAccountNo())
				.photoNo(dto.getPhotoNo())
				.favoriteCount(dto.getFavoriteCount())
				.isFavorite(dto.getIsFavorite())
				.photoAt(new PhotoAt(dto.getPhotoAt().value().plusHours(9)))
				.imageFilePath(dto.getImageFilePath())
				.caption(dto.getCaption())
				.directionKbn(dto.getDirectionKbn())
				.photoTagModelList(photoTagModelList)
				.build();
	}
}
