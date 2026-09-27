package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;

/**
 * 写真の削除時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 * @param photoNo 写真番号
 */
public record PhotoDeletedEvent(AccountNo accountNo, PhotoNo photoNo) {}
