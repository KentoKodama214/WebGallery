package com.web.gallary.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallary.entity.PhotoFavorite;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoFavoriteMapperTest {
	@Autowired
	private PhotoFavoriteMapper photoFavoriteMapper;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoFavoriteMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class insert {
		@Test
		@Order(1)
		@DisplayName("正常系：登録成功")
		void insert_success() {
			PhotoFavorite insertPhotoFavorite = PhotoFavorite.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(2)
					.favoritePhotoNo(1)
					.createdBy(1)
					.build();
			
			Integer actualCount = photoFavoriteMapper.insert(insertPhotoFavorite);
			assertEquals(1, actualCount);
			
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
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoFavoriteMapperTest.sql")
	class delete {
		private List<PhotoFavorite> getPhotoFavoriteList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE " + condition, (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのdelete")
		void delete_by_accountNo() {
			PhotoFavorite deletePhotoFavorite = PhotoFavorite.builder().accountNo(1).build();
			Integer actual = photoFavoriteMapper.delete(deletePhotoFavorite);
			assertEquals(2, actual);
			
			List<PhotoFavorite> actualData = getPhotoFavoriteList("account_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoFavorite> actualRestData = getPhotoFavoriteList("account_no<>1");
			assertEquals(2, actualRestData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：お気に入り写真アカウント番号でのdelete")
		void delete_by_favoritePhotoAccountNo() {
			PhotoFavorite deletePhotoFavorite = PhotoFavorite.builder().favoritePhotoAccountNo(1).build();
			Integer actual = photoFavoriteMapper.delete(deletePhotoFavorite);
			assertEquals(3, actual);
			
			List<PhotoFavorite> actualData = getPhotoFavoriteList("favorite_photo_account_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoFavorite> actualRestData = getPhotoFavoriteList("favorite_photo_account_no<>1");
			assertEquals(1, actualRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：お気に入り写真番号でのdelete")
		void delete_by_favoritePhotoNo() {
			PhotoFavorite deletePhotoFavorite = PhotoFavorite.builder().favoritePhotoNo(1).build();
			Integer actual = photoFavoriteMapper.delete(deletePhotoFavorite);
			assertEquals(2, actual);
			
			List<PhotoFavorite> actualData = getPhotoFavoriteList("favorite_photo_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoFavorite> actualRestData = getPhotoFavoriteList("favorite_photo_no<>1");
			assertEquals(2, actualRestData.size());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：削除対象のレコードなし")
		void delete_not_found() {
			PhotoFavorite deletePhotoFavorite = PhotoFavorite.builder().accountNo(3).build();
			Integer actual = photoFavoriteMapper.delete(deletePhotoFavorite);
			assertEquals(0, actual);
			
			List<PhotoFavorite> actualData = getPhotoFavoriteList("account_no=3");
			assertEquals(0, actualData.size());
			
			List<PhotoFavorite> actualRestData = getPhotoFavoriteList("account_no<>3");
			assertEquals(4, actualRestData.size());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：複数の条件でdeleteする場合")
		void delete_some_conditions() {
			PhotoFavorite deletePhotoFavorite = PhotoFavorite.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			Integer actual = photoFavoriteMapper.delete(deletePhotoFavorite);
			assertEquals(1, actual);
			
			List<PhotoFavorite> actualData = getPhotoFavoriteList("account_no=1 and favorite_photo_account_no=1 and favorite_photo_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoFavorite> actualRestData = getPhotoFavoriteList("account_no<>1 or favorite_photo_account_no<>1 or favorite_photo_no<>1");
			assertEquals(3, actualRestData.size());
		}
	}
}