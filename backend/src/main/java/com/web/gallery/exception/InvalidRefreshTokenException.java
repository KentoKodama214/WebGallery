package com.web.gallery.exception;

/**
 * リフレッシュトークンが無効な場合のExceptionクラス
 */
public class InvalidRefreshTokenException extends RuntimeException {
	public InvalidRefreshTokenException(String message) {
		super(message);
	}
}
