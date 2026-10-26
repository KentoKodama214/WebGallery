package com.web.gallery.application.aggregate;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.TagNo;
import java.util.ArrayList;
import java.util.List;

/**
 * ポートフォリオ・ポートフォリオ写真・ポートフォリオタグのライフサイクルを管理する集約ルートクラス
 *
 * <p>公開状態の遷移（下書き⇔公開、写真削除に伴う下書きへの自動差し戻し）のルールをこのクラスに閉じ込める。 公開できる設定かどうかの判定は{@link
 * com.web.gallery.domain.service.PortfolioTemplateSettingsPolicy}が行い、Service層が判定結果に応じて {@link
 * #publish}を呼び出す
 */
public class Portfolio {

  /** アカウント番号（ポートフォリオ所有者） */
  private final AccountNo accountNo;

  /** ポートフォリオ番号 */
  private final PortfolioNo portfolioNo;

  /** ポートフォリオの詳細情報（写真・タグを含む。削除時はnull） */
  private PortfolioDetailModel detail;

  /** 写真削除に伴い公開から下書きへ差し戻したかどうか */
  private boolean revertedToDraft;

  private Portfolio(AccountNo accountNo, PortfolioNo portfolioNo, PortfolioDetailModel detail) {
    this.accountNo = accountNo;
    this.portfolioNo = portfolioNo;
    this.detail = detail;
  }

  /**
   * 新規登録用のPortfolioを生成する
   *
   * <p>公開状態は下書きとする。写真の表示順・タグ番号はリクエストの並び順で1から振り直す
   *
   * @param requestDetail 登録リクエストのポートフォリオ詳細情報
   * @param newPortfolioNo 新規採番されたポートフォリオ番号
   * @return {@link Portfolio}
   */
  public static Portfolio forRegist(
      PortfolioDetailModel requestDetail, PortfolioNo newPortfolioNo) {
    AccountNo accountNo = requestDetail.getAccountNo();
    return new Portfolio(
        accountNo,
        newPortfolioNo,
        normalize(requestDetail, accountNo, newPortfolioNo, PortfolioStatusEnum.DRAFT));
  }

  /**
   * 更新用のPortfolioを生成する
   *
   * <p>所有者・ポートフォリオ番号・公開状態は登録済みの値を引き継ぎ、それ以外の項目をリクエストの内容で置き換える。 写真の表示順・タグ番号はリクエストの並び順で1から振り直す
   *
   * @param existingDetail 登録済みのポートフォリオ詳細情報
   * @param requestDetail 更新リクエストのポートフォリオ詳細情報
   * @return {@link Portfolio}
   */
  public static Portfolio forUpdate(
      PortfolioDetailModel existingDetail, PortfolioDetailModel requestDetail) {
    AccountNo accountNo = existingDetail.getAccountNo();
    PortfolioNo portfolioNo = existingDetail.getPortfolioNo();
    return new Portfolio(
        accountNo,
        portfolioNo,
        normalize(requestDetail, accountNo, portfolioNo, existingDetail.getStatusKbn()));
  }

  /**
   * 登録済みのポートフォリオから、公開状態の変更・写真の除外のためのPortfolioを復元する
   *
   * @param existingDetail 登録済みのポートフォリオ詳細情報
   * @return {@link Portfolio}
   */
  public static Portfolio reconstruct(PortfolioDetailModel existingDetail) {
    return new Portfolio(
        existingDetail.getAccountNo(), existingDetail.getPortfolioNo(), existingDetail);
  }

  /**
   * 削除用のPortfolioを生成する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link Portfolio}
   */
  public static Portfolio forDelete(AccountNo accountNo, PortfolioNo portfolioNo) {
    return new Portfolio(accountNo, portfolioNo, null);
  }

  /** ポートフォリオを公開する */
  public void publish() {
    this.detail = this.detail.toBuilder().statusKbn(PortfolioStatusEnum.PUBLISHED).build();
  }

  /** ポートフォリオを下書きに戻す */
  public void unpublish() {
    this.detail = this.detail.toBuilder().statusKbn(PortfolioStatusEnum.DRAFT).build();
  }

