package com.web.gallery.entity;

import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 写真タグマスタテーブルの抽出条件クラス */
@Data
@Builder
public class PhotoTagMstCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** 写真番号 */
  private Long photoNo;

  /** 写真番号リスト */
  private List<Long> photoNoList;

  /** タグ番号 */
  private Long tagNo;

  /** タグ日本語名 */
  private String tagJapaneseName;

  /** タグ英語名 */
  private String tagEnglishName;

  /**
   * 写真タグ削除用のPhotoTagDeleteModelから抽出条件を生成する
   *
   * @param model {@link PhotoTagDeleteModel}
   * @return {@link PhotoTagMstCondition}
   */
  public static PhotoTagMstCondition from(PhotoTagDeleteModel model) {
    return PhotoTagMstCondition.builder()
        .accountNo(model.getAccountNo().value())
        .photoNo(model.getPhotoNo().value())
        .build();
  }

  /**
   * PhotoDetailSearchModelから抽出条件を生成する
   *
   * @param model {@link PhotoDetailSearchModel}
   * @return {@link PhotoTagMstCondition}
   */
  public static PhotoTagMstCondition from(PhotoDetailSearchModel model) {
    return PhotoTagMstCondition.builder()
        .accountNo(model.getPhotoAccountNo().value())
        .photoNo(model.getPhotoNo().value())
        .build();
  }

  /**
   * PhotoGetModelから抽出条件を生成する
   *
   * <p>取得対象の写真番号リストで絞り込むことで、アカウント全体のタグではなく該当写真のタグのみを取得する
   *
   * @param model {@link PhotoGetModel}
   * @param photoNoList 抽出対象の写真番号リスト
   * @return {@link PhotoTagMstCondition}
   */
  public static PhotoTagMstCondition from(PhotoGetModel model, List<Long> photoNoList) {
    return PhotoTagMstCondition.builder()
        .accountNo(model.getPhotoAccountNo().value())
        .photoNoList(photoNoList)
        .build();
  }

  /**
   * アカウント番号で写真タグ削除用の抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link PhotoTagMstCondition}
   */
  public static PhotoTagMstCondition byAccountNo(Long accountNo) {
    return PhotoTagMstCondition.builder().accountNo(accountNo).build();
  }
}
