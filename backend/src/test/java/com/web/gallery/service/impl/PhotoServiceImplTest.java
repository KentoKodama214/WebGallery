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

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.AccountModel;
import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.enumuration.ErrorEnum;
import com.web.gallery.enumuration.SortPhotoEnum;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.repository.impl.AccountRepositoryImpl;
import com.web.gallery.repository.impl.FileRepositoryImpl;
import com.web.gallery.repository.impl.PhotoDetailRepositoryImpl;
import com.web.gallery.repository.impl.PhotoFavoriteRepositoryImpl;
import com.web.gallery.repository.impl.PhotoMstRepositoryImpl;
import com.web.gallery.repository.impl.PhotoTagMstRepositoryImpl;

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
	private PhotoTagMstRepositoryImpl photoTagMstRepositoryImpl;
	
	@Mock
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;
	
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
		List<PhotoModel> createPhotoModelList() {
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			
			List<PhotoTagModel> photoTagModelList1 = new ArrayList<PhotoTagModel>();
			photoTagModelList1.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList1.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			PhotoModel photoModel1 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(photoTagModelList1)
					.build();
			photoModelList.add(photoModel1);
			
			List<PhotoTagModel> photoTagModelList2 = new ArrayList<PhotoTagModel>();
			photoTagModelList2.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList2.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			PhotoModel photoModel2 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(photoTagModelList2)
					.build();
			photoModelList.add(photoModel2);
			
			List<PhotoTagModel> photoTagModelList3 = new ArrayList<PhotoTagModel>();
			photoTagModelList3.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList3.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			PhotoModel photoModel3 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(photoTagModelList3)
					.build();
			photoModelList.add(photoModel3);
			
			List<PhotoTagModel> photoTagModelList4 = new ArrayList<PhotoTagModel>();
			photoTagModelList4.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			PhotoModel photoModel4 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.favoriteCount(4)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2001, 4, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(photoTagModelList4)
					.build();
			photoModelList.add(photoModel4);
			
			List<PhotoTagModel> photoTagModelList5 = new ArrayList<PhotoTagModel>();
			photoTagModelList5.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.tagNo(1L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			PhotoModel photoModel5 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.favoriteCount(10)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 5, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(photoTagModelList5)
					.build();
			photoModelList.add(photoModel5);
			
			PhotoModel photoModel6 = PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(6L)
					.favoriteCount(0)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("DSC666.jpg")
					.caption("キャプション6")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build();
			photoModelList.add(photoModel6);
			
			return photoModelList;
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：写真が存在しなかった場合")
		void getPhotoList_not_found() {
			String accountId = "aaaaaaaa";
			List<String> tags = new ArrayList<String>();
			
			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(accountId);
			
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(new ArrayList<PhotoModel>()).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(accountId)
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(new ArrayList<PhotoModel>(), actual);
			verify(accountRepositoryImpl).getByAccountId(accountId);
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));
			
			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が存在した場合で、撮影日順に並び替え")
		void getPhotoList_sortBy_photoAt() {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");
			
			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(accountId);
			
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(createPhotoModelList()).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(accountId)
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(3, actual.size());
			assertEquals(3L, actual.get(0).getPhotoNo());
			assertEquals(2L, actual.get(1).getPhotoNo());
			assertEquals(1L, actual.get(2).getPhotoNo());
			
			verify(accountRepositoryImpl).getByAccountId(accountId);
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));
			
			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：写真が存在した場合で、お気に入り数順に並び替え")
		void getPhotoList_sortBy_Favorite() {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");
			
			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(accountId);
			
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(createPhotoModelList()).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(accountId)
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(3, actual.size());
			assertEquals(2L, actual.get(0).getPhotoNo());
			assertEquals(3L, actual.get(1).getPhotoNo());
			assertEquals(1L, actual.get(2).getPhotoNo());
			
			verify(accountRepositoryImpl).getByAccountId(accountId);
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));
			
			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：写真が存在した場合で、季節・時期順に並び替え")
		void getPhotoList_sortBy_season() {
			String accountId = "aaaaaaaa";
			List<String> tags = Arrays.asList("太陽", "海");
			
			AccountModel account = AccountModel.builder().accountNo(new AccountNo(1L)).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(accountId);
			
			ArgumentCaptor<PhotoGetModel> photoGetModelCaptor = ArgumentCaptor.forClass(PhotoGetModel.class);
			doReturn(createPhotoModelList()).when(photoDetailRepositoryImpl).getPhotoList(photoGetModelCaptor.capture());
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(2L))
					.photoAccountId(accountId)
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(3, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo());
			assertEquals(2L, actual.get(1).getPhotoNo());
			assertEquals(3L, actual.get(2).getPhotoNo());
			
			verify(accountRepositoryImpl).getByAccountId(accountId);
			verify(photoDetailRepositoryImpl).getPhotoList(any(PhotoGetModel.class));
			
			PhotoGetModel photoGetModel = photoGetModelCaptor.getValue();
			assertEquals(new AccountNo(2L), photoGetModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoGetModel.getPhotoAccountNo());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void getPhotoDetail_success() throws PhotoNotFoundException {
			PhotoDetailModel actual = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.build();
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(1L)
					.build();
			
			doReturn(actual).when(photoDetailRepositoryImpl).getPhotoDetail(photoDetailGetModel);
			assertEquals(actual, photoServiceImpl.getPhotoDetail(photoDetailGetModel));
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(1L)
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
					.photoNo(5L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("")
					.photoJapaneseTitle("タイトル1")
					.photoEnglishTitle("title1")
					.caption("キャプション1")
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.photoTagModelList(photoTagModelList)
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
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
		}
		
		PhotoDetailModel createUpdatePhotoWithTag() {
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC222.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("https://localhost:8080/image/DSC222.jpg")
					.photoJapaneseTitle("タイトル2")
					.photoEnglishTitle("title2")
					.caption("キャプション2")
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.photoTagModelList(photoTagModelList)
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
					.photoNo(3L)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("https://localhost:8080/image/DSC333.jpg")
					.photoJapaneseTitle("タイトル3")
					.photoEnglishTitle("title3")
					.caption("キャプション3")
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：photoDetailModelListがnullの場合、終了")
		void savePhotos_photoDetailModelList_is_null() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			Long actual = photoServiceImpl.savePhotos("aaaaaaaa", null);
			assertNull(actual);
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListがemptyの場合、終了")
		void savePhotos_photoDetailModelList_is_empty() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			Long actual = photoServiceImpl.savePhotos("aaaaaaaa", photoDetailModelList);
			assertNull(actual);
			verify(photoMstRepositoryImpl, times(0)).getNewPhotoNo(any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：新規登録のみ")
		void savePhotos_newPhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());
			
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel1);
			doNothing().when(photoMstRepositoryImpl).regist(photoDetailModel1, filePath + accountId + "/DSC111.jpg", 5L);
			
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);
			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel2);
			doNothing().when(photoMstRepositoryImpl).regist(photoDetailModel2, filePath + accountId + "/DSC222.jpg", 6L);
			
			Long actual = photoServiceImpl.savePhotos(accountId, photoDetailModelList);

			assertEquals(5, actual);
			verify(photoMstRepositoryImpl, times(2)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(2)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(2)).save(any(FileModel.class));

			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(5L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(1).getAccountNo());
			assertEquals(5L, photoTagModelCaptureList.get(1).getPhotoNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getTagNo());
			assertEquals("海", photoTagModelCaptureList.get(1).getTagJapaneseName());
			assertEquals("sea", photoTagModelCaptureList.get(1).getTagEnglishName());

			List<FileModel> fileModelCapture = fileModelCaptor.getAllValues();
			assertEquals(filePath + accountId + "/DSC111.jpg", fileModelCapture.get(0).getFilePath());
			assertEquals(photoDetailModel1.getImageFile(), fileModelCapture.get(0).getImageFile());
			assertEquals(filePath + accountId + "/DSC222.jpg", fileModelCapture.get(1).getFilePath());
			assertEquals(photoDetailModel2.getImageFile(), fileModelCapture.get(1).getImageFile());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：更新のみ")
		void savePhotos_updatePhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doNothing().when(photoMstRepositoryImpl).update(photoDetailModel1);
			
			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			doNothing().when(photoMstRepositoryImpl).update(photoDetailModel2);
			
			Long actual = photoServiceImpl.savePhotos(accountId, photoDetailModelList);

			assertEquals(3, actual);
			verify(photoMstRepositoryImpl, times(0)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(2)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(2)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));

			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(2L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(1).getAccountNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getPhotoNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getTagNo());
			assertEquals("海", photoTagModelCaptureList.get(1).getTagJapaneseName());
			assertEquals("sea", photoTagModelCaptureList.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：新規登録＋更新")
		void savePhotos_newPhoto_and_updatePhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException  {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());
			
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel1);
			doNothing().when(photoMstRepositoryImpl).regist(photoDetailModel1, filePath + accountId + "/DSC111.jpg", 5L);
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			doNothing().when(photoMstRepositoryImpl).update(photoDetailModel2);
			
			Long actual = photoServiceImpl.savePhotos(accountId, photoDetailModelList);

			assertEquals(3, actual);
			verify(photoMstRepositoryImpl, times(1)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(1)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(1)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(1)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(1)).save(any(FileModel.class));

			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(5L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(1).getAccountNo());
			assertEquals(5L, photoTagModelCaptureList.get(1).getPhotoNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getTagNo());
			assertEquals("海", photoTagModelCaptureList.get(1).getTagJapaneseName());
			assertEquals("sea", photoTagModelCaptureList.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(6)
		@DisplayName("異常系：FileDuplicateExceptionをthrowする（写真は複数枚）")
		void savePhotos_FileDuplicateException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doReturn(true).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel1);
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(FileDuplicateException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
			
			verify(photoMstRepositoryImpl, times(1)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
		}
		
		@Test
		@Order(7)
		@DisplayName("異常系：写真登録でRegistFailureExceptionをthrowする（写真は複数枚）")
		void savePhotos_registPhoto_RegistFailureException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel1);
			doThrow(RegistFailureException.class).when(photoMstRepositoryImpl).regist(photoDetailModel1, filePath + accountId + "/DSC111.jpg", 5L);
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
			
			verify(photoMstRepositoryImpl, times(1)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(1)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
		}
		
		@Test
		@Order(8)
		@DisplayName("異常系：新規登録時、写真タグ登録でRegistFailureExceptionをthrowする（写真は複数枚）")
		void savePhotos_newPhoto_registPhotoTag_RegistFailureException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doThrow(RegistFailureException.class).when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doReturn(false).when(photoMstRepositoryImpl).isExistPhoto(photoDetailModel1);
			doNothing().when(photoMstRepositoryImpl).regist(photoDetailModel1, filePath + accountId + "/DSC111.jpg", 5L);
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
			
			verify(photoMstRepositoryImpl, times(1)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(1)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(1)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			
			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(5L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
		}
		
		@Test
		@Order(9)
		@DisplayName("異常系：更新時、写真タグ登録でRegistFailureExceptionをthrowする（写真は複数枚）")
		void savePhotos_updatePhoto_registPhotoTag_RegistFailureException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doThrow(RegistFailureException.class).when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doNothing().when(photoMstRepositoryImpl).update(photoDetailModel1);
			
			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(RegistFailureException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
			
			verify(photoMstRepositoryImpl, times(0)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(1)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(1)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(1)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
			
			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(2L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
		}
		
		@Test
		@Order(10)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする（写真は複数枚）")
		void savePhotos_UpdateFailureException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			String filePath = "https://localhost:8080/image/";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			
			doReturn(5L).when(photoMstRepositoryImpl).getNewPhotoNo(1L);
			doReturn(filePath).when(photoConfig).getOutputPath();
			
			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			doThrow(UpdateFailureException.class).when(photoMstRepositoryImpl).update(photoDetailModel1);
			
			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
			
			verify(photoMstRepositoryImpl, times(0)).isExistPhoto(any(PhotoDetailModel.class));
			verify(photoMstRepositoryImpl, times(0)).regist(any(PhotoDetailModel.class), any(String.class), any(Long.class));
			verify(photoMstRepositoryImpl, times(1)).update(any(PhotoDetailModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).save(any(FileModel.class));
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deletePhotos {
		@Test
		@Order(1)
		@DisplayName("正常系：photoDeleteModelListが0件の場合、終了")
		void deletePhotos_photoDeleteModelList_empty() throws UpdateFailureException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();
			
			photoServiceImpl.deletePhotos("aaaaaaaa", new ArrayList<PhotoDeleteModel>());
			verify(photoFavoriteRepositoryImpl, times(0)).clear(any(PhotoFavoriteDeleteModel.class));
			verify(photoTagMstRepositoryImpl, times(0)).clear(any(PhotoTagDeleteModel.class));
			verify(photoMstRepositoryImpl, times(0)).delete(any(PhotoDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).delete(any(String.class));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListが2件以上の場合")
		void deletePhotos_success() throws UpdateFailureException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();
			
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doNothing().when(photoFavoriteRepositoryImpl).clear(photoFavoriteDeleteModelCaptor.capture());
			
			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());
			
			ArgumentCaptor<PhotoDeleteModel> photoDeleteModelCaptor = ArgumentCaptor.forClass(PhotoDeleteModel.class);
			doNothing().when(photoMstRepositoryImpl).delete(photoDeleteModelCaptor.capture());
			
			ArgumentCaptor<String> fileDeleteCaptor = ArgumentCaptor.forClass(String.class);
			doNothing().when(fileRepositoryImpl).delete(fileDeleteCaptor.capture());
			
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.imageFilePath("DSC111.jpg")
					.build());
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.imageFilePath("DSC222.jpg")
					.build());
			
			photoServiceImpl.deletePhotos("aaaaaaaa", photoDeleteModelList);
			verify(photoFavoriteRepositoryImpl, times(2)).clear(any(PhotoFavoriteDeleteModel.class));
			verify(photoTagMstRepositoryImpl, times(2)).clear(any(PhotoTagDeleteModel.class));
			verify(photoMstRepositoryImpl, times(2)).delete(any(PhotoDeleteModel.class));
			verify(fileRepositoryImpl, times(2)).delete(any(String.class));
			
			List<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptureList = photoFavoriteDeleteModelCaptor.getAllValues();
			assertEquals(null, photoFavoriteDeleteModelCaptureList.get(0).getAccountNo());
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModelCaptureList.get(0).getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavoriteDeleteModelCaptureList.get(0).getFavoritePhotoNo());
			assertEquals(null, photoFavoriteDeleteModelCaptureList.get(1).getAccountNo());
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModelCaptureList.get(1).getFavoritePhotoAccountNo());
			assertEquals(2L, photoFavoriteDeleteModelCaptureList.get(1).getFavoritePhotoNo());
			
			List<PhotoTagDeleteModel> photoTagDeleteModelCaptureList = photoTagDeleteModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagDeleteModelCaptureList.get(0).getAccountNo());
			assertEquals(1L, photoTagDeleteModelCaptureList.get(0).getPhotoNo());
			assertEquals(new AccountNo(1L), photoTagDeleteModelCaptureList.get(1).getAccountNo());
			assertEquals(2L, photoTagDeleteModelCaptureList.get(1).getPhotoNo());
			
			List<PhotoDeleteModel> photoDeleteModelCaptureList = photoDeleteModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoDeleteModelCaptureList.get(0).getAccountNo());
			assertEquals(1L, photoDeleteModelCaptureList.get(0).getPhotoNo());
			assertEquals("DSC111.jpg", photoDeleteModelCaptureList.get(0).getImageFilePath());
			assertEquals(new AccountNo(1L), photoDeleteModelCaptureList.get(1).getAccountNo());
			assertEquals(2L, photoDeleteModelCaptureList.get(1).getPhotoNo());
			assertEquals("DSC222.jpg", photoDeleteModelCaptureList.get(1).getImageFilePath());
			
			List<String> fileDeleteCaptureList = fileDeleteCaptor.getAllValues();
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", fileDeleteCaptureList.get(0));
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC222.jpg", fileDeleteCaptureList.get(1));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhotos_UpdateFailureException() throws UpdateFailureException {
			doReturn("https://localhost:8080/image/").when(photoConfig).getOutputPath();
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doNothing().when(photoFavoriteRepositoryImpl).clear(photoFavoriteDeleteModelCaptor.capture());
			doNothing().when(photoTagMstRepositoryImpl).clear(any(PhotoTagDeleteModel.class));
			doThrow(UpdateFailureException.class).when(photoMstRepositoryImpl).delete(any(PhotoDeleteModel.class));
			
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.build());
			
			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.deletePhotos("aaaaaaaa", photoDeleteModelList));

			verify(photoFavoriteRepositoryImpl, times(1)).clear(any(PhotoFavoriteDeleteModel.class));
			verify(photoMstRepositoryImpl, times(1)).delete(any(PhotoDeleteModel.class));
			verify(fileRepositoryImpl, times(0)).delete(any(String.class));
			
			PhotoFavoriteDeleteModel photoFavoriteDeleteModelCapture = photoFavoriteDeleteModelCaptor.getValue();
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModelCapture.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavoriteDeleteModelCapture.getFavoritePhotoNo());
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class isReachedUpperLimit {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号がnullの場合")
		void isReachedUpperLimit_accountNo_is_null() {
			assertTrue(photoServiceImpl.isReachedUpperLimit(null));
			verify(accountRepositoryImpl, times(0)).getByAccountNo(any(Long.class));
			verify(photoMstRepositoryImpl, times(0)).count(any(Long.class));
			verify(photoConfig, times(0)).getMiniUserUpperLimit();
			verify(photoConfig, times(0)).getNormalUserUpperLimit();
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：mini-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_mini_user_reached() {
			Long accountNo = 1L;
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
			Long accountNo = 1L;
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
			Long accountNo = 1L;
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
			Long accountNo = 1L;
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
			Long accountNo = 1L;
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(1000).when(photoMstRepositoryImpl).count(accountNo);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：administratorの場合")
		void isReachedUpperLimit_administrator() {
			Long accountNo = 1L;
			AccountModel account = AccountModel.builder().authorityKbn(AuthorityEnum.ADMINISTRATOR).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountNo(accountNo);
			doReturn(1000).when(photoMstRepositoryImpl).count(accountNo);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getComparator {
		@Test
		@Order(1)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：photoAtの場合")
		void getComparator_photoAt() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getComparator = PhotoServiceImpl.class.getDeclaredMethod("getComparator", SortPhotoEnum.class);
			getComparator.setAccessible(true);
			
			Comparator<PhotoModel> actual = (Comparator<PhotoModel>) getComparator.invoke(photoServiceImpl, SortPhotoEnum.PHOTO_AT);
			
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2003, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			
			List<PhotoModel> actualData = photoModelList.stream().sorted(actual).toList();
			assertEquals(5L, actualData.get(0).getPhotoNo());
			assertEquals(3L, actualData.get(1).getPhotoNo());
			assertEquals(2L, actualData.get(2).getPhotoNo());
			assertEquals(1L, actualData.get(3).getPhotoNo());
			assertEquals(4L, actualData.get(4).getPhotoNo());
		}
		
		@Test
		@Order(2)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：favoriteの場合")
		void getComparator_favorite() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getComparator = PhotoServiceImpl.class.getDeclaredMethod("getComparator", SortPhotoEnum.class);
			getComparator.setAccessible(true);
			
			Comparator<PhotoModel> actual = (Comparator<PhotoModel>) getComparator.invoke(photoServiceImpl, SortPhotoEnum.FAVORITE);
			
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2003, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			
			List<PhotoModel> actualData = photoModelList.stream().sorted(actual).toList();
			assertEquals(2L, actualData.get(0).getPhotoNo());
			assertEquals(5L, actualData.get(1).getPhotoNo());
			assertEquals(3L, actualData.get(2).getPhotoNo());
			assertEquals(4L, actualData.get(3).getPhotoNo());
			assertEquals(1L, actualData.get(4).getPhotoNo());
		}
		
		@Test
		@Order(3)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：seasonの場合")
		void getComparator_season() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getComparator = PhotoServiceImpl.class.getDeclaredMethod("getComparator", SortPhotoEnum.class);
			getComparator.setAccessible(true);
			
			Comparator<PhotoModel> actual = (Comparator<PhotoModel>) getComparator.invoke(photoServiceImpl, SortPhotoEnum.SEASON);
			
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2003, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			
			List<PhotoModel> actualData = photoModelList.stream().sorted(actual).toList();
			assertEquals(4L, actualData.get(0).getPhotoNo());
			assertEquals(5L, actualData.get(1).getPhotoNo());
			assertEquals(3L, actualData.get(2).getPhotoNo());
			assertEquals(2L, actualData.get(3).getPhotoNo());
			assertEquals(1L, actualData.get(4).getPhotoNo());
		}
		
		@Test
		@Order(4)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：それ以外の場合")
		void getComparator_others() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getComparator = PhotoServiceImpl.class.getDeclaredMethod("getComparator", SortPhotoEnum.class);
			getComparator.setAccessible(true);
			
			Comparator<PhotoModel> actual = (Comparator<PhotoModel>) getComparator.invoke(photoServiceImpl, SortPhotoEnum.PHOTO_AT);
			
			List<PhotoModel> photoModelList = new ArrayList<PhotoModel>();
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(2L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(3L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2002, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(4L)
					.favoriteCount(2)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2001, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoModelList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(5L)
					.favoriteCount(3)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2003, 3, 31, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://www.xxx.com/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			
			List<PhotoModel> actualData = photoModelList.stream().sorted(actual).toList();
			assertEquals(5L, actualData.get(0).getPhotoNo());
			assertEquals(3L, actualData.get(1).getPhotoNo());
			assertEquals(2L, actualData.get(2).getPhotoNo());
			assertEquals(1L, actualData.get(3).getPhotoNo());
			assertEquals(4L, actualData.get(4).getPhotoNo());
		}
	}
	
	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filteringByDirectionKbn {
		@Test
		@Order(1)
		@DisplayName("正常系：抽出条件が未指定の場合")
		void filteringByDirectionKbn_not_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByDirectionKbn = PhotoServiceImpl.class.getDeclaredMethod("filteringByDirectionKbn", DirectionEnum.class, DirectionEnum.class);
			filteringByDirectionKbn.setAccessible(true);
			assertTrue((Boolean) filteringByDirectionKbn.invoke(photoServiceImpl, DirectionEnum.NONE, DirectionEnum.NONE));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：抽出条件が指定されていて、条件と一致の場合")
		void filteringByDirectionKbn_match_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByDirectionKbn = PhotoServiceImpl.class.getDeclaredMethod("filteringByDirectionKbn", DirectionEnum.class, DirectionEnum.class);
			filteringByDirectionKbn.setAccessible(true);
			assertTrue((Boolean) filteringByDirectionKbn.invoke(photoServiceImpl, DirectionEnum.VERTICAL, DirectionEnum.VERTICAL));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：抽出条件が指定されていて、条件と不一致の場合")
		void filteringByDirectionKbn_mismatch_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByDirectionKbn = PhotoServiceImpl.class.getDeclaredMethod("filteringByDirectionKbn", DirectionEnum.class, DirectionEnum.class);
			filteringByDirectionKbn.setAccessible(true);
			assertFalse((Boolean) filteringByDirectionKbn.invoke(photoServiceImpl, DirectionEnum.VERTICAL, DirectionEnum.HORIZONTAL));
		}
	}
	
	@Nested
	@Order(8)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filteringByIsFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系：お気に入りのみが未指定の場合")
		void filteringByIsFavorite_not_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByIsFavorite = PhotoServiceImpl.class.getDeclaredMethod("filteringByIsFavorite", Boolean.class, Boolean.class);
			filteringByIsFavorite.setAccessible(true);
			assertTrue((Boolean) filteringByIsFavorite.invoke(photoServiceImpl, true, false));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：お気に入りのみが指定されていて、写真がお気に入りの場合")
		void filteringByIsFavorite_match_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByIsFavorite = PhotoServiceImpl.class.getDeclaredMethod("filteringByIsFavorite", Boolean.class, Boolean.class);
			filteringByIsFavorite.setAccessible(true);
			assertTrue((Boolean) filteringByIsFavorite.invoke(photoServiceImpl, true, true));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：お気に入りのみが指定されていて、写真がお気に入りでない場合")
		void filteringByIsFavorite_mismatch_condition() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByIsFavorite = PhotoServiceImpl.class.getDeclaredMethod("filteringByIsFavorite", Boolean.class, Boolean.class);
			filteringByIsFavorite.setAccessible(true);
			assertFalse((Boolean) filteringByIsFavorite.invoke(photoServiceImpl, false, true));
		}
	}
	
	@Nested
	@Order(9)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class filteringByTag {
		@Test
		@Order(1)
		@DisplayName("正常系：tagsが0件の場合")
		void filteringByTag_tags_empty() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByTag = PhotoServiceImpl.class.getDeclaredMethod("filteringByTag", List.class, List.class);
			filteringByTag.setAccessible(true);
			assertTrue((Boolean) filteringByTag.invoke(photoServiceImpl, new ArrayList<PhotoTagModel>(), new ArrayList<String>()));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：tagsの1件目が''の場合")
		void filteringByTag_tags_blank() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByTag = PhotoServiceImpl.class.getDeclaredMethod("filteringByTag", List.class, List.class);
			filteringByTag.setAccessible(true);
			
			List<String> tags = new ArrayList<String>();
			tags.add("");
			assertTrue((Boolean) filteringByTag.invoke(photoServiceImpl, new ArrayList<PhotoTagModel>(), tags));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：tagsのすべてが含まれる場合")
		void filteringByTag_contain_all_tag() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByTag = PhotoServiceImpl.class.getDeclaredMethod("filteringByTag", List.class, List.class);
			filteringByTag.setAccessible(true);
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("月")
					.tagEnglishName("moon")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());

			List<String> tags = new ArrayList<String>();
			tags.add("太陽");
			tags.add("sun");
			tags.add("sea");
			
			assertTrue((Boolean) filteringByTag.invoke(photoServiceImpl, photoTagModelList, tags));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：tagsのすべてが含まれない場合")
		void filteringByTag_not_contain_all_tag() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method filteringByTag = PhotoServiceImpl.class.getDeclaredMethod("filteringByTag", List.class, List.class);
			filteringByTag.setAccessible(true);
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("月")
					.tagEnglishName("moon")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());

			List<String> tags = new ArrayList<String>();
			tags.add("太陽");
			tags.add("sum");
			tags.add("wood");
			
			assertFalse((Boolean) filteringByTag.invoke(photoServiceImpl, photoTagModelList, tags));
		}
	}
	
	@Nested
	@Order(10)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class registPhotoTags {
		@Test
		@Order(1)
		@DisplayName("正常系：photoTagModelListがnullの場合")
		void registPhotoTags_photoTagModelList_is_null() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, RegistFailureException {
			Method registPhotoTags = PhotoServiceImpl.class.getDeclaredMethod("registPhotoTags", List.class, Long.class);
			registPhotoTags.setAccessible(true);
			
			registPhotoTags.invoke(photoServiceImpl, null, null);
			
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoTagModelListがemptyの場合")
		void registPhotoTags_photoTagModelList_is_empty() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, RegistFailureException {
			Method registPhotoTags = PhotoServiceImpl.class.getDeclaredMethod("registPhotoTags", List.class, Long.class);
			registPhotoTags.setAccessible(true);
			
			registPhotoTags.invoke(photoServiceImpl, new ArrayList<PhotoTagModel>(), null);
			
			verify(photoTagMstRepositoryImpl, times(0)).regist(any(PhotoTagModel.class));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：newPhotoNoがnullの場合")
		void registPhotoTags_newPhotoNo_is_null() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, RegistFailureException {
			Method registPhotoTags = PhotoServiceImpl.class.getDeclaredMethod("registPhotoTags", List.class, Long.class);
			registPhotoTags.setAccessible(true);
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			
			registPhotoTags.invoke(photoServiceImpl, photoTagModelList, null);
			
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			
			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(1).getAccountNo());
			assertEquals(1L, photoTagModelCaptureList.get(1).getPhotoNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getTagNo());
			assertEquals("海", photoTagModelCaptureList.get(1).getTagJapaneseName());
			assertEquals("sea", photoTagModelCaptureList.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：newPhotoNoがnullでない場合")
		void registPhotoTags_newPhotoNo_is_not_null() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, RegistFailureException {
			Method registPhotoTags = PhotoServiceImpl.class.getDeclaredMethod("registPhotoTags", List.class, Long.class);
			registPhotoTags.setAccessible(true);
			
			ArgumentCaptor<PhotoTagModel> photoTagModelCaptor = ArgumentCaptor.forClass(PhotoTagModel.class);
			doNothing().when(photoTagMstRepositoryImpl).regist(photoTagModelCaptor.capture());
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			
			registPhotoTags.invoke(photoServiceImpl, photoTagModelList, 3L);
			
			verify(photoTagMstRepositoryImpl, times(2)).regist(any(PhotoTagModel.class));
			
			List<PhotoTagModel> photoTagModelCaptureList = photoTagModelCaptor.getAllValues();
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(0).getAccountNo());
			assertEquals(3L, photoTagModelCaptureList.get(0).getPhotoNo());
			assertEquals(1L, photoTagModelCaptureList.get(0).getTagNo());
			assertEquals("太陽", photoTagModelCaptureList.get(0).getTagJapaneseName());
			assertEquals("sun", photoTagModelCaptureList.get(0).getTagEnglishName());
			assertEquals(new AccountNo(1L), photoTagModelCaptureList.get(1).getAccountNo());
			assertEquals(3L, photoTagModelCaptureList.get(1).getPhotoNo());
			assertEquals(2L, photoTagModelCaptureList.get(1).getTagNo());
			assertEquals("海", photoTagModelCaptureList.get(1).getTagJapaneseName());
			assertEquals("sea", photoTagModelCaptureList.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(5)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void registPhotoTags_RegistFailureException() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, RegistFailureException {
			Method registPhotoTags = PhotoServiceImpl.class.getDeclaredMethod("registPhotoTags", List.class, Long.class);
			registPhotoTags.setAccessible(true);
			
			doThrow(new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG)).when(photoTagMstRepositoryImpl).regist(any(PhotoTagModel.class));
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(1L)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(1L)
					.tagNo(2L)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			
			try {
				registPhotoTags.invoke(photoServiceImpl, photoTagModelList, null);
				assertTrue(false);
			}
			catch(InvocationTargetException e) {
				Throwable targetException = e.getTargetException();
				assertEquals(RegistFailureException.class, targetException.getClass());
				assertEquals(ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG.getErrorMessage(), targetException.getMessage());
				verify(photoTagMstRepositoryImpl, times(1)).regist(any(PhotoTagModel.class));
			}
		}
	}
	
	@Nested
	@Order(11)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class uploadFile {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void uploadFile_success() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method uploadFile = PhotoServiceImpl.class.getDeclaredMethod("uploadFile", String.class, MultipartFile.class);
			uploadFile.setAccessible(true);
			
			ArgumentCaptor<FileModel> fileModelCaptor = ArgumentCaptor.forClass(FileModel.class);
			doNothing().when(fileRepositoryImpl).save(fileModelCaptor.capture());
			
			String filePath = "DSC111.jpg";
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			uploadFile.invoke(photoServiceImpl, filePath, multipartFile);
			
			verify(fileRepositoryImpl).save(any(FileModel.class));
			FileModel fileModelCapture = fileModelCaptor.getValue();
			assertEquals(filePath, fileModelCapture.getFilePath());
			assertEquals(multipartFile, fileModelCapture.getImageFile());
		}
	}
	
	@Nested
	@Order(12)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deletePhotoTags {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deletePhotoTags_success() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method deletePhotoTags = PhotoServiceImpl.class.getDeclaredMethod("deletePhotoTags", AccountNo.class, Long.class);
			deletePhotoTags.setAccessible(true);

			ArgumentCaptor<PhotoTagDeleteModel> photoTagDeleteModelCaptor = ArgumentCaptor.forClass(PhotoTagDeleteModel.class);
			doNothing().when(photoTagMstRepositoryImpl).clear(photoTagDeleteModelCaptor.capture());

			deletePhotoTags.invoke(photoServiceImpl, new AccountNo(1L), 1L);
			
			verify(photoTagMstRepositoryImpl).clear(any(PhotoTagDeleteModel.class));
			PhotoTagDeleteModel photoTagDeleteModelCapture = photoTagDeleteModelCaptor.getValue();
			assertEquals(new AccountNo(1L), photoTagDeleteModelCapture.getAccountNo());
			assertEquals(1L, photoTagDeleteModelCapture.getPhotoNo());
		}
	}
}