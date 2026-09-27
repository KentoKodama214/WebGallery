package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailGetDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoListGetDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** 写真のメタデータを含めた詳細情報のMapperクラス */
@Mapper
public interface PhotoDetailMapper {
  /**
   * 写真の一覧を取得する
   *
   * @param photoSelectDto {@link PhotoListGetDto}
   * @return {@link PhotoDto}
   */
  public List<PhotoDto> getPhotoList(PhotoListGetDto photoSelectDto);

  /**
   * 1枚の写真のメタデータを含めた詳細情報を取得する
   *
   * @param photoGetDto {@link PhotoDetailGetDto}
   * @return {@link PhotoDetailDto}
   */
  public PhotoDetailDto getPhotoDetail(PhotoDetailGetDto photoGetDto);
}
