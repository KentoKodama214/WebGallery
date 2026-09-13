package com.web.gallery.entity;

import com.web.gallery.model.LoginHistoryModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * ログイン履歴テーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class LoginHistory {
  /** ログイン履歴No */
  private Long loginHistoryNo;

  /** アカウント番号 */
  private Long accountNo;

  /** 送信元IPアドレス */
  private String ipAddress;

  /** 国（未解決時は空文字） */
  private String country;

  /** 地域（未解決時は空文字） */
  private String region;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * LoginHistoryModelからLoginHistoryエンティティを生成する
   *
   * @param model {@link LoginHistoryModel}
   * @return {@link LoginHistory}
   */
  public static LoginHistory from(LoginHistoryModel model) {
    return LoginHistory.builder()
        .accountNo(model.getAccountNo().value())
        .ipAddress(model.getIpAddress().value())
        .country(
            model.getGeoLocation().country() != null
                ? model.getGeoLocation().country().value()
                : "")
        .region(
            model.getGeoLocation().region() != null ? model.getGeoLocation().region().value() : "")
        .createdBy(model.getAccountNo().value())
        .build();
  }
}
