package com.web.gallary.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.web.gallary.config.PhotoConfig;
import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.controller.request.PhotoDetailRequest;
import com.web.gallary.controller.request.PhotoSettingRequest;
import com.web.gallary.controller.request.PhotoTagRequest;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.service.PhotoService;
import com.web.gallary.service.impl.AccountServiceImpl;
import com.web.gallary.util.AccountUrlUtil;
import com.web.gallary.util.PhotoUrlUtil;

import lombok.RequiredArgsConstructor;

/**
 * 写真に関するページを扱うControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Controller
@RequiredArgsConstructor
public class PhotoController {

	private final PhotoService photoService;
	private final AccountServiceImpl accountServiceImpl;
	private final SessionHelper sessionHelper;
	private final PhotoConfig photoConfig;
	
	/**
	 * 写真一覧ページ
	 * 
	 * @param	photoAccountId		ページ所有者のアカウントID
	 * @return	ModelAndView		写真一覧ページ。Modelとして以下を返す<p>
	 * 								ownerId:			ページの所有者のアカウントId<p>
	 * 								isOwner:			ページの所有者である場合はtrue<p>
	 * 								isAbleToAddPhoto:	写真の登録枚数の上限に達していない場合はtrue
	 */
	@GetMapping(ApiRoutes.PHOTO_LIST)
	public ModelAndView photoList(@PathVariable String photoAccountId) {
		// 登録枚数の上限に達している場合に写真一覧ページの追加ボタンを非表示にするため
		// アカウント番号をもとに、アカウントの権限から写真の登録枚数の上限に達しているかどうかを判定する(未ログインの場合、true)
		Boolean isReachedUpperLimit = photoService.isReachedUpperLimit(sessionHelper.getAccountNo());
		Boolean isOwner = photoAccountId.equals(sessionHelper.getAccountId());
		
		ModelAndView mv = getModelAndView("photo_list", photoAccountId);
		mv.addObject("ownerId", photoAccountId);
		mv.addObject("isOwner", isOwner);
		mv.addObject("isAbleToAddPhoto", isOwner & !isReachedUpperLimit);
		
		return mv;
	}

	/**
	 * 写真詳細ページ
	 * 
	 * @param	photoAccountId			ページ所有者のアカウントID
	 * @param	photoDetailRequest		{@link PhotoDetailRequest}
	 * @return	ModelAndView			写真詳細ページ。Modelとして以下を返す<p>
	 * 									PhotoSettingRequest:	写真のメタ情報<p>
	 * 									isOwner:				ページの所有者である場合はtrue
	 * @throws	PhotoNotFoundException	削除済みで写真が存在しない場合
	 */
	@GetMapping(ApiRoutes.PHOTO_DETAIL)
	public ModelAndView photoDetail(@PathVariable String photoAccountId, @ModelAttribute PhotoDetailRequest photoDetailRequest) throws PhotoNotFoundException {
		ModelAndView mv = getModelAndView("photo_detail", photoAccountId);
		
		PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
				.accountNo(sessionHelper.getAccountNo())
				.photoAccountNo(photoDetailRequest.getAccountNo())
				.photoNo(photoDetailRequest.getPhotoNo())
				.build();
		
		PhotoDetailModel photoDetailModel = photoService.getPhotoDetail(photoDetailGetModel);
		mv.addObject("PhotoSettingRequest", photoDetailModel);
		mv.addObject("isOwner", photoAccountId.equals(sessionHelper.getAccountId()));

		return mv;
	}

	/**
	 * 写真登録・編集ページ
	 * 
	 * @param photoAccountId				ページ所有者のアカウントID
	 * @param photoSettingRequest			{@link PhotoSettingRequest}
	 * @return ModelAndView					写真登録・編集ページ。Modelとして以下を返す<p>
	 * 										PhotoSettingRequest:	写真のメタ情報<p>
	 * 										maxFileSizeMb:			最大ファイルサイズ（MB）<p>
	 * 										isAddMode:				新規登録の場合はtrue<p>
	 * 										maxTagNo:				タグ番号の最大値。タグが存在する場合のみ返す
	 * @throws ForbiddenAccountException	写真の所有者以外がリクエストした場合
	 */
	@GetMapping(ApiRoutes.PHOTO_SETTING)
	public ModelAndView photoSetting(@PathVariable String photoAccountId, @ModelAttribute PhotoSettingRequest photoSettingRequest) throws ForbiddenAccountException {
		if (!photoAccountId.equals(sessionHelper.getAccountId())) {
			throw new ForbiddenAccountException(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO);
		}
		photoSettingRequest.setAccountNo(sessionHelper.getAccountNo());

		ModelAndView mv = getModelAndView("photo_setting", photoAccountId);
		mv.addObject("PhotoSettingRequest", photoSettingRequest);
		mv.addObject("maxFileSizeMb", photoConfig.getMaxFileSizeMb());
		mv.addObject("isAddMode", Objects.isNull(photoSettingRequest.getPhotoNo()));
		
		List<PhotoTagRequest> photoTagRequestList = photoSettingRequest.getPhotoTagRequestList();
		
		if (!Objects.isNull(photoTagRequestList)) {
			Integer maxTagNo = photoTagRequestList.stream()
					.map(photoTagRequest -> photoTagRequest.getTagNo())
					.max(Comparator.naturalOrder())
					.get();
			mv.addObject("maxTagNo", maxTagNo);
		}

		return mv;
	}

	/**
	 * 各画面で共通のObjectを生成し、ModelAndViewを返却する
	 * 
	 * @param	viewName		ビューの名称
	 * @param	photoAccountId	ページ所有者のアカウントID
	 * @return	ModelAndView	各種ページのリンクをModelとして返すオブジェクト
	 */
	private ModelAndView getModelAndView(String viewName, String photoAccountId) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName(viewName);
		mv.addObject("accountName", accountServiceImpl.getAccountById(photoAccountId).getAccountName());

		mv.addObject("account_list_url", ApiRoutes.ACCOUNT_LIST);
		mv.addObject("photo_list_url",   PhotoUrlUtil.getPhotoListUrl(photoAccountId));
		mv.addObject("photo_detail_url", PhotoUrlUtil.getPhotoDetailUrl(photoAccountId));

		String accountId = sessionHelper.getAccountId();

		if (!Objects.isNull(accountId)) {
			mv.addObject("account_setting_url", AccountUrlUtil.getAccountSettingUrl(accountId));
			mv.addObject("my_photo_list_url", PhotoUrlUtil.getPhotoListUrl(accountId));
			mv.addObject("photo_setting_url", PhotoUrlUtil.getPhotoSettingUrl(accountId));
			mv.addObject("photo_delete_url",  PhotoUrlUtil.getPhotoApiUrl(accountId));
			mv.addObject("photo_save_url",    PhotoUrlUtil.getPhotoApiUrl(accountId));
		}

		return mv;
	}
}