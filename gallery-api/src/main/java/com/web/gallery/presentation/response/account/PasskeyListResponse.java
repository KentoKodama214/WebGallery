package com.web.gallery.presentation.response.account;

import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * パスキー一覧のレスポンスパラメータを保持するクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "パスキー一覧レスポンス")
@Data
@Builder
public class PasskeyListResponse {
  /** パスキー一覧（登録日時の昇順） */
  @Schema(description = "パスキー一覧（登録日時の昇順）")
  private List<PasskeyResponse> passkeys;

  /** 登録できるパスキー数の上限 */
  @Schema(description = "登録できるパスキー数の上限", example = "10")
  private Integer maxCount;

  /**
   * PasskeyCredentialModelListからPasskeyListResponseを生成する
   *
   * @param modelList {@link PasskeyCredentialModelList}
   * @param maxCount 登録できるパスキー数の上限
   * @return {@link PasskeyListResponse}
   */
  public static PasskeyListResponse from(PasskeyCredentialModelList modelList, Integer maxCount) {
    return PasskeyListResponse.builder()
        .passkeys(modelList.stream().map(PasskeyResponse::from).toList())
        .maxCount(maxCount)
        .build();
  }
}
