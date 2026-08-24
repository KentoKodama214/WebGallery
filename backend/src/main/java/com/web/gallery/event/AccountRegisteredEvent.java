package com.web.gallery.event;

import com.web.gallery.domain.account.AccountId;

/**
 * アカウントの新規登録時に発行されるドメインイベント
 *
 * @param	accountId	アカウントID
 */
public record AccountRegisteredEvent(AccountId accountId) {
}
