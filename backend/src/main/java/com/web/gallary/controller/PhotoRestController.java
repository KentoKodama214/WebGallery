package com.web.gallary.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallary.config.PhotoConfig;
import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.constant.Consts;
import com.web.gallary.constant.MessageConst;
import com.web.gallary.controller.request.PhotoDeleteRequest;
import com.web.gallary.controller.request.PhotoListRequest;
import com.web.gallary.controller.request.PhotoSaveRequest;
import com.web.gallary.controller.response.PhotoDetailGetResponse;
import com.web.gallary.controller.response.PhotoEditResponse;
import com.web.gallary.controller.response.PhotoListGetResponse;
import com.web.gallary.controller.response.PhotoUpperLimitResponse;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotAdditableException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoListGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.service.PhotoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真に関するAPI通信を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Slf4j
@RestController
@RequiredArgsConstructor
public class PhotoRestController {
	
	private final PhotoService photoService;
	private final SessionHelper sessionHelper;
	private final PhotoConfig photoConfig;
	
	/**
	 * 写真一覧の写真取得<p>
	 * リクエストの抽出条件に該当する写真を、指定の並び順で取得する
	 * 
	 * @param	photoAccountId		ページ所有者のアカウントID
	 * @param	photoListRequest	{@link PhotoListRequest}
	 * @return						{@link PhotoListGetResponse}
	 */
	@GetMapping(ApiRoutes.API_PHOTOS)
	public ResponseEntity<PhotoListGetResponse> getPhotoList(
			@PathVariable String photoAccountId,
			@ModelAttribute @Validated PhotoListRequest photoListRequest) {
		// 抽出条件に該当する写真の一覧を、指定の並び順で取得する
		List<PhotoModel> photoList = photoService.getPhotoList(
				PhotoListGetModel.from(photoListRequest, sessionHelper.getAccountNo(), photoAccountId));
		return ResponseEntity.ok(PhotoListGetResponse.from(photoList, photoListRequest.getPageNo(), photoConfig.getPhotoCountPerPage()));
	}

	/**
	 * 写真登録上限チェック<p>
	 * 指定のアカウントが写真の登録枚数の上限に達しているかをチェックする
	 *
	 * @param	photoAccountId		ページ所有者のアカウントID
	 * @return						{@link PhotoUpperLimitResponse}
	 */
	@GetMapping(ApiRoutes.API_PHOTO_UPPER_LIMIT)
	public ResponseEntity<PhotoUpperLimitResponse> getPhotoUpperLimit(
			@PathVariable String photoAccountId) {
		Boolean isReachedUpperLimit = false;
		if (photoAccountId.equals(sessionHelper.getAccountId())) {
			isReachedUpperLimit = photoService.isReachedUpperLimit(sessionHelper.getAccountNo());
		}
		return ResponseEntity.ok(PhotoUpperLimitResponse.of(isReachedUpperLimit));
	}

	/**
	 * 写真詳細取得
	 *
	 * @param	photoAccountId		ページ所有者のアカウントID
	 * @param	photoNo				写真番号
	 * @param	accountNo			写真所有者のアカウント番号
	 * @return						{@link PhotoDetailGetResponse}
	 * @throws	PhotoNotFoundException	写真が存在しない場合
	 */
	@GetMapping(ApiRoutes.API_PHOTO_DETAIL)
	public ResponseEntity<PhotoDetailGetResponse> getPhotoDetail(
			@PathVariable String photoAccountId,
			@PathVariable Integer photoNo,
			Integer accountNo) throws PhotoNotFoundException {

		PhotoDetailModel photoDetailModel = photoService.getPhotoDetail(
				PhotoDetailGetModel.of(sessionHelper.getAccountNo(), accountNo, photoNo));

		return ResponseEntity.ok(PhotoDetailGetResponse.from(photoDetailModel));
	}

