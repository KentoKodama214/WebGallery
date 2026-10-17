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
              .isDeleted(false)
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
      assertEquals(false, actual.get("is_deleted"));
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
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号に該当する未削除の1件だけを、指定した項目で更新すること")
    void update_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 1L),
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
      assertEquals(false, actual.get("is_deleted"));
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
              PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 1L),
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
    @DisplayName("正常系：論理削除済みのポートフォリオは、削除済みを除外する条件では更新されないこと")
    void update_deletedNotUpdated() {
      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 3L),
              PortfolioMstUpdateTarget.forDelete(1L));

      assertEquals(0, actualCount);
    }

    @Test
    @Order(4)
    @DisplayName("正常系：論理削除の更新対象で、削除フラグを立てること")
    void update_logicalDelete() {
      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 2L),
              PortfolioMstUpdateTarget.forDelete(1L));

      assertEquals(1, actualCount);
      assertTrue(
          jdbcTemplate.queryForObject(
              "SELECT is_deleted FROM photo.portfolio_mst WHERE id = 2", Boolean.class));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件更新せず0件となること")
    void update_noCondition() {
      Integer actualCount =
          portfolioMstMapper.update(
              PortfolioMstCondition.builder().build(), PortfolioMstUpdateTarget.forDelete(1L));

      assertEquals(0, actualCount);
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_mst WHERE is_deleted = true", Integer.class));
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
    @DisplayName("正常系：アカウント番号に該当するポートフォリオを、論理削除済みも含めて物理削除すること")
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
    @DisplayName("正常系：論理削除済みを含めた最大のポートフォリオ番号を返すこと")
    void getMaxPortfolioNo_includesDeleted() {
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
    @DisplayName("正常系：アカウント番号・削除済み除外で絞り込み、更新日時の新しい順に写真の枚数とあわせて取得すること")
    void selectList_byAccountNoNotDeleted() {
      List<PortfolioDto> actual =
          portfolioMstMapper.selectList(PortfolioMstCondition.byAccountNoNotDeleted(1L));

      assertEquals(2, actual.size());
      PortfolioDto first = actual.get(0);
      assertEquals(1L, first.getAccountNo());
      assertEquals(2L, first.getPortfolioNo());
      assertEquals("ポートフォリオ12", first.getTheme());
      assertEquals(PortfolioTemplateEnum.FEATURE, first.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.DRAFT, first.getStatusKbn());
      assertEquals(1, first.getPhotoCount());
      PortfolioDto second = actual.get(1);
      assertEquals(1L, second.getPortfolioNo());
      assertEquals(PortfolioTemplateEnum.GRID, second.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.PUBLISHED, second.getStatusKbn());
      assertEquals(2, second.getPhotoCount());
      assertTrue(UPDATED_AT_11.isEqual(second.getUpdatedAt()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除フラグを条件に含めない場合、論理削除済みも取得すること")
    void selectList_byAccountNo() {
      List<PortfolioDto> actual =
          portfolioMstMapper.selectList(PortfolioMstCondition.byAccountNo(1L));

      assertEquals(List.of(3L, 2L, 1L), actual.stream().map(PortfolioDto::getPortfolioNo).toList());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ポートフォリオ番号の条件で絞り込むこと")
    void selectList_byPortfolioNo() {
      List<PortfolioDto> actual =
          portfolioMstMapper.selectList(
              PortfolioMstCondition.byAccountAndPortfolioNotDeleted(2L, 1L));

      assertEquals(1, actual.size());
      assertEquals("ポートフォリオ21", actual.get(0).getTheme());
      assertEquals(1, actual.get(0).getPhotoCount());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：該当するポートフォリオがない場合、0件となること")
    void selectList_none() {
      assertTrue(
          portfolioMstMapper.selectList(PortfolioMstCondition.byAccountNoNotDeleted(3L)).isEmpty());
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
          portfolioMstMapper.select(PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 1L));

      assertEquals(1, actual.size());
      PortfolioMst mst = actual.get(0);
      assertEquals(1L, mst.getId());
      assertEquals(1L, mst.getAccountNo());
      assertEquals(1L, mst.getPortfolioNo());
      assertEquals(1L, mst.getCreatedBy());
      assertEquals(1L, mst.getUpdatedBy());
      assertTrue(UPDATED_AT_11.isEqual(mst.getUpdatedAt()));
      assertFalse(mst.getIsDeleted());
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
    @DisplayName("正常系：論理削除済みのポートフォリオは、削除済みを除外する条件では取得しないこと")
    void select_deletedExcluded() {
      assertTrue(
          portfolioMstMapper
              .select(PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 3L))
              .isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：他アカウントのポートフォリオ番号を指定しても取得しないこと（アカウント番号とのAND条件）")
    void select_otherAccount() {
      List<PortfolioMst> actual =
          portfolioMstMapper.select(PortfolioMstCondition.byAccountAndPortfolioNotDeleted(2L, 2L));

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
    @DisplayName("正常系：指定した写真を使用している未削除のポートフォリオを、ポートフォリオ番号順に取得すること")
    void selectByPhoto_success() {
      List<PortfolioMst> actual = portfolioMstMapper.selectByPhoto(1L, 1L);

      // 論理削除済みのポートフォリオ3も写真1を使用しているが、対象外とする
      assertEquals(List.of(1L, 2L), actual.stream().map(PortfolioMst::getPortfolioNo).toList());
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
