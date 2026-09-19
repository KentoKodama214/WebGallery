package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AccountTest {

  @Mock private PasswordEncoder passwordEncoder;

  private AccountModel.AccountModelBuilder baseBuilder() {
    return AccountModel.builder()
        .accountId(new AccountId("testuser01"))
        .accountName(new AccountName("テストユーザー"))
        .password(new Password("plainPassword"));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      AccountModel model =
          baseBuilder()
              .birthdate(new BirthDate(LocalDate.of(1990, 1, 1)))
              .sexKbn(SexEnum.MAN)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
              .freeMemo(new FreeMemo("memo"))
              .build();
      doReturn("encodedPassword").when(passwordEncoder).encode("plainPassword");

      Account actual = Account.from(model, passwordEncoder, 1L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(0L, actual.getCreatedBy());
      assertEquals(0L, actual.getUpdatedBy());
      assertEquals("testuser01", actual.getAccountId());
      assertEquals("テストユーザー", actual.getAccountName());
      assertEquals("encodedPassword", actual.getPassword());
      assertEquals(LocalDate.of(1990, 1, 1), actual.getBirthdate());
      assertEquals(SexEnum.MAN, actual.getSexKbn());
      assertEquals("Hokkaido", actual.getBirthplacePrefectureKbnCode());
      assertEquals("Tokyo", actual.getResidentPrefectureKbnCode());
      assertEquals("memo", actual.getFreeMemo());
      assertEquals(Consts.MIN_OFFSET_DATE_TIME, actual.getLastLoginDatetime());
      assertEquals(0, actual.getLoginFailureCount());
      assertFalse(actual.getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未設定項目のみの場合、デフォルト値が反映されること")
    void from_allFieldsAbsent() {
      AccountModel model = baseBuilder().build();
      doReturn("encodedPassword").when(passwordEncoder).encode("plainPassword");

      Account actual = Account.from(model, passwordEncoder, 1L);

      assertEquals(Consts.MIN_LOCAL_DATE, actual.getBirthdate());
      assertEquals(SexEnum.NONE, actual.getSexKbn());
      assertEquals(Consts.STRING_NONE, actual.getBirthplacePrefectureKbnCode());
      assertEquals(Consts.STRING_NONE, actual.getResidentPrefectureKbnCode());
      assertEquals(Consts.STRING_EMPTY, actual.getFreeMemo());
    }
  }
}
