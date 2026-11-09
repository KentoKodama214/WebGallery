package com.web.gallery.application.service.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameTemplateNo;

/** 額縁テンプレートに関するビジネスロジックを扱うServiceクラス */
public interface FrameTemplateService {
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
   * 額縁テンプレートを新規登録する
   *
   * @param requestModel 登録リクエストの額縁テンプレート
   * @return 新規採番した額縁テンプレート番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・作成数が上限に達している場合 ・額縁定義が不正な場合 ・登録に失敗した場合
   */
  FrameTemplateNo registFrameTemplate(FrameTemplateModel requestModel) throws GalleryException;

  /**
   * 額縁テンプレートを更新する
   *
   * <p>額縁テンプレートを使用しているポートフォリオは額縁定義をコピーして保持しているため、更新は反映されない
   *
   * @param requestModel 更新リクエストの額縁テンプレート（額縁テンプレート番号を含む）
   * @throws GalleryException 以下のいずれかに該当する場合 ・額縁テンプレートが存在しない場合 ・額縁定義が不正な場合 ・更新に失敗した場合
   */
  void updateFrameTemplate(FrameTemplateModel requestModel) throws GalleryException;

  /**
   * 額縁テンプレートを削除する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @throws GalleryException 額縁テンプレートが存在しない場合
   */
  void deleteFrameTemplate(AccountNo accountNo, FrameTemplateNo frameTemplateNo)
      throws GalleryException;
}
