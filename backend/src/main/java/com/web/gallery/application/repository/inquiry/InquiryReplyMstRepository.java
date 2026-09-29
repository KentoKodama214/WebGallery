package com.web.gallery.application.repository.inquiry;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyNo;

/** お問い合わせ返信マスタデータを永続化するRepositoryクラス */
public interface InquiryReplyMstRepository {
  /**
   * お問い合わせIDから新しい返信番号を発番する
   *
   * @param inquiryId ID
   * @return 新規採番した返信番号
   */
  ReplyNo getNewReplyNo(InquiryId inquiryId);

  /**
   * 指定アカウントが管理者として投稿した返信が存在するかどうかを判定する
   *
   * <p>アカウント削除の可否判定に用いる。返信を巻き込んで削除すると無関係な第三者のお問い合わせから 回答本文だけが消えてしまうため、1件でも存在する場合は削除を許可しない
   *
   * @param adminAccountNo 返信した管理者のアカウント番号
   * @return 1件以上存在する場合、true
   */
  boolean existsByAdminAccountNo(AccountNo adminAccountNo);
}
