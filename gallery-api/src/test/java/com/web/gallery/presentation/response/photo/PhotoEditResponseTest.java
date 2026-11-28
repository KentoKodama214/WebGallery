package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PhotoSaveResultModel;
import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.presentation.request.photo.PhotoSaveRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoEditResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class ofWithMessageAndPhotoNoAndImageFilePath {
    @Test
    @Order(1)
    @DisplayName("正常系：指定した値がそのまま設定されること")
    void of_success() {
      PhotoEditResponse actual = PhotoEditResponse.of("メッセージ", 1L, "path/to/image.jpg");

      assertEquals("メッセージ", actual.getMessage());
      assertEquals(1L, actual.getPhotoNo());
      assertEquals("path/to/image.jpg", actual.getImageFilePath());
      assertTrue(actual.getIsSuccess());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class ofWithPhotoSaveResultModelAndPhotoSaveRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：保存結果に画像ファイルパスが設定されている場合、その値が採用されること")
    void of_withSavedImageFilePath() {
      PhotoSaveResultModel resultModel =
          PhotoSaveResultModel.builder()
              .photoNo(new PhotoNo(1L))
              .imageFilePath(new ImageFilePath("saved/path.jpg"))
              .build();
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setImageFilePath("request/path.jpg");

      PhotoEditResponse actual = PhotoEditResponse.of(resultModel, request);

      assertEquals("saved/path.jpg", actual.getImageFilePath());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：保存結果に画像ファイルパスが未設定でリクエストに設定されている場合、リクエストの値が採用されること")
    void of_withRequestImageFilePath() {
      PhotoSaveResultModel resultModel =
          PhotoSaveResultModel.builder().photoNo(new PhotoNo(1L)).imageFilePath(null).build();
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setImageFilePath("request/path.jpg");

      PhotoEditResponse actual = PhotoEditResponse.of(resultModel, request);

      assertEquals("request/path.jpg", actual.getImageFilePath());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：保存結果・リクエストともに画像ファイルパスが未設定の場合、空文字が採用されること")
    void of_withoutImageFilePath() {
      PhotoSaveResultModel resultModel =
          PhotoSaveResultModel.builder().photoNo(new PhotoNo(1L)).imageFilePath(null).build();
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setImageFilePath(null);

      PhotoEditResponse actual = PhotoEditResponse.of(resultModel, request);

      assertEquals("", actual.getImageFilePath());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class ofDeleted {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きに戻したポートフォリオがない場合、通常の削除完了メッセージと空リストが設定されること")
    void ofDeleted_noReverted() {
      PhotoEditResponse actual = PhotoEditResponse.ofDeleted(PortfolioModelList.empty());

      assertEquals(200, actual.getHttpStatus());
      assertTrue(actual.getIsSuccess());
      assertEquals("写真削除が完了しました。", actual.getMessage());
      assertTrue(actual.getRevertedPortfolioList().isEmpty());
      assertNull(actual.getPhotoNo());
      assertNull(actual.getImageFilePath());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：下書きに戻したポートフォリオがある場合、テーマを「、」区切りで含む確認を促すメッセージと対象が設定されること")
    void ofDeleted_withReverted() {
      PhotoEditResponse actual =
          PhotoEditResponse.ofDeleted(
              PortfolioModelList.of(List.of(portfolio(1L, "日本の自然"), portfolio(3L, "光と影"))));

      assertTrue(actual.getIsSuccess());
      assertEquals(
          "写真削除が完了しました。この写真を使用していたポートフォリオ（日本の自然、光と影）を下書きに戻しました。内容を確認して公開してください。",
          actual.getMessage());
      assertEquals(2, actual.getRevertedPortfolioList().size());
      assertEquals(1L, actual.getRevertedPortfolioList().get(0).getPortfolioNo());
      assertEquals("日本の自然", actual.getRevertedPortfolioList().get(0).getTheme());
      assertEquals(3L, actual.getRevertedPortfolioList().get(1).getPortfolioNo());
      assertEquals("光と影", actual.getRevertedPortfolioList().get(1).getTheme());
    }

    private PortfolioModel portfolio(Long portfolioNo, String theme) {
      return PortfolioModel.builder()
          .accountNo(new AccountNo(1L))
          .portfolioNo(new PortfolioNo(portfolioNo))
          .theme(new PortfolioTheme(theme))
          .templateKbn(PortfolioTemplateEnum.FEATURE)
          .statusKbn(PortfolioStatusEnum.DRAFT)
          .photoCount(new PhotoCount(1))
          .build();
    }
  }
}
