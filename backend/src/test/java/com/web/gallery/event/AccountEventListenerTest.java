package com.web.gallery.event;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountEventListenerTest {
  @InjectMocks private AccountEventListener accountEventListener;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountRegisteredEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountRegisteredEventを受け取っても例外が発生しないこと")
    void handle_accountRegisteredEvent_success() {
      AccountRegisteredEvent event = new AccountRegisteredEvent(new AccountId("aaaaaaaa"));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountUpdatedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountUpdatedEventを受け取っても例外が発生しないこと")
    void handle_accountUpdatedEvent_success() {
      AccountUpdatedEvent event =
          new AccountUpdatedEvent(
              new AccountNo(1L), new AccountId("aaaaaaaa"), new AccountId("aaaaaaaa"));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountDeletedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountDeletedEventを受け取っても例外が発生しないこと")
    void handle_accountDeletedEvent_success() {
      AccountDeletedEvent event =
          new AccountDeletedEvent(new AccountNo(1L), new AccountId("aaaaaaaa"));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountLockedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountLockedEventを受け取っても例外が発生しないこと")
    void handle_accountLockedEvent_success() {
      AccountLockedEvent event = new AccountLockedEvent(new AccountNo(1L));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountUnlockedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountUnlockedEventを受け取っても例外が発生しないこと")
    void handle_accountUnlockedEvent_success() {
      AccountUnlockedEvent event = new AccountUnlockedEvent(new AccountNo(1L));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleAccountAuthorityChangedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountAuthorityChangedEventを受け取っても例外が発生しないこと")
    void handle_accountAuthorityChangedEvent_success() {
      AccountAuthorityChangedEvent event = new AccountAuthorityChangedEvent(new AccountNo(1L));
      assertDoesNotThrow(() -> accountEventListener.handle(event));
    }
  }
}
