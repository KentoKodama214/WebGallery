package com.web.gallary.helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.web.gallary.AccountPrincipal;

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
	public Integer getAccountNo() {
		Integer accountNo = null;

		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication.getPrincipal() instanceof AccountPrincipal) {
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
		
		if (authentication.getPrincipal() instanceof AccountPrincipal) {
			AccountPrincipal accountPrincipal =
					AccountPrincipal.class.cast(authentication.getPrincipal());
			accountId = accountPrincipal.getUsername();
		}
		
		return accountId;
	}
	
	/**
	 * セッション情報からパスワードを取得する
	 * 
	 * @return	パスワード
	 */
	public String getPassword() {
		String password = null;
		
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication.getPrincipal() instanceof AccountPrincipal) {
			AccountPrincipal accountPrincipal =
					AccountPrincipal.class.cast(authentication.getPrincipal());
			password = accountPrincipal.getPassword();
		}
		
		return password;
	}
}