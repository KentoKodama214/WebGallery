package com.web.gallery.presentation.controller.photo.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.Password;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
// S3ストレージアクセスはモックする（統合テストでは実ストレージへ接続しない）。
// Controller層の統合テスト全クラスで同一の宣言に揃え、Springのテストコンテキストを共有する
@MockitoBean(types = FileRepository.class)
public class FrameTemplateControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final String FRAME_TEMPLATES = "/api/v1/accounts/aaaaaaaa/frame-templates";

  private Authentication createAuthentication(Long accountNo, String accountId) {
    AccountModel sessionAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(accountNo))
            .accountId(new AccountId(accountId))
            .accountName(new AccountName(accountId.toUpperCase()))
            .password(new Password("$2a$10$password1"))
            .authorityKbn(AuthorityEnum.NORMAL)
            .build();
    AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
    return new UsernamePasswordAuthenticationToken(
        accountPrincipal, null, accountPrincipal.getAuthorities());
  }

  private Authentication owner() {
    return createAuthentication(1L, "aaaaaaaa");
  }

  private Authentication other() {
    return createAuthentication(2L, "bbbbbbbb");
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource(
                "json/controller/integration/FrameTemplateControllerIntegrationTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  private Integer countOf(Long accountNo) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM photo.frame_template_mst WHERE account_no = ?",
        Integer.class,
        accountNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/FrameTemplateControllerIntegrationTest.sql")
  class getFrameTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレートだけを、額縁テンプレート番号の昇順に返す")
    void getFrameTemplateList_success() throws Exception {
      mockMvc
          .perform(
              get(FRAME_TEMPLATES)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.frameTemplateList.length()").value(2))
          .andExpect(jsonPath("$.frameTemplateList[0].frameTemplateNo").value(1))
          .andExpect(jsonPath("$.frameTemplateList[0].templateName").value("横長 2枚組"))
          .andExpect(jsonPath("$.frameTemplateList[0].frameDefinition.slots.length()").value(2))
          .andExpect(
              jsonPath("$.frameTemplateList[0].frameDefinition.texts[0].fontFamily")
                  .value("mincho"))
          .andExpect(jsonPath("$.frameTemplateList[1].frameTemplateNo").value(2));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未認証の場合、401を返す")
    void getFrameTemplateList_unauthenticated() throws Exception {
      mockMvc.perform(get(FRAME_TEMPLATES)).andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返す")
    void getFrameTemplateList_forbidden() throws Exception {
      mockMvc
          .perform(
              get(FRAME_TEMPLATES)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(other())))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_FRAME_TEMPLATE.getErrorCode()));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/FrameTemplateControllerIntegrationTest.sql")
  class getFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレートを返す")
    void getFrameTemplate_success() throws Exception {
      mockMvc
          .perform(
              get(FRAME_TEMPLATES + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.frameTemplateNo").value(1))
          .andExpect(jsonPath("$.templateName").value("横長 2枚組"))
          .andExpect(jsonPath("$.frameDefinition.background.color").value("#f5f0e6"))
          .andExpect(jsonPath("$.frameDefinition.slots[1].rotation").value(3));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：存在しない額縁テンプレート番号を指定した場合、404を返す")
    void getFrameTemplate_notFound() throws Exception {
      mockMvc
          .perform(
              get(FRAME_TEMPLATES + "/3")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.getErrorMessage()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/FrameTemplateControllerIntegrationTest.sql")
  class registFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大値+1の額縁テンプレート番号で登録する")
    void registFrameTemplate_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("額縁テンプレートを登録しました。"))
          .andExpect(jsonPath("$.frameTemplateNo").value(3));

      Map<String, Object> row =
          jdbcTemplate.queryForMap(
              "SELECT template_name, created_by, updated_by,"
                  + " jsonb_array_length(frame_definition -> 'slots') AS slot_count,"
                  + " frame_definition -> 'texts' -> 0 ->> 'fontFamily' AS font_family"
                  + " FROM photo.frame_template_mst WHERE account_no = 1 AND frame_template_no = 3");
      assertEquals("四つ切 3枚組", row.get("template_name"));
      assertEquals(1L, row.get("created_by"));
      assertEquals(1L, row.get("updated_by"));
      assertEquals(3, row.get("slot_count"));
      assertEquals("mincho", row.get("font_family"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM photo.frame_template_mst"
                  + " WHERE account_no = 1 AND frame_template_no = 3",
              OffsetDateTime.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：額縁定義が不正（写真枠がない）な場合、400を返し登録しない")
    void registFrameTemplate_invalidDefinition() throws Exception {
      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_invalid_definition.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.INVALID_FRAME_DEFINITION.getErrorMessage()));

      assertEquals(2, countOf(1L));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：作成数が上限（50件）に達している場合、400を返し登録しない")
    void registFrameTemplate_reachedLimit() throws Exception {
      jdbcTemplate.update(
          "INSERT INTO photo.frame_template_mst (account_no, frame_template_no, created_by,"
              + " created_at, updated_by, updated_at, template_name, frame_definition)"
              + " SELECT 1, n, 1, NOW(), 1, NOW(), '額縁' || n, '{}'::jsonb"
              + " FROM generate_series(3, 50) AS n");

      mockMvc
          .perform(
              post(FRAME_TEMPLATES)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.REACHED_FRAME_TEMPLATE_LIMIT.getErrorMessage()));

      assertEquals(50, countOf(1L));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/FrameTemplateControllerIntegrationTest.sql")
  class updateFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレートのテンプレート名・額縁定義を更新し、他の額縁テンプレートは変更しない")
    void updateFrameTemplate_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("額縁テンプレートを更新しました。"))
          .andExpect(jsonPath("$.frameTemplateNo").value(2));

      Map<String, Object> row =
          jdbcTemplate.queryForMap(
              "SELECT template_name, jsonb_array_length(frame_definition -> 'slots') AS slot_count"
                  + " FROM photo.frame_template_mst WHERE account_no = 1 AND frame_template_no = 2");
      assertEquals("四つ切 3枚組（改）", row.get("template_name"));
      assertEquals(3, row.get("slot_count"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM photo.frame_template_mst"
                  + " WHERE account_no = 1 AND frame_template_no = 2",
              OffsetDateTime.class));
      assertEquals(
          "横長 2枚組",
          jdbcTemplate.queryForObject(
              "SELECT template_name FROM photo.frame_template_mst"
                  + " WHERE account_no = 1 AND frame_template_no = 1",
              String.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：存在しない額縁テンプレート番号を指定した場合、404を返す")
    void updateFrameTemplate_notFound() throws Exception {
      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/3")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_success.json")))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.getErrorCode()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：額縁定義が不正な場合、400を返し更新しない")
    void updateFrameTemplate_invalidDefinition() throws Exception {
      mockMvc
          .perform(
              put(FRAME_TEMPLATES + "/2")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_invalid_definition.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.INVALID_FRAME_DEFINITION.getErrorMessage()));

      assertEquals(
          "四つ切 1枚",
          jdbcTemplate.queryForObject(
              "SELECT template_name FROM photo.frame_template_mst"
                  + " WHERE account_no = 1 AND frame_template_no = 2",
              String.class));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/FrameTemplateControllerIntegrationTest.sql")
  class deleteFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：本人の額縁テンプレートだけを物理削除する")
    void deleteFrameTemplate_success() throws Exception {
      mockMvc
          .perform(
              delete(FRAME_TEMPLATES + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("額縁テンプレートを削除しました。"));

      assertEquals(1, countOf(1L));
      assertEquals(1, countOf(2L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他人の額縁テンプレートと同じ番号でも、自分のものでなければ404を返し削除しない")
    void deleteFrameTemplate_notFound() throws Exception {
      mockMvc
          .perform(
              delete(FRAME_TEMPLATES + "/3")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf()))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.FRAME_TEMPLATE_NOT_FOUND.getErrorCode()));

      assertEquals(2, countOf(1L));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返し削除しない")
    void deleteFrameTemplate_forbidden() throws Exception {
      mockMvc
          .perform(
              delete(FRAME_TEMPLATES + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(other()))
                  .with(csrf()))
          .andExpect(status().isForbidden());

      assertEquals(2, countOf(1L));
    }
  }
}
