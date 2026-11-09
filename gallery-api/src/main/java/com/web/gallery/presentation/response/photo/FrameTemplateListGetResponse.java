package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.FrameTemplateModelList;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 額縁テンプレート一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "額縁テンプレート一覧レスポンス")
@Data
@Builder
public class FrameTemplateListGetResponse {
  /** 額縁テンプレート一覧（額縁テンプレート番号の昇順） */
  @Schema(description = "額縁テンプレート一覧（額縁テンプレート番号の昇順）")
  private List<FrameTemplateItemResponse> frameTemplateList;

  /**
   * FrameTemplateModelListからFrameTemplateListGetResponseを生成する
   *
   * @param modelList {@link FrameTemplateModelList}
   * @return {@link FrameTemplateListGetResponse}
   */
  public static FrameTemplateListGetResponse from(FrameTemplateModelList modelList) {
    return FrameTemplateListGetResponse.builder()
        .frameTemplateList(modelList.stream().map(FrameTemplateItemResponse::from).toList())
        .build();
  }
}
