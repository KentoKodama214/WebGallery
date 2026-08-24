package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;

/**
 * アカウントのロック解除時に発行されるドメインイベント
 *
 * @param	accountNo	アカウント番号
 */
public record AccountUnlockedEvent(AccountNo accountNo) {
}
