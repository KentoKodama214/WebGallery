package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import java.time.OffsetDateTime;
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
public class PortfolioTagMstMapperTest {
  @Autowired private PortfolioTagMstMapper portfolioTagMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioTagMstMapperTest.sql")
  class insertBulk {
    @Test
    @Order(1)
    @DisplayName("正常系：複数件を一括登録すること")
    void insertBulk_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          portfolioTagMstMapper.insertBulk(
              List.of(
                  PortfolioTagMst.builder()
                      .accountNo(1L)
                      .portfolioNo(2L)
                      .tagNo(1L)
                      .createdBy(1L)
                      .tagJapaneseName("光")
                      .tagEnglishName("light")
                      .build(),
                  PortfolioTagMst.builder()
                      .accountNo(1L)
                      .portfolioNo(2L)
                      .tagNo(2L)
                      .createdBy(1L)
                      .tagJapaneseName("影")
                      .tagEnglishName("")
                      .build()));

      assertEquals(2, actualCount);
      Map<String, Object> actual =
          jdbcTemplate.queryForMap("SELECT * FROM photo.portfolio_tag_mst WHERE id = 4");
      assertEquals(1L, actual.get("account_no"));
      assertEquals(2L, actual.get("portfolio_no"));
      assertEquals(1L, actual.get("tag_no"));
      assertEquals(1L, actual.get("created_by"));
      assertEquals("光", actual.get("tag_japanese_name"));
      assertEquals("light", actual.get("tag_english_name"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM photo.portfolio_tag_mst WHERE id = 4", OffsetDateTime.class));
      assertEquals(
          "",
          jdbcTemplate.queryForObject(
              "SELECT tag_english_name FROM photo.portfolio_tag_mst WHERE id = 5", String.class));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioTagMstMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号に該当するタグだけを削除すること")
    void delete_byAccountAndPortfolio() {
      Integer actualCount =
          portfolioTagMstMapper.delete(PortfolioTagMstCondition.byAccountAndPortfolio(1L, 1L));

      assertEquals(2, actualCount);
      // 他アカウントの同じポートフォリオ番号のタグは削除されない
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_tag_mst WHERE account_no = 2", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号に該当するタグをすべて削除すること")
    void delete_byAccountNo() {
      Integer actualCount = portfolioTagMstMapper.delete(PortfolioTagMstCondition.byAccountNo(2L));

      assertEquals(1, actualCount);
      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_tag_mst", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件削除せず0件となること")
    void delete_noCondition() {
      Integer actualCount =
          portfolioTagMstMapper.delete(PortfolioTagMstCondition.builder().build());

      assertEquals(0, actualCount);
      assertEquals(
          3,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.portfolio_tag_mst", Integer.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PortfolioTagMstMapperTest.sql")
  class select {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号で絞り込み、タグ番号順に全項目を取得すること")
    void select_byAccountAndPortfolio() {
      List<PortfolioTagMst> actual =
          portfolioTagMstMapper.select(PortfolioTagMstCondition.byAccountAndPortfolio(1L, 1L));

      assertEquals(2, actual.size());
      PortfolioTagMst first = actual.get(0);
      assertEquals(1L, first.getId());
      assertEquals(1L, first.getAccountNo());
      assertEquals(1L, first.getPortfolioNo());
      assertEquals(1L, first.getTagNo());
      assertEquals(1L, first.getCreatedBy());
      assertNotNull(first.getCreatedAt());
      assertEquals("自然", first.getTagJapaneseName());
      assertEquals("nature", first.getTagEnglishName());
      assertEquals(2L, actual.get(1).getTagNo());
      assertEquals("", actual.get(1).getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号の条件で絞り込むこと")
    void select_byAccountNo() {
      List<PortfolioTagMst> actual =
          portfolioTagMstMapper.select(PortfolioTagMstCondition.byAccountNo(2L));

      assertEquals(1, actual.size());
      assertEquals("海", actual.get(0).getTagJapaneseName());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：該当するタグがない場合、0件となること")
    void select_none() {
      assertTrue(
          portfolioTagMstMapper
              .select(PortfolioTagMstCondition.byAccountAndPortfolio(1L, 2L))
              .isEmpty());
    }
  }
}
