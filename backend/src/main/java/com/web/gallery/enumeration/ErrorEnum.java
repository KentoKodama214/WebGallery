package com.web.gallery.enumeration;

import com.web.gallery.constant.MessageConst;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FavoriteNotFoundException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.SystemException;
import com.web.gallery.exception.UpdateFailureException;

import lombok.Getter;

/**
 * エラーに関する情報を管理するEnumクラス
 */
@Getter
public enum ErrorEnum {
	/**
	 * エラーコード：E-C-0000
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_INVALID_INPUT}
	 */
	INVALID_INPUT("E-C-0000", MessageConst.ERR_INVALID_INPUT) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-C-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_ACCOUNT}
	 */
	FAIL_TO_REGIST_ACCOUNT("E-C-0001", MessageConst.ERR_FAIL_TO_REGIST_ACCOUNT) {
		@Override
		public GalleryException toException() {
			return new RegistFailureException(this);
		}
	},

	/**
	 * エラーコード：E-C-0002
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_UPDATE_ACCOUNT}
	 */
	FAIL_TO_UPDATE_ACCOUNT("E-C-0002", MessageConst.ERR_FAIL_TO_UPDATE_ACCOUNT) {
		@Override
		public GalleryException toException() {
			return new UpdateFailureException(this);
		}
	},

	/**
	 * エラーコード：E-C-0003
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_NOT_AUTHORIZED_TO_EDIT_ACCOUNT}
	 */
	NOT_AUTHORIZED_TO_EDIT_ACCOUNT("E-C-0003", MessageConst.ERR_NOT_AUTHORIZED_TO_EDIT_ACCOUNT) {
		@Override
		public GalleryException toException() {
			return new ForbiddenAccountException(this);
		}
	},

	/**
	 * エラーコード：E-P-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_PHOTO}
	 */
	FAIL_TO_REGIST_PHOTO("E-P-0001", MessageConst.ERR_FAIL_TO_REGIST_PHOTO) {
		@Override
		public GalleryException toException() {
			return new RegistFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0002
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_UPDATE_PHOTO}
	 */
	FAIL_TO_UPDATE_PHOTO("E-P-0002", MessageConst.ERR_FAIL_TO_UPDATE_PHOTO) {
		@Override
		public GalleryException toException() {
			return new UpdateFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0003
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_DELETE_PHOTO}
	 */
	FAIL_TO_DELETE_PHOTO("E-P-0003", MessageConst.ERR_FAIL_TO_DELETE_PHOTO) {
		@Override
		public GalleryException toException() {
			return new UpdateFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0004
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_PHOTO_TAG}
	 */
	FAIL_TO_REGIST_PHOTO_TAG("E-P-0004", MessageConst.ERR_FAIL_TO_REGIST_PHOTO_TAG) {
		@Override
		public GalleryException toException() {
			return new RegistFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0005
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_REGIST_FAVORITE}
	 */
	FAIL_TO_REGIST_FAVORITE("E-P-0005", MessageConst.ERR_FAIL_TO_REGIST_FAVORITE) {
		@Override
		public GalleryException toException() {
			return new RegistFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0006
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_CANCEL_FAVORITE}
	 */
	FAIL_TO_CANCEL_FAVORITE("E-P-0006", MessageConst.ERR_FAIL_TO_CANCEL_FAVORITE) {
		@Override
		public GalleryException toException() {
			return new UpdateFailureException(this);
		}
	},

	/**
	 * エラーコード：E-P-0007
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_DUPLICATE_PHOTO_FILE}
	 */
	DUPLICATE_PHOTO_FILE("E-P-0007", MessageConst.ERR_DUPLICATE_PHOTO_FILE) {
		@Override
		public GalleryException toException() {
			return new FileDuplicateException(this);
		}
	},

	/**
	 * エラーコード：E-P-0008
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_NOT_AUTHORIZED_TO_EDIT_PHOTO}
	 */
	NOT_AUTHORIZED_TO_EDIT_PHOTO("E-P-0008", MessageConst.ERR_NOT_AUTHORIZED_TO_EDIT_PHOTO) {
		@Override
		public GalleryException toException() {
			return new ForbiddenAccountException(this);
		}
	},

	/**
	 * エラーコード：E-P-0009
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_PHOTO_NOT_FOUND}
	 */
	PHOTO_NOT_FOUND("E-P-0009", MessageConst.ERR_PHOTO_NOT_FOUND) {
		@Override
		public GalleryException toException() {
			return new PhotoNotFoundException(this);
		}
	},

	/**
	 * エラーコード：E-P-0010
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_REACHED_REGISTRATION_LIMIT}
	 */
	REACHED_REGISTRATION_LIMIT("E-P-0010", MessageConst.ERR_REACHED_REGISTRATION_LIMIT) {
		@Override
		public GalleryException toException() {
			return new PhotoNotAdditableException(this);
		}
	},

	/**
	 * エラーコード：E-P-0011
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_INVALID_PHOTO_FILE_EXTENSION}
	 */
	INVALID_PHOTO_FILE_EXTENSION("E-P-0011", MessageConst.ERR_INVALID_PHOTO_FILE_EXTENSION) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-P-0012
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_IMAGE_FILE_REQUIRED}
	 */
	IMAGE_FILE_REQUIRED("E-P-0012", MessageConst.ERR_IMAGE_FILE_REQUIRED) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-P-0013
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_UNSUPPORTED_IMAGE_CONTENT_TYPE}
	 */
	UNSUPPORTED_IMAGE_CONTENT_TYPE("E-P-0013", MessageConst.ERR_UNSUPPORTED_IMAGE_CONTENT_TYPE) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-P-0014
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_INVALID_IMAGE_SIGNATURE}
	 */
	INVALID_IMAGE_SIGNATURE("E-P-0014", MessageConst.ERR_INVALID_IMAGE_SIGNATURE) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-P-0015
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_IMAGE_FILE_SIZE_EXCEEDED}
	 */
	IMAGE_FILE_SIZE_EXCEEDED("E-P-0015", MessageConst.ERR_IMAGE_FILE_SIZE_EXCEEDED) {
		@Override
		public GalleryException toException() {
			return new BadRequestException(this);
		}
	},

	/**
	 * エラーコード：E-P-0016
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAVORITE_NOT_FOUND}
	 */
	FAVORITE_NOT_FOUND("E-P-0016", MessageConst.ERR_FAVORITE_NOT_FOUND) {
		@Override
		public GalleryException toException() {
			return new FavoriteNotFoundException(this);
		}
	},

	/**
	 * エラーコード：E-A-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_NOT_AUTHORIZED_TO_ADMIN}
	 */
	NOT_AUTHORIZED_TO_ADMIN("E-A-0001", MessageConst.ERR_NOT_AUTHORIZED_TO_ADMIN) {
		@Override
		public GalleryException toException() {
			return new ForbiddenAccountException(this);
		}
	},

	/**
	 * エラーコード：E-C-0004
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_FAIL_TO_DELETE_ACCOUNT}
	 */
	FAIL_TO_DELETE_ACCOUNT("E-C-0004", MessageConst.ERR_FAIL_TO_DELETE_ACCOUNT) {
		@Override
		public GalleryException toException() {
			return new UpdateFailureException(this);
		}
	},

	/**
	 * エラーコード：E-C-0005
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_CURRENT_PASSWORD_MISMATCH}
	 */
	CURRENT_PASSWORD_MISMATCH("E-C-0005", MessageConst.ERR_CURRENT_PASSWORD_MISMATCH) {
		@Override
		public GalleryException toException() {
			return new ForbiddenAccountException(this);
		}
	},

	/**
	 * エラーコード：E-S-0001
	 * <p>
	 * エラーメッセージ：{@value MessageConst#ERR_SYSTEM_ERROR}
	 */
	SYSTEM_ERROR("E-S-0001", MessageConst.ERR_SYSTEM_ERROR) {
		@Override
		public GalleryException toException() {
			return new SystemException(this);
		}
	};

	/** エラーコード */
	private final String errorCode;

	/** エラーメッセージ */
	private final String errorMessage;

	ErrorEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * このエラーに対応する例外を生成する
	 *
	 * @return 生成された例外
	 */
	public abstract GalleryException toException();
}
