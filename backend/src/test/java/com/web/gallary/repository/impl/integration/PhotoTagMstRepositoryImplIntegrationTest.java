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

import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.model.PhotoTagDeleteModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.repository.impl.PhotoTagMstRepositoryImpl;

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
		void regist_contain_null_parameter() throws RegistFailureException {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(3)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build();
			
			photoTagMstRepositoryImpl.regist(photoTagModel);
			
			List<PhotoTagMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1 and tag_no=3", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getPhotoNo());
			assertEquals(3, actualData.getFirst().getTagNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals("海", actualData.getFirst().getTagJapaneseName());
			assertEquals("sea", actualData.getFirst().getTagEnglishName());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
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
					.accountNo(1)
					.photoNo(1)
					.build();
			
			photoTagMstRepositoryImpl.clear(photoTagDeleteModel);
			
			List<PhotoTagMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(3, actualRestData.size());
		}
	}
}