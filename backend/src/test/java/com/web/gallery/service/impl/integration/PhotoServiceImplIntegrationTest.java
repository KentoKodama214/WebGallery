package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoPageModel;
import com.web.gallery.model.PhotoSaveResultModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import com.web.gallery.service.impl.PhotoServiceImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoServiceImplIntegrationTest {
	@Autowired
	private PhotoServiceImpl photoServiceImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoServiceImplIntegrationTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：写真が存在しなかった場合")
		void getPhotoList_not_found() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("dddddddd"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertTrue(actual.getPhotoModelList().isEmpty());
			assertTrue(actual.getIsLast());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：写真が存在した場合で、撮影日順に並び替え（1ページ目）")
		void getPhotoList_sortBy_photoAt() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック（1ページあたり5件のうち、10件中の1ページ目）
			assertEquals(5, actual.getPhotoModelList().size());
			assertFalse(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(9L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(8L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(7L, actual.getPhotoModelList().get(2).getPhotoNo().value());
			assertEquals(6L, actual.getPhotoModelList().get(3).getPhotoNo().value());
			assertEquals(5L, actual.getPhotoModelList().get(4).getPhotoNo().value());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(0, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertFalse(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2023, 9, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC19.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション19", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(0, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：写真が存在した場合で、撮影日順に並び替え（最終ページ）")
		void getPhotoList_sortBy_photoAt_lastPage() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(2)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック（10件中の残り5件が最終ページとして取得できること）
			assertEquals(5, actual.getPhotoModelList().size());
			assertTrue(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(4L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(10L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(3L, actual.getPhotoModelList().get(2).getPhotoNo().value());
			assertEquals(2L, actual.getPhotoModelList().get(3).getPhotoNo().value());
			assertEquals(1L, actual.getPhotoModelList().get(4).getPhotoNo().value());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：写真が存在した場合で、お気に入り数順に並び替え")
		void getPhotoList_sortBy_Favorite() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック（1ページあたり5件のうち、10件中の1ページ目）
			assertEquals(5, actual.getPhotoModelList().size());
			assertFalse(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(2L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(1L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(3L, actual.getPhotoModelList().get(2).getPhotoNo().value());
			assertEquals(4L, actual.getPhotoModelList().get(3).getPhotoNo().value());
			assertEquals(5L, actual.getPhotoModelList().get(4).getPhotoNo().value());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(4, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertTrue(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション12", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(3, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());

			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.getPhotoModelList().get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(2L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：写真が存在した場合で、季節・時期順に並び替え")
		void getPhotoList_sortBy_season() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック（1ページあたり5件のうち、10件中の1ページ目）
			assertEquals(5, actual.getPhotoModelList().size());
			assertFalse(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(10L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(9L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(8L, actual.getPhotoModelList().get(2).getPhotoNo().value());
			assertEquals(7L, actual.getPhotoModelList().get(3).getPhotoNo().value());
			assertEquals(6L, actual.getPhotoModelList().get(4).getPhotoNo().value());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(0, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertFalse(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 10, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC20.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション20", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(0, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());
		}

		@Test
		@Order(6)
		@DisplayName("正常系：写真が存在した場合で、写真の向きで絞り込み")
		void getPhotoList_filtering_by_directionKbnCode() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック
			assertEquals(3, actual.getPhotoModelList().size());
			assertTrue(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(8L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(7L, actual.getPhotoModelList().get(1).getPhotoNo().value());
			assertEquals(5L, actual.getPhotoModelList().get(2).getPhotoNo().value());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(0, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertFalse(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2023, 8, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC18.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション18", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(0, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());
		}

		@Test
		@Order(7)
		@DisplayName("正常系：写真が存在した場合で、お気に入りで絞り込み")
		void getPhotoList_filtering_by_isFavoriteOnly() throws GalleryException {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(true))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック
			assertEquals(2, actual.getPhotoModelList().size());
			assertTrue(actual.getIsLast());

			// List<PhotoModel>の並び順チェック
			assertEquals(2L, actual.getPhotoModelList().get(0).getPhotoNo().value());
			assertEquals(1L, actual.getPhotoModelList().get(1).getPhotoNo().value());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(4, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertTrue(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション12", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(3, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());

			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.getPhotoModelList().get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(2L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
		}

		@Test
		@Order(8)
		@DisplayName("正常系：写真が存在した場合で、写真タグで絞り込み")
		void getPhotoList_filtering_by_tags() throws GalleryException {
			List<String> tags = new ArrayList<String>();
			tags.add("太陽");
			tags.add("bluesky");

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			PhotoPageModel actual = photoServiceImpl.getPhotoList(photoListGetModel);

			// List<PhotoModel>の数チェック
			assertEquals(1, actual.getPhotoModelList().size());
			assertTrue(actual.getIsLast());

			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.getPhotoModelList().get(0).getAccountNo().value());
			assertEquals(3, actual.getPhotoModelList().get(0).getFavoriteCount().value());
			assertTrue(actual.getPhotoModelList().get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoModelList().get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.getPhotoModelList().get(0).getImageFilePath().value());
			assertEquals("キャプション11", actual.getPhotoModelList().get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(0).getDirectionKbn());
			assertEquals(2, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());

			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.getPhotoModelList().get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(1L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
		}

		@Test
		@Order(8)
		@DisplayName("異常系：指定のアカウントが存在しない場合、PhotoNotFoundExceptionをthrowすること")
		void getPhotoList_accountNotFound() {
			List<String> tags = new ArrayList<String>();

			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("zzzzzzzz"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.pageNo(1)
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoList(photoListGetModel));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoServiceImplIntegrationTest.sql")
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void getPhotoDetail_success() throws GalleryException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.photoNo(new PhotoNo(1L))
					.build();

			PhotoDetailModel actual = photoServiceImpl.getPhotoDetail(photoDetailGetModel);
			assertEquals(1L, actual.getAccountNo().value());
			assertEquals(1L, actual.getPhotoNo().value());
			assertTrue(actual.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoAt().value());
			assertEquals(1L, actual.getLocationNo().value());
			assertEquals("住所1", actual.getGeoLocation().address().value());
			assertEquals(0, BigDecimal.valueOf(38.100).compareTo(actual.getGeoLocation().latitude().value()));
			assertEquals(0, BigDecimal.valueOf(115.100).compareTo(actual.getGeoLocation().longitude().value()));
			assertEquals("ロケーション1", actual.getLocationName().value());
			assertNull(actual.getImageFile());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.getImageFilePath().value());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle().value());
			assertEquals("title11", actual.getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actual.getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
			assertEquals(24, actual.getExifData().focalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getExifData().fValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getExifData().shutterSpeed().value()));
			assertEquals(100, actual.getExifData().iso().value());
			assertEquals(2, actual.getPhotoTagModelList().size());
			
			assertEquals(1L, actual.getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(2L, actual.getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("青空", actual.getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("bluesky", actual.getPhotoTagModelList().get(1).getTagEnglishName().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() throws GalleryException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.photoNo(new PhotoNo(11L))
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoDetail(photoDetailGetModel));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：指定のアカウントが存在しない場合、PhotoNotFoundExceptionをthrowすること")
		void getPhotoDetail_accountNotFound() {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("zzzzzzzz"))
					.photoNo(new PhotoNo(1L))
					.build();

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.getPhotoDetail(photoDetailGetModel));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoServiceImplIntegrationTest.sql")
	class savePhotos {
		/** 新規登録時のバリデーション（Content-Type・マジックバイト）を通過させるための、実際のJPEGファイルの先頭バイト列 */
		private final byte[] jpegBytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};

		PhotoDetailModel createNewPhotoWithTag() {
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(11L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(11L))
					.tagNo(new TagNo(2L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC21.jpg",
					"image/jpeg",
					jpegBytes
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル21"))
					.photoEnglishTitle(new PhotoEnglishTitle("title21"))
					.caption(new Caption("キャプション21"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList))
					.build();
		}
		
		PhotoDetailModel createNewPhoto() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC22.jpg",
					"image/jpeg",
					jpegBytes
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
					.imageFilePath(new ImageFilePath("https://www.xxx.com/aaaaaaaa/DSC222.jpg"))
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
					"DSC13.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(3L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/aaaaaaaa/DSC333.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル3"))
					.photoEnglishTitle(new PhotoEnglishTitle("title3"))
					.caption(new Caption("キャプション3"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.build();
		}
		
		List<PhotoMst> getPhotoMstData(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='" + accountId + "')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getLong("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getLong("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
		}
		
		List<PhotoTagMst> getPhotoTagMst(String accountId, Long photoNo) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no= (SELECT account_no FROM common.account where account_id='" + accountId + "') and photo_no=" + photoNo , (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：photoDetailModelListがnullの場合、終了")
		void savePhotos_photoDetailModelList_is_null() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<PhotoMst> beforeSaveData = getPhotoMstData(accountId);
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), null);
			assertNull(actual);
			List<PhotoMst> afterData = getPhotoMstData(accountId);
			assertEquals(beforeSaveData.size(), afterData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListがemptyの場合、終了")
		void savePhotos_photoDetailModelList_is_empty() throws GalleryException {
			String accountId = "aaaaaaaa";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			List<PhotoMst> beforeSaveData = getPhotoMstData(accountId);
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));
			assertNull(actual);
			List<PhotoMst> afterData = getPhotoMstData(accountId);
			assertEquals(beforeSaveData.size(), afterData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：新規登録のみ")
		void savePhotos_newPhoto() throws GalleryException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(11L), actual.getPhotoNo());
			assertEquals(new ImageFilePath("https://www.xxx.com/" + accountId + "/DSC22.jpg"), actual.getImageFilePath());
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo() > 10).toList();
			assertEquals(2, actualData.size());

			assertEquals(1L, actualData.get(0).getAccountNo());
			assertEquals(11L, actualData.get(0).getPhotoNo());
			assertEquals(transactionNow, actualData.get(0).getCreatedAt());
			assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath());
			assertEquals(0L, actualData.get(0).getLocationNo());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション21", actualData.get(0).getCaption());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(100, actualData.get(0).getIso());

			assertEquals(1L, actualData.get(1).getAccountNo());
			assertEquals(12L, actualData.get(1).getPhotoNo());
			assertEquals(transactionNow, actualData.get(1).getCreatedAt());
			assertEquals(transactionNow, actualData.get(1).getUpdatedAt());
			assertFalse(actualData.get(1).getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getPhotoAt().plusHours(9));
			assertEquals("https://www.xxx.com/" + accountId + "/DSC22.jpg", actualData.get(1).getImageFilePath());
			assertEquals(0L, actualData.get(1).getLocationNo());
			assertEquals("", actualData.get(1).getPhotoJapaneseTitle());
			assertEquals("", actualData.get(1).getPhotoEnglishTitle());
			assertEquals("", actualData.get(1).getCaption());
			assertEquals(0, actualData.get(1).getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getShutterSpeed()));
			assertEquals(0, actualData.get(1).getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo());
			assertEquals(11L, actualTagData1.get(0).getPhotoNo());
			assertEquals(1L, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1L, actualTagData1.get(1).getAccountNo());
			assertEquals(11L, actualTagData1.get(1).getPhotoNo());
			assertEquals(2L, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 12L);
			assertEquals(0, actualTagData2.size());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：更新のみ")
		void savePhotos_updatePhoto() throws GalleryException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual.getPhotoNo());
			assertNull(actual.getImageFilePath());
			List<PhotoMst> actualData1 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==2).toList();
			assertEquals(1, actualData1.size());
			assertEquals(1L, actualData1.getFirst().getAccountNo());
			assertEquals(2L, actualData1.getFirst().getPhotoNo());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData1.getFirst().getCreatedAt());
			assertEquals(transactionNow, actualData1.getFirst().getUpdatedAt());
			assertFalse(actualData1.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData1.getFirst().getPhotoAt());
			// リクエストのimageFilePathは無視され、DB上の既存パスがそのまま維持される（ファイルパス汚染防止）
			assertEquals("https://www.xxx.com/" + accountId + "/DSC12.jpg", actualData1.getFirst().getImageFilePath());
			assertEquals(0L, actualData1.getFirst().getLocationNo());
			assertEquals("タイトル2", actualData1.getFirst().getPhotoJapaneseTitle());
			assertEquals("title2", actualData1.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション2", actualData1.getFirst().getCaption());
			assertEquals(24, actualData1.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData1.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData1.getFirst().getShutterSpeed()));
			assertEquals(100, actualData1.getFirst().getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 2L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo());
			assertEquals(2L, actualTagData1.get(0).getPhotoNo());
			assertEquals(1L, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1L, actualTagData1.get(1).getAccountNo());
			assertEquals(2L, actualTagData1.get(1).getPhotoNo());
			assertEquals(2L, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1L, actualData2.getFirst().getAccountNo());
			assertEquals(3L, actualData2.getFirst().getPhotoNo());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getCreatedAt());
			assertEquals(transactionNow, actualData2.getFirst().getUpdatedAt());
			assertFalse(actualData2.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt());
			// リクエストのimageFilePathは無視され、DB上の既存パスがそのまま維持される（ファイルパス汚染防止）
			assertEquals("https://www.xxx.com/" + accountId + "/DSC13.jpg", actualData2.getFirst().getImageFilePath());
			assertEquals(0L, actualData2.getFirst().getLocationNo());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション3", actualData2.getFirst().getCaption());
			assertEquals(24, actualData2.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed()));
			assertEquals(100, actualData2.getFirst().getIso());

			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3L);
			assertEquals(0, actualTagData2.size());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：新規登録＋更新")
		void savePhotos_newPhoto_and_updatePhoto() throws GalleryException  {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			PhotoSaveResultModel actual = photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(new PhotoNo(3L), actual.getPhotoNo());
			assertEquals(new ImageFilePath("https://www.xxx.com/" + accountId + "/DSC21.jpg"), actual.getImageFilePath());
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo() > 10).toList();
			assertEquals(1, actualData.size());

			assertEquals(1L, actualData.get(0).getAccountNo());
			assertEquals(11L, actualData.get(0).getPhotoNo());
			assertEquals(transactionNow, actualData.get(0).getCreatedAt());
			assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath());
			assertEquals(0L, actualData.get(0).getLocationNo());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション21", actualData.get(0).getCaption());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(100, actualData.get(0).getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo());
			assertEquals(11L, actualTagData1.get(0).getPhotoNo());
			assertEquals(1L, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1L, actualTagData1.get(1).getAccountNo());
			assertEquals(11L, actualTagData1.get(1).getPhotoNo());
			assertEquals(2L, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1L, actualData2.getFirst().getAccountNo());
			assertEquals(3L, actualData2.getFirst().getPhotoNo());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getCreatedAt());
			assertEquals(transactionNow, actualData2.getFirst().getUpdatedAt());
			assertFalse(actualData2.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt());
			// リクエストのimageFilePathは無視され、DB上の既存パスがそのまま維持される（ファイルパス汚染防止）
			assertEquals("https://www.xxx.com/" + accountId + "/DSC13.jpg", actualData2.getFirst().getImageFilePath());
			assertEquals(0L, actualData2.getFirst().getLocationNo());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション3", actualData2.getFirst().getCaption());
			assertEquals(24, actualData2.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed()));
			assertEquals(100, actualData2.getFirst().getIso());

			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3L);
			assertEquals(0, actualTagData2.size());
		}

		@Test
		@Order(6)
		@DisplayName("異常系：FileDuplicateExceptionをthrowする（写真は複数枚）")
		void savePhotos_FileDuplicateException() throws GalleryException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC11.jpg",
					"image/jpeg",
					jpegBytes
			);
			PhotoDetailModel photoDetailModel1 = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル11"))
					.photoEnglishTitle(new PhotoEnglishTitle("title11"))
					.caption(new Caption("キャプション11"))
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.build();
			photoDetailModelList.add(photoDetailModel1);
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(FileDuplicateException.class, () -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(photoDetailModelList)));
		}

		@Test
		@Order(7)
		@DisplayName("異常系：mini-userで登録枚数の上限に達している場合、PhotoNotAdditableExceptionをthrowすること")
		void savePhotos_reachedUpperLimit_throws() {
			String accountId = "ggggggg1";

			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC7011.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(7L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.build();

			assertThrows(PhotoNotAdditableException.class,
					() -> photoServiceImpl.savePhotos(new AccountId(accountId), PhotoDetailModelList.of(List.of(photoDetailModel))));

			Integer photoCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=7", Integer.class);
			assertEquals(10, photoCount);
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoServiceImplIntegrationTest.sql")
	class deletePhotos {
		@Test
		@Order(1)
		@DisplayName("正常系：photoDeleteModelListが0件の場合、終了")
		void deletePhotos_photoDeleteModelList_empty() throws GalleryException {
			photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.empty());
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='aaaaaaaa')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getLong("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getLong("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(10, actualData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListが2件以上の場合")
		void deletePhotos_success() throws GalleryException {
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath("DSC11.jpg"))
					.build());
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(2L))
					.imageFilePath(new ImageFilePath("DSC12.jpg"))
					.build());

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.of(photoDeleteModelList));

			List<PhotoMst> actualPhotoMstData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and photo_no in (1, 2)", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getLong("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getLong("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			assertEquals(2, actualPhotoMstData.size());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMstData.get(0).getCreatedAt());
			assertEquals(transactionNow, actualPhotoMstData.get(0).getUpdatedAt());
			assertTrue(actualPhotoMstData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMstData.get(1).getCreatedAt());
			assertEquals(transactionNow, actualPhotoMstData.get(1).getUpdatedAt());
			assertTrue(actualPhotoMstData.get(1).getIsDeleted());
			
			List<PhotoMst> actualPhotoMstRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and is_deleted=false", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getLong("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getLong("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			assertEquals(8, actualPhotoMstRestData.size());
			
			List<PhotoFavorite> actualPhotoFavoriteData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite where favorite_photo_account_no=1 and favorite_photo_no in (1, 2)", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getLong("account_no"))
							.favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getLong("favorite_photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(0, actualPhotoFavoriteData.size());
			
			List<PhotoFavorite> actualPhotoFavoriteRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getLong("account_no"))
							.favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getLong("favorite_photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(4, actualPhotoFavoriteRestData.size());
			
			List<PhotoTagMst> actualPhotoTagData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no in (1,2)", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(0, actualPhotoTagData.size());
			
			List<PhotoTagMst> actualPhotoTagRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(2, actualPhotoTagRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：対象写真が存在しない場合、PhotoNotFoundExceptionをthrowする")
		void deletePhotos_PhotoNotFoundException() throws GalleryException {
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(99L))
					.imageFilePath(new ImageFilePath("DSC99.jpg"))
					.build());

			assertThrows(PhotoNotFoundException.class, () -> photoServiceImpl.deletePhotos(new AccountId("aaaaaaaa"), PhotoDeleteModelList.of(photoDeleteModelList)));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoServiceImplIntegrationTest.sql")
	class isReachedUpperLimit {
		@Test
		@Order(1)
		@DisplayName("異常系：アカウント番号がnullの場合、NullPointerExceptionをthrowする")
		void isReachedUpperLimit_accountNo_is_null() {
			assertThrows(NullPointerException.class, () -> photoServiceImpl.isReachedUpperLimit(null));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：mini-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_mini_user_reached() {
			AccountNo accountNo = new AccountNo(7L);
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：mini-userで、上限まで未登録の場合")
		void isReachedUpperLimit_mini_user_not_reached() {
			AccountNo accountNo = new AccountNo(2L);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：normal-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_normal_user_reached() {
			AccountNo accountNo = new AccountNo(3L);
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：normal-userで、上限まで未登録の場合")
		void isReachedUpperLimit_normal_user_not_reached() {
			AccountNo accountNo = new AccountNo(4L);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：special-userの場合")
		void isReachedUpperLimit_special_user() {
			AccountNo accountNo = new AccountNo(5L);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：administratorの場合")
		void isReachedUpperLimit_administrator() {
			AccountNo accountNo = new AccountNo(6L);
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
	}
}