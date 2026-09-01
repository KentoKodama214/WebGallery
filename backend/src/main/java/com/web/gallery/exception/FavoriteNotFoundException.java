package com.web.gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.web.gallery.enumeration.ErrorEnum;

/**
 * 解除対象のお気に入りが存在しない時のExceptionクラス
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FavoriteNotFoundException extends GalleryException {
	public FavoriteNotFoundException(ErrorEnum error) {
		super(error);
	}
}
