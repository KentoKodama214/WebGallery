package com.web.gallary.util;

import java.util.Objects;

import com.web.gallary.constant.ApiRoutes;

/**
 * アカウント関連のURLを取得するUtilityクラス
 */
public final class AccountUrlUtil {
	/**
	 * アカウントIDに対するアカウント設定ページのパスを取得する<p>
	 * アカウントIDがnullや空文字の場合は、ログインページのURLを返す
	 * 
	 * @param accountId	アカウントID
	 * @return	アカウント設定ページのパス
	 */
	public static String getAccountSettingUrl(String accountId) {
		if(Objects.isNull(accountId) || accountId.trim().isEmpty()) {
			return ApiRoutes.LOGIN;
		}
		
		return ApiRoutes.ACCOUNT_SETTING.replace(ApiRoutes.ACCOUNT_ID, accountId);
	}
}