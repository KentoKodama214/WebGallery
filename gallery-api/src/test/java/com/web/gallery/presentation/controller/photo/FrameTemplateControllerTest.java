package com.web.gallery.presentation.controller.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.application.service.photo.FrameTemplateService;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
import com.web.gallery.presentation.converter.photo.FrameTemplateConverter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FrameTemplateControllerTest {

  @InjectMocks private FrameTemplateController frameTemplateController;

  @Mock private FrameTemplateService frameTemplateService;

  @Mock private SessionHelper sessionHelper;

  @Spy private FrameTemplateConverter frameTemplateConverter = new FrameTemplateConverter();

  private MockMvc mockMvc;

  private static final String FRAME_TEMPLATES = "/api/v1/accounts/aaaaaaaa/frame-templates";

  private static final String OTHER_FRAME_TEMPLATES = "/api/v1/accounts/bbbbbbbb/frame-templates";

  private static final Map<String, Object> DEFINITION =
      Map.of(
          "size", Map.of("width", 364, "height", 257),
          "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40)));

  @BeforeEach
  void setUp() {
    lenient().doReturn(1L).when(sessionHelper).getAccountNo();
    lenient().doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
    JsonMapper jsonMapper = JsonMapper.builder().build();

    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(frameTemplateController)
            .setMessageConverters(converter)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/FrameTemplateControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  private static FrameTemplateModel model(Long frameTemplateNo) {
    return FrameTemplateModel.builder()
        .accountNo(new AccountNo(1L))
        .frameTemplateNo(new FrameTemplateNo(frameTemplateNo))
        .templateName(new FrameTemplateName("額縁" + frameTemplateNo))
        .frameDefinition(new FrameDefinition(DEFINITION))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレート一覧を返すこと")
    void getFrameTemplateList_success() throws Exception {
      doReturn(FrameTemplateModelList.of(List.of(model(1L), model(2L))))
          .when(frameTemplateService)
          .getFrameTemplateList(new AccountNo(1L));

      mockMvc
          .perform(get(FRAME_TEMPLATES))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.frameTemplateList.length()").value(2))
          .andExpect(jsonPath("$.frameTemplateList[0].frameTemplateNo").value(1))
          .andExpect(jsonPath("$.frameTemplateList[0].templateName").value("額縁1"))
          .andExpect(jsonPath("$.frameTemplateList[0].frameDefinition.size.width").value(364))
          .andExpect(jsonPath("$.frameTemplateList[1].frameTemplateNo").value(2));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返すこと")
    void getFrameTemplateList_forbidden() throws Exception {
      mockMvc
          .perform(get(OTHER_FRAME_TEMPLATES))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_FRAME_TEMPLATE.getErrorCode()));

      verifyNoInteractions(frameTemplateService);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレートを返すこと")
    void getFrameTemplate_success() throws Exception {
      doReturn(model(2L))
          .when(frameTemplateService)
          .getFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L));

      mockMvc
          .perform(get(FRAME_TEMPLATES + "/2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.frameTemplateNo").value(2))
          .andExpect(jsonPath("$.templateName").value("額縁2"))
          .andExpect(jsonPath("$.frameDefinition.slots[0].width").value(30));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：額縁テンプレート番号が0以下の場合、400を返すこと")
    void getFrameTemplate_invalidNo() throws Exception {
      mockMvc
          .perform(get(FRAME_TEMPLATES + "/0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      verifyNoInteractions(frameTemplateService);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返すこと")
    void getFrameTemplate_forbidden() throws Exception {
      mockMvc.perform(get(OTHER_FRAME_TEMPLATES + "/2")).andExpect(status().isForbidden());

      verifyNoInteractions(frameTemplateService);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、404を返すこと")
    void getFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateService)
          .getFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L));

      mockMvc
          .perform(get(FRAME_TEMPLATES + "/2"))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.getErrorCode()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：セッションのアカウント番号で登録し、採番した額縁テンプレート番号を返すこと")
    void registFrameTemplate_success() throws Exception {
      doReturn(new FrameTemplateNo(3L))
          .when(frameTemplateService)
          .registFrameTemplate(any(FrameTemplateModel.class));

      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("額縁テンプレートを登録しました。"))
          .andExpect(jsonPath("$.frameTemplateNo").value(3));

      ArgumentCaptor<FrameTemplateModel> captor = ArgumentCaptor.forClass(FrameTemplateModel.class);
      verify(frameTemplateService).registFrameTemplate(captor.capture());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
      assertEquals(new FrameTemplateName("四つ切 3枚組"), captor.getValue().getTemplateName());
      assertEquals(
          3, ((List<?>) captor.getValue().getFrameDefinition().value().get("slots")).size());
      assertNull(captor.getValue().getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テンプレート名が空白のみの場合、400を返すこと")
    void registFrameTemplate_blankName() throws Exception {
      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_blank_name.json")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      verify(frameTemplateService, times(0)).registFrameTemplate(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：テンプレート名が上限超過（51文字）の場合、400を返すこと")
    void registFrameTemplate_tooLongName() throws Exception {
      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_too_long_name.json")))
          .andExpect(status().isBadRequest());

      verify(frameTemplateService, times(0)).registFrameTemplate(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：額縁定義が未指定の場合、400を返すこと")
    void registFrameTemplate_missingDefinition() throws Exception {
      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_missing_definition.json")))
          .andExpect(status().isBadRequest());

      verify(frameTemplateService, times(0)).registFrameTemplate(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返すこと")
    void registFrameTemplate_forbidden() throws Exception {
      mockMvc
          .perform(
              post(OTHER_FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isForbidden());

      verify(frameTemplateService, times(0)).registFrameTemplate(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：額縁定義が不正な場合、400を返すこと")
    void registFrameTemplate_invalidDefinition() throws Exception {
      doThrow(ErrorEnum.INVALID_FRAME_DEFINITION.toException())
          .when(frameTemplateService)
          .registFrameTemplate(any(FrameTemplateModel.class));

      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.INVALID_FRAME_DEFINITION.getErrorMessage()));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：登録に失敗した場合、409を返すこと")
    void registFrameTemplate_registFailure() throws Exception {
      doThrow(ErrorEnum.FAIL_TO_REGIST_FRAME_TEMPLATE.toException())
          .when(frameTemplateService)
          .registFrameTemplate(any(FrameTemplateModel.class));

      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.FAIL_TO_REGIST_FRAME_TEMPLATE.getErrorCode()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：パスパラメータの額縁テンプレート番号を指定して更新すること")
    void updateFrameTemplate_success() throws Exception {
      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("額縁テンプレートを更新しました。"))
          .andExpect(jsonPath("$.frameTemplateNo").value(2));

      ArgumentCaptor<FrameTemplateModel> captor = ArgumentCaptor.forClass(FrameTemplateModel.class);
      verify(frameTemplateService).updateFrameTemplate(captor.capture());
      assertEquals(new FrameTemplateNo(2L), captor.getValue().getFrameTemplateNo());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テンプレート名が空白のみの場合、400を返すこと")
    void updateFrameTemplate_blankName() throws Exception {
      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_badrequest_blank_name.json")))
          .andExpect(status().isBadRequest());

      verify(frameTemplateService, times(0)).updateFrameTemplate(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返すこと")
    void updateFrameTemplate_forbidden() throws Exception {
      mockMvc
          .perform(
              put(OTHER_FRAME_TEMPLATES + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isForbidden());

      verify(frameTemplateService, times(0)).updateFrameTemplate(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、404を返すこと")
    void updateFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateService)
          .updateFrameTemplate(any(FrameTemplateModel.class));

      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：更新に失敗した場合、409を返すこと")
    void updateFrameTemplate_updateFailure() throws Exception {
      doThrow(ErrorEnum.FAIL_TO_UPDATE_FRAME_TEMPLATE.toException())
          .when(frameTemplateService)
          .updateFrameTemplate(any(FrameTemplateModel.class));

      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("save_success.json")))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.FAIL_TO_UPDATE_FRAME_TEMPLATE.getErrorCode()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：セッションのアカウント番号・パスパラメータの額縁テンプレート番号を指定して削除すること")
    void deleteFrameTemplate_success() throws Exception {
      mockMvc
          .perform(delete(FRAME_TEMPLATES + "/2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("額縁テンプレートを削除しました。"))
          .andExpect(jsonPath("$.frameTemplateNo").value(2));

      verify(frameTemplateService).deleteFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返すこと")
    void deleteFrameTemplate_forbidden() throws Exception {
      mockMvc.perform(delete(OTHER_FRAME_TEMPLATES + "/2")).andExpect(status().isForbidden());

      verifyNoInteractions(frameTemplateService);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、404を返すこと")
    void deleteFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateService)
          .deleteFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L));

      mockMvc.perform(delete(FRAME_TEMPLATES + "/2")).andExpect(status().isNotFound());
    }
  }
}
