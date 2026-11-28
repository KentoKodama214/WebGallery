package com.web.gallery.infrastructure.persistence.repository.photo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.domain.exception.FrameTemplateNotFoundException;
import com.web.gallery.domain.exception.RegistFailureException;
import com.web.gallery.domain.exception.UpdateFailureException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMst;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstUpdateTarget;
import com.web.gallery.infrastructure.persistence.mapper.photo.FrameTemplateMstMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FrameTemplateMstRepositoryImplTest {

  @InjectMocks private FrameTemplateMstRepositoryImpl repository;

  @Mock private FrameTemplateMstMapper frameTemplateMstMapper;

  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  private static final OffsetDateTime UPDATED_AT =
      OffsetDateTime.of(2001, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  private static final Map<String, Object> DEFINITION =
      Map.of(
          "size", Map.of("width", 364, "height", 257),
          "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40)));

  private static FrameTemplateMst entity(Long frameTemplateNo, Map<String, Object> definition) {
    return FrameTemplateMst.builder()
        .id(10L)
        .accountNo(1L)
        .frameTemplateNo(frameTemplateNo)
        .createdBy(1L)
        .createdAt(CREATED_AT)
        .updatedBy(1L)
        .updatedAt(UPDATED_AT)
        .templateName("額縁" + frameTemplateNo)
        .frameDefinition(definition)
        .build();
  }

  private static FrameTemplateModel model() {
    return FrameTemplateModel.builder()
        .accountNo(new AccountNo(1L))
        .frameTemplateNo(new FrameTemplateNo(2L))
        .templateName(new FrameTemplateName("四つ切 3枚組"))
        .frameDefinition(new FrameDefinition(DEFINITION))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNewFrameTemplateNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大値+1を採番すること")
    void getNewFrameTemplateNo_existing() {
      doReturn(3L).when(frameTemplateMstMapper).getMaxFrameTemplateNo(1L);

      assertEquals(new FrameTemplateNo(4L), repository.getNewFrameTemplateNo(new AccountNo(1L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未登録の場合、1を採番すること")
    void getNewFrameTemplateNo_none() {
      doReturn(null).when(frameTemplateMstMapper).getMaxFrameTemplateNo(1L);

      assertEquals(new FrameTemplateNo(1L), repository.getNewFrameTemplateNo(new AccountNo(1L)));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class count {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号で絞り込んだ件数を返すこと")
    void count_success() {
      doReturn(5).when(frameTemplateMstMapper).count(any(FrameTemplateMstCondition.class));

      assertEquals(5, repository.count(new AccountNo(1L)));

      ArgumentCaptor<FrameTemplateMstCondition> captor =
          ArgumentCaptor.forClass(FrameTemplateMstCondition.class);
      verify(frameTemplateMstMapper).count(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertNull(captor.getValue().getFrameTemplateNo());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：取得したEntityの全項目をModelに変換して、取得順に返すこと")
    void getFrameTemplateList_success() {
      doReturn(List.of(entity(1L, DEFINITION), entity(2L, DEFINITION)))
          .when(frameTemplateMstMapper)
          .select(any(FrameTemplateMstCondition.class));

      FrameTemplateModelList actual = repository.getFrameTemplateList(new AccountNo(1L));

      assertEquals(2, actual.size());
      FrameTemplateModel first = actual.get(0);
      assertEquals(new AccountNo(1L), first.getAccountNo());
      assertEquals(new FrameTemplateNo(1L), first.getFrameTemplateNo());
      assertEquals(new FrameTemplateName("額縁1"), first.getTemplateName());
      assertEquals(new FrameDefinition(DEFINITION), first.getFrameDefinition());
      assertEquals(CREATED_AT, first.getCreatedAt());
      assertEquals(UPDATED_AT, first.getUpdatedAt());
      assertEquals(new FrameTemplateNo(2L), actual.get(1).getFrameTemplateNo());

      ArgumentCaptor<FrameTemplateMstCondition> captor =
          ArgumentCaptor.forClass(FrameTemplateMstCondition.class);
      verify(frameTemplateMstMapper).select(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertNull(captor.getValue().getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：額縁定義がnullの場合、項目なしの額縁定義に変換すること")
    void getFrameTemplateList_nullDefinition() {
      doReturn(List.of(entity(1L, null)))
          .when(frameTemplateMstMapper)
          .select(any(FrameTemplateMstCondition.class));

      FrameTemplateModelList actual = repository.getFrameTemplateList(new AccountNo(1L));

      assertEquals(new FrameDefinition(Map.of()), actual.get(0).getFrameDefinition());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：該当する額縁テンプレートがない場合、空の一覧を返すこと")
    void getFrameTemplateList_empty() {
      doReturn(List.of()).when(frameTemplateMstMapper).select(any(FrameTemplateMstCondition.class));

      assertTrue(repository.getFrameTemplateList(new AccountNo(1L)).isEmpty());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号で取得したEntityをModelに変換して返すこと")
    void getFrameTemplate_success() throws Exception {
      doReturn(List.of(entity(2L, DEFINITION)))
          .when(frameTemplateMstMapper)
          .select(any(FrameTemplateMstCondition.class));

      FrameTemplateModel actual =
          repository.getFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L));

      assertEquals(new FrameTemplateNo(2L), actual.getFrameTemplateNo());
      assertEquals(new FrameTemplateName("額縁2"), actual.getTemplateName());

      ArgumentCaptor<FrameTemplateMstCondition> captor =
          ArgumentCaptor.forClass(FrameTemplateMstCondition.class);
      verify(frameTemplateMstMapper).select(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(2L, captor.getValue().getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：該当する額縁テンプレートがない場合、FrameTemplateNotFoundExceptionをスローすること")
    void getFrameTemplate_notFound() {
      doReturn(List.of()).when(frameTemplateMstMapper).select(any(FrameTemplateMstCondition.class));

      assertThrows(
          FrameTemplateNotFoundException.class,
          () -> repository.getFrameTemplate(new AccountNo(1L), new FrameTemplateNo(2L)));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：Modelの全項目をEntityに変換して登録すること")
    void regist_success() throws Exception {
      doReturn(1).when(frameTemplateMstMapper).insert(any(FrameTemplateMst.class));

      repository.regist(model());

      ArgumentCaptor<FrameTemplateMst> captor = ArgumentCaptor.forClass(FrameTemplateMst.class);
      verify(frameTemplateMstMapper).insert(captor.capture());
      FrameTemplateMst actual = captor.getValue();
      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getFrameTemplateNo());
      assertEquals(1L, actual.getCreatedBy());
      assertEquals(1L, actual.getUpdatedBy());
      assertEquals("四つ切 3枚組", actual.getTemplateName());
      assertEquals(DEFINITION, actual.getFrameDefinition());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録件数が0件の場合、RegistFailureExceptionをスローすること")
    void regist_zero() {
      doReturn(0).when(frameTemplateMstMapper).insert(any(FrameTemplateMst.class));

      assertThrows(RegistFailureException.class, () -> repository.regist(model()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：額縁テンプレート番号が重複した場合、RegistFailureExceptionをスローすること")
    void regist_duplicateKey() {
      doThrow(new DuplicateKeyException("duplicate"))
          .when(frameTemplateMstMapper)
          .insert(any(FrameTemplateMst.class));

      assertThrows(RegistFailureException.class, () -> repository.regist(model()));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号を条件に、テンプレート名・額縁定義を更新すること")
    void update_success() throws Exception {
      doReturn(1)
          .when(frameTemplateMstMapper)
          .update(any(FrameTemplateMstCondition.class), any(FrameTemplateMstUpdateTarget.class));

      repository.update(model());

      ArgumentCaptor<FrameTemplateMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(FrameTemplateMstCondition.class);
      ArgumentCaptor<FrameTemplateMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(FrameTemplateMstUpdateTarget.class);
      verify(frameTemplateMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(2L, conditionCaptor.getValue().getFrameTemplateNo());
      assertEquals(1L, targetCaptor.getValue().getUpdatedBy());
      assertEquals("四つ切 3枚組", targetCaptor.getValue().getTemplateName());
      assertEquals(DEFINITION, targetCaptor.getValue().getFrameDefinition());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：更新件数が0件の場合、UpdateFailureExceptionをスローすること")
    void update_zero() {
      doReturn(0)
          .when(frameTemplateMstMapper)
          .update(any(FrameTemplateMstCondition.class), any(FrameTemplateMstUpdateTarget.class));

      assertThrows(UpdateFailureException.class, () -> repository.update(model()));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号を条件に削除すること")
    void delete_success() throws Exception {
      doReturn(1).when(frameTemplateMstMapper).delete(any(FrameTemplateMstCondition.class));

      repository.delete(new AccountNo(1L), new FrameTemplateNo(2L));

      ArgumentCaptor<FrameTemplateMstCondition> captor =
          ArgumentCaptor.forClass(FrameTemplateMstCondition.class);
      verify(frameTemplateMstMapper).delete(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(2L, captor.getValue().getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：削除件数が0件の場合、FrameTemplateNotFoundExceptionをスローすること")
    void delete_notFound() {
      doReturn(0).when(frameTemplateMstMapper).delete(any(FrameTemplateMstCondition.class));

      assertThrows(
          FrameTemplateNotFoundException.class,
          () -> repository.delete(new AccountNo(1L), new FrameTemplateNo(2L)));
    }
  }
}
