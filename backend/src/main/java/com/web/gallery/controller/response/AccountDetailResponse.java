package com.web.gallery.controller.response;

import com.web.gallery.constant.Consts;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/** アカウント詳細情報のレスポンスパラメータを保持するクラス */
@Schema(description = "アカウント詳細レスポンス")
@Data
@Builder
public class AccountDetailResponse {
  /** アカウントID */
  @Schema(description = "アカウントID", example = "testuser01")
  private String accountId;

  /** アカウント名 */
  @Schema(description = "アカウント名", example = "テストユーザー")
  private String accountName;

  /** 生年月日 */
  @Schema(description = "生年月日", example = "1990-01-01")
  private LocalDate birthdate;

  /** 性別区分 */
  @Schema(description = "性別区分")
  private SexEnum sexKbn;

  /** 出身地都道府県区分コード */
  @Schema(description = "出身都道府県区分コード", example = "Hokkaido")
  private String birthplacePrefectureKbnCode;

  /** 居住地都道府県区分コード */
  @Schema(description = "在住都道府県区分コード", example = "Tokyo")
  private String residentPrefectureKbnCode;

  /** フリーメモ */
  @Schema(description = "フリーメモ")
  private String freeMemo;

  /**
   * AccountModelからAccountDetailResponseを生成する
   *
   * @param model {@link AccountModel}
   * @return {@link AccountDetailResponse}
   */
  public static AccountDetailResponse from(AccountModel model) {
    return AccountDetailResponse.builder()
        .accountId(model.getAccountId().value())
        .accountName(model.getAccountName().value())
        .birthdate(
            model.getBirthdate() == null
                ? null
                : (Consts.MIN_LOCAL_DATE.equals(model.getBirthdate().value())
                    ? null
                    : model.getBirthdate().value()))
        .sexKbn(model.getSexKbn())
        .birthplacePrefectureKbnCode(
            model.getBirthplacePrefectureKbnCode() == null
                ? null
                : model.getBirthplacePrefectureKbnCode().value())
        .residentPrefectureKbnCode(
            model.getResidentPrefectureKbnCode() == null
                ? null
                : model.getResidentPrefectureKbnCode().value())
        .freeMemo(model.getFreeMemo() == null ? null : model.getFreeMemo().value())
        .build();
  }
}