  /**
   * 削除された写真をポートフォリオから除外する
   *
   * <p>残りの写真の表示順を1から詰め直す。公開中だった場合は、写真の構成が変わり表示が崩れうるため下書きへ差し戻す （ユーザーが内容を確認してから公開し直す）
   *
   * @param photoNo 削除された写真の写真番号
   */
  public void removePhoto(PhotoNo photoNo) {
    List<PortfolioPhotoModel> remaining =
        this.detail.getPortfolioPhotoModelList().stream()
            .filter(photo -> !photo.getPhotoNo().equals(photoNo))
            .toList();
    this.detail =
        this.detail.toBuilder()
            .portfolioPhotoModelList(renumberPhotos(remaining, accountNo, portfolioNo))
            .build();

    if (isPublished()) {
      unpublish();
      this.revertedToDraft = true;
    }
  }

  /**
   * 公開中かどうかを取得する
   *
   * @return 公開中の場合はtrue
   */
  public Boolean isPublished() {
    return this.detail.getStatusKbn() == PortfolioStatusEnum.PUBLISHED;
  }

  /**
   * 写真削除に伴い公開から下書きへ差し戻したかどうかを取得する
   *
   * @return 差し戻した場合はtrue
   */
  public Boolean isRevertedToDraft() {
    return revertedToDraft;
  }

  /**
   * ポートフォリオに設定した写真の枚数を取得する
   *
   * @return {@link PhotoCount}
   */
  public PhotoCount getPhotoCount() {
    return new PhotoCount(this.detail.getPortfolioPhotoModelList().size());
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
   * ポートフォリオ番号を取得する
   *
   * @return ポートフォリオ番号
   */
  public PortfolioNo getPortfolioNo() {
    return portfolioNo;
  }

  /**
   * ポートフォリオの詳細情報を取得する
   *
   * <p>{@link #forDelete}で生成したPortfolioではnullとなる
   *
   * @return {@link PortfolioDetailModel}
   */
  public PortfolioDetailModel getDetail() {
    return detail;
  }

  /**
   * 所有者・ポートフォリオ番号・公開状態を確定し、写真の表示順・タグ番号を振り直した詳細情報を生成する
   *
   * <p>写真・タグのアカウント番号はリクエスト由来の値ではなく、集約ルート（＝ポートフォリオの所有者）の値を必ず採用する （他人のポートフォリオへの写真・タグの注入を防ぐため）
   *
   * @param source リクエスト由来のポートフォリオ詳細情報
   * @param accountNo ポートフォリオ所有者のアカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @param statusKbn 公開状態区分
   * @return 確定済みのポートフォリオ詳細情報
   */
  private static PortfolioDetailModel normalize(
      PortfolioDetailModel source,
      AccountNo accountNo,
      PortfolioNo portfolioNo,
      PortfolioStatusEnum statusKbn) {
    return source.toBuilder()
        .accountNo(accountNo)
        .portfolioNo(portfolioNo)
        .statusKbn(statusKbn)
        .portfolioPhotoModelList(
            renumberPhotos(source.getPortfolioPhotoModelList().toList(), accountNo, portfolioNo))
        .portfolioTagModelList(
            renumberTags(source.getPortfolioTagModelList().toList(), accountNo, portfolioNo))
        .build();
  }

  private static PortfolioPhotoModelList renumberPhotos(
      List<PortfolioPhotoModel> photos, AccountNo accountNo, PortfolioNo portfolioNo) {
    List<PortfolioPhotoModel> renumbered = new ArrayList<>();
    for (int i = 0; i < photos.size(); i++) {
      renumbered.add(
          photos.get(i).toBuilder()
              .accountNo(accountNo)
              .portfolioNo(portfolioNo)
              .displayOrder(new DisplayOrder(i + 1))
              .build());
    }
    return PortfolioPhotoModelList.of(renumbered);
  }

  private static PortfolioTagModelList renumberTags(
      List<PortfolioTagModel> tags, AccountNo accountNo, PortfolioNo portfolioNo) {
    List<PortfolioTagModel> renumbered = new ArrayList<>();
    for (int i = 0; i < tags.size(); i++) {
      renumbered.add(
          tags.get(i).toBuilder()
              .accountNo(accountNo)
              .portfolioNo(portfolioNo)
              .tagNo(new TagNo((long) i + 1))
              .build());
    }
    return PortfolioTagModelList.of(renumbered);
  }
}
