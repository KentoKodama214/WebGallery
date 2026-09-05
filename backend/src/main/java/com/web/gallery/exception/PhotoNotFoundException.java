package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 写真が存在しない時のExceptionクラス */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhotoNotFoundException extends GalleryException {
  public PhotoNotFoundException(ErrorEnum error) {
    super(error);
  }
}
