package com.web.gallery.controller.response.common;

import com.web.gallery.model.common.LocationModelList;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** ロケーション一覧取得のレスポンスパラメータを保持するクラス */
@Schema(description = "ロケーション一覧取得レスポンス")
@Data
@Builder
public class LocationListGetResponse {
  /** ロケーションリスト */
  @Schema(description = "ロケーションリスト")
  private List<LocationResponse> locations;

  /**
   * LocationModelListからLocationListGetResponseを生成する
   *
   * @param locationModelList {@link LocationModelList}
   * @return {@link LocationListGetResponse}
   */
  public static LocationListGetResponse from(LocationModelList locationModelList) {
    return LocationListGetResponse.builder()
        .locations(locationModelList.stream().map(LocationResponse::from).toList())
        .build();
  }
}
