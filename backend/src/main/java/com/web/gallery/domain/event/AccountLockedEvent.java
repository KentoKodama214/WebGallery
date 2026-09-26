package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountNo;

/**
 * アカウントの強制ロック時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 */
public record AccountLockedEvent(AccountNo accountNo) {}
