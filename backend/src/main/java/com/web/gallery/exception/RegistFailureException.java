package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 登録失敗時のExceptionクラス */
@ResponseStatus(HttpStatus.CONFLICT)
public class RegistFailureException extends GalleryException {
  public RegistFailureException(ErrorEnum error) {
    super(error);
  }
}
