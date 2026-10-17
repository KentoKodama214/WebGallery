package com.web.gallery.application.service.impl.photo;

import com.web.gallery.application.aggregate.Portfolio;
import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.application.repository.photo.PortfolioAggregateRepository;
import com.web.gallery.application.repository.photo.PortfolioMstRepository;
import com.web.gallery.application.service.photo.PortfolioService;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.service.PortfolioTemplateSettingsPolicy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ポートフォリオの設定に関するビジネスロジックを扱うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

  private final PortfolioMstRepository portfolioMstRepository;
  private final PortfolioAggregateRepository portfolioAggregateRepository;
  private final FileRepository fileRepository;
  private final PortfolioTemplateSettingsPolicy portfolioTemplateSettingsPolicy;

  /**
   * 本人のポートフォリオ一覧（下書きを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioModelList}
   */
  @Override
  @Transactional(readOnly = true)
  public PortfolioModelList getPortfolioList(AccountNo accountNo) {
    return portfolioMstRepository.getPortfolioList(accountNo);
  }

  /**
   * 本人のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * <p>写真の画像ファイルパスは署名付きURLに差し替えて返す
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioDetailModel}
   * @throws GalleryException ポートフォリオが存在しない場合
   */
  @Override
  @Transactional(readOnly = true)
  public PortfolioDetailModel getPortfolioDetail(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException {
    PortfolioDetailModel detail = portfolioMstRepository.getPortfolioDetail(accountNo, portfolioNo);

    List<PortfolioPhotoModel> signedPhotos =
        detail.getPortfolioPhotoModelList().stream()
            .map(
                photo ->
                    photo.toBuilder()
                        .imageFilePath(fileRepository.getPresignedUrl(photo.getImageFilePath()))
                        .build())
            .toList();
    return detail.toBuilder()
        .portfolioPhotoModelList(PortfolioPhotoModelList.of(signedPhotos))
        .build();
  }

  /**
   * ポートフォリオを下書きとして新規登録する
   *
   * @param requestDetail 登録リクエストのポートフォリオ詳細情報
   * @return 新規採番したポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・設定内容が不正な場合 ・設定できない写真が含まれる場合 ・登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public PortfolioNo registPortfolio(PortfolioDetailModel requestDetail) throws GalleryException {
    validateContent(requestDetail);

    PortfolioNo newPortfolioNo =
        portfolioMstRepository.getNewPortfolioNo(requestDetail.getAccountNo());
    Portfolio portfolio = Portfolio.forRegist(requestDetail, newPortfolioNo);
    portfolioAggregateRepository.regist(portfolio);

    return portfolio.getPortfolioNo();
  }

  /**
   * ポートフォリオを更新する
   *
   * <p>公開状態は変更しない。公開中のポートフォリオは、更新後の内容が公開できる状態であることを要求する （公開ページの表示が崩れた状態で公開され続けるのを防ぐため）
   *
   * @param requestDetail 更新リクエストのポートフォリオ詳細情報（ポートフォリオ番号を含む）
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・設定内容が不正な場合 ・設定できない写真が含まれる場合
   *     ・公開中で、更新後の内容が公開できない場合 ・更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void updatePortfolio(PortfolioDetailModel requestDetail) throws GalleryException {
    PortfolioDetailModel existing =
        portfolioMstRepository.getPortfolioDetail(
            requestDetail.getAccountNo(), requestDetail.getPortfolioNo());
    validateContent(requestDetail);

    Portfolio portfolio = Portfolio.forUpdate(existing, requestDetail);
    if (portfolio.isPublished()) {
      validatePublishable(portfolio);
    }
    portfolioAggregateRepository.update(portfolio);
  }

  /**
   * ポートフォリオを公開する
   *
   * <p>既に公開中の場合も、公開できる状態かを確認したうえで公開状態を更新する（何度呼び出しても結果が変わらない冪等な操作）
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・公開に必要な設定が不足している場合 ・更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void publishPortfolio(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException {
    Portfolio portfolio =
        Portfolio.reconstruct(portfolioMstRepository.getPortfolioDetail(accountNo, portfolioNo));
    validatePublishable(portfolio);

    portfolio.publish();
    portfolioAggregateRepository.updateStatus(portfolio);
  }

  /**
   * ポートフォリオを下書きに戻す
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void unpublishPortfolio(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException {
    Portfolio portfolio =
        Portfolio.reconstruct(portfolioMstRepository.getPortfolioDetail(accountNo, portfolioNo));

    portfolio.unpublish();
    portfolioAggregateRepository.updateStatus(portfolio);
  }

  /**
   * ポートフォリオを削除する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException ポートフォリオが存在しない場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deletePortfolio(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException {
    portfolioAggregateRepository.delete(Portfolio.forDelete(accountNo, portfolioNo));
  }

  /**
   * 登録・更新リクエストの内容を検証する（下書き保存時の検証）
   *
   * @param detail 登録・更新リクエストのポートフォリオ詳細情報
   * @throws GalleryException 以下のいずれかに該当する場合 ・同じ写真が重複して指定されている場合 ・テンプレート設定の構造が不正な場合
   *     ・本人が所有する未削除の写真以外が含まれる場合
   */
  private void validateContent(PortfolioDetailModel detail) throws GalleryException {
    PortfolioTemplateEnum templateKbn = detail.getTemplateKbn();
    List<PhotoNo> photoNos =
        detail.getPortfolioPhotoModelList().stream().map(PortfolioPhotoModel::getPhotoNo).toList();

    if (photoNos.stream().distinct().count() != photoNos.size()) {
      log.info(
          "Portfolio: Duplicate photo (AccountNo: {}, PhotoNos: {})",
          detail.getAccountNo().value(),
          photoNos);
      throw ErrorEnum.INVALID_INPUT.toException();
    }

    boolean isValidSettings =
        portfolioTemplateSettingsPolicy.isValidTemplateSettings(
                templateKbn, detail.getTemplateSettings())
            && detail.getPortfolioPhotoModelList().stream()
                .allMatch(
                    photo ->
                        portfolioTemplateSettingsPolicy.isValidPhotoSettings(
                            templateKbn, photo.getPhotoSettings()));
    if (!isValidSettings) {
      log.info(
          "Portfolio: Invalid settings (AccountNo: {}, TemplateKbn: {})",
          detail.getAccountNo().value(),
          templateKbn);
      throw ErrorEnum.INVALID_PORTFOLIO_SETTINGS.toException();
    }

    Integer availableCount =
        portfolioMstRepository.countAvailablePhotos(
            detail.getAccountNo(), PhotoNoList.of(photoNos));
    if (availableCount != photoNos.size()) {
      // 他人の写真・削除済みの写真・存在しない写真番号を指定された場合
      log.info(
          "Portfolio: Unavailable photo (AccountNo: {}, PhotoNos: {})",
          detail.getAccountNo().value(),
          photoNos);
      throw ErrorEnum.PORTFOLIO_PHOTO_NOT_AVAILABLE.toException();
    }
  }

  /**
   * ポートフォリオが公開できる状態かを検証する（公開時・公開中の更新時の検証）
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 公開に必要な設定が不足している場合
   */
  private void validatePublishable(Portfolio portfolio) throws GalleryException {
    PortfolioDetailModel detail = portfolio.getDetail();
    PortfolioTemplateEnum templateKbn = detail.getTemplateKbn();

    boolean isPublishable =
        portfolioTemplateSettingsPolicy.isPublishable(
                templateKbn, detail.getTemplateSettings(), portfolio.getPhotoCount())
            && detail.getPortfolioPhotoModelList().stream()
                .allMatch(
                    photo ->
                        portfolioTemplateSettingsPolicy.isPublishablePhotoSettings(
                            templateKbn, photo.getPhotoSettings()));
    if (!isPublishable) {
      log.info(
          "Portfolio: Not publishable (AccountNo: {}, PortfolioNo: {})",
          portfolio.getAccountNo().value(),
          portfolio.getPortfolioNo().value());
      throw ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.toException();
    }
  }
}
