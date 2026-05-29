package com.web.gallery;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.model.AccountModel;

/**
 * Spring SecurityのUserDetailsクラスの実装クラス
 */
public class AccountPrincipal implements UserDetails {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_USER = "ROLE_USER";

	private final AccountModel accountModel;
	private final int maxFailCount;

	public AccountPrincipal(AccountModel accountModel, int failCount) {
		this.accountModel = accountModel;
		this.maxFailCount = failCount;
	}

	/**
	 * ユーザーに与えられる権限を返す
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		switch(accountModel.getAuthorityKbn()) {
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
	public Long getAccountNo() {
		return accountModel.getAccountNo();
	}

	/**
	 * 権限区分を取得する
	 *
	 * @return	権限区分
	 */
	public AuthorityEnum getAuthorityKbn() {
		return accountModel.getAuthorityKbn();
	}

	/**
	 * アカウント名を取得する
	 *
	 * @return	アカウント名
	 */
	public String getAccountName() {
		return accountModel.getAccountName();
	}

	/**
	 * パスワードを取得する
	 *
	 * @return	パスワード
	 */
	@Override
	public String getPassword() {
		return accountModel.getPassword();
	}

	/**
	 * アカウントIDを取得する
	 *
	 * @return	アカウントID
	 */
	@Override
	public String getUsername() {
		return accountModel.getAccountId();
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
		return accountModel.getLoginFailureCount() < maxFailCount;
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
		return !accountModel.getIsDeleted();
	}
}
