package com.web.gallery.controller;

import com.web.gallery.annotation.RequireAdminAuthority;
import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.account.AccountListRequest;
import com.web.gallery.controller.request.account.AdminAccountAuthorityUpdateRequest;
import com.web.gallery.controller.response.account.AdminAccountAuthorityUpdateResponse;
import com.web.gallery.controller.response.account.AdminAccountListGetResponse;
import com.web.gallery.controller.response.account.AdminAccountLockResponse;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.ValidationErrorLogger;
import com.web.gallery.model.account.AccountListGetModel;
import com.web.gallery.model.account.AccountModel;
import com.web.gallery.model.account.AccountPageModel;
import com.web.gallery.service.AccountService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 管理者用アカウント管理に関するAPI通信を扱うControllerクラス */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "管理者アカウント管理", description = "管理者用アカウント管理に関するAPI")
@SecurityRequirement(name = "Bearer")
public class AdminAccountController {
  private final AccountService accountService;

  /**
   * 管理者用アカウント一覧取得
   *
   * @param accountListRequest {@link AccountListRequest}
   * @param result バリデーション結果
   * @return {@link AdminAccountListGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合
   */
  @Operation(summary = "管理者用アカウント一覧取得", description = "削除済みを含む全アカウントの一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @GetMapping(ApiRoutes.API_ADMIN_ACCOUNTS)
  public ResponseEntity<AdminAccountListGetResponse> getAdminAccountList(
      @ParameterObject @ModelAttribute @Validated AccountListRequest accountListRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AccountPageModel accountPageModel =
        accountService.getAccountListForAdmin(AccountListGetModel.from(accountListRequest));

    return ResponseEntity.ok(AdminAccountListGetResponse.from(accountPageModel));
  }

  /**
   * アカウントロック解除
   *
   * @param accountNo アカウント番号
   * @return {@link AdminAccountLockResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・更新に失敗した場合
   */
  @Operation(summary = "アカウントロック解除", description = "指定したアカウントのロックを解除する")
  @ApiResponse(responseCode = "200", description = "ロック解除成功")
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @PutMapping(ApiRoutes.API_ADMIN_ACCOUNT_UNLOCK)
  public ResponseEntity<AdminAccountLockResponse> unlockAccount(@PathVariable Long accountNo)
      throws GalleryException {

    accountService.unlockAccount(new AccountNo(accountNo));

    return ResponseEntity.ok(AdminAccountLockResponse.of(MessageConst.UNLOCK_ACCOUNT));
  }

  /**
   * アカウント強制ロック
   *
   * @param accountNo アカウント番号
   * @return {@link AdminAccountLockResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・更新に失敗した場合
   */
  @Operation(summary = "アカウント強制ロック", description = "指定したアカウントを強制的にロックする")
  @ApiResponse(responseCode = "200", description = "ロック成功")
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @PutMapping(ApiRoutes.API_ADMIN_ACCOUNT_LOCK)
  public ResponseEntity<AdminAccountLockResponse> lockAccount(@PathVariable Long accountNo)
      throws GalleryException {

    accountService.lockAccount(new AccountNo(accountNo));

    return ResponseEntity.ok(AdminAccountLockResponse.of(MessageConst.LOCK_ACCOUNT));
  }

  /**
   * アカウント権限変更
   *
   * @param accountNo アカウント番号
   * @param request {@link AdminAccountAuthorityUpdateRequest}
   * @param result バリデーション結果
   * @return {@link AdminAccountAuthorityUpdateResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合 ・更新に失敗した場合
   */
  @Operation(summary = "アカウント権限変更", description = "指定したアカウントの権限区分を変更する")
  @ApiResponse(responseCode = "200", description = "権限変更成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @PutMapping(ApiRoutes.API_ADMIN_ACCOUNT_AUTHORITY)
  public ResponseEntity<AdminAccountAuthorityUpdateResponse> updateAccountAuthority(
      @PathVariable Long accountNo,
      @RequestBody @Validated AdminAccountAuthorityUpdateRequest request,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    accountService.updateAccountAuthority(
        AccountModel.forAuthorityChange(accountNo, request.getAuthorityKbn()));

    return ResponseEntity.ok(
        AdminAccountAuthorityUpdateResponse.of(MessageConst.UPDATE_ACCOUNT_AUTHORITY));
  }
}
