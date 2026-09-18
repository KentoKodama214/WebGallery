package com.web.gallery.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * お問い合わせに関するドメインイベントをハンドリングするリスナークラス
 *
 * <p>お問い合わせ登録・返信のログ集計を、Service層のビジネスロジックから疎結合に行う
 */
@Slf4j
@Component
public class InquiryEventListener {

  /**
   * お問い合わせの新規登録イベントをハンドリングする
   *
   * @param event {@link InquiryRegisteredEvent}
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(InquiryRegisteredEvent event) {
    log.info(
        "Inquiry registered (accountNo: {}, inquiryNo: {})",
        event.accountNo().value(),
        event.inquiryNo().value());
  }

  /**
   * お問い合わせへの返信登録イベントをハンドリングする
   *
   * @param event {@link InquiryRepliedEvent}
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(InquiryRepliedEvent event) {
    log.info(
        "Inquiry replied (accountNo: {}, inquiryNo: {}, replyNo: {})",
        event.accountNo().value(),
        event.inquiryNo().value(),
        event.replyNo().value());
  }

  /**
   * お問い合わせ取り下げイベントをハンドリングする
   *
   * @param event {@link InquiryWithdrawnEvent}
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(InquiryWithdrawnEvent event) {
    log.info(
        "Inquiry withdrawn (accountNo: {}, inquiryNo: {})",
        event.accountNo().value(),
        event.inquiryNo().value());
  }
}
