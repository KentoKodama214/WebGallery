package com.web.gallary.controller;

import com.web.gallary.constant.Consts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallary.config.JwtConfig;
import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.controller.request.AuthLoginRequest;
import com.web.gallary.controller.response.AuthLoginResponse;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.model.AuthTokenModel;
import com.web.gallary.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT認証に関するAPI通信を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthRestController {
	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

	private final AuthService authService;
	private final JwtConfig jwtConfig;

	/**
	 * ログイン認証
	 *
	 * @param	authLoginRequest	{@link AuthLoginRequest}
	 * @param	result				AuthLoginRequestのバインディング結果
	 * @return						{@link AuthLoginResponse}
	 * @throws	BadRequestException	リクエストパラメータが不正の場合
	 */
	@PostMapping(ApiRoutes.API_AUTH_LOGIN)
	public ResponseEntity<AuthLoginResponse> login(
			@RequestBody @Validated AuthLoginRequest authLoginRequest,
			BindingResult result) throws BadRequestException {

		if (result.hasErrors()) {
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}

		AuthTokenModel tokenModel = authService.login(
				authLoginRequest.getAccountId(),
				authLoginRequest.getPassword());

		ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
				tokenModel.getRefreshToken(),
				jwtConfig.getRefreshTokenExpirationDays() * 24 * 60 * 60L);

		AuthLoginResponse response = AuthLoginResponse.from(tokenModel);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
				.body(response);
	}

	/**
	 * アクセストークンのリフレッシュ
	 *
	 * @param	refreshToken	リフレッシュトークン（cookieから取得）
	 * @return					{@link AuthLoginResponse}
	 */
	@PostMapping(ApiRoutes.API_AUTH_REFRESH)
	public ResponseEntity<AuthLoginResponse> refresh(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

		if (refreshToken == null || refreshToken.isEmpty()) {
			return ResponseEntity.status(401).build();
		}

		AuthTokenModel tokenModel = authService.refresh(refreshToken);

		AuthLoginResponse response = AuthLoginResponse.from(tokenModel);

		return ResponseEntity.ok(response);
	}

	/**
	 * ログアウト
	 *
	 * @param	refreshToken	リフレッシュトークン（cookieから取得）
	 * @return					204 No Content
	 */
	@PostMapping(ApiRoutes.API_AUTH_LOGOUT)
	public ResponseEntity<Void> logout(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

		if (refreshToken != null && !refreshToken.isEmpty()) {
			authService.logout(refreshToken);
		}

		// リフレッシュトークンcookieを削除
		ResponseCookie clearCookie = createRefreshTokenCookie(Consts.STRING_EMPTY, 0);

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, clearCookie.toString())
				.build();
	}

	/**
	 * 認証失敗（パスワード不一致）のExceptionHandler
	 *
	 * @param	exception	{@link BadCredentialsException}
	 * @return				401 Unauthorized
	 */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
		log.info("Authentication failed: {}", exception.getMessage());
		return ResponseEntity.status(401)
				.body(new ErrorResponse("アカウントIDまたはパスワードが間違っています。"));
	}

	/**
	 * アカウントロック時のExceptionHandler
	 *
	 * @param	exception	{@link LockedException}
	 * @return				423 Locked
	 */
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ErrorResponse> handleLocked(LockedException exception) {
		log.info("Account locked: {}", exception.getMessage());
		return ResponseEntity.status(423)
				.body(new ErrorResponse("アカウントがロックされています。"));
	}

	/**
	 * リフレッシュトークン無効時のExceptionHandler
	 *
	 * @param	exception	{@link IllegalArgumentException}
	 * @return				401 Unauthorized
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(IllegalArgumentException exception) {
		log.info("Invalid refresh token: {}", exception.getMessage());
		return ResponseEntity.status(401)
				.body(new ErrorResponse(exception.getMessage()));
	}

	/**
	 * リフレッシュトークンのcookieを作成する
	 *
	 * @param	value		cookie値
	 * @param	maxAge		有効期限（秒）
	 * @return				{@link ResponseCookie}
	 */
	private ResponseCookie createRefreshTokenCookie(String value, long maxAge) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
				.httpOnly(true)
				.secure(true)
				.sameSite("Strict")
				.path("/api/v1/auth")
				.maxAge(maxAge)
				.build();
	}

	/**
	 * エラーレスポンス用の内部クラス
	 * @param message エラーメッセージ
	 */
	private record ErrorResponse(String message) {}
}
