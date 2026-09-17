package com.web.gallery.controller.response;

import com.web.gallery.model.InquiryPageModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 自分のお問い合わせ一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ一覧レスポンス")
@Data
@Builder
public class InquiryListGetResponse {
  /** 最後まで取得できたか */
  @Schema(description = "最後のページかどうか")
  private Boolean isLast;

  /** お問い合わせ一覧 */
  @Schema(description = "お問い合わせ一覧")
  private List<InquiryListItemResponse> inquiryList;

  /**
   * InquiryPageModelからInquiryListGetResponseを生成する
   *
   * @param inquiryPageModel {@link InquiryPageModel}
   * @return {@link InquiryListGetResponse}
   */
  public static InquiryListGetResponse from(InquiryPageModel inquiryPageModel) {
    List<InquiryListItemResponse> inquiryListItemResponseList =
        inquiryPageModel.getInquiryModelList().stream().map(InquiryListItemResponse::from).toList();

    return InquiryListGetResponse.builder()
        .isLast(inquiryPageModel.getIsLast())
        .inquiryList(inquiryListItemResponseList)
        .build();
  }
}
