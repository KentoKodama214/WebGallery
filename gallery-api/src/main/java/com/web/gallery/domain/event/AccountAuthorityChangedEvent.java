package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountNo;

/**
 * アカウントの権限変更時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 */
public record AccountAuthorityChangedEvent(AccountNo accountNo) {}
