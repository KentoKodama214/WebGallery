package com.web.gallery.presentation.request.account;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.SexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** アカウント登録時のリクエストパラメータを保持するクラス */
@Schema(description = "アカウント登録リクエスト")
@Data
public class AccountRegistRequest {
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
   * 登録レスポンス
   *
   * <p>アカウント新規登録オプション発行APIで受け取ったオプションで{@code navigator.credentials.create()}が返した {@code
   * PublicKeyCredential}を{@code toJSON()}した値を、さらにJSON文字列にしたもの
   */
  @Schema(description = "最初のパスキーの登録レスポンス（PublicKeyCredential#toJSON()のJSON文字列）")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_RESPONSE_JSON_MAX_LENGTH, message = "{validation.common.max_length}")
  private String credential;

  /** パスキー名 */
  @Schema(description = "パスキー名", example = "MacBookのTouch ID")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String passkeyName;

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
