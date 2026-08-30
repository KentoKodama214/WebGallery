package com.web.gallery.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.AccountListRequest;
import com.web.gallery.controller.request.AccountRegistRequest;
import com.web.gallery.controller.request.AccountUpdateRequest;
import com.web.gallery.controller.response.AccountDetailResponse;
import com.web.gallery.controller.response.AccountListGetResponse;
import com.web.gallery.controller.response.AccountRegistResponse;
import com.web.gallery.controller.response.AccountUpdateResponse;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.AccountListGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountPageModel;
import com.web.gallery.service.AccountService;

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
	private final AccountService accountService;
	private final SessionHelper sessionHelper;
	
	/**
	 * アカウント一覧取得
	 *
	 * @param	accountListRequest	{@link AccountListRequest}
	 * @param	result				バリデーション結果
	 * @return						{@link AccountListGetResponse}
	 * @throws	GalleryException	リクエストパラメータが不正な場合
	 */
	@Operation(summary = "アカウント一覧取得", description = "登録されているアカウントの一覧を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@GetMapping(ApiRoutes.API_ACCOUNTS)
	public ResponseEntity<AccountListGetResponse> getAccountList(
			@ModelAttribute @Validated AccountListRequest accountListRequest,
			BindingResult result) throws GalleryException {

		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}
			throw ErrorEnum.INVALID_INPUT.toException();
		}

		AccountPageModel accountPageModel = accountService.getAccountList(AccountListGetModel.from(accountListRequest));

		return ResponseEntity.ok(AccountListGetResponse.from(accountPageModel));
	}

	/**
	 * アカウント詳細取得
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountDetailResponse}
	 * @throws	GalleryException	認証ユーザーと異なるアカウントIDの場合
	 */
	@Operation(summary = "アカウント詳細取得", description = "指定したアカウントの詳細情報を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
	@SecurityRequirement(name = "Bearer")
	@GetMapping(ApiRoutes.API_ACCOUNT)
	public ResponseEntity<AccountDetailResponse> getAccount(
			@PathVariable String accountId) throws GalleryException {

		if (!accountId.equals(sessionHelper.getAccountId())) {
			throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.toException();
		}

		AccountModel accountModel = accountService.getAccountById(new AccountId(accountId));

		AccountDetailResponse response = AccountDetailResponse.from(accountModel);

		return ResponseEntity.ok(response);
	}

	/**
	 * アカウント登録
	 *
	 * @param	accountRegistRequest	{@link AccountRegistRequest}
	 * @param	result					AccountRegistRequestのバインディング結果
	 * @return							{@link AccountRegistResponse}
	 * @throws	GalleryException		以下のいずれかに該当する場合
	 *                              	・リクエストパラメータが不正の場合
	 *                              	・一意制約違反でアカウントの登録に失敗した場合
	 */
	@Operation(summary = "アカウント登録", description = "新規アカウントを登録する")
	@ApiResponse(responseCode = "200", description = "登録成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "409", description = "アカウントIDが既に使用されている", content = @Content)
	@PostMapping(ApiRoutes.API_ACCOUNTS)
	public ResponseEntity<AccountRegistResponse> register(
			@RequestBody @Validated AccountRegistRequest accountRegistRequest,
			BindingResult result) throws GalleryException {

		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}
			throw ErrorEnum.INVALID_INPUT.toException();
		}
		
		AccountModel accountModel = AccountModel.from(accountRegistRequest);
		
		Boolean isSuccess = accountService.registAccount(accountModel);
		return ResponseEntity.ok(AccountRegistResponse.of(isSuccess, Consts.STRING_EMPTY));
	}
	
	/**
	 * アカウント更新
	 * 
	 * @param	accountId				アカウントID
	 * @param	accountUpdateRequest	{@link AccountUpdateRequest}
	 * @param	result					AccountUpdateRequestのバインディング結果
	 * @return							{@link AccountUpdateResponse}
	 * @throws	GalleryException		以下のいずれかに該当する場合
	 *                              	・リクエストパラメータが不正の場合
	 *                              	・更新に失敗した場合
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
			BindingResult result) throws GalleryException {

		// 新しいパスワードは未指定（JSONでnull）でも空文字と同様に「変更なし」として扱う
		String newPassword = Objects.requireNonNullElse(accountUpdateRequest.getNewPassword(), Consts.STRING_EMPTY);

		if(result.hasErrors()) {
			for(FieldError error : result.getFieldErrors()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
			}

			List<String> fieldList
				= result.getFieldErrors().stream().map(FieldError::getField).distinct().toList();

			if(!(fieldList.size() == 1 &&
					"newPassword".equals(fieldList.getFirst()) &&
				newPassword.isEmpty())) {
					// 新しいパスワードが空欄で他にパラメータ不正がない場合は、スキップ
					// 新しいパスワード以外や新しいパスワードの入力に不正がある場合は、例外
					throw ErrorEnum.INVALID_INPUT.toException();
			}
		}

		AccountModel accountModel = AccountModel.from(accountUpdateRequest, sessionHelper.getAccountNo());

		Boolean isDuplicateAccountId = accountService.updateAccount(accountModel);

		return ResponseEntity.ok(AccountUpdateResponse.of(
					isDuplicateAccountId,
					!accountUpdateRequest.getAccountId().equals(sessionHelper.getAccountId()),
					!newPassword.isEmpty(),
					Consts.STRING_EMPTY));
	}
	
	/**
	 * アカウント削除
	 *
	 * @param	accountId				アカウントID
	 * @return							{@link ResponseEntity}
	 * @throws	GalleryException		認証ユーザーと異なるアカウントIDの場合
	 */
	@Operation(summary = "アカウント削除", description = "アカウントと関連データをすべて物理削除する")
	@ApiResponse(responseCode = "200", description = "削除成功")
	@ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
	@SecurityRequirement(name = "Bearer")
	@DeleteMapping(ApiRoutes.API_ACCOUNT)
	public ResponseEntity<Void> deleteAccount(
			@PathVariable String accountId) throws GalleryException {

		if (!accountId.equals(sessionHelper.getAccountId())) {
			throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.toException();
		}

		accountService.deleteAccount(new AccountNo(sessionHelper.getAccountNo()), new AccountId(accountId));

		return ResponseEntity.ok().build();
	}
}