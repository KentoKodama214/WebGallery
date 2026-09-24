package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;

/**
 * お問い合わせの新規登録時に発行されるドメインイベント
 *
 * @param accountNo アカウント番号
 * @param inquiryNo お問い合わせ番号
 */
public record InquiryRegisteredEvent(AccountNo accountNo, InquiryNo inquiryNo) {}
