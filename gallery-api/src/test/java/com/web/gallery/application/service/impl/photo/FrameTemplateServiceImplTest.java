package com.web.gallery.application.service.impl.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.BadRequestException;
import com.web.gallery.domain.exception.FrameTemplateNotFoundException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.domain.service.FrameDefinitionPolicy;
import com.web.gallery.infrastructure.persistence.repository.photo.FrameTemplateMstRepositoryImpl;
import java.util.List;
import java.util.Map;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FrameTemplateServiceImplTest {

  @InjectMocks private FrameTemplateServiceImpl frameTemplateService;

  @Mock private FrameTemplateMstRepositoryImpl frameTemplateMstRepository;

  @Mock private FrameDefinitionPolicy frameDefinitionPolicy;

  private static final AccountNo ACCOUNT_NO = new AccountNo(1L);

  private static final FrameDefinition DEFINITION =
      new FrameDefinition(
          Map.of(
              "size", Map.of("width", 364, "height", 257),
              "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40))));

  /** 登録・更新リクエストの額縁テンプレート（額縁テンプレート番号は更新時のみ） */
  private static FrameTemplateModel request(FrameTemplateNo frameTemplateNo) {
    return FrameTemplateModel.builder()
        .accountNo(ACCOUNT_NO)
        .frameTemplateNo(frameTemplateNo)
        .templateName(new FrameTemplateName("四つ切 3枚組"))
        .frameDefinition(DEFINITION)
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：Repositoryから取得した一覧をそのまま返すこと")
    void getFrameTemplateList_success() {
      FrameTemplateModelList expected =
          FrameTemplateModelList.of(List.of(request(new FrameTemplateNo(1L))));
      doReturn(expected).when(frameTemplateMstRepository).getFrameTemplateList(ACCOUNT_NO);

      assertSame(expected, frameTemplateService.getFrameTemplateList(ACCOUNT_NO));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：Repositoryから取得した額縁テンプレートをそのまま返すこと")
    void getFrameTemplate_success() throws Exception {
      FrameTemplateModel expected = request(new FrameTemplateNo(2L));
      doReturn(expected)
          .when(frameTemplateMstRepository)
          .getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));

      assertSame(
          expected, frameTemplateService.getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、FrameTemplateNotFoundExceptionをスローすること")
    void getFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateMstRepository)
          .getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));

      assertThrows(
          FrameTemplateNotFoundException.class,
          () -> frameTemplateService.getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L)));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：採番した額縁テンプレート番号を設定して登録し、その番号を返すこと")
    void registFrameTemplate_success() throws Exception {
      doReturn(3).when(frameTemplateMstRepository).count(ACCOUNT_NO);
      doReturn(true).when(frameDefinitionPolicy).isValid(DEFINITION);
      doReturn(new FrameTemplateNo(4L))
          .when(frameTemplateMstRepository)
          .getNewFrameTemplateNo(ACCOUNT_NO);

      FrameTemplateNo actual = frameTemplateService.registFrameTemplate(request(null));

      assertEquals(new FrameTemplateNo(4L), actual);
      ArgumentCaptor<FrameTemplateModel> captor = ArgumentCaptor.forClass(FrameTemplateModel.class);
      verify(frameTemplateMstRepository).regist(captor.capture());
      assertEquals(new FrameTemplateNo(4L), captor.getValue().getFrameTemplateNo());
      assertEquals(ACCOUNT_NO, captor.getValue().getAccountNo());
      assertEquals(new FrameTemplateName("四つ切 3枚組"), captor.getValue().getTemplateName());
      assertEquals(DEFINITION, captor.getValue().getFrameDefinition());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：作成数が上限の1つ手前（49件）の場合、登録できること")
    void registFrameTemplate_belowLimit() throws Exception {
      doReturn(49).when(frameTemplateMstRepository).count(ACCOUNT_NO);
      doReturn(true).when(frameDefinitionPolicy).isValid(DEFINITION);
      doReturn(new FrameTemplateNo(50L))
          .when(frameTemplateMstRepository)
          .getNewFrameTemplateNo(ACCOUNT_NO);

      assertEquals(
          new FrameTemplateNo(50L), frameTemplateService.registFrameTemplate(request(null)));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：作成数が上限（50件）に達している場合、BadRequestExceptionをスローし登録しないこと")
    void registFrameTemplate_reachedLimit() throws Exception {
      doReturn(50).when(frameTemplateMstRepository).count(ACCOUNT_NO);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> frameTemplateService.registFrameTemplate(request(null)));
      assertEquals(ErrorEnum.REACHED_FRAME_TEMPLATE_LIMIT.getErrorCode(), exception.getErrorCode());
      verifyNoInteractions(frameDefinitionPolicy);
      verify(frameTemplateMstRepository, never()).regist(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：額縁定義が不正な場合、BadRequestExceptionをスローし登録しないこと")
    void registFrameTemplate_invalidDefinition() throws Exception {
      doReturn(0).when(frameTemplateMstRepository).count(ACCOUNT_NO);
      doReturn(false).when(frameDefinitionPolicy).isValid(DEFINITION);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> frameTemplateService.registFrameTemplate(request(null)));
      assertEquals(ErrorEnum.INVALID_FRAME_DEFINITION.getErrorCode(), exception.getErrorCode());
      verify(frameTemplateMstRepository, never()).getNewFrameTemplateNo(any());
      verify(frameTemplateMstRepository, never()).regist(any());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：存在を確認したうえで、リクエストの内容で更新すること")
    void updateFrameTemplate_success() throws Exception {
      FrameTemplateModel requestModel = request(new FrameTemplateNo(2L));
      doReturn(requestModel)
          .when(frameTemplateMstRepository)
          .getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));
      doReturn(true).when(frameDefinitionPolicy).isValid(DEFINITION);

      frameTemplateService.updateFrameTemplate(requestModel);

      verify(frameTemplateMstRepository).update(requestModel);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、FrameTemplateNotFoundExceptionをスローし更新しないこと")
    void updateFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateMstRepository)
          .getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));

      assertThrows(
          FrameTemplateNotFoundException.class,
          () -> frameTemplateService.updateFrameTemplate(request(new FrameTemplateNo(2L))));
      verify(frameTemplateMstRepository, never()).update(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：額縁定義が不正な場合、BadRequestExceptionをスローし更新しないこと")
    void updateFrameTemplate_invalidDefinition() throws Exception {
      FrameTemplateModel requestModel = request(new FrameTemplateNo(2L));
      doReturn(requestModel)
          .when(frameTemplateMstRepository)
          .getFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));
      doReturn(false).when(frameDefinitionPolicy).isValid(DEFINITION);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> frameTemplateService.updateFrameTemplate(requestModel));
      assertEquals(ErrorEnum.INVALID_FRAME_DEFINITION.getErrorCode(), exception.getErrorCode());
      verify(frameTemplateMstRepository, never()).update(any());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号を指定して削除すること")
    void deleteFrameTemplate_success() throws Exception {
      frameTemplateService.deleteFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L));

      verify(frameTemplateMstRepository).delete(ACCOUNT_NO, new FrameTemplateNo(2L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：額縁テンプレートが存在しない場合、FrameTemplateNotFoundExceptionをスローすること")
    void deleteFrameTemplate_notFound() throws Exception {
      doThrow(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.toException())
          .when(frameTemplateMstRepository)
          .delete(ACCOUNT_NO, new FrameTemplateNo(2L));

      assertThrows(
          FrameTemplateNotFoundException.class,
          () -> frameTemplateService.deleteFrameTemplate(ACCOUNT_NO, new FrameTemplateNo(2L)));
    }
  }
}
