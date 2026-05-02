package com.web.gallary.controller.integration;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.controller.request.PhotoSettingRequest;
import com.web.gallary.controller.request.PhotoTagRequest;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoTagModel;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PhotoControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
	class photoList {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void photoList_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_list")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(view().name("photo_list"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザー（ページ所有者でない）の場合")
		void photoList_login_user_not_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "eeeeeeee";
			
			Account sessionAccount = Account.builder()
					.accountNo(5)
					.accountId(loginAccountId)
					.accountName("EEEEEEEE")
					.password("$2a$10$password5")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_list")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(view().name("photo_list"));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：ログインユーザー（ページ所有者）で、登録枚数の上限に達していないの場合")
		void photoList_login_user_owner_not_ReachedUpperLimit() throws Exception {
			String photoAccountId = "eeeeeeee";
			String loginAccountId = "eeeeeeee";
			
			Account sessionAccount = Account.builder()
					.accountNo(5)
					.accountId(loginAccountId)
					.accountName("EEEEEEEE")
					.password("$2a$10$password5")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_list")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "EEEEEEEE"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(model().attribute("isAbleToAddPhoto", true))
				.andExpect(view().name("photo_list"));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：ログインユーザー（ページ所有者）で、登録枚数の上限に達している場合")
		void photoList_login_user_owner_ReachedUpperLimit() throws Exception {
			String photoAccountId = "bbbbbbbb";
			String loginAccountId = "bbbbbbbb";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId(loginAccountId)
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_list")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "BBBBBBBB"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(view().name("photo_list"));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
	class photoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合（メタ情報の入力あり）")
		void photoDetail_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(2)
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build());
			
			PhotoDetailModel expected = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.isFavorite(false)
					.photoAt(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.address("住所1")
					.latitude(BigDecimal.valueOf(38.100).setScale(4))
					.longitude(BigDecimal.valueOf(115.100).setScale(4))
					.locationName("ロケーション1")
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.photoJapaneseTitle("タイトル11")
					.photoEnglishTitle("title11")
					.caption("キャプション11")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.focalLength(24)
					.fValue(BigDecimal.valueOf(8.0).setScale(2))
					.shutterSpeed(BigDecimal.valueOf(1).setScale(5))
					.iso(100)
					.photoTagModelList(photoTagModelList)
					.build();

			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attributeExists("PhotoSettingRequest"))
				.andExpect(model().attribute("PhotoSettingRequest", instanceOf(PhotoDetailModel.class)))
				.andExpect(model().attribute("PhotoSettingRequest", expected))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(view().name("photo_detail"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザー（ページ所有者でない）の場合（メタ情報の入力なし）")
		void photoDetail_login_user_not_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "eeeeeeee";
			
			Account sessionAccount = Account.builder()
					.accountNo(5)
					.accountId(loginAccountId)
					.accountName("EEEEEEEE")
					.password("$2a$10$password5")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
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
					.tagJapaneseName("曇天")
					.tagEnglishName("cloudy")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(3)
					.tagJapaneseName("花")
					.tagEnglishName("flower")
					.build());
			
			PhotoDetailModel expected = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(2)
					.isFavorite(false)
					.photoAt(null)
					.locationNo(null)
					.address(null)
					.latitude(null)
					.longitude(null)
					.locationName(null)
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC12.jpg")
					.photoJapaneseTitle("")
					.photoEnglishTitle("")
					.caption("")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.focalLength(null)
					.fValue(null)
					.shutterSpeed(null)
					.iso(null)
					.photoTagModelList(photoTagModelList)
					.build();
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "2")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attributeExists("PhotoSettingRequest"))
				.andExpect(model().attribute("PhotoSettingRequest", instanceOf(PhotoDetailModel.class)))
				.andExpect(model().attribute("PhotoSettingRequest", expected))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(view().name("photo_detail"));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：ログインユーザー（ページ所有者）の場合")
		void photoDetail_login_user_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			List<PhotoTagModel> photoTagModelList = new ArrayList<PhotoTagModel>();
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagModelList.add(PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(2)
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build());
			
			PhotoDetailModel expected = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.isFavorite(true)
					.photoAt(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.address("住所1")
					.latitude(BigDecimal.valueOf(38.100).setScale(4))
					.longitude(BigDecimal.valueOf(115.100).setScale(4))
					.locationName("ロケーション1")
					.imageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.photoJapaneseTitle("タイトル11")
					.photoEnglishTitle("title11")
					.caption("キャプション11")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.focalLength(24)
					.fValue(BigDecimal.valueOf(8.0).setScale(2))
					.shutterSpeed(BigDecimal.valueOf(1).setScale(5))
					.iso(100)
					.photoTagModelList(photoTagModelList)
					.build();
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attributeExists("PhotoSettingRequest"))
				.andExpect(model().attribute("PhotoSettingRequest", instanceOf(PhotoDetailModel.class)))
				.andExpect(model().attribute("PhotoSettingRequest", expected))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(view().name("photo_detail"));
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする（ログインユーザー）")
		void photoDetail_PhotoNotFoundException_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "99")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(model().attribute("httpStatus", HttpStatus.BAD_REQUEST.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.PHOTO_NOT_FOUND.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.PHOTO_NOT_FOUND.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(view().name("error_page"));
		}
		
		@Test
		@Order(5)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする（非ログインユーザー）")
		void photoDetail_PhotoNotFoundException_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "99")
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(model().attribute("httpStatus", HttpStatus.BAD_REQUEST.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.PHOTO_NOT_FOUND.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.PHOTO_NOT_FOUND.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/login"))
				.andExpect(view().name("error_page"));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoControllerIntegrationTest.sql")
	class photoSetting {
		@Test
		@Order(1)
		@DisplayName("正常系：新規登録（写真タグが未登録）")
		void photoSetting_addPhoto() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			PhotoSettingRequest expected = new PhotoSettingRequest();
			expected.setAccountNo(1);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_setting")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attributeExists("PhotoSettingRequest"))
				.andExpect(model().attribute("PhotoSettingRequest", instanceOf(PhotoSettingRequest.class)))
				.andExpect(model().attribute("PhotoSettingRequest", expected))
				.andExpect(model().attribute("maxFileSizeMb", 5))
				.andExpect(model().attribute("isAddMode", true))
				.andExpect(model().attributeDoesNotExist("maxTagNo"))
				.andExpect(view().name("photo_setting"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：更新（写真タグが登録済み）")
		void photoSetting_updatePhoto_with_photoTag() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			List<PhotoTagRequest> photoTagRequestList = new ArrayList<PhotoTagRequest>();
			PhotoTagRequest photoTagRequest1 = new PhotoTagRequest();
			photoTagRequest1.setAccountNo(1);
			photoTagRequest1.setPhotoNo(1);
			photoTagRequest1.setTagNo(1);
			photoTagRequest1.setTagJapaneseName("太陽");
			photoTagRequest1.setTagEnglishName("sun");
			photoTagRequestList.add(photoTagRequest1);

			PhotoTagRequest photoTagRequest2 = new PhotoTagRequest();
			photoTagRequest2.setAccountNo(1);
			photoTagRequest2.setPhotoNo(1);
			photoTagRequest2.setTagNo(2);
			photoTagRequest2.setTagJapaneseName("青空");
			photoTagRequest2.setTagEnglishName("bluesky");
			photoTagRequestList.add(photoTagRequest2);
			
			PhotoSettingRequest expected = new PhotoSettingRequest();
			expected.setAccountNo(1);
			expected.setPhotoNo(1);
			expected.setIsFavorite(false);
			expected.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			expected.setLocationNo(1);
			expected.setAddress("住所1");
			expected.setLatitude(BigDecimal.valueOf(38.100).setScale(3));
			expected.setLongitude(BigDecimal.valueOf(115.100).setScale(3));
			expected.setLocationName("ロケーション1");
			expected.setImageFilePath("https://www.xxx.com/aaaaaaaa/DSC11.jpg");
			expected.setPhotoJapaneseTitle("タイトル11");
			expected.setPhotoEnglishTitle("title11");
			expected.setCaption("キャプション11");
			expected.setDirectionKbn(DirectionEnum.HORIZONTAL);
			expected.setFocalLength(24);
			expected.setFValue(BigDecimal.valueOf(8.0));
			expected.setShutterSpeed(BigDecimal.valueOf(1.0));
			expected.setIso(100);
			expected.setPhotoTagRequestList(photoTagRequestList);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_setting")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("isFavorite", "false")
					.param("photoAt", OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
					.param("locationNo", "1")
					.param("address", "住所1")
					.param("latitude", "38.100")
					.param("longitude", "115.100")
					.param("locationName", "ロケーション1")
					.param("imageFilePath", "https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.param("photoJapaneseTitle", "タイトル11")
					.param("photoEnglishTitle", "title11")
					.param("caption", "キャプション11")
					.param("directionKbn", "HORIZONTAL")
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "1.0")
					.param("iso", "100")
					.param("photoTagRequestList[0].accountNo", "1")
					.param("photoTagRequestList[0].photoNo", "1")
					.param("photoTagRequestList[0].tagNo", "1")
					.param("photoTagRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRequestList[0].tagEnglishName", "sun")
					.param("photoTagRequestList[1].accountNo", "1")
					.param("photoTagRequestList[1].photoNo", "1")
					.param("photoTagRequestList[1].tagNo", "2")
					.param("photoTagRequestList[1].tagJapaneseName", "青空")
					.param("photoTagRequestList[1].tagEnglishName", "bluesky")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("accountName", "AAAAAAAA"))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attributeExists("PhotoSettingRequest"))
				.andExpect(model().attribute("PhotoSettingRequest", instanceOf(PhotoSettingRequest.class)))
				.andExpect(model().attribute("PhotoSettingRequest", expected))
				.andExpect(model().attribute("maxFileSizeMb", 5))
				.andExpect(model().attribute("isAddMode", false))
				.andExpect(model().attribute("maxTagNo", 2))
				.andExpect(view().name("photo_setting"));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：不正アクセスの場合（ログインユーザー）")
		void photoSetting_ForbiddenAccountException_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "xxxxxxxx";
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_setting")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("isFavorite", "false")
					.param("photoAt", OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
					.param("locationNo", "1")
					.param("address", "住所1")
					.param("latitude", "38.100")
					.param("longitude", "115.100")
					.param("locationName", "ロケーション1")
					.param("imageFilePath", "https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.param("photoJapaneseTitle", "タイトル11")
					.param("photoEnglishTitle", "title11")
					.param("caption", "キャプション11")
					.param("directionKbn", "HORIZONTAL")
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "1.0")
					.param("iso", "100")
					.param("photoTagRequestList[0].accountNo", "1")
					.param("photoTagRequestList[0].photoNo", "1")
					.param("photoTagRequestList[0].tagNo", "1")
					.param("photoTagRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRequestList[0].tagEnglishName", "sun")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(model().attribute("httpStatus", HttpStatus.FORBIDDEN.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(view().name("error_page"));
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：不正アクセスの場合（非ログインユーザー）")
		void photoSetting_ForbiddenAccountException_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";

			mockMvc.perform(
					get("/photo/" + photoAccountId + "/photo_setting")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("isFavorite", "false")
					.param("photoAt", OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
					.param("locationNo", "1")
					.param("address", "住所1")
					.param("latitude", "38.100")
					.param("longitude", "115.100")
					.param("locationName", "ロケーション1")
					.param("imageFilePath", "https://www.xxx.com/aaaaaaaa/DSC11.jpg")
					.param("photoJapaneseTitle", "タイトル11")
					.param("photoEnglishTitle", "title11")
					.param("caption", "キャプション11")
					.param("directionKbn", "HORIZONTAL")
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "1.0")
					.param("iso", "100")
					.param("photoTagRequestList[0].accountNo", "1")
					.param("photoTagRequestList[0].photoNo", "1")
					.param("photoTagRequestList[0].tagNo", "1")
					.param("photoTagRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRequestList[0].tagEnglishName", "sun")
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(model().attribute("httpStatus", HttpStatus.FORBIDDEN.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/login"))
				.andExpect(view().name("error_page"));
		}
	}
}