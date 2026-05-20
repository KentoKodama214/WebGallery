package com.web.gallery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.PhotoFavoriteDeleteRequest;
import com.web.gallery.controller.request.PhotoFavoriteRegistRequest;
import com.web.gallery.controller.response.PhotoFavoriteResponse;
import com.web.gallery.enumuration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.service.PhotoFavoriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真のお気に入りの登録・解除を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Slf4j
@RestController
@RequiredArgsConstructor
public class PhotoFavoriteController {
	
	private final PhotoFavoriteService photoFavoriteService;
	private final SessionHelper sessionHelper;

	/**
	 * お気に入り登録
	 * 
	 * @param	photoFavoriteRegistRequest	{@link PhotoFavoriteRegistRequest}
	 * @param	result						PhotoFavoriteRegistRequestのバインディング結果
	 * @return	PhotoFavoriteResponse		{@link PhotoFavoriteResponse}
	 * @throws	BadRequestException 		リクエストパラメータが不正の場合
	 * @throws	RegistFailureException 		お気に入りの登録に失敗した場合
	 */
	@PostMapping(ApiRoutes.API_FAVORITES)
	public ResponseEntity<PhotoFavoriteResponse> addFavorite(
			@RequestBody @Validated PhotoFavoriteRegistRequest photoFavoriteRegistRequest,
			BindingResult result) throws BadRequestException, RegistFailureException {
		
		if(result.hasErrors()) {
			log.info("Invalid input. (FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
					photoFavoriteRegistRequest.getFavoritePhotoAccountNo(), photoFavoriteRegistRequest.getFavoritePhotoNo());
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		photoFavoriteService.addFavorite(PhotoFavoriteModel.from(photoFavoriteRegistRequest, sessionHelper.getAccountNo()));
		
		return ResponseEntity.ok(PhotoFavoriteResponse.of(MessageConst.REGIST_FAVORITE));
	}

	/**
	 * お気に入り解除
	 * 
	 * @param	photoFavoriteDeleteRequest	{@link PhotoFavoriteDeleteRequest}
	 * @param	result						PhotoFavoriteDeleteRequestのバインディング結果
	 * @return	PhotoFavoriteResponse		{@link PhotoFavoriteResponse}
	 * @throws	BadRequestException 		リクエストパラメータが不正の場合
	 * @throws	UpdateFailureException 		お気に入りの解除に失敗した場合
	 */
	@DeleteMapping(ApiRoutes.API_FAVORITES)
	public ResponseEntity<PhotoFavoriteResponse> deleteFavorite(
			@RequestBody @Validated PhotoFavoriteDeleteRequest photoFavoriteDeleteRequest,
			BindingResult result) throws BadRequestException, UpdateFailureException {

		if(result.hasErrors()) {
			log.info("Invalid input. (FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
					photoFavoriteDeleteRequest.getFavoritePhotoAccountNo(), photoFavoriteDeleteRequest.getFavoritePhotoNo());
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		photoFavoriteService.deleteFavorite(PhotoFavoriteModel.from(photoFavoriteDeleteRequest, sessionHelper.getAccountNo()));
		
		return ResponseEntity.ok(PhotoFavoriteResponse.of(MessageConst.CANCEL_FAVORITE));
	}
}