package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 更新失敗時のExceptionクラス
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UpdateFailureException extends GalleryException {
	public UpdateFailureException(ErrorEnum error) {
		super(error);
	}
}
