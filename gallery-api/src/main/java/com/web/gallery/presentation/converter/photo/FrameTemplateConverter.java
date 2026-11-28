package com.web.gallery.presentation.converter.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.presentation.request.photo.FrameTemplateSaveRequest;
import org.springframework.stereotype.Component;

/** 額縁テンプレート関連のRequestをModelへ変換するConverterクラス */
@Component
public class FrameTemplateConverter {

  /**
   * 額縁テンプレート登録リクエストとログイン中のアカウント番号からFrameTemplateModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の額縁テンプレートの登録を防ぐため）。
   * テンプレート名は前後の空白を除く。額縁テンプレート番号はサーバ側で採番するためここでは設定しない
   *
   * @param request {@link FrameTemplateSaveRequest}（バリデーション済み）
   * @param accountNo ログイン中のアカウント番号
   * @return {@link FrameTemplateModel}
   */
  public FrameTemplateModel toFrameTemplateModel(
      FrameTemplateSaveRequest request, AccountNo accountNo) {
    return FrameTemplateModel.builder()
        .accountNo(accountNo)
        .templateName(new FrameTemplateName(request.getTemplateName().strip()))
        .frameDefinition(new FrameDefinition(request.getFrameDefinition()))
        .build();
  }

  /**
   * 額縁テンプレート更新リクエスト・ログイン中のアカウント番号・パスパラメータの額縁テンプレート番号からFrameTemplateModelを生成する
   *
   * @param request {@link FrameTemplateSaveRequest}（バリデーション済み）
   * @param accountNo ログイン中のアカウント番号
   * @param frameTemplateNo 更新対象の額縁テンプレート番号
   * @return {@link FrameTemplateModel}
   */
  public FrameTemplateModel toFrameTemplateModel(
      FrameTemplateSaveRequest request, AccountNo accountNo, FrameTemplateNo frameTemplateNo) {
    return toFrameTemplateModel(request, accountNo).toBuilder()
        .frameTemplateNo(frameTemplateNo)
        .build();
  }
}
