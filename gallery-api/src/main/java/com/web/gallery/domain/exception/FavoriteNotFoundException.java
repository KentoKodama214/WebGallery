package com.web.gallery.domain.exception;

import com.web.gallery.domain.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 解除対象のお気に入りが存在しない時のExceptionクラス */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FavoriteNotFoundException extends GalleryException {
  public FavoriteNotFoundException(ErrorEnum error) {
    super(error);
  }
}
