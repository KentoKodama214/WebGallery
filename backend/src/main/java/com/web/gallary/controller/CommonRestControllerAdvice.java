package com.web.gallary.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.controller.request.ErrorRequest;
import com.web.gallary.controller.response.BadRequestResponse;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotAdditableException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.util.PhotoUrlUtil;

import lombok.RequiredArgsConstructor;

/**
 * システム共通のExceptionHandlerを扱うRestControllerAdviceクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@RestControllerAdvice(assignableTypes = {
		AccountRestController.class,
		PhotoFavoriteController.class,
		PhotoRestController.class
})
@Component
@RequiredArgsConstructor
public class CommonRestControllerAdvice {

	private final SessionHelper sessionHelper;
	
	/**
	 * リクエストパラメータが不正のときに制御するExceptionHandler
	 * 
	 * @param	exception	{@link BadRequestException}
	 * @return				{@link BadRequestResponse}
	 */
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<BadRequestResponse> handleBadRequestException(BadRequestException exception) {
		BadRequestResponse response = BadRequestResponse.builder()
				.httpStatus(HttpStatus.BAD_REQUEST.value())
				.isSuccess(false)
				.message(exception.getMessage())
				.build();
		
		return new ResponseEntity<BadRequestResponse>(response, HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * 権限のないアカウントからの不正アクセスがあったときに制御するExceptionHandler
	 * 
	 * @param	exception	{@link ForbiddenAccountException}
	 * @return				{@link ErrorRequest}
	 */
	@ExceptionHandler(ForbiddenAccountException.class)
	public ResponseEntity<ErrorRequest> handleForbiddenAccountException(ForbiddenAccountException exception) {
		ErrorRequest errorResponse = ErrorRequest.builder()
				.httpStatus(HttpStatus.FORBIDDEN.value())
				.errorCode(exception.getErrorCode())
				.errorMessage(exception.getMessage())
				.goBackPageUrl(getGoBackPageUrl()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.FORBIDDEN);
	}
	
	/**
	 * 保存するファイルが重複した時に制御するExceptionHandler
	 * 
	 * @param	exception	{@link FileDuplicateException}
	 * @return				{@link ErrorRequest}
	 */
	@ExceptionHandler(FileDuplicateException.class)
	public ResponseEntity<ErrorRequest> handleFileDuplicateException(FileDuplicateException exception) {
		ErrorRequest errorResponse = ErrorRequest.builder()
				.httpStatus(HttpStatus.CONFLICT.value())
				.errorCode(exception.getErrorCode())
				.errorMessage(exception.getMessage())
				.goBackPageUrl(getGoBackPageUrl()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.CONFLICT);
	}
	
	/**
	 * 写真の登録枚数の上限に達した状態での登録を制御するExceptionHandler
	 * 
	 * @param	exception	{@link PhotoNotAdditableException}
	 * @return				{@link ErrorRequest}
	 */
	@ExceptionHandler(PhotoNotAdditableException.class)
	public ResponseEntity<ErrorRequest> handlePhotoNotAdditableException(PhotoNotAdditableException exception) {
		ErrorRequest errorResponse = ErrorRequest.builder()
				.httpStatus(HttpStatus.BAD_REQUEST.value())
				.errorCode(exception.getErrorCode())
				.errorMessage(exception.getMessage())
				.goBackPageUrl(getGoBackPageUrl()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * データの登録に失敗したときに制御するExceptionHandler
	 * 
	 * @param	exception	{@link RegistFailureException}
	 * @return				{@link ErrorRequest}
	 */
	@ExceptionHandler(RegistFailureException.class)
	public ResponseEntity<ErrorRequest> handleInsertFailedException(RegistFailureException exception) {
		ErrorRequest errorResponse = ErrorRequest.builder()
				.httpStatus(HttpStatus.CONFLICT.value())
				.errorCode(exception.getErrorCode())
				.errorMessage(exception.getMessage())
				.goBackPageUrl(getGoBackPageUrl()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.CONFLICT);
	}
	
	/**
	 * データの更新に失敗したときに制御するExceptionHandler
	 * 
	 * @param	exception	{@link UpdateFailureException}
	 * @return				{@link ErrorRequest}
	 */
	@ExceptionHandler(UpdateFailureException.class)
	public ResponseEntity<ErrorRequest> handleUpdateFailureException(UpdateFailureException exception) {
		ErrorRequest errorResponse = ErrorRequest.builder()
				.httpStatus(HttpStatus.CONFLICT.value())
				.errorCode(exception.getErrorCode())
				.errorMessage(exception.getMessage())
				.goBackPageUrl(getGoBackPageUrl()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.CONFLICT);
	}
	
	/**
	 * エラーページから戻るページのURLを生成する<p>
	 * 未ログイン者の場合はログインページ、ログイン者は自身の写真一覧ページへ戻る
	 * 
	 * @return	遷移先ページのURL
	 */
	private String getGoBackPageUrl() {
		return Objects.isNull(sessionHelper.getAccountId()) ? ApiRoutes.LOGIN : PhotoUrlUtil.getPhotoListUrl(sessionHelper.getAccountId());
	}
}