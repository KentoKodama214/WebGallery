package com.web.gallery.application.repository.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameTemplateNo;

/** 額縁テンプレートマスタデータを操作するRepositoryクラス */
public interface FrameTemplateMstRepository {
  /**
   * アカウント番号から新しい額縁テンプレート番号を発番する
   *
   * @param accountNo アカウント番号
   * @return 新規採番した額縁テンプレート番号
   */
  FrameTemplateNo getNewFrameTemplateNo(AccountNo accountNo);

  /**
   * アカウントが作成済みの額縁テンプレートの件数を取得する
   *
   * @param accountNo アカウント番号
   * @return 作成済みの件数
   */
  Integer count(AccountNo accountNo);

  /**
   * 本人の額縁テンプレート一覧を、額縁テンプレート番号の昇順に取得する
   *
   * @param accountNo アカウント番号
   * @return {@link FrameTemplateModelList}
   */
  FrameTemplateModelList getFrameTemplateList(AccountNo accountNo);

  /**
   * 本人の額縁テンプレートを取得する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateModel}
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  FrameTemplateModel getFrameTemplate(AccountNo accountNo, FrameTemplateNo frameTemplateNo)
      throws GalleryException;

  /**
   * 額縁テンプレートを登録する
   *
   * @param frameTemplateModel 額縁テンプレート番号を採番済みの{@link FrameTemplateModel}
   * @throws GalleryException 登録に失敗した場合
   */
  void regist(FrameTemplateModel frameTemplateModel) throws GalleryException;

  /**
   * 額縁テンプレートのテンプレート名・額縁定義を更新する
   *
   * @param frameTemplateModel 更新後の{@link FrameTemplateModel}（額縁テンプレート番号を含む）
   * @throws GalleryException 更新に失敗した場合
   */
  void update(FrameTemplateModel frameTemplateModel) throws GalleryException;

  /**
   * 額縁テンプレートを物理削除する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  void delete(AccountNo accountNo, FrameTemplateNo frameTemplateNo) throws GalleryException;
}
