package com.web.gallery.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.controller.request.PhotoDeleteRequest;
import com.web.gallery.controller.request.PhotoListRequest;
import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.controller.response.PhotoDetailGetResponse;
import com.web.gallery.controller.response.PhotoEditResponse;
import com.web.gallery.controller.response.PhotoListGetResponse;
import com.web.gallery.controller.response.PhotoUpperLimitResponse;
import com.web.gallery.enumuration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "写真", description = "写真管理に関するAPI")
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
	@Operation(summary = "写真一覧取得", description = "抽出条件に該当する写真を、指定の並び順で取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@GetMapping(ApiRoutes.API_PHOTOS)
	public ResponseEntity<PhotoListGetResponse> getPhotoList(
			@PathVariable String photoAccountId,
			@ModelAttribute @Validated PhotoListRequest photoListRequest) {
		// 抽出条件に該当する写真の一覧を、指定の並び順で取得する
		PhotoModelList photoList = photoService.getPhotoList(
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
	@Operation(summary = "写真登録上限チェック", description = "指定のアカウントが写真の登録枚数の上限に達しているかをチェックする")
	@ApiResponse(responseCode = "200", description = "チェック成功")
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
	@Operation(summary = "写真詳細取得", description = "写真の詳細情報（EXIF・タグを含む）を取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@ApiResponse(responseCode = "404", description = "写真が存在しない", content = @Content)
	@GetMapping(ApiRoutes.API_PHOTO_DETAIL)
	public ResponseEntity<PhotoDetailGetResponse> getPhotoDetail(
			@PathVariable String photoAccountId,
			@PathVariable Long photoNo,
			Long accountNo) throws PhotoNotFoundException {

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
	@Operation(summary = "写真保存", description = "写真を新規登録または更新する")
	@ApiResponse(responseCode = "200", description = "保存成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "403", description = "写真の所有者以外によるリクエスト", content = @Content)
	@ApiResponse(responseCode = "409", description = "ファイルが重複または登録失敗", content = @Content)
	@SecurityRequirement(name = "Bearer")
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
		
		List<PhotoDetailModel> photoDetailModelList = List.of(PhotoDetailModel.from(photoSaveRequest));

		Long savedPhotoNo = photoService.savePhotos(photoAccountId, photoDetailModelList);

		String savedImageFilePath;
		if (Objects.isNull(photoSaveRequest.getPhotoNo()) && !Objects.isNull(photoSaveRequest.getImageFile())) {
			savedImageFilePath = photoConfig.getOutputPath() + photoAccountId + "/" + photoSaveRequest.getImageFile().getOriginalFilename();
		} else {
			savedImageFilePath = Optional.ofNullable(photoSaveRequest.getImageFilePath()).orElse(Consts.STRING_EMPTY);
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
	@Operation(summary = "写真削除", description = "指定した写真を削除する")
	@ApiResponse(responseCode = "200", description = "削除成功")
	@ApiResponse(responseCode = "400", description = "リクエストパラメータ不正", content = @Content)
	@ApiResponse(responseCode = "403", description = "写真の所有者以外によるリクエスト", content = @Content)
	@ApiResponse(responseCode = "409", description = "削除失敗", content = @Content)
	@SecurityRequirement(name = "Bearer")
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