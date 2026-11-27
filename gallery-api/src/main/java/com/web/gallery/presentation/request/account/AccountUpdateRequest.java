package com.web.gallery.presentation.request.account;

import com.web.gallery.domain.enumeration.SexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** アカウント更新時のリクエストパラメータを保持するクラス */
@Schema(description = "アカウント更新リクエスト")
@Data
public class AccountUpdateRequest {
  /**
   * アカウントID
   *
   * <p>半角英数8〜20桁、ブランクなし
   */
  @Schema(description = "アカウントID（半角英数8〜20桁）", example = "testuser01")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(min = 8, max = 20, message = "{validation.common.min_max_length}")
  @Pattern(regexp = "[a-zA-Z0-9]{8,20}", message = "{validation.common.pattern}")
  private String accountId;

  /**
   * アカウント名
   *
   * <p>ブランクなし
   */
  @Schema(description = "アカウント名", example = "テストユーザー")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = 50, message = "{validation.common.max_length}")
  @Pattern(regexp = "[^　]+", message = "{validation.common.all_space}")
  private String accountName;

  /**
   * 生年月日
   *
   * <p>yyyy-mm-ddで、過去日付
   */
  @Schema(description = "生年月日", example = "1990-01-01")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Past(message = "{validation.common.pastDate}")
  private LocalDate birthdate;

  /**
   * 性別区分
   *
   * <p>{@link SexEnum}
   */
  @Schema(description = "性別区分")
  private SexEnum sexKbn;

  /** 出身都道府県区分コード */
  @Schema(description = "出身都道府県区分コード", example = "Hokkaido")
  @Size(max = 20, message = "{validation.common.max_length}")
  @Pattern(regexp = "[a-zA-Z0-9]*", message = "{validation.common.pattern}")
  private String birthplacePrefectureKbnCode;

  /** 在住都道府県区分コード */
  @Schema(description = "在住都道府県区分コード", example = "Tokyo")
  @Size(max = 20, message = "{validation.common.max_length}")
  @Pattern(regexp = "[a-zA-Z0-9]*", message = "{validation.common.pattern}")
  private String residentPrefectureKbnCode;

  /** フリーメモ */
  @Schema(description = "フリーメモ", example = "よろしくお願いします")
  @Size(max = 1000, message = "{validation.common.max_length}")
  private String freeMemo;
}
