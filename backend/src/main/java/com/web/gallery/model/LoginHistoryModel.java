package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ログイン履歴を受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class LoginHistoryModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 送信元IPアドレス */
  @NonNull private IpAddress ipAddress;

  /** 国・地域 */
  @NonNull private IpGeoLocation geoLocation;

  /**
   * アカウント番号・IPアドレス・位置情報からLoginHistoryModelを生成する
   *
   * @param accountNo アカウント番号
   * @param ipAddress 送信元IPアドレス
   * @param geoLocation 位置情報（国・地域）
   * @return {@link LoginHistoryModel}
   */
  public static LoginHistoryModel of(
      AccountNo accountNo, IpAddress ipAddress, IpGeoLocation geoLocation) {
    return LoginHistoryModel.builder()
        .accountNo(accountNo)
        .ipAddress(ipAddress)
        .geoLocation(geoLocation)
        .build();
  }
}
