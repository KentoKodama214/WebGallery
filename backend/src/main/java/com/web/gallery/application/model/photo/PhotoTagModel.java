package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真タグの情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoTagModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真番号 */
  private PhotoNo photoNo;

  /** タグ番号 */
  private TagNo tagNo;

  /** タグ日本語名 */
  @NonNull private TagJapaneseName tagJapaneseName;

  /** タグ英語名 */
  @NonNull private TagEnglishName tagEnglishName;

  /**
   * タグ登録用にaccountNo・photoNo・tagNoを差し替えたPhotoTagModelを生成する
   *
   * <p>アカウント番号は元のPhotoTagModelの値ではなく、集約ルート（＝写真の所有者）の値を必ず採用する （元の値はクライアント入力由来のため、他人の写真へのタグ注入を防ぐ）
   *
   * @param source 元のPhotoTagModel
   * @param accountNo 写真所有者のアカウント番号
   * @param photoNo 写真番号
   * @param tagNo タグ番号
   * @return {@link PhotoTagModel}
   */
  public static PhotoTagModel forRegist(
      PhotoTagModel source, AccountNo accountNo, PhotoNo photoNo, TagNo tagNo) {
    return PhotoTagModel.builder()
        .accountNo(accountNo)
        .photoNo(photoNo)
        .tagNo(tagNo)
        .tagJapaneseName(source.getTagJapaneseName())
        .tagEnglishName(source.getTagEnglishName())
        .build();
  }
}
