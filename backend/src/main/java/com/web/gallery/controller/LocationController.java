package com.web.gallery.controller;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.controller.response.common.LocationListGetResponse;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.common.LocationModelList;
import com.web.gallery.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * ロケーションに関するAPI通信を扱うControllerクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "ロケーション", description = "ロケーションマスタに関するAPI")
public class LocationController {

  private final LocationService locationService;
  private final SessionHelper sessionHelper;

  /**
   * ロケーション一覧取得
   *
   * <p>本人が登録済みのロケーションマスタの一覧を取得する。本人以外からのリクエストの場合は空のリストを返す （ロケーションマスタは本人のみが選択・参照できるプライベートマスタのため）
   *
   * @param accountId ページ所有者のアカウントID
   * @return {@link LocationListGetResponse}
   */
  @Operation(summary = "ロケーション一覧取得", description = "本人が登録済みのロケーションマスタの一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @SecurityRequirement(name = "Bearer")
  @GetMapping(ApiRoutes.API_LOCATIONS)
  public ResponseEntity<LocationListGetResponse> getLocationList(@PathVariable String accountId) {
    LocationModelList locationModelList =
        accountId.equals(sessionHelper.getAccountId())
            ? locationService.getLocationList(new AccountNo(sessionHelper.getAccountNo()))
            : LocationModelList.empty();
    return ResponseEntity.ok(LocationListGetResponse.from(locationModelList));
  }
}
