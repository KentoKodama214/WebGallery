package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * ポートフォリオ登録・更新時のリクエストパラメータを保持するクラス
 *
 * <p>アカウント番号・ポートフォリオ番号・公開状態はサーバ側で決定するため受け付けない（公開状態の変更は公開状態APIで行う）
 */
@Schema(description = "ポートフォリオ登録・更新リクエスト")
@Data
public class PortfolioSaveRequest {
  /** テーマ */
  @Schema(description = "テーマ", example = "日本の自然")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PORTFOLIO_THEME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String theme;

  /** 説明文 */
  @Schema(description = "説明文", example = "日本各地の四季折々の風景を集めました。")
  @Size(max = Consts.PORTFOLIO_DESCRIPTION_MAX_LENGTH, message = "{validation.common.max_length}")
  private String description;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}。未知の値はnullとして扱われ、必須チェックで不正となる
   */
  @Schema(description = "テンプレート区分", example = "feature")
  @NotNull(message = "{validation.common.notBlank}")
  private PortfolioTemplateEnum templateKbn;

  /** ポートフォリオ単位のテンプレート設定（項目はテンプレートごとに異なる） */
  @Schema(
      description =
          "ポートフォリオ単位のテンプレート設定（項目はテンプレートごとに異なる。grid: {\"rows\":[{\"pattern\":\"TWO_EQUAL\"}]}）",
      example = "{}")
  private Map<String, Object> templateSettings;

  /**
   * 写真一覧（この並び順が表示順になる）
   *
   * <p>下書きでも1枚以上を必須とする
   */
  @Schema(description = "写真一覧（この並び順が表示順になる）")
  @NotEmpty(message = "{validation.portfolio.photoList.notEmpty}")
  @Size(max = Consts.PORTFOLIO_PHOTO_MAX_SIZE, message = "{validation.portfolio.photoList.maxSize}")
  @Valid
  private List<PortfolioPhotoSaveRequest> photoList;

  /** タグ一覧 */
  @Schema(description = "タグ一覧")
  @Size(max = Consts.PORTFOLIO_TAG_MAX_SIZE, message = "{validation.portfolio.tagList.maxSize}")
  @Valid
  private List<PortfolioTagSaveRequest> tagList;
}
