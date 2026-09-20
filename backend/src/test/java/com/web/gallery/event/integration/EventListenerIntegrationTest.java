package com.web.gallery.event.integration;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.event.AccountAuthorityChangedEvent;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountEventListener;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountRegisteredEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.event.InquiryEventListener;
import com.web.gallery.event.InquiryRegisteredEvent;
import com.web.gallery.event.InquiryRepliedEvent;
import com.web.gallery.event.InquiryWithdrawnEvent;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.event.PhotoEventListener;
import com.web.gallery.event.PhotoRegisteredEvent;
import com.web.gallery.event.PhotoUpdatedEvent;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AccountEventListener}・{@link PhotoEventListener}・{@link InquiryEventListener}の統合テスト
 *
 * <p>これらは{@code @TransactionalEventListener(phase = AFTER_COMMIT)}で実装されており、他の統合テストが前提とする
 * {@code @Transactional}の自動ロールバックの下では（トランザクションが物理コミットされないため）一切発火しない。 そのため本クラスでは、イベント発行後に{@link
 * TestTransaction}で意図的に物理コミットし、 リスナーが実際にAFTER_COMMITのタイミングで実行されることをログ出力で検証する。
 * DBへ実際の行を作らないため、コミットしても他の行に影響しない
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class EventListenerIntegrationTest {
  @Autowired private ApplicationEventPublisher applicationEventPublisher;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger targetLogger;

  /** 指定したリスナークラスのロガーにアペンダーを追加し、ログ出力の捕捉を開始する */
  private void startCapture(Class<?> loggerClass) {
    targetLogger = (Logger) LoggerFactory.getLogger(loggerClass);
    listAppender = new ListAppender<>();
    listAppender.start();
    targetLogger.addAppender(listAppender);
  }

  /** イベントを発行し、テストトランザクションを意図的に物理コミットしてAFTER_COMMITリスナーを発火させる */
  private void publishAndCommit(Object event) {
    applicationEventPublisher.publishEvent(event);
    TestTransaction.flagForCommit();
    TestTransaction.end();
  }

  private List<String> capturedMessages() {
    return listAppender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
  }

  @AfterEach
  void tearDown() {
    if (targetLogger != null) {
      targetLogger.detachAppender(listAppender);
    }
    // publishAndCommitで物理コミット済みの場合に備え、
    // クラスレベルの@Transactionalが後処理でロールバックできるよう新しいトランザクションを開始しておく
    if (!TestTransaction.isActive()) {
      TestTransaction.start();
    }
    TestTransaction.flagForRollback();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class accountEventListener {
    @Test
    @Order(1)
    @DisplayName("正常系：AccountRegisteredEventのコミット後、handleが実行されログ出力されること")
    void handle_accountRegistered() {
      startCapture(AccountEventListener.class);

      publishAndCommit(new AccountRegisteredEvent(new AccountId("aaaaaaaa")));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Account registered")));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：AccountUpdatedEventのコミット後、handleが実行されログ出力されること")
    void handle_accountUpdated() {
      startCapture(AccountEventListener.class);

      publishAndCommit(
          new AccountUpdatedEvent(
              new AccountNo(1L), new AccountId("aaaaaaaa"), new AccountId("bbbbbbbb")));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Account updated")));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：AccountDeletedEventのコミット後、handleが実行されログ出力されること")
    void handle_accountDeleted() {
      startCapture(AccountEventListener.class);

      publishAndCommit(new AccountDeletedEvent(new AccountNo(1L), new AccountId("aaaaaaaa")));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Account deleted")));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：AccountLockedEventのコミット後、handleが実行されログ出力されること")
    void handle_accountLocked() {
      startCapture(AccountEventListener.class);

      publishAndCommit(new AccountLockedEvent(new AccountNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Account locked")));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：AccountUnlockedEventのコミット後、handleが実行されログ出力されること")
    void handle_accountUnlocked() {
      startCapture(AccountEventListener.class);

      publishAndCommit(new AccountUnlockedEvent(new AccountNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Account unlocked")));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：AccountAuthorityChangedEventのコミット後、handleが実行されログ出力されること")
    void handle_accountAuthorityChanged() {
      startCapture(AccountEventListener.class);

      publishAndCommit(new AccountAuthorityChangedEvent(new AccountNo(1L)));

      assertTrue(
          capturedMessages().stream().anyMatch(m -> m.contains("Account authority changed")));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class photoEventListener {
    @Test
    @Order(1)
    @DisplayName("正常系：PhotoRegisteredEventのコミット後、handleが実行されログ出力されること")
    void handle_photoRegistered() {
      startCapture(PhotoEventListener.class);

      publishAndCommit(new PhotoRegisteredEvent(new AccountNo(1L), new PhotoNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Photo registered")));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：PhotoUpdatedEventのコミット後、handleが実行されログ出力されること")
    void handle_photoUpdated() {
      startCapture(PhotoEventListener.class);

      publishAndCommit(new PhotoUpdatedEvent(new AccountNo(1L), new PhotoNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Photo updated")));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：PhotoDeletedEventのコミット後、handleが実行されログ出力されること")
    void handle_photoDeleted() {
      startCapture(PhotoEventListener.class);

      publishAndCommit(new PhotoDeletedEvent(new AccountNo(1L), new PhotoNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Photo deleted")));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class inquiryEventListener {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryRegisteredEventのコミット後、handleが実行されログ出力されること")
    void handle_inquiryRegistered() {
      startCapture(InquiryEventListener.class);

      publishAndCommit(new InquiryRegisteredEvent(new AccountNo(1L), new InquiryNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Inquiry registered")));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：InquiryRepliedEventのコミット後、handleが実行されログ出力されること")
    void handle_inquiryReplied() {
      startCapture(InquiryEventListener.class);

      publishAndCommit(
          new InquiryRepliedEvent(new AccountNo(1L), new InquiryNo(1L), new ReplyNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Inquiry replied")));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：InquiryWithdrawnEventのコミット後、handleが実行されログ出力されること")
    void handle_inquiryWithdrawn() {
      startCapture(InquiryEventListener.class);

      publishAndCommit(new InquiryWithdrawnEvent(new AccountNo(1L), new InquiryNo(1L)));

      assertTrue(capturedMessages().stream().anyMatch(m -> m.contains("Inquiry withdrawn")));
    }
  }
}
