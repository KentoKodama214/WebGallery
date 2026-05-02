package com.web.gallary.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.web.gallary.controller.response.PhotoEditResponse;
import com.web.gallary.controller.response.PhotoListGetResponse;
import com.web.gallary.controller.response.PhotoListResponse;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotAdditableException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoDeleteModel;
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
		Optional<String> tagsOpt = Optional.ofNullable(photoListRequest.getTagList());
		photoListRequest.setTagList(tagsOpt.map(tag -> tag.replace(Consts.HALF_SPACE, Consts.FULL_SPACE)).orElse(Consts.STRING_EMPTY));
		List<String> tagList = tagsOpt.map(tag -> 
			new ArrayList<String>(Arrays.asList(tag.replace(Consts.FULL_SPACE, Consts.HALF_SPACE).split(Consts.HALF_SPACE)))).orElse(new ArrayList<String>());
		
		// 抽出条件に該当する写真の一覧を、指定の並び順で取得する
		List<PhotoModel> photoList = photoService.getPhotoList(
				PhotoListGetModel.builder()
					.accountNo(sessionHelper.getAccountNo())
					.photoAccountId(photoAccountId)
					.directionKbn(photoListRequest.getDirectionKbn())
					.isFavoriteOnly(Optional.ofNullable(photoListRequest.getIsFavorite()).orElse(Boolean.FALSE))
					.tagList(tagList)
					.sortBy(photoListRequest.getSortBy())
					.build()
			);
		return ResponseEntity.ok(createPhotoListGetResponse(photoList, photoListRequest.getPageNo()));
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
		
		List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
		
		if(!Objects.isNull(photoSaveRequest.getPhotoTagRegistRequestList())) {
			photoSaveRequest.getPhotoTagRegistRequestList().stream().forEach(photoTag -> {
				photoTagModelList.add(
					PhotoTagModel.builder()
						.accountNo(photoTag.getAccountNo())
						.photoNo(photoTag.getPhotoNo())
						.tagNo(photoTag.getTagNo())
						.tagJapaneseName(Optional.ofNullable(photoTag.getTagJapaneseName()).orElse(Consts.STRING_EMPTY))
						.tagEnglishName(Optional.ofNullable(photoTag.getTagEnglishName()).orElse(Consts.STRING_EMPTY))
						.build()
				);
			});
		}
		
		List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
		photoDetailModelList.add(
			PhotoDetailModel.builder()
				.accountNo(photoSaveRequest.getAccountNo())
				.photoNo(photoSaveRequest.getPhotoNo())
				.isFavorite(photoSaveRequest.getIsFavorite())
				.photoAt(
					Optional.ofNullable(photoSaveRequest.getPhotoAt())
						.map(photoAt -> photoAt.atOffset(Consts.JST)).orElse(null))
				.locationNo(photoSaveRequest.getLocationNo())
				.address(photoSaveRequest.getAddress())
				.latitude(photoSaveRequest.getLatitude())
				.longitude(photoSaveRequest.getLongitude())
				.locationName(photoSaveRequest.getLocationName())
				.imageFile(photoSaveRequest.getImageFile())
				.imageFilePath(photoSaveRequest.getImageFilePath())
				.photoJapaneseTitle(photoSaveRequest.getPhotoJapaneseTitle())
				.photoEnglishTitle(photoSaveRequest.getPhotoEnglishTitle())
				.caption(photoSaveRequest.getCaption())
				.directionKbn(photoSaveRequest.getDirectionKbn())
				.focalLength(photoSaveRequest.getFocalLength())
				.fValue(photoSaveRequest.getFValue())
				.shutterSpeed(photoSaveRequest.getShutterSpeed())
				.iso(photoSaveRequest.getIso())
				.photoTagModelList(photoTagModelList)
				.build()
		);
		
		photoService.savePhotos(photoAccountId, photoDetailModelList);
		
		return ResponseEntity.ok(PhotoEditResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(MessageConst.REGIST_PHOTO)
				.build());
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
		
		List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
		photoDeleteModelList.add(
			PhotoDeleteModel.builder()
				.accountNo(photoDeleteRequest.getAccountNo())
				.photoNo(photoDeleteRequest.getPhotoNo())
				.imageFilePath(photoDeleteRequest.getImageFilePath())
				.build()
		);
		
		photoService.deletePhotos(photoAccountId, photoDeleteModelList);
		
		return ResponseEntity.ok(PhotoEditResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(MessageConst.DELETE_PHOTO)
				.build());
	}
	
	/**
	 * ページ番号から写真のリストを絞り込み、写真一覧のレスポンスのクラスへ詰め替えをする
	 * 
	 * @param	photoList	{@link PhotoModel}
	 * @param	pageNo		ページ番号
	 * @return				{@link PhotoListGetResponse}
	 */
	private PhotoListGetResponse createPhotoListGetResponse(List<PhotoModel> photoList, Integer pageNo) {
		Integer photoCountPerPage = photoConfig.getPhotoCountPerPage();
		List<PhotoListResponse> photoListResponseList = new ArrayList<PhotoListResponse>();
		
		photoList.subList(
			(pageNo - 1) * photoCountPerPage, 
			Math.min(pageNo * photoCountPerPage, photoList.size())).forEach(photo -> {
				photoListResponseList.add(PhotoListResponse.builder()
						.accountNo(photo.getAccountNo())
						.photoNo(photo.getPhotoNo())
						.isFavorite(photo.getIsFavorite())
						.imageFilePath(photo.getImageFilePath())
						.caption(photo.getCaption())
						.directionKbn(photo.getDirectionKbn())
						.build());
			});
		
		return PhotoListGetResponse.builder()
				.isLast(pageNo * photoCountPerPage >= photoList.size())
				.photoList(photoListResponseList)
				.build();
	}
}