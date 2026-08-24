package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;

/**
 * アカウントの強制ロック時に発行されるドメインイベント
 *
 * @param	accountNo	アカウント番号
 */
public record AccountLockedEvent(AccountNo accountNo) {
}
