package com.web.gallery.application.model.auth;

import com.web.gallery.domain.enumeration.LoginMethodEnum;
import com.web.gallery.domain.model.common.IpAddress;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ログイン試行の付帯情報（送信元IPアドレス・ログイン方式）を受け渡すためのModelクラス
 *
 * <p>認証リクエスト（{@code Authentication#getDetails()}）に載せて認証成功イベントへ伝播させ、 ログイン履歴の記録に使用する
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class LoginContextModel {
  /** 送信元IPアドレス */
  @NonNull private IpAddress ipAddress;

  /** ログイン方式 */
  @NonNull private LoginMethodEnum loginMethodKbn;

  /**
   * 送信元IPアドレス・ログイン方式からLoginContextModelを生成する
   *
   * @param ipAddress 送信元IPアドレス
   * @param loginMethodKbn ログイン方式
   * @return {@link LoginContextModel}
   */
  public static LoginContextModel of(IpAddress ipAddress, LoginMethodEnum loginMethodKbn) {
    return LoginContextModel.builder().ipAddress(ipAddress).loginMethodKbn(loginMethodKbn).build();
  }
}
