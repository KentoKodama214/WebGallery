package com.web.gallary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.web.gallary.config.PhotoConfig;
import com.web.gallary.controller.request.PhotoTagRequest;
import com.web.gallary.entity.Account;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.service.impl.AccountServiceImpl;
import com.web.gallary.service.impl.PhotoServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoControllerTest {
	@InjectMocks
	private PhotoController photoController;

	@Mock
	private PhotoServiceImpl photoServiceImpl;

	@Mock
	private AccountServiceImpl accountServiceImpl;

	@Mock
	private SessionHelper sessionHelper;

	@Mock
	private PhotoConfig photoConfig;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(photoController)
				.setControllerAdvice(new CommonControllerAdvice(sessionHelper))
				.setViewResolvers(viewResolver)
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class photoList {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void photoList_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			doReturn(null).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(null);
			doReturn(null).when(sessionHelper).getAccountId();
			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_list"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(model().attribute("accountName", accountName))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザー（ページ所有者でない）の場合")
		void photoList_login_user_not_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "bbbbbbbb";
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(1);
			doReturn(loginAccountId).when(sessionHelper).getAccountId();
			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_list"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(model().attribute("accountName", accountName))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + loginAccountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + loginAccountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + loginAccountId + "/photos"));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：ログインユーザー（ページ所有者）で、登録枚数の上限に達していないの場合")
		void photoList_login_user_owner_not_ReachedUpperLimit() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "aaaaaaaa";
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(false).when(photoServiceImpl).isReachedUpperLimit(1);
			doReturn(loginAccountId).when(sessionHelper).getAccountId();
			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_list"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(model().attribute("isAbleToAddPhoto", true))
				.andExpect(model().attribute("accountName", accountName));
		}

		@Test
		@Order(4)
		@DisplayName("正常系：ログインユーザー（ページ所有者）で、登録枚数の上限に達している場合")
		void photoList_login_user_owner_ReachedUpperLimit() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(true).when(photoServiceImpl).isReachedUpperLimit(1);
			doReturn(loginAccountId).when(sessionHelper).getAccountId();
			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_list"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_list"))
				.andExpect(model().attribute("ownerId", photoAccountId))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(model().attribute("isAbleToAddPhoto", false))
				.andExpect(model().attribute("accountName", accountName));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class photoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void photoDetail_not_login_user() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath("https://localhost:8080/image/DSC111.jpg")
					.build();

			doReturn(null).when(sessionHelper).getAccountNo();
			doReturn(null).when(sessionHelper).getAccountId();

			ArgumentCaptor<PhotoDetailGetModel> photoDetailGetModelCaptor = ArgumentCaptor.forClass(PhotoDetailGetModel.class);
			doReturn(photoDetailModel).when(photoServiceImpl).getPhotoDetail(photoDetailGetModelCaptor.capture());

			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_detail"))
				.andExpect(model().attribute("PhotoSettingRequest", photoDetailModel))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("accountName", accountName))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + photoAccountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + photoAccountId + "/photo_detail"));

			PhotoDetailGetModel photoDetailGetModel = photoDetailGetModelCaptor.getValue();
			assertNull(photoDetailGetModel.getAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoNo());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザー（ページ所有者でない）の場合")
		void photoDetail_login_user_not_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "bbbbbbbb";

			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath("https://localhost:8080/image/DSC111.jpg")
					.build();

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(loginAccountId).when(sessionHelper).getAccountId();

			ArgumentCaptor<PhotoDetailGetModel> photoDetailGetModelCaptor = ArgumentCaptor.forClass(PhotoDetailGetModel.class);
			doReturn(photoDetailModel).when(photoServiceImpl).getPhotoDetail(photoDetailGetModelCaptor.capture());

			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_detail"))
				.andExpect(model().attribute("PhotoSettingRequest", photoDetailModel))
				.andExpect(model().attribute("isOwner", false))
				.andExpect(model().attribute("accountName", accountName))
				.andExpect(model().attribute("account_setting_url", "/" + loginAccountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + loginAccountId + "/photo_list"));

			PhotoDetailGetModel photoDetailGetModel = photoDetailGetModelCaptor.getValue();
			assertEquals(1, photoDetailGetModel.getAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoNo());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：ログインユーザー（ページ所有者）の場合")
		void photoDetail_login_user_owner() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "aaaaaaaa";

			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath("https://localhost:8080/image/DSC111.jpg")
					.build();

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(loginAccountId).when(sessionHelper).getAccountId();

			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			ArgumentCaptor<PhotoDetailGetModel> photoDetailGetModelCaptor = ArgumentCaptor.forClass(PhotoDetailGetModel.class);
			doReturn(photoDetailModel).when(photoServiceImpl).getPhotoDetail(photoDetailGetModelCaptor.capture());

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_detail"))
				.andExpect(model().attribute("PhotoSettingRequest", photoDetailModel))
				.andExpect(model().attribute("isOwner", true))
				.andExpect(model().attribute("accountName", accountName));

			PhotoDetailGetModel photoDetailGetModel = photoDetailGetModelCaptor.getValue();
			assertEquals(1, photoDetailGetModel.getAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoNo());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void photoDetail_PhotoNotFoundException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";
			String loginAccountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(loginAccountId).when(sessionHelper).getAccountId();

			Account account = Account.builder().accountId(photoAccountId).accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);

			ArgumentCaptor<PhotoDetailGetModel> photoDetailGetModelCaptor = ArgumentCaptor.forClass(PhotoDetailGetModel.class);
			doThrow(com.web.gallary.exception.PhotoNotFoundException.class).when(photoServiceImpl).getPhotoDetail(photoDetailGetModelCaptor.capture());

			mockMvc.perform(get("/photo/" + photoAccountId + "/photo_detail")
					.param("accountNo", "1")
					.param("photoNo", "1"))
				.andExpect(status().isBadRequest());

			PhotoDetailGetModel photoDetailGetModel = photoDetailGetModelCaptor.getValue();
			assertEquals(1, photoDetailGetModel.getAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoAccountNo());
			assertEquals(1, photoDetailGetModel.getPhotoNo());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class photoSetting {
		private List<PhotoTagRequest> createPhotoTagRequestList() {
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
			photoTagRequest2.setTagJapaneseName("海");
			photoTagRequest2.setTagEnglishName("sea");
			photoTagRequestList.add(photoTagRequest2);

			PhotoTagRequest photoTagRequest3 = new PhotoTagRequest();
			photoTagRequest3.setAccountNo(1);
			photoTagRequest3.setPhotoNo(1);
			photoTagRequest3.setTagNo(3);
			photoTagRequest3.setTagJapaneseName("空");
			photoTagRequest3.setTagEnglishName("sky");
			photoTagRequestList.add(photoTagRequest3);

			return photoTagRequestList;
		}

		@Test
		@Order(1)
		@DisplayName("正常系：新規登録（写真タグが未登録）")
		void photoSetting_addPhoto() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			doReturn(accountId).when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(5).when(photoConfig).getMaxFileSizeMb();

			Account account = Account.builder().accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(accountId);

			mockMvc.perform(get("/photo/" + accountId + "/photo_setting"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_setting"))
				.andExpect(model().attribute("maxFileSizeMb", 5))
				.andExpect(model().attribute("isAddMode", true))
				.andExpect(model().attribute("accountName", accountName))
				.andExpect(model().attribute("account_list_url", "/account_list"))
				.andExpect(model().attribute("photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attribute("photo_detail_url", "/photo/" + accountId + "/photo_detail"))
				.andExpect(model().attribute("account_setting_url", "/" + accountId + "/account_setting"))
				.andExpect(model().attribute("my_photo_list_url", "/photo/" + accountId + "/photo_list"))
				.andExpect(model().attribute("photo_setting_url", "/photo/" + accountId + "/photo_setting"))
				.andExpect(model().attribute("photo_delete_url", "/api/v1/accounts/" + accountId + "/photos"))
				.andExpect(model().attribute("photo_save_url", "/api/v1/accounts/" + accountId + "/photos"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：更新（写真タグが登録済み）")
		void photoSetting_updatePhoto_with_photoTag() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			doReturn(accountId).when(sessionHelper).getAccountId();
			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(5).when(photoConfig).getMaxFileSizeMb();

			Account account = Account.builder().accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(accountId);

			mockMvc.perform(get("/photo/" + accountId + "/photo_setting")
					.param("accountNo", "1")
					.param("photoNo", "1")
					.param("photoTagRequestList[0].accountNo", "1")
					.param("photoTagRequestList[0].photoNo", "1")
					.param("photoTagRequestList[0].tagNo", "1")
					.param("photoTagRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRequestList[0].tagEnglishName", "sun")
					.param("photoTagRequestList[1].accountNo", "1")
					.param("photoTagRequestList[1].photoNo", "1")
					.param("photoTagRequestList[1].tagNo", "2")
					.param("photoTagRequestList[1].tagJapaneseName", "海")
					.param("photoTagRequestList[1].tagEnglishName", "sea")
					.param("photoTagRequestList[2].accountNo", "1")
					.param("photoTagRequestList[2].photoNo", "1")
					.param("photoTagRequestList[2].tagNo", "3")
					.param("photoTagRequestList[2].tagJapaneseName", "空")
					.param("photoTagRequestList[2].tagEnglishName", "sky"))
				.andExpect(status().isOk())
				.andExpect(view().name("photo_setting"))
				.andExpect(model().attribute("maxFileSizeMb", 5))
				.andExpect(model().attribute("isAddMode", false))
				.andExpect(model().attribute("maxTagNo", 3))
				.andExpect(model().attribute("accountName", accountName));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：不正アクセスの場合")
		void photoSetting_ForbiddenAccountException() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/photo/" + accountId + "/photo_setting")
					.param("accountNo", "1")
					.param("photoNo", "1"))
				.andExpect(status().isForbidden());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(photoConfig, times(0)).getMaxFileSizeMb();
			verify(accountServiceImpl, times(0)).getAccountById(any(String.class));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getModelAndView {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void getModelAndView_not_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getModelAndView = PhotoController.class.getDeclaredMethod("getModelAndView", String.class, String.class);
			getModelAndView.setAccessible(true);

			String viewName = "photo_setting";
			String photoAccountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			Account account = Account.builder().accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);
			doReturn(null).when(sessionHelper).getAccountId();

			ModelAndView actual = (ModelAndView) getModelAndView.invoke(photoController, viewName, photoAccountId);
			assertEquals(viewName, actual.getViewName());
			Map<String, Object> models = actual.getModel();
			assertEquals(4, models.size());
			assertEquals(accountName, models.get("accountName").toString());
			assertEquals("/account_list", models.get("account_list_url").toString());
			assertEquals("/photo/" + photoAccountId + "/photo_list", models.get("photo_list_url").toString());
			assertEquals("/photo/" + photoAccountId + "/photo_detail", models.get("photo_detail_url").toString());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void getModelAndView_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method getModelAndView = PhotoController.class.getDeclaredMethod("getModelAndView", String.class, String.class);
			getModelAndView.setAccessible(true);

			String viewName = "photo_setting";
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "bbbbbbbb";
			String accountName = "AAAAAAAA";

			Account account = Account.builder().accountName(accountName).build();
			doReturn(account).when(accountServiceImpl).getAccountById(photoAccountId);
			doReturn(loginAccountId).when(sessionHelper).getAccountId();

			ModelAndView actual = (ModelAndView) getModelAndView.invoke(photoController, viewName, photoAccountId);
			assertEquals(viewName, actual.getViewName());
			Map<String, Object> models = actual.getModel();
			assertEquals(9, models.size());
			assertEquals(accountName, models.get("accountName").toString());
			assertEquals("/account_list", models.get("account_list_url").toString());
			assertEquals("/photo/" + photoAccountId + "/photo_list", models.get("photo_list_url").toString());
			assertEquals("/photo/" + photoAccountId + "/photo_detail", models.get("photo_detail_url").toString());

			assertEquals("/" + loginAccountId + "/account_setting", models.get("account_setting_url").toString());
			assertEquals("/photo/" + loginAccountId + "/photo_list", models.get("my_photo_list_url").toString());
			assertEquals("/photo/" + loginAccountId + "/photo_setting", models.get("photo_setting_url").toString());
			assertEquals("/api/v1/accounts/" + loginAccountId + "/photos", models.get("photo_delete_url").toString());
			assertEquals("/api/v1/accounts/" + loginAccountId + "/photos", models.get("photo_save_url").toString());
		}
	}
}
