package com.web.gallery.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * アカウントに関するドメインイベントをハンドリングするリスナークラス<p>
 * アカウント登録・削除のログ集計を、Service層のビジネスロジックから疎結合に行う
 */
@Slf4j
@Component
public class AccountEventListener {

	/**
	 * アカウントの新規登録イベントをハンドリングする
	 *
	 * @param	event	{@link AccountRegisteredEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(AccountRegisteredEvent event) {
		log.info("Account registered (accountId: {})", event.accountId().value());
	}

	/**
	 * アカウントの削除イベントをハンドリングする
	 *
	 * @param	event	{@link AccountDeletedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(AccountDeletedEvent event) {
		log.info("Account deleted (accountNo: {}, accountId: {})", event.accountNo().value(), event.accountId().value());
	}
}
