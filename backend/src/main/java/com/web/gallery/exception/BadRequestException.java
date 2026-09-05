package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** リクエストパラメータ不正のExceptionクラス */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends GalleryException {
  public BadRequestException(ErrorEnum error) {
    super(error);
  }
}
