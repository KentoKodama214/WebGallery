package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;

/**
 * アカウントの権限変更時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 */
public record AccountAuthorityChangedEvent(AccountNo accountNo) {}
