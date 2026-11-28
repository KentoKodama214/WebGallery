package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountNo;

/**
 * アカウントの削除時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 * @param accountId アカウントID
 */
public record AccountDeletedEvent(AccountNo accountNo, AccountId accountId) {}
