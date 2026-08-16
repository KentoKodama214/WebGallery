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
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModelList;
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
		void getPhotoList_not_found() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("dddddddd"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertTrue(actual.isEmpty());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が存在した場合で、撮影日順に並び替え")
		void getPhotoList_sortBy_photoAt() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(9L, actual.get(0).getPhotoNo().value());
			assertEquals(8L, actual.get(1).getPhotoNo().value());
			assertEquals(7L, actual.get(2).getPhotoNo().value());
			assertEquals(6L, actual.get(3).getPhotoNo().value());
			assertEquals(5L, actual.get(4).getPhotoNo().value());
			assertEquals(4L, actual.get(5).getPhotoNo().value());
			assertEquals(10L, actual.get(6).getPhotoNo().value());
			assertEquals(3L, actual.get(7).getPhotoNo().value());
			assertEquals(2L, actual.get(8).getPhotoNo().value());
			assertEquals(1L, actual.get(9).getPhotoNo().value());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(0, actual.get(0).getFavoriteCount().value());
			assertFalse(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2023, 9, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC19.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション19", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：写真が存在した場合で、お気に入り数順に並び替え")
		void getPhotoList_sortBy_Favorite() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(2L, actual.get(0).getPhotoNo().value());
			assertEquals(1L, actual.get(1).getPhotoNo().value());
			assertEquals(3L, actual.get(2).getPhotoNo().value());
			assertEquals(4L, actual.get(3).getPhotoNo().value());
			assertEquals(5L, actual.get(4).getPhotoNo().value());
			assertEquals(6L, actual.get(5).getPhotoNo().value());
			assertEquals(7L, actual.get(6).getPhotoNo().value());
			assertEquals(8L, actual.get(7).getPhotoNo().value());
			assertEquals(9L, actual.get(8).getPhotoNo().value());
			assertEquals(10L, actual.get(9).getPhotoNo().value());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(4, actual.get(0).getFavoriteCount().value());
			assertTrue(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション12", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(3, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(2L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：写真が存在した場合で、季節・時期順に並び替え")
		void getPhotoList_sortBy_season() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(10L, actual.get(0).getPhotoNo().value());
			assertEquals(9L, actual.get(1).getPhotoNo().value());
			assertEquals(8L, actual.get(2).getPhotoNo().value());
			assertEquals(7L, actual.get(3).getPhotoNo().value());
			assertEquals(6L, actual.get(4).getPhotoNo().value());
			assertEquals(5L, actual.get(5).getPhotoNo().value());
			assertEquals(4L, actual.get(6).getPhotoNo().value());
			assertEquals(3L, actual.get(7).getPhotoNo().value());
			assertEquals(2L, actual.get(8).getPhotoNo().value());
			assertEquals(1L, actual.get(9).getPhotoNo().value());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(0, actual.get(0).getFavoriteCount().value());
			assertFalse(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 10, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC20.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション20", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：写真が存在した場合で、写真の向きで絞り込み")
		void getPhotoList_filtering_by_directionKbnCode() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(new IsFavoriteOnly(false))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(3, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(8L, actual.get(0).getPhotoNo().value());
			assertEquals(7L, actual.get(1).getPhotoNo().value());
			assertEquals(5L, actual.get(2).getPhotoNo().value());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(0, actual.get(0).getFavoriteCount().value());
			assertFalse(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2023, 8, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC18.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション18", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：写真が存在した場合で、お気に入りで絞り込み")
		void getPhotoList_filtering_by_isFavoriteOnly() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountId(new AccountId("aaaaaaaa"))
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(new IsFavoriteOnly(true))
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(2, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(2L, actual.get(0).getPhotoNo().value());
			assertEquals(1L, actual.get(1).getPhotoNo().value());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(4, actual.get(0).getFavoriteCount().value());
			assertTrue(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション12", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(3, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(2L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真が存在した場合で、写真タグで絞り込み")
		void getPhotoList_filtering_by_tags() {
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
					.build();
			
			PhotoModelList actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(1, actual.size());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1L, actual.get(0).getAccountNo().value());
			assertEquals(3, actual.get(0).getFavoriteCount().value());
			assertTrue(actual.get(0).getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.get(0).getImageFilePath().value());
			assertEquals("キャプション11", actual.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(2, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo().value() == 1).toList().getFirst();
			assertEquals(1L, actualTag.getAccountNo().value());
			assertEquals(1L, actualTag.getPhotoNo().value());
			assertEquals(1L, actualTag.getTagNo().value());
			assertEquals("太陽", actualTag.getTagJapaneseName().value());
			assertEquals("sun", actualTag.getTagEnglishName().value());
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
		void getPhotoDetail_success() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			PhotoDetailModel actual = photoServiceImpl.getPhotoDetail(photoDetailGetModel);
			assertEquals(1L, actual.getAccountNo().value());
			assertEquals(1L, actual.getPhotoNo().value());
			assertTrue(actual.getIsFavorite().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoAt().value());
			assertEquals(1L, actual.getLocationNo().value());
			assertEquals("住所1", actual.getAddress().value());
			assertEquals(0, BigDecimal.valueOf(38.100).compareTo(actual.getLatitude().value()));
			assertEquals(0, BigDecimal.valueOf(115.100).compareTo(actual.getLongitude().value()));
			assertEquals("ロケーション1", actual.getLocationName().value());
			assertNull(actual.getImageFile());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.getImageFilePath().value());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle().value());
			assertEquals("title11", actual.getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actual.getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
			assertEquals(24, actual.getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed().value()));
			assertEquals(100, actual.getIso().value());
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
		void getPhotoDetail_PhotoNotFoundException() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAccountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(11L))
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
					"multipart/form-data",
					"image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル21"))
					.photoEnglishTitle(new PhotoEnglishTitle("title21"))
					.caption(new Caption("キャプション21"))
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
					.photoTagModelList(PhotoTagModelList.of(photoTagModelList))
					.build();
		}
		
		PhotoDetailModel createNewPhoto() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC22.jpg",
					"multipart/form-data",
					"image".getBytes()
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
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
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
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
					.build();
		}
		
		List<PhotoMst> getPhotoMstData(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='" + accountId + "')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());
		}
		
		List<PhotoTagMst> getPhotoTagMst(String accountId, Long photoNo) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no= (SELECT account_no FROM common.account where account_id='" + accountId + "') and photo_no=" + photoNo , (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.tagNo(new TagNo(rs.getLong("tag_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
							.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：photoDetailModelListがnullの場合、終了")
		void savePhotos_photoDetailModelList_is_null() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			List<PhotoMst> beforeSaveData = getPhotoMstData(accountId);
			Long actual = photoServiceImpl.savePhotos(accountId, null);
			assertNull(actual);
			List<PhotoMst> afterData = getPhotoMstData(accountId);
			assertEquals(beforeSaveData.size(), afterData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListがemptyの場合、終了")
		void savePhotos_photoDetailModelList_is_empty() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			List<PhotoMst> beforeSaveData = getPhotoMstData(accountId);
			Long actual = photoServiceImpl.savePhotos(accountId, PhotoDetailModelList.of(photoDetailModelList));
			assertNull(actual);
			List<PhotoMst> afterData = getPhotoMstData(accountId);
			assertEquals(beforeSaveData.size(), afterData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：新規登録のみ")
		void savePhotos_newPhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Long actual = photoServiceImpl.savePhotos(accountId, PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(11, actual);
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo().value() > 10).toList();
			assertEquals(2, actualData.size());

			assertEquals(1L, actualData.get(0).getAccountNo().value());
			assertEquals(11L, actualData.get(0).getPhotoNo().value());
			assertEquals(transactionNow, actualData.get(0).getCreatedAt().value());
			assertEquals(transactionNow, actualData.get(0).getUpdatedAt().value());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals(0L, actualData.get(0).getLocationNo().value());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション21", actualData.get(0).getCaption().value());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(100, actualData.get(0).getIso().value());

			assertEquals(1L, actualData.get(1).getAccountNo().value());
			assertEquals(12L, actualData.get(1).getPhotoNo().value());
			assertEquals(transactionNow, actualData.get(1).getCreatedAt().value());
			assertEquals(transactionNow, actualData.get(1).getUpdatedAt().value());
			assertFalse(actualData.get(1).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getPhotoAt().value().plusHours(9));
			assertEquals("https://www.xxx.com/" + accountId + "/DSC22.jpg", actualData.get(1).getImageFilePath().value());
			assertEquals(0L, actualData.get(1).getLocationNo().value());
			assertEquals("", actualData.get(1).getPhotoJapaneseTitle().value());
			assertEquals("", actualData.get(1).getPhotoEnglishTitle().value());
			assertEquals("", actualData.get(1).getCaption().value());
			assertEquals(0, actualData.get(1).getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getShutterSpeed().value()));
			assertEquals(0, actualData.get(1).getIso().value());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo().value());
			assertEquals(11L, actualTagData1.get(0).getPhotoNo().value());
			assertEquals(1L, actualTagData1.get(0).getTagNo().value());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName().value());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName().value());
			assertEquals(1L, actualTagData1.get(1).getAccountNo().value());
			assertEquals(11L, actualTagData1.get(1).getPhotoNo().value());
			assertEquals(2L, actualTagData1.get(1).getTagNo().value());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName().value());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName().value());
			
			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 12L);
			assertEquals(0, actualTagData2.size());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：更新のみ")
		void savePhotos_updatePhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 更新1枚目
			PhotoDetailModel photoDetailModel1 = createUpdatePhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 更新2枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Long actual = photoServiceImpl.savePhotos(accountId, PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(3, actual);
			List<PhotoMst> actualData1 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo().value()==2).toList();
			assertEquals(1, actualData1.size());
			assertEquals(1L, actualData1.getFirst().getAccountNo().value());
			assertEquals(2L, actualData1.getFirst().getPhotoNo().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData1.getFirst().getCreatedAt().value());
			assertEquals(transactionNow, actualData1.getFirst().getUpdatedAt().value());
			assertFalse(actualData1.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData1.getFirst().getPhotoAt().value());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC222.jpg", actualData1.getFirst().getImageFilePath().value());
			assertEquals(0L, actualData1.getFirst().getLocationNo().value());
			assertEquals("タイトル2", actualData1.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title2", actualData1.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション2", actualData1.getFirst().getCaption().value());
			assertEquals(24, actualData1.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData1.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData1.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualData1.getFirst().getIso().value());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 2L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo().value());
			assertEquals(2L, actualTagData1.get(0).getPhotoNo().value());
			assertEquals(1L, actualTagData1.get(0).getTagNo().value());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName().value());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName().value());
			assertEquals(1L, actualTagData1.get(1).getAccountNo().value());
			assertEquals(2L, actualTagData1.get(1).getPhotoNo().value());
			assertEquals(2L, actualTagData1.get(1).getTagNo().value());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName().value());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName().value());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo().value()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1L, actualData2.getFirst().getAccountNo().value());
			assertEquals(3L, actualData2.getFirst().getPhotoNo().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getCreatedAt().value());
			assertEquals(transactionNow, actualData2.getFirst().getUpdatedAt().value());
			assertFalse(actualData2.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt().value());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC333.jpg", actualData2.getFirst().getImageFilePath().value());
			assertEquals(0L, actualData2.getFirst().getLocationNo().value());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション3", actualData2.getFirst().getCaption().value());
			assertEquals(24, actualData2.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualData2.getFirst().getIso().value());

			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3L);
			assertEquals(0, actualTagData2.size());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：新規登録＋更新")
		void savePhotos_newPhoto_and_updatePhoto() throws FileDuplicateException, RegistFailureException, UpdateFailureException  {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			PhotoDetailModel photoDetailModel1 = createNewPhotoWithTag();
			photoDetailModelList.add(photoDetailModel1);
			// 更新1枚目
			PhotoDetailModel photoDetailModel2 = createUpdatePhoto();
			photoDetailModelList.add(photoDetailModel2);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Long actual = photoServiceImpl.savePhotos(accountId, PhotoDetailModelList.of(photoDetailModelList));

			assertEquals(3, actual);
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo().value() > 10).toList();
			assertEquals(1, actualData.size());

			assertEquals(1L, actualData.get(0).getAccountNo().value());
			assertEquals(11L, actualData.get(0).getPhotoNo().value());
			assertEquals(transactionNow, actualData.get(0).getCreatedAt().value());
			assertEquals(transactionNow, actualData.get(0).getUpdatedAt().value());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals(0L, actualData.get(0).getLocationNo().value());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション21", actualData.get(0).getCaption().value());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(100, actualData.get(0).getIso().value());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11L);
			assertEquals(2, actualTagData1.size());
			assertEquals(1L, actualTagData1.get(0).getAccountNo().value());
			assertEquals(11L, actualTagData1.get(0).getPhotoNo().value());
			assertEquals(1L, actualTagData1.get(0).getTagNo().value());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName().value());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName().value());
			assertEquals(1L, actualTagData1.get(1).getAccountNo().value());
			assertEquals(11L, actualTagData1.get(1).getPhotoNo().value());
			assertEquals(2L, actualTagData1.get(1).getTagNo().value());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName().value());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName().value());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo().value()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1L, actualData2.getFirst().getAccountNo().value());
			assertEquals(3L, actualData2.getFirst().getPhotoNo().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getCreatedAt().value());
			assertEquals(transactionNow, actualData2.getFirst().getUpdatedAt().value());
			assertFalse(actualData2.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt().value());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC333.jpg", actualData2.getFirst().getImageFilePath().value());
			assertEquals(0L, actualData2.getFirst().getLocationNo().value());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション3", actualData2.getFirst().getCaption().value());
			assertEquals(24, actualData2.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualData2.getFirst().getIso().value());

			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3L);
			assertEquals(0, actualTagData2.size());
		}

		@Test
		@Order(6)
		@DisplayName("異常系：FileDuplicateExceptionをthrowする（写真は複数枚）")
		void savePhotos_FileDuplicateException() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			
			List<PhotoDetailModel> photoDetailModelList = new ArrayList<PhotoDetailModel>();
			// 新規登録1枚目
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC11.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			PhotoDetailModel photoDetailModel1 = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル11"))
					.photoEnglishTitle(new PhotoEnglishTitle("title11"))
					.caption(new Caption("キャプション11"))
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
					.build();
			photoDetailModelList.add(photoDetailModel1);
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(FileDuplicateException.class, () -> photoServiceImpl.savePhotos(accountId, PhotoDetailModelList.of(photoDetailModelList)));
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
		void deletePhotos_photoDeleteModelList_empty() throws UpdateFailureException {
			photoServiceImpl.deletePhotos("aaaaaaaa", PhotoDeleteModelList.empty());
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='aaaaaaaa')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());
			
			assertEquals(10, actualData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：photoDetailModelListが2件以上の場合")
		void deletePhotos_success() throws UpdateFailureException {
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
			photoServiceImpl.deletePhotos("aaaaaaaa", PhotoDeleteModelList.of(photoDeleteModelList));

			List<PhotoMst> actualPhotoMstData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and photo_no in (1, 2)", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());
			assertEquals(2, actualPhotoMstData.size());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMstData.get(0).getCreatedAt().value());
			assertEquals(transactionNow, actualPhotoMstData.get(0).getUpdatedAt().value());
			assertTrue(actualPhotoMstData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMstData.get(1).getCreatedAt().value());
			assertEquals(transactionNow, actualPhotoMstData.get(1).getUpdatedAt().value());
			assertTrue(actualPhotoMstData.get(1).getIsDeleted().value());
			
			List<PhotoMst> actualPhotoMstRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and is_deleted=false", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());
			assertEquals(8, actualPhotoMstRestData.size());
			
			List<PhotoFavorite> actualPhotoFavoriteData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite where favorite_photo_account_no=1 and favorite_photo_no in (1, 2)", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.favoritePhotoAccountNo(new AccountNo(rs.getLong("favorite_photo_account_no")))
							.favoritePhotoNo(new PhotoNo(rs.getLong("favorite_photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.build());
			assertEquals(0, actualPhotoFavoriteData.size());
			
			List<PhotoFavorite> actualPhotoFavoriteRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.favoritePhotoAccountNo(new AccountNo(rs.getLong("favorite_photo_account_no")))
							.favoritePhotoNo(new PhotoNo(rs.getLong("favorite_photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.build());
			assertEquals(4, actualPhotoFavoriteRestData.size());
			
			List<PhotoTagMst> actualPhotoTagData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no in (1,2)", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.tagNo(new TagNo(rs.getLong("tag_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
							.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
							.build());
			assertEquals(0, actualPhotoTagData.size());
			
			List<PhotoTagMst> actualPhotoTagRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.tagNo(new TagNo(rs.getLong("tag_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.tagJapaneseName(new TagJapaneseName(rs.getObject("tag_japanese_name").toString()))
							.tagEnglishName(new TagEnglishName(rs.getObject("tag_english_name").toString()))
							.build());
			assertEquals(2, actualPhotoTagRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhotos_UpdateFailureException() throws UpdateFailureException {
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(99L))
					.imageFilePath(new ImageFilePath("DSC99.jpg"))
					.build());
			
			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.deletePhotos("aaaaaaaa", PhotoDeleteModelList.of(photoDeleteModelList)));
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
		@DisplayName("正常系：アカウント番号がnullの場合")
		void isReachedUpperLimit_accountNo_is_null() {
			assertTrue(photoServiceImpl.isReachedUpperLimit(null));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：mini-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_mini_user_reached() {
			Long accountNo = 1L;
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：mini-userで、上限まで未登録の場合")
		void isReachedUpperLimit_mini_user_not_reached() {
			Long accountNo = 2L;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：normal-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_normal_user_reached() {
			Long accountNo = 3L;
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：normal-userで、上限まで未登録の場合")
		void isReachedUpperLimit_normal_user_not_reached() {
			Long accountNo = 4L;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：special-userの場合")
		void isReachedUpperLimit_special_user() {
			Long accountNo = 5L;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：administratorの場合")
		void isReachedUpperLimit_administrator() {
			Long accountNo = 6L;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
	}
}