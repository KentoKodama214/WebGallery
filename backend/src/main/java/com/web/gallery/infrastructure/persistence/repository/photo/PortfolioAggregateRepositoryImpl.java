package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.aggregate.Portfolio;
import com.web.gallery.application.repository.photo.PortfolioAggregateRepository;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstUpdateTarget;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhoto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioMstMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioPhotoMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PortfolioTagMstMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * ポートフォリオ集約（{@link Portfolio}）を永続化するRepositoryの実装クラス
 *
 * <p>PortfolioMst・PortfolioPhoto・PortfolioTagMstの3テーブルへの永続化を、ポートフォリオの登録・更新・削除という
 * ユースケース単位で整合性のある1操作としてまとめる。他のRepositoryには依存せず、Mapperを直接操作する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PortfolioAggregateRepositoryImpl implements PortfolioAggregateRepository {

  private final PortfolioMstMapper portfolioMstMapper;
  private final PortfolioPhotoMapper portfolioPhotoMapper;
  private final PortfolioTagMstMapper portfolioTagMstMapper;

  /**
   * ポートフォリオ集約（ポートフォリオ・写真・タグ）を新規登録する
   *
   * <p>写真・タグは外部キーでポートフォリオ本体を参照するため、ポートフォリオ本体を先に登録する
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  public void regist(Portfolio portfolio) throws GalleryException {
    try {
      if (portfolioMstMapper.insert(PortfolioMst.fromForRegist(portfolio.getDetail())) < 1) {
        log.warn(
            "PortfolioMst: Regist Failed (AccountNo: {}, PortfolioNo: {})",
            portfolio.getAccountNo().value(),
            portfolio.getPortfolioNo().value());
        throw ErrorEnum.FAIL_TO_REGIST_PORTFOLIO.toException();
      }
      registChildren(portfolio);
    } catch (DuplicateKeyException e) {
      // 同一アカウントの並行登録でポートフォリオ番号が衝突した場合
      log.warn(
          "PortfolioMst: Duplicate Key (AccountNo: {}, PortfolioNo: {})",
          portfolio.getAccountNo().value(),
          portfolio.getPortfolioNo().value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_PORTFOLIO.toException();
    }
  }

  /**
   * ポートフォリオ集約を更新する
   *
   * <p>ポートフォリオ本体を更新し、写真・タグは全件を削除してから登録し直す（表示順・タグ番号を振り直すため）
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void update(Portfolio portfolio) throws GalleryException {
    Long accountNo = portfolio.getAccountNo().value();
    Long portfolioNo = portfolio.getPortfolioNo().value();

    if (portfolioMstMapper.update(
            PortfolioMstCondition.byAccountAndPortfolioNotDeleted(accountNo, portfolioNo),
            PortfolioMstUpdateTarget.forUpdate(portfolio.getDetail()))
        < 1) {
      log.warn(
          "PortfolioMst: Update Failed (AccountNo: {}, PortfolioNo: {})", accountNo, portfolioNo);
      throw ErrorEnum.FAIL_TO_UPDATE_PORTFOLIO.toException();
    }

    portfolioPhotoMapper.delete(
        PortfolioPhotoCondition.byAccountAndPortfolio(accountNo, portfolioNo));
    portfolioTagMstMapper.delete(
        PortfolioTagMstCondition.byAccountAndPortfolio(accountNo, portfolioNo));
    try {
      registChildren(portfolio);
    } catch (DuplicateKeyException e) {
      log.warn(
          "PortfolioPhoto/PortfolioTagMst: Duplicate Key (AccountNo: {}, PortfolioNo: {})",
          accountNo,
          portfolioNo,
          e);
      throw ErrorEnum.FAIL_TO_UPDATE_PORTFOLIO.toException();
    }
  }

  /**
   * ポートフォリオの公開状態を更新する
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void updateStatus(Portfolio portfolio) throws GalleryException {
    Long accountNo = portfolio.getAccountNo().value();
    Long portfolioNo = portfolio.getPortfolioNo().value();

    if (portfolioMstMapper.update(
            PortfolioMstCondition.byAccountAndPortfolioNotDeleted(accountNo, portfolioNo),
            PortfolioMstUpdateTarget.forStatus(portfolio.getDetail()))
        < 1) {
      log.warn(
          "PortfolioMst: Update Failed for status (AccountNo: {}, PortfolioNo: {})",
          accountNo,
          portfolioNo);
      throw ErrorEnum.FAIL_TO_UPDATE_PORTFOLIO.toException();
    }
  }

  /**
   * ポートフォリオ集約を削除する
   *
   * <p>写真・タグを物理削除したうえでポートフォリオ本体を論理削除する
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException ポートフォリオが存在しない（未登録・削除済み）場合
   */
  @Override
  public void delete(Portfolio portfolio) throws GalleryException {
    Long accountNo = portfolio.getAccountNo().value();
    Long portfolioNo = portfolio.getPortfolioNo().value();

    if (portfolioMstMapper.update(
            PortfolioMstCondition.byAccountAndPortfolioNotDeleted(accountNo, portfolioNo),
            PortfolioMstUpdateTarget.forDelete(accountNo))
        < 1) {
      // 対象ポートフォリオが存在しない（未登録・既に削除済み）＝404。競合ではないため409は返さない
      log.warn(
          "PortfolioMst: Not Found for delete (AccountNo: {}, PortfolioNo: {})",
          accountNo,
          portfolioNo);
      throw ErrorEnum.PORTFOLIO_NOT_FOUND.toException();
    }

    portfolioPhotoMapper.delete(
        PortfolioPhotoCondition.byAccountAndPortfolio(accountNo, portfolioNo));
    portfolioTagMstMapper.delete(
        PortfolioTagMstCondition.byAccountAndPortfolio(accountNo, portfolioNo));
  }

  private void registChildren(Portfolio portfolio) {
    List<PortfolioPhoto> photoList =
        portfolio.getDetail().getPortfolioPhotoModelList().stream()
            .map(PortfolioPhoto::from)
            .toList();
    if (!photoList.isEmpty()) {
      portfolioPhotoMapper.insertBulk(photoList);
    }

    List<PortfolioTagMst> tagList =
        portfolio.getDetail().getPortfolioTagModelList().stream()
            .map(PortfolioTagMst::from)
            .toList();
    if (!tagList.isEmpty()) {
      portfolioTagMstMapper.insertBulk(tagList);
    }
  }
}
