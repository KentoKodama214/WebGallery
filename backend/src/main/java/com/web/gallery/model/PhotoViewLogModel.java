package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真詳細閲覧ログを受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PhotoViewLogModel {
  /** 写真アカウント番号 */
  @NonNull private AccountNo photoAccountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** リファラ */
  @NonNull private Referer referer;

  /** 送信元IPアドレス */
  @NonNull private IpAddress ipAddress;

  /** 国・地域 */
  @NonNull private IpGeoLocation geoLocation;
}
