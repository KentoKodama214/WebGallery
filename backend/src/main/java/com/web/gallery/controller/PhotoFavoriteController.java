package com.web.gallery.controller;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.PhotoFavoriteDeleteRequest;
import com.web.gallery.controller.request.PhotoFavoriteRegistRequest;
import com.web.gallery.controller.response.PhotoFavoriteResponse;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.service.PhotoFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 写真のお気に入りの登録・解除を扱うRestControllerクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "お気に入り", description = "お気に入り管理に関するAPI")
public class PhotoFavoriteController {

  private final PhotoFavoriteService photoFavoriteService;
  private final SessionHelper sessionHelper;

  /**
   * お気に入り登録
   *
   * @param photoFavoriteRegistRequest {@link PhotoFavoriteRegistRequest}
   * @param result PhotoFavoriteRegistRequestのバインディング結果
   * @return PhotoFavoriteResponse {@link PhotoFavoriteResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・リクエストパラメータが不正の場合 ・お気に入りの登録に失敗した場合
   */
  @Operation(summary = "お気に入り登録", description = "指定した写真をお気に入りに登録する")
  @ApiResponse(responseCode = "200", description = "登録成功")
  @ApiResponse(responseCode = "409", description = "既にお気に入りに登録済み", content = @Content)
  @SecurityRequirement(name = "Bearer")
  @PostMapping(ApiRoutes.API_FAVORITES)
  public ResponseEntity<PhotoFavoriteResponse> addFavorite(
      @RequestBody @Validated PhotoFavoriteRegistRequest photoFavoriteRegistRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      log.info(
          "Invalid input. (FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
          photoFavoriteRegistRequest.getFavoritePhotoAccountNo(),
          photoFavoriteRegistRequest.getFavoritePhotoNo());
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    photoFavoriteService.addFavorite(
        PhotoFavoriteModel.from(photoFavoriteRegistRequest, sessionHelper.getAccountNo()));

    return ResponseEntity.ok(PhotoFavoriteResponse.of(MessageConst.REGIST_FAVORITE));
  }

  /**
   * お気に入り解除
   *
   * @param photoFavoriteDeleteRequest {@link PhotoFavoriteDeleteRequest}
   * @param result PhotoFavoriteDeleteRequestのバインディング結果
   * @return PhotoFavoriteResponse {@link PhotoFavoriteResponse}
   * @throws GalleryException 以下のいずれかに該当する場合 ・リクエストパラメータが不正の場合 ・お気に入りの解除に失敗した場合
   */
  @Operation(summary = "お気に入り解除", description = "指定した写真のお気に入りを解除する")
  @ApiResponse(responseCode = "200", description = "解除成功")
  @ApiResponse(responseCode = "409", description = "お気に入りに登録されていない", content = @Content)
  @SecurityRequirement(name = "Bearer")
  @DeleteMapping(ApiRoutes.API_FAVORITES)
  public ResponseEntity<PhotoFavoriteResponse> deleteFavorite(
      @RequestBody @Validated PhotoFavoriteDeleteRequest photoFavoriteDeleteRequest,
      BindingResult result)
      throws GalleryException {

    if (result.hasErrors()) {
      log.info(
          "Invalid input. (FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
          photoFavoriteDeleteRequest.getFavoritePhotoAccountNo(),
          photoFavoriteDeleteRequest.getFavoritePhotoNo());
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    photoFavoriteService.deleteFavorite(
        PhotoFavoriteModel.from(photoFavoriteDeleteRequest, sessionHelper.getAccountNo()));

    return ResponseEntity.ok(PhotoFavoriteResponse.of(MessageConst.CANCEL_FAVORITE));
  }
}
