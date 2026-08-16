package com.web.gallery.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.json.JsonMapper;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.service.impl.AccountServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AdminAccountRestControllerTest {
	@InjectMocks
	private AdminAccountRestController adminAccountRestController;

	@Mock
	private AccountServiceImpl accountServiceImpl;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		JsonMapper jsonMapper = JsonMapper.builder().build();

		JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

		mockMvc = MockMvcBuilders.standaloneSetup(adminAccountRestController)
				.setMessageConverters(converter)
				.setControllerAdvice(new CommonRestControllerAdvice())
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAdminAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：管理者がアカウント一覧を取得できること")
		void getAdminAccountList_success() throws Exception {
			AccountModelList accountModels = AccountModelList.of(List.of(
					AccountModel.builder()
							.accountNo(new AccountNo(1L))
							.accountId(new AccountId("aaaaaaaa"))
							.accountName(new AccountName("AAAAAAAA"))
							.authorityKbn(AuthorityEnum.ADMINISTRATOR)
							.isDeleted(new IsDeleted(false))
							.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
							.loginFailureCount(new LoginFailureCount(0))
							.build(),
					AccountModel.builder()
							.accountNo(new AccountNo(2L))
							.accountId(new AccountId("bbbbbbbb"))
							.accountName(new AccountName("BBBBBBBB"))
							.authorityKbn(AuthorityEnum.MINI)
							.isDeleted(new IsDeleted(true))
							.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
							.loginFailureCount(new LoginFailureCount(5))
							.build()
			));

			doReturn(accountModels).when(accountServiceImpl).getAccountListForAdmin();

			mockMvc.perform(get("/api/v1/admin/accounts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].accountNo").value(1))
				.andExpect(jsonPath("$[0].accountId").value("aaaaaaaa"))
				.andExpect(jsonPath("$[0].accountName").value("AAAAAAAA"))
				.andExpect(jsonPath("$[0].authorityKbn").value("administrator"))
				.andExpect(jsonPath("$[0].isDeleted").value(false))
				.andExpect(jsonPath("$[0].loginFailureCount").value(0))
				.andExpect(jsonPath("$[1].accountNo").value(2))
				.andExpect(jsonPath("$[1].accountId").value("bbbbbbbb"))
				.andExpect(jsonPath("$[1].isDeleted").value(true))
				.andExpect(jsonPath("$[1].loginFailureCount").value(5));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件の場合は空リストを返すこと")
		void getAdminAccountList_empty() throws Exception {
			doReturn(AccountModelList.empty()).when(accountServiceImpl).getAccountListForAdmin();

			mockMvc.perform(get("/api/v1/admin/accounts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class unlockAccountTest {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントのロックを解除できること")
		void unlockAccount_success() throws Exception {
			doNothing().when(accountServiceImpl).unlockAccount(1L);

			mockMvc.perform(put("/api/v1/admin/accounts/1/unlock"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(MessageConst.UNLOCK_ACCOUNT));

			verify(accountServiceImpl, times(1)).unlockAccount(1L);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionが発生した場合は409を返すこと")
		void unlockAccount_updateFailure() throws Exception {
			doThrow(UpdateFailureException.class).when(accountServiceImpl).unlockAccount(999L);

			mockMvc.perform(put("/api/v1/admin/accounts/999/unlock"))
				.andExpect(status().isConflict());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class lockAccountTest {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを強制ロックできること")
		void lockAccount_success() throws Exception {
			doNothing().when(accountServiceImpl).lockAccount(1L);

			mockMvc.perform(put("/api/v1/admin/accounts/1/lock"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(MessageConst.LOCK_ACCOUNT));

			verify(accountServiceImpl, times(1)).lockAccount(1L);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionが発生した場合は409を返すこと")
		void lockAccount_updateFailure() throws Exception {
			doThrow(UpdateFailureException.class).when(accountServiceImpl).lockAccount(999L);

			mockMvc.perform(put("/api/v1/admin/accounts/999/lock"))
				.andExpect(status().isConflict());
		}
	}
}
