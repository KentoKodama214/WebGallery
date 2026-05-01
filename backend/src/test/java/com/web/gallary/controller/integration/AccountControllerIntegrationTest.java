package com.web.gallary.controller.integration;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;
import com.web.gallary.model.AccountModel;
import com.web.gallary.model.KbnMstModel;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AccountControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountControllerIntegrationTest.sql")
	class register {
		private Map<String, List<KbnMstModel>> createLinkedHashMap() {
			LinkedHashMap<String, List<KbnMstModel>> kbnMstLinkedHashMap = new LinkedHashMap<String, List<KbnMstModel>>();
			
			List<KbnMstModel> hokkaido_tohoku = new ArrayList<KbnMstModel>();
			hokkaido_tohoku.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Hokkaido")
					.sortOrder(1)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("北海道")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Hokkaido")
					.explanation("")
					.build());
			hokkaido_tohoku.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Aomori")
					.sortOrder(2)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("青森")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Aomori")
					.explanation("")
					.build());
			kbnMstLinkedHashMap.put("北海道・東北", hokkaido_tohoku);
			
			List<KbnMstModel> kyushu_okinawa = new ArrayList<KbnMstModel>();
			kyushu_okinawa.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagoshima")
					.sortOrder(46)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("鹿児島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Kagoshima")
					.explanation("")
					.build());
			kyushu_okinawa.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Okinawa")
					.sortOrder(47)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("沖縄")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Okinawa")
					.explanation("")
					.build());
			kbnMstLinkedHashMap.put("九州・沖縄", kyushu_okinawa);
			
			return kbnMstLinkedHashMap;
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系")
		void register_success() throws Exception {
			Map<String, List<KbnMstModel>> expected = createLinkedHashMap();
			mockMvc.perform(
					get("/register")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("prefectureGroupList"))
				.andExpect(model().attribute("prefectureGroupList", expected))
				.andExpect(view().name("account_register"));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountControllerIntegrationTest.sql")
	class account_setting {
		private Map<String, List<KbnMstModel>> createLinkedHashMap() {
			LinkedHashMap<String, List<KbnMstModel>> kbnMstLinkedHashMap = new LinkedHashMap<String, List<KbnMstModel>>();
			
			List<KbnMstModel> hokkaido_tohoku = new ArrayList<KbnMstModel>();
			hokkaido_tohoku.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Hokkaido")
					.sortOrder(1)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("北海道")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Hokkaido")
					.explanation("")
					.build());
			hokkaido_tohoku.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Aomori")
					.sortOrder(2)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("青森")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Aomori")
					.explanation("")
					.build());
			kbnMstLinkedHashMap.put("北海道・東北", hokkaido_tohoku);
			
			List<KbnMstModel> kyushu_okinawa = new ArrayList<KbnMstModel>();
			kyushu_okinawa.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagoshima")
					.sortOrder(46)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("鹿児島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Kagoshima")
					.explanation("")
					.build());
			kyushu_okinawa.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Okinawa")
					.sortOrder(47)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("沖縄")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Okinawa")
					.explanation("")
					.build());
			kbnMstLinkedHashMap.put("九州・沖縄", kyushu_okinawa);
			
			return kbnMstLinkedHashMap;
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：生年月日が1900/01/01の場合")
		void account_setting_birthdate_is_19000101() throws Exception {
			String accountId = "bbbbbbbb";
			Map<String, List<KbnMstModel>> expected = createLinkedHashMap();
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.createdBy(2)
					.createdAt(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(2)
					.updatedAt(OffsetDateTime.of(2001, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId(accountId)
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(null)
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/" + accountId + "/account_setting")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
			.andExpect(status().isOk())
			.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
			.andExpect(model().attributeExists("AccountSettingRequest"))
			.andExpect(model().attribute("AccountSettingRequest", instanceOf(Account.class)))
			.andExpect(model().attribute("AccountSettingRequest", sessionAccount))
			.andExpect(model().attributeExists("prefectureGroupList"))
			.andExpect(model().attribute("prefectureGroupList", expected))
			.andExpect(view().name("account_setting"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：生年月日が1900/01/01以外の場合")
		void account_setting_birthdate_is_not_19000101() throws Exception {
			String accountId = "aaaaaaaa";
			Map<String, List<KbnMstModel>> expected = createLinkedHashMap();
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId(accountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/" + accountId + "/account_setting")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attributeExists("AccountSettingRequest"))
				.andExpect(model().attribute("AccountSettingRequest", instanceOf(Account.class)))
				.andExpect(model().attribute("AccountSettingRequest", sessionAccount))
				.andExpect(model().attributeExists("prefectureGroupList"))
				.andExpect(model().attribute("prefectureGroupList", expected))
				.andExpect(view().name("account_setting"));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：不正アクセス")
		void account_setting_ForbiddenAccountException() throws Exception {
			String accountId = "aaaaaaaa";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.createdBy(2)
					.createdAt(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(2)
					.updatedAt(OffsetDateTime.of(2001, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/" + accountId + "/account_setting")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden());
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountControllerIntegrationTest.sql")
	class account_list {
		@Test
		@Order(1)
		@DisplayName("正常系：ログインユーザー")
		void account_list_success_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			List<AccountModel> expected = new ArrayList<AccountModel>();
			expected.add(AccountModel.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			expected.add(AccountModel.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			expected.add(AccountModel.builder()
					.accountNo(3)
					.accountId("cccccccc")
					.accountName("CCCCCCCC")
					.password("$2a$10$password3")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(accountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/account_list")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attributeExists("AccountList"))
				.andExpect(model().attribute("AccountList", expected))
				.andExpect(view().name("account_list"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：非ログインユーザー")
		void account_list_success_not_login_user() throws Exception {
			List<AccountModel> expected = new ArrayList<AccountModel>();
			expected.add(AccountModel.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			expected.add(AccountModel.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			expected.add(AccountModel.builder()
					.accountNo(3)
					.accountId("cccccccc")
					.accountName("CCCCCCCC")
					.password("$2a$10$password3")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build());
			
			mockMvc.perform(
					get("/account_list")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("my_photo_list_url"))
				.andExpect(model().attributeExists("AccountList"))
				.andExpect(model().attribute("AccountList", expected))
				.andExpect(view().name("account_list"));
		}
	}
}