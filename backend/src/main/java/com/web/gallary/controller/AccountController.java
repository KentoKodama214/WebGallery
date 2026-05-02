package com.web.gallary.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.constant.Consts;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.helper.KbnHelper;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.AccountModel;
import com.web.gallary.model.KbnMstModel;
import com.web.gallary.service.KbnMstService;
import com.web.gallary.service.impl.AccountServiceImpl;
import com.web.gallary.util.PhotoUrlUtil;

import lombok.RequiredArgsConstructor;

/**
 * アカウントに関するページを扱うControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Controller
@RequiredArgsConstructor
public class AccountController {
	
	private final AccountServiceImpl accountService;
	private final KbnMstService kbnMstService;
	private final SessionHelper sessionHelper;
	private final KbnHelper kbnHepler;
	
	/**
	 * アカウントの登録を行うページ
	 * 
	 * @return	ModelAndView	アカウント登録ページ。Modelとして都道府県一覧（prefectureGroupList）を返す
	 */
	@GetMapping(ApiRoutes.REGISTER)
	public ModelAndView register() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("account_register");
		
		List<KbnMstModel> prefectureList = kbnMstService.getPrefectureList();
		mv.addObject("prefectureGroupList", kbnHepler.convertToLinkedHashMap(prefectureList));
		
		return mv;
	}
	
	/**
	 * アカウント設定を行うページ
	 * 
	 * @param	accountId		アカウントID
	 * @return	ModelAndView	アカウント編集ページ。
	 * @throws ForbiddenAccountException	アカウントを編集する権限がない場合
	 */
	@GetMapping(ApiRoutes.ACCOUNT_SETTING)
	public ModelAndView account_setting(@PathVariable String accountId) throws ForbiddenAccountException {
		if (!accountId.equals(sessionHelper.getAccountId())) {
			throw new ForbiddenAccountException(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT);
		}
		
		ModelAndView mv = new ModelAndView();
		mv.setViewName("account_setting");
		mv.addObject("my_photo_list_url", PhotoUrlUtil.getPhotoListUrl(sessionHelper.getAccountId()));
		
		Account account = accountService.getAccountById(accountId);
		if(account.getBirthdate().equals(Consts.MIN_LOCAL_DATE)) {
			account.setBirthdate(null);
		}
		mv.addObject("AccountSettingRequest", account);
		
		List<KbnMstModel> prefectureList = kbnMstService.getPrefectureList();
		mv.addObject("prefectureGroupList", kbnHepler.convertToLinkedHashMap(prefectureList));
		
		return mv;
	}
	
	/**
	 * アカウントの一覧を参照するページ
	 * 
	 * @return	ModelAndView	アカウント一覧ページ。Modelとしてアカウント一覧（AccountList）と写真一覧のURL（my_photo_list_url）を返す
	 */
	@GetMapping(ApiRoutes.ACCOUNT_LIST)
	public ModelAndView account_list() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("account_list");
		
		List<AccountModel> accountModelList = accountService.getAccountList();
		mv.addObject("AccountList", accountModelList);
		
		if(!Objects.isNull(sessionHelper.getAccountId()))
			mv.addObject("my_photo_list_url", PhotoUrlUtil.getPhotoListUrl(sessionHelper.getAccountId()));
		
		return mv;
	}
}