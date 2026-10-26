package com.web.gallery.presentation.request.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import lombok.Data;

/**
 * ポートフォリオ登録・更新時の写真のリクエストパラメータを保持するクラス
 *
 * <p>写真番号は本人が所有する未削除の写真であることをサーバ側で検証する（他人の写真の混入を防ぐため）
 */
@Schema(description = "ポートフォリオ写真保存リクエスト")
@Data
public class PortfolioPhotoSaveRequest {
  /** 写真番号 */
  @Schema(description = "写真番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  private Long photoNo;

  /** 写真単位のテンプレート設定（項目はテンプレートごとに異なる） */
  @Schema(
      description =
          "写真単位のテンプレート設定（項目はテンプレートごとに異なる。feature: theme/subTheme/description、grid: caption）",
      example = "{\"theme\":\"日本の自然\",\"subTheme\":\"四季の風景\",\"description\":\"\"}")
  private Map<String, Object> photoSettings;
}
