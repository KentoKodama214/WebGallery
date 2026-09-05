package com.web.gallery.config;

import com.web.gallery.constant.MessageConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未認証（アクセストークン不在・不正・期限切れ）で保護リソースへアクセスされた場合に、 アプリケーション共通のJSONエラーレスポンス（401
 * Unauthorized）を返すエントリーポイントクラス
 *
 * <p>これを設定しないとSpring Securityのデフォルトで403（空ボディ）が返り、 フロントエンドの「401でアクセストークンを再取得する」制御が働かなくなる。 {@link
 * RestAccessDeniedHandler}（認可エラー＝403）と対になる
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /** 未認証エラーのエラーコード */
  private static final String ERROR_CODE = "E-A-0002";

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    String body =
        String.format(
            "{\"httpStatus\":%d,\"errorCode\":\"%s\",\"errorMessage\":\"%s\"}",
            HttpStatus.UNAUTHORIZED.value(), ERROR_CODE, MessageConst.ERR_UNAUTHENTICATED);
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setContentLength(bytes.length);
    response.getOutputStream().write(bytes);
  }
}
