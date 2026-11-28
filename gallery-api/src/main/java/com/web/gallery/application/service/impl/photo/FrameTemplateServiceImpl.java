package com.web.gallery.application.service.impl.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.application.repository.photo.FrameTemplateMstRepository;
import com.web.gallery.application.service.photo.FrameTemplateService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.domain.service.FrameDefinitionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 額縁テンプレートに関するビジネスロジックを扱うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrameTemplateServiceImpl implements FrameTemplateService {

  private final FrameTemplateMstRepository frameTemplateMstRepository;
  private final FrameDefinitionPolicy frameDefinitionPolicy;

  /**
   * 本人の額縁テンプレート一覧を、額縁テンプレート番号の昇順に取得する
   *
   * @param accountNo アカウント番号
   * @return {@link FrameTemplateModelList}
   */
  @Override
  @Transactional(readOnly = true)
  public FrameTemplateModelList getFrameTemplateList(AccountNo accountNo) {
    return frameTemplateMstRepository.getFrameTemplateList(accountNo);
  }

  /**
   * 本人の額縁テンプレートを取得する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateModel}
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  @Override
  @Transactional(readOnly = true)
  public FrameTemplateModel getFrameTemplate(AccountNo accountNo, FrameTemplateNo frameTemplateNo)
      throws GalleryException {
    return frameTemplateMstRepository.getFrameTemplate(accountNo, frameTemplateNo);
  }

  /**
   * 額縁テンプレートを新規登録する
   *
   * <p>作成済みの額縁テンプレートの件数が上限（{@value Consts#FRAME_TEMPLATE_MAX_SIZE}件）に達している場合は登録しない
   *
   * @param requestModel 登録リクエストの額縁テンプレート
   * @return 新規採番した額縁テンプレート番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・作成数が上限に達している場合 ・額縁定義が不正な場合 ・登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public FrameTemplateNo registFrameTemplate(FrameTemplateModel requestModel)
      throws GalleryException {
    AccountNo accountNo = requestModel.getAccountNo();
    if (frameTemplateMstRepository.count(accountNo) >= Consts.FRAME_TEMPLATE_MAX_SIZE) {
      log.info("FrameTemplate: Reached Upper Limit (AccountNo: {})", accountNo.value());
      throw ErrorEnum.REACHED_FRAME_TEMPLATE_LIMIT.toException();
    }
    validateDefinition(requestModel);

    FrameTemplateNo newFrameTemplateNo =
        frameTemplateMstRepository.getNewFrameTemplateNo(accountNo);
    frameTemplateMstRepository.regist(
        requestModel.toBuilder().frameTemplateNo(newFrameTemplateNo).build());

    return newFrameTemplateNo;
  }

  /**
   * 額縁テンプレートを更新する
   *
   * <p>額縁テンプレートを使用しているポートフォリオは額縁定義をコピーして保持しているため、更新は反映されない
   *
   * @param requestModel 更新リクエストの額縁テンプレート（額縁テンプレート番号を含む）
   * @throws GalleryException 以下のいずれかに該当する場合 ・額縁テンプレートが存在しない場合 ・額縁定義が不正な場合 ・更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void updateFrameTemplate(FrameTemplateModel requestModel) throws GalleryException {
    // 存在しない額縁テンプレートは、更新失敗（409）ではなく存在しない（404）として扱う
    frameTemplateMstRepository.getFrameTemplate(
        requestModel.getAccountNo(), requestModel.getFrameTemplateNo());
    validateDefinition(requestModel);

    frameTemplateMstRepository.update(requestModel);
  }

  /**
   * 額縁テンプレートを削除する
   *
   * <p>額縁テンプレートを使用しているポートフォリオは額縁定義をコピーして保持しているため、削除しても影響しない
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deleteFrameTemplate(AccountNo accountNo, FrameTemplateNo frameTemplateNo)
      throws GalleryException {
    frameTemplateMstRepository.delete(accountNo, frameTemplateNo);
  }

  /**
   * 額縁定義が妥当であることを検証する
   *
   * @param model 登録・更新リクエストの額縁テンプレート
   * @throws GalleryException 額縁定義が不正な場合
   */
  private void validateDefinition(FrameTemplateModel model) throws GalleryException {
    if (!frameDefinitionPolicy.isValid(model.getFrameDefinition())) {
      log.info("FrameTemplate: Invalid definition (AccountNo: {})", model.getAccountNo().value());
      throw ErrorEnum.INVALID_FRAME_DEFINITION.toException();
    }
  }
}
