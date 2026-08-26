package com.web.gallery.repository.impl;

import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
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
	private final ModelMapper modelMapper = new ModelMapper();

	/**
	 * 該当アカウントの写真の一覧を取得する
	 *
	 * @param	photoGetModel	{@link PhotoGetModel}
	 * @return						{@link PhotoModelList}
	 */
	@Override
	public PhotoModelList getPhotoList(PhotoGetModel photoGetModel) {
		List<PhotoDto> photoDtoList = photoDetailMapper.getPhotoList(PhotoListGetDto.from(photoGetModel));

		if (photoDtoList.isEmpty()) {
			return PhotoModelList.empty();
		}

		List<Long> photoNoList = photoDtoList.stream()
				.map(PhotoDto::getPhotoNo)
				.toList();

		List<PhotoTagMst> photoTagMstList = photoTagMstMapper.select(
				PhotoTagMstCondition.from(photoGetModel, photoNoList));

		return PhotoModelList.from(photoDtoList, photoTagMstList);
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
