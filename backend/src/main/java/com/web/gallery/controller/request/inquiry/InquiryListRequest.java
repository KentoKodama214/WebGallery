package com.web.gallery.controller.request.inquiry;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 自分のお問い合わせ一覧表示時のリクエストパラメータを保持するクラス */
@Schema(description = "お問い合わせ一覧リクエスト")
@Data
public class InquiryListRequest {
  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
