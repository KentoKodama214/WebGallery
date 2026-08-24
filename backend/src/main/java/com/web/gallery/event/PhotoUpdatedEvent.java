package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;

/**
 * 写真の更新時に発行されるドメインイベント
 *
 * @param	accountNo	アカウント番号
 * @param	photoNo		写真番号
 */
public record PhotoUpdatedEvent(AccountNo accountNo, PhotoNo photoNo) {
}
