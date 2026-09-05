package com.web.gallery.aggregate;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 写真マスタ・タグのライフサイクルを管理する集約ルートクラス */
public class Photo {

  /** アカウント番号 */
  private final AccountNo accountNo;

  /** 写真番号 */
  private final PhotoNo photoNo;

  /** 写真の詳細情報（タグを含む） */
  private PhotoDetailModel detail;

  /** 削除フラグ */
  private boolean deleted;

  /** 削除する画像ファイルパス（削除時のみ設定） */
  private final ImageFilePath imageFilePathForDelete;

  private Photo(
      AccountNo accountNo,
      PhotoNo photoNo,
      PhotoDetailModel detail,
      ImageFilePath imageFilePathForDelete) {
    this.accountNo = accountNo;
    this.photoNo = photoNo;
    this.detail = detail;
    this.imageFilePathForDelete = imageFilePathForDelete;
  }

  /**
   * 新規登録用のPhotoを生成する
   *
   * <p>タグ番号は1からの連番に振り直す
   *
   * @param requestDetail 登録リクエストの写真詳細情報
   * @param newPhotoNo 新規採番された写真番号
   * @param assignedImageFilePath 採番された画像ファイルパス
   * @return {@link Photo}
   */
  public static Photo forRegist(
      PhotoDetailModel requestDetail, PhotoNo newPhotoNo, ImageFilePath assignedImageFilePath) {
    PhotoDetailModel detail =
        requestDetail.toBuilder().photoNo(newPhotoNo).imageFilePath(assignedImageFilePath).build();
    Photo photo = new Photo(requestDetail.getAccountNo(), newPhotoNo, detail, null);
    photo.updateTags(requestDetail.getPhotoTagModelList());
    return photo;
  }

  /**
   * 更新用のPhotoを生成する
   *
   * <p>タグ番号は1からの連番に振り直す
   *
   * @param requestDetail 更新リクエストの写真詳細情報
   * @return {@link Photo}
   */
  public static Photo forUpdate(PhotoDetailModel requestDetail) {
    Photo photo =
        new Photo(requestDetail.getAccountNo(), requestDetail.getPhotoNo(), requestDetail, null);
    photo.updateTags(requestDetail.getPhotoTagModelList());
    return photo;
  }

  /**
   * 削除用のPhotoを生成する
   *
   * @param accountNo アカウント番号
   * @param photoNo 写真番号
   * @param imageFilePathForDelete 削除する画像ファイルパス
   * @return {@link Photo}
   */
  public static Photo forDelete(
      AccountNo accountNo, PhotoNo photoNo, ImageFilePath imageFilePathForDelete) {
    Photo photo = new Photo(accountNo, photoNo, null, imageFilePathForDelete);
    photo.markAsDeleted();
    return photo;
  }

  /**
   * 写真タグを差し替える
   *
   * <p>タグ番号は1からの連番に振り直す
   *
   * @param newTags 差し替え後の{@link PhotoTagModelList}
   */
  public void updateTags(PhotoTagModelList newTags) {
    this.detail =
        this.detail.toBuilder()
            .photoTagModelList(renumberTags(newTags, this.accountNo, this.photoNo))
            .build();
  }

  /** 削除済みとしてマークする */
  public void markAsDeleted() {
    this.deleted = true;
  }

  /**
   * アカウント番号を取得する
   *
   * @return アカウント番号
   */
  public AccountNo getAccountNo() {
    return accountNo;
  }

  /**
   * 写真番号を取得する
   *
   * @return 写真番号
   */
  public PhotoNo getPhotoNo() {
    return photoNo;
  }

  /**
   * 写真の詳細情報を取得する
   *
   * <p>{@link #forDelete}で生成したPhotoでは呼び出せない（detailを保持しないため）
   *
   * @return {@link PhotoDetailModel}
   */
  public PhotoDetailModel getDetail() {
    return detail;
  }

  /**
   * 写真タグリストを取得する
   *
   * <p>{@link #forDelete}で生成したPhotoでは呼び出せない（detailを保持しないため）
   *
   * @return {@link PhotoTagModelList}
   */
  public PhotoTagModelList getPhotoTagModelList() {
    return detail.getPhotoTagModelList();
  }

  /**
   * 画像ファイルを取得する
   *
   * <p>{@link #forDelete}で生成したPhotoでは呼び出せない（detailを保持しないため）
   *
   * @return {@link ImageFile}
   */
  public ImageFile getImageFile() {
    return detail.getImageFile();
  }

  /**
   * 画像ファイルパスを取得する
   *
   * <p>{@link #forDelete}で生成したPhotoでは呼び出せない（detailを保持しないため）
   *
   * @return {@link ImageFilePath}
   */
  public ImageFilePath getImageFilePath() {
    return detail.getImageFilePath();
  }

  /**
   * 削除する画像ファイルパスを取得する
   *
   * @return {@link ImageFilePath}
   */
  public ImageFilePath getImageFilePathForDelete() {
    return imageFilePathForDelete;
  }

  /**
   * 削除済みかどうかを取得する
   *
   * @return 削除済みの場合、true
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * 写真タグのアカウント番号・写真番号を集約ルートの値に統一し、タグ番号を1からの連番に振り直す
   *
   * <p>アカウント番号はクライアント入力を信用せず、必ず写真の所有者の値に上書きする（他人の写真へのタグ注入を防ぐ）
   *
   * @param source 振り直し前の{@link PhotoTagModelList}
   * @param accountNo 写真所有者のアカウント番号
   * @param photoNo 振り直し後の写真番号
   * @return {@link PhotoTagModelList}
   */
  private static PhotoTagModelList renumberTags(
      PhotoTagModelList source, AccountNo accountNo, PhotoNo photoNo) {
    if (Objects.isNull(source)) {
      return null;
    }

    List<PhotoTagModel> renumbered = new ArrayList<>();
    int tagNo = 1;
    for (PhotoTagModel tag : source) {
      renumbered.add(PhotoTagModel.forRegist(tag, accountNo, photoNo, new TagNo((long) tagNo)));
      ++tagNo;
    }
    return PhotoTagModelList.of(renumbered);
  }
}
