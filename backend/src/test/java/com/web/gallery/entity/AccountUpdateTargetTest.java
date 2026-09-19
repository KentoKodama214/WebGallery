package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.IsAdminLocked;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
class AccountUpdateTargetTest {

  @Mock private PasswordEncoder passwordEncoder;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void fromForUpdate_allFieldsPresent() {
      AccountModel model =
          AccountModel.builder()
              .accountId(new AccountId("testuser01"))
              .accountName(new AccountName("テストユーザー"))
              .password(new Password("plainPassword"))
              .birthdate(new BirthDate(LocalDate.of(1990, 1, 1)))
              .sexKbn(SexEnum.MAN)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
              .freeMemo(new FreeMemo("memo"))
              .lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.now()))
              .loginFailureCount(new LoginFailureCount(2))
              .build();
      doReturn("encodedPassword").when(passwordEncoder).encode("plainPassword");

      AccountUpdateTarget actual = AccountUpdateTarget.fromForUpdate(model, passwordEncoder);

      assertEquals("testuser01", actual.getAccountId());
      assertEquals("テストユーザー", actual.getAccountName());
      assertEquals("encodedPassword", actual.getPassword());
      assertEquals(LocalDate.of(1990, 1, 1), actual.getBirthdate());
      assertEquals(SexEnum.MAN, actual.getSexKbn());
      assertEquals("Hokkaido", actual.getBirthplacePrefectureKbnCode());
      assertEquals("Tokyo", actual.getResidentPrefectureKbnCode());
      assertEquals("memo", actual.getFreeMemo());
      assertNotNull(actual.getLastLoginDatetime());
      assertEquals(2, actual.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未設定項目のみの場合、nullのままでパスワードは更新されないこと")
    void fromForUpdate_allFieldsAbsent() {
      AccountModel model = AccountModel.builder().build();

      AccountUpdateTarget actual = AccountUpdateTarget.fromForUpdate(model, passwordEncoder);

      assertNull(actual.getAccountId());
      assertNull(actual.getAccountName());
      assertNull(actual.getPassword());
      assertNull(actual.getBirthdate());
      assertNull(actual.getBirthplacePrefectureKbnCode());
      assertNull(actual.getResidentPrefectureKbnCode());
      assertNull(actual.getFreeMemo());
      assertNull(actual.getLastLoginDatetime());
      assertNull(actual.getLoginFailureCount());
      verify(passwordEncoder, never()).encode(anyString());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForUpdateLoginFailure {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void fromForUpdateLoginFailure_allFieldsPresent() {
      AccountModel model =
          AccountModel.builder()
              .lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.now()))
              .loginFailureCount(new LoginFailureCount(3))
              .isAdminLocked(new IsAdminLocked(true))
              .build();

      AccountUpdateTarget actual = AccountUpdateTarget.fromForUpdateLoginFailure(model);

      assertNotNull(actual.getLastLoginDatetime());
      assertEquals(3, actual.getLoginFailureCount());
      assertTrue(actual.getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未設定項目のみの場合、ログイン失敗回数は0、その他はnullになること")
    void fromForUpdateLoginFailure_allFieldsAbsent() {
      AccountModel model = AccountModel.builder().build();

      AccountUpdateTarget actual = AccountUpdateTarget.fromForUpdateLoginFailure(model);

      assertNull(actual.getLastLoginDatetime());
      assertEquals(0, actual.getLoginFailureCount());
      assertNull(actual.getIsAdminLocked());
    }
  }
}
