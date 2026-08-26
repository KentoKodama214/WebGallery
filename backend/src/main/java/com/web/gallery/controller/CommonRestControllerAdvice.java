package com.web.gallery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;
import com.web.gallery.controller.response.BadRequestResponse;
import com.web.gallery.controller.response.ErrorResponse;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;

/**
 * システム共通のExceptionHandlerを扱うRestControllerAdviceクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Hidden
@RestControllerAdvice(assignableTypes = {
		AccountRestController.class,
		AdminAccountRestController.class,
		KbnMstRestController.class,
		PhotoFavoriteController.class,
		PhotoRestController.class
})
@Component
public class CommonRestControllerAdvice {

	/**
	 * リクエストパラメータが不正のときに制御するExceptionHandler
	 *
	 * @param	exception	{@link BadRequestException}
	 * @return				{@link BadRequestResponse}
	 */
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<BadRequestResponse> handleBadRequestException(BadRequestException exception) {
		return new ResponseEntity<BadRequestResponse>(BadRequestResponse.of(exception), HttpStatus.BAD_REQUEST);
	}

	/**
	 * 権限のないアカウントからの不正アクセスがあったときに制御するExceptionHandler
	 *
	 * @param	exception	{@link ForbiddenAccountException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(ForbiddenAccountException.class)
	public ResponseEntity<ErrorResponse> handleForbiddenAccountException(ForbiddenAccountException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.FORBIDDEN);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
	}

	/**
	 * 保存するファイルが重複した時に制御するExceptionHandler
	 *
	 * @param	exception	{@link FileDuplicateException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(FileDuplicateException.class)
	public ResponseEntity<ErrorResponse> handleFileDuplicateException(FileDuplicateException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.CONFLICT);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
	}

	/**
	 * 写真の登録枚数の上限に達した状態での登録を制御するExceptionHandler
	 *
	 * @param	exception	{@link PhotoNotAdditableException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(PhotoNotAdditableException.class)
	public ResponseEntity<ErrorResponse> handlePhotoNotAdditableException(PhotoNotAdditableException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.BAD_REQUEST);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	/**
	 * データの登録に失敗したときに制御するExceptionHandler
	 *
	 * @param	exception	{@link RegistFailureException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(RegistFailureException.class)
	public ResponseEntity<ErrorResponse> handleInsertFailedException(RegistFailureException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.CONFLICT);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
	}

	/**
	 * 写真が存在しないときに制御するExceptionHandler
	 *
	 * @param	exception	{@link PhotoNotFoundException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(PhotoNotFoundException.class)
	public ResponseEntity<ErrorResponse> handlePhotoNotFoundException(PhotoNotFoundException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.NOT_FOUND);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
	}

	/**
	 * データの更新に失敗したときに制御するExceptionHandler
	 *
	 * @param	exception	{@link UpdateFailureException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(UpdateFailureException.class)
	public ResponseEntity<ErrorResponse> handleUpdateFailureException(UpdateFailureException exception) {
		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.CONFLICT);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
	}
}
