package com.web.gallery.helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.enumeration.AuthorityEnum;

/**
 * セッション管理のためのHelperクラス
 */
@Component
public class SessionHelper {
	/**
	 * セッション情報からアカウント番号を取得する
	 * 
	 * @return	アカウント番号
	 */
	public Long getAccountNo() {
		Long accountNo = null;

		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof AccountPrincipal) {
			AccountPrincipal accountPrincipal =
					AccountPrincipal.class.cast(authentication.getPrincipal());
			accountNo = accountPrincipal.getAccountNo();
		}

		return accountNo;
	}
	
	/**
	 * セッション情報からアカウントIDを取得する
	 *
	 * @return	アカウントID
	 */
	public String getAccountId() {
		String accountId = null;

		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof AccountPrincipal) {
			AccountPrincipal accountPrincipal =
					AccountPrincipal.class.cast(authentication.getPrincipal());
			accountId = accountPrincipal.getUsername();
		}

		return accountId;
	}

	/**
	 * セッション情報から権限区分を取得する
	 *
	 * @return	権限区分
	 */
	public AuthorityEnum getAuthorityKbn() {
		AuthorityEnum authorityKbn = null;

		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof AccountPrincipal) {
			AccountPrincipal accountPrincipal =
					AccountPrincipal.class.cast(authentication.getPrincipal());
			authorityKbn = accountPrincipal.getAuthorityKbn();
		}

		return authorityKbn;
	}
}