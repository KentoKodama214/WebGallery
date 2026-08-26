package com.web.gallery.controller;

import com.web.gallery.constant.Consts;
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

import com.web.gallery.config.JwtConfig;
import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.AuthLoginRequest;
import com.web.gallery.controller.response.AuthErrorResponse;
import com.web.gallery.controller.response.AuthLoginResponse;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.InvalidRefreshTokenException;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "認証", description = "JWT認証に関するAPI")
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
	 * @throws	GalleryException	リクエストパラメータが不正の場合
	 */
	@Operation(summary = "ログイン", description = "アカウントIDとパスワードで認証し、JWTトークンを発行する")
	@ApiResponse(responseCode = "200", description = "認証成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "401", description = "認証失敗（アカウントIDまたはパスワードが不正）", content = @Content)
	@ApiResponse(responseCode = "423", description = "アカウントロック", content = @Content)
	@PostMapping(ApiRoutes.API_AUTH_LOGIN)
	public ResponseEntity<AuthLoginResponse> login(
			@RequestBody @Validated AuthLoginRequest authLoginRequest,
			BindingResult result) throws GalleryException {

		if (result.hasErrors()) {
			throw ErrorEnum.INVALID_INPUT.toException();
		}

		AuthTokenModel tokenModel = authService.login(
				new AccountId(authLoginRequest.getAccountId()),
				new Password(authLoginRequest.getPassword()));

		ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
				tokenModel.getRefreshToken().value(),
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
	@Operation(summary = "トークンリフレッシュ", description = "リフレッシュトークン（cookie）を使用してアクセストークンを再発行する")
	@ApiResponse(responseCode = "200", description = "リフレッシュ成功")
	@ApiResponse(responseCode = "401", description = "リフレッシュトークンが無効", content = @Content)
	@ApiResponse(responseCode = "423", description = "アカウントロック", content = @Content)
	@PostMapping(ApiRoutes.API_AUTH_REFRESH)
	public ResponseEntity<AuthLoginResponse> refresh(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

		if (refreshToken == null || refreshToken.isEmpty()) {
			return ResponseEntity.status(401).build();
		}

		AuthTokenModel tokenModel = authService.refresh(new RefreshTokenValue(refreshToken));

		AuthLoginResponse response = AuthLoginResponse.from(tokenModel);

		return ResponseEntity.ok(response);
	}

	/**
	 * ログアウト
	 *
	 * @param	refreshToken	リフレッシュトークン（cookieから取得）
	 * @return					204 No Content
	 */
	@Operation(summary = "ログアウト", description = "リフレッシュトークンを無効化し、cookieを削除する")
	@ApiResponse(responseCode = "204", description = "ログアウト成功")
	@PostMapping(ApiRoutes.API_AUTH_LOGOUT)
	public ResponseEntity<Void> logout(
			@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

		if (refreshToken != null && !refreshToken.isEmpty()) {
			authService.logout(new RefreshTokenValue(refreshToken));
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
	public ResponseEntity<AuthErrorResponse> handleBadCredentials(BadCredentialsException exception) {
		log.info("Authentication failed: {}", exception.getMessage());
		return ResponseEntity.status(401)
				.body(AuthErrorResponse.of("アカウントIDまたはパスワードが間違っています。"));
	}

	/**
	 * アカウントロック時のExceptionHandler
	 *
	 * @param	exception	{@link LockedException}
	 * @return				423 Locked
	 */
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<AuthErrorResponse> handleLocked(LockedException exception) {
		log.info("Account locked: {}", exception.getMessage());
		return ResponseEntity.status(423)
				.body(AuthErrorResponse.of("アカウントがロックされています。"));
	}

	/**
	 * リフレッシュトークン無効時のExceptionHandler
	 *
	 * @param	exception	{@link InvalidRefreshTokenException}
	 * @return				401 Unauthorized
	 */
	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<AuthErrorResponse> handleInvalidToken(InvalidRefreshTokenException exception) {
		log.info("Invalid refresh token: {}", exception.getMessage());
		return ResponseEntity.status(401)
				.body(AuthErrorResponse.of(MessageConst.ERR_INVALID_REFRESH_TOKEN));
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
}
