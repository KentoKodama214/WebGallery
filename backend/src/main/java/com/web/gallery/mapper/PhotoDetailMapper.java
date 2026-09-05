package com.web.gallery.mapper;

import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.dto.PhotoDetailGetDto;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.dto.PhotoListGetDto;
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
