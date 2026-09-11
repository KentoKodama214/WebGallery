package com.web.gallery.config;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.helper.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 送信元IPアドレスごとにエンドポイントのカテゴリ別リクエスト数を数え、上限を超えたリクエストを 429 で拒否するフィルター
 *
 * <p>前段（WAF / ロードバランサ）のレート制限に加えたアプリ側の多層防御。とくにログインの総当たり・
 * アカウント登録スパムに対して低いしきい値を適用する。認証・認可より前に実行し、無駄な認証処理を避ける。
 * IP単位のため共有NAT（CGNAT・社内NAT）配下では誤検知しうる。厳密な制御は前段のWAFに委ね、 アカウント単位のロック（ログイン失敗3回）と併せた多層防御と位置づける。
 *
 * <p>送信元IPは {@code HttpServletRequest#getRemoteAddr()} で取得する。これは Tomcat の RemoteIpValve
 * （`server.tomcat.remoteip`）が `X-Forwarded-For` を解決した後の値であり、信頼できるプロキシ
 * （`server.tomcat.remoteip.internal-proxies` / 環境変数 `TRUSTED_PROXIES`）経由のリクエストでのみ
 * 実クライアントIPになる。フロントの `/api` プロキシは実クライアントIPを `X-Forwarded-For` に載せ直す。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  /** 429 レスポンスのエラーコード */
  private static final String ERROR_CODE = "E-A-0003";

  /** 429 レスポンスの Retry-After ヘッダー値（秒） */
  private static final String RETRY_AFTER_SECONDS = "60";

  private final RateLimitConfig rateLimitConfig;

  private final RateLimiter rateLimiter;

  /**
   * コンストラクタ
   *
   * @param rateLimitConfig レート制限の設定
   * @param rateLimiter レート制限のカウンタ
   */
  public RateLimitFilter(RateLimitConfig rateLimitConfig, RateLimiter rateLimiter) {
    this.rateLimitConfig = rateLimitConfig;
    this.rateLimiter = rateLimiter;
  }

  /** エンドポイントのカテゴリ */
  private enum Category {
    AUTH,
    REGISTER,
    GENERAL
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !rateLimitConfig.isEnabled()
        || !request.getRequestURI().startsWith(ApiRoutes.API_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Category category = resolveCategory(request);
    RateLimitConfig.Bucket bucket = bucketOf(category);
    String clientIp = clientIpOf(request);
    String key = clientIp + "|" + category.name();

    if (!rateLimiter.tryAcquire(key, new RateLimiter.Rule(bucket.capacity(), bucket.window()))) {
      log.info("Rate limit exceeded. (category: {}, clientIp: {})", category, clientIp);
      writeTooManyRequests(response);
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * リクエストのパス・メソッドからカテゴリを判定する
   *
   * @param request リクエスト
   * @return カテゴリ
   */
  private Category resolveCategory(HttpServletRequest request) {
    String uri = request.getRequestURI();
    // ログインのみ厳格な枠にする。トークンリフレッシュは128bitのランダムトークンで
    // 総当たり不能なため GENERAL 枠で足りる（正常系では15分ごとに1回程度）
    if (uri.equals(ApiRoutes.API_AUTH_LOGIN)) {
      return Category.AUTH;
    }
    if ("POST".equalsIgnoreCase(request.getMethod()) && uri.equals(ApiRoutes.API_ACCOUNTS)) {
      return Category.REGISTER;
    }
    return Category.GENERAL;
  }

  /**
   * カテゴリに対応するしきい値を返す
   *
   * @param category カテゴリ
   * @return しきい値
   */
  private RateLimitConfig.Bucket bucketOf(Category category) {
    return switch (category) {
      case AUTH -> rateLimitConfig.getAuth();
      case REGISTER -> rateLimitConfig.getRegister();
      case GENERAL -> rateLimitConfig.getGeneral();
    };
  }

  /**
   * 送信元IPアドレスを取得する（取得できない場合は固定キーにフォールバックする）
   *
   * @param request リクエスト
   * @return 送信元IPアドレス
   */
  private String clientIpOf(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
  }

  /**
   * 429 Too Many Requests のJSONレスポンスを書き込む
   *
   * @param response レスポンス
   * @throws IOException 書き込みに失敗した場合
   */
  private void writeTooManyRequests(HttpServletResponse response) throws IOException {
    String body =
        String.format(
            "{\"httpStatus\":%d,\"errorCode\":\"%s\",\"errorMessage\":\"%s\"}",
            HttpStatus.TOO_MANY_REQUESTS.value(), ERROR_CODE, MessageConst.ERR_TOO_MANY_REQUESTS);
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
    response.setContentLength(bytes.length);
    response.getOutputStream().write(bytes);
  }
}
