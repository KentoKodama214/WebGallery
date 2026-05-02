package com.web.gallary.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.entity.PhotoFavorite;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoFavoriteModel;
import com.web.gallary.repository.impl.PhotoFavoriteRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoFavoriteRepositoryImplIntegrationTest {
	@Autowired
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void regist_contain_null_parameter() throws RegistFailureException {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(2)
					.favoritePhotoNo(1)
					.build();
			
			photoFavoriteRepositoryImpl.regist(favoriteModel);
			
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
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			assertThrows(RegistFailureException.class, () -> photoFavoriteRepositoryImpl.regist(favoriteModel));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void delete_contain_null_parameter() throws UpdateFailureException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			photoFavoriteRepositoryImpl.delete(favoriteDeleteModel);
			
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
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void delete_UpdateFailureException() throws UpdateFailureException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(3)
					.favoritePhotoNo(1)
					.build();
			
			assertThrows(UpdateFailureException.class, () -> photoFavoriteRepositoryImpl.delete(favoriteDeleteModel));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoFavoriteRepositoryImplIntegrationTest.sql")
	class clear {
		@Test
		@Order(1)
		@DisplayName("正常系：")
		void clear_success() {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			photoFavoriteRepositoryImpl.clear(favoriteDeleteModel);
			
			List<PhotoFavorite> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE favorite_photo_account_no=1 and favorite_photo_no=1", (rs, rowNum) ->
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
		}
	}
}