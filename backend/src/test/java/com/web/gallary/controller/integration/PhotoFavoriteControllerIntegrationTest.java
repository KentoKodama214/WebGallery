package com.web.gallary.controller.integration;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

import com.web.gallary.AccountPrincipal;
import com.web.gallary.entity.Account;
import com.web.gallary.entity.PhotoFavorite;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.ErrorEnum;

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
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(2, actualData.getFirst().getFavoritePhotoAccountNo());
			assertEquals(1, actualData.getFirst().getFavoritePhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void addFavorite_BadRequestException() throws Exception {
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_REGIST_FAVORITE.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/aaaaaaaa/photo_list"));
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
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(0, actualData.size());

			List<PhotoFavorite> actualRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());

			assertEquals(3, actualRestData.size());
			assertEquals(1, actualRestData.get(0).getAccountNo());
			assertEquals(1, actualRestData.get(0).getFavoritePhotoAccountNo());
			assertEquals(2, actualRestData.get(0).getFavoritePhotoNo());
			assertEquals(2, actualRestData.get(1).getAccountNo());
			assertEquals(1, actualRestData.get(1).getFavoritePhotoAccountNo());
			assertEquals(2, actualRestData.get(1).getFavoritePhotoNo());
			assertEquals(2, actualRestData.get(2).getAccountNo());
			assertEquals(2, actualRestData.get(2).getFavoritePhotoAccountNo());
			assertEquals(1, actualRestData.get(2).getFavoritePhotoNo());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void deleteFavorite_BadRequestException() throws Exception {
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

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
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_CANCEL_FAVORITE.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/aaaaaaaa/photo_list"));
		}
	}
}
