package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 権限のないアカウントからの不正アクセスの時のExceptionクラス
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenAccountException extends GalleryException {
	public ForbiddenAccountException(ErrorEnum error) {
		super(error);
	}
}
