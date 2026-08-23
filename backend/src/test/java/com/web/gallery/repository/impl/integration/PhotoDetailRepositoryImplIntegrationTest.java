package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.repository.impl.PhotoDetailRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoDetailRepositoryImplIntegrationTest {
	@Autowired
	private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoDetailRepositoryImplIntegrationTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_photo_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(3L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			assertEquals(0, actual.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が1件以上、写真タグが0件の場合")
		void getPhotoList_photoTag_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(2L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(2, actual.size());
			assertEquals(new AccountNo(2L), actual.get(0).getAccountNo());
			assertEquals(2L, actual.get(0).getPhotoNo().value());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
			assertEquals(new AccountNo(2L), actual.get(1).getAccountNo());
			assertEquals(1L, actual.get(1).getPhotoNo().value());
			assertEquals(0, actual.get(1).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：写真が1件以上、写真タグが1件以上の場合")
		void getPhotoList_photoTag_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(2, actual.size());

			assertEquals(new AccountNo(1L), actual.get(0).getAccountNo());
			assertEquals(2L, actual.get(0).getPhotoNo().value());
			assertEquals(3, actual.get(0).getPhotoTagModelList().size());
			assertEquals(new AccountNo(1L), actual.get(0).getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(2L, actual.get(0).getPhotoTagModelList().get(0).getPhotoNo().value());
			assertEquals(1L, actual.get(0).getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", actual.get(0).getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", actual.get(0).getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(1L), actual.get(0).getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(2L, actual.get(0).getPhotoTagModelList().get(1).getPhotoNo().value());
			assertEquals(2L, actual.get(0).getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("曇天", actual.get(0).getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("cloudy", actual.get(0).getPhotoTagModelList().get(1).getTagEnglishName().value());
			assertEquals(new AccountNo(1L), actual.get(0).getPhotoTagModelList().get(2).getAccountNo());
			assertEquals(2L, actual.get(0).getPhotoTagModelList().get(2).getPhotoNo().value());
			assertEquals(3L, actual.get(0).getPhotoTagModelList().get(2).getTagNo().value());
			assertEquals("花", actual.get(0).getPhotoTagModelList().get(2).getTagJapaneseName().value());
			assertEquals("flower", actual.get(0).getPhotoTagModelList().get(2).getTagEnglishName().value());

			assertEquals(new AccountNo(1L), actual.get(1).getAccountNo());
			assertEquals(1L, actual.get(1).getPhotoNo().value());
			assertEquals(2, actual.get(1).getPhotoTagModelList().size());
			assertEquals(new AccountNo(1L), actual.get(1).getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(1L, actual.get(1).getPhotoTagModelList().get(0).getPhotoNo().value());
			assertEquals(1L, actual.get(1).getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", actual.get(1).getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", actual.get(1).getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(1L), actual.get(1).getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(1L, actual.get(1).getPhotoTagModelList().get(1).getPhotoNo().value());
			assertEquals(2L, actual.get(1).getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("青空", actual.get(1).getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("bluesky", actual.get(1).getPhotoTagModelList().get(1).getTagEnglishName().value());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：向き区分で絞り込まれること")
		void getPhotoList_filterByDirectionKbn() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(1, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo().value());
			assertEquals(DirectionEnum.VERTICAL, actual.get(0).getDirectionKbn());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：お気に入りのみに絞り込まれること")
		void getPhotoList_filterByFavorite() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(true))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(1, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo().value());
			assertTrue(actual.get(0).getIsFavorite().value());
		}

		@Test
		@Order(6)
		@DisplayName("正常系：タグをすべて保持する写真のみに絞り込まれること")
		void getPhotoList_filterByTags() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of("太陽", "青空"))
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(1, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo().value());
		}

		@Test
		@Order(7)
		@DisplayName("正常系：お気に入り数の降順に並び替えられること")
		void getPhotoList_sortBy_favorite() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.FAVORITE)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(2, actual.size());
			assertEquals(1L, actual.get(0).getPhotoNo().value());
			assertEquals(2, actual.get(0).getFavoriteCount().value());
			assertEquals(2L, actual.get(1).getPhotoNo().value());
			assertEquals(1, actual.get(1).getFavoriteCount().value());
		}

		@Test
		@Order(8)
		@DisplayName("正常系：撮影日時の降順に並び替えられること")
		void getPhotoList_sortBy_photoAt() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(List.of())
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();

			PhotoModelList actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

			assertEquals(2, actual.size());
			assertEquals(2L, actual.get(0).getPhotoNo().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals(1L, actual.get(1).getPhotoNo().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.get(1).getPhotoAt().value());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoDetailRepositoryImplIntegrationTest.sql")
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：写真のメタデータがデフォルト値、写真タグが0件の場合")
		void getPhotoDetail_photoTag_default_value_not_found() throws GalleryException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(2L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(new AccountNo(2L), actual.getAccountNo());
			assertEquals(1L, actual.getPhotoNo().value());
			assertFalse(actual.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2022, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoAt().value());
			assertEquals(4L, actual.getLocationNo().value());
			assertEquals("住所4", actual.getGeoLocation().address().value());
			assertEquals(0, BigDecimal.valueOf(38.400).compareTo(actual.getGeoLocation().latitude().value()));
			assertEquals(0, BigDecimal.valueOf(115.400).compareTo(actual.getGeoLocation().longitude().value()));
			assertEquals("ロケーション4", actual.getLocationName().value());
			assertEquals("https://www.xxx.com/DSC444.jpg", actual.getImageFilePath().value());
			assertEquals("タイトル21", actual.getPhotoJapaneseTitle().value());
			assertEquals("title21", actual.getPhotoEnglishTitle().value());
			assertEquals("キャプション21", actual.getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
			assertEquals(80, actual.getExifData().focalLength().value());
			assertEquals(0, BigDecimal.valueOf(12.0).compareTo(actual.getExifData().fValue().value()));
			assertEquals(0, BigDecimal.valueOf(5).compareTo(actual.getExifData().shutterSpeed().value()));
			assertEquals(800, actual.getExifData().iso().value());
			assertEquals(0, actual.getPhotoTagModelList().size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真のメタデータがデフォルト値でない場、写真タグが1件以上の場合")
		void getPhotoDetail_not_default_value_photoTag_found() throws GalleryException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(new AccountNo(1L), actual.getAccountNo());
			assertEquals(1L, actual.getPhotoNo().value());
			assertTrue(actual.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoAt().value());
			assertEquals(1L, actual.getLocationNo().value());
			assertEquals("住所1", actual.getGeoLocation().address().value());
			assertEquals(0, BigDecimal.valueOf(38.100).compareTo(actual.getGeoLocation().latitude().value()));
			assertEquals(0, BigDecimal.valueOf(115.100).compareTo(actual.getGeoLocation().longitude().value()));
			assertEquals("ロケーション1", actual.getLocationName().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actual.getImageFilePath().value());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle().value());
			assertEquals("title11", actual.getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actual.getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertEquals(24, actual.getExifData().focalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getExifData().fValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getExifData().shutterSpeed().value()));
			assertEquals(100, actual.getExifData().iso().value());
			assertEquals(2, actual.getPhotoTagModelList().size());
			
			assertEquals(new AccountNo(1L), actual.getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(1L, actual.getPhotoTagModelList().get(0).getPhotoNo().value());
			assertEquals(1L, actual.getPhotoTagModelList().get(0).getTagNo().value());
			assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName().value());
			assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName().value());
			assertEquals(new AccountNo(1L), actual.getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(1L, actual.getPhotoTagModelList().get(1).getPhotoNo().value());
			assertEquals(2L, actual.getPhotoTagModelList().get(1).getTagNo().value());
			assertEquals("青空", actual.getPhotoTagModelList().get(1).getTagJapaneseName().value());
			assertEquals("bluesky", actual.getPhotoTagModelList().get(1).getTagEnglishName().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(99L))
					.build();
			
			assertThrows(PhotoNotFoundException.class, () -> photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel));
		}
	}
}