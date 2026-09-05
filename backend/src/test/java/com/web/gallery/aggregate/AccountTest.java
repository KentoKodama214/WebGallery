package com.web.gallery.aggregate;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoNoList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class AccountTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forDelete {
    @Test
    @Order(1)
    @DisplayName("正常系：削除済みとしてマークされること")
    void forDelete_success() {
      AccountNo accountNo = new AccountNo(1L);

      Account account = Account.forDelete(accountNo);

      assertEquals(accountNo, account.getAccountNo());
      assertTrue(account.isDeleted());
      assertNull(account.getDeletedPhotoNoList());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class recordDeletedPhotoNos {
    @Test
    @Order(1)
    @DisplayName("正常系：削除された写真番号一覧が記録されること")
    void recordDeletedPhotoNos_success() {
      Account account = Account.forDelete(new AccountNo(1L));
      PhotoNoList deletedPhotoNoList = PhotoNoList.of(List.of(new PhotoNo(1L), new PhotoNo(2L)));

      account.recordDeletedPhotoNos(deletedPhotoNoList);

      assertEquals(deletedPhotoNoList, account.getDeletedPhotoNoList());
    }
  }
}
