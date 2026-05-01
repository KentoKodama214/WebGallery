package com.web.gallary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.web.gallary.config.PhotoConfig;
import com.web.gallary.controller.response.PhotoListGetResponse;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.SortPhotoEnum;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoListGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.service.impl.PhotoServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoRestControllerTest {
	@InjectMocks
	private PhotoRestController photoRestController;

	@Mock
	private PhotoServiceImpl photoServiceImpl;

	@Mock
	private SessionHelper sessionHelper;

	@Mock
	private PhotoConfig photoConfig;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(photoRestController)
				.setControllerAdvice(new CommonRestControllerAdvice(sessionHelper))
				.build();
	}

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/PhotoRestControllerTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoList {
		private List<PhotoModel> createPhotoModelList() {
			List<PhotoModel> photoList = new ArrayList<PhotoModel>();
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(1)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(2)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(3)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(4)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(5)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(6)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC666.jpg")
					.caption("キャプション6")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(7)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC777.jpg")
					.caption("キャプション7")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());

			return photoList;
		}

		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータがある場合")
		void getPhotoList_with_null_parameter() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			List<PhotoModel> photoList = createPhotoModelList();
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(photoList).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isLast").value(false))
				.andExpect(jsonPath("$.photoList.length()").value(3))
				.andExpect(jsonPath("$.photoList[0].accountNo").value(1))
				.andExpect(jsonPath("$.photoList[0].photoNo").value(1))
				.andExpect(jsonPath("$.photoList[0].isFavorite").value(false))
				.andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
				.andExpect(jsonPath("$.photoList[0].caption").value("キャプション1"))
				.andExpect(jsonPath("$.photoList[0].directionKbn").value("vertical"))
				.andExpect(jsonPath("$.photoList[1].accountNo").value(1))
				.andExpect(jsonPath("$.photoList[1].photoNo").value(2))
				.andExpect(jsonPath("$.photoList[1].isFavorite").value(true))
				.andExpect(jsonPath("$.photoList[1].imageFilePath").value("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"))
				.andExpect(jsonPath("$.photoList[1].caption").value("キャプション2"))
				.andExpect(jsonPath("$.photoList[1].directionKbn").value("horizontal"))
				.andExpect(jsonPath("$.photoList[2].accountNo").value(1))
				.andExpect(jsonPath("$.photoList[2].photoNo").value(3))
				.andExpect(jsonPath("$.photoList[2].isFavorite").value(true))
				.andExpect(jsonPath("$.photoList[2].imageFilePath").value("https://localhost:8080/image/aaaaaaaa/DSC333.jpg"))
				.andExpect(jsonPath("$.photoList[2].caption").value("キャプション3"))
				.andExpect(jsonPath("$.photoList[2].directionKbn").value("horizontal"));

			PhotoListGetModel photoListGetModel = photoListGetModelCaptor.getValue();
			assertEquals(1, photoListGetModel.getAccountNo());
			assertEquals("aaaaaaaa", photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.NONE, photoListGetModel.getDirectionKbn());
			assertFalse(photoListGetModel.getIsFavoriteOnly());
			assertEquals(new ArrayList<String>(), photoListGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoListGetModel.getSortBy());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：タグに半角スペースが含まれている場合")
		void getPhotoList_with_halfspace_tag() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			List<PhotoModel> photoList = createPhotoModelList().subList(0, 4);
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(photoList).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos")
					.param("directionKbn", "VERTICAL")
					.param("isFavorite", "true")
					.param("sortBy", "SEASON")
					.param("tagList", "太陽 海")
					.param("pageNo", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.photoList.length()").value(1))
				.andExpect(jsonPath("$.photoList[0].accountNo").value(1))
				.andExpect(jsonPath("$.photoList[0].photoNo").value(4))
				.andExpect(jsonPath("$.photoList[0].isFavorite").value(true))
				.andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://localhost:8080/image/aaaaaaaa/DSC444.jpg"))
				.andExpect(jsonPath("$.photoList[0].caption").value("キャプション4"))
				.andExpect(jsonPath("$.photoList[0].directionKbn").value("horizontal"));

			PhotoListGetModel photoListGetModel = photoListGetModelCaptor.getValue();
			assertEquals(1, photoListGetModel.getAccountNo());
			assertEquals("aaaaaaaa", photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.VERTICAL, photoListGetModel.getDirectionKbn());
			assertTrue(photoListGetModel.getIsFavoriteOnly());
			assertEquals("太陽", photoListGetModel.getTagList().get(0));
			assertEquals("海", photoListGetModel.getTagList().get(1));
			assertEquals(SortPhotoEnum.SEASON, photoListGetModel.getSortBy());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：タグに全角スペースが含まれている場合")
		void getPhotoList_with_fullspace_tag() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			List<PhotoModel> photoList = createPhotoModelList().subList(0, 4);
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(photoList).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos")
					.param("directionKbn", "VERTICAL")
					.param("isFavorite", "true")
					.param("sortBy", "SEASON")
					.param("tagList", "太陽　海")
					.param("pageNo", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.photoList.length()").value(1))
				.andExpect(jsonPath("$.photoList[0].accountNo").value(1))
				.andExpect(jsonPath("$.photoList[0].photoNo").value(4))
				.andExpect(jsonPath("$.photoList[0].isFavorite").value(true))
				.andExpect(jsonPath("$.photoList[0].imageFilePath").value("https://localhost:8080/image/aaaaaaaa/DSC444.jpg"))
				.andExpect(jsonPath("$.photoList[0].caption").value("キャプション4"))
				.andExpect(jsonPath("$.photoList[0].directionKbn").value("horizontal"));

			PhotoListGetModel photoListGetModel = photoListGetModelCaptor.getValue();
			assertEquals(1, photoListGetModel.getAccountNo());
			assertEquals("aaaaaaaa", photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.VERTICAL, photoListGetModel.getDirectionKbn());
			assertTrue(photoListGetModel.getIsFavoriteOnly());
			assertEquals("太陽", photoListGetModel.getTagList().get(0));
			assertEquals("海", photoListGetModel.getTagList().get(1));
			assertEquals(SortPhotoEnum.SEASON, photoListGetModel.getSortBy());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_not_found_photo() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			List<PhotoModel> photoList = new ArrayList<PhotoModel>();
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(photoList).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.photoList.length()").value(0));

			PhotoListGetModel photoListGetModel = photoListGetModelCaptor.getValue();
			assertEquals(1, photoListGetModel.getAccountNo());
			assertEquals("aaaaaaaa", photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.NONE, photoListGetModel.getDirectionKbn());
			assertFalse(photoListGetModel.getIsFavoriteOnly());
			assertEquals(new ArrayList<String>(), photoListGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoListGetModel.getSortBy());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class savePhoto {
		@Test
		@Order(1)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：写真タグなし、撮影日時なし。Nullパラメータあり")
		void savePhoto_addPhoto_not_photoTag_and_photoAt() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(1);

			ArgumentCaptor<List<PhotoDetailModel>> photoDetailModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doNothing().when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("accountNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));

			List<PhotoDetailModel> photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(1, photoDetailModelList.getFirst().getAccountNo());
			assertNull(photoDetailModelList.getFirst().getPhotoNo());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getAddress());
			assertNull(photoDetailModelList.getFirst().getLatitude());
			assertNull(photoDetailModelList.getFirst().getLongitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath());
			assertNull(photoDetailModelList.getFirst().getPhotoJapaneseTitle());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getFocalLength());
			assertNull(photoDetailModelList.getFirst().getFValue());
			assertNull(photoDetailModelList.getFirst().getShutterSpeed());
			assertNull(photoDetailModelList.getFirst().getIso());
			assertEquals(new ArrayList<PhotoDetailModel>(), photoDetailModelList.getFirst().getPhotoTagModelList());

			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(2)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系：写真タグあり、撮影日時あり。Nullパラメータなし")
		void savePhoto_addPhoto_with_photoTag_and_photoAt() throws Exception {
			String address = "東京都港区芝公園４丁目２−８";
			String locationName = "東京タワー";
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());
			String photoJapaneseTitle = "タイトル";
			String photoEnglishTitle = "title";
			String caption = "キャプション";

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDetailModel>> photoDetailModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doNothing().when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("isFavorite", "false")
					.param("photoAt", "2000-12-01T00:00")
					.param("locationNo", "1")
					.param("address", address)
					.param("latitude", "35.000")
					.param("longitude", "135.00")
					.param("locationName", locationName)
					.param("imageFilePath", imageFilePath)
					.param("photoJapaneseTitle", photoJapaneseTitle)
					.param("photoEnglishTitle", photoEnglishTitle)
					.param("caption", caption)
					.param("directionKbn", "VERTICAL")
					.param("focalLength", "50")
					.param("fValue", "8.0")
					.param("shutterSpeed", "0.001")
					.param("iso", "100")
					.param("photoTagRegistRequestList[0].accountNo", "1")
					.param("photoTagRegistRequestList[0].photoNo", "1")
					.param("photoTagRegistRequestList[0].tagNo", "1")
					.param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRegistRequestList[0].tagEnglishName", "sun")
					.param("photoTagRegistRequestList[1].accountNo", "1")
					.param("photoTagRegistRequestList[1].photoNo", "1")
					.param("photoTagRegistRequestList[1].tagNo", "2")
					.param("photoTagRegistRequestList[1].tagJapaneseName", "海"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));

			verify(sessionHelper, times(0)).getAccountNo();
			verify(photoServiceImpl, times(0)).isReachedUpperLimit(1);

			List<PhotoDetailModel> photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(1, photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1, photoDetailModelList.getFirst().getPhotoNo());
			assertFalse(photoDetailModelList.getFirst().getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoDetailModelList.getFirst().getPhotoAt());
			assertEquals(1, photoDetailModelList.getFirst().getLocationNo());
			assertEquals(address, photoDetailModelList.getFirst().getAddress());
			assertEquals(0, BigDecimal.valueOf(35.000).compareTo(photoDetailModelList.getFirst().getLatitude()));
			assertEquals(0, BigDecimal.valueOf(135.000).compareTo(photoDetailModelList.getFirst().getLongitude()));
			assertEquals(locationName, photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle());
			assertEquals(photoEnglishTitle, photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertEquals(caption, photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertEquals(50, photoDetailModelList.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(photoDetailModelList.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.001).compareTo(photoDetailModelList.getFirst().getShutterSpeed()));
			assertEquals(100, photoDetailModelList.getFirst().getIso());

			assertEquals(1, photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals(2, photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagNo());
			assertEquals("海", photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("", photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagEnglishName());

			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(3)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
		void savePhoto_ForbiddenAccountException() throws Exception {
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isForbidden());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(Integer.class));
			verify(photoServiceImpl, times(0)).savePhotos(any(String.class), any(List.class));
		}

		@Test
		@Order(4)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：登録上限に達している。PhotoNotAdditableExceptionをthrowする")
		void savePhoto_PhotoNotAdditableException() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(1);

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(String.class), any(List.class));
		}

		@Test
		@Order(5)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイル、ファイルパスともにnull。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_null() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(1);

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(String.class), any(List.class));
		}

		@Test
		@Order(6)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイルがnull、ファイルパスがblank。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_blank() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(1);

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(String.class), any(List.class));
		}

		@Test
		@Order(7)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイル、ファイルパス以外のパラメータ不正。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_others() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(1);

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("imageFilePath", "https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.param("directionKbn", "VERTICAL")
					.param("focalLength", "-1"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(String.class), any(List.class));
		}

		@Test
		@Order(8)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：FileDuplicateExceptionをthrowする")
		void savePhoto_FileDuplicateException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDetailModel>> photoDetailModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doThrow(FileDuplicateException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(Integer.class));
			verify(sessionHelper, times(0)).getAccountNo();

			List<PhotoDetailModel> photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(1, photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1, photoDetailModelList.getFirst().getPhotoNo());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getAddress());
			assertNull(photoDetailModelList.getFirst().getLatitude());
			assertNull(photoDetailModelList.getFirst().getLongitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath());
			assertNull(photoDetailModelList.getFirst().getPhotoJapaneseTitle());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getFocalLength());
			assertNull(photoDetailModelList.getFirst().getFValue());
			assertNull(photoDetailModelList.getFirst().getShutterSpeed());
			assertNull(photoDetailModelList.getFirst().getIso());
			assertEquals(new ArrayList<PhotoDetailModel>(), photoDetailModelList.getFirst().getPhotoTagModelList());

			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(9)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void savePhoto_RegistFailureException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDetailModel>> photoDetailModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doThrow(RegistFailureException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(Integer.class));
			verify(sessionHelper, times(0)).getAccountNo();

			List<PhotoDetailModel> photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(1, photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1, photoDetailModelList.getFirst().getPhotoNo());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getAddress());
			assertNull(photoDetailModelList.getFirst().getLatitude());
			assertNull(photoDetailModelList.getFirst().getLongitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath());
			assertNull(photoDetailModelList.getFirst().getPhotoJapaneseTitle());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getFocalLength());
			assertNull(photoDetailModelList.getFirst().getFValue());
			assertNull(photoDetailModelList.getFirst().getShutterSpeed());
			assertNull(photoDetailModelList.getFirst().getIso());
			assertEquals(new ArrayList<PhotoDetailModel>(), photoDetailModelList.getFirst().getPhotoTagModelList());

			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(10)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void savePhoto_UpdateFailureException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDetailModel>> photoDetailModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doThrow(UpdateFailureException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(Integer.class));
			verify(sessionHelper, times(0)).getAccountNo();

			List<PhotoDetailModel> photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(1, photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1, photoDetailModelList.getFirst().getPhotoNo());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getAddress());
			assertNull(photoDetailModelList.getFirst().getLatitude());
			assertNull(photoDetailModelList.getFirst().getLongitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath());
			assertNull(photoDetailModelList.getFirst().getPhotoJapaneseTitle());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getFocalLength());
			assertNull(photoDetailModelList.getFirst().getFValue());
			assertNull(photoDetailModelList.getFirst().getShutterSpeed());
			assertNull(photoDetailModelList.getFirst().getIso());
			assertEquals(new ArrayList<PhotoDetailModel>(), photoDetailModelList.getFirst().getPhotoTagModelList());

			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deletePhoto {
		@Test
		@Order(1)
		@SuppressWarnings("unchecked")
		@DisplayName("正常系")
		void deletePhoto_success() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDeleteModel>> photoDeleteModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doNothing().when(photoServiceImpl).deletePhotos(photoAcountIdCaptor.capture(), photoDeleteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真削除が完了しました。"));

			List<PhotoDeleteModel> photoDeleteModelList = photoDeleteModelCaptor.getValue();
			assertEquals(1, photoDeleteModelList.size());
			assertEquals(1, photoDeleteModelList.getFirst().getAccountNo());
			assertEquals(1, photoDeleteModelList.getFirst().getPhotoNo());
			assertEquals(imageFilePath, photoDeleteModelList.getFirst().getImageFilePath());
			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：不正アクセス。ForbiddenAccountExceptionをthrowする")
		void deletePhoto_ForbiddenAccountException() throws Exception {
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo.json")))
				.andExpect(status().isForbidden());

			verify(photoServiceImpl, times(0)).deletePhotos(any(), any());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：パラメータ不正。BadRequestExceptionをthrowする")
		void deletePhoto_BadRequestException() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo_badrequest.json")))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).deletePhotos(any(), any());
		}

		@Test
		@Order(4)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhoto_UpdateFailureException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

			ArgumentCaptor<List<PhotoDeleteModel>> photoDeleteModelCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<String> photoAcountIdCaptor = ArgumentCaptor.forClass(String.class);
			doThrow(UpdateFailureException.class).when(photoServiceImpl).deletePhotos(photoAcountIdCaptor.capture(), photoDeleteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo.json")))
				.andExpect(status().isConflict());

			List<PhotoDeleteModel> photoDeleteModelList = photoDeleteModelCaptor.getValue();
			assertEquals(1, photoDeleteModelList.size());
			assertEquals(1, photoDeleteModelList.getFirst().getAccountNo());
			assertEquals(1, photoDeleteModelList.getFirst().getPhotoNo());
			assertEquals(imageFilePath, photoDeleteModelList.getFirst().getImageFilePath());
			assertEquals("aaaaaaaa", photoAcountIdCaptor.getValue());
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class createPhotoListGetResponse {
		private List<PhotoModel> createPhotoList() {
			List<PhotoModel> photoList = new ArrayList<PhotoModel>();
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(1)
					.favoriteCount(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(2)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg")
					.caption("キャプション2")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(3)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC333.jpg")
					.caption("キャプション3")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(4)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC444.jpg")
					.caption("キャプション4")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(5)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC555.jpg")
					.caption("キャプション5")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(6)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC666.jpg")
					.caption("キャプション6")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(1)
					.photoNo(7)
					.favoriteCount(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC777.jpg")
					.caption("キャプション7")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(new ArrayList<PhotoTagModel>())
					.build());

			return photoList;
		}

		@Test
		@Order(1)
		@DisplayName("正常系：ページ番号が1で、2ページに到達しない")
		void createPhotoListGetResponse_pageNo_1_lastPage() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Integer pageNo = 1;
			List<PhotoModel> photoList = createPhotoList().subList(0, 1);

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			Method createPhotoListGetResponse = PhotoRestController.class.getDeclaredMethod("createPhotoListGetResponse", List.class, Integer.class);
			createPhotoListGetResponse.setAccessible(true);

			PhotoListGetResponse actual = (PhotoListGetResponse) createPhotoListGetResponse.invoke(photoRestController, photoList, pageNo);
			assertEquals(1, actual.getPhotoList().size());
			assertEquals(1, actual.getPhotoList().getFirst().getAccountNo());
			assertEquals(1, actual.getPhotoList().getFirst().getPhotoNo());
			assertFalse(actual.getPhotoList().getFirst().getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getPhotoList().getFirst().getImageFilePath());
			assertEquals("キャプション1", actual.getPhotoList().getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getPhotoList().getFirst().getDirectionKbn());
			assertTrue(actual.getIsLast());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ページ番号が1で、2ページに到達する")
		void createPhotoListGetResponse_pageNo_1() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Integer pageNo = 1;
			List<PhotoModel> photoList = createPhotoList().subList(0, 4);

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			Method createPhotoListGetResponse = PhotoRestController.class.getDeclaredMethod("createPhotoListGetResponse", List.class, Integer.class);
			createPhotoListGetResponse.setAccessible(true);

			PhotoListGetResponse actual = (PhotoListGetResponse) createPhotoListGetResponse.invoke(photoRestController, photoList, pageNo);
			assertEquals(3, actual.getPhotoList().size());
			assertEquals(1, actual.getPhotoList().get(0).getAccountNo());
			assertEquals(1, actual.getPhotoList().get(0).getPhotoNo());
			assertFalse(actual.getPhotoList().get(0).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getPhotoList().get(0).getImageFilePath());
			assertEquals("キャプション1", actual.getPhotoList().get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getPhotoList().get(0).getDirectionKbn());
			assertEquals(1, actual.getPhotoList().get(1).getAccountNo());
			assertEquals(2, actual.getPhotoList().get(1).getPhotoNo());
			assertTrue(actual.getPhotoList().get(1).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC222.jpg", actual.getPhotoList().get(1).getImageFilePath());
			assertEquals("キャプション2", actual.getPhotoList().get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(1).getDirectionKbn());
			assertEquals(1, actual.getPhotoList().get(2).getAccountNo());
			assertEquals(3, actual.getPhotoList().get(2).getPhotoNo());
			assertTrue(actual.getPhotoList().get(2).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC333.jpg", actual.getPhotoList().get(2).getImageFilePath());
			assertEquals("キャプション3", actual.getPhotoList().get(2).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(2).getDirectionKbn());
			assertFalse(actual.getIsLast());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：ページ番号が2で、3ページに到達しない")
		void createPhotoListGetResponse_pageNo_2_lastPage() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Integer pageNo = 2;
			List<PhotoModel> photoList = createPhotoList().subList(0, 4);

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			Method createPhotoListGetResponse = PhotoRestController.class.getDeclaredMethod("createPhotoListGetResponse", List.class, Integer.class);
			createPhotoListGetResponse.setAccessible(true);

			PhotoListGetResponse actual = (PhotoListGetResponse) createPhotoListGetResponse.invoke(photoRestController, photoList, pageNo);
			assertEquals(1, actual.getPhotoList().size());
			assertEquals(1, actual.getPhotoList().get(0).getAccountNo());
			assertEquals(4, actual.getPhotoList().get(0).getPhotoNo());
			assertTrue(actual.getPhotoList().get(0).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC444.jpg", actual.getPhotoList().get(0).getImageFilePath());
			assertEquals("キャプション4", actual.getPhotoList().get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(0).getDirectionKbn());
			assertTrue(actual.getIsLast());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：ページ番号が2で、3ページに到達する")
		void createPhotoListGetResponse_pageNo_2() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Integer pageNo = 2;
			List<PhotoModel> photoList = createPhotoList();

			doReturn(3).when(photoConfig).getPhotoCountPerPage();

			Method createPhotoListGetResponse = PhotoRestController.class.getDeclaredMethod("createPhotoListGetResponse", List.class, Integer.class);
			createPhotoListGetResponse.setAccessible(true);

			PhotoListGetResponse actual = (PhotoListGetResponse) createPhotoListGetResponse.invoke(photoRestController, photoList, pageNo);
			assertEquals(3, actual.getPhotoList().size());
			assertEquals(1, actual.getPhotoList().get(0).getAccountNo());
			assertEquals(4, actual.getPhotoList().get(0).getPhotoNo());
			assertTrue(actual.getPhotoList().get(0).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC444.jpg", actual.getPhotoList().get(0).getImageFilePath());
			assertEquals("キャプション4", actual.getPhotoList().get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(0).getDirectionKbn());
			assertEquals(1, actual.getPhotoList().get(1).getAccountNo());
			assertEquals(5, actual.getPhotoList().get(1).getPhotoNo());
			assertTrue(actual.getPhotoList().get(1).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC555.jpg", actual.getPhotoList().get(1).getImageFilePath());
			assertEquals("キャプション5", actual.getPhotoList().get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(1).getDirectionKbn());
			assertEquals(1, actual.getPhotoList().get(2).getAccountNo());
			assertEquals(6, actual.getPhotoList().get(2).getPhotoNo());
			assertTrue(actual.getPhotoList().get(2).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC666.jpg", actual.getPhotoList().get(2).getImageFilePath());
			assertEquals("キャプション6", actual.getPhotoList().get(2).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(2).getDirectionKbn());
			assertFalse(actual.getIsLast());
		}
	}
}
