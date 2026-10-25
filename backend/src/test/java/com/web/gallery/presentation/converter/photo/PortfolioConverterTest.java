package com.web.gallery.presentation.converter.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioNoList;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TemplateSettings;
import com.web.gallery.presentation.request.photo.PortfolioDisplayOrderUpdateRequest;
import com.web.gallery.presentation.request.photo.PortfolioListRequest;
import com.web.gallery.presentation.request.photo.PortfolioPhotoSaveRequest;
import com.web.gallery.presentation.request.photo.PortfolioPhotoSearchRequest;
import com.web.gallery.presentation.request.photo.PortfolioSaveRequest;
import com.web.gallery.presentation.request.photo.PortfolioTagSaveRequest;
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
class PortfolioConverterTest {

  private final PortfolioConverter portfolioConverter = new PortfolioConverter();

  /**
   * 全項目を設定したポートフォリオ登録・更新リクエストを生成する
   *
   * @return {@link PortfolioSaveRequest}
   */
  private static PortfolioSaveRequest fullRequest() {
    PortfolioPhotoSaveRequest photo1 = new PortfolioPhotoSaveRequest();
    photo1.setPhotoNo(20L);
    photo1.setPhotoSettings(Map.of("caption", "金閣寺"));
    PortfolioPhotoSaveRequest photo2 = new PortfolioPhotoSaveRequest();
    photo2.setPhotoNo(10L);
    photo2.setPhotoSettings(Map.of("caption", "橋杭岩"));
    PortfolioTagSaveRequest tag = new PortfolioTagSaveRequest();
    tag.setTagJapaneseName("自然");
    tag.setTagEnglishName("nature");

    PortfolioSaveRequest request = new PortfolioSaveRequest();
    request.setTheme("日本の自然");
    request.setDescription("説明文");
    request.setTemplateKbn(PortfolioTemplateEnum.GRID);
    request.setTemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))));
    request.setPhotoList(List.of(photo1, photo2));
    request.setTagList(List.of(tag));
    return request;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPortfolioDetailModel {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目を設定した場合、写真の並び順を保ったまま値が転写され、アカウント番号はセッション由来の値となること")
    void toPortfolioDetailModel_allFieldsSet() {
      PortfolioDetailModel actual =
          portfolioConverter.toPortfolioDetailModel(fullRequest(), new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertNull(actual.getPortfolioNo());
      assertEquals(new PortfolioTheme("日本の自然"), actual.getTheme());
      assertEquals(new PortfolioDescription("説明文"), actual.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, actual.getTemplateKbn());
      assertEquals(
          new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")))),
          actual.getTemplateSettings());
      // 公開状態はサーバ側で決定するため設定しない
      assertNull(actual.getStatusKbn());

      List<PortfolioPhotoModel> photos = actual.getPortfolioPhotoModelList().toList();
      assertEquals(
          List.of(new PhotoNo(20L), new PhotoNo(10L)),
          photos.stream().map(PortfolioPhotoModel::getPhotoNo).toList());
      assertEquals(
          new TemplateSettings(Map.of("caption", "金閣寺")), photos.get(0).getPhotoSettings());
      // 所有者・表示順は集約ルートが設定するため、ここでは設定しない
      assertNull(photos.get(0).getAccountNo());
      assertNull(photos.get(0).getDisplayOrder());

      PortfolioTagModel tag = actual.getPortfolioTagModelList().get(0);
      assertEquals(new TagJapaneseName("自然"), tag.getTagJapaneseName());
      assertEquals(new TagEnglishName("nature"), tag.getTagEnglishName());
      assertNull(tag.getAccountNo());
      assertNull(tag.getTagNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：任意項目を未設定にした場合、空文字・項目なしの設定・空リストとなること")
    void toPortfolioDetailModel_optionalFieldsNull() {
      PortfolioPhotoSaveRequest photo = new PortfolioPhotoSaveRequest();
      photo.setPhotoNo(1L);
      PortfolioTagSaveRequest tag = new PortfolioTagSaveRequest();
      tag.setTagJapaneseName("自然");
      PortfolioSaveRequest request = new PortfolioSaveRequest();
      request.setTheme("日本の自然");
      request.setTemplateKbn(PortfolioTemplateEnum.FEATURE);
      request.setPhotoList(List.of(photo));
      request.setTagList(List.of(tag));

      PortfolioDetailModel actual =
          portfolioConverter.toPortfolioDetailModel(request, new AccountNo(1L));

      assertEquals(new PortfolioDescription(""), actual.getDescription());
      assertEquals(TemplateSettings.empty(), actual.getTemplateSettings());
      assertEquals(
          TemplateSettings.empty(), actual.getPortfolioPhotoModelList().get(0).getPhotoSettings());
      assertEquals(
          new TagEnglishName(""), actual.getPortfolioTagModelList().get(0).getTagEnglishName());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：写真一覧・タグ一覧がnullの場合、空リストとなること")
    void toPortfolioDetailModel_listsNull() {
      PortfolioSaveRequest request = new PortfolioSaveRequest();
      request.setTheme("日本の自然");
      request.setTemplateKbn(PortfolioTemplateEnum.FEATURE);

      PortfolioDetailModel actual =
          portfolioConverter.toPortfolioDetailModel(request, new AccountNo(1L));

      assertTrue(actual.getPortfolioPhotoModelList().isEmpty());
      assertTrue(actual.getPortfolioTagModelList().isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：更新用の場合、パスパラメータのポートフォリオ番号が設定されること")
    void toPortfolioDetailModel_forUpdate() {
      PortfolioDetailModel actual =
          portfolioConverter.toPortfolioDetailModel(
              fullRequest(), new AccountNo(1L), new PortfolioNo(5L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PortfolioNo(5L), actual.getPortfolioNo());
      assertEquals(new PortfolioTheme("日本の自然"), actual.getTheme());
      assertEquals(2, actual.getPortfolioPhotoModelList().size());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPortfolioListGetModel {
    @Test
    @Order(1)
    @DisplayName("正常系：テーマ・タグの前後の空白を除き、ログイン中のアカウント番号とあわせて変換すること")
    void toPortfolioListGetModel_allFieldsSet() {
      PortfolioListRequest request = new PortfolioListRequest();
      request.setTheme(" 自然 ");
      request.setTag("風景");

      PortfolioListGetModel actual =
          portfolioConverter.toPortfolioListGetModel(request, new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals("自然", actual.getTheme());
      assertEquals("風景", actual.getTag());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未入力・空白のみの条件はnull（未指定）になること")
    void toPortfolioListGetModel_optionalFieldsNull() {
      PortfolioListRequest request = new PortfolioListRequest();
      request.setTag("　 ");

      PortfolioListGetModel actual =
          portfolioConverter.toPortfolioListGetModel(request, new AccountNo(1L));

      assertNull(actual.getTheme());
      assertNull(actual.getTag());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPortfolioPhotoSearchModel {
    @Test
    @Order(1)
    @DisplayName("正常系：検索語の前後の空白を除き、ページ番号・アカウント番号とあわせて変換すること")
    void toPortfolioPhotoSearchModel_allFieldsSet() {
      PortfolioPhotoSearchRequest request = new PortfolioPhotoSearchRequest();
      request.setKeyword(" 金閣 ");
      request.setPageNo(3);

      PortfolioPhotoSearchModel actual =
          portfolioConverter.toPortfolioPhotoSearchModel(request, new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals("金閣", actual.getKeyword());
      assertEquals(3, actual.getPageNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空白のみの検索語はnull（未指定）、ページ番号は既定値の1になること")
    void toPortfolioPhotoSearchModel_optionalFieldsNull() {
      PortfolioPhotoSearchRequest request = new PortfolioPhotoSearchRequest();
      request.setKeyword(" ");

      PortfolioPhotoSearchModel actual =
          portfolioConverter.toPortfolioPhotoSearchModel(request, new AccountNo(1L));

      assertNull(actual.getKeyword());
      assertEquals(1, actual.getPageNo());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPortfolioNoList {
    @Test
    @Order(1)
    @DisplayName("正常系：リクエストのポートフォリオ番号が、並び順を保ったままPortfolioNoListへ変換されること")
    void toPortfolioNoList_allFieldsSet() {
      PortfolioDisplayOrderUpdateRequest request = new PortfolioDisplayOrderUpdateRequest();
      request.setPortfolioNoList(List.of(3L, 1L, 2L));

      PortfolioNoList actual = portfolioConverter.toPortfolioNoList(request);

      assertEquals(
          List.of(new PortfolioNo(3L), new PortfolioNo(1L), new PortfolioNo(2L)), actual.toList());
    }
  }
}
