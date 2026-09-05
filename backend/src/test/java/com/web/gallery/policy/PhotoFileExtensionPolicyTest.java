package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.photo.ImageFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhotoFileExtensionPolicyTest {

  private final PhotoFileExtensionPolicy photoFileExtensionPolicy = new PhotoFileExtensionPolicy();

  private ImageFile createImageFile(String originalFilename) {
    MultipartFile multipartFile =
        new MockMultipartFile(
            "file", originalFilename, "multipart/form-data", "sample image".getBytes());
    return new ImageFile(multipartFile);
  }

  @Test
  @Order(1)
  @DisplayName("正常系：許可されている拡張子（小文字）の場合、trueを返すこと")
  void isAllowedExtension_allowed_lowercase() {
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.jpg")));
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.jpeg")));
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.png")));
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.gif")));
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.webp")));
  }

  @Test
  @Order(2)
  @DisplayName("正常系：許可されている拡張子（大文字）の場合、trueを返すこと（大文字小文字を区別しない）")
  void isAllowedExtension_allowed_uppercase() {
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.JPG")));
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.PNG")));
  }

  @Test
  @Order(3)
  @DisplayName("異常系：許可されていない拡張子の場合、falseを返すこと")
  void isAllowedExtension_not_allowed() {
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile("malicious.exe")));
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile("malicious.jsp")));
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile("malicious.html")));
  }

  @Test
  @Order(4)
  @DisplayName("異常系：拡張子が存在しない場合、falseを返すこと")
  void isAllowedExtension_no_extension() {
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile("passwd")));
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile("DSC111.")));
  }

  @Test
  @Order(5)
  @DisplayName("異常系：オリジナルファイル名がnullの場合、falseを返すこと")
  void isAllowedExtension_filename_is_null() {
    assertFalse(photoFileExtensionPolicy.isAllowedExtension(createImageFile(null)));
  }

  @Test
  @Order(6)
  @DisplayName("正常系：パストラバーサルを含むファイル名でも、拡張子が許可されている場合はtrueを返すこと（ベース名化は呼び出し元の責務）")
  void isAllowedExtension_path_traversal_filename_with_allowed_extension() {
    assertTrue(photoFileExtensionPolicy.isAllowedExtension(createImageFile("../../etc/evil.jpg")));
  }
}
