package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.AccountAuthority;
import com.web.gallery.entity.AccountAuthorityCondition;
import com.web.gallery.entity.AccountAuthorityUpdateTarget;
import com.web.gallery.enumeration.AuthorityEnum;
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
public class AccountAuthorityMapperTest {
  @Autowired private AccountAuthorityMapper accountAuthorityMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<AccountAuthority> getAccountAuthorityList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM common.account_authority WHERE " + condition,
        (rs, rowNum) ->
            AccountAuthority.builder()
                .accountNo(rs.getLong("account_no"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedBy(rs.getLong("updated_by"))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
                .build());
  }

  @Nested
  @Order(1)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/AccountAuthorityMapperTest.sql")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功")
    void insert_success() {
      AccountAuthority insertAccountAuthority =
          AccountAuthority.builder()
              .accountNo(1L)
              .createdBy(1L)
              .updatedBy(1L)
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = accountAuthorityMapper.insert(insertAccountAuthority);
      assertEquals(1, actualCount);

      List<AccountAuthority> actualData = getAccountAuthorityList("account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
    }
  }

  @Nested
  @Order(2)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/AccountAuthorityMapperTest.sql")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：権限区分が更新されること")
    void update_success() {
      AccountAuthorityCondition condition = AccountAuthorityCondition.byAccountNo(2L);
      AccountAuthorityUpdateTarget target =
          AccountAuthorityUpdateTarget.builder()
              .updatedBy(9L)
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = accountAuthorityMapper.update(condition, target);
      assertEquals(1, actualCount);

      List<AccountAuthority> actualData = getAccountAuthorityList("account_no=2");
      assertEquals(1, actualData.size());
      assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：更新対象のレコードなし")
    void update_not_found() {
      AccountAuthorityCondition condition = AccountAuthorityCondition.byAccountNo(1L);
      AccountAuthorityUpdateTarget target =
          AccountAuthorityUpdateTarget.builder()
              .updatedBy(9L)
              .authorityKbn(AuthorityEnum.MINI)
              .build();

      Integer actualCount = accountAuthorityMapper.update(condition, target);
      assertEquals(0, actualCount);
    }
  }

  @Nested
  @Order(3)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/AccountAuthorityMapperTest.sql")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号でのdelete")
    void delete_by_accountNo() {
      AccountAuthorityCondition deleteAccountAuthority =
          AccountAuthorityCondition.builder().accountNo(2L).build();
      Integer actual = accountAuthorityMapper.delete(deleteAccountAuthority);
      assertEquals(1, actual);

      List<AccountAuthority> actualData = getAccountAuthorityList("account_no=2");
      assertEquals(0, actualData.size());

      List<AccountAuthority> actualRestData = getAccountAuthorityList("account_no<>2");
      assertEquals(1, actualRestData.size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除対象のレコードなし")
    void delete_not_found() {
      AccountAuthorityCondition deleteAccountAuthority =
          AccountAuthorityCondition.builder().accountNo(1L).build();
      Integer actual = accountAuthorityMapper.delete(deleteAccountAuthority);
      assertEquals(0, actual);

      List<AccountAuthority> actualRestData = getAccountAuthorityList("account_no<>1");
      assertEquals(2, actualRestData.size());
    }
  }
}
