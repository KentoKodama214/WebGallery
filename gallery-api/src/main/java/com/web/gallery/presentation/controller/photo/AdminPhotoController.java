package com.web.gallery.presentation.controller.photo;

import com.web.gallery.application.model.photo.AdminPhotoPageModel;
import com.web.gallery.application.service.photo.PhotoService;
import com.web.gallery.domain.constant.ApiRoutes;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.infrastructure.security.RequireAdminAuthority;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.infrastructure.web.ValidationErrorLogger;
import com.web.gallery.presentation.converter.photo.PhotoConverter;
import com.web.gallery.presentation.request.photo.AdminPhotoListRequest;
import com.web.gallery.presentation.request.photo.AdminPhotoVisibilityUpdateRequest;
import com.web.gallery.presentation.response.photo.AdminPhotoListGetResponse;
import com.web.gallery.presentation.response.photo.AdminPhotoVisibilityUpdateResponse;
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

/** 管理者用写真管理に関するAPI通信を扱うControllerクラス */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "管理者写真管理", description = "管理者用写真管理に関するAPI")
@SecurityRequirement(name = "Bearer")
public class AdminPhotoController {

  private final PhotoService photoService;
  private final SessionHelper sessionHelper;
  private final PhotoConverter photoConverter;

  /**
   * 管理者用写真一覧取得
   *
   * @param adminPhotoListRequest {@link AdminPhotoListRequest}
   * @param result バリデーション結果
   * @return {@link AdminPhotoListGetResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合
   */
  @Operation(summary = "管理者用写真一覧取得", description = "管理者により非公開にされた写真を含む、全アカウントの写真一覧を登録日時の新しい順に取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @RequireAdminAuthority
  @GetMapping(ApiRoutes.API_ADMIN_PHOTOS)
  public ResponseEntity<AdminPhotoListGetResponse> getAdminPhotoList(
      @ParameterObject @ModelAttribute @Validated AdminPhotoListRequest adminPhotoListRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    AdminPhotoPageModel adminPhotoPageModel =
        photoService.getPhotoListForAdmin(
            photoConverter.toAdminPhotoListGetModel(adminPhotoListRequest));

    return ResponseEntity.ok(AdminPhotoListGetResponse.from(adminPhotoPageModel));
  }

  /**
   * 管理者による写真の公開状態変更（非公開・非公開解除）
   *
   * @param accountNo 写真所有者のアカウント番号
   * @param photoNo 写真番号
   * @param request {@link AdminPhotoVisibilityUpdateRequest}
   * @param result バリデーション結果
   * @return {@link AdminPhotoVisibilityUpdateResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・管理者権限がない場合 ・リクエストパラメータが不正な場合 ・写真が存在しない場合
   */
  @Operation(
      summary = "管理者による写真の公開状態変更",
      description = "指定した写真を非公開にする、または非公開を解除する。操作は理由とともに履歴に記録する")
  @ApiResponse(responseCode = "200", description = "変更成功")
  @ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
  @ApiResponse(responseCode = "403", description = "管理者権限がない", content = @Content)
  @ApiResponse(responseCode = "404", description = "写真が存在しない", content = @Content)
  @RequireAdminAuthority
  @PutMapping(ApiRoutes.API_ADMIN_PHOTO_VISIBILITY)
  public ResponseEntity<AdminPhotoVisibilityUpdateResponse> updatePhotoVisibility(
      @PathVariable Long accountNo,
      @PathVariable Long photoNo,
      @RequestBody @Validated AdminPhotoVisibilityUpdateRequest request,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      ValidationErrorLogger.logFieldErrors(log, result);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    photoService.changePhotoVisibilityByAdmin(
        photoConverter.toPhotoVisibilityChangeModel(
            request, accountNo, photoNo, new AccountNo(sessionHelper.getAccountNo())));

    return ResponseEntity.ok(
        AdminPhotoVisibilityUpdateResponse.of(
            request.getIsHiddenByAdmin() ? MessageConst.HIDE_PHOTO : MessageConst.UNHIDE_PHOTO));
  }
}
