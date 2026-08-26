package com.web.gallery.repository.impl.integration;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.repository.impl.PhotoTagMstRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoTagMstRepositoryImplIntegrationTest {
	@Autowired
	private PhotoTagMstRepositoryImpl photoTagMstRepositoryImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoTagMstRepositoryImplIntegrationTest.sql")
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void regist_contain_null_parameter() throws GalleryException {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(3L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoTagMstRepositoryImpl.regist(photoTagModel);

			List<PhotoTagMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1 and tag_no=3", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getPhotoNo());
			assertEquals(3L, actualData.getFirst().getTagNo());
			assertEquals(1L, actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
			assertEquals("海", actualData.getFirst().getTagJapaneseName());
			assertEquals("sea", actualData.getFirst().getTagEnglishName());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("海"))
					.tagEnglishName(new TagEnglishName("sea"))
					.build();
			
			assertThrows(RegistFailureException.class, () -> photoTagMstRepositoryImpl.regist(photoTagModel));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoTagMstRepositoryImplIntegrationTest.sql")
	class clear {
		@Test
		@Order(1)
		@DisplayName("正常系：")
		void clear_success() {
			PhotoTagDeleteModel photoTagDeleteModel = PhotoTagDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			photoTagMstRepositoryImpl.clear(photoTagDeleteModel);
			
			List<PhotoTagMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(3, actualRestData.size());
		}
	}
}