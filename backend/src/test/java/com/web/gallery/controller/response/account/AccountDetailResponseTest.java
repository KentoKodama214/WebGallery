package com.web.gallery.controller.response.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.account.AccountModel;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountDetailResponseTest {

  private AccountModel.AccountModelBuilder baseBuilder() {
    return AccountModel.builder()
        .accountId(new AccountId("testuser01"))
        .accountName(new AccountName("テストユーザー"))
        .sexKbn(SexEnum.MAN)
        .isDeleted(new IsDeleted(false));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class birthdate {
      @Test
      @Order(1)
      @DisplayName("正常系：実際の生年月日が設定されている場合、その値が設定されること")
      void birthdate_actualValue() {
        AccountModel model =
            baseBuilder().birthdate(new BirthDate(LocalDate.of(1990, 1, 1))).build();

        AccountDetailResponse actual = AccountDetailResponse.from(model);

        assertEquals(LocalDate.of(1990, 1, 1), actual.getBirthdate());
      }

      @Test
      @Order(2)
      @DisplayName("異常系：生年月日が未設定の場合、nullが設定されること")
      void birthdate_null() {
        AccountModel model = baseBuilder().birthdate(null).build();

        AccountDetailResponse actual = AccountDetailResponse.from(model);

        assertNull(actual.getBirthdate());
      }

      @Test
      @Order(3)
      @DisplayName("異常系：生年月日がデフォルト値（未設定を表す番兵値）の場合、nullが設定されること")
      void birthdate_defaultValue() {
        AccountModel model = baseBuilder().birthdate(new BirthDate(Consts.MIN_LOCAL_DATE)).build();

        AccountDetailResponse actual = AccountDetailResponse.from(model);

        assertNull(actual.getBirthdate());
      }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class nullableFields {
      @Test
      @Order(1)
      @DisplayName("正常系：出身・在住都道府県区分コード、フリーメモが設定されている場合、それぞれの値が設定されること")
      void nullableFields_withValue() {
        AccountModel model =
            baseBuilder()
                .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
                .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
                .freeMemo(new FreeMemo("メモ"))
                .build();

        AccountDetailResponse actual = AccountDetailResponse.from(model);

        assertEquals("Hokkaido", actual.getBirthplacePrefectureKbnCode());
        assertEquals("Tokyo", actual.getResidentPrefectureKbnCode());
        assertEquals("メモ", actual.getFreeMemo());
      }

      @Test
      @Order(2)
      @DisplayName("異常系：出身・在住都道府県区分コード、フリーメモが未設定の場合、それぞれnullが設定されること")
      void nullableFields_withoutValue() {
        AccountModel model =
            baseBuilder()
                .birthplacePrefectureKbnCode(null)
                .residentPrefectureKbnCode(null)
                .freeMemo(null)
                .build();

        AccountDetailResponse actual = AccountDetailResponse.from(model);

        assertNull(actual.getBirthplacePrefectureKbnCode());
        assertNull(actual.getResidentPrefectureKbnCode());
        assertNull(actual.getFreeMemo());
      }
    }
  }
}
