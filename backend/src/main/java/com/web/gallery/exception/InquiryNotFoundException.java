package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** お問い合わせが存在しない時のExceptionクラス */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InquiryNotFoundException extends GalleryException {
  public InquiryNotFoundException(ErrorEnum error) {
    super(error);
  }
}
