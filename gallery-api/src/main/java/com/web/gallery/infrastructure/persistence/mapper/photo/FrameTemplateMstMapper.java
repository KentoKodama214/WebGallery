package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMst;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstUpdateTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 額縁テンプレートマスタテーブルのMapperクラス */
@Mapper
public interface FrameTemplateMstMapper {
  /**
   * 額縁テンプレートマスタを登録する
   *
   * @param frameTemplateMst {@link FrameTemplateMst}
   * @return 登録件数
   */
  public Integer insert(FrameTemplateMst frameTemplateMst);

  /**
   * 額縁テンプレートマスタを更新する
   *
   * @param condition 更新対象の抽出条件
   * @param target 更新内容
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") FrameTemplateMstCondition condition,
      @Param("target") FrameTemplateMstUpdateTarget target);

  /**
   * 条件に該当する額縁テンプレートマスタを物理削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(FrameTemplateMstCondition condition);

  /**
   * アカウントが登録済みの最大の額縁テンプレート番号を取得する
   *
   * @param accountNo アカウント番号
   * @return 最大の額縁テンプレート番号（未登録の場合はnull）
   */
  public Long getMaxFrameTemplateNo(Long accountNo);

  /**
   * 条件に該当する額縁テンプレートマスタの件数を取得する
   *
   * @param condition {@link FrameTemplateMstCondition}
   * @return 件数
   */
  public Integer count(FrameTemplateMstCondition condition);

  /**
   * 条件に該当する額縁テンプレートマスタを、額縁テンプレート番号の昇順に取得する
   *
   * @param condition {@link FrameTemplateMstCondition}
   * @return {@link FrameTemplateMst}のリスト
   */
  public List<FrameTemplateMst> select(FrameTemplateMstCondition condition);
}
