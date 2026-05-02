package com.web.gallary.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.web.gallary.entity.Account;
import com.web.gallary.helper.KbnHelper;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.AccountModel;
import com.web.gallary.model.KbnMstModel;
import com.web.gallary.service.impl.AccountServiceImpl;
import com.web.gallary.service.impl.KbnMstServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {
	@InjectMocks
	private AccountController accountController;

	@Mock
	private AccountServiceImpl accountServiceImpl;

	@Mock
	private KbnMstServiceImpl kbnMstServiceImpl;

	@Mock
	private SessionHelper sessionHelper;

	@Mock
	private KbnHelper kbnHepler;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(accountController)
				.setControllerAdvice(new CommonControllerAdvice(sessionHelper))
				.setViewResolvers(viewResolver)
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class register {
		private List<KbnMstModel> createPrefectureList() {
			List<KbnMstModel> prefectureList = new ArrayList<KbnMstModel>();
			prefectureList.add(KbnMstModel.builder()
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
					.explanation("北海道はでっかいどう")
					.build());
			prefectureList.add(KbnMstModel.builder()
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
					.explanation("沖縄は南国")
					.build());

			return prefectureList;
		}

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
					.explanation("北海道はでっかいどう")
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
					.explanation("青森は本州最北")
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
					.explanation("鹿児島は九州最南")
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
					.explanation("沖縄は南国")
					.build());
			kbnMstLinkedHashMap.put("九州・沖縄", kyushu_okinawa);

			return kbnMstLinkedHashMap;
		}

		@Test
		@Order(1)
		@DisplayName("正常系")
		void register_success() throws Exception {
			List<KbnMstModel> prefectureList = createPrefectureList();
			doReturn(prefectureList).when(kbnMstServiceImpl).getPrefectureList();

			Map<String, List<KbnMstModel>> prefectureMap = createLinkedHashMap();
			doReturn(prefectureMap).when(kbnHepler).convertToLinkedHashMap(prefectureList);

			mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("account_register"))
				.andExpect(model().attribute("prefectureGroupList", prefectureMap));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class account_setting {
		private List<KbnMstModel> createPrefectureList() {
			List<KbnMstModel> prefectureList = new ArrayList<KbnMstModel>();
			prefectureList.add(KbnMstModel.builder()
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
					.explanation("北海道はでっかいどう")
					.build());
			prefectureList.add(KbnMstModel.builder()
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
					.explanation("沖縄は南国")
					.build());

			return prefectureList;
		}

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
					.explanation("北海道はでっかいどう")
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
					.explanation("青森は本州最北")
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
					.explanation("鹿児島は九州最南")
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
					.explanation("沖縄は南国")
					.build());
			kbnMstLinkedHashMap.put("九州・沖縄", kyushu_okinawa);

			return kbnMstLinkedHashMap;
		}

		@Test
		@Order(1)
		@DisplayName("正常系：生年月日が1900/01/01の場合")
		void account_setting_birthdate_is_19000101() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			Account account = Account.builder()
					.accountId(accountId)
					.birthdate(LocalDate.of(1900,1,1))
					.build();

			doReturn(account).when(accountServiceImpl).getAccountById(accountId);

			List<KbnMstModel> prefectureList = createPrefectureList();
			doReturn(prefectureList).when(kbnMstServiceImpl).getPrefectureList();

			Map<String, List<KbnMstModel>> prefectureMap = createLinkedHashMap();
			doReturn(prefectureMap).when(kbnHepler).convertToLinkedHashMap(prefectureList);

			mockMvc.perform(get("/" + accountId + "/account_setting"))
				.andExpect(status().isOk())
				.andExpect(view().name("account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attribute("AccountSettingRequest", account))
				.andExpect(model().attribute("prefectureGroupList", prefectureMap));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：生年月日が1900/01/01以外の場合")
		void account_setting_birthdate_is_not_19000101() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			Account account = Account.builder()
					.accountId(accountId)
					.birthdate(LocalDate.of(2000,1,1))
					.build();
			doReturn(account).when(accountServiceImpl).getAccountById(accountId);

			List<KbnMstModel> prefectureList = createPrefectureList();
			doReturn(prefectureList).when(kbnMstServiceImpl).getPrefectureList();

			Map<String, List<KbnMstModel>> prefectureMap = createLinkedHashMap();
			doReturn(prefectureMap).when(kbnHepler).convertToLinkedHashMap(prefectureList);

			mockMvc.perform(get("/" + accountId + "/account_setting"))
				.andExpect(status().isOk())
				.andExpect(view().name("account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attribute("AccountSettingRequest", account))
				.andExpect(model().attribute("prefectureGroupList", prefectureMap));
		}

		@Test
		@Order(3)
		@SuppressWarnings("unchecked")
		@DisplayName("異常系：ForbiddenAccountExceptionをthrowする")
		void account_setting_ForbiddenAccountException() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/" + accountId + "/account_setting"))
				.andExpect(status().isForbidden());

			verify(accountServiceImpl, times(0)).getAccountById(any(String.class));
			verify(kbnMstServiceImpl, times(0)).getPrefectureList();
			verify(kbnHepler, times(0)).convertToLinkedHashMap(any(List.class));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class account_list {
		@Test
		@Order(1)
		@DisplayName("正常系：ログインユーザー")
		void account_list_success_login_user() throws Exception {
			List<AccountModel> accountModelList = new ArrayList<AccountModel>();
			accountModelList.add(AccountModel.builder().accountNo(1).build());
			accountModelList.add(AccountModel.builder().accountNo(2).build());
			accountModelList.add(AccountModel.builder().accountNo(3).build());

			doReturn(accountModelList).when(accountServiceImpl).getAccountList();

			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/account_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("account_list"))
				.andExpect(model().attribute("AccountList", accountModelList))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：非ログインユーザー")
		void account_list_success_not_login_user() throws Exception {
			List<AccountModel> accountModelList = new ArrayList<AccountModel>();
			accountModelList.add(AccountModel.builder().accountNo(1).build());
			accountModelList.add(AccountModel.builder().accountNo(2).build());
			accountModelList.add(AccountModel.builder().accountNo(3).build());

			doReturn(accountModelList).when(accountServiceImpl).getAccountList();
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/account_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("account_list"))
				.andExpect(model().attribute("AccountList", accountModelList))
				.andExpect(model().attributeDoesNotExist("my_photo_list_url"));
		}
	}
}
