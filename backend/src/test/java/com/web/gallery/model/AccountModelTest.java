package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.AccountUpdateRequest;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.enumeration.SexEnum;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromAccountUpdateRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      AccountUpdateRequest request = new AccountUpdateRequest();
      request.setAccountId("testuser01");
      request.setAccountName("テストユーザー");
      request.setNewPassword("newPassword01");
      request.setBirthdate(LocalDate.of(1990, 1, 1));
      request.setSexKbn(SexEnum.MAN);
      request.setBirthplacePrefectureKbnCode("Hokkaido");
      request.setResidentPrefectureKbnCode("Tokyo");
      request.setFreeMemo("よろしくお願いします");

      AccountModel actual = AccountModel.from(request, 1L);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("testuser01"), actual.getAccountId());
      assertEquals(new AccountName("テストユーザー"), actual.getAccountName());
      assertEquals(new Password("newPassword01"), actual.getPassword());
      assertEquals(new BirthDate(LocalDate.of(1990, 1, 1)), actual.getBirthdate());
      assertEquals(SexEnum.MAN, actual.getSexKbn());
      assertEquals(
          new BirthplacePrefectureKbnCode("Hokkaido"), actual.getBirthplacePrefectureKbnCode());
      assertEquals(new ResidentPrefectureKbnCode("Tokyo"), actual.getResidentPrefectureKbnCode());
      assertEquals(new FreeMemo("よろしくお願いします"), actual.getFreeMemo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：新しいパスワードが空文字の場合、パスワードは変更しない（null）こと")
    void from_newPasswordEmpty() {
      AccountUpdateRequest request = new AccountUpdateRequest();
      request.setAccountId("testuser01");
      request.setAccountName("テストユーザー");
      request.setNewPassword("");

      AccountModel actual = AccountModel.from(request, 1L);

      assertNull(actual.getPassword());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：新しいパスワードがnullの場合、パスワードは変更しない（null）こと")
    void from_newPasswordNull() {
      AccountUpdateRequest request = new AccountUpdateRequest();
      request.setAccountId("testuser01");
      request.setAccountName("テストユーザー");
      request.setNewPassword(null);

      AccountModel actual = AccountModel.from(request, 1L);

      assertNull(actual.getPassword());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：任意項目が未設定の場合、nullが設定されること")
    void from_optionalFieldsNull() {
      AccountUpdateRequest request = new AccountUpdateRequest();
      request.setAccountId("testuser01");
      request.setAccountName("テストユーザー");
      request.setNewPassword(null);
      request.setBirthdate(null);
      request.setBirthplacePrefectureKbnCode(null);
      request.setResidentPrefectureKbnCode(null);
      request.setFreeMemo(null);

      AccountModel actual = AccountModel.from(request, 1L);

      assertNull(actual.getBirthdate());
      assertNull(actual.getBirthplacePrefectureKbnCode());
      assertNull(actual.getResidentPrefectureKbnCode());
      assertNull(actual.getFreeMemo());
    }
  }
}
