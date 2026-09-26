package com.web.gallery.domain.exception;

import com.web.gallery.domain.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 保存するファイルが重複した時のExceptionクラス */
@ResponseStatus(HttpStatus.CONFLICT)
public class FileDuplicateException extends GalleryException {
  public FileDuplicateException(ErrorEnum error) {
    super(error);
  }
}
