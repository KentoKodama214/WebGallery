package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.PhotoViewLog;
import com.web.gallery.entity.PhotoViewLogCondition;
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
public class PhotoViewLogMapperTest {
  @Autowired private PhotoViewLogMapper photoViewLogMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<PhotoViewLog> getPhotoViewLogList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM photo.photo_view_log WHERE " + condition,
        (rs, rowNum) ->
            PhotoViewLog.builder()
                .photoViewLogNo(rs.getLong("photo_view_log_no"))
                .photoAccountNo(rs.getLong("photo_account_no"))
                .photoNo(rs.getLong("photo_no"))
                .accountNo(rs.getLong("account_no"))
                .referer(rs.getString("referer"))
                .ipAddress(rs.getString("ip_address"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
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
      PhotoViewLog insertLog =
          PhotoViewLog.builder()
              .photoAccountNo(1L)
              .photoNo(1L)
              .accountNo(1L)
              .referer("https://example.com/")
              .ipAddress("203.0.113.9")
              .country("JP")
              .region("Tokyo")
              .createdBy(1L)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = photoViewLogMapper.insert(insertLog);
      assertEquals(1, actualCount);

      List<PhotoViewLog> actualData = getPhotoViewLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getPhotoNo());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals("https://example.com/", actualData.getFirst().getReferer());
      assertEquals("203.0.113.9", actualData.getFirst().getIpAddress());
      assertEquals("JP", actualData.getFirst().getCountry());
      assertEquals("Tokyo", actualData.getFirst().getRegion());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：任意項目（referer・country・region）が未設定の場合、空文字で登録されること")
    void insert_withoutOptionalValues() {
      insertAccountAndPhoto();
      PhotoViewLog insertLog =
          PhotoViewLog.builder()
              .photoAccountNo(1L)
              .photoNo(1L)
              .accountNo(0L)
              .referer(null)
              .ipAddress("203.0.113.9")
              .country(null)
              .region(null)
              .createdBy(1L)
              .build();

      Integer actualCount = photoViewLogMapper.insert(insertLog);
      assertEquals(1, actualCount);

      List<PhotoViewLog> actualData = getPhotoViewLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertEquals("", actualData.getFirst().getReferer());
      assertEquals("", actualData.getFirst().getCountry());
      assertEquals("", actualData.getFirst().getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PhotoViewLogMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：写真アカウント番号でのdelete")
    void delete_by_photoAccountNo() {
      PhotoViewLogCondition deleteCondition = PhotoViewLogCondition.byPhotoAccountNo(1L);
      Integer actual = photoViewLogMapper.delete(deleteCondition);
      assertEquals(2, actual);

      List<PhotoViewLog> actualData = getPhotoViewLogList("photo_account_no=1");
      assertEquals(0, actualData.size());

      List<PhotoViewLog> actualRestData = getPhotoViewLogList("photo_account_no<>1");
      assertEquals(1, actualRestData.size());
    }
  }
}
