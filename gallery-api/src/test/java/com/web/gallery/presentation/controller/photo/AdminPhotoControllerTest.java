package com.web.gallery.presentation.controller.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.photo.AdminPhotoListGetModel;
import com.web.gallery.application.model.photo.AdminPhotoModel;
import com.web.gallery.application.model.photo.AdminPhotoModelList;
import com.web.gallery.application.model.photo.AdminPhotoPageModel;
import com.web.gallery.application.model.photo.PhotoVisibilityChangeModel;
import com.web.gallery.application.service.photo.PhotoService;
import com.web.gallery.domain.exception.PhotoNotFoundException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
import com.web.gallery.presentation.converter.photo.PhotoConverter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AdminPhotoControllerTest {
  @InjectMocks private AdminPhotoController adminPhotoController;

  @Mock private PhotoService photoService;

  @Mock private SessionHelper sessionHelper;

  @Spy private PhotoConverter photoConverter = new PhotoConverter();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    lenient().when(sessionHelper.getAccountNo()).thenReturn(99L);
    mockMvc =
        MockMvcBuilders.standaloneSetup(adminPhotoController)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/AdminPhotoControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAdminPhotoList {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者により非公開にされた写真を含む、全アカウントの写真一覧を取得できること")
    void getAdminPhotoList_success() throws Exception {
      AdminPhotoModelList adminPhotoModelList =
          AdminPhotoModelList.of(
              List.of(
                  AdminPhotoModel.builder()
                      .accountNo(new AccountNo(1L))
                      .accountId(new AccountId("aaaaaaaa"))
                      .accountName(new AccountName("AAAAAAAA"))
                      .photoNo(new PhotoNo(3L))
                      .photoJapaneseTitle(new PhotoJapaneseTitle("タイトル"))
                      .imageFilePath(new ImageFilePath("https://signed.example/1/3-abc.jpg"))
                      .isHiddenByAdmin(new IsHiddenByAdmin(true))
                      .createdAt(OffsetDateTime.of(2026, 11, 1, 10, 0, 0, 0, ZoneOffset.ofHours(9)))
                      .build()));
      ArgumentCaptor<AdminPhotoListGetModel> modelCaptor =
          ArgumentCaptor.forClass(AdminPhotoListGetModel.class);
      doReturn(AdminPhotoPageModel.of(adminPhotoModelList, true))
          .when(photoService)
          .getPhotoListForAdmin(modelCaptor.capture());

      mockMvc
          .perform(
              get("/api/v1/admin/photos")
                  .param("accountId", "aaaaaaaa")
                  .param("isHiddenByAdmin", "true")
                  .param("pageNo", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.photoList[0].accountNo").value(1))
          .andExpect(jsonPath("$.photoList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.photoList[0].accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.photoList[0].photoNo").value(3))
          .andExpect(jsonPath("$.photoList[0].photoJapaneseTitle").value("タイトル"))
          .andExpect(
              jsonPath("$.photoList[0].imageFilePath").value("https://signed.example/1/3-abc.jpg"))
          .andExpect(jsonPath("$.photoList[0].isHiddenByAdmin").value(true));

      assertEquals("aaaaaaaa", modelCaptor.getValue().getAccountId().value());
      assertTrue(modelCaptor.getValue().getIsHiddenByAdmin().value());
      assertEquals(2, modelCaptor.getValue().getPageNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ページ番号が正の数でない場合、400エラーになること")
    void getAdminPhotoList_BadRequestException_pageNo_not_positive() throws Exception {
      mockMvc
          .perform(get("/api/v1/admin/photos").param("pageNo", "0"))
          .andExpect(status().isBadRequest());

      verify(photoService, never()).getPhotoListForAdmin(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：アカウントIDの形式が不正な場合、400エラーになること")
    void getAdminPhotoList_BadRequestException_invalid_accountId() throws Exception {
      mockMvc
          .perform(get("/api/v1/admin/photos").param("accountId", "abc"))
          .andExpect(status().isBadRequest());

      verify(photoService, never()).getPhotoListForAdmin(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updatePhotoVisibility {
    @Test
    @Order(1)
    @DisplayName("正常系：写真を非公開にできること。操作した管理者はセッションのアカウント番号とすること")
    void updatePhotoVisibility_hide() throws Exception {
      ArgumentCaptor<PhotoVisibilityChangeModel> modelCaptor =
          ArgumentCaptor.forClass(PhotoVisibilityChangeModel.class);
      doNothing().when(photoService).changePhotoVisibilityByAdmin(modelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/3/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_hide_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("写真を非公開にしました。"));

      PhotoVisibilityChangeModel model = modelCaptor.getValue();
      assertEquals(new AccountNo(1L), model.getPhotoAccountNo());
      assertEquals(new PhotoNo(3L), model.getPhotoNo());
      assertEquals(new AccountNo(99L), model.getAdminAccountNo());
      assertTrue(model.getIsHiddenByAdmin().value());
      assertEquals("お問い合わせで不適切な画像との報告があったため", model.getReason().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真の非公開を解除できること")
    void updatePhotoVisibility_unhide() throws Exception {
      ArgumentCaptor<PhotoVisibilityChangeModel> modelCaptor =
          ArgumentCaptor.forClass(PhotoVisibilityChangeModel.class);
      doNothing().when(photoService).changePhotoVisibilityByAdmin(modelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/3/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_unhide_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("写真の非公開を解除しました。"));

      assertFalse(modelCaptor.getValue().getIsHiddenByAdmin().value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：理由が空の場合、400エラーになること")
    void updatePhotoVisibility_BadRequestException_blank_reason() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/3/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_badrequest_blank_reason.json")))
          .andExpect(status().isBadRequest());

      verify(photoService, never()).changePhotoVisibilityByAdmin(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：理由が500文字を超える場合、400エラーになること")
    void updatePhotoVisibility_BadRequestException_too_long_reason() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/3/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_badrequest_too_long_reason.json")))
          .andExpect(status().isBadRequest());

      verify(photoService, never()).changePhotoVisibilityByAdmin(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：非公開にするかどうかが未指定の場合、400エラーになること")
    void updatePhotoVisibility_BadRequestException_null_isHiddenByAdmin() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/3/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_badrequest_null_isHiddenByAdmin.json")))
          .andExpect(status().isBadRequest());

      verify(photoService, never()).changePhotoVisibilityByAdmin(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：写真が存在しない場合、404エラーになること")
    void updatePhotoVisibility_PhotoNotFoundException() throws Exception {
      doThrow(PhotoNotFoundException.class).when(photoService).changePhotoVisibilityByAdmin(any());

      mockMvc
          .perform(
              put("/api/v1/admin/photos/1/999/visibility")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("visibility_hide_success.json")))
          .andExpect(status().isNotFound());
    }
  }
}
