package com.web.gallery.application.aggregate;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioTest {

  private static final TemplateSettings GRID_SETTINGS =
      new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))));

  /**
   * 登録・更新リクエスト相当の写真を生成する（アカウント番号・ポートフォリオ番号・表示順は未設定）
   *
   * @param photoNo 写真番号
   * @param caption キャプション
   * @return {@link PortfolioPhotoModel}
   */
  private static PortfolioPhotoModel requestPhoto(Long photoNo, String caption) {
    return PortfolioPhotoModel.builder()
        .photoNo(new PhotoNo(photoNo))
        .photoSettings(new TemplateSettings(Map.of("caption", caption)))
        .build();
  }

  /**
   * 登録・更新リクエスト相当のタグを生成する（アカウント番号・ポートフォリオ番号・タグ番号は未設定）
   *
   * @param japaneseName タグ日本語名
   * @return {@link PortfolioTagModel}
   */
  private static PortfolioTagModel requestTag(String japaneseName) {
    return PortfolioTagModel.builder()
        .tagJapaneseName(new TagJapaneseName(japaneseName))
        .tagEnglishName(new TagEnglishName(""))
        .build();
  }

  /**
   * 登録・更新リクエスト相当のポートフォリオ詳細情報を生成する
   *
   * @param accountNo アカウント番号
   * @param photos 写真
   * @param tags タグ
   * @return {@link PortfolioDetailModel}
   */
  private static PortfolioDetailModel requestDetail(
      Long accountNo, List<PortfolioPhotoModel> photos, List<PortfolioTagModel> tags) {
    return PortfolioDetailModel.builder()
        .accountNo(new AccountNo(accountNo))
        .theme(new PortfolioTheme("日本の自然"))
        .description(new PortfolioDescription("説明文"))
        .templateKbn(PortfolioTemplateEnum.GRID)
        .templateSettings(GRID_SETTINGS)
        .portfolioPhotoModelList(PortfolioPhotoModelList.of(photos))
        .portfolioTagModelList(PortfolioTagModelList.of(tags))
        .build();
  }

  /**
   * 登録済みのポートフォリオ詳細情報を生成する
   *
   * @param statusKbn 公開状態区分
   * @param photoNos 表示順に並べた写真番号
   * @return {@link PortfolioDetailModel}
   */
  private static PortfolioDetailModel existingDetail(
      PortfolioStatusEnum statusKbn, Long... photoNos) {
    Portfolio registered =
        Portfolio.forRegist(
            requestDetail(
                1L,
                Arrays.stream(photoNos)
                    .map(photoNo -> requestPhoto(photoNo, "キャプション" + photoNo))
                    .toList(),
                List.of(requestTag("自然"))),
            new PortfolioNo(5L));
    return registered.getDetail().toBuilder().statusKbn(statusKbn).build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きとして生成し、写真の表示順・タグ番号をリクエストの並び順で1から振ること")
    void forRegist_success() {
      PortfolioDetailModel request =
          requestDetail(
              1L,
              List.of(requestPhoto(30L, "c"), requestPhoto(10L, "a"), requestPhoto(20L, "b")),
              List.of(requestTag("自然"), requestTag("風景")));

      Portfolio actual = Portfolio.forRegist(request, new PortfolioNo(5L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(5L), actual.getPortfolioNo());
      assertFalse(actual.isPublished());
      assertFalse(actual.isRevertedToDraft());
      assertEquals(new PhotoCount(3), actual.getPhotoCount());

      PortfolioDetailModel detail = actual.getDetail();
      assertEquals(new AccountNo(1L), detail.getAccountNo());
      assertEquals(new PortfolioNo(5L), detail.getPortfolioNo());
      assertEquals(new PortfolioTheme("日本の自然"), detail.getTheme());
      assertEquals(new PortfolioDescription("説明文"), detail.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, detail.getTemplateKbn());
      assertEquals(GRID_SETTINGS, detail.getTemplateSettings());
      assertEquals(PortfolioStatusEnum.DRAFT, detail.getStatusKbn());

      List<PortfolioPhotoModel> photos = detail.getPortfolioPhotoModelList().toList();
      assertEquals(
          List.of(new PhotoNo(30L), new PhotoNo(10L), new PhotoNo(20L)),
          photos.stream().map(PortfolioPhotoModel::getPhotoNo).toList());
      assertEquals(
          List.of(new DisplayOrder(1), new DisplayOrder(2), new DisplayOrder(3)),
          photos.stream().map(PortfolioPhotoModel::getDisplayOrder).toList());
      photos.forEach(
          photo -> {
            assertEquals(new AccountNo(1L), photo.getAccountNo());
            assertEquals(new PortfolioNo(5L), photo.getPortfolioNo());
          });
      assertEquals(new TemplateSettings(Map.of("caption", "c")), photos.get(0).getPhotoSettings());

      List<PortfolioTagModel> tags = detail.getPortfolioTagModelList().toList();
      assertEquals(
          List.of(new TagNo(1L), new TagNo(2L)),
          tags.stream().map(PortfolioTagModel::getTagNo).toList());
      assertEquals(new TagJapaneseName("風景"), tags.get(1).getTagJapaneseName());
      tags.forEach(
          tag -> {
            assertEquals(new AccountNo(1L), tag.getAccountNo());
            assertEquals(new PortfolioNo(5L), tag.getPortfolioNo());
          });
    }

    @Test
    @Order(2)
    @DisplayName("セキュリティ：写真・タグのアカウント番号・表示順・タグ番号が改ざんされていても、集約ルートの値で上書きすること")
    void forRegist_ownerForced() {
      PortfolioDetailModel request =
          requestDetail(
              1L,
              List.of(
                  requestPhoto(10L, "a").toBuilder()
                      .accountNo(new AccountNo(99L))
                      .portfolioNo(new PortfolioNo(99L))
                      .displayOrder(new DisplayOrder(99))
                      .build()),
              List.of(
                  requestTag("自然").toBuilder()
                      .accountNo(new AccountNo(99L))
                      .portfolioNo(new PortfolioNo(99L))
                      .tagNo(new TagNo(99L))
                      .build()));

      Portfolio actual = Portfolio.forRegist(request, new PortfolioNo(5L));

      PortfolioPhotoModel photo = actual.getDetail().getPortfolioPhotoModelList().get(0);
      assertEquals(new AccountNo(1L), photo.getAccountNo());
      assertEquals(new PortfolioNo(5L), photo.getPortfolioNo());
      assertEquals(new DisplayOrder(1), photo.getDisplayOrder());
      PortfolioTagModel tag = actual.getDetail().getPortfolioTagModelList().get(0);
      assertEquals(new AccountNo(1L), tag.getAccountNo());
      assertEquals(new PortfolioNo(5L), tag.getPortfolioNo());
      assertEquals(new TagNo(1L), tag.getTagNo());
    }

    @Test
    @Order(3)
    @DisplayName("セキュリティ：リクエストで公開状態が指定されていても、下書きとして生成すること")
    void forRegist_statusForcedToDraft() {
      PortfolioDetailModel request =
          requestDetail(1L, List.of(requestPhoto(10L, "a")), List.of()).toBuilder()
              .statusKbn(PortfolioStatusEnum.PUBLISHED)
              .build();

      Portfolio actual = Portfolio.forRegist(request, new PortfolioNo(5L));

      assertEquals(PortfolioStatusEnum.DRAFT, actual.getDetail().getStatusKbn());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：所有者・ポートフォリオ番号・公開状態を登録済みの値で引き継ぎ、内容をリクエストで置き換えること")
    void forUpdate_success() {
      PortfolioDetailModel existing = existingDetail(PortfolioStatusEnum.PUBLISHED, 1L, 2L);
      PortfolioDetailModel request =
          requestDetail(
                  1L,
                  List.of(requestPhoto(3L, "新3"), requestPhoto(1L, "新1")),
                  List.of(requestTag("新タグ")))
              .toBuilder()
              .theme(new PortfolioTheme("新テーマ"))
              .build();

      Portfolio actual = Portfolio.forUpdate(existing, request);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(5L), actual.getPortfolioNo());
      assertTrue(actual.isPublished());
      PortfolioDetailModel detail = actual.getDetail();
      assertEquals(new PortfolioNo(5L), detail.getPortfolioNo());
      assertEquals(new PortfolioTheme("新テーマ"), detail.getTheme());
      assertEquals(PortfolioStatusEnum.PUBLISHED, detail.getStatusKbn());
      assertEquals(
          List.of(new PhotoNo(3L), new PhotoNo(1L)),
          detail.getPortfolioPhotoModelList().stream()
              .map(PortfolioPhotoModel::getPhotoNo)
              .toList());
      assertEquals(
          List.of(new DisplayOrder(1), new DisplayOrder(2)),
          detail.getPortfolioPhotoModelList().stream()
              .map(PortfolioPhotoModel::getDisplayOrder)
              .toList());
      assertEquals(new TagNo(1L), detail.getPortfolioTagModelList().get(0).getTagNo());
      assertEquals(
          new TagJapaneseName("新タグ"),
          detail.getPortfolioTagModelList().get(0).getTagJapaneseName());
    }

    @Test
    @Order(2)
    @DisplayName("セキュリティ：リクエストのアカウント番号・ポートフォリオ番号・公開状態が改ざんされていても、登録済みの値を優先すること")
    void forUpdate_ownerForced() {
      PortfolioDetailModel existing = existingDetail(PortfolioStatusEnum.DRAFT, 1L);
      PortfolioDetailModel request =
          requestDetail(99L, List.of(requestPhoto(1L, "a")), List.of(requestTag("自然"))).toBuilder()
              .portfolioNo(new PortfolioNo(99L))
              .statusKbn(PortfolioStatusEnum.PUBLISHED)
              .build();

      Portfolio actual = Portfolio.forUpdate(existing, request);

      PortfolioDetailModel detail = actual.getDetail();
      assertEquals(new AccountNo(1L), detail.getAccountNo());
      assertEquals(new PortfolioNo(5L), detail.getPortfolioNo());
      assertEquals(PortfolioStatusEnum.DRAFT, detail.getStatusKbn());
      assertEquals(new AccountNo(1L), detail.getPortfolioPhotoModelList().get(0).getAccountNo());
      assertEquals(new AccountNo(1L), detail.getPortfolioTagModelList().get(0).getAccountNo());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class reconstruct {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの詳細情報をそのまま保持すること")
    void reconstruct_success() {
      PortfolioDetailModel existing = existingDetail(PortfolioStatusEnum.PUBLISHED, 1L, 2L);

      Portfolio actual = Portfolio.reconstruct(existing);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(5L), actual.getPortfolioNo());
      assertSame(existing, actual.getDetail());
      assertTrue(actual.isPublished());
      assertFalse(actual.isRevertedToDraft());
      assertEquals(new PhotoCount(2), actual.getPhotoCount());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forDelete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号のみを保持し、詳細情報はnullとすること")
    void forDelete_success() {
      Portfolio actual = Portfolio.forDelete(new AccountNo(1L), new PortfolioNo(5L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(5L), actual.getPortfolioNo());
      assertNull(actual.getDetail());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class publish {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きから公開に変更すること")
    void publish_success() {
      Portfolio portfolio = Portfolio.reconstruct(existingDetail(PortfolioStatusEnum.DRAFT, 1L));

      portfolio.publish();

      assertTrue(portfolio.isPublished());
      assertEquals(PortfolioStatusEnum.PUBLISHED, portfolio.getDetail().getStatusKbn());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class unpublish {
    @Test
    @Order(1)
    @DisplayName("正常系：公開から下書きに変更すること（写真削除による差し戻しとは扱わない）")
    void unpublish_success() {
      Portfolio portfolio =
          Portfolio.reconstruct(existingDetail(PortfolioStatusEnum.PUBLISHED, 1L));

      portfolio.unpublish();

      assertFalse(portfolio.isPublished());
      assertEquals(PortfolioStatusEnum.DRAFT, portfolio.getDetail().getStatusKbn());
      assertFalse(portfolio.isRevertedToDraft());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class removePhoto {
    @Test
    @Order(1)
    @DisplayName("正常系：公開中の場合、写真を除外して表示順を詰め、下書きに差し戻すこと")
    void removePhoto_published() {
      Portfolio portfolio =
          Portfolio.reconstruct(existingDetail(PortfolioStatusEnum.PUBLISHED, 1L, 2L, 3L));

      portfolio.removePhoto(new PhotoNo(2L));

      List<PortfolioPhotoModel> photos =
          portfolio.getDetail().getPortfolioPhotoModelList().toList();
      assertEquals(
          List.of(new PhotoNo(1L), new PhotoNo(3L)),
          photos.stream().map(PortfolioPhotoModel::getPhotoNo).toList());
      assertEquals(
          List.of(new DisplayOrder(1), new DisplayOrder(2)),
          photos.stream().map(PortfolioPhotoModel::getDisplayOrder).toList());
      // 写真単位の設定は除外されなかった写真のものをそのまま引き継ぐ
      assertEquals(
          new TemplateSettings(Map.of("caption", "キャプション3")), photos.get(1).getPhotoSettings());
      assertFalse(portfolio.isPublished());
      assertTrue(portfolio.isRevertedToDraft());
      assertEquals(new PhotoCount(2), portfolio.getPhotoCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：下書きの場合、写真を除外するが差し戻しとは扱わないこと")
    void removePhoto_draft() {
      Portfolio portfolio =
          Portfolio.reconstruct(existingDetail(PortfolioStatusEnum.DRAFT, 1L, 2L));

      portfolio.removePhoto(new PhotoNo(1L));

      assertEquals(
          new PhotoNo(2L), portfolio.getDetail().getPortfolioPhotoModelList().get(0).getPhotoNo());
      assertEquals(
          new DisplayOrder(1),
          portfolio.getDetail().getPortfolioPhotoModelList().get(0).getDisplayOrder());
      assertFalse(portfolio.isPublished());
      assertFalse(portfolio.isRevertedToDraft());
    }
  }
}
