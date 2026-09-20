package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.PhotoListFilterLog;
import com.web.gallery.entity.PhotoListFilterLogCondition;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
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
public class PhotoListFilterLogMapperTest {
  @Autowired private PhotoListFilterLogMapper photoListFilterLogMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<PhotoListFilterLog> getPhotoListFilterLogList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM photo.photo_list_filter_log WHERE " + condition,
        (rs, rowNum) ->
            PhotoListFilterLog.builder()
                .photoListFilterLogNo(rs.getLong("photo_list_filter_log_no"))
                .photoAccountNo(rs.getLong("photo_account_no"))
                .accountNo(rs.getLong("account_no"))
                .directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
                .isFavorite(rs.getBoolean("is_favorite"))
                .tagList(rs.getString("tag_list"))
                .sortBy(SortPhotoEnum.getOrDefault(rs.getString("sort_by")))
                .referer(rs.getString("referer"))
                .ipAddress(rs.getString("ip_address"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build());
  }

  private void insertAccount() {
    jdbcTemplate.update(
        "INSERT INTO common.account VALUES"
            + " (1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo',"
            + " false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none',"
            + " 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false)");
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
      insertAccount();
      PhotoListFilterLog insertLog =
          PhotoListFilterLog.builder()
              .photoAccountNo(1L)
              .accountNo(1L)
              .directionKbn(DirectionEnum.VERTICAL)
              .isFavorite(true)
              .tagList("風景")
              .sortBy(SortPhotoEnum.FAVORITE)
              .referer("https://example.com/")
              .ipAddress("203.0.113.9")
              .country("JP")
              .region("Tokyo")
              .createdBy(1L)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = photoListFilterLogMapper.insert(insertLog);
      assertEquals(1, actualCount);

      List<PhotoListFilterLog> actualData = getPhotoListFilterLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(DirectionEnum.VERTICAL, actualData.getFirst().getDirectionKbn());
      assertTrue(actualData.getFirst().getIsFavorite());
      assertEquals("風景", actualData.getFirst().getTagList());
      assertEquals(SortPhotoEnum.FAVORITE, actualData.getFirst().getSortBy());
      assertEquals("https://example.com/", actualData.getFirst().getReferer());
      assertEquals("203.0.113.9", actualData.getFirst().getIpAddress());
      assertEquals("JP", actualData.getFirst().getCountry());
      assertEquals("Tokyo", actualData.getFirst().getRegion());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：任意項目（tagList・referer・country・region）が未設定の場合、空文字で登録されること")
    void insert_withoutOptionalValues() {
      insertAccount();
      PhotoListFilterLog insertLog =
          PhotoListFilterLog.builder()
              .photoAccountNo(1L)
              .accountNo(0L)
              .directionKbn(DirectionEnum.NONE)
              .isFavorite(false)
              .tagList(null)
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .referer(null)
              .ipAddress("203.0.113.9")
              .country(null)
              .region(null)
              .createdBy(1L)
              .build();

      Integer actualCount = photoListFilterLogMapper.insert(insertLog);
      assertEquals(1, actualCount);

      List<PhotoListFilterLog> actualData = getPhotoListFilterLogList("photo_account_no=1");
      assertEquals(1, actualData.size());
      assertEquals("", actualData.getFirst().getTagList());
      assertEquals("", actualData.getFirst().getReferer());
      assertEquals("", actualData.getFirst().getCountry());
      assertEquals("", actualData.getFirst().getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PhotoListFilterLogMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：写真アカウント番号でのdelete")
    void delete_by_photoAccountNo() {
      PhotoListFilterLogCondition deleteCondition =
          PhotoListFilterLogCondition.byPhotoAccountNo(1L);
      Integer actual = photoListFilterLogMapper.delete(deleteCondition);
      assertEquals(2, actual);

      List<PhotoListFilterLog> actualData = getPhotoListFilterLogList("photo_account_no=1");
      assertEquals(0, actualData.size());

      List<PhotoListFilterLog> actualRestData = getPhotoListFilterLogList("photo_account_no<>1");
      assertEquals(1, actualRestData.size());
    }
  }
}
