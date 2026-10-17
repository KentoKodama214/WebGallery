package com.web.gallery.domain.exception;

import com.web.gallery.domain.enumeration.ErrorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** ポートフォリオが存在しない時のExceptionクラス */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PortfolioNotFoundException extends GalleryException {
  public PortfolioNotFoundException(ErrorEnum error) {
    super(error);
  }
}
