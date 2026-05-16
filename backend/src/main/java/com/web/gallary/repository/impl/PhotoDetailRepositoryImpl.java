package com.web.gallary.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import com.web.gallary.dto.PhotoDetailDto;
import com.web.gallary.dto.PhotoDetailGetDto;
import com.web.gallary.dto.PhotoDto;
import com.web.gallary.dto.PhotoListGetDto;
import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.mapper.PhotoDetailMapper;
import com.web.gallary.mapper.PhotoTagMstMapper;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.repository.PhotoDetailRepository;

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
	 * @return						{@link PhotoModel}
	 */
	@Override
	public List<PhotoModel> getPhotoList(PhotoGetModel photoGetModel) {
		PhotoListGetDto photoListGetDto = modelMapper.map(photoGetModel, PhotoListGetDto.class);
		List<PhotoDto> photoDtoList = photoDetailMapper.getPhotoList(photoListGetDto);
		
		PhotoTagMst photoTagMst = PhotoTagMst.builder()
				.accountNo(photoGetModel.getPhotoAccountNo())
				.build();
		List<PhotoTagMst> photoTagMstList = photoTagMstMapper.select(photoTagMst);

		List<PhotoTagModel> photoTagModelList = photoTagMstList.stream().map(PhotoTagModel::from).toList();

		List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
		photoDtoList.stream().forEach(photoDto -> {
			List<PhotoTagModel> tagList = photoTagModelList.stream().filter(photoTagModel ->
					photoTagModel.getAccountNo() == photoDto.getAccountNo() &&
					photoTagModel.getPhotoNo()   == photoDto.getPhotoNo()
				).toList();
			photoModelList.add(PhotoModel.from(photoDto, tagList));
		});

		return photoModelList;
	}
	
	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 * 
	 * @param	photoDetailGetModel		{@link PhotoDetailGetModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	PhotoNotFoundException	写真が存在しなかった場合
	 */
	@Override
	public PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws PhotoNotFoundException {
		PhotoDetailGetDto photoGetDto = modelMapper.map(photoDetailGetModel, PhotoDetailGetDto.class);
		PhotoDetailDto photoDetailDto = photoDetailMapper.getPhotoDetail(photoGetDto);
		
		if(Objects.isNull(photoDetailDto)) {
			log.warn("Photo not found. (AccountNo: {}, PhotoAccountNo: {}, PhotoNo: {})"
					, photoGetDto.getAccountNo(), photoGetDto.getPhotoAccountNo(), photoGetDto.getPhotoNo());
			throw new PhotoNotFoundException(ErrorEnum.PHOTO_NOT_FOUND);
		}
		
		PhotoTagMst photoTagMst = PhotoTagMst.builder()
				.accountNo(photoDetailGetModel.getPhotoAccountNo())
				.photoNo(photoDetailGetModel.getPhotoNo())
				.build();
		List<PhotoTagMst> photoTagMstList = photoTagMstMapper.select(photoTagMst);

		List<PhotoTagModel> photoTagModelList = photoTagMstList.stream().map(PhotoTagModel::from).toList();

		return PhotoDetailModel.from(photoDetailDto, photoTagModelList);
	}
}