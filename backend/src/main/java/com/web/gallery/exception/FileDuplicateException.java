package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 保存するファイルが重複した時のExceptionクラス
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class FileDuplicateException extends GalleryException {
	public FileDuplicateException(ErrorEnum error) {
		super(error);
	}
}
