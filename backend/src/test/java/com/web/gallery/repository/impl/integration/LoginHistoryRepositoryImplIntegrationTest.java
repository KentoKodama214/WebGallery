package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Region;
import com.web.gallery.entity.LoginHistory;
import com.web.gallery.model.LoginHistoryModel;
import com.web.gallery.repository.impl.LoginHistoryRepositoryImpl;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class LoginHistoryRepositoryImplIntegrationTest {
  @Autowired private LoginHistoryRepositoryImpl loginHistoryRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<LoginHistory> getLoginHistoryByAccountNo(Long accountNo) {
    return jdbcTemplate.query(
        "SELECT * FROM common.login_history WHERE account_no = ? ORDER BY login_history_no",
        (rs, rowNum) ->
            LoginHistory.builder()
                .loginHistoryNo(rs.getLong("login_history_no"))
                .accountNo(rs.getLong("account_no"))
                .ipAddress(rs.getString("ip_address"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build(),
        accountNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/LoginHistoryRepositoryImplIntegrationTest.sql")
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：国・地域を解決できた場合、ログイン履歴を保存する")
    void save_success() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L),
              new IpAddress("203.0.113.1"),
              new IpGeoLocation(new Country("JP"), new Region("Tokyo")));

      loginHistoryRepositoryImpl.save(model);

      List<LoginHistory> actual = getLoginHistoryByAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals("203.0.113.1", actual.getFirst().getIpAddress());
      assertEquals("JP", actual.getFirst().getCountry());
      assertEquals("Tokyo", actual.getFirst().getRegion());
      // created_byはログイン試行者自身のアカウント番号
      assertEquals(1L, actual.getFirst().getCreatedBy());
      assertNotNull(actual.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：国・地域が未解決の場合、空文字で保存する（NOT NULL制約のため）")
    void save_success_with_empty_geo() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L), new IpAddress("198.51.100.99"), IpGeoLocation.empty());

      loginHistoryRepositoryImpl.save(model);

      List<LoginHistory> actual = getLoginHistoryByAccountNo(1L);
      assertEquals(1, actual.size());
      assertEquals("", actual.getFirst().getCountry());
      assertEquals("", actual.getFirst().getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/LoginHistoryRepositoryImplIntegrationTest.sql")
  class deleteByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号に該当するログイン履歴を削除する")
    void deleteByAccountNo_success() {
      // フィクスチャでaccount_no=2のログイン履歴が1件存在する
      assertEquals(1, getLoginHistoryByAccountNo(2L).size());

      loginHistoryRepositoryImpl.deleteByAccountNo(new AccountNo(2L));

      assertTrue(getLoginHistoryByAccountNo(2L).isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当するログイン履歴が存在しない場合もエラーにならない")
    void deleteByAccountNo_no_history() {
      assertDoesNotThrow(() -> loginHistoryRepositoryImpl.deleteByAccountNo(new AccountNo(999L)));
    }
  }
}
