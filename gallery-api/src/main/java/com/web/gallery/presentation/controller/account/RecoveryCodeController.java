package com.web.gallery.presentation.controller.account;

import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.request.account.RecoveryCodeRegenerateRequest;
import com.web.gallery.presentation.response.account.RecoveryCodeListResponse;
import com.web.gallery.presentation.response.account.RecoveryCodeStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * リカバリーコードの管理に関するAPI通信を扱うControllerクラス
 *
 * <p>いずれのAPIも本人のみが操作できる（パスのアカウントIDがセッションのアカウントIDと一致しない場合は403）
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "リカバリーコード", description = "パスキーをすべて失ったときの復旧に使うリカバリーコードの管理に関するAPI")
public class RecoveryCodeController {
  private final AccountService accountService;
  private final SessionHelper sessionHelper;

  /**
   * リカバリーコードの状態の取得
   *
   * @param accountId アカウントID
   * @return {@link RecoveryCodeStatusResponse}
   * @throws GalleryException 本人以外がアクセスした場合
   */
  @Operation(summary = "リカバリーコード状態取得", description = "未使用のリカバリーコードの件数を取得する（コード自体は再表示できない）")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス", content = @Content)
  @GetMapping(ApiRoutes.API_RECOVERY_CODES)
  public ResponseEntity<RecoveryCodeStatusResponse> getRecoveryCodeStatus(
      @PathVariable String accountId) throws GalleryException {
    verifySelf(accountId);
    return ResponseEntity.ok(
        RecoveryCodeStatusResponse.of(
            accountService.getRecoveryCodeCount(new AccountNo(sessionHelper.getAccountNo())),
            Consts.RECOVERY_CODE_COUNT));
  }

  /**
   * リカバリーコードの再発行
   *
   * @param accountId アカウントID
   * @param request {@link RecoveryCodeRegenerateRequest}
   * @param result RecoveryCodeRegenerateRequestのバインディング結果
   * @return {@link RecoveryCodeListResponse}
   * @throws GalleryException 本人以外のアクセス、リクエストパラメータ不正、本人確認の失敗の場合
   */
  @Operation(
      summary = "リカバリーコード再発行",
      description = "パスキーによる本人確認のうえ、リカバリーコードを再発行する。未使用のコードはすべて無効になる。発行したコードはこの応答でのみ返す")
  @ApiResponse(responseCode = "200", description = "再発行成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス・本人確認の失敗", content = @Content)
  @PostMapping(ApiRoutes.API_RECOVERY_CODES)
  public ResponseEntity<RecoveryCodeListResponse> regenerateRecoveryCodes(
      @PathVariable String accountId,
      @RequestBody @Validated RecoveryCodeRegenerateRequest request,
      BindingResult result)
      throws GalleryException {
    verifySelf(accountId);
    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    return ResponseEntity.ok(
        RecoveryCodeListResponse.from(
            accountService.regenerateRecoveryCodes(
                new AccountNo(sessionHelper.getAccountNo()),
                new PasskeyResponseJson(request.getCredential()))));
  }

  /**
   * パスのアカウントIDがセッションのアカウントIDと一致することを確かめる
   *
   * @param accountId パスのアカウントID
   * @throws GalleryException 一致しない場合
   */
  private void verifySelf(String accountId) throws GalleryException {
    if (!accountId.equals(sessionHelper.getAccountId())) {
      throw ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.toException();
    }
  }
}
