package com.web.gallery.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.web.gallery.enumeration.ErrorEnum;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 認可エラー（403 Forbidden）発生時に、アプリケーション共通のJSONエラーレスポンスを返すハンドラクラス<p>
 * Spring Securityの認可（{@code hasRole}等）で拒否された場合でも、
 * {@link com.web.gallery.controller.CommonRestControllerAdvice}経由の場合と同一のレスポンス形式に揃える
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {

		ErrorEnum error = ErrorEnum.NOT_AUTHORIZED_TO_ADMIN;
		String body = String.format(
				"{\"httpStatus\":%d,\"errorCode\":\"%s\",\"errorMessage\":\"%s\"}",
				HttpStatus.FORBIDDEN.value(), error.getErrorCode(), error.getErrorMessage());
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes);
	}
}
