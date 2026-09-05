package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 権限のないアカウントからの不正アクセスの時のExceptionクラス */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenAccountException extends GalleryException {
  public ForbiddenAccountException(ErrorEnum error) {
    super(error);
  }
}
