package com.web.gallery.controller.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.web.gallery.enumeration.InquiryStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 管理者用お問い合わせ一覧表示時のリクエストパラメータを保持するクラス */
@Schema(description = "管理者用お問い合わせ一覧リクエスト")
@Data
public class AdminInquiryListRequest {
  /**
   * ステータス区分による絞り込み（任意）
   *
   * <p>{@link InquiryStatusEnum}
   */
  @Schema(description = "ステータス区分（未指定の場合は全件）")
  private InquiryStatusEnum statusKbn;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
