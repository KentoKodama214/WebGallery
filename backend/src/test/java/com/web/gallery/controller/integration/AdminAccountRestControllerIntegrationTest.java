package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.model.AccountModel;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AdminAccountRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Authentication createAdminAuthentication() {
		AccountModel sessionAccount = AccountModel.builder()
				.accountNo(new AccountNo(1L))
				.accountId(new AccountId("aaaaaaaa"))
				.accountName(new AccountName("AAAAAAAA"))
				.password(new Password("$2a$10$password1"))
				.authorityKbn(AuthorityEnum.ADMINISTRATOR)
				.build();
		AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
		return new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
	}

	private Authentication createNonAdminAuthentication() {
		AccountModel sessionAccount = AccountModel.builder()
				.accountNo(new AccountNo(2L))
				.accountId(new AccountId("bbbbbbbb"))
				.accountName(new AccountName("BBBBBBBB"))
				.password(new Password("$2a$10$password2"))
				.authorityKbn(AuthorityEnum.MINI)
				.build();
		AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
		return new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AdminAccountRestControllerIntegrationTest.sql")
	class getAdminAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：管理者が全アカウント一覧を取得できる（削除済み含む）")
		void getAdminAccountList_success() throws Exception {
			mockMvc.perform(
					get("/api/v1/admin/accounts")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.accountList.length()").value(3))
				.andExpect(jsonPath("$.accountList[0].accountId").value("aaaaaaaa"))
				.andExpect(jsonPath("$.accountList[0].accountName").value("AAAAAAAA"))
				.andExpect(jsonPath("$.accountList[0].authorityKbn").value("administrator"))
				.andExpect(jsonPath("$.accountList[0].isDeleted").value(false))
				.andExpect(jsonPath("$.accountList[0].loginFailureCount").value(0))
				.andExpect(jsonPath("$.accountList[1].accountId").value("bbbbbbbb"))
				.andExpect(jsonPath("$.accountList[1].isDeleted").value(false))
				.andExpect(jsonPath("$.accountList[1].loginFailureCount").value(10))
				.andExpect(jsonPath("$.accountList[2].accountId").value("cccccccc"))
				.andExpect(jsonPath("$.accountList[2].isDeleted").value(true));
		}

		@Test
		@Order(2)
		@DisplayName("異常系：管理者以外は403を返す")
		void getAdminAccountList_forbidden() throws Exception {
			mockMvc.perform(
					get("/api/v1/admin/accounts")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createNonAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorMessage()));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AdminAccountRestControllerIntegrationTest.sql")
	class unlockAccountTest {
		@Test
		@Order(1)
		@DisplayName("正常系：ロックされたアカウントを解除できる")
		void unlockAccount_success() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/2/unlock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(MessageConst.UNLOCK_ACCOUNT));

			Integer loginFailureCount = jdbcTemplate.queryForObject(
					"SELECT login_failure_count FROM common.account WHERE account_no = 2",
					Integer.class);
			assertEquals(0, loginFailureCount);
			Boolean isAdminLocked = jdbcTemplate.queryForObject(
					"SELECT is_admin_locked FROM common.account WHERE account_no = 2",
					Boolean.class);
			assertFalse(isAdminLocked);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：管理者以外は403を返す")
		void unlockAccount_forbidden() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/2/unlock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createNonAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isForbidden());

			Integer loginFailureCount = jdbcTemplate.queryForObject(
					"SELECT login_failure_count FROM common.account WHERE account_no = 2",
					Integer.class);
			assertEquals(10, loginFailureCount);
		}

		@Test
		@Order(3)
		@DisplayName("異常系：存在しないアカウント番号の場合は409を返す")
		void unlockAccount_notFound() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/999/unlock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isConflict());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AdminAccountRestControllerIntegrationTest.sql")
	class lockAccountTest {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを強制ロックできる")
		void lockAccount_success() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/1/lock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(MessageConst.LOCK_ACCOUNT));

			Boolean isAdminLocked = jdbcTemplate.queryForObject(
					"SELECT is_admin_locked FROM common.account WHERE account_no = 1",
					Boolean.class);
			assertTrue(isAdminLocked);
			Integer loginFailureCount = jdbcTemplate.queryForObject(
					"SELECT login_failure_count FROM common.account WHERE account_no = 1",
					Integer.class);
			assertTrue(loginFailureCount > 0);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：管理者以外は403を返す")
		void lockAccount_forbidden() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/1/lock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createNonAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isForbidden());

			Integer loginFailureCount = jdbcTemplate.queryForObject(
					"SELECT login_failure_count FROM common.account WHERE account_no = 1",
					Integer.class);
			assertEquals(0, loginFailureCount);
		}

		@Test
		@Order(3)
		@DisplayName("異常系：存在しないアカウント番号の場合は409を返す")
		void lockAccount_notFound() throws Exception {
			mockMvc.perform(
					put("/api/v1/admin/accounts/999/lock")
					.with(SecurityMockMvcRequestPostProcessors.authentication(createAdminAuthentication()))
					.with(csrf())
				)
				.andExpect(status().isConflict());
		}
	}
}
