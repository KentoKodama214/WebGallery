package com.web.gallery.infrastructure.persistence.mapper.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.photo.PhotoModerationLog;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoModerationLogCondition;
import java.time.OffsetDateTime;
import java.util.List;
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
public class PhotoModerationLogMapperTest {
  @Autowired private PhotoModerationLogMapper photoModerationLogMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<PhotoModerationLog> getPhotoModerationLogList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM photo.photo_moderation_log WHERE "
            + condition
            + " ORDER BY photo_moderation_log_no",
        (rs, rowNum) ->
            PhotoModerationLog.builder()
                .photoModerationLogNo(rs.getLong("photo_moderation_log_no"))
                .photoAccountNo(rs.getLong("photo_account_no"))
                .photoNo(rs.getLong("photo_no"))
                .adminAccountNo(rs.getLong("admin_account_no"))
                .isHidden(rs.getBoolean("is_hidden"))
                .reason(rs.getString("reason"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build());
  }

  private void insertAccountAndPhoto() {
    jdbcTemplate.update(
        "INSERT INTO common.account VALUES"
            + " (1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo',"
            + " false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none',"
            + " 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false)");
    jdbcTemplate.update(
        "INSERT INTO photo.photo_mst VALUES"
            + " (DEFAULT, 1, 1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1,"
            + " '2000-01-01 09:00:00 Asia/Tokyo', false, '2021-01-01 09:00:00 Asia/Tokyo', 1,"
            + " 'https://www.xxx.com/DSC111.jpg', 'DSC111.jpg', 'タイトル11', 'title11',"
            + " 'キャプション11', 'vertical', 24, 8.0, 1, 100, true)");
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま登録されること")
    void insert_withAllValues() {
      insertAccountAndPhoto();
      PhotoModerationLog insertLog =
          PhotoModerationLog.builder()
              .photoAccountNo(1L)
              .photoNo(1L)
              .adminAccountNo(99L)
              .isHidden(true)
              .reason("不適切な画像のため")
              .createdBy(99L)
              .createdAt(OffsetDateTime.parse("2026-11-01T10:00:00+09:00"))
              .build();

      Integer actualCount = photoModerationLogMapper.insert(insertLog);
      assertEquals(1, actualCount);

      List<PhotoModerationLog> actualData = getPhotoModerationLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getPhotoNo());
      // 操作した管理者のアカウント番号は外部キーを持たないため、存在しないアカウント番号でも登録できる
      assertEquals(99L, actualData.getFirst().getAdminAccountNo());
      assertTrue(actualData.getFirst().getIsHidden());
      assertEquals("不適切な画像のため", actualData.getFirst().getReason());
      assertEquals(99L, actualData.getFirst().getCreatedBy());
      assertTrue(
          OffsetDateTime.parse("2026-11-01T10:00:00+09:00")
              .isEqual(actualData.getFirst().getCreatedAt()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：作成日時が未設定の場合、現在日時で登録されること")
    void insert_withDefaultCreatedAt() {
      insertAccountAndPhoto();
      PhotoModerationLog insertLog =
          PhotoModerationLog.builder()
              .photoAccountNo(1L)
              .photoNo(1L)
              .adminAccountNo(99L)
              .isHidden(false)
              .reason("誤判定だったため")
              .createdBy(99L)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      photoModerationLogMapper.insert(insertLog);

      List<PhotoModerationLog> actualData = getPhotoModerationLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertFalse(actualData.getFirst().getIsHidden());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PhotoModerationLogMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：写真アカウント番号でのdelete")
    void delete_by_photoAccountNo() {
      PhotoModerationLogCondition deleteCondition =
          PhotoModerationLogCondition.byPhotoAccountNo(1L);
      Integer actual = photoModerationLogMapper.delete(deleteCondition);
      assertEquals(2, actual);

      assertEquals(0, getPhotoModerationLogList("photo_account_no=1").size());
      assertEquals(1, getPhotoModerationLogList("photo_account_no<>1").size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：抽出条件が未設定の場合、1件も削除しないこと")
    void delete_without_condition() {
      Integer actual =
          photoModerationLogMapper.delete(PhotoModerationLogCondition.builder().build());
      assertEquals(0, actual);

      assertEquals(3, getPhotoModerationLogList("true").size());
    }
  }
}
