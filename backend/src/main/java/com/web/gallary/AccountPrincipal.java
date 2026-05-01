package com.web.gallary;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.web.gallary.entity.Account;

/**
 * Spring SecurityのUserDetailsクラスの実装クラス
 */
public class AccountPrincipal implements UserDetails {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_USER = "ROLE_USER";

	private Account account;
	private int maxFailCount;

	public AccountPrincipal(Account account, int failCount) {
		this.account = account;
		this.maxFailCount = failCount;
	}

	/**
	 * ユーザーに与えられる権限を返す
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		switch(account.getAuthorityKbn()) {
			case ADMINISTRATOR:
				return Collections.singleton(new SimpleGrantedAuthority(ROLE_ADMIN));
			default:
				return Collections.singleton(new SimpleGrantedAuthority(ROLE_USER));
		}
	}
	
	/**
	 * アカウント番号を取得する
	 * 
	 * @return	アカウント番号
	 */
	public Integer getAccountNo() {
		return account.getAccountNo();
	}
	
	/**
	 * アカウント名を取得する
	 * 
	 * @return	アカウント名
	 */
	public String getAccountName() {
		return account.getAccountName();
	}
	
	/**
	 * パスワードを取得する
	 * 
	 * @return	パスワード
	 */
	@Override
	public String getPassword() {
		return account.getPassword();
	}
	
	/**
	 * アカウントIDを取得する
	 * 
	 * @return	アカウントID
	 */
	@Override
	public String getUsername() {
		return account.getAccountId();
	}

	/***
	 * アカウントが有効期限切れかどうかを返す
	 * 
	 * @return 有効期限は設定しないため、常にtrueを返す
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/***
	 * アカウントがロックされていないかどうかを返す
	 * 
	 * @return ログイン失敗回数が一定数を超えていなければtrueを返す
	 */
	@Override
	public boolean isAccountNonLocked() {
		return account.getLoginFailureCount() < maxFailCount;
	}

	/***
	 * 資格情報（ここではパスワード）が有効期限切れかどうかを返す
	 * 
	 * @return 有効期限は設定しないため、常にtrueを返す
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/***
	 * アカウントの有効／無効を返す
	 * 
	 * @return 有効な場合、trueを返す
	 */
	@Override
	public boolean isEnabled() {
		return !account.getIsDeleted();
	}
}