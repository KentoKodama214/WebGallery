package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.model.AccountModel;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PhotoFavoriteControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/integration/PhotoFavoriteControllerIntegrationTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	private Authentication createAuthentication() {
		return createAuthentication(1L, "aaaaaaaa", "$2a$10$password1");
	}

	private Authentication createAuthentication(Long accountNo, String accountId, String password) {
		AccountModel sessionAccount = AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.accountId(new AccountId(accountId))
				.accountName(new AccountName("AAAAAAAA"))
				.password(new Password(password))
				.authorityKbn(AuthorityEnum.ADMINISTRATOR)
				.isDeleted(new IsDeleted(false))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
		AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
		return new UsernamePasswordAuthenticationToken(
				accountPrincipal, null, accountPrincipal.getAuthorities());
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoFavoriteControllerIntegrationTest.sql")
	class addFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void addFavorite_success() throws Exception {
			Authentication authentication = createAuthentication();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			mockMvc.perform(
					post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite_success.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("お気に入りに追加しました。"));

			List<PhotoFavorite> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE account_no=1 and favorite_photo_account_no=2 and favorite_photo_no=1", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getLong("account_no"))
							.favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getLong("favorite_photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(2L, actualData.getFirst().getFavoritePhotoAccountNo());
			assertEquals(1L, actualData.getFirst().getFavoritePhotoNo());
			assertEquals(1L, actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void addFavorite_BadRequestException() throws Exception {
			Authentication authentication = createAuthentication();

			mockMvc.perform(
					post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite_badrequest.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void addFavorite_RegistFailureException() throws Exception {
			// 既に登録済みのお気に入り（account_no=2, favorite_photo_account_no=1, favorite_photo_no=2）を再登録する
			Authentication authentication = createAuthentication(2L, "bbbbbbbb", "$2a$10$password2");

			mockMvc.perform(
					post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite_regist_failure.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_REGIST_FAVORITE.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_REGIST_FAVORITE.getErrorMessage()));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoFavoriteControllerIntegrationTest.sql")
	class deleteFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deleteFavorite_success() throws Exception {
			Authentication authentication = createAuthentication();

			mockMvc.perform(
					delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite_success.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("お気に入りを解除しました。"));

			List<PhotoFavorite> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE account_no=1 and favorite_photo_account_no=1 and favorite_photo_no=1", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getLong("account_no"))
							.favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getLong("favorite_photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(0, actualData.size());

			List<PhotoFavorite> actualRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getLong("account_no"))
							.favoritePhotoAccountNo(rs.getLong("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getLong("favorite_photo_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());

			assertEquals(3, actualRestData.size());
			assertEquals(1L, actualRestData.get(0).getAccountNo());
			assertEquals(1L, actualRestData.get(0).getFavoritePhotoAccountNo());
			assertEquals(2L, actualRestData.get(0).getFavoritePhotoNo());
			assertEquals(2L, actualRestData.get(1).getAccountNo());
			assertEquals(1L, actualRestData.get(1).getFavoritePhotoAccountNo());
			assertEquals(2L, actualRestData.get(1).getFavoritePhotoNo());
			assertEquals(2L, actualRestData.get(2).getAccountNo());
			assertEquals(2L, actualRestData.get(2).getFavoritePhotoAccountNo());
			assertEquals(1L, actualRestData.get(2).getFavoritePhotoNo());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void deleteFavorite_BadRequestException() throws Exception {
			Authentication authentication = createAuthentication();

			mockMvc.perform(
					delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite_badrequest.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deleteFavorite_UpdateFailureException() throws Exception {
			Authentication authentication = createAuthentication();

			mockMvc.perform(
					delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite_update_failure.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_CANCEL_FAVORITE.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_CANCEL_FAVORITE.getErrorMessage()));
		}
	}
}
