package com.web.gallery.domain.service;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ポートフォリオのテンプレート固有の設定に関するビジネスルールを、テンプレートに応じて判定するドメインサービス
 *
 * <p>判定そのものはテンプレートごとのPolicy（{@link FeatureTemplateSettingsPolicy}・{@link
 * GridTemplateSettingsPolicy}）に委譲する。テンプレートを追加した場合は、{@link PortfolioTemplateEnum}の値を
 * 網羅するswitch式のコンパイルエラーに従って、ここに委譲先を追加する
 *
 * <p>検証は2段階に分かれる。下書き保存時は設定の構造（未知の項目・型・文字数）のみを判定し、設定途中の不整合
 * （グリッド型の行の枚数の合計と写真の枚数の不一致等）は許容する。公開時・公開中の更新時は、加えて表示に必要な設定が 揃っているかを判定する
 */
@Component
@RequiredArgsConstructor
public class PortfolioTemplateSettingsPolicy {

  private final FeatureTemplateSettingsPolicy featureTemplateSettingsPolicy;
  private final GridTemplateSettingsPolicy gridTemplateSettingsPolicy;

  /**
   * ポートフォリオ単位の設定の構造が妥当かどうかを判定する
   *
   * @param templateKbn テンプレート区分
   * @param templateSettings ポートフォリオ単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidTemplateSettings(
      PortfolioTemplateEnum templateKbn, TemplateSettings templateSettings) {
    return switch (templateKbn) {
      case FEATURE -> featureTemplateSettingsPolicy.isValidTemplateSettings(templateSettings);
      case GRID -> gridTemplateSettingsPolicy.isValidTemplateSettings(templateSettings);
    };
  }

  /**
   * 写真単位の設定の構造が妥当かどうかを判定する
   *
   * @param templateKbn テンプレート区分
   * @param photoSettings 写真単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidPhotoSettings(
      PortfolioTemplateEnum templateKbn, TemplateSettings photoSettings) {
    return switch (templateKbn) {
      case FEATURE -> featureTemplateSettingsPolicy.isValidPhotoSettings(photoSettings);
      case GRID -> gridTemplateSettingsPolicy.isValidPhotoSettings(photoSettings);
    };
  }

  /**
   * ポートフォリオ単位の設定と写真枚数から、公開できる状態かどうかを判定する
   *
   * @param templateKbn テンプレート区分
   * @param templateSettings ポートフォリオ単位の設定
   * @param photoCount ポートフォリオに設定した写真の枚数
   * @return 公開できる場合、true
   */
  public Boolean isPublishable(
      PortfolioTemplateEnum templateKbn, TemplateSettings templateSettings, PhotoCount photoCount) {
    return switch (templateKbn) {
      case FEATURE -> featureTemplateSettingsPolicy.isPublishable(templateSettings, photoCount);
      case GRID -> gridTemplateSettingsPolicy.isPublishable(templateSettings, photoCount);
    };
  }

  /**
   * 写真単位の設定が、公開できる状態かどうかを判定する
   *
   * @param templateKbn テンプレート区分
   * @param photoSettings 写真単位の設定
   * @return 公開できる場合、true
   */
  public Boolean isPublishablePhotoSettings(
      PortfolioTemplateEnum templateKbn, TemplateSettings photoSettings) {
    return switch (templateKbn) {
      case FEATURE -> featureTemplateSettingsPolicy.isPublishablePhotoSettings(photoSettings);
      case GRID -> gridTemplateSettingsPolicy.isPublishablePhotoSettings(photoSettings);
    };
  }
}
