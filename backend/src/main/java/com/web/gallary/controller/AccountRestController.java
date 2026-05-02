package com.web.gallary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.constant.Consts;
import com.web.gallary.controller.request.AccountRegistRequest;
import com.web.gallary.controller.request.AccountUpdateRequest;
import com.web.gallary.controller.request.ErrorRequest;
import com.web.gallary.controller.response.AccountRegistResponse;
import com.web.gallary.controller.response.AccountUpdateResponse;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.AccountModel;
import com.web.gallary.service.impl.AccountServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * アカウントに関するAPI通信を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountRestController {
	private final AccountServiceImpl accountServiceImpl;
	private final SessionHelper sessionHelper;
	
	/**
	 * アカウント登録
	 * 
	 * @param	accuontRegistRequest	{@link AccountRegistRequest}
	 * @param	result					AccountRegistRequestのバインディング結果
	 * @return							{@link AccountRegistResponse}
	 * @throws	BadRequestException 	リクエストパラメータが不正の場合
	 * @throws	RegistFailureException 	一意制約違反でアカウントの登録に失敗した場合
	 */
	@PostMapping(ApiRoutes.API_ACCOUNTS)
	public ResponseEntity<AccountRegistResponse> register(
			@RequestBody @Validated AccountRegistRequest accuontRegistRequest, 
			BindingResult result) throws BadRequestException, RegistFailureException {
		
		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		AccountModel accountModel = AccountModel.builder()
				.accountId(accuontRegistRequest.getAccountId())
				.accountName(accuontRegistRequest.getAccountName())
				.password(accuontRegistRequest.getPassword())
				.birthdate(accuontRegistRequest.getBirthdate())
				.sexKbn(accuontRegistRequest.getSexKbn())
				.birthplacePrefectureKbnCode(accuontRegistRequest.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(accuontRegistRequest.getResidentPrefectureKbnCode())
				.freeMemo(accuontRegistRequest.getFreeMemo())
				.loginFailureCount(0)
				.build();
		
		Boolean isSuccess = accountServiceImpl.registAccount(accountModel);
		return ResponseEntity.ok(AccountRegistResponse.builder()
					.httpStatus(HttpStatus.OK.value())
					.isSuccess(isSuccess)
					.message(Consts.STRING_EMPTY)
					.build());
	}
	
	/**
	 * アカウント更新
	 * 
	 * @param	accountId				アカウントID
	 * @param	accountUpdateRequest	{@link AccountUpdateRequest}
	 * @param	result					AccountUpdateRequestのバインディング結果
	 * @return							{@link AccountUpdateResponse}
	 * @throws	BadRequestException		リクエストパラメータが不正の場合
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@PutMapping(ApiRoutes.API_ACCOUNT)
	public ResponseEntity<AccountUpdateResponse> update(
			@PathVariable String accountId,
			@RequestBody @Validated AccountUpdateRequest accountUpdateRequest,
			BindingResult result) throws BadRequestException, UpdateFailureException {
		
		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}
			
			List<String> fieldList
				= result.getFieldErrors().stream().map(FieldError::getField).distinct().toList();
			
			if(!(fieldList.size() == 1 && 
					"newPassword".equals(fieldList.getFirst()) && 
				accountUpdateRequest.getNewPassword().isEmpty())) {
					// 新しいパスワードが空欄で他にパラメータ不正がない場合は、スキップ
					// 新しいパスワード以外や新しいパスワードの入力に不正がある場合は、例外
					throw new BadRequestException(ErrorEnum.INVALID_INPUT);
			}
		}
		
		AccountModel accountModel = AccountModel.builder()
				.accountNo(sessionHelper.getAccountNo())
				.accountId(accountUpdateRequest.getAccountId())
				.accountName(accountUpdateRequest.getAccountName())
				.password(accountUpdateRequest.getNewPassword().isEmpty() ? null : accountUpdateRequest.getNewPassword())
				.birthdate(accountUpdateRequest.getBirthdate())
				.sexKbn(accountUpdateRequest.getSexKbn())
				.birthplacePrefectureKbnCode(accountUpdateRequest.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(accountUpdateRequest.getResidentPrefectureKbnCode())
				.freeMemo(accountUpdateRequest.getFreeMemo())
				.build();
		
		Boolean isDuplicateAccountId = accountServiceImpl.updateAccount(accountModel);
		
		return ResponseEntity.ok(AccountUpdateResponse.builder()
					.httpStatus(HttpStatus.OK.value())
					.isDuplicateAccountId(isDuplicateAccountId)
					.isAccountIdChanged(!accountUpdateRequest.getAccountId().equals(sessionHelper.getAccountId()))
					.isPasswordChanged(!accountUpdateRequest.getNewPassword().isEmpty())
					.message(Consts.STRING_EMPTY)
					.build());
	}
	
	/**
	 * アカウント登録に失敗した時のExceptionHandler
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
				.goBackPageUrl(ApiRoutes.REGISTER).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.CONFLICT);
	}
}