package com.web.gallery.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.SessionHelper;

import lombok.RequiredArgsConstructor;

/**
 * {@link com.web.gallery.annotation.RequireAdminAuthority}が付与されたメソッドの実行前に
 * 管理者権限を検証するAspectクラス
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuthorityAspect {
	private final SessionHelper sessionHelper;

	/**
	 * 管理者権限のバリデーションを行う
	 *
	 * @throws	GalleryException	管理者権限がない場合
	 */
	@Before("@annotation(com.web.gallery.annotation.RequireAdminAuthority)")
	public void validateAdminAuthority() throws GalleryException {
		if (sessionHelper.getAuthorityKbn() != AuthorityEnum.ADMINISTRATOR) {
			throw ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.toException();
		}
	}
}
