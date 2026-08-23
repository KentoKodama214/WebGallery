package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 登録枚数の上限に達して写真が追加できない時のExceptionクラス
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhotoNotAdditableException extends GalleryException {
	public PhotoNotAdditableException(ErrorEnum error) {
		super(error);
	}
}
