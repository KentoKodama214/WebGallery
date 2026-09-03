package com.web.gallery.helper;

import java.util.Set;

import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * リクエストのバリデーションエラーをログ出力するためのHelperクラス<p>
 * パスワード等の機微なフィールドは、入力値（{@link FieldError#getRejectedValue()}）を
 * そのまま出力するとログ集約基盤に平文の資格情報が残るため、値をマスクして出力する。
 */
public final class ValidationErrorLogger {

	/** 入力値をマスクして出力する機微フィールド名 */
	private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of("password", "newPassword", "currentPassword");

	/** マスク後の表示文字列 */
	private static final String MASKED_VALUE = "***";

	private ValidationErrorLogger() {
	}

	/**
	 * バリデーションエラーの各フィールドを{@code INFO}レベルでログ出力する<p>
	 * 機微フィールド（{@link #SENSITIVE_FIELD_NAMES}）の入力値は{@code ***}に置き換える。
	 *
	 * @param	log		呼び出し元クラスのロガー
	 * @param	result	バリデーション結果
	 */
	public static void logFieldErrors(Logger log, BindingResult result) {
		for (FieldError error : result.getFieldErrors()) {
			Object value = isSensitive(error.getField()) ? MASKED_VALUE : error.getRejectedValue();
			log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
					error.getField(), value, error.getDefaultMessage());
		}
	}

	/**
	 * フィールド名が機微フィールドかどうかを判定する<p>
	 * ネストしたパス（{@code parent.password} や {@code list[0].password}）にも対応するため、
	 * 末尾のセグメントで判定する。
	 *
	 * @param	field	フィールド名（バインドパス）
	 * @return			機微フィールドの場合true
	 */
	private static boolean isSensitive(String field) {
		if (field == null) {
			return false;
		}
		String lastSegment = field.substring(field.lastIndexOf('.') + 1);
		return SENSITIVE_FIELD_NAMES.contains(lastSegment);
	}
}
