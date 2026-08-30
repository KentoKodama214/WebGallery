package com.web.gallery.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
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

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.controller.response.PhotoListGetResponse;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoPageModel;
import com.web.gallery.model.PhotoTagModelList;
import com.web.gallery.service.impl.PhotoServiceImpl;

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
				.setControllerAdvice(new CommonRestControllerAdvice())
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
		private PhotoModelList createPhotoModelList() {
			List<PhotoModel> photoList = new ArrayList<PhotoModel>();
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"))
					.caption(new Caption("キャプション2"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC333.jpg"))
					.caption(new Caption("キャプション3"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC444.jpg"))
					.caption(new Caption("キャプション4"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC555.jpg"))
					.caption(new Caption("キャプション5"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(6L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC666.jpg"))
					.caption(new Caption("キャプション6"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(7L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC777.jpg"))
					.caption(new Caption("キャプション7"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());

			return PhotoModelList.of(photoList);
		}

		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータがある場合")
		void getPhotoList_with_null_parameter() throws Exception {
			doReturn(1L).when(sessionHelper).getAccountNo();

			// DB側で既にページング済みの結果を想定してモックする
			PhotoModelList photoList = PhotoModelList.of(createPhotoModelList().toList().subList(0, 3));
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(PhotoPageModel.of(photoList, false)).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

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
			assertEquals(new AccountNo(1L), photoListGetModel.getAccountNo());
			assertEquals(new AccountId("aaaaaaaa"), photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.NONE, photoListGetModel.getDirectionKbn());
			assertFalse(photoListGetModel.getIsFavoriteOnly().value());
			assertEquals(new ArrayList<String>(), photoListGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoListGetModel.getSortBy());
			assertEquals(1, photoListGetModel.getPageNo());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：タグに半角スペースが含まれている場合")
		void getPhotoList_with_halfspace_tag() throws Exception {
			doReturn(1L).when(sessionHelper).getAccountNo();

			PhotoModelList photoList = PhotoModelList.of(createPhotoModelList().toList().subList(3, 4));
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(PhotoPageModel.of(photoList, true)).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

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
			assertEquals(new AccountNo(1L), photoListGetModel.getAccountNo());
			assertEquals(new AccountId("aaaaaaaa"), photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.VERTICAL, photoListGetModel.getDirectionKbn());
			assertTrue(photoListGetModel.getIsFavoriteOnly().value());
			assertEquals("太陽", photoListGetModel.getTagList().get(0));
			assertEquals("海", photoListGetModel.getTagList().get(1));
			assertEquals(SortPhotoEnum.SEASON, photoListGetModel.getSortBy());
			assertEquals(2, photoListGetModel.getPageNo());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：タグに全角スペースが含まれている場合")
		void getPhotoList_with_fullspace_tag() throws Exception {
			doReturn(1L).when(sessionHelper).getAccountNo();

			PhotoModelList photoList = PhotoModelList.of(createPhotoModelList().toList().subList(3, 4));
			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(PhotoPageModel.of(photoList, true)).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

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
			assertEquals(new AccountNo(1L), photoListGetModel.getAccountNo());
			assertEquals(new AccountId("aaaaaaaa"), photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.VERTICAL, photoListGetModel.getDirectionKbn());
			assertTrue(photoListGetModel.getIsFavoriteOnly().value());
			assertEquals("太陽", photoListGetModel.getTagList().get(0));
			assertEquals("海", photoListGetModel.getTagList().get(1));
			assertEquals(SortPhotoEnum.SEASON, photoListGetModel.getSortBy());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_not_found_photo() throws Exception {
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(PhotoPageModel.of(PhotoModelList.empty(), true)).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.photoList.length()").value(0));

			PhotoListGetModel photoListGetModel = photoListGetModelCaptor.getValue();
			assertEquals(new AccountNo(1L), photoListGetModel.getAccountNo());
			assertEquals(new AccountId("aaaaaaaa"), photoListGetModel.getPhotoAccountId());
			assertEquals(DirectionEnum.NONE, photoListGetModel.getDirectionKbn());
			assertFalse(photoListGetModel.getIsFavoriteOnly().value());
			assertEquals(new ArrayList<String>(), photoListGetModel.getTagList());
			assertEquals(SortPhotoEnum.PHOTO_AT, photoListGetModel.getSortBy());
		}

		@Test
		@Order(5)
		@DisplayName("異常系：ページ番号が0以下。BadRequestExceptionをthrowする")
		void getPhotoList_BadRequestException_pageNo_not_positive() throws Exception {
			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos")
					.param("pageNo", "0"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).getPhotoList(any(PhotoListGetModel.class));
		}

		@Test
		@Order(6)
		@DisplayName("異常系：タグの指定数が上限（20件）を超える場合。BadRequestExceptionをthrowする")
		void getPhotoList_BadRequestException_tagList_exceeds_maxSize() throws Exception {
			String tooManyTags = String.join(" ", Collections.nCopies(21, "tag"));

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos")
					.param("tagList", tooManyTags))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).getPhotoList(any(PhotoListGetModel.class));
		}

		@Test
		@Order(7)
		@DisplayName("正常系：タグに連続した空白が含まれていても、空文字トークンは除去されて渡される")
		void getPhotoList_tagList_ignores_blank_tokens() throws Exception {
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoListGetModel> photoListGetModelCaptor = ArgumentCaptor.forClass(PhotoListGetModel.class);
			doReturn(PhotoPageModel.of(PhotoModelList.empty(), true)).when(photoServiceImpl).getPhotoList(photoListGetModelCaptor.capture());

			// 「太陽」＋全角スペース20個＋「海」。バリデーションは非空トークン2件として通過するが、
			// 旧実装ではモデルのタグリストに空文字が20個以上残り、SQLの相関サブクエリを無制限に増やせた
			String tagListParam = "太陽" + "　".repeat(20) + "海";

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos")
					.param("tagList", tagListParam))
				.andExpect(status().isOk());

			List<String> actualTagList = photoListGetModelCaptor.getValue().getTagList();
			assertEquals(List.of("太陽", "海"), actualTagList);
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
			String photoJapaneseTitle = "タイトル";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			ArgumentCaptor<PhotoDetailModelList> photoDetailModelCaptor = ArgumentCaptor.forClass(PhotoDetailModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doReturn(new PhotoNo(1L)).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("imageFilePath", imageFilePath)
					.param("photoJapaneseTitle", photoJapaneseTitle)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));

			PhotoDetailModelList photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getAccountNo());
			assertNull(photoDetailModelList.getFirst().getPhotoNo());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().address());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().latitude());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().longitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath().value());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle().value());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getExifData().focalLength());
			assertNull(photoDetailModelList.getFirst().getExifData().fValue());
			assertNull(photoDetailModelList.getFirst().getExifData().shutterSpeed());
			assertNull(photoDetailModelList.getFirst().getExifData().iso());
			assertTrue(photoDetailModelList.getFirst().getPhotoTagModelList().isEmpty());

			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
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
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDetailModelList> photoDetailModelCaptor = ArgumentCaptor.forClass(PhotoDetailModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doReturn(new PhotoNo(1L)).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
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

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(new AccountNo(1L));

			PhotoDetailModelList photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDetailModelList.getFirst().getPhotoNo().value());
			assertFalse(photoDetailModelList.getFirst().getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoDetailModelList.getFirst().getPhotoAt().value());
			assertEquals(1L, photoDetailModelList.getFirst().getLocationNo().value());
			assertEquals(address, photoDetailModelList.getFirst().getGeoLocation().address().value());
			assertEquals(0, BigDecimal.valueOf(35.000).compareTo(photoDetailModelList.getFirst().getGeoLocation().latitude().value()));
			assertEquals(0, BigDecimal.valueOf(135.000).compareTo(photoDetailModelList.getFirst().getGeoLocation().longitude().value()));
			assertEquals(locationName, photoDetailModelList.getFirst().getLocationName().value());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath().value());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle().value());
			assertEquals(photoEnglishTitle, photoDetailModelList.getFirst().getPhotoEnglishTitle().value());
			assertEquals(caption, photoDetailModelList.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertEquals(50, photoDetailModelList.getFirst().getExifData().focalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(photoDetailModelList.getFirst().getExifData().fValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.001).compareTo(photoDetailModelList.getFirst().getExifData().shutterSpeed().value()));
			assertEquals(100, photoDetailModelList.getFirst().getExifData().iso().value());

			// アカウント番号・写真番号・タグ番号はリクエスト値を採用せず、集約側で採番・振り直しされるためこの時点ではnull（accountNoはセッション値）
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getAccountNo());
			assertNull(photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagNo());
			assertNull(photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals("太陽", photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", photoDetailModelList.getFirst().getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getAccountNo());
			assertNull(photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagNo());
			assertEquals("海", photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("", photoDetailModelList.getFirst().getPhotoTagModelList().get(1).getTagEnglishName().value());

			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(3)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
		void savePhoto_ForbiddenAccountException() throws Exception {
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isForbidden());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(AccountNo.class));
			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(4)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：登録上限に達している。PhotoNotAdditableExceptionをthrowする")
		void savePhoto_PhotoNotAdditableException() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(5)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイル、ファイルパスともにnull。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_null() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(6)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイルがnull、ファイルパスがblank。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_blank() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(7)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：画像ファイル、ファイルパス以外のパラメータ不正。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_others() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("imageFilePath", "https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.param("directionKbn", "VERTICAL")
					.param("focalLength", "-1"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(8)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：FileDuplicateExceptionをthrowする")
		void savePhoto_FileDuplicateException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			String photoJapaneseTitle = "タイトル";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDetailModelList> photoDetailModelCaptor = ArgumentCaptor.forClass(PhotoDetailModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doThrow(FileDuplicateException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("photoJapaneseTitle", photoJapaneseTitle)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(AccountNo.class));

			PhotoDetailModelList photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDetailModelList.getFirst().getPhotoNo().value());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().address());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().latitude());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().longitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath().value());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle().value());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getExifData().focalLength());
			assertNull(photoDetailModelList.getFirst().getExifData().fValue());
			assertNull(photoDetailModelList.getFirst().getExifData().shutterSpeed());
			assertNull(photoDetailModelList.getFirst().getExifData().iso());
			assertTrue(photoDetailModelList.getFirst().getPhotoTagModelList().isEmpty());

			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(9)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void savePhoto_RegistFailureException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			String photoJapaneseTitle = "タイトル";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDetailModelList> photoDetailModelCaptor = ArgumentCaptor.forClass(PhotoDetailModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doThrow(RegistFailureException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("photoJapaneseTitle", photoJapaneseTitle)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(AccountNo.class));

			PhotoDetailModelList photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDetailModelList.getFirst().getPhotoNo().value());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().address());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().latitude());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().longitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath().value());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle().value());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getExifData().focalLength());
			assertNull(photoDetailModelList.getFirst().getExifData().fValue());
			assertNull(photoDetailModelList.getFirst().getExifData().shutterSpeed());
			assertNull(photoDetailModelList.getFirst().getExifData().iso());
			assertTrue(photoDetailModelList.getFirst().getPhotoTagModelList().isEmpty());

			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(10)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void savePhoto_UpdateFailureException() throws Exception {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			String photoJapaneseTitle = "タイトル";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes());

			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDetailModelList> photoDetailModelCaptor = ArgumentCaptor.forClass(PhotoDetailModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doThrow(UpdateFailureException.class).when(photoServiceImpl).savePhotos(photoAcountIdCaptor.capture(), photoDetailModelCaptor.capture());

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.file(multipartFile)
					.param("photoNo", "1")
					.param("imageFilePath", imageFilePath)
					.param("photoJapaneseTitle", photoJapaneseTitle)
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isConflict());

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(AccountNo.class));

			PhotoDetailModelList photoDetailModelList = photoDetailModelCaptor.getValue();
			assertEquals(1, photoDetailModelList.size());
			assertEquals(new AccountNo(1L), photoDetailModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDetailModelList.getFirst().getPhotoNo().value());
			assertNull(photoDetailModelList.getFirst().getIsFavorite());
			assertNull(photoDetailModelList.getFirst().getPhotoAt());
			assertNull(photoDetailModelList.getFirst().getLocationNo());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().address());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().latitude());
			assertNull(photoDetailModelList.getFirst().getGeoLocation().longitude());
			assertNull(photoDetailModelList.getFirst().getLocationName());
			assertNotNull(photoDetailModelList.getFirst().getImageFile());
			assertEquals(imageFilePath, photoDetailModelList.getFirst().getImageFilePath().value());
			assertEquals(photoJapaneseTitle, photoDetailModelList.getFirst().getPhotoJapaneseTitle().value());
			assertNull(photoDetailModelList.getFirst().getPhotoEnglishTitle());
			assertNull(photoDetailModelList.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoDetailModelList.getFirst().getDirectionKbn());
			assertNull(photoDetailModelList.getFirst().getExifData().focalLength());
			assertNull(photoDetailModelList.getFirst().getExifData().fValue());
			assertNull(photoDetailModelList.getFirst().getExifData().shutterSpeed());
			assertNull(photoDetailModelList.getFirst().getExifData().iso());
			assertTrue(photoDetailModelList.getFirst().getPhotoTagModelList().isEmpty());

			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
		}

		@Test
		@Order(11)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：写真タイトル日本語名が未入力。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_photoJapaneseTitle_blank() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("imageFilePath", "https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.param("directionKbn", "VERTICAL"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
		}

		@Test
		@Order(12)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：タグ英語名が20文字を超える。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_tagEnglishName_too_long() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(multipart("/api/v1/accounts/aaaaaaaa/photos")
					.param("accountNo", "1")
					.param("imageFilePath", "https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.param("photoJapaneseTitle", "タイトル")
					.param("directionKbn", "VERTICAL")
					.param("photoTagRegistRequestList[0].accountNo", "1")
					.param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRegistRequestList[0].tagEnglishName", "abcdefghijklmnopqrstu"))
				.andExpect(status().isBadRequest());

			verify(photoServiceImpl, times(0)).savePhotos(any(AccountId.class), any(PhotoDetailModelList.class));
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
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDeleteModelList> photoDeleteModelCaptor = ArgumentCaptor.forClass(PhotoDeleteModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doNothing().when(photoServiceImpl).deletePhotos(photoAcountIdCaptor.capture(), photoDeleteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真削除が完了しました。"));

			PhotoDeleteModelList photoDeleteModelList = photoDeleteModelCaptor.getValue();
			assertEquals(1, photoDeleteModelList.size());
			assertEquals(new AccountNo(1L), photoDeleteModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDeleteModelList.getFirst().getPhotoNo().value());
			assertEquals(imageFilePath, photoDeleteModelList.getFirst().getImageFilePath().value());
			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
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
			doReturn(1L).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoDeleteModelList> photoDeleteModelCaptor = ArgumentCaptor.forClass(PhotoDeleteModelList.class);
			ArgumentCaptor<AccountId> photoAcountIdCaptor = ArgumentCaptor.forClass(AccountId.class);
			doThrow(UpdateFailureException.class).when(photoServiceImpl).deletePhotos(photoAcountIdCaptor.capture(), photoDeleteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/accounts/aaaaaaaa/photos")
					.contentType("application/json")
					.content(readJsonFile("delete_photo.json")))
				.andExpect(status().isConflict());

			PhotoDeleteModelList photoDeleteModelList = photoDeleteModelCaptor.getValue();
			assertEquals(1, photoDeleteModelList.size());
			assertEquals(new AccountNo(1L), photoDeleteModelList.getFirst().getAccountNo());
			assertEquals(1L, photoDeleteModelList.getFirst().getPhotoNo().value());
			assertEquals(imageFilePath, photoDeleteModelList.getFirst().getImageFilePath().value());
			assertEquals(new AccountId("aaaaaaaa"), photoAcountIdCaptor.getValue());
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class createPhotoListGetResponse {
		private PhotoModelList createPhotoList() {
			List<PhotoModel> photoList = new ArrayList<PhotoModel>();
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"))
					.caption(new Caption("キャプション2"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC333.jpg"))
					.caption(new Caption("キャプション3"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC444.jpg"))
					.caption(new Caption("キャプション4"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(5L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC555.jpg"))
					.caption(new Caption("キャプション5"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(6L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC666.jpg"))
					.caption(new Caption("キャプション6"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());
			photoList.add(PhotoModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(7L))
					.favoriteCount(new FavoriteCount(1))
					.isFavorite(new IsFavorite(true))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC777.jpg"))
					.caption(new Caption("キャプション7"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.photoTagModelList(PhotoTagModelList.empty())
					.build());

			return PhotoModelList.of(photoList);
		}

		@Test
		@Order(1)
		@DisplayName("正常系：DB側で絞り込み済みの写真一覧・isLastをそのままレスポンスに変換すること")
		void createPhotoListGetResponse_passThrough() {
			PhotoModelList photoList = PhotoModelList.of(createPhotoList().toList().subList(0, 3));
			PhotoPageModel photoPageModel = PhotoPageModel.of(photoList, false);

			PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
			assertFalse(actual.getIsLast());
			assertEquals(3, actual.getPhotoList().size());
			assertEquals(1L, actual.getPhotoList().get(0).getAccountNo());
			assertEquals(1L, actual.getPhotoList().get(0).getPhotoNo());
			assertFalse(actual.getPhotoList().get(0).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getPhotoList().get(0).getImageFilePath());
			assertEquals("キャプション1", actual.getPhotoList().get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getPhotoList().get(0).getDirectionKbn());
			assertEquals(1L, actual.getPhotoList().get(1).getAccountNo());
			assertEquals(2L, actual.getPhotoList().get(1).getPhotoNo());
			assertTrue(actual.getPhotoList().get(1).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC222.jpg", actual.getPhotoList().get(1).getImageFilePath());
			assertEquals("キャプション2", actual.getPhotoList().get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(1).getDirectionKbn());
			assertEquals(1L, actual.getPhotoList().get(2).getAccountNo());
			assertEquals(3L, actual.getPhotoList().get(2).getPhotoNo());
			assertTrue(actual.getPhotoList().get(2).getIsFavorite());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC333.jpg", actual.getPhotoList().get(2).getImageFilePath());
			assertEquals("キャプション3", actual.getPhotoList().get(2).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(2).getDirectionKbn());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：isLastがtrueの場合、そのままtrueとして変換されること")
		void createPhotoListGetResponse_isLast_true() {
			PhotoModelList photoList = PhotoModelList.of(createPhotoList().toList().subList(0, 1));
			PhotoPageModel photoPageModel = PhotoPageModel.of(photoList, true);

			PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
			assertTrue(actual.getIsLast());
			assertEquals(1, actual.getPhotoList().size());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：写真が0件の場合、空リストに変換されること")
		void createPhotoListGetResponse_empty() {
			PhotoPageModel photoPageModel = PhotoPageModel.of(PhotoModelList.empty(), true);

			PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
			assertTrue(actual.getIsLast());
			assertTrue(actual.getPhotoList().isEmpty());
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoUpperLimit {
		@Test
		@Order(1)
		@DisplayName("正常系：自分のアカウントで上限未到達の場合")
		void getPhotoUpperLimit_not_reached() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos/upper-limit"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isReachedUpperLimit").value(false));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：自分のアカウントで上限到達の場合")
		void getPhotoUpperLimit_reached() throws Exception {
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			doReturn(1L).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(new AccountNo(1L));

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos/upper-limit"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isReachedUpperLimit").value(true));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：他人のアカウントの場合はfalse")
		void getPhotoUpperLimit_other_account() throws Exception {
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			mockMvc.perform(get("/api/v1/accounts/aaaaaaaa/photos/upper-limit"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isReachedUpperLimit").value(false));

			verify(photoServiceImpl, times(0)).isReachedUpperLimit(any(AccountNo.class));
		}
	}
}
