package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountId;

/**
 * アカウントの新規登録時に発行されるドメインイベント
 *
 * @param accountId アカウントID
 */
public record AccountRegisteredEvent(AccountId accountId) {}
