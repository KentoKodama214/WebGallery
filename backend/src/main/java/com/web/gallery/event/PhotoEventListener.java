package com.web.gallery.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 写真に関するドメインイベントをハンドリングするリスナークラス<p>
 * 写真登録・更新・削除のログ集計を、Service層のビジネスロジックから疎結合に行う
 */
@Slf4j
@Component
public class PhotoEventListener {

	/**
	 * 写真の新規登録イベントをハンドリングする
	 *
	 * @param	event	{@link PhotoRegisteredEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PhotoRegisteredEvent event) {
		log.info("Photo registered (accountNo: {}, photoNo: {})", event.accountNo().value(), event.photoNo().value());
	}

	/**
	 * 写真の更新イベントをハンドリングする
	 *
	 * @param	event	{@link PhotoUpdatedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PhotoUpdatedEvent event) {
		log.info("Photo updated (accountNo: {}, photoNo: {})", event.accountNo().value(), event.photoNo().value());
	}

	/**
	 * 写真の削除イベントをハンドリングする
	 *
	 * @param	event	{@link PhotoDeletedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PhotoDeletedEvent event) {
		log.info("Photo deleted (accountNo: {}, photoNo: {})", event.accountNo().value(), event.photoNo().value());
	}
}
