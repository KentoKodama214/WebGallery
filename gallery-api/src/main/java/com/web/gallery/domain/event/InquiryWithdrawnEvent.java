package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryNo;

/**
 * お問い合わせ取り下げ時に発行されるドメインイベント
 *
 * @param accountNo お問い合わせ所有者のアカウント番号
 * @param inquiryNo お問い合わせ番号
 */
public record InquiryWithdrawnEvent(AccountNo accountNo, InquiryNo inquiryNo) {}
