package com.web.gallery.event;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountEventListenerTest {
  @InjectMocks private AccountEventListener accountEventListener;

  @Test
  @DisplayName("正常系：AccountRegisteredEventを受け取っても例外が発生しないこと")
  void handle_accountRegisteredEvent_success() {
    AccountRegisteredEvent event = new AccountRegisteredEvent(new AccountId("aaaaaaaa"));
    assertDoesNotThrow(() -> accountEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：AccountUpdatedEventを受け取っても例外が発生しないこと")
  void handle_accountUpdatedEvent_success() {
    AccountUpdatedEvent event =
        new AccountUpdatedEvent(
            new AccountNo(1L), new AccountId("aaaaaaaa"), new AccountId("aaaaaaaa"));
    assertDoesNotThrow(() -> accountEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：AccountDeletedEventを受け取っても例外が発生しないこと")
  void handle_accountDeletedEvent_success() {
    AccountDeletedEvent event =
        new AccountDeletedEvent(new AccountNo(1L), new AccountId("aaaaaaaa"));
    assertDoesNotThrow(() -> accountEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：AccountLockedEventを受け取っても例外が発生しないこと")
  void handle_accountLockedEvent_success() {
    AccountLockedEvent event = new AccountLockedEvent(new AccountNo(1L));
    assertDoesNotThrow(() -> accountEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：AccountUnlockedEventを受け取っても例外が発生しないこと")
  void handle_accountUnlockedEvent_success() {
    AccountUnlockedEvent event = new AccountUnlockedEvent(new AccountNo(1L));
    assertDoesNotThrow(() -> accountEventListener.handle(event));
  }
}
