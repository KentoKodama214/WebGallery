package com.web.gallary.enumuration;

import com.web.gallary.constant.MessageConst;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * エラーに関する情報を管理するEnumクラス
 */
@Getter
@AllArgsConstructor
public enum ErrorEnum {
	/**
	 * エラーコード：E-C-0000
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_INVALID_INPUT}
	 */
	INVALID_INPUT("E-C-0000", MessageConst.ERR_INVALID_INPUT),
	
	/**
	 * エラーコード：E-C-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_ACCOUNT}
	 */
	FAIL_TO_REGIST_ACCOUNT("E-C-0001", MessageConst.ERR_FAIL_TO_REGIST_ACCOUNT),

	/**
	 * エラーコード：E-C-0002
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_UPDATE_ACCOUNT}
	 */
	FAIL_TO_UPDATE_ACCOUNT("E-C-0002", MessageConst.ERR_FAIL_TO_UPDATE_ACCOUNT),
	
	/**
	 * エラーコード：E-C-0003
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_NOT_AUTHORIZED_TO_EDIT_ACCOUNT}
	 */
	NOT_AUTHORIZED_TO_EDIT_ACCOUNT("E-C-0003", MessageConst.ERR_NOT_AUTHORIZED_TO_EDIT_ACCOUNT),
	
	/**
	 * エラーコード：E-P-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_PHOTO}
	 */
	FAIL_TO_REGIST_PHOTO("E-P-0001", MessageConst.ERR_FAIL_TO_REGIST_PHOTO),
	
	/**
	 * エラーコード：E-P-0002
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_UPDATE_PHOTO}
	 */
	FAIL_TO_UPDATE_PHOTO("E-P-0002", MessageConst.ERR_FAIL_TO_UPDATE_PHOTO),
	
	/**
	 * エラーコード：E-P-0003
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_DELETE_PHOTO}
	 */
	FAIL_TO_DELETE_PHOTO("E-P-0003", MessageConst.ERR_FAIL_TO_DELETE_PHOTO),
	
	/**
	 * エラーコード：E-P-0004
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_PHOTO_TAG}
	 */
	FAIL_TO_REGIST_PHOTO_TAG("E-P-0004", MessageConst.ERR_FAIL_TO_REGIST_PHOTO_TAG),
	
	/**
	 * エラーコード：E-P-0005
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_FAVORITE}
	 */
	FAIL_TO_REGIST_FAVORITE("E-P-0005", MessageConst.ERR_FAIL_TO_REGIST_FAVORITE),

	/**
	 * エラーコード：E-P-0006
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_CANCEL_FAVORITE}
	 */
	FAIL_TO_CANCEL_FAVORITE("E-P-0006", MessageConst.ERR_FAIL_TO_CANCEL_FAVORITE),
	
	/**
	 * エラーコード：E-P-0007
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_DUPLICATE_PHOTO_FILE}
	 */
	DUPLICATE_PHOTO_FILE("E-P-0007", MessageConst.ERR_DUPLICATE_PHOTO_FILE),
	
	/**
	 * エラーコード：E-P-0008
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_NOT_AUTHORIZED_TO_EDIT_PHOTO}
	 */
	NOT_AUTHORIZED_TO_EDIT_PHOTO("E-P-0008", MessageConst.ERR_NOT_AUTHORIZED_TO_EDIT_PHOTO),

	/**
	 * エラーコード：E-P-0009
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_PHOTO_NOT_FOUND}
	 */
	PHOTO_NOT_FOUND("E-P-0009", MessageConst.ERR_PHOTO_NOT_FOUND),
	
	/**
	 * エラーコード：E-P-0010
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_REACHED_REGISTRATION_LIMIT}
	 */
	REACHED_REGISTRATION_LIMIT("E-P-0010", MessageConst.ERR_REACHED_REGISTRATION_LIMIT);
	
	/** エラーコード */
	private final String errorCode;
	
	/** エラーメッセージ */
	private final String errorMessage;
}