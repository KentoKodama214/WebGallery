package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.application.repository.photo.FrameTemplateMstRepository;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMst;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstUpdateTarget;
import com.web.gallery.infrastructure.persistence.mapper.photo.FrameTemplateMstMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** 額縁テンプレートマスタデータを操作するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FrameTemplateMstRepositoryImpl implements FrameTemplateMstRepository {

  private final FrameTemplateMstMapper frameTemplateMstMapper;

  /**
   * アカウント番号から新しい額縁テンプレート番号を発番する
   *
   * <p>登録済みの額縁テンプレート番号の最大値の次の番号とする
   *
   * @param accountNo アカウント番号
   * @return 新規採番した額縁テンプレート番号
   */
  @Override
  public FrameTemplateNo getNewFrameTemplateNo(AccountNo accountNo) {
    return FrameTemplateNo.next(frameTemplateMstMapper.getMaxFrameTemplateNo(accountNo.value()));
  }

  /**
   * アカウントが作成済みの額縁テンプレートの件数を取得する
   *
   * @param accountNo アカウント番号
   * @return 作成済みの件数
   */
  @Override
  public Integer count(AccountNo accountNo) {
    return frameTemplateMstMapper.count(FrameTemplateMstCondition.byAccountNo(accountNo.value()));
  }

  /**
   * 本人の額縁テンプレート一覧を、額縁テンプレート番号の昇順に取得する
   *
   * @param accountNo アカウント番号
   * @return {@link FrameTemplateModelList}
   */
  @Override
  public FrameTemplateModelList getFrameTemplateList(AccountNo accountNo) {
    List<FrameTemplateMst> entityList =
        frameTemplateMstMapper.select(FrameTemplateMstCondition.byAccountNo(accountNo.value()));
    return FrameTemplateModelList.of(entityList.stream().map(this::toFrameTemplateModel).toList());
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
  public FrameTemplateModel getFrameTemplate(AccountNo accountNo, FrameTemplateNo frameTemplateNo)
      throws GalleryException {
    List<FrameTemplateMst> entityList =
        frameTemplateMstMapper.select(
            FrameTemplateMstCondition.byAccountAndFrameTemplate(
                accountNo.value(), frameTemplateNo.value()));
    if (entityList.isEmpty()) {
      log.info(
          "FrameTemplateMst: Not Found (AccountNo: {}, FrameTemplateNo: {})",
          accountNo.value(),
          frameTemplateNo.value());
      throw ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException();
    }
    return toFrameTemplateModel(entityList.get(0));
  }

  /**
   * 額縁テンプレートを登録する
   *
   * @param frameTemplateModel 額縁テンプレート番号を採番済みの{@link FrameTemplateModel}
   * @throws GalleryException 登録に失敗した場合（同時に登録して額縁テンプレート番号が重複した場合を含む）
   */
  @Override
  public void regist(FrameTemplateModel frameTemplateModel) throws GalleryException {
    try {
      if (frameTemplateMstMapper.insert(FrameTemplateMst.fromForRegist(frameTemplateModel)) < 1) {
        log.warn(
            "FrameTemplateMst: Regist Failed (AccountNo: {})",
            frameTemplateModel.getAccountNo().value());
        throw ErrorEnum.FAIL_TO_REGIST_FRAME_TEMPLATE.toException();
      }
    } catch (DuplicateKeyException e) {
      log.warn(
          "FrameTemplateMst: Duplicate Key (AccountNo: {})",
          frameTemplateModel.getAccountNo().value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_FRAME_TEMPLATE.toException();
    }
  }

  /**
   * 額縁テンプレートのテンプレート名・額縁定義を更新する
   *
   * @param frameTemplateModel 更新後の{@link FrameTemplateModel}（額縁テンプレート番号を含む）
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void update(FrameTemplateModel frameTemplateModel) throws GalleryException {
    FrameTemplateMstCondition condition =
        FrameTemplateMstCondition.byAccountAndFrameTemplate(
            frameTemplateModel.getAccountNo().value(),
            frameTemplateModel.getFrameTemplateNo().value());
    if (frameTemplateMstMapper.update(
            condition, FrameTemplateMstUpdateTarget.forUpdate(frameTemplateModel))
        < 1) {
      log.warn(
          "FrameTemplateMst: Update Failed (AccountNo: {}, FrameTemplateNo: {})",
          frameTemplateModel.getAccountNo().value(),
          frameTemplateModel.getFrameTemplateNo().value());
      throw ErrorEnum.FAIL_TO_UPDATE_FRAME_TEMPLATE.toException();
    }
  }

  /**
   * 額縁テンプレートを物理削除する
   *
   * <p>額縁テンプレートを使用しているポートフォリオは額縁定義をコピーして保持しているため、削除しても影響しない
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  @Override
  public void delete(AccountNo accountNo, FrameTemplateNo frameTemplateNo) throws GalleryException {
    Integer deletedCount =
        frameTemplateMstMapper.delete(
            FrameTemplateMstCondition.byAccountAndFrameTemplate(
                accountNo.value(), frameTemplateNo.value()));
    if (deletedCount < 1) {
      log.info(
          "FrameTemplateMst: Not Found for delete (AccountNo: {}, FrameTemplateNo: {})",
          accountNo.value(),
          frameTemplateNo.value());
      throw ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException();
    }
  }

  private FrameTemplateModel toFrameTemplateModel(FrameTemplateMst entity) {
    return FrameTemplateModel.builder()
        .accountNo(new AccountNo(entity.getAccountNo()))
        .frameTemplateNo(new FrameTemplateNo(entity.getFrameTemplateNo()))
        .templateName(new FrameTemplateName(entity.getTemplateName()))
        .frameDefinition(toFrameDefinition(entity.getFrameDefinition()))
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private FrameDefinition toFrameDefinition(Map<String, Object> definition) {
    return new FrameDefinition(definition != null ? definition : Map.of());
  }
}
