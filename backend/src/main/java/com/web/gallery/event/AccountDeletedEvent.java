package com.web.gallery.event;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;

/**
 * アカウントの削除時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 * @param accountId アカウントID
 */
public record AccountDeletedEvent(AccountNo accountNo, AccountId accountId) {}
