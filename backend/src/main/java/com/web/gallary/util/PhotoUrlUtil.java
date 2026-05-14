package com.web.gallary.util;

import java.util.Objects;

import com.web.gallary.constant.ApiRoutes;

/**
 * 写真関連のURLを取得するUtilityクラス
 */
public final class PhotoUrlUtil {
	/**
	 * アカウントIDに対する写真一覧ページのパスを取得する<p>
	 * アカウントIDがnullや空文字の場合は、ログインページのURLを返す
	 * 
	 * @param accountId	アカウントID
	 * @return	写真一覧ページのパス
	 */
	public static String getPhotoListUrl(String accountId) {
		if(Objects.isNull(accountId) || accountId.trim().isEmpty()) {
			return ApiRoutes.LOGIN;
		}
		
		return ApiRoutes.PHOTO_LIST.replace(ApiRoutes.PHOTO_ACCOUNT_ID, accountId);
	}
}