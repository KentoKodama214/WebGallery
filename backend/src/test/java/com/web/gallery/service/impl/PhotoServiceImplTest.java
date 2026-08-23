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
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
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
		void getPhotoList_not_found() {
			String accountId = "aaaaaaaa";
			List<String> tags = new ArrayList<String>();

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(PhotoModelList.empty()).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertTrue(actual.isEmpty());
			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));

			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
			assertEquals(DirectionEnum.NONE, photoGetModel.getDirectionKbn());
			assertFalse(photoGetModel.getIsFavoriteOnly().value());
			assertEquals(tags, photoGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoGetModel.getSortBy());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：sortByがSEASON以外の場合、フィルタリング・ソート済みのRepositoryの取得結果をそのまま返すこと")
		void getPhotoList_passThrough_when_sortBy_is_not_season() {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

			PhotoModelList repositoryResult = createPhotoModelList();
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(repositoryResult).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(true))
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.build();

			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(repositoryResult.toList(), actual.toList());

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
		void getPhotoList_sortBy_season() {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");

			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

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
			doReturn(PhotoModelList.of(repositoryResultList)).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(new AccountId(accountId))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.build();

			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(3, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo().value());
			assertEquals(2L, actual.get(1).getPhotoNo().value());
			assertEquals(3L, actual.get(2).getPhotoNo().value());

			verify(accountRepositoryImpl).getByAccountId(new AccountId(accountId));
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));

			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
			assertEquals(SortPhotoEnum.SEASON, photoGetModel.getSortBy());
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
			PhotoDetailModel actual = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg"))
					.build();
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			doReturn(actual).when(photoDetailRepositoryImpl).getPhotoDetail(photoDetailGetModel);
			assertEquals(actual, photoServiceImpl.getPhotoDetail(photoDetailGetModel));
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() throws GalleryException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			doThrow(PhotoNotFoundException.class).when(photoDetailRepositoryImpl).getPhotoDetail(photoDetailGetModel);
			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoDetail(photoDetailGetModel));
			verify(photoDetailRepositoryImpl).getPhotoDetail(any(PhotoDetailGetModel.class));
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
			PhotoNo actual = photoServiceImpl.savePhotos(new AccountId("aaaaaaaa"), null);
			assertNull(actual);
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(AccountNo.class));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListがemptyの場合、終了")
		void savePhotos_photoDetailModelList_is_empty() throws GalleryException {
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			PhotoNo actual = photoServiceImpl.savePhotos(new AccountId("aaaaaaaa"), PhotoDetailModelList.of(photoDetailModelList));
			assertNull(actual);
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(AccountNo.class));
			verify(photoAggregateRepositoryImpl, times(0)).regist(any(Photo.class));
			verify(photoAggregateRepositoryImpl, times(0)).update(any(Photo.class));
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

			PhotoNo actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(5L), actual);
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

			ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
			doNothing().when(photoAggregateRepositoryImpl).update(photoCaptor.capture());

			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);

			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			PhotoNo actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual);
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

			PhotoNo actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual);
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
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhotos_UpdateFailureException() throws GalleryException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();
			doThrow(UpdateFailureException.class).when(photoAggregateRepositoryImpl).delete(any(Photo.class));

			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg"))
					.build());

			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.of(photoDeleteModelList)));

			verify(photoAggregateRepositoryImpl, times(1)).delete(any(Photo.class));
			verify(fileRepositoryImpl, times(0)).delete(any(ImageFilePath.class));
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
			verify(photoConfig, times(0)).getMiniUserUpperLimit();
			verify(photoConfig, times(0)).getNormalUserUpperLimit();
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：mini-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_mini_user_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.MINI).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(10).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(10).when(photoConfig).getMiniUserUpperLimit();
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：mini-userで、上限まで未登録の場合")
		void isReachedUpperLimit_mini_user_not_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.MINI).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(9).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(10).when(photoConfig).getMiniUserUpperLimit();
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：normal-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_normal_user_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.NORMAL).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(1000).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(1000).when(photoConfig).getNormalUserUpperLimit();
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：normal-userで、上限まで未登録の場合")
		void isReachedUpperLimit_normal_user_not_reached() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.NORMAL).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(999).when(photoMstRepositoryImpl).count(accountNo);
			doReturn(1000).when(photoConfig).getNormalUserUpperLimit();
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：special-userの場合")
		void isReachedUpperLimit_special_user() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(1000).when(photoMstRepositoryImpl).count(accountNo);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：administratorの場合")
		void isReachedUpperLimit_administrator() {
			AccountNo accountNo = new AccountNo(1L);
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.ADMINISTRATOR).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(1000).when(photoMstRepositoryImpl).count(accountNo);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
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