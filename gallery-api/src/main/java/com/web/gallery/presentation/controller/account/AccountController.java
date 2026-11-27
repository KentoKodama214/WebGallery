package com.web.gallery.presentation.controller.account;

import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.converter.account.AccountConverter;
import com.web.gallery.presentation.request.account.AccountDeleteRequest;
import com.web.gallery.presentation.request.account.AccountListRequest;
import com.web.gallery.presentation.request.account.AccountRegistRequest;
import com.web.gallery.presentation.request.account.AccountSignupOptionsRequest;
import com.web.gallery.presentation.request.account.AccountUpdateRequest;
import com.web.gallery.presentation.response.account.AccountDetailResponse;
import com.web.gallery.presentation.response.account.AccountListGetResponse;
import com.web.gallery.presentation.response.account.AccountRegistResponse;
import com.web.gallery.presentation.response.account.AccountUpdateResponse;
import com.web.gallery.presentation.response.account.PasskeyRegistrationOptionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * アカウントに関するAPI通信を扱うControllerクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "アカウント", description = "アカウント管理に関するAPI")
public class AccountController {

  private final AccountService accountService;
  private final SessionHelper sessionHelper;
  private final AccountConverter accountConverter;

  /**
   * アカウント一覧取得
   *
   * @param accountListRequest {@link AccountListRequest}
   * @param result バリデーション結果
   * @return {@link AccountListGetResponse}
   * @throws GalleryException リクエストパラメータが不正な場合
   */
  @Operation(summary = "アカウント一覧取得", description = "登録されているアカウントの一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @GetMapping(ApiRoutes.API_ACCOUNTS)
  public ResponseEntity<AccountListGetResponse> getAccountList(
      @ParameterObject @ModelAttribute @Validated AccountListRequest accountListRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountPageModel accountPageModel =
        accountService.getAccountList(accountConverter.toAccountListGetModel(accountListRequest));

    return ResponseEntity.ok(AccountListGetResponse.from(accountPageModel));
  }

  /**
   * アカウント詳細取得
   *
   * @param accountId アカウントID
   * @return {@link AccountDetailResponse}
   * @throws GalleryException 認証ユーザーと異なるアカウントIDの場合
   */
  @Operation(summary = "アカウント詳細取得", description = "指定したアカウントの詳細情報を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "認証ユーザーと異なるアカウントIDを指定", content = @Content)
  @SecurityRequirement(name = "Bearer")
  @GetMapping(ApiRoutes.API_ACCOUNT)
  public ResponseEntity<AccountDetailResponse> getAccount(@PathVariable String accountId)
      throws GalleryException {

    if (!accountId.equals(sessionHelper.getAccountId())) {
      throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.toException();
    }

    AccountModel accountModel = accountService.getAccountById(new AccountId(accountId));

    AccountDetailResponse response = AccountDetailResponse.from(accountModel);

    return ResponseEntity.ok(response);
  }

  /**
   * アカウント新規登録オプションの発行
   *
   * @param request {@link AccountSignupOptionsRequest}
   * @param result AccountSignupOptionsRequestのバインディング結果
   * @return {@link PasskeyRegistrationOptionsResponse}
   * @throws GalleryException リクエストパラメータが不正な場合、アカウントIDが既に使われている場合
   */
  @Operation(
      summary = "アカウント新規登録オプション発行",
      description =
          "アカウントと同時に登録する最初のパスキーを作成するためのチャレンジ等（PublicKeyCredentialCreationOptionsJSON）を発行する")
  @ApiResponse(responseCode = "200", description = "発行成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "409", description = "アカウントIDが既に使用されている", content = @Content)
  @PostMapping(ApiRoutes.API_ACCOUNT_SIGNUP_OPTIONS)
  public ResponseEntity<PasskeyRegistrationOptionsResponse> signupOptions(
      @RequestBody @Validated AccountSignupOptionsRequest request, BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    return ResponseEntity.ok(
        PasskeyRegistrationOptionsResponse.from(
            accountService.createSignupOptions(
                new AccountId(request.getAccountId()), new AccountName(request.getAccountName()))));
  }

  /**
   * アカウント登録
   *
   * @param accountRegistRequest {@link AccountRegistRequest}
   * @param result AccountRegistRequestのバインディング結果
   * @return {@link AccountRegistResponse}（発行したリカバリーコードを含む）
   * @throws GalleryException 以下のいずれかに該当する場合 ・リクエストパラメータが不正の場合 ・パスキーの登録レスポンスの検証に失敗した場合
   *     ・アカウントIDが既に使われている場合
   */
  @Operation(
      summary = "アカウント登録",
      description = "最初のパスキーの登録レスポンスを検証し、新規アカウントとパスキーを登録する。あわせてリカバリーコードを発行して返す（再表示不可）")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ不正・パスキーの登録レスポンスの検証失敗",
      content = @Content)
  @ApiResponse(
      responseCode = "409",
      description = "アカウントIDが既に使用されている・登録済みのパスキー",
      content = @Content)
  @PostMapping(ApiRoutes.API_ACCOUNTS)
  public ResponseEntity<AccountRegistResponse> register(
      @RequestBody @Validated AccountRegistRequest accountRegistRequest, BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountModel accountModel = accountConverter.toAccountModelForRegist(accountRegistRequest);

    RecoveryCodeList recoveryCodes =
        accountService.registAccount(
            accountModel,
            new PasskeyResponseJson(accountRegistRequest.getCredential()),
            new PasskeyName(accountRegistRequest.getPasskeyName().strip()));
    return ResponseEntity.ok(AccountRegistResponse.of(true, Consts.STRING_EMPTY, recoveryCodes));
  }

  /**
   * アカウント更新
   *
   * @param accountId アカウントID
   * @param accountUpdateRequest {@link AccountUpdateRequest}
   * @param result AccountUpdateRequestのバインディング結果
   * @return {@link AccountUpdateResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・リクエストパラメータが不正の場合 ・更新に失敗した場合
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
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountModel accountModel =
        accountConverter.toAccountModelForUpdate(
            accountUpdateRequest, sessionHelper.getAccountNo());

    Boolean isDuplicateAccountId = accountService.updateAccount(accountModel);

    return ResponseEntity.ok(
        AccountUpdateResponse.of(
            isDuplicateAccountId,
            !accountUpdateRequest.getAccountId().equals(sessionHelper.getAccountId()),
            Consts.STRING_EMPTY));
  }

  /**
   * アカウント削除
   *
   * <p>本人確認に用いるパスキーの認証レスポンスはリクエストボディ（JSON）で受け取る。 DELETEのボディは一部の中継経路で破棄されうるため、 DELETEではなく {@code
   * POST /api/v1/accounts/{accountId}/deletion} のサブリソースとして提供する。
   *
   * @param accountId アカウントID
   * @param accountDeleteRequest {@link AccountDeleteRequest}
   * @param result AccountDeleteRequestのバインディング結果
   * @return {@link ResponseEntity}
   * @throws GalleryException 以下のいずれかに該当する場合 ・認証ユーザーと異なるアカウントIDの場合 ・リクエストパラメータが不正の場合
   *     ・パスキーによる本人確認に失敗した場合
   */
  @Operation(summary = "アカウント削除", description = "パスキーによる再認証のうえ、アカウントと関連データをすべて物理削除する")
  @ApiResponse(responseCode = "200", description = "削除成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "認証ユーザーと異なるアカウントIDを指定、またはパスキーによる本人確認の失敗",
      content = @Content)
  @SecurityRequirement(name = "Bearer")
  @PostMapping(ApiRoutes.API_ACCOUNT_DELETION)
  public ResponseEntity<Void> deleteAccount(
      @PathVariable String accountId,
      @RequestBody @Validated AccountDeleteRequest accountDeleteRequest,
      BindingResult result)
      throws GalleryException {

    if (!accountId.equals(sessionHelper.getAccountId())) {
      throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.toException();
    }

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    accountService.deleteAccount(
        new AccountNo(sessionHelper.getAccountNo()),
        new AccountId(accountId),
        new PasskeyResponseJson(accountDeleteRequest.getCredential()));

    return ResponseEntity.ok().build();
  }
}
