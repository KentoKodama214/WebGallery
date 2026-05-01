package com.web.gallary.service.impl.integration;

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

import com.web.gallary.entity.PhotoFavorite;
import com.web.gallary.entity.PhotoMst;
import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.SortPhotoEnum;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoListGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.service.impl.PhotoServiceImpl;

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
					.accountNo(1)
					.photoAccountId("dddddddd")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			assertEquals(new ArrayList<PhotoModel>(), actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が存在した場合で、撮影日順に並び替え")
		void getPhotoList_sortBy_photoAt() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(9, actual.get(0).getPhotoNo());
			assertEquals(8, actual.get(1).getPhotoNo());
			assertEquals(7, actual.get(2).getPhotoNo());
			assertEquals(6, actual.get(3).getPhotoNo());
			assertEquals(5, actual.get(4).getPhotoNo());
			assertEquals(4, actual.get(5).getPhotoNo());
			assertEquals(10, actual.get(6).getPhotoNo());
			assertEquals(3, actual.get(7).getPhotoNo());
			assertEquals(2, actual.get(8).getPhotoNo());
			assertEquals(1, actual.get(9).getPhotoNo());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(0, actual.get(0).getFavoriteCount());
			assertFalse(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2023, 9, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC19.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション19", actual.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：写真が存在した場合で、お気に入り数順に並び替え")
		void getPhotoList_sortBy_Favorite() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.FAVORITE)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(2, actual.get(0).getPhotoNo());
			assertEquals(1, actual.get(1).getPhotoNo());
			assertEquals(3, actual.get(2).getPhotoNo());
			assertEquals(4, actual.get(3).getPhotoNo());
			assertEquals(5, actual.get(4).getPhotoNo());
			assertEquals(6, actual.get(5).getPhotoNo());
			assertEquals(7, actual.get(6).getPhotoNo());
			assertEquals(8, actual.get(7).getPhotoNo());
			assertEquals(9, actual.get(8).getPhotoNo());
			assertEquals(10, actual.get(9).getPhotoNo());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(4, actual.get(0).getFavoriteCount());
			assertTrue(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション12", actual.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(3, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo() == 1).toList().getFirst();
			assertEquals(1, actualTag.getAccountNo());
			assertEquals(2, actualTag.getPhotoNo());
			assertEquals(1, actualTag.getTagNo());
			assertEquals("太陽", actualTag.getTagJapaneseName());
			assertEquals("sun", actualTag.getTagEnglishName());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：写真が存在した場合で、季節・時期順に並び替え")
		void getPhotoList_sortBy_season() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.SEASON)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(10, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(10, actual.get(0).getPhotoNo());
			assertEquals(9, actual.get(1).getPhotoNo());
			assertEquals(8, actual.get(2).getPhotoNo());
			assertEquals(7, actual.get(3).getPhotoNo());
			assertEquals(6, actual.get(4).getPhotoNo());
			assertEquals(5, actual.get(5).getPhotoNo());
			assertEquals(4, actual.get(6).getPhotoNo());
			assertEquals(3, actual.get(7).getPhotoNo());
			assertEquals(2, actual.get(8).getPhotoNo());
			assertEquals(1, actual.get(9).getPhotoNo());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(0, actual.get(0).getFavoriteCount());
			assertFalse(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 10, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC20.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション20", actual.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：写真が存在した場合で、写真の向きで絞り込み")
		void getPhotoList_filtering_by_directionKbnCode() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.VERTICAL)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(3, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(8, actual.get(0).getPhotoNo());
			assertEquals(7, actual.get(1).getPhotoNo());
			assertEquals(5, actual.get(2).getPhotoNo());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(0, actual.get(0).getFavoriteCount());
			assertFalse(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2023, 8, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC18.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション18", actual.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.get(0).getDirectionKbn());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：写真が存在した場合で、お気に入りで絞り込み")
		void getPhotoList_filtering_by_isFavoriteOnly() {
			List<String> tags = new ArrayList<String>();
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(true)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(2, actual.size());
			
			// List<PhotoModel>の並び順チェック
			assertEquals(2, actual.get(0).getPhotoNo());
			assertEquals(1, actual.get(1).getPhotoNo());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(4, actual.get(0).getFavoriteCount());
			assertTrue(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC12.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション12", actual.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(3, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo() == 1).toList().getFirst();
			assertEquals(1, actualTag.getAccountNo());
			assertEquals(2, actualTag.getPhotoNo());
			assertEquals(1, actualTag.getTagNo());
			assertEquals("太陽", actualTag.getTagJapaneseName());
			assertEquals("sun", actualTag.getTagEnglishName());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真が存在した場合で、写真タグで絞り込み")
		void getPhotoList_filtering_by_tags() {
			List<String> tags = new ArrayList<String>();
			tags.add("太陽");
			tags.add("bluesky");
			
			PhotoListGetModel photoListGetModel = PhotoListGetModel.builder()
					.accountNo(1)
					.photoAccountId("aaaaaaaa")
					.directionKbn(DirectionEnum.NONE)
					.isFavoriteOnly(false)
					.tagList(tags)
					.sortBy(SortPhotoEnum.PHOTO_AT)
					.build();
			
			List<PhotoModel> actual = photoServiceImpl.getPhotoList(photoListGetModel);
			
			// List<PhotoModel>の数チェック
			assertEquals(1, actual.size());
			
			// 抜き取りで、PhotoModelのデータチェック
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(3, actual.get(0).getFavoriteCount());
			assertTrue(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション11", actual.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(0).getDirectionKbn());
			assertEquals(2, actual.get(0).getPhotoTagModelList().size());
			
			// 抜き取りで、PhotoTagModelのデータチェック
			PhotoTagModel actualTag = actual.get(0).getPhotoTagModelList().stream().filter(tag -> tag.getTagNo() == 1).toList().getFirst();
			assertEquals(1, actualTag.getAccountNo());
			assertEquals(1, actualTag.getPhotoNo());
			assertEquals(1, actualTag.getTagNo());
			assertEquals("太陽", actualTag.getTagJapaneseName());
			assertEquals("sun", actualTag.getTagEnglishName());
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
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(1)
					.build();
			
			PhotoDetailModel actual = photoServiceImpl.getPhotoDetail(photoDetailGetModel);
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertTrue(actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(1, actual.getLocationNo());
			assertEquals("住所1", actual.getAddress());
			assertEquals(0, BigDecimal.valueOf(38.100).compareTo(actual.getLatitude()));
			assertEquals(0, BigDecimal.valueOf(115.100).compareTo(actual.getLongitude()));
			assertEquals("ロケーション1", actual.getLocationName());
			assertNull(actual.getImageFile());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", actual.getImageFilePath());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle());
			assertEquals("title11", actual.getPhotoEnglishTitle());
			assertEquals("キャプション11", actual.getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
			assertEquals(24, actual.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed()));
			assertEquals(100, actual.getIso());
			assertEquals(2, actual.getPhotoTagModelList().size());
			
			assertEquals(1, actual.getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals(2, actual.getPhotoTagModelList().get(1).getTagNo());
			assertEquals("青空", actual.getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("bluesky", actual.getPhotoTagModelList().get(1).getTagEnglishName());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(11)
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
					.accountNo(1)
					.photoNo(11)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(11)
					.tagNo(2)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC21.jpg",
					"multipart/form-data",
					"image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(1)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("")
					.photoJapaneseTitle("タイトル21")
					.photoEnglishTitle("title21")
					.caption("キャプション21")
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
					"DSC22.jpg",
					"multipart/form-data",
					"image".getBytes()
				);
			return PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
		}
		
		PhotoDetailModel createUpdatePhotoWithTag() {
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(2)
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
					.accountNo(1)
					.photoNo(2)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC222.jpg")
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
					"DSC13.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			return PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(3)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC333.jpg")
					.photoJapaneseTitle("タイトル3")
					.photoEnglishTitle("title3")
					.caption("キャプション3")
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
		}
		
		List<PhotoMst> getPhotoMstData(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='" + accountId + "')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
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
		
		List<PhotoTagMst> getPhotoTagMst(String accountId, Integer photoNo) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no= (SELECT account_no FROM common.account where account_id='" + accountId + "') and photo_no=" + photoNo , (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：photoDetailModelListがnullの場合、終了")
		void savePhotos_photoDetailModelList_is_null() throws FileDuplicateException, RegistFailureException, UpdateFailureException {
			String accountId = "aaaaaaaa";
			List<PhotoMst> beforeSaveData = getPhotoMstData(accountId);
			photoServiceImpl.savePhotos(accountId, null);
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
			photoServiceImpl.savePhotos(accountId, photoDetailModelList);
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
			
			photoServiceImpl.savePhotos(accountId, photoDetailModelList);
			
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo() > 10).toList();
			assertEquals(2, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(11, actualData.get(0).getPhotoNo());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath());
			assertEquals(0, actualData.get(0).getLocationNo());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション21", actualData.get(0).getCaption());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(100, actualData.get(0).getIso());
			
			assertEquals(1, actualData.get(1).getAccountNo());
			assertEquals(12, actualData.get(1).getPhotoNo());
			assertFalse(actualData.get(1).getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getPhotoAt().plusHours(9));
			assertEquals("https://www.xxx.com/" + accountId + "/DSC22.jpg", actualData.get(1).getImageFilePath());
			assertEquals(0, actualData.get(1).getLocationNo());
			assertEquals("", actualData.get(1).getPhotoJapaneseTitle());
			assertEquals("", actualData.get(1).getPhotoEnglishTitle());
			assertEquals("", actualData.get(1).getCaption());
			assertEquals(0, actualData.get(1).getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.get(1).getShutterSpeed()));
			assertEquals(0, actualData.get(1).getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11);
			assertEquals(2, actualTagData1.size());
			assertEquals(1, actualTagData1.get(0).getAccountNo());
			assertEquals(11, actualTagData1.get(0).getPhotoNo());
			assertEquals(1, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1, actualTagData1.get(1).getAccountNo());
			assertEquals(11, actualTagData1.get(1).getPhotoNo());
			assertEquals(2, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 12);
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
			
			photoServiceImpl.savePhotos(accountId, photoDetailModelList);
			
			List<PhotoMst> actualData1 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==2).toList();
			assertEquals(1, actualData1.size());
			assertEquals(1, actualData1.getFirst().getAccountNo());
			assertEquals(2, actualData1.getFirst().getPhotoNo());
			assertFalse(actualData1.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData1.getFirst().getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC222.jpg", actualData1.getFirst().getImageFilePath());
			assertEquals(0, actualData1.getFirst().getLocationNo());
			assertEquals("タイトル2", actualData1.getFirst().getPhotoJapaneseTitle());
			assertEquals("title2", actualData1.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション2", actualData1.getFirst().getCaption());
			assertEquals(24, actualData1.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData1.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData1.getFirst().getShutterSpeed()));
			assertEquals(100, actualData1.getFirst().getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 2);
			assertEquals(2, actualTagData1.size());
			assertEquals(1, actualTagData1.get(0).getAccountNo());
			assertEquals(2, actualTagData1.get(0).getPhotoNo());
			assertEquals(1, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1, actualTagData1.get(1).getAccountNo());
			assertEquals(2, actualTagData1.get(1).getPhotoNo());
			assertEquals(2, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1, actualData2.getFirst().getAccountNo());
			assertEquals(3, actualData2.getFirst().getPhotoNo());
			assertFalse(actualData2.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC333.jpg", actualData2.getFirst().getImageFilePath());
			assertEquals(0, actualData2.getFirst().getLocationNo());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション3", actualData2.getFirst().getCaption());
			assertEquals(24, actualData2.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed()));
			assertEquals(100, actualData2.getFirst().getIso());
			
			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3);
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
			
			photoServiceImpl.savePhotos(accountId, photoDetailModelList);
			
			List<PhotoMst> actualData = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo() > 10).toList();
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(11, actualData.get(0).getPhotoNo());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC21.jpg", actualData.get(0).getImageFilePath());
			assertEquals(0, actualData.get(0).getLocationNo());
			assertEquals("タイトル21", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title21", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション21", actualData.get(0).getCaption());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(100, actualData.get(0).getIso());
			
			List<PhotoTagMst> actualTagData1 = getPhotoTagMst(accountId, 11);
			assertEquals(2, actualTagData1.size());
			assertEquals(1, actualTagData1.get(0).getAccountNo());
			assertEquals(11, actualTagData1.get(0).getPhotoNo());
			assertEquals(1, actualTagData1.get(0).getTagNo());
			assertEquals("太陽", actualTagData1.get(0).getTagJapaneseName());
			assertEquals("sun", actualTagData1.get(0).getTagEnglishName());
			assertEquals(1, actualTagData1.get(1).getAccountNo());
			assertEquals(11, actualTagData1.get(1).getPhotoNo());
			assertEquals(2, actualTagData1.get(1).getTagNo());
			assertEquals("海", actualTagData1.get(1).getTagJapaneseName());
			assertEquals("sea", actualTagData1.get(1).getTagEnglishName());
			
			List<PhotoMst> actualData2 = getPhotoMstData(accountId).stream().filter(photoMst -> photoMst.getPhotoNo()==3).toList();
			assertEquals(1, actualData2.size());
			assertEquals(1, actualData2.getFirst().getAccountNo());
			assertEquals(3, actualData2.getFirst().getPhotoNo());
			assertFalse(actualData2.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData2.getFirst().getPhotoAt());
			assertEquals("https://www.xxx.com/" + accountId + "/DSC333.jpg", actualData2.getFirst().getImageFilePath());
			assertEquals(0, actualData2.getFirst().getLocationNo());
			assertEquals("タイトル3", actualData2.getFirst().getPhotoJapaneseTitle());
			assertEquals("title3", actualData2.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション3", actualData2.getFirst().getCaption());
			assertEquals(24, actualData2.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData2.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData2.getFirst().getShutterSpeed()));
			assertEquals(100, actualData2.getFirst().getIso());
			
			List<PhotoTagMst> actualTagData2 = getPhotoTagMst(accountId, 3);
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
					.accountNo(1)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.imageFile(multipartFile)
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.photoJapaneseTitle("タイトル11")
					.photoEnglishTitle("title11")
					.caption("キャプション11")
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
			photoDetailModelList.add(photoDetailModel1);
			// 新規登録2枚目
			PhotoDetailModel photoDetailModel2 = createNewPhoto();
			photoDetailModelList.add(photoDetailModel2);
			
			assertThrows(FileDuplicateException.class, () -> photoServiceImpl.savePhotos(accountId, photoDetailModelList));
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
			photoServiceImpl.deletePhotos("aaaaaaaa", new ArrayList<PhotoDeleteModel>());
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = (SELECT account_no FROM common.account where account_id='aaaaaaaa')", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
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
		void deletePhotos_success() throws UpdateFailureException {
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath("DSC11.jpg")
					.build());
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(2)
					.imageFilePath("DSC12.jpg")
					.build());
			
			photoServiceImpl.deletePhotos("aaaaaaaa", photoDeleteModelList);
			
			List<PhotoMst> actualPhotoMstData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and photo_no in (1, 2)", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
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
			assertTrue(actualPhotoMstData.get(0).getIsDeleted());
			assertTrue(actualPhotoMstData.get(1).getIsDeleted());
			
			List<PhotoMst> actualPhotoMstRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no=1 and is_deleted=false", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
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
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(0, actualPhotoFavoriteData.size());
			
			List<PhotoFavorite> actualPhotoFavoriteRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(4, actualPhotoFavoriteRestData.size());
			
			List<PhotoTagMst> actualPhotoTagData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no in (1,2)", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(0, actualPhotoTagData.size());
			
			List<PhotoTagMst> actualPhotoTagRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(2, actualPhotoTagRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhotos_UpdateFailureException() throws UpdateFailureException {
			List<PhotoDeleteModel> photoDeleteModelList = new ArrayList<PhotoDeleteModel>();
			photoDeleteModelList.add(PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(99)
					.imageFilePath("DSC99.jpg")
					.build());
			
			assertThrows(UpdateFailureException.class, () -> photoServiceImpl.deletePhotos("aaaaaaaa", photoDeleteModelList));
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
			Integer accountNo = 1;
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：mini-userで、上限まで未登録の場合")
		void isReachedUpperLimit_mini_user_not_reached() {
			Integer accountNo = 2;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：normal-userで、上限まで登録済みの場合")
		void isReachedUpperLimit_normal_user_reached() {
			Integer accountNo = 3;
			assertTrue(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：normal-userで、上限まで未登録の場合")
		void isReachedUpperLimit_normal_user_not_reached() {
			Integer accountNo = 4;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：special-userの場合")
		void isReachedUpperLimit_special_user() {
			Integer accountNo = 5;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：administratorの場合")
		void isReachedUpperLimit_administrator() {
			Integer accountNo = 6;
			assertFalse(photoServiceImpl.isReachedUpperLimit(accountNo));
		}
	}
}