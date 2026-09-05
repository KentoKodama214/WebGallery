package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 更新失敗時のExceptionクラス */
@ResponseStatus(HttpStatus.CONFLICT)
public class UpdateFailureException extends GalleryException {
  public UpdateFailureException(ErrorEnum error) {
    super(error);
  }
}
