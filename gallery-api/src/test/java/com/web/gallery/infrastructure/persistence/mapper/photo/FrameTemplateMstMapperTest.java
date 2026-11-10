package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMst;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.FrameTemplateMstUpdateTarget;
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
public class FrameTemplateMstMapperTest {
  @Autowired private FrameTemplateMstMapper frameTemplateMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final Map<String, Object> DEFINITION =
      Map.of(
          "size", Map.of("width", 364, "height", 257),
          "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40, "rotation", -3.5)));

  private static final String DEFINITION_JSON =
      "{\"size\": {\"width\": 364, \"height\": 257},"
          + " \"slots\": [{\"x\": 10, \"y\": 10, \"width\": 30, \"height\": 40, \"rotation\": -3.5}]}";

  /**
   * 額縁テンプレートマスタの額縁定義（jsonb）が、指定したJSONと等価かどうかを判定する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @param json 期待するJSON
   * @return 等価な場合、true
   */
  private Boolean isFrameDefinitionEqualTo(Long accountNo, Long frameTemplateNo, String json) {
    return jdbcTemplate.queryForObject(
        "SELECT frame_definition = ?::jsonb FROM photo.frame_template_mst"
            + " WHERE account_no = ? AND frame_template_no = ?",
        Boolean.class,
        json,
        accountNo,
        frameTemplateNo);
  }

  private OffsetDateTime selectDateTime(String column, Long accountNo, Long frameTemplateNo) {
    return jdbcTemplate.queryForObject(
        "SELECT "
            + column
            + " FROM photo.frame_template_mst WHERE account_no = ? AND frame_template_no = ?",
        OffsetDateTime.class,
        accountNo,
        frameTemplateNo);
  }

  private Map<String, Object> selectRow(Long accountNo, Long frameTemplateNo) {
    return jdbcTemplate.queryForMap(
        "SELECT template_name, created_by, updated_by"
            + " FROM photo.frame_template_mst WHERE account_no = ? AND frame_template_no = ?",
        accountNo,
        frameTemplateNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功。額縁定義はjsonb型で保存され、作成日時・更新日時は現在時刻となること")
    void insert_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      FrameTemplateMst entity =
          FrameTemplateMst.builder()
              .accountNo(3L)
              .frameTemplateNo(1L)
              .createdBy(3L)
              .updatedBy(3L)
              .templateName("新しい額縁")
              .frameDefinition(DEFINITION)
              .build();

      assertEquals(1, frameTemplateMstMapper.insert(entity));

      Map<String, Object> row = selectRow(3L, 1L);
      assertEquals("新しい額縁", row.get("template_name"));
      assertEquals(3L, row.get("created_by"));
      assertEquals(3L, row.get("updated_by"));
      assertEquals(transactionNow, selectDateTime("created_at", 3L, 1L));
      assertEquals(transactionNow, selectDateTime("updated_at", 3L, 1L));
      assertTrue(isFrameDefinitionEqualTo(3L, 1L, DEFINITION_JSON));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号に一致する1件だけを更新し、更新日時を現在時刻にすること")
    void update_byAccountAndFrameTemplate() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actual =
          frameTemplateMstMapper.update(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 1L),
              FrameTemplateMstUpdateTarget.builder()
                  .updatedBy(1L)
                  .templateName("更新後")
                  .frameDefinition(DEFINITION)
                  .build());

      assertEquals(1, actual);
      Map<String, Object> row = selectRow(1L, 1L);
      assertEquals("更新後", row.get("template_name"));
      assertEquals(transactionNow, selectDateTime("updated_at", 1L, 1L));
      assertTrue(isFrameDefinitionEqualTo(1L, 1L, DEFINITION_JSON));
      // 同じアカウントの別の額縁テンプレート・別アカウントの同じ番号は更新しない
      assertEquals("横長 2枚組", selectRow(1L, 2L).get("template_name"));
      assertEquals("正方形", selectRow(2L, 1L).get("template_name"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：更新対象に指定しない項目は変更しないこと")
    void update_partialTarget() {
      Integer actual =
          frameTemplateMstMapper.update(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 1L),
              FrameTemplateMstUpdateTarget.builder().templateName("名前だけ変更").build());

      assertEquals(1, actual);
      assertEquals("名前だけ変更", selectRow(1L, 1L).get("template_name"));
      assertEquals(1L, selectRow(1L, 1L).get("updated_by"));
      assertTrue(
          isFrameDefinitionEqualTo(
              1L,
              1L,
              "{\"size\": {\"width\": 254, \"height\": 305}, \"slots\": [{\"x\": 10, \"y\": 10,"
                  + " \"width\": 80, \"height\": 70}], \"texts\": [{\"x\": 10, \"y\": 85, \"width\":"
                  + " 80, \"fontFamily\": \"mincho\", \"fontSize\": 3}]}"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：該当する額縁テンプレートがない場合、0件を返すこと")
    void update_notFound() {
      Integer actual =
          frameTemplateMstMapper.update(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(3L, 1L),
              FrameTemplateMstUpdateTarget.builder().templateName("更新後").build());

      assertEquals(0, actual);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件更新せず0件を返すこと")
    void update_noCondition() {
      Integer actual =
          frameTemplateMstMapper.update(
              FrameTemplateMstCondition.builder().build(),
              FrameTemplateMstUpdateTarget.builder().templateName("更新後").build());

      assertEquals(0, actual);
      assertEquals("四つ切 1枚", selectRow(1L, 1L).get("template_name"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号に一致する1件だけを削除すること")
    void delete_byAccountAndFrameTemplate() {
      assertEquals(
          1,
          frameTemplateMstMapper.delete(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 1L)));

      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.frame_template_mst", Integer.class));
      assertEquals(
          0,
          frameTemplateMstMapper.count(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 1L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号に一致する額縁テンプレートをすべて削除すること")
    void delete_byAccountNo() {
      assertEquals(2, frameTemplateMstMapper.delete(FrameTemplateMstCondition.byAccountNo(1L)));

      assertEquals(0, frameTemplateMstMapper.count(FrameTemplateMstCondition.byAccountNo(1L)));
      assertEquals(1, frameTemplateMstMapper.count(FrameTemplateMstCondition.byAccountNo(2L)));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：抽出条件が1つも指定されていない場合、全件削除せず0件を返すこと")
    void delete_noCondition() {
      assertEquals(0, frameTemplateMstMapper.delete(FrameTemplateMstCondition.builder().build()));

      assertEquals(
          3,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.frame_template_mst", Integer.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class getMaxFrameTemplateNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントの最大の額縁テンプレート番号を返すこと")
    void getMaxFrameTemplateNo_existing() {
      assertEquals(2L, frameTemplateMstMapper.getMaxFrameTemplateNo(1L));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：額縁テンプレートが未登録のアカウントの場合、nullを返すこと")
    void getMaxFrameTemplateNo_none() {
      assertNull(frameTemplateMstMapper.getMaxFrameTemplateNo(3L));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class count {
    @Test
    @Order(1)
    @DisplayName("アカウント番号で絞り込んだ件数を返すこと")
    void count_byAccountNo() {
      assertEquals(2, frameTemplateMstMapper.count(FrameTemplateMstCondition.byAccountNo(1L)));
      assertEquals(0, frameTemplateMstMapper.count(FrameTemplateMstCondition.byAccountNo(3L)));
    }

    @Test
    @Order(2)
    @DisplayName("アカウント番号・額縁テンプレート番号のAND条件で絞り込んだ件数を返すこと")
    void count_byAccountAndFrameTemplate() {
      assertEquals(
          1,
          frameTemplateMstMapper.count(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(2L, 1L)));
      assertEquals(
          0,
          frameTemplateMstMapper.count(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(2L, 2L)));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/FrameTemplateMstMapperTest.sql")
  class select {
    @Test
    @Order(1)
    @DisplayName("アカウント番号で絞り込み、額縁テンプレート番号の昇順に全項目を取得すること")
    void select_byAccountNo() {
      List<FrameTemplateMst> actual =
          frameTemplateMstMapper.select(FrameTemplateMstCondition.byAccountNo(1L));

      assertEquals(2, actual.size());
      FrameTemplateMst first = actual.get(0);
      assertEquals(2L, first.getId());
      assertEquals(1L, first.getAccountNo());
      assertEquals(1L, first.getFrameTemplateNo());
      assertEquals(1L, first.getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)).toInstant(),
          first.getCreatedAt().toInstant());
      assertEquals(1L, first.getUpdatedBy());
      assertEquals(
          OffsetDateTime.of(2001, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)).toInstant(),
          first.getUpdatedAt().toInstant());
      assertEquals("四つ切 1枚", first.getTemplateName());
      assertEquals(
          Map.of(
              "size", Map.of("width", 254, "height", 305),
              "slots", List.of(Map.of("x", 10, "y", 10, "width", 80, "height", 70)),
              "texts",
                  List.of(
                      Map.of(
                          "x", 10, "y", 85, "width", 80, "fontFamily", "mincho", "fontSize", 3))),
          first.getFrameDefinition());
      assertEquals(2L, actual.get(1).getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("アカウント番号・額縁テンプレート番号のAND条件で1件に絞り込むこと")
    void select_byAccountAndFrameTemplate() {
      List<FrameTemplateMst> actual =
          frameTemplateMstMapper.select(
              FrameTemplateMstCondition.byAccountAndFrameTemplate(2L, 1L));

      assertEquals(1, actual.size());
      assertEquals("正方形", actual.get(0).getTemplateName());
    }

    @Test
    @Order(3)
    @DisplayName("該当する額縁テンプレートがない場合、空のリストを返すこと")
    void select_narrowsToZero() {
      assertTrue(
          frameTemplateMstMapper.select(FrameTemplateMstCondition.byAccountNo(3L)).isEmpty());
      assertTrue(
          frameTemplateMstMapper
              .select(FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 3L))
              .isEmpty());
    }
  }
}
