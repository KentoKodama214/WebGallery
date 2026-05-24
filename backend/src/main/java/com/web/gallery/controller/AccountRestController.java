package com.web.gallery.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.AccountRegistRequest;
import com.web.gallery.controller.request.AccountUpdateRequest;
import com.web.gallery.controller.request.ErrorRequest;
import com.web.gallery.controller.response.AccountDetailResponse;
import com.web.gallery.controller.response.AccountListItemResponse;
import com.web.gallery.controller.response.AccountRegistResponse;
import com.web.gallery.controller.response.AccountUpdateResponse;
import com.web.gallery.enumuration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.AccountModel;
import com.web.gallery.service.impl.AccountServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "アカウント", description = "アカウント管理に関するAPI")
public class AccountRestController {
	private final AccountServiceImpl accountServiceImpl;
	private final SessionHelper sessionHelper;
	
	/**
	 * アカウント一覧取得
	 *
	 * @return	{@link AccountListItemResponse}のリスト
	 */
	@Operation(summary = "アカウント一覧取得", description = "登録されているアカウントの一覧を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@GetMapping(ApiRoutes.API_ACCOUNTS)
	public ResponseEntity<List<AccountListItemResponse>> getAccountList() {
		List<AccountListItemResponse> responseList = accountServiceImpl.getAccountList().stream()
				.map(AccountListItemResponse::from)
				.toList();

		return ResponseEntity.ok(responseList);
	}

	/**
	 * アカウント詳細取得
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountDetailResponse}
	 * @throws	ForbiddenAccountException	認証ユーザーと異なるアカウントIDの場合
	 */
	@Operation(summary = "アカウント詳細取得", description = "指定したアカウントの詳細情報を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
	@SecurityRequirement(name = "Bearer")
	@GetMapping(ApiRoutes.API_ACCOUNT)
	public ResponseEntity<AccountDetailResponse> getAccount(
			@PathVariable String accountId) throws ForbiddenAccountException {

		if (!accountId.equals(sessionHelper.getAccountId())) {
			throw new ForbiddenAccountException(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT);
		}

		AccountModel accountModel = accountServiceImpl.getAccountById(accountId);

		AccountDetailResponse response = AccountDetailResponse.from(accountModel);

		return ResponseEntity.ok(response);
	}

	/**
	 * アカウント登録
	 *
	 * @param	accountRegistRequest	{@link AccountRegistRequest}
	 * @param	result					AccountRegistRequestのバインディング結果
	 * @return							{@link AccountRegistResponse}
	 * @throws	BadRequestException 	リクエストパラメータが不正の場合
	 * @throws	RegistFailureException 	一意制約違反でアカウントの登録に失敗した場合
	 */
	@Operation(summary = "アカウント登録", description = "新規アカウントを登録する")
	@ApiResponse(responseCode = "200", description = "登録成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "409", description = "アカウントIDが既に使用されている", content = @Content)
	@PostMapping(ApiRoutes.API_ACCOUNTS)
	public ResponseEntity<AccountRegistResponse> register(
			@RequestBody @Validated AccountRegistRequest accountRegistRequest,
			BindingResult result) throws BadRequestException, RegistFailureException {
		
		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		AccountModel accountModel = AccountModel.from(accountRegistRequest);
		
		Boolean isSuccess = accountServiceImpl.registAccount(accountModel);
		return ResponseEntity.ok(AccountRegistResponse.of(isSuccess, Consts.STRING_EMPTY));
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
	@Operation(summary = "アカウント更新", description = "アカウント情報を更新する")
	@ApiResponse(responseCode = "200", description = "更新成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
	@ApiResponse(responseCode = "409", description = "変更後のアカウントIDが既に使用されている", content = @Content)
	@SecurityRequirement(name = "Bearer")
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
		
		AccountModel accountModel = AccountModel.from(accountUpdateRequest, sessionHelper.getAccountNo());
		
		Boolean isDuplicateAccountId = accountServiceImpl.updateAccount(accountModel);
		
		return ResponseEntity.ok(AccountUpdateResponse.of(
					isDuplicateAccountId,
					!accountUpdateRequest.getAccountId().equals(sessionHelper.getAccountId()),
					!accountUpdateRequest.getNewPassword().isEmpty(),
					Consts.STRING_EMPTY));
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
				.errorMessage(exception.getMessage()).build();
		
		return new ResponseEntity<ErrorRequest>(errorResponse, HttpStatus.CONFLICT);
	}
}