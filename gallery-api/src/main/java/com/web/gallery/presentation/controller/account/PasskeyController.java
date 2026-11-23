package com.web.gallery.presentation.controller.account;

import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.Password;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.request.account.PasskeyDeleteRequest;
import com.web.gallery.presentation.request.account.PasskeyRegistRequest;
import com.web.gallery.presentation.request.account.PasskeyRegistrationOptionsRequest;
import com.web.gallery.presentation.request.account.PasskeyUpdateRequest;
import com.web.gallery.presentation.response.account.PasskeyListResponse;
import com.web.gallery.presentation.response.account.PasskeyRegistrationOptionsResponse;
import com.web.gallery.presentation.response.account.PasskeyResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * パスキー（WebAuthnクレデンシャル）の管理に関するAPI通信を扱うControllerクラス
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
@Tag(name = "パスキー", description = "パスキーの登録・管理に関するAPI")
public class PasskeyController {
  private final AccountService accountService;
  private final SessionHelper sessionHelper;

  /**
   * パスキー一覧の取得
   *
   * @param accountId アカウントID
   * @return {@link PasskeyListResponse}
   * @throws GalleryException 本人以外がアクセスした場合
   */
  @Operation(summary = "パスキー一覧取得", description = "本人のパスキーを登録日時の昇順で取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス", content = @Content)
  @GetMapping(ApiRoutes.API_PASSKEYS)
  public ResponseEntity<PasskeyListResponse> getPasskeys(@PathVariable String accountId)
      throws GalleryException {
    verifySelf(accountId);
    return ResponseEntity.ok(
        PasskeyListResponse.from(
            accountService.getPasskeyList(new AccountNo(sessionHelper.getAccountNo())),
            Consts.PASSKEY_MAX_COUNT));
  }

  /**
   * パスキー登録オプションの発行
   *
   * @param accountId アカウントID
   * @param request {@link PasskeyRegistrationOptionsRequest}
   * @param result PasskeyRegistrationOptionsRequestのバインディング結果
   * @return {@link PasskeyRegistrationOptionsResponse}
   * @throws GalleryException 本人以外のアクセス、リクエストパラメータ不正、現在のパスワード不一致、登録数上限の場合
   */
  @Operation(
      summary = "パスキー登録オプション発行",
      description =
          "現在のパスワードによる本人確認のうえ、パスキー登録用のチャレンジ等（PublicKeyCredentialCreationOptionsJSON）を発行する")
  @ApiResponse(responseCode = "200", description = "発行成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正・登録数上限", content = @Content)
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス・現在のパスワード不一致", content = @Content)
  @PostMapping(ApiRoutes.API_PASSKEY_REGISTRATION_OPTIONS)
  public ResponseEntity<PasskeyRegistrationOptionsResponse> createRegistrationOptions(
      @PathVariable String accountId,
      @RequestBody @Validated PasskeyRegistrationOptionsRequest request,
      BindingResult result)
      throws GalleryException {
    verifySelf(accountId);
    rejectIfInvalid(result);

    return ResponseEntity.ok(
        PasskeyRegistrationOptionsResponse.from(
            accountService.createPasskeyRegistrationOptions(
                new AccountNo(sessionHelper.getAccountNo()),
                new Password(request.getCurrentPassword()))));
  }

  /**
   * パスキーの登録
   *
   * @param accountId アカウントID
   * @param request {@link PasskeyRegistRequest}
   * @param result PasskeyRegistRequestのバインディング結果
   * @return 登録した{@link PasskeyResponse}
   * @throws GalleryException 本人以外のアクセス、リクエストパラメータ不正、登録レスポンスの検証失敗、登録済み、登録数上限の場合
   */
  @Operation(summary = "パスキー登録", description = "パスキーの登録レスポンスを検証し、パスキーを登録する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(
      responseCode = "400",
      description = "リクエストパラメータ不正・登録レスポンスの検証失敗・登録数上限",
      content = @Content)
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス", content = @Content)
  @ApiResponse(responseCode = "409", description = "登録済みのパスキー", content = @Content)
  @PostMapping(ApiRoutes.API_PASSKEYS)
  public ResponseEntity<PasskeyResponse> registPasskey(
      @PathVariable String accountId,
      @RequestBody @Validated PasskeyRegistRequest request,
      BindingResult result)
      throws GalleryException {
    verifySelf(accountId);
    rejectIfInvalid(result);

    return ResponseEntity.ok(
        PasskeyResponse.from(
            accountService.registerPasskey(
                new AccountNo(sessionHelper.getAccountNo()),
                new PasskeyResponseJson(request.getCredential()),
                new PasskeyName(request.getPasskeyName().strip()))));
  }

  /**
   * パスキー名の変更
   *
   * @param accountId アカウントID
   * @param passkeyNo パスキー番号
   * @param request {@link PasskeyUpdateRequest}
   * @param result PasskeyUpdateRequestのバインディング結果
   * @return 200 OK
   * @throws GalleryException 本人以外のアクセス、リクエストパラメータ不正、パスキーが存在しない場合
   */
  @Operation(summary = "パスキー名変更", description = "パスキーの名前を変更する")
  @ApiResponse(responseCode = "200", description = "変更成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス", content = @Content)
  @ApiResponse(responseCode = "404", description = "パスキーが存在しない", content = @Content)
  @PutMapping(ApiRoutes.API_PASSKEY)
  public ResponseEntity<Void> updatePasskey(
      @PathVariable String accountId,
      @PathVariable Long passkeyNo,
      @RequestBody @Validated PasskeyUpdateRequest request,
      BindingResult result)
      throws GalleryException {
    verifySelf(accountId);
    rejectIfInvalid(result);

    accountService.renamePasskey(
        new AccountNo(sessionHelper.getAccountNo()),
        new PasskeyNo(passkeyNo),
        new PasskeyName(request.getPasskeyName().strip()));
    return ResponseEntity.ok().build();
  }

  /**
   * パスキーの削除
   *
   * @param accountId アカウントID
   * @param passkeyNo パスキー番号
   * @param request {@link PasskeyDeleteRequest}
   * @param result PasskeyDeleteRequestのバインディング結果
   * @return 200 OK
   * @throws GalleryException 本人以外のアクセス、リクエストパラメータ不正、現在のパスワード不一致、パスキーが存在しない場合
   */
  @Operation(summary = "パスキー削除", description = "現在のパスワードによる本人確認のうえ、パスキーを削除する")
  @ApiResponse(responseCode = "200", description = "削除成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "本人以外のアクセス・現在のパスワード不一致", content = @Content)
  @ApiResponse(responseCode = "404", description = "パスキーが存在しない", content = @Content)
  @PostMapping(ApiRoutes.API_PASSKEY_DELETION)
  public ResponseEntity<Void> deletePasskey(
      @PathVariable String accountId,
      @PathVariable Long passkeyNo,
      @RequestBody @Validated PasskeyDeleteRequest request,
      BindingResult result)
      throws GalleryException {
    verifySelf(accountId);
    rejectIfInvalid(result);

    accountService.deletePasskey(
        new AccountNo(sessionHelper.getAccountNo()),
        new PasskeyNo(passkeyNo),
        new Password(request.getCurrentPassword()));
    return ResponseEntity.ok().build();
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

  /**
   * バインディング結果にエラーがあれば入力エラーとする
   *
   * @param result バインディング結果
   * @throws GalleryException エラーがある場合
   */
  private void rejectIfInvalid(BindingResult result) throws GalleryException {
    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }
  }
}
