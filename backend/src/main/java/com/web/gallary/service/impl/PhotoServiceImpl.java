package com.web.gallary.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallary.config.PhotoConfig;
import com.web.gallary.constant.Consts;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.enumuration.SortPhotoEnum;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.FileModel;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoGetModel;
import com.web.gallary.model.PhotoListGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.model.PhotoTagDeleteModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.repository.AccountRepository;
import com.web.gallary.repository.FileRepository;
import com.web.gallary.repository.PhotoDetailRepository;
import com.web.gallary.repository.PhotoFavoriteRepository;
import com.web.gallary.repository.PhotoMstRepository;
import com.web.gallary.repository.PhotoTagMstRepository;
import com.web.gallary.service.PhotoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真に関するビジネスロジックを行うServiceの実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

	private final PhotoDetailRepository photoDetailRepository;
	private final PhotoMstRepository photoMstRepository;
	private final PhotoTagMstRepository photoTagMstRepository;
	private final PhotoFavoriteRepository photoFavoriteRepository;
	private final AccountRepository accountRepository;
	private final FileRepository fileRepository;
	private final PhotoConfig photoConfig;
	
	/**
	 * 写真一覧を取得する
	 * 
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @return						{@link PhotoModel}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PhotoModel> getPhotoList(PhotoListGetModel photoListGetModel) {
		Account account = accountRepository.getByAccountId(photoListGetModel.getPhotoAccountId());
		
		List<PhotoModel> photoModelList
			= photoDetailRepository.getPhotoList(PhotoGetModel.builder()
					.accountNo(photoListGetModel.getAccountNo())
					.photoAccountNo(account.getAccountNo())
					.build());
		
		return photoModelList.stream()
					.filter(photoModel -> 
						filteringByDirectionKbn(photoModel.getDirectionKbn(), photoListGetModel.getDirectionKbn()))
					.filter(photoModel -> 
						filteringByIsFavorite(photoModel.getIsFavorite(), photoListGetModel.getIsFavoriteOnly()))
					.filter(photoModel -> 
						filteringByTag(photoModel.getPhotoTagModelList(), photoListGetModel.getTagList()))
					.sorted(getComparator(photoListGetModel.getSortBy()))
					.toList();
	}

	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 * 
	 * @param	photoDetailGetModel		{@link PhotoDetailGetModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	PhotoNotFoundException	写真が存在しなかった場合
	 */
	@Override
	@Transactional(readOnly = true)
	public PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws PhotoNotFoundException {
		return photoDetailRepository.getPhotoDetail(photoDetailGetModel);
	}
	
	/**
	 * 写真を登録・更新する
	 * 
	 * @param	photoDetailModelList	{@link PhotoDetailModel}
	 * @throws	FileDuplicateException 	同じファイル名のファイルが既に保存済みの場合
	 * @throws	RegistFailureException	登録に失敗した場合
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Override
	@Transactional
	public void savePhotos(String accountId, List<PhotoDetailModel> photoDetailModelList) throws FileDuplicateException, RegistFailureException, UpdateFailureException {
		if(Objects.isNull(photoDetailModelList)) return;
		if(photoDetailModelList.isEmpty()) return;
		
		Integer photoNo = photoMstRepository.getNewPhotoNo(photoDetailModelList.getFirst().getAccountNo());
		String filePath = photoConfig.getOutputPath() + accountId + "/";
		
		for(PhotoDetailModel photoDetailModel : photoDetailModelList){
			if(Objects.isNull(photoDetailModel.getPhotoNo())) {
				String filename = photoDetailModel.getImageFile().getOriginalFilename();
				if(photoMstRepository.isExistPhoto(photoDetailModel)) {
					log.warn("Duplicate image file (filename: {}}", filename);
					throw new FileDuplicateException(ErrorEnum.DUPLICATE_PHOTO_FILE);
				}
				
				photoMstRepository.regist(photoDetailModel, filePath + filename, photoNo);
				registPhotoTags(photoDetailModel.getPhotoTagModelList(), photoNo++);
				uploadFile(filePath + filename, photoDetailModel.getImageFile());
			} else {
				photoMstRepository.update(photoDetailModel);
				deletePhotoTags(photoDetailModel.getAccountNo(), photoDetailModel.getPhotoNo());
				registPhotoTags(photoDetailModel.getPhotoTagModelList(), null);
			}
		}
	}
	
	/**
	 * 写真を削除する
	 * 
	 * @param	accountId				アカウントID
	 * @param	photoDeleteModelList	{@link PhotoDeleteModel}
	 * @throws	UpdateFailureException	削除に失敗した場合
	 */
	@Override
	@Transactional
	public void deletePhotos(String accountId, List<PhotoDeleteModel> photoDeleteModelList) throws UpdateFailureException {
		String filePath = photoConfig.getOutputPath() + accountId + "/";
		
		for(PhotoDeleteModel photoDeleteModel : photoDeleteModelList) {
			photoFavoriteRepository.clear(
				PhotoFavoriteDeleteModel.builder()
					.favoritePhotoAccountNo(photoDeleteModel.getAccountNo())
					.favoritePhotoNo(photoDeleteModel.getPhotoNo())
					.build()
			);
			deletePhotoTags(photoDeleteModel.getAccountNo(), photoDeleteModel.getPhotoNo());
			
			photoMstRepository.delete(photoDeleteModel);
			
			String fileName = new File(photoDeleteModel.getImageFilePath()).getName();
			fileRepository.delete(filePath + fileName);
		}
	}
	
	/**
	 * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
	 * 
	 * @param	accountNo	アカウント番号
	 * @return				上限に達している場合、true
	 */
	@Override
	@Transactional(readOnly = true)
	public Boolean isReachedUpperLimit(Integer accountNo) {
		if(Objects.isNull(accountNo)) return true;
		
		Account account = accountRepository.getByAccountNo(accountNo);
		Integer count = photoMstRepository.count(accountNo);
		
		switch(account.getAuthorityKbn()) {
			case MINI:
				return count > (photoConfig.getMiniUserUpperLimit() - 1);
			case NORMAL:
				return count > (photoConfig.getNormalUserUpperLimit() - 1);
			case SPECIAL:
			case ADMINISTRATOR:
				return false;
			default:
				return true;
		}
	}
	
	/**
	 * 写真一覧の並び順のComparatorを取得する
	 * 
	 * @param	sortBy	{@link SortPhotoEnum}
	 * @return			{@link PhotoModel}のComparator
	 */
	private Comparator<PhotoModel> getComparator(SortPhotoEnum sortBy) {
		switch(sortBy) {
			case PHOTO_AT:
				return Comparator.comparing(PhotoModel::getPhotoAt).reversed();
			case FAVORITE:
				return Comparator.comparing(PhotoModel::getFavoriteCount).reversed();
			case SEASON:
				return new Comparator<PhotoModel>() {
					@Override
					public int compare(PhotoModel photoModelA, PhotoModel photoModelB) {
						OffsetDateTime photoAtA = photoModelA.getPhotoAt().plusHours(9);
						OffsetDateTime photoAtB = photoModelB.getPhotoAt().plusHours(9);
						
						LocalDate dateA = LocalDate.of(2000, photoAtA.getMonth().getValue(), photoAtA.getDayOfMonth()); 
						LocalDate dateB = LocalDate.of(2000, photoAtB.getMonth().getValue(), photoAtB.getDayOfMonth()); 
						
						return (int) ChronoUnit.DAYS.between(dateA, dateB);
					}
				};
			default:
				return Comparator.comparing(PhotoModel::getPhotoAt).reversed();
		}
	}
	
	/**
	 * 写真の向きでフィルタリングする
	 * 
	 * @param	targetDirectionKbn	フィルター対象の向き区分
	 * @param	conditionDirectionKbn	フィルター条件の向き区分
	 * @return	フィルタリングして除外する場合はfalse
	 */
	private Boolean filteringByDirectionKbn(DirectionEnum targetDirectionKbn, DirectionEnum conditionDirectionKbn) {
		if(DirectionEnum.NONE.equals(conditionDirectionKbn)) return true;
		else return targetDirectionKbn.equals(conditionDirectionKbn);
	}
	
	/**
	 * お気に入りでフィルタリングする
	 * 
	 * @param	isFavorite		写真がお気に入りならtrue
	 * @param	isFavoriteOnly	お気に入りに絞るならtrue
	 * @return					フィルタリングして除外する場合はfalse
	 */
	private Boolean filteringByIsFavorite(Boolean isFavorite, Boolean isFavoriteOnly) {
		if(!isFavoriteOnly) return true;
		else return isFavorite;
	}
	
	/**
	 * タグでフィルタリングする<p>
	 * タグが複数ある場合、すべてのタグを持つ写真にフィルタリングする
	 * 
	 * @param	photoTagModelList	{@link PhotoTagModel}
	 * @param	tags				フィルター条件のタグのリスト
	 * @return						フィルタリングして除外する場合はfalse
	 */
	private Boolean filteringByTag(List<PhotoTagModel> photoTagModelList, List<String> tags) {
		if(tags.size() == 0 || Consts.STRING_EMPTY.equals(tags.getFirst())) return true;
		
		List<String> photoTags = new ArrayList<String>();
		photoTags.addAll(photoTagModelList.stream().map(photoTagModel -> photoTagModel.getTagJapaneseName()).toList());
		photoTags.addAll(photoTagModelList.stream().map(photoTagModel -> photoTagModel.getTagEnglishName()).toList());
		
		return photoTags.containsAll(tags);
	}
	
	/**
	 * 写真タグを登録する
	 * 
	 * @param	photoTagModelList		{@link PhotoTagModel}
	 * @param	newPhotoNo				新規採番された写真番号
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	private void registPhotoTags(List<PhotoTagModel> photoTagModelList, Integer newPhotoNo) throws RegistFailureException {
		if(Objects.isNull(photoTagModelList)) return; 
		
		int tagNo = 1;
		for(PhotoTagModel photoTagModel : photoTagModelList) {
			PhotoTagModel photoTagRegistModel = PhotoTagModel.builder()
					.accountNo(photoTagModel.getAccountNo())
					.photoNo(!Objects.isNull(newPhotoNo) ? newPhotoNo : photoTagModel.getPhotoNo())
					.tagNo(tagNo)
					.tagJapaneseName(photoTagModel.getTagJapaneseName())
					.tagEnglishName(photoTagModel.getTagEnglishName())
					.build();
			photoTagMstRepository.regist(photoTagRegistModel);
			++tagNo;
		}
	}
	
	/**
	 * ファイルをアップロードする
	 * 
	 * @param	filePath	アップロードのファイルパス
	 * @param	imageFile	アップロードするファイル
	 */
	private void uploadFile(String filePath, MultipartFile imageFile) {
		fileRepository.save(
			FileModel.builder()
				.filePath(filePath)
				.imageFile(imageFile)
				.build()
		);
	}
	
	/**
	 * 写真タグを一括削除する
	 * 
	 * @param	accountNo	削除する写真のアカウント番号
	 * @param	photoNo		削除する写真の写真番号
	 */
	private void deletePhotoTags(Integer accountNo, Integer photoNo) {
		photoTagMstRepository.clear(
			PhotoTagDeleteModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.build()
		);
	}
}