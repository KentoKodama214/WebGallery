package com.web.gallery.exception;

import com.web.gallery.enumeration.ErrorEnum;

import lombok.Getter;

/**
 * アプリケーション固有のビジネスエラーを表す例外の基底クラス
 */
@Getter
public abstract class GalleryException extends Exception {
	/** エラーコード */
	private final String errorCode;

	protected GalleryException(ErrorEnum error) {
		super(error.getErrorMessage());
		this.errorCode = error.getErrorCode();
	}
}
