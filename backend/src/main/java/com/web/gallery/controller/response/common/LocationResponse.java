package com.web.gallery.controller.response.common;

import com.web.gallery.model.common.LocationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** ロケーションのレスポンスパラメータを保持するクラス */
@Schema(description = "ロケーションレスポンス")
@Data
@Builder
public class LocationResponse {
  /** ロケーション番号 */
  @Schema(description = "ロケーション番号", example = "1")
  private Long locationNo;

  /** ロケーション名 */
  @Schema(description = "ロケーション名", example = "渋谷スクランブル交差点")
  private String locationName;

  /** 住所 */
  @Schema(description = "住所", example = "東京都渋谷区")
  private String address;

  /** 緯度 */
  @Schema(description = "緯度", example = "35.6812")
  private BigDecimal latitude;

  /** 経度 */
  @Schema(description = "経度", example = "139.7671")
  private BigDecimal longitude;

  /**
   * LocationModelからLocationResponseを生成する
   *
   * @param model {@link LocationModel}
   * @return {@link LocationResponse}
   */
  public static LocationResponse from(LocationModel model) {
    return LocationResponse.builder()
        .locationNo(model.getLocationNo().value())
        .locationName(model.getLocationName().value())
        .address(model.getGeoLocation().address().value())
        .latitude(model.getGeoLocation().latitude().value())
        .longitude(model.getGeoLocation().longitude().value())
        .build();
  }
}
