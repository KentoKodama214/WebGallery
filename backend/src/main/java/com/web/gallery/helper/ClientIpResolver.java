package com.web.gallery.helper;

import com.web.gallery.domain.common.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * リクエストから送信元IPアドレスを取得するHelperクラス
 *
 * <p>{@code HttpServletRequest#getRemoteAddr()}で取得する。これはTomcatのRemoteIpValve （{@code
 * server.tomcat.remoteip}）がX-Forwarded-Forを解決した後の値であり、信頼できるプロキシ （{@code
 * server.tomcat.remoteip.internal-proxies}）経由のリクエストでのみ実クライアントIPになる。
 */
@Component
public class ClientIpResolver {

  /** IPアドレスが取得できない場合のフォールバック値 */
  private static final String UNKNOWN = "unknown";

  /**
   * リクエストから送信元IPアドレスを取得する（取得できない場合は固定値にフォールバックする）
   *
   * @param request リクエスト
   * @return {@link IpAddress}
   */
  public IpAddress resolve(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    return new IpAddress(StringUtils.hasText(remoteAddr) ? remoteAddr : UNKNOWN);
  }
}
