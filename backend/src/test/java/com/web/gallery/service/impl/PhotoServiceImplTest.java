package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.model.AccountModel;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.event.PhotoRegisteredEvent;
import com.web.gallery.event.PhotoUpdatedEvent;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoPageModel;
import com.web.gallery.model.PhotoSaveResultModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import com.web.gallery.policy.ImageFileValidationPolicy;
import com.web.gallery.policy.PhotoFileExtensionPolicy;
import com.web.gallery.policy.PhotoQuotaPolicy;
import com.web.gallery.repository.impl.AccountRepositoryImpl;
import com.web.gallery.repository.impl.FileRepositoryImpl;
import com.web.gallery.repository.impl.PhotoAggregateRepositoryImpl;
import com.web.gallery.repository.impl.PhotoDetailRepositoryImpl;
import com.web.gallery.repository.impl.PhotoMstRepositoryImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoServiceImplTest {
	@InjectMocks
	private PhotoServiceImpl photoServiceImpl;
	
	@Mock
	private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;
	
	@Mock
	private PhotoMstRepositoryImpl photoMstRepositoryImpl;
	
	@Mock
	private PhotoAggregateRepositoryImpl photoAggregateRepositoryImpl;

	@Mock
	private AccountRepositoryImpl accountRepositoryImpl;

	@Mock
	private FileRepositoryImpl fileRepositoryImpl;

	@Mock
	private PhotoConfig photoConfig;

	@Mock
	private PhotoQuotaPolicy photoQuotaPolicy;

	@Mock
	private ImageFileValidationPolicy imageFileValidationPolicy;

	@Mock
	private PhotoFileExtensionPolicy photoFileExtensionPolicy;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoList {
		PhotoModelList createPhotoModelList() {
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			
			List<PhotoTagModel> photoTagModelList1 = new ArrayList<PhotoTagModel>();
			photoTagModelList1.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList1.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			PhotoModel photoModel1 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC111.jpg"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList1))
					.build();
			photoModelList.add(photoModel1);
			
			List<PhotoTagModel> photoTagModelList2 = new ArrayList<PhotoTagModel>();
			photoTagModelList2.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList2.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			PhotoModel photoModel2 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.favoriteCount(new FavoriteCount(3))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC222.jpg"))
					.caption(new Caption("キャプション2"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList2))
					.build();
			photoModelList.add(photoModel2);
			
			List<PhotoTagModel> photoTagModelList3 = new ArrayList<PhotoTagModel>();
			photoTagModelList3.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList3.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			PhotoModel photoModel3 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.favoriteCount(new FavoriteCount(2))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC333.jpg"))
					.caption(new Caption("キャプション3"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList3))
					.build();
			photoModelList.add(photoModel3);
			
			List<PhotoTagModel> photoTagModelList4 = new ArrayList<PhotoTagModel>();
			photoTagModelList4.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			PhotoModel photoModel4 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.favoriteCount(new FavoriteCount(4))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 4, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC444.jpg"))
					.caption(new Caption("キャプション4"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList4))
					.build();
			photoModelList.add(photoModel4);
			
			List<PhotoTagModel> photoTagModelList5 = new ArrayList<PhotoTagModel>();
			photoTagModelList5.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			PhotoModel photoModel5 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.favoriteCount(new FavoriteCount(10))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 5, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC444.jpg"))
					.caption(new Caption("キャプション4"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList5))
					.build();
			photoModelList.add(photoModel5);
			
			PhotoModel photoModel6 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(6L))
					.favoriteCount(new FavoriteCount(0))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC666.jpg"))
					.caption(new Caption("キャプション6"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build();
			photoModelList.add(photoModel6);

			return PhotoModelList.of(photoModelList);
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：写真が存在しなかった場合")
		void getPhotoList_not_found() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<String> tags = new ArrayList<String>();

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			doReturn(5).when(photoConfig).getPhotoCountPerPage();

			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(PhotoPageModel.of(PhotoModelList.empty(), true)).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertTrue(actual.getPhotoModelList().isEmpty());
			assertTrue(actual.getIsLast());
			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));

			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
			assertEquals(DirectionEnum.NONE, photoGetModel.getDirectionKbn());
			assertFalse(photoGetModel.getIsFavoriteOnly().value());
			assertEquals(tags, photoGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoGetModel.getSortBy());
			assertEquals(6, photoGetModel.getLimit());
			assertEquals(0, photoGetModel.getOffset());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：sortByがSEASON以外の場合、フィルタリング・ソート済みのRepositoryの取得結果をそのまま返すこと")
		void getPhotoList_passThrough_when_sortBy_is_not_season() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			doReturn(5).when(photoConfig).getPhotoCountPerPage();

			PhotoModelList repositoryResult = createPhotoModelList();
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(PhotoPageModel.of(repositoryResult, false)).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(true))
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(repositoryResult.toList(), actual.getPhotoModelList().toList());
			assertFalse(actual.getIsLast());

			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));

			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
			assertEquals(DirectionEnum.VERTICAL, photoGetModel.getDirectionKbn());
			assertTrue(photoGetModel.getIsFavoriteOnly().value());
			assertEquals(tags, photoGetModel.getTagList());
			assertEquals(SortPhotoEnum.FAVORITE, photoGetModel.getSortBy());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：sortByがSEASONの場合、季節・時期順に並び替えられること")
		void getPhotoList_sortBy_season() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			doReturn(5).when(photoConfig).getPhotoCountPerPage();

			// SQL側で絞り込み済みの想定で、季節順とは異なる並びでRepositoryの結果をモックする
			List<PhotoModel> repositoryResultList = new ArrayList<PhotoModel>();
			repositoryResultList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.favoriteCount(new FavoriteCount(2))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC333.jpg"))
					.caption(new Caption("キャプション3"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			repositoryResultList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC111.jpg"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			repositoryResultList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.favoriteCount(new FavoriteCount(3))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("DSC222.jpg"))
					.caption(new Caption("キャプション2"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());

			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(PhotoPageModel.of(PhotoModelList.of(repositoryResultList), true)).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(3, actual.getPhotoModelList().size());
			assertEquals(1L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(2L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(3L, actual.getPhotoModelList().get(2).getPhotoNo().value());
			assertTrue(actual.getIsLast());

			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));

			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
			assertEquals(SortPhotoEnum.SEASON, photoGetModel.getSortBy());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：指定のアカウントが存在しない場合、PhotoNotFoundExceptionをthrowすること")
		void getPhotoList_accountNotFound() {
			String accountId = "aaaaaaaa";

			doReturn(null).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(new ArrayList<String>())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoList(photoListGetModel));
			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl, never()).getPhotoList(any(PhotoGetModel.class));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void getPhotoDetail_success() throws GalleryException {
			String accountId = "aaaaaaaa";

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			PhotoDetailModel actual = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg"))
					.build();
			PhotoDetailSearchModel photoDetailSearchModel = PhotoDetailSearchModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();

			doReturn(actual).when(photoDetailRepositoryImpl).getPhotoDetail(photoDetailSearchModel);

			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.photoNo(new PhotoNo(1L))
					.build();

			assertEquals(actual, photoServiceImpl.getPhotoDetail(photoDetailGetModel));
			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
		}

		@Test
		@Order(2)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() throws GalleryException {
			String accountId = "aaaaaaaa";

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			doThrow(PhotoNotFoundException.class).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));

			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.photoNo(new PhotoNo(1L))
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoDetail(photoDetailGetModel));
			verify(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：指定のアカウントが存在しない場合、PhotoNotFoundExceptionをthrowすること")
		void getPhotoDetail_accountNotFound() throws GalleryException {
			String accountId = "aaaaaaaa";

			doReturn(null).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.photoNo(new PhotoNo(1L))
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoDetail(photoDetailGetModel));
			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl, never()).getPhotoDetail(any(PhotoDetailSearchModel.class));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class savePhotos {
		PhotoDetailModel createNewPhotoWithTag() {
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル1"))
					.photoEnglishTitle(new PhotoEnglishTitle("title1"))
					.caption(new Caption("キャプション1"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList))
					.build();
		}
		
		PhotoDetailModel createNewPhoto() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC222.jpg",
					"multipart/form-data",
					"sample image".getBytes()
				);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.build();
		}

		PhotoDetailModel createNewPhotoWithFilename(String originalFilename) {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					originalFilename,
					"multipart/form-data",
					"sample image".getBytes()
				);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.build();
		}

		PhotoDetailModel createUpdatePhotoWithTag() {
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC222.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/DSC222.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル2"))
					.photoEnglishTitle(new PhotoEnglishTitle("title2"))
					.caption(new Caption("キャプション2"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList))
					.build();
		}
		
		PhotoDetailModel createUpdatePhoto() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC333.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/DSC333.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル3"))
					.photoEnglishTitle(new PhotoEnglishTitle("title3"))
					.caption(new Caption("キャプション3"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.build();
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：photoDetailModelListがnullの場合、終了")
		void savePhotos_photoDetailModelList_is_null() throws GalleryException {
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId("aaaaaaaa"), null);
			assertNull(actual);
			verify(accountRepositoryImpl, times(0)).lockForUpdate(any(AccountNo.class));
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(AccountNo.class));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListがemptyの場合、終了")
		void savePhotos_photoDetailModelList_is_empty() throws GalleryException {
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId("aaaaaaaa"), PhotoDetailModelList.of(photoDetailModelList));
			assertNull(actual);
			verify(accountRepositoryImpl, times(0)).lockForUpdate(any(AccountNo.class));
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(AccountNo.class));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：新規登録のみ")
		void savePhotos_newPhoto() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).regist(photoCaptor.capture());

			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());

			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);

			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(5L), actual.getPhotoNo());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC222.jpg"), actual.getImageFilePath());
			verify(accountRepositoryImpl).lockForUpdate(new AccountNo(1L));
			verify(photoAggregateRepositoryImpl, times(2)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
			verify(fileRepositoryImpl, times(2)).save(any(FileModel.class));

			List<FileModel> fileModelCaptureList = fileModelCaptor.getAllValues();
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), fileModelCaptureList.get(0).getFilePath());
			assertEquals(photoDetailModel1.getImageFile(), fileModelCaptureList.get(0).getImageFile());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC222.jpg"), fileModelCaptureList.get(1).getFilePath());
			assertEquals(photoDetailModel2.getImageFile(), fileModelCaptureList.get(1).getImageFile());

			List<Photo> photoCaptureList = photoCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoCaptureList.get(0).getAccountNo());
			assertEquals(new PhotoNo(5L), photoCaptureList.get(0).getPhotoNo());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), photoCaptureList.get(0).getImageFilePath());
			assertEquals(2, photoCaptureList.get(0).getPhotoTagModelList().size());
			assertEquals(new AccountNo(1L), photoCaptureList.get(0).getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(new PhotoNo(5L), photoCaptureList.get(0).getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(1L, photoCaptureList.get(0).getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", photoCaptureList.get(0).getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", photoCaptureList.get(0).getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(new PhotoNo(5L), photoCaptureList.get(0).getPhotoTagModelList().get(1).getPhotoNo());
			assertEquals(2L, photoCaptureList.get(0).getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("海", photoCaptureList.get(0).getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("sea", photoCaptureList.get(0).getPhotoTagModelList().get(1).getTagEnglishName().value());

			assertEquals(new AccountNo(1L), photoCaptureList.get(1).getAccountNo());
			assertEquals(new PhotoNo(6L), photoCaptureList.get(1).getPhotoNo());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC222.jpg"), photoCaptureList.get(1).getImageFilePath());

			ArgumentCaptor<PhotoRegisteredEvent> photoRegisteredEventCaptor = ArgumentCaptor.forClass(PhotoRegisteredEvent.class);
			verify(applicationEventPublisher, times(2)).publishEvent(photoRegisteredEventCaptor.capture());
			List<PhotoRegisteredEvent> photoRegisteredEventCaptureList = photoRegisteredEventCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoRegisteredEventCaptureList.get(0).accountNo());
			assertEquals(new PhotoNo(5L), photoRegisteredEventCaptureList.get(0).photoNo());
			assertEquals(new AccountNo(1L), photoRegisteredEventCaptureList.get(1).accountNo());
			assertEquals(new PhotoNo(6L), photoRegisteredEventCaptureList.get(1).photoNo());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：更新のみ")
		void savePhotos_updatePhoto() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/existing.jpg"))
					.build()).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).update(photoCaptor.capture());

			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual.getPhotoNo());
			assertNull(actual.getImageFilePath());
			verify(accountRepositoryImpl).lockForUpdate(new AccountNo(1L));
			verify(accountRepositoryImpl, times(0)).getByAccountNo(any(AccountNo.class));
			verify(photoMstRepositoryImpl, times(0)).count(any(AccountNo.class));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(2)).update(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));

			List<Photo> photoCaptureList = photoCaptor.getAllValues();
			assertEquals(new PhotoNo(2L), photoCaptureList.get(0).getPhotoNo());
			assertEquals(2, photoCaptureList.get(0).getPhotoTagModelList().size());
			assertEquals(1L, photoCaptureList.get(0).getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", photoCaptureList.get(0).getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals(2L, photoCaptureList.get(0).getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("海", photoCaptureList.get(0).getPhotoTagModelList().get(1).getTagJapaneseName().value());

			assertEquals(new PhotoNo(3L), photoCaptureList.get(1).getPhotoNo());

			ArgumentCaptor<PhotoUpdatedEvent> photoUpdatedEventCaptor = ArgumentCaptor.forClass(PhotoUpdatedEvent.class);
			verify(applicationEventPublisher, times(2)).publishEvent(photoUpdatedEventCaptor.capture());
			List<PhotoUpdatedEvent> photoUpdatedEventCaptureList = photoUpdatedEventCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoUpdatedEventCaptureList.get(0).accountNo());
			assertEquals(new PhotoNo(2L), photoUpdatedEventCaptureList.get(0).photoNo());
			assertEquals(new AccountNo(1L), photoUpdatedEventCaptureList.get(1).accountNo());
			assertEquals(new PhotoNo(3L), photoUpdatedEventCaptureList.get(1).photoNo());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：新規登録＋更新")
		void savePhotos_newPhoto_and_updatePhoto() throws GalleryException  {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/existing.jpg"))
					.build()).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));

			ArgumentCaptor<Photo> photoRegistCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).regist(photoRegistCaptor.capture());

			ArgumentCaptor<Photo> photoUpdateCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).update(photoUpdateCaptor.capture());

			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());

			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual.getPhotoNo());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), actual.getImageFilePath());
			verify(photoAggregateRepositoryImpl, times(1)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(1)).update(any(Photo.class));
			verify(fileRepositoryImpl, times(1)).save(any(FileModel.class));

			FileModel fileModelCapture = fileModelCaptor.getValue();
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), fileModelCapture.getFilePath());
			assertEquals(photoDetailModel1.getImageFile(), fileModelCapture.getImageFile());

			Photo registeredPhoto = photoRegistCaptor.getValue();
			assertEquals(new PhotoNo(5L), registeredPhoto.getPhotoNo());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), registeredPhoto.getImageFilePath());
			assertEquals(2, registeredPhoto.getPhotoTagModelList().size());

			Photo updatedPhoto = photoUpdateCaptor.getValue();
			assertEquals(new PhotoNo(3L), updatedPhoto.getPhotoNo());

			ArgumentCaptor<PhotoRegisteredEvent> photoRegisteredEventCaptor = ArgumentCaptor.forClass(PhotoRegisteredEvent.class);
			verify(applicationEventPublisher, times(1)).publishEvent(photoRegisteredEventCaptor.capture());
			assertEquals(new AccountNo(1L), photoRegisteredEventCaptor.getValue().accountNo());
			assertEquals(new PhotoNo(5L), photoRegisteredEventCaptor.getValue().photoNo());

			ArgumentCaptor<PhotoUpdatedEvent> photoUpdatedEventCaptor = ArgumentCaptor.forClass(PhotoUpdatedEvent.class);
			verify(applicationEventPublisher, times(1)).publishEvent(photoUpdatedEventCaptor.capture());
			assertEquals(new AccountNo(1L), photoUpdatedEventCaptor.getValue().accountNo());
			assertEquals(new PhotoNo(3L), photoUpdatedEventCaptor.getValue().photoNo());
		}

		@Test
		@Order(6)
		@DisplayName("異常系：写真登録でGalleryExceptionをthrowする（写真は複数枚）")
		void savePhotos_registPhoto_GalleryException() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));
			doThrow(RegistFailureException.class).when(photoAggregateRepositoryImpl).regist(any(Photo.class));

			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(photoAggregateRepositoryImpl, times(1)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(7)
		@DisplayName("異常系：写真更新でGalleryExceptionをthrowする（写真は複数枚）")
		void savePhotos_updatePhoto_GalleryException() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/existing.jpg"))
					.build()).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));
			doThrow(UpdateFailureException.class).when(photoAggregateRepositoryImpl).update(any(Photo.class));

			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(1)).update(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(8)
		@DisplayName("異常系：更新対象の写真がDBに存在しない場合、PhotoNotFoundExceptionをthrowする")
		void savePhotos_updatePhoto_PhotoNotFoundException() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doThrow(PhotoNotFoundException.class).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));

			PhotoDetailModel photoDetailModel = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel);

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(9)
		@DisplayName("正常系：更新時にリクエストの画像ファイルパスを信用せず、DB上の既存パスで更新する（ファイルパス汚染防止）")
		void savePhotos_updatePhoto_ignoresRequestImageFilePath() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			ImageFilePath existingImageFilePath = new ImageFilePath("https://localhost:8080/image/existing.jpg");
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(existingImageFilePath)
					.build()).when(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailSearchModel.class));

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).update(photoCaptor.capture());

			// リクエストには悪意のあるファイルパスが指定されているものとする
			PhotoDetailModel photoDetailModel = createUpdatePhoto().toBuilder()
					.imageFilePath(new ImageFilePath("../../../etc/passwd"))
					.build();
			photoDetailModelList.add(photoDetailModel);

			photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(existingImageFilePath, photoCaptor.getValue().getImageFilePath());
		}

		@Test
		@Order(10)
		@DisplayName("異常系：登録枚数の上限に達している場合、トランザクション内の再検証でREACHED_REGISTRATION_LIMITをthrowすること")
		void savePhotos_reachedUpperLimit_throws() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.MINI).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(3).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(photoQuotaPolicy).isReached(AuthorityEnum.MINI, new PhotoCount(3));

			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel1);

			assertThrows(PhotoNotAdditableException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(accountRepositoryImpl).lockForUpdate(new AccountNo(1L));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(11)
		@DisplayName("異常系：新規登録時に画像ファイルが指定されていない場合、GalleryExceptionをthrowする")
		void savePhotos_registPhoto_imageFileRequired() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));

			PhotoDetailModel photoDetailModel = createNewPhoto().toBuilder().imageFile(null).build();
			photoDetailModelList.add(photoDetailModel);

			BadRequestException exception = assertThrows(BadRequestException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
			assertEquals(ErrorEnum.IMAGE_FILE_REQUIRED.getErrorCode(), exception.getErrorCode());

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(12)
		@DisplayName("正常系：オリジナルファイル名にパストラバーサルを含む場合、ベース名のみを保存パスに使用すること")
		void savePhotos_newPhoto_sanitizes_path_traversal_filename() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).regist(photoCaptor.capture());

			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());

			// クライアントが「../../etc/evil.jpg」のようなオリジナルファイル名を送信した場合を想定
			PhotoDetailModel photoDetailModel = createNewPhotoWithFilename("../../etc/evil.jpg");
			photoDetailModelList.add(photoDetailModel);

			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(5L), actual.getPhotoNo());
			verify(photoAggregateRepositoryImpl, times(1)).regist(any(Photo.class));

			// ベース名（evil.jpg）のみが保存先パスの末尾に使用され、パストラバーサル部分は除去されていること
			assertEquals(new ImageFilePath(filePath + accountId + "/evil.jpg"), fileModelCaptor.getValue().getFilePath());
			assertEquals(new ImageFilePath(filePath + accountId + "/evil.jpg"), photoCaptor.getValue().getImageFilePath());
			// 戻り値の画像ファイルパスもサニタイズ済みの値であり、レスポンスに生のオリジナルファイル名が漏れないこと
			assertEquals(new ImageFilePath(filePath + accountId + "/evil.jpg"), actual.getImageFilePath());
		}

		@Test
		@Order(13)
		@DisplayName("異常系：画像ファイルのContent-Typeが許可されていない（偽装された）場合、GalleryExceptionをthrowする")
		void savePhotos_registPhoto_unsupportedContentType() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(false).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));

			PhotoDetailModel photoDetailModel = createNewPhoto();
			photoDetailModelList.add(photoDetailModel);

			BadRequestException exception = assertThrows(BadRequestException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
			assertEquals(ErrorEnum.UNSUPPORTED_IMAGE_CONTENT_TYPE.getErrorCode(), exception.getErrorCode());

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(14)
		@DisplayName("異常系：画像ファイルのマジックバイトが既知の画像フォーマットと一致しない場合、GalleryExceptionをthrowする")
		void savePhotos_registPhoto_invalidSignature() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));

			PhotoDetailModel photoDetailModel = createNewPhoto();
			photoDetailModelList.add(photoDetailModel);

			BadRequestException exception = assertThrows(BadRequestException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
			assertEquals(ErrorEnum.INVALID_IMAGE_SIGNATURE.getErrorCode(), exception.getErrorCode());

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(15)
		@DisplayName("異常系：画像ファイルのサイズが上限を超えている場合、GalleryExceptionをthrowする")
		void savePhotos_registPhoto_sizeExceeded() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));

			PhotoDetailModel photoDetailModel = createNewPhoto();
			photoDetailModelList.add(photoDetailModel);

			BadRequestException exception = assertThrows(BadRequestException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
			assertEquals(ErrorEnum.IMAGE_FILE_SIZE_EXCEEDED.getErrorCode(), exception.getErrorCode());

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(16)
		@DisplayName("異常系：許可されていない拡張子の場合、BadRequestExceptionをthrowし登録処理を行わないこと")
		void savePhotos_newPhoto_disallowed_extension() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(false).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));

			PhotoDetailModel photoDetailModel = createNewPhotoWithFilename("malicious.exe");
			photoDetailModelList.add(photoDetailModel);

			assertThrows(BadRequestException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(17)
		@DisplayName("異常系：複数枚新規登録中にN枚目でDB登録が失敗した場合、書き込み済みファイルを補償削除する")
		void savePhotos_registPhoto_compensatesOrphanedFileOnFailure() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));
			// 1枚目のDB登録は成功、2枚目のDB登録で失敗させる
			doNothing().doThrow(RegistFailureException.class).when(photoAggregateRepositoryImpl).regist(any(Photo.class));

			// 新規登録1枚目（成功する）
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 新規登録2枚目（DB登録が失敗する）
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);

			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));

			verify(photoAggregateRepositoryImpl, times(2)).regist(any(Photo.class));
			verify(fileRepositoryImpl, times(1)).save(any(FileModel.class));

			// 1枚目は既にファイル書き込みが成功しているため、DBロールバックとの整合性を保つべく補償削除される
			ArgumentCaptor<ImageFilePath> deleteCaptor = ArgumentCaptor.forClass(ImageFilePath.class);
			verify(fileRepositoryImpl, times(1)).delete(deleteCaptor.capture());
			assertEquals(new ImageFilePath(filePath + accountId + "/DSC111.jpg"), deleteCaptor.getValue());
		}

		@Test
		@Order(18)
		@DisplayName("異常系：補償削除自体が失敗しても、元のGalleryExceptionが伝播する")
		void savePhotos_registPhoto_deleteFailureDoesNotMaskOriginalException() throws GalleryException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();

			doReturn(new PhotoNo(5L)).when(photoMstRepositoryImpl).getNewPhotoNo(new AccountNo(1L));
			doReturn(filePath).when(photoConfig).getOutputPath();
			doReturn(AccountModel.builder().accountNo(new AccountNo(1L)).authorityKbn(AuthorityEnum.NORMAL).build())
					.when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
			doReturn(0).when(photoMstRepositoryImpl).count(new AccountNo(1L));
			doReturn(true).when(imageFileValidationPolicy).isAllowedContentType(any(ImageFile.class));
			doReturn(true).when(imageFileValidationPolicy).isValidSignature(any(ImageFile.class));
			doReturn(false).when(imageFileValidationPolicy).isSizeExceeded(any(ImageFile.class));
			doReturn(true).when(photoFileExtensionPolicy).isAllowedExtension(any(ImageFile.class));
			doNothing().doThrow(RegistFailureException.class).when(photoAggregateRepositoryImpl).regist(any(Photo.class));
			doThrow(new RuntimeException("delete failed")).when(fileRepositoryImpl).delete(any(ImageFilePath.class));

			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);

			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deletePhotos {
		@Test
		@Order(1)
		@DisplayName("正常系：photoDeleteModelListが0件の場合、終了")
		void deletePhotos_photoDeleteModelList_empty() throws GalleryException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();

			photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.empty());
			verify(photoAggregateRepositoryImpl, times(0)).delete(any(Photo.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListが2件以上の場合")
		void deletePhotos_success() throws GalleryException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).delete(photoCaptor.capture());

			ArgumentCaptor<ImageFilePath> fileDeleteCaptor = ArgumentCaptor.forClass(ImageFilePath.class);
			doNothing().when(fileRepositoryImpl).delete(fileDeleteCaptor.capture());

			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath("DSC111.jpg"))
					.build());
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.imageFilePath(new ImageFilePath("DSC222.jpg"))
					.build());

			photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.of(photoDeleteModelList));
			verify(photoAggregateRepositoryImpl, times(2)).delete(any(Photo.class));
			verify(fileRepositoryImpl, times(2)).delete(any(ImageFilePath.class));

			List<ImageFilePath> fileDeleteCaptureList = fileDeleteCaptor.getAllValues();
			assertEquals(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"), fileDeleteCaptureList.get(0));
			assertEquals(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"), fileDeleteCaptureList.get(1));

			List<Photo> photoCaptureList = photoCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoCaptureList.get(0).getAccountNo());
			assertEquals(1L, photoCaptureList.get(0).getPhotoNo().value());
			assertTrue(photoCaptureList.get(0).isDeleted());
			assertEquals(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"), photoCaptureList.get(0).getImageFilePathForDelete());
			assertEquals(new AccountNo(1L), photoCaptureList.get(1).getAccountNo());
			assertEquals(2L, photoCaptureList.get(1).getPhotoNo().value());
			assertEquals(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"), photoCaptureList.get(1).getImageFilePathForDelete());

			ArgumentCaptor<PhotoDeletedEvent> photoDeletedEventCaptor = ArgumentCaptor.forClass(PhotoDeletedEvent.class);
			verify(applicationEventPublisher, times(2)).publishEvent(photoDeletedEventCaptor.capture());
			List<PhotoDeletedEvent> photoDeletedEventCaptureList = photoDeletedEventCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoDeletedEventCaptureList.get(0).accountNo());
			assertEquals(1L, photoDeletedEventCaptureList.get(0).photoNo().value());
			assertEquals(new AccountNo(1L), photoDeletedEventCaptureList.get(1).accountNo());
			assertEquals(2L, photoDeletedEventCaptureList.get(1).photoNo().value());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：対象写真が存在しない場合、PhotoNotFoundExceptionをthrowする")
		void deletePhotos_PhotoNotFoundException() throws GalleryException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();
			doThrow(PhotoNotFoundException.class).when(photoAggregateRepositoryImpl).delete(any(Photo.class));

			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg"))
					.build());

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.of(photoDeleteModelList)));

			verify(photoAggregateRepositoryImpl, times(1)).delete(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).delete(any(ImageFilePath.class));
			verify(applicationEventPublisher, times(0)).publishEvent(any());
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class isReachedUpperLimit {
		@Test
		@Order(1)
		@DisplayName("異常系：アカウント番号がnullの場合、NullPointerExceptionをthrowする")
		void isReachedUpperLimit_accountNo_is_null() {
			assertThrows(NullPointerException.class, () -> photoServiceImpl.isReachedUpperLimit(null));
			verify(photoQuotaPolicy, times(0)).isReached(any(AuthorityEnum.class), any(PhotoCount.class));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：PhotoQuotaPolicyの判定結果（上限に達している）をそのまま返すこと")
		void isReachedUpperLimit_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.MINI).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(10).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(true).when(photoQuotaPolicy).isReached(AuthorityEnum.MINI, new PhotoCount(10));

			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
			verify(photoQuotaPolicy).isReached(AuthorityEnum.MINI, new PhotoCount(10));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：PhotoQuotaPolicyの判定結果（上限に達していない）をそのまま返すこと")
		void isReachedUpperLimit_not_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.NORMAL).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(999).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(false).when(photoQuotaPolicy).isReached(AuthorityEnum.NORMAL, new PhotoCount(999));

			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
			verify(photoQuotaPolicy).isReached(AuthorityEnum.NORMAL, new PhotoCount(999));
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getSeasonComparator {
		@Test
		@Order(1)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：季節・時期順に並び替えられること")
		void getSeasonComparator_success() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getSeasonComparator = PhotoServiceImpl.class.getDeclaredMethod("getSeasonComparator");
			getSeasonComparator.setAccessible(true);

			Comparator<PhotoModel> actual = (Comparator<PhotoModel>) getSeasonComparator.invoke(photoServiceImpl);

			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.favoriteCount(new FavoriteCount(3))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2002, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC222.jpg"))
					.caption(new Caption("キャプション2"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.favoriteCount(new FavoriteCount(2))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC333.jpg"))
					.caption(new Caption("キャプション3"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.favoriteCount(new FavoriteCount(2))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2001, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC444.jpg"))
					.caption(new Caption("キャプション4"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.favoriteCount(new FavoriteCount(3))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2003, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC555.jpg"))
					.caption(new Caption("キャプション5"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());

			List<PhotoModel> actualData = photoModelList.stream().sorted(actual).toList();
			assertEquals(4L, actualData.get(0).getPhotoNo().value());
			assertEquals(5L, actualData.get(1).getPhotoNo().value());
			assertEquals(3L, actualData.get(2).getPhotoNo().value());
			assertEquals(2L, actualData.get(3).getPhotoNo().value());
			assertEquals(1L, actualData.get(4).getPhotoNo().value());
		}
	}
	
}