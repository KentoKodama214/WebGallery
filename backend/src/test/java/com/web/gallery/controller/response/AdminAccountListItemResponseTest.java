package com.web.gallery.controller.response;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.model.AccountModel;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminAccountListItemResponseTest {

  private AccountModel.AccountModelBuilder baseBuilder() {
    return AccountModel.builder()
        .accountNo(new AccountNo(1L))
        .accountId(new AccountId("testuser01"))
        .accountName(new AccountName("テストユーザー"))
        .authorityKbn(AuthorityEnum.NORMAL)
        .isDeleted(new IsDeleted(false));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：最終ログイン日時・ログイン失敗回数が設定されている場合、それぞれの値が設定されること")
    void from_withValue() {
      OffsetDateTime lastLoginDatetime = OffsetDateTime.now();
      AccountModel model =
          baseBuilder()
              .lastLoginDatetime(new LastLoginDatetime(lastLoginDatetime))
              .loginFailureCount(new LoginFailureCount(2))
              .build();

      AdminAccountListItemResponse actual = AdminAccountListItemResponse.from(model);

      assertEquals(lastLoginDatetime, actual.getLastLoginDatetime());
      assertEquals(2, actual.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：最終ログイン日時・ログイン失敗回数が未設定の場合、それぞれnullが設定されること")
    void from_withoutValue() {
      AccountModel model = baseBuilder().lastLoginDatetime(null).loginFailureCount(null).build();

      AdminAccountListItemResponse actual = AdminAccountListItemResponse.from(model);

      assertNull(actual.getLastLoginDatetime());
      assertNull(actual.getLoginFailureCount());
    }
  }
}
