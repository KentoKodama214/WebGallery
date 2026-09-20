package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.account.LoginHistory;
import com.web.gallery.entity.account.LoginHistoryCondition;
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
public class LoginHistoryMapperTest {
  @Autowired private LoginHistoryMapper loginHistoryMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<LoginHistory> getLoginHistoryList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM common.login_history WHERE " + condition,
        (rs, rowNum) ->
            LoginHistory.builder()
                .loginHistoryNo(rs.getLong("login_history_no"))
                .accountNo(rs.getLong("account_no"))
                .ipAddress(rs.getString("ip_address"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：国・地域が解決できた場合、そのまま登録されること")
    void insert_withCountryAndRegion() {
      jdbcTemplate.update(
          "INSERT INTO common.account VALUES"
              + " (1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo',"
              + " false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none',"
              + " 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false)");
      LoginHistory insertLoginHistory =
          LoginHistory.builder()
              .accountNo(1L)
              .ipAddress("203.0.113.9")
              .country("JP")
              .region("Tokyo")
              .createdBy(1L)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = loginHistoryMapper.insert(insertLoginHistory);
      assertEquals(1, actualCount);

      List<LoginHistory> actualData = getLoginHistoryList("account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals("203.0.113.9", actualData.getFirst().getIpAddress());
      assertEquals("JP", actualData.getFirst().getCountry());
      assertEquals("Tokyo", actualData.getFirst().getRegion());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：国・地域が未解決（null）の場合、空文字で登録されること")
    void insert_withoutCountryAndRegion() {
      jdbcTemplate.update(
          "INSERT INTO common.account VALUES"
              + " (1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo',"
              + " false, 'aaaaaaaa', 'AAAAAAAA', '$2a$10$password1', '1991-02-14', 'none', 'none',"
              + " 'none', '', '2002-01-01 09:00:00 Asia/Tokyo', 0, false)");
      LoginHistory insertLoginHistory =
          LoginHistory.builder()
              .accountNo(1L)
              .ipAddress("203.0.113.9")
              .country(null)
              .region(null)
              .createdBy(1L)
              .build();

      Integer actualCount = loginHistoryMapper.insert(insertLoginHistory);
      assertEquals(1, actualCount);

      List<LoginHistory> actualData = getLoginHistoryList("account_no=1");
      assertEquals(1, actualData.size());
      assertEquals("", actualData.getFirst().getCountry());
      assertEquals("", actualData.getFirst().getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/LoginHistoryMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号でのdelete")
    void delete_by_accountNo() {
      LoginHistoryCondition deleteCondition = LoginHistoryCondition.byAccountNo(1L);
      Integer actual = loginHistoryMapper.delete(deleteCondition);
      assertEquals(2, actual);

      List<LoginHistory> actualData = getLoginHistoryList("account_no=1");
      assertEquals(0, actualData.size());

      List<LoginHistory> actualRestData = getLoginHistoryList("account_no<>1");
      assertEquals(1, actualRestData.size());
    }
  }
}
