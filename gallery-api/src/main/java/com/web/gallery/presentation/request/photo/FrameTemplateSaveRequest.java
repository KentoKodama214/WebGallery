package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

/**
 * 額縁テンプレート登録・更新時のリクエストパラメータを保持するクラス
 *
 * <p>アカウント番号・額縁テンプレート番号はサーバ側で決定するため受け付けない
 */
@Schema(description = "額縁テンプレート登録・更新リクエスト")
@Data
public class FrameTemplateSaveRequest {
  /** テンプレート名 */
  @Schema(description = "テンプレート名", example = "四つ切 3枚組")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.FRAME_TEMPLATE_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String templateName;

  /** 額縁定義（構造は{@link com.web.gallery.domain.service.FrameDefinitionPolicy}を参照） */
  @Schema(
      description =
          "額縁定義（size: 額縁のサイズ、background: 背景、border: 外枠、slots: 写真枠の一覧、texts: メッセージ枠の一覧。"
              + "長さはすべてmm。写真枠・メッセージ枠の位置は台紙（外枠の内側）の左上からの距離）",
      example =
          "{\"size\":{\"width\":364,\"height\":257},\"background\":{\"color\":\"#ffffff\"},"
              + "\"border\":{\"color\":\"#3b2a1a\",\"width\":9},"
              + "\"slots\":[{\"x\":30,\"y\":25,\"width\":120,\"height\":130,\"rotation\":-3}],"
              + "\"texts\":[{\"x\":170,\"y\":190,\"width\":150,\"fontFamily\":\"mincho\",\"fontSize\":9}]}")
  @NotNull(message = "{validation.common.notBlank}")
  private Map<String, Object> frameDefinition;
}
