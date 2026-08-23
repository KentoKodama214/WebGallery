package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 写真が存在しない時のExceptionクラス
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhotoNotFoundException extends GalleryException {
	public PhotoNotFoundException(ErrorEnum error) {
		super(error);
	}
}
