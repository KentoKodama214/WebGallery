package com.web.gallery.event;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyNo;

/**
 * お問い合わせへの返信登録時に発行されるドメインイベント
 *
 * @param accountNo お問い合わせ所有者のアカウント番号
 * @param inquiryNo お問い合わせ番号
 * @param replyNo 返信番号
 */
public record InquiryRepliedEvent(AccountNo accountNo, InquiryNo inquiryNo, ReplyNo replyNo) {}
