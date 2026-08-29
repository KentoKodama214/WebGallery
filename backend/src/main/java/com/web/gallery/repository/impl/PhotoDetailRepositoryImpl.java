package com.web.gallery.repository.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.dto.PhotoDetailGetDto;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.dto.PhotoListGetDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.PhotoDetailMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoPageModel;
import com.web.gallery.repository.PhotoDetailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真のメタデータを含めた詳細情報を永続化するRepositoryの実装クラス
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoDetailRepositoryImpl implements PhotoDetailRepository {

	private final PhotoTagMstMapper photoTagMstMapper;
	private final PhotoDetailMapper photoDetailMapper;

	/**
	 * 該当アカウントの写真の一覧を、ページング情報に従い取得する<p>
	 * 最後のページかどうかを判定するため、DBからは1ページあたりの表示件数より1件多く取得し、
	 * 実際に返す件数が上限を超えていた場合は表示件数分のみに切り詰める
	 *
	 * @param	photoGetModel	{@link PhotoGetModel}
	 * @return						{@link PhotoPageModel}
	 */
	@Override
	public PhotoPageModel getPhotoList(PhotoGetModel photoGetModel) {
		List<PhotoDto> photoDtoList = photoDetailMapper.getPhotoList(PhotoListGetDto.from(photoGetModel));

		Boolean isLast = photoDtoList.size() < photoGetModel.getLimit();
		List<PhotoDto> pageDtoList = isLast ? photoDtoList : photoDtoList.subList(0, photoGetModel.getLimit() - 1);

		if (pageDtoList.isEmpty()) {
			return PhotoPageModel.of(PhotoModelList.empty(), isLast);
		}

		List<Long> photoNoList = pageDtoList.stream()
				.map(PhotoDto::getPhotoNo)
				.toList();

		List<PhotoTagMst> photoTagMstList = photoTagMstMapper.select(
				PhotoTagMstCondition.from(photoGetModel, photoNoList));

		return PhotoPageModel.of(PhotoModelList.from(pageDtoList, photoTagMstList), isLast);
	}

	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 *
	 * @param	photoDetailSearchModel	{@link PhotoDetailSearchModel}
	 * @return						{@link PhotoDetailModel}
	 * @throws	GalleryException	写真が存在しなかった場合
	 */
	@Override
	public PhotoDetailModel getPhotoDetail(PhotoDetailSearchModel photoDetailSearchModel) throws GalleryException {
		PhotoDetailGetDto photoGetDto = PhotoDetailGetDto.from(photoDetailSearchModel);
		PhotoDetailDto photoDetailDto = photoDetailMapper.getPhotoDetail(photoGetDto);

		if(Objects.isNull(photoDetailDto)) {
			log.warn("Photo not found. (AccountNo: {}, PhotoAccountNo: {}, PhotoNo: {})"
					, photoGetDto.getAccountNo(), photoGetDto.getPhotoAccountNo(), photoGetDto.getPhotoNo());
			throw ErrorEnum.PHOTO_NOT_FOUND.toException();
		}

		List<PhotoTagMst> photoTagMstList = photoTagMstMapper.select(
				PhotoTagMstCondition.from(photoDetailSearchModel));

		return PhotoDetailModel.from(photoDetailDto, photoTagMstList);
	}
}
