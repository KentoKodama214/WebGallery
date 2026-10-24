package com.web.gallery.presentation.controller.photo.integration;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.Password;
import com.web.gallery.domain.model.photo.ImageFilePath;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
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
import org.springframework.http.HttpStatus;
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
public class PortfolioControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private FileRepository fileRepository;

  private static final String PORTFOLIOS = "/api/v1/accounts/aaaaaaaa/portfolios";

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

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource(
                "json/controller/integration/PortfolioControllerIntegrationTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  private String statusOf(Long accountNo, Long portfolioNo) {
    return jdbcTemplate.queryForObject(
        "SELECT status_kbn::text FROM photo.portfolio_mst WHERE account_no = ? AND portfolio_no = ?",
        String.class,
        accountNo,
        portfolioNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class getPortfolioList {
    @Test
    @Order(1)
    @DisplayName("正常系：本人のポートフォリオ（下書きを含む）だけを、更新日時の新しい順に写真の枚数とあわせて取得できる")
    void getPortfolioList_success() throws Exception {
      mockMvc
          .perform(
              get(PORTFOLIOS).with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioList.length()").value(2))
          .andExpect(jsonPath("$.portfolioList[0].portfolioNo").value(2))
          .andExpect(jsonPath("$.portfolioList[0].theme").value("光と影"))
          .andExpect(jsonPath("$.portfolioList[0].templateKbn").value("grid"))
          .andExpect(jsonPath("$.portfolioList[0].statusKbn").value("draft"))
          .andExpect(jsonPath("$.portfolioList[0].photoCount").value(1))
          .andExpect(jsonPath("$.portfolioList[1].portfolioNo").value(1))
          .andExpect(jsonPath("$.portfolioList[1].statusKbn").value("published"))
          .andExpect(jsonPath("$.portfolioList[1].photoCount").value(2));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未認証の場合、401を返す")
    void getPortfolioList_unauthenticated() throws Exception {
      mockMvc
          .perform(get(PORTFOLIOS))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返す")
    void getPortfolioList_forbidden() throws Exception {
      mockMvc
          .perform(
              get(PORTFOLIOS)
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(2L, "bbbbbbbb"))))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PORTFOLIO.getErrorCode()));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class getPortfolioDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：写真（署名付きURL・写真タイトル・向き）・写真単位の設定・タグを表示順に取得できる")
    void getPortfolioDetail_success() throws Exception {
      doAnswer(
              invocation ->
                  new ImageFilePath(
                      "https://signed/" + ((ImageFilePath) invocation.getArgument(0)).value()))
          .when(fileRepository)
          .getPresignedUrl(any(ImageFilePath.class));

      mockMvc
          .perform(
              get(PORTFOLIOS + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioNo").value(1))
          .andExpect(jsonPath("$.theme").value("日本の自然"))
          .andExpect(jsonPath("$.description").value("日本各地の風景"))
          .andExpect(jsonPath("$.templateKbn").value("feature"))
          .andExpect(jsonPath("$.statusKbn").value("published"))
          .andExpect(jsonPath("$.photoList[0].photoNo").value(1))
          .andExpect(jsonPath("$.photoList[0].displayOrder").value(1))
          .andExpect(jsonPath("$.photoList[0].photoSettings.theme").value("日本の自然"))
          .andExpect(jsonPath("$.photoList[0].photoSettings.subTheme").value("京都"))
          .andExpect(jsonPath("$.photoList[0].photoJapaneseTitle").value("金閣寺"))
          .andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://signed/1/1-aaa.jpg"))
          .andExpect(jsonPath("$.photoList[0].directionKbn").value("horizontal"))
          .andExpect(jsonPath("$.photoList[1].photoNo").value(2))
          .andExpect(jsonPath("$.tagList[0].tagJapaneseName").value("自然"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：存在しないポートフォリオ番号を指定した場合、404を返す")
    void getPortfolioDetail_notFound() throws Exception {
      mockMvc
          .perform(
              get(PORTFOLIOS + "/9")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PORTFOLIO_NOT_FOUND.getErrorCode()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class registPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きとして登録し、写真の表示順・タグ番号がリクエストの並び順で採番される")
    void registPortfolio_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_feature_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value("ポートフォリオを登録しました。"))
          .andExpect(jsonPath("$.portfolioNo").value(3));

      Map<String, Object> mst =
          jdbcTemplate.queryForMap(
              "SELECT theme, description, template_kbn::text AS template_kbn,"
                  + " status_kbn::text AS status_kbn, created_by, updated_by"
                  + " FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 3");
      assertEquals("日本の動物・植物", mst.get("theme"));
      assertEquals("四季の動植物", mst.get("description"));
      assertEquals("feature", mst.get("template_kbn"));
      assertEquals("draft", mst.get("status_kbn"));
      assertEquals(1L, mst.get("created_by"));
      assertEquals(1L, mst.get("updated_by"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 3",
              OffsetDateTime.class));

      assertEquals(
          List.of(
              Map.of("photo_no", 2L, "display_order", 1),
              Map.of("photo_no", 1L, "display_order", 2)),
          jdbcTemplate.queryForList(
              "SELECT photo_no, display_order FROM photo.portfolio_photo"
                  + " WHERE account_no = 1 AND portfolio_no = 3 ORDER BY display_order"));
      assertTrue(
          jdbcTemplate.queryForObject(
              "SELECT photo_settings = '{\"theme\": \"茶畑\", \"subTheme\": \"静岡\", \"description\": \"一面の緑\"}'::jsonb"
                  + " FROM photo.portfolio_photo"
                  + " WHERE account_no = 1 AND portfolio_no = 3 AND photo_no = 2",
              Boolean.class));
      assertEquals(
          List.of(Map.of("tag_no", 1L, "tag_japanese_name", "植物", "tag_english_name", "plant")),
          jdbcTemplate.queryForList(
              "SELECT tag_no, tag_japanese_name, tag_english_name FROM photo.portfolio_tag_mst"
                  + " WHERE account_no = 1 AND portfolio_no = 3"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テンプレートの設定内容が不正な場合、400を返し登録しない")
    void registPortfolio_invalidSettings() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_invalid_settings.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.INVALID_PORTFOLIO_SETTINGS.getErrorMessage()));

      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 1", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：論理削除済みの写真を指定した場合、400を返し登録しない")
    void registPortfolio_deletedPhoto() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_deleted_photo.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.PORTFOLIO_PHOTO_NOT_AVAILABLE.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：存在しない写真番号（他人の写真の番号を含む）を指定した場合、400を返し登録しない")
    void registPortfolio_otherAccountPhoto() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_other_account_photo.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.PORTFOLIO_PHOTO_NOT_AVAILABLE.getErrorMessage()));

      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 3",
              Integer.class));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：他人のアカウントIDを指定した場合、403を返し登録しない")
    void registPortfolio_forbidden() throws Exception {
      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(2L, "bbbbbbbb")))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_feature_success.json")))
          .andExpect(status().isForbidden());

      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE portfolio_no = 3", Integer.class));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：mini-userで作成数が上限（3個）に達している場合、400を返し登録しない")
    void registPortfolio_reachedLimit() throws Exception {
      // account_no=1（mini-user）は作成済みが2個のため、1個追加して上限の3個にする
      jdbcTemplate.update(
          "INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at,"
              + " updated_by, updated_at, theme, template_kbn)"
              + " VALUES (1, 3, 1, NOW(), 1, NOW(), '3個目', 'feature')");

      mockMvc
          .perform(
              post(PORTFOLIOS)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_feature_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.REACHED_PORTFOLIO_LIMIT.getErrorMessage()));

      assertEquals(
          3,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 1", Integer.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class updatePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：下書きの内容・写真・タグを置き換え、公開状態は下書きのまま維持する")
    void updatePortfolio_success() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_grid_publishable.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを更新しました。"));

      assertEquals(
          "光と影（改）",
          jdbcTemplate.queryForObject(
              "SELECT theme FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 2",
              String.class));
      assertEquals("draft", statusOf(1L, 2L));
      assertTrue(
          jdbcTemplate.queryForObject(
              "SELECT template_settings = '{\"rows\": [{\"pattern\": \"WIDE_NARROW\"}]}'::jsonb"
                  + " FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 2",
              Boolean.class));
      assertEquals(
          List.of(
              Map.of("photo_no", 1L, "display_order", 1),
              Map.of("photo_no", 2L, "display_order", 2)),
          jdbcTemplate.queryForList(
              "SELECT photo_no, display_order FROM photo.portfolio_photo"
                  + " WHERE account_no = 1 AND portfolio_no = 2 ORDER BY display_order"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：公開中で、更新後の内容が公開できない（テーマ未入力の写真がある）場合、400を返し更新しない")
    void updatePortfolio_publishedNotPublishable() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_feature_without_theme.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorMessage()));

      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_photo WHERE account_no = 1 AND portfolio_no = 1",
              Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないポートフォリオ番号を指定した場合、404を返す")
    void updatePortfolio_notFound() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/9")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_grid_publishable.json")))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class updatePortfolioStatus {
    @Test
    @Order(1)
    @DisplayName("正常系：公開できる状態に更新した下書きを公開できる")
    void updatePortfolioStatus_publish() throws Exception {
      // 写真2枚・行パターン（2枚）で公開できる状態に更新してから公開する
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_grid_publishable.json")))
          .andExpect(status().isOk());

      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_published.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを公開しました。"));

      assertEquals("published", statusOf(1L, 2L));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：公開中のポートフォリオを下書きに戻せる")
    void updatePortfolioStatus_unpublish() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/1/status")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_draft.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを下書きに戻しました。"));

      assertEquals("draft", statusOf(1L, 1L));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：行パターンの枚数の合計と写真の枚数が一致しない下書きは、公開できず400を返す")
    void updatePortfolioStatus_notPublishable() throws Exception {
      mockMvc
          .perform(
              put(PORTFOLIOS + "/2/status")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("status_published.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.PORTFOLIO_NOT_PUBLISHABLE.getErrorMessage()));

      assertEquals("draft", statusOf(1L, 2L));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class deletePortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ本体・写真・タグを物理削除する。以降は詳細で取得できない")
    void deletePortfolio_success() throws Exception {
      mockMvc
          .perform(
              delete(PORTFOLIOS + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("ポートフォリオを削除しました。"));

      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 1 AND portfolio_no = 1",
              Integer.class));
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_photo WHERE account_no = 1 AND portfolio_no = 1",
              Integer.class));
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_tag_mst WHERE account_no = 1 AND portfolio_no = 1",
              Integer.class));

      mockMvc
          .perform(
              get(PORTFOLIOS + "/1")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：削除済み・存在しないポートフォリオを指定した場合、404を返す")
    void deletePortfolio_notFound() throws Exception {
      mockMvc
          .perform(
              delete(PORTFOLIOS + "/9")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner()))
                  .with(csrf()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PORTFOLIO_NOT_FOUND.getErrorCode()));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPortfolioTemplateList {
    @Test
    @Order(1)
    @DisplayName("正常系：認証済みのユーザーはテンプレート一覧を取得できる")
    void getPortfolioTemplateList_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/portfolio-templates")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.templateList.length()").value(2))
          .andExpect(jsonPath("$.gridRowPatternList.length()").value(4));
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PortfolioControllerIntegrationTest.sql")
  class searchAndLimit {
    @Test
    @Order(1)
    @DisplayName("正常系：一覧をテーマ・タグ名の部分一致で絞り込めること")
    void getPortfolioList_keywords() throws Exception {
      mockMvc
          .perform(
              get(PORTFOLIOS)
                  .param("theme", "光")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioList.length()").value(1))
          .andExpect(jsonPath("$.portfolioList[0].portfolioNo").value(2));

      mockMvc
          .perform(
              get(PORTFOLIOS)
                  .param("tag", "NATURE")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.portfolioList.length()").value(1))
          .andExpect(jsonPath("$.portfolioList[0].portfolioNo").value(1));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：mini-userは作成済み（2個）が上限（3個）未満の場合、上限に達していないと返すこと")
    void getPortfolioUpperLimit_notReached() throws Exception {
      mockMvc
          .perform(
              get(PORTFOLIOS + "/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isReachedUpperLimit").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：mini-userは作成済みが上限（3個）に達した場合、上限に達していると返すこと")
    void getPortfolioUpperLimit_reached() throws Exception {
      // 同一トランザクション内のMyBatisのキャッシュを避けるため、APIを呼び出す前に件数を上限に合わせる
      jdbcTemplate.update(
          "INSERT INTO photo.portfolio_mst (account_no, portfolio_no, created_by, created_at,"
              + " updated_by, updated_at, theme, template_kbn)"
              + " VALUES (1, 3, 1, NOW(), 1, NOW(), '3個目', 'feature')");

      mockMvc
          .perform(
              get(PORTFOLIOS + "/upper-limit")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isReachedUpperLimit").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：本人の未削除の写真を、タイトルの部分一致で検索できること（画像は署名付きURL）")
    void searchPortfolioPhotos_byTitle() throws Exception {
      doAnswer(
              invocation ->
                  new ImageFilePath(
                      "https://signed/" + ((ImageFilePath) invocation.getArgument(0)).value()))
          .when(fileRepository)
          .getPresignedUrl(any(ImageFilePath.class));

      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/portfolio-photos")
                  .param("title", "金閣")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.photoList.length()").value(1))
          .andExpect(jsonPath("$.photoList[0].photoNo").value(1))
          .andExpect(jsonPath("$.photoList[0].photoJapaneseTitle").value("金閣寺"))
          .andExpect(jsonPath("$.photoList[0].directionKbn").value("horizontal"))
          .andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://signed/1/1-aaa.jpg"))
          .andExpect(jsonPath("$.isLast").value(true));

      // 条件が未指定の場合は、論理削除済みの写真3を除いて撮影日時の新しい順に返す
      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/portfolio-photos")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.photoList[*].photoNo").value(contains(2, 1)));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：他人のアカウントIDで写真を検索した場合、403を返すこと")
    void searchPortfolioPhotos_forbidden() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/accounts/bbbbbbbb/portfolio-photos")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(owner())))
          .andExpect(status().isForbidden());
    }
  }
}
