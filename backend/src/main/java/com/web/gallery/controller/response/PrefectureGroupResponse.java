package com.web.gallery.controller.response;

import com.web.gallery.model.KbnMstModelList;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 都道府県グループのレスポンスパラメータを保持するクラス */
@Schema(description = "都道府県グループレスポンス")
@Data
@Builder
public class PrefectureGroupResponse {
  /** グループ名 */
  @Schema(description = "グループ名（地方名）", example = "北海道・東北")
  private String groupName;

  /** 都道府県リスト */
  @Schema(description = "都道府県リスト")
  private List<PrefectureResponse> prefectures;

  /**
   * グループ名とKbnMstModelListからPrefectureGroupResponseを生成する
   *
   * @param groupName グループ名
   * @param kbnMstModels {@link KbnMstModelList}
   * @return {@link PrefectureGroupResponse}
   */
  public static PrefectureGroupResponse from(String groupName, KbnMstModelList kbnMstModels) {
    List<PrefectureResponse> prefectures =
        kbnMstModels.stream().map(PrefectureResponse::from).toList();

    return PrefectureGroupResponse.builder().groupName(groupName).prefectures(prefectures).build();
  }
}