	/**
	 * 写真保存
	 * 
	 * @param	photoAccountId				ページ所有者のアカウントID
	 * @param	photoSaveRequest			{@link PhotoSaveRequest}
	 * @param	result						PhotoSaveRequestのバインディング結果
	 * @return								{@link PhotoEditResponse}
	 * @throws	ForbiddenAccountException 	写真の所有者以外がリクエストした場合
	 * @throws	PhotoNotAdditableException	写真の登録枚数の上限に達している場合
	 * @throws	BadRequestException 		リクエストパラメータが不正の場合
	 * @throws	FileDuplicateException 		保存するファイルが重複した場合
	 * @throws	RegistFailureException 		写真の登録に失敗した場合
	 * @throws	UpdateFailureException 		写真の更新に失敗した場合
	 */
	@RequestMapping(value = ApiRoutes.API_PHOTOS, method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<PhotoEditResponse> savePhoto(
			@PathVariable String photoAccountId, 
			@ModelAttribute @Validated PhotoSaveRequest photoSaveRequest, 
			BindingResult result) throws FileDuplicateException, ForbiddenAccountException, RegistFailureException, UpdateFailureException, BadRequestException, PhotoNotAdditableException {
		
		if(!photoAccountId.equals(sessionHelper.getAccountId())) {
			throw new ForbiddenAccountException(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO);
		}

		if(Objects.isNull(photoSaveRequest.getPhotoNo()) && photoService.isReachedUpperLimit(sessionHelper.getAccountNo())) {
			throw new PhotoNotAdditableException(ErrorEnum.REACHED_REGISTRATION_LIMIT);
		}
		
		if(Objects.isNull(photoSaveRequest.getImageFile()) && 
				(Objects.isNull(photoSaveRequest.getImageFilePath()) || Consts.STRING_EMPTY.equals(photoSaveRequest.getImageFilePath()))) {
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		for(FieldError error : result.getFieldErrors()) {
			if(!error.isBindingFailure()) {
				log.info("Invalid input. (Field: {}, Value: {}, Message: {})",
						error.getField(), error.getRejectedValue(), error.getDefaultMessage());
				throw new BadRequestException(ErrorEnum.INVALID_INPUT);
			}
		};
		
		List<PhotoTagModel> photoTagModelList = Objects.isNull(photoSaveRequest.getPhotoTagRegistRequestList())
				? new ArrayList<PhotoTagModel>()
				: photoSaveRequest.getPhotoTagRegistRequestList().stream()
						.map(PhotoTagModel::from)
						.collect(Collectors.toList());

		List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
		photoDetailModelList.add(PhotoDetailModel.from(photoSaveRequest, photoTagModelList));
		
		Integer savedPhotoNo = photoService.savePhotos(photoAccountId, photoDetailModelList);

		String savedImageFilePath;
		if (Objects.isNull(photoSaveRequest.getPhotoNo()) && !Objects.isNull(photoSaveRequest.getImageFile())) {
			savedImageFilePath = photoConfig.getOutputPath() + photoAccountId + "/" + photoSaveRequest.getImageFile().getOriginalFilename();
		} else {
			savedImageFilePath = Optional.ofNullable(photoSaveRequest.getImageFilePath()).orElse("");
		}

		return ResponseEntity.ok(PhotoEditResponse.of(MessageConst.REGIST_PHOTO, savedPhotoNo, savedImageFilePath));
	}
	
	/**
	 * 写真削除
	 * 
	 * @param photoAccountId				ページ所有者のアカウントID
	 * @param photoDeleteRequest			{@link PhotoDeleteRequest}
	 * @param result						PhotoDeleteRequestのバインディング結果
	 * @return								{@link PhotoEditResponse}
	 * @throws BadRequestException 			リクエストパラメータが不正の場合
	 * @throws ForbiddenAccountException 	写真の所有者以外がリクエストした場合
	 * @throws UpdateFailureException 		写真の削除に失敗した場合
	 */
	@DeleteMapping(ApiRoutes.API_PHOTOS)
	public ResponseEntity<PhotoEditResponse> deletePhoto(
			@PathVariable String photoAccountId, 
			@RequestBody @Validated PhotoDeleteRequest photoDeleteRequest, 
			BindingResult result) throws BadRequestException, ForbiddenAccountException, UpdateFailureException {
		
		if(!photoAccountId.equals(sessionHelper.getAccountId())) {
			throw new ForbiddenAccountException(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO);
		}
		
		if(result.hasErrors()) {
			log.info("Invalid input. (AccountNo: {}, PhotoNo: {}, ImageFilePath: {})",
					photoDeleteRequest.getAccountNo(), photoDeleteRequest.getPhotoNo(), photoDeleteRequest.getImageFilePath());
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}
		
		List<PhotoDeleteModel> photoDeleteModelList = List.of(PhotoDeleteModel.from(photoDeleteRequest));

		photoService.deletePhotos(photoAccountId, photoDeleteModelList);
		
		return ResponseEntity.ok(PhotoEditResponse.of(MessageConst.DELETE_PHOTO, null, null));
	}
	
}