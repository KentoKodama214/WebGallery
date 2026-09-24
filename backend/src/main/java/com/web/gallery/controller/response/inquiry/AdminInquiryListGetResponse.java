package com.web.gallery.controller.response.inquiry;

import com.web.gallery.model.inquiry.InquiryPageModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 管理者用お問い合わせ一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用お問い合わせ一覧レスポンス")
@Data
@Builder
public class AdminInquiryListGetResponse {
  /** 最後まで取得できたか */
  @Schema(description = "最後のページかどうか")
  private Boolean isLast;

  /** お問い合わせ一覧 */
  @Schema(description = "お問い合わせ一覧")
  private List<AdminInquiryListItemResponse> inquiryList;

  /**
   * InquiryPageModelからAdminInquiryListGetResponseを生成する
   *
   * @param inquiryPageModel {@link InquiryPageModel}
   * @return {@link AdminInquiryListGetResponse}
   */
  public static AdminInquiryListGetResponse from(InquiryPageModel inquiryPageModel) {
    List<AdminInquiryListItemResponse> inquiryListItemResponseList =
        inquiryPageModel.getInquiryModelList().stream()
            .map(AdminInquiryListItemResponse::from)
            .toList();

    return AdminInquiryListGetResponse.builder()
        .isLast(inquiryPageModel.getIsLast())
        .inquiryList(inquiryListItemResponseList)
        .build();
  }
}
