package com.web.gallery.infrastructure.persistence.repository.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCodeCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.RecoveryCodeMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RecoveryCodeRepositoryImplTest {

  @InjectMocks private RecoveryCodeRepositoryImpl recoveryCodeRepositoryImpl;

  @Mock private RecoveryCodeMapper recoveryCodeMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class saveAll {
    @Test
    @Order(1)
    @DisplayName("正常系：平文ではなくハッシュに変換してまとめて登録すること")
    @SuppressWarnings("unchecked")
    void saveAll_hashed() {
      RecoveryCode first = new RecoveryCode("ABCDEFGHJKMNPQRS");
      RecoveryCode second = new RecoveryCode("0123456789ABCDEF");

      recoveryCodeRepositoryImpl.saveAll(
          new AccountNo(1L), RecoveryCodeList.of(List.of(first, second)));

      ArgumentCaptor<List<com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCode>>
          captor = ArgumentCaptor.forClass(List.class);
      verify(recoveryCodeMapper).bulkInsert(captor.capture());
      assertEquals(2, captor.getValue().size());
      assertEquals(1L, captor.getValue().getFirst().getAccountNo());
      assertEquals(first.toHash().value(), captor.getValue().getFirst().getCodeHash());
      assertEquals(1L, captor.getValue().getFirst().getCreatedBy());
      assertEquals(second.toHash().value(), captor.getValue().get(1).getCodeHash());
      assertNotEquals(first.value(), captor.getValue().getFirst().getCodeHash());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空の場合、Mapperを呼び出さないこと（VALUES句が空のINSERTを発行しない）")
    void saveAll_empty() {
      recoveryCodeRepositoryImpl.saveAll(new AccountNo(1L), RecoveryCodeList.empty());

      verifyNoInteractions(recoveryCodeMapper);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号のみを条件に削除すること")
    void deleteByAccountNo_success() {
      recoveryCodeRepositoryImpl.deleteByAccountNo(new AccountNo(1L));

      ArgumentCaptor<RecoveryCodeCondition> captor =
          ArgumentCaptor.forClass(RecoveryCodeCondition.class);
      verify(recoveryCodeMapper).delete(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertNull(captor.getValue().getCodeHash());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consume {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号と入力コードのハッシュを条件に削除し、1件削除できればtrueを返すこと")
    void consume_matched() {
      doReturn(1).when(recoveryCodeMapper).delete(any());
      RecoveryCode code = new RecoveryCode("abcd-efgh-jkmn-pqrs");

      assertTrue(recoveryCodeRepositoryImpl.consume(new AccountNo(1L), code));

      ArgumentCaptor<RecoveryCodeCondition> captor =
          ArgumentCaptor.forClass(RecoveryCodeCondition.class);
      verify(recoveryCodeMapper).delete(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(code.toHash().value(), captor.getValue().getCodeHash());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：一致するコードが無い（誤り・使用済み）場合、falseを返すこと")
    void consume_notMatched() {
      doReturn(0).when(recoveryCodeMapper).delete(any());

      assertFalse(
          recoveryCodeRepositoryImpl.consume(
              new AccountNo(1L), new RecoveryCode("ABCDEFGHJKMNPQRS")));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class countByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号を条件に件数を返すこと")
    void countByAccountNo_success() {
      doReturn(7).when(recoveryCodeMapper).count(any());

      assertEquals(7, recoveryCodeRepositoryImpl.countByAccountNo(new AccountNo(1L)));

      ArgumentCaptor<RecoveryCodeCondition> captor =
          ArgumentCaptor.forClass(RecoveryCodeCondition.class);
      verify(recoveryCodeMapper).count(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
    }
  }
}
