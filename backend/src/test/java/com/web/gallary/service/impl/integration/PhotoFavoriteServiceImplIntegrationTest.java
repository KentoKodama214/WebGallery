package com.web.gallary.service.impl.integration;

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
import com.web.gallary.model.PhotoFavoriteModel;
import com.web.gallary.service.impl.PhotoFavoriteServiceImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoFavoriteServiceImplIntegrationTest {
	@Autowired
	private PhotoFavoriteServiceImpl photoFavoriteServiceImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoFavoriteServiceImplIntegrationTest.sql")
	class addFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void addFavorite_success() throws RegistFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(2)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			photoFavoriteServiceImpl.addFavorite(photoFavoriteModel);
			
			List<PhotoFavorite> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE account_no=2 and favorite_photo_account_no=1 and favorite_photo_no=1", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(1, actualData.size());
			assertEquals(2, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getFavoritePhotoAccountNo());
			assertEquals(1, actualData.getFirst().getFavoritePhotoNo());
			assertEquals(2, actualData.getFirst().getCreatedBy());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void addFavorite_RegistFailureException() throws RegistFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			assertThrows(RegistFailureException.class, () -> photoFavoriteServiceImpl.addFavorite(photoFavoriteModel));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/PhotoFavoriteServiceImplIntegrationTest.sql")
	class deleteFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deleteFavorite_success() throws UpdateFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel);
			
			List<PhotoFavorite> actualData = jdbcTemplate.query(
			"SELECT * FROM photo.photo_favorite WHERE account_no=1 and favorite_photo_account_no=2 and favorite_photo_no=1", (rs, rowNum) ->
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
		void deleteFavorite_UpdateFailureException() throws UpdateFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(9)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			assertThrows(UpdateFailureException.class, () ->photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel));
		}
	}
}