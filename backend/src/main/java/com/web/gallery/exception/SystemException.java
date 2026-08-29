package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 予期しないシステムエラー発生時のExceptionクラス
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemException extends GalleryException {
	public SystemException(ErrorEnum error) {
		super(error);
	}
}
