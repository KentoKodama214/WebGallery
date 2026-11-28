package com.web.gallery.infrastructure.persistence.repository.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandleCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.PasskeyUserHandleMapper;
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
class PasskeyUserHandleRepositoryImplTest {

  @InjectMocks private PasskeyUserHandleRepositoryImpl passkeyUserHandleRepositoryImpl;

  @Mock private PasskeyUserHandleMapper passkeyUserHandleMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class findByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号を条件に取得し、ユーザーハンドルを返すこと")
    void findByAccountNo_found() {
      doReturn(
              List.of(
                  com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle.of(
                      1L, "handle-1")))
          .when(passkeyUserHandleMapper)
          .select(any());

      assertEquals(
          new PasskeyUserHandle("handle-1"),
          passkeyUserHandleRepositoryImpl.findByAccountNo(new AccountNo(1L)));

      ArgumentCaptor<PasskeyUserHandleCondition> captor =
          ArgumentCaptor.forClass(PasskeyUserHandleCondition.class);
      verify(passkeyUserHandleMapper).select(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未発行の場合、nullを返すこと")
    void findByAccountNo_notFound() {
      doReturn(List.of()).when(passkeyUserHandleMapper).select(any());

      assertNull(passkeyUserHandleRepositoryImpl.findByAccountNo(new AccountNo(1L)));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class saveIfAbsent {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ユーザーハンドル・作成者を設定して登録すること")
    void saveIfAbsent_success() {
      passkeyUserHandleRepositoryImpl.saveIfAbsent(
          new AccountNo(1L), new PasskeyUserHandle("handle-1"));

      ArgumentCaptor<com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle>
          captor =
              ArgumentCaptor.forClass(
                  com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle.class);
      verify(passkeyUserHandleMapper).insertIfAbsent(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals("handle-1", captor.getValue().getUserHandle());
      assertEquals(1L, captor.getValue().getCreatedBy());
    }
  }
}
