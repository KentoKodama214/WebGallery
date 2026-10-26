package com.web.gallery.presentation.converter.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioDisplayOrderMoveModel;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.presentation.request.photo.PortfolioDisplayOrderMoveRequest;
import com.web.gallery.presentation.request.photo.PortfolioListRequest;
import com.web.gallery.presentation.request.photo.PortfolioPhotoSaveRequest;
import com.web.gallery.presentation.request.photo.PortfolioPhotoSearchRequest;
import com.web.gallery.presentation.request.photo.PortfolioSaveRequest;
import com.web.gallery.presentation.request.photo.PortfolioTagSaveRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** ポートフォリオ関連のRequestをModelへ変換するConverterクラス */
@Component
public class PortfolioConverter {

  /**
   * ポートフォリオ登録リクエストとログイン中のアカウント番号からPortfolioDetailModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人のポートフォリオの登録を防ぐため）。
   * ポートフォリオ番号・公開状態・写真の表示順・タグ番号はサーバ側で決定するためここでは設定しない
   *
   * @param request {@link PortfolioSaveRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PortfolioDetailModel}
   */
  public PortfolioDetailModel toPortfolioDetailModel(
      PortfolioSaveRequest request, AccountNo accountNo) {
    return PortfolioDetailModel.builder()
        .accountNo(accountNo)
        .theme(new PortfolioTheme(request.getTheme()))
        .description(
            new PortfolioDescription(
                Optional.ofNullable(request.getDescription()).orElse(Consts.STRING_EMPTY)))
        .templateKbn(request.getTemplateKbn())
        .templateSettings(toTemplateSettings(request.getTemplateSettings()))
        .portfolioPhotoModelList(toPortfolioPhotoModelList(request.getPhotoList()))
        .portfolioTagModelList(toPortfolioTagModelList(request.getTagList()))
        .build();
  }

  /**
   * ポートフォリオ更新リクエスト・ログイン中のアカウント番号・パスパラメータのポートフォリオ番号からPortfolioDetailModelを生成する
   *
   * @param request {@link PortfolioSaveRequest}
   * @param accountNo ログイン中のアカウント番号
   * @param portfolioNo 更新対象のポートフォリオ番号
   * @return {@link PortfolioDetailModel}
   */
  public PortfolioDetailModel toPortfolioDetailModel(
      PortfolioSaveRequest request, AccountNo accountNo, PortfolioNo portfolioNo) {
    return toPortfolioDetailModel(request, accountNo).toBuilder().portfolioNo(portfolioNo).build();
  }

  /**
   * ポートフォリオ一覧の絞り込みリクエストとログイン中のアカウント番号からPortfolioListGetModelを生成する
   *
   * <p>前後の空白を除いて空になる条件は、未指定（null）として扱う
   *
   * @param request {@link PortfolioListRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PortfolioListGetModel}
   */
  public PortfolioListGetModel toPortfolioListGetModel(
      PortfolioListRequest request, AccountNo accountNo) {
    return PortfolioListGetModel.builder()
        .accountNo(accountNo)
        .keyword(toKeyword(request.getKeyword()))
        .pageNo(request.getPageNo())
        .build();
  }

  /**
   * ポートフォリオ表示順の移動リクエスト・ログイン中のアカウント番号・パスパラメータのポートフォリオ番号からPortfolioDisplayOrderMoveModelを生成する
   *
   * @param request {@link PortfolioDisplayOrderMoveRequest}（バリデーション済み）
   * @param accountNo ログイン中のアカウント番号
   * @param portfolioNo 移動するポートフォリオ番号
   * @return {@link PortfolioDisplayOrderMoveModel}
   */
  public PortfolioDisplayOrderMoveModel toPortfolioDisplayOrderMoveModel(
      PortfolioDisplayOrderMoveRequest request, AccountNo accountNo, PortfolioNo portfolioNo) {
    return PortfolioDisplayOrderMoveModel.builder()
        .accountNo(accountNo)
        .portfolioNo(portfolioNo)
        .direction(request.getDirection())
        .build();
  }

  /**
   * ポートフォリオ用の写真検索リクエストとログイン中のアカウント番号からPortfolioPhotoSearchModelを生成する
   *
   * <p>前後の空白を除いて空になる条件は、未指定（null）として扱う
   *
   * @param request {@link PortfolioPhotoSearchRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PortfolioPhotoSearchModel}
   */
  public PortfolioPhotoSearchModel toPortfolioPhotoSearchModel(
      PortfolioPhotoSearchRequest request, AccountNo accountNo) {
    return PortfolioPhotoSearchModel.builder()
        .accountNo(accountNo)
        .keyword(toKeyword(request.getKeyword()))
        .pageNo(request.getPageNo())
        .build();
  }

  /**
   * 絞り込みの入力値を、前後の空白を除いた検索語に変換する
   *
   * @param value 入力値（null許容）
   * @return 検索語。未入力・空白のみの場合はnull
   */
  private String toKeyword(String value) {
    if (Objects.isNull(value) || value.isBlank()) {
      return null;
    }
    return value.strip();
  }

  private PortfolioPhotoModelList toPortfolioPhotoModelList(
      List<PortfolioPhotoSaveRequest> requestList) {
    if (Objects.isNull(requestList)) {
      return PortfolioPhotoModelList.empty();
    }
    return PortfolioPhotoModelList.of(
        requestList.stream()
            .map(
                request ->
                    PortfolioPhotoModel.builder()
                        .photoNo(new PhotoNo(request.getPhotoNo()))
                        .photoSettings(toTemplateSettings(request.getPhotoSettings()))
                        .build())
            .toList());
  }

  /**
   * ポートフォリオタグ保存リクエストの一覧からPortfolioTagModelListを生成する
   *
   * <p>アカウント番号・ポートフォリオ番号・タグ番号は集約ルート（{@link com.web.gallery.application.aggregate.Portfolio}）が
   * 所有者・並び順に応じて設定するためここでは設定しない
   *
   * @param requestList {@link PortfolioTagSaveRequest}のリスト（null許容）
   * @return {@link PortfolioTagModelList}
   */
  private PortfolioTagModelList toPortfolioTagModelList(List<PortfolioTagSaveRequest> requestList) {
    if (Objects.isNull(requestList)) {
      return PortfolioTagModelList.empty();
    }
    return PortfolioTagModelList.of(
        requestList.stream()
            .map(
                request ->
                    PortfolioTagModel.builder()
                        .tagJapaneseName(
                            new TagJapaneseName(
                                Optional.ofNullable(request.getTagJapaneseName())
                                    .orElse(Consts.STRING_EMPTY)))
                        .tagEnglishName(
                            new TagEnglishName(
                                Optional.ofNullable(request.getTagEnglishName())
                                    .orElse(Consts.STRING_EMPTY)))
                        .build())
            .toList());
  }

  private TemplateSettings toTemplateSettings(Map<String, Object> settings) {
    return Objects.isNull(settings) ? TemplateSettings.empty() : new TemplateSettings(settings);
  }
}
