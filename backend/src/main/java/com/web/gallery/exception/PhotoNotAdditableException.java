package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 登録枚数の上限に達して写真が追加できない時のExceptionクラス */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhotoNotAdditableException extends GalleryException {
  public PhotoNotAdditableException(ErrorEnum error) {
    super(error);
  }
}
