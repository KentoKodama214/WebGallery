package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;

/**
 * お問い合わせ取り下げ時に発行されるドメインイベント
 *
 * @param accountNo お問い合わせ所有者のアカウント番号
 * @param inquiryNo お問い合わせ番号
 */
public record InquiryWithdrawnEvent(AccountNo accountNo, InquiryNo inquiryNo) {}
