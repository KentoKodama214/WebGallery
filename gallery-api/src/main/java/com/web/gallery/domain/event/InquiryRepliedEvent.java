package com.web.gallery.domain.event;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.domain.model.inquiry.ReplyNo;

/**
 * お問い合わせへの返信登録時に発行されるドメインイベント
 *
 * @param accountNo お問い合わせ所有者のアカウント番号
 * @param inquiryNo お問い合わせ番号
 * @param replyNo 返信番号
 */
public record InquiryRepliedEvent(AccountNo accountNo, InquiryNo inquiryNo, ReplyNo replyNo) {}
