package com.web.gallery.event;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;

/**
 * アカウントの更新時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 * @param accountId 更新後のアカウントID
 * @param previousAccountId 更新前のアカウントID（アカウントID未変更時は{@code accountId}と同値。取得できなかった場合はnull可）
 */
public record AccountUpdatedEvent(
    AccountNo accountNo, AccountId accountId, AccountId previousAccountId) {}
