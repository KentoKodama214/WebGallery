package com.web.gallery.controller.response;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoSaveResultModel;
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
}
