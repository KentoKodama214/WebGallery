package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstUpdateTarget;
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
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PortfolioMstMapperTest {
  @Autowired private PortfolioMstMapper portfolioMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final OffsetDateTime UPDATED_AT_11 =
      OffsetDateTime.of(2001, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  /**
   * ポートフォリオマスタのjsonbカラムが、指定したJSONと等価かどうかを判定する
   *
   * @param id ID
   * @param json 期待するJSON
   * @return 等価な場合、true
   */
  private Boolean isTemplateSettingsEqualTo(Long id, String json) {
    return jdbcTemplate.queryForObject(
        "SELECT template_settings = ?::jsonb FROM photo.portfolio_mst WHERE id = ?",
        Boolean.class,
        json,
        id);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功。テンプレート区分・公開状態はEnum型、テンプレート設定はjsonb型で保存されること")
    void insert_success() {
      PortfolioMst portfolioMst =
          PortfolioMst.builder()
              .accountNo(3L)
              .portfolioNo(1L)
              .createdBy(3L)
              .updatedBy(3L)
              .theme("光と影")
              .description("説明文")
              .templateKbn(PortfolioTemplateEnum.GRID)
              .templateSettings(
                  Map.of(
                      "rows",
                      List.of(Map.of("pattern", "THREE_EQUAL"), Map.of("pattern", "WIDE_NARROW"))))
              .settingsVersion(1)
              .statusKbn(PortfolioStatusEnum.DRAFT)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      assertEquals(1, portfolioMstMapper.insert(portfolioMst));

      Map<String, Object> actual =
          jdbcTemplate.queryForMap("SELECT * FROM photo.portfolio_mst WHERE id = 5");
      assertEquals(3L, actual.get("account_no"));
      assertEquals(1L, actual.get("portfolio_no"));
      assertEquals(3L, actual.get("created_by"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM photo.portfolio_mst WHERE id = 5", OffsetDateTime.class));
      assertEquals(3L, actual.get("updated_by"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM photo.portfolio_mst WHERE id = 5", OffsetDateTime.class));
      assertEquals("光と影", actual.get("theme"));
      assertEquals("説明文", actual.get("description"));
      assertEquals("grid", actual.get("template_kbn").toString());
      assertEquals(1, actual.get("settings_version"));
      assertEquals("draft", actual.get("status_kbn").toString());
      assertTrue(
          isTemplateSettingsEqualTo(
              5L, "{\"rows\": [{\"pattern\": \"THREE_EQUAL\"}, {\"pattern\": \"WIDE_NARROW\"}]}"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号に該当する1件だけを、指定した項目で更新すること")
    void update_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.byAccountAndPortfolio(1L, 1L),
              PortfolioMstUpdateTarget.builder()
                  .updatedBy(1L)
                  .theme("新テーマ")
                  .description("新説明")
                  .templateKbn(PortfolioTemplateEnum.FEATURE)
                  .templateSettings(Map.of())
                  .settingsVersion(1)
                  .statusKbn(PortfolioStatusEnum.DRAFT)
                  .build());

      assertEquals(1, actualCount);
      Map<String, Object> actual =
          jdbcTemplate.queryForMap("SELECT * FROM photo.portfolio_mst WHERE id = 1");
      assertEquals("新テーマ", actual.get("theme"));
      assertEquals("新説明", actual.get("description"));
      assertEquals("feature", actual.get("template_kbn").toString());
      assertEquals("draft", actual.get("status_kbn").toString());
      assertTrue(isTemplateSettingsEqualTo(1L, "{}"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM photo.portfolio_mst WHERE id = 1", OffsetDateTime.class));
      // 他のポートフォリオは更新されない
      assertEquals(
          "ポートフォリオ12",
          jdbcTemplate.queryForObject(
              "SELECT theme FROM photo.portfolio_mst WHERE id = 2", String.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：更新対象にnullの項目がある場合、その項目は変更しないこと")
    void update_partial() {
      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.byAccountAndPortfolio(1L, 1L),
              PortfolioMstUpdateTarget.builder()
                  .updatedBy(1L)
                  .statusKbn(PortfolioStatusEnum.DRAFT)
                  .build());

      assertEquals(1, actualCount);
      Map<String, Object> actual =
          jdbcTemplate.queryForMap("SELECT * FROM photo.portfolio_mst WHERE id = 1");
      assertEquals("draft", actual.get("status_kbn").toString());
      assertEquals("ポートフォリオ11", actual.get("theme"));
      assertEquals("grid", actual.get("template_kbn").toString());
      assertTrue(isTemplateSettingsEqualTo(1L, "{\"rows\": [{\"pattern\": \"TWO_EQUAL\"}]}"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件更新せず0件となること")
    void update_noCondition() {
      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.builder().build(),
              PortfolioMstUpdateTarget.builder().updatedBy(1L).theme("新テーマ").build());

      assertEquals(0, actualCount);
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE theme = '新テーマ'", Integer.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号に該当するポートフォリオをすべて物理削除すること")
    void delete_byAccountNo() {
      // 外部キーで参照するポートフォリオ写真・タグを先に削除しておく
      jdbcTemplate.update("DELETE FROM photo.portfolio_photo WHERE account_no = 1");
      jdbcTemplate.update("DELETE FROM photo.portfolio_tag_mst WHERE account_no = 1");

      Integer actualCount = portfolioMstMapper.delete(PortfolioMstCondition.byAccountNo(1L));

      assertEquals(3, actualCount);
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 1", Integer.class));
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 2", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号に該当する1件だけを物理削除すること")
    void delete_byAccountAndPortfolio() {
      // 外部キーで参照するポートフォリオ写真を先に削除しておく
      jdbcTemplate.update(
          "DELETE FROM photo.portfolio_photo WHERE account_no = 1 AND portfolio_no = 2");

      Integer actualCount =
          portfolioMstMapper.delete(PortfolioMstCondition.byAccountAndPortfolio(1L, 2L));

      assertEquals(1, actualCount);
      assertEquals(
          List.of(1L, 3L),
          jdbcTemplate.queryForList(
              "SELECT portfolio_no FROM photo.portfolio_mst WHERE account_no = 1 ORDER BY portfolio_no",
              Long.class));
      // 他アカウントの同じ番号のポートフォリオは削除しない
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE account_no = 2", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件削除せず0件となること")
    void delete_noCondition() {
      Integer actualCount = portfolioMstMapper.delete(PortfolioMstCondition.builder().build());

      assertEquals(0, actualCount);
      assertEquals(
          4,
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM photo.portfolio_mst", Integer.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class getMaxPortfolioNo {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大のポートフォリオ番号を返すこと")
    void getMaxPortfolioNo_success() {
      assertEquals(3L, portfolioMstMapper.getMaxPortfolioNo(1L));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ポートフォリオが未登録のアカウントの場合、nullを返すこと")
    void getMaxPortfolioNo_none() {
      assertNull(portfolioMstMapper.getMaxPortfolioNo(3L));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class selectList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号で絞り込み、更新日時の新しい順に写真の枚数とあわせて取得すること")
    void selectList_byAccountNo() {
      List<PortfolioDto> actual =
          portfolioMstMapper.selectList(PortfolioMstCondition.byAccountNo(1L));

      assertEquals(List.of(3L, 2L, 1L), actual.stream().map(PortfolioDto::getPortfolioNo).toList());
      PortfolioDto second = actual.get(1);
      assertEquals(1L, second.getAccountNo());
      assertEquals("ポートフォリオ12", second.getTheme());
      assertEquals(PortfolioTemplateEnum.FEATURE, second.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.DRAFT, second.getStatusKbn());
      assertEquals(1, second.getPhotoCount());
      PortfolioDto third = actual.get(2);
      assertEquals(PortfolioTemplateEnum.GRID, third.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.PUBLISHED, third.getStatusKbn());
      assertEquals(2, third.getPhotoCount());
      assertTrue(UPDATED_AT_11.isEqual(third.getUpdatedAt()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ポートフォリオ番号の条件で絞り込むこと")
    void selectList_byPortfolioNo() {
      List<PortfolioDto> actual =
          portfolioMstMapper.selectList(PortfolioMstCondition.byAccountAndPortfolio(2L, 1L));

      assertEquals(1, actual.size());
      assertEquals("ポートフォリオ21", actual.get(0).getTheme());
      assertEquals(1, actual.get(0).getPhotoCount());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：該当するポートフォリオがない場合、0件となること")
    void selectList_none() {
      assertTrue(portfolioMstMapper.selectList(PortfolioMstCondition.byAccountNo(3L)).isEmpty());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class select {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号で絞り込み、全項目（jsonbを含む）を取得すること")
    void select_success() {
      List<PortfolioMst> actual =
          portfolioMstMapper.select(PortfolioMstCondition.byAccountAndPortfolio(1L, 1L));

      assertEquals(1, actual.size());
      PortfolioMst mst = actual.get(0);
      assertEquals(1L, mst.getId());
      assertEquals(1L, mst.getAccountNo());
      assertEquals(1L, mst.getPortfolioNo());
      assertEquals(1L, mst.getCreatedBy());
      assertEquals(1L, mst.getUpdatedBy());
      assertTrue(UPDATED_AT_11.isEqual(mst.getUpdatedAt()));
      assertEquals("ポートフォリオ11", mst.getTheme());
      assertEquals("説明11", mst.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, mst.getTemplateKbn());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), mst.getTemplateSettings());
      assertEquals(1, mst.getSettingsVersion());
      assertEquals(PortfolioStatusEnum.PUBLISHED, mst.getStatusKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：他アカウントのポートフォリオ番号を指定しても取得しないこと（アカウント番号とのAND条件）")
    void select_otherAccount() {
      List<PortfolioMst> actual =
          portfolioMstMapper.select(PortfolioMstCondition.byAccountAndPortfolio(2L, 2L));

      assertTrue(actual.isEmpty());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioMstMapperTest.sql")
  class selectByPhoto {
    @Test
    @Order(1)
    @DisplayName("正常系：指定した写真を使用しているポートフォリオを、ポートフォリオ番号順に取得すること")
    void selectByPhoto_success() {
      List<PortfolioMst> actual = portfolioMstMapper.selectByPhoto(1L, 1L);

      assertEquals(List.of(1L, 2L, 3L), actual.stream().map(PortfolioMst::getPortfolioNo).toList());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))),
          actual.get(0).getTemplateSettings());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：同じ写真番号でも、他アカウントのポートフォリオは取得しないこと")
    void selectByPhoto_otherAccount() {
      List<PortfolioMst> actual = portfolioMstMapper.selectByPhoto(2L, 1L);

      assertEquals(1, actual.size());
      assertEquals(2L, actual.get(0).getAccountNo());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：どのポートフォリオにも使用されていない写真の場合、0件となること")
    void selectByPhoto_none() {
      assertTrue(portfolioMstMapper.selectByPhoto(1L, 3L).isEmpty());
    }
  }
}
