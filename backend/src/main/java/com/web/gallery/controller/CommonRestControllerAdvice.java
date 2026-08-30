package com.web.gallery.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import io.swagger.v3.oas.annotations.Hidden;
import com.web.gallery.controller.response.BadRequestResponse;
import com.web.gallery.controller.response.ErrorResponse;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;

import lombok.extern.slf4j.Slf4j;

/**
 * システム共通のExceptionHandlerを扱うRestControllerAdviceクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Hidden
@Slf4j
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

	/**
	 * リクエストボディのバインド・バリデーション・パースに失敗したときに制御するExceptionHandler<p>
	 * （{@code @Valid}付きボディの検証失敗、不正なJSON、必須パラメータ欠落、型不一致、アップロードサイズ超過）
	 *
	 * @param	exception	発生した例外
	 * @return				{@link BadRequestResponse}
	 */
	@ExceptionHandler({
			MethodArgumentNotValidException.class,
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class,
			MaxUploadSizeExceededException.class
	})
	public ResponseEntity<BadRequestResponse> handleRequestBindingException(Exception exception) {
		log.info("Invalid request. ({}: {})", exception.getClass().getSimpleName(), exception.getMessage());

		BadRequestException badRequestException = (BadRequestException) ErrorEnum.INVALID_INPUT.toException();
		return new ResponseEntity<BadRequestResponse>(BadRequestResponse.of(badRequestException), HttpStatus.BAD_REQUEST);
	}

	/**
	 * 個別のExceptionHandlerで捕捉されない予期しないDBアクセスエラーが発生したときに制御するExceptionHandler
	 * <p>
	 * スタックトレース等の内部情報を含まない一般的なエラーレスポンスに変換する安全網として機能する
	 *
	 * @param	exception	{@link DataAccessException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException exception) {
		log.error("Unexpected data access error occurred.", exception);

		GalleryException systemException = ErrorEnum.SYSTEM_ERROR.toException();
		ErrorResponse errorResponse = ErrorResponse.of(systemException, HttpStatus.INTERNAL_SERVER_ERROR);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 個別のExceptionHandlerで捕捉されないアプリケーション例外の安全網<p>
	 * 主に{@link com.web.gallery.exception.SystemException}を想定し、内部情報を含まない一般的なエラーレスポンスに変換する
	 *
	 * @param	exception	{@link GalleryException}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(GalleryException.class)
	public ResponseEntity<ErrorResponse> handleGalleryException(GalleryException exception) {
		log.error("Unhandled application error occurred.", exception);

		ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 上記いずれのExceptionHandlerでも捕捉されない予期しない例外が発生したときに制御するExceptionHandler<p>
	 * スタックトレース等の内部情報を含まない一般的なエラーレスポンスに変換する最終的な安全網として機能する
	 *
	 * @param	exception	{@link Exception}
	 * @return				{@link ErrorResponse}
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
		log.error("Unexpected error occurred.", exception);

		GalleryException systemException = ErrorEnum.SYSTEM_ERROR.toException();
		ErrorResponse errorResponse = ErrorResponse.of(systemException, HttpStatus.INTERNAL_SERVER_ERROR);

		return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
