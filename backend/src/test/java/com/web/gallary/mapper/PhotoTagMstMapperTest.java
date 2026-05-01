package com.web.gallary.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
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

import com.web.gallary.entity.PhotoTagMst;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoTagMstMapperTest {
	@Autowired
	private PhotoTagMstMapper photoTagMstMapper;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class select {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのselectで1件以上の場合")
		void select_by_accountNo() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().accountNo(1).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(2)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build();
			PhotoTagMst expectedPhotoTagMst3 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst4 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(2)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("曇天")
					.tagEnglishName("cloudy")
					.build();
			PhotoTagMst expectedPhotoTagMst5 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(3)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 3, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("花")
					.tagEnglishName("flower")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			expected.add(expectedPhotoTagMst3);
			expected.add(expectedPhotoTagMst4);
			expected.add(expectedPhotoTagMst5);
			
			assertEquals(5, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのselectで1件の場合")
		void select_by_photoNo() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().photoNo(1).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(2)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：タグ番号でのselectで1件の場合")
		void select_by_tagNo() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().tagNo(1).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：タグ日本語名でのselectで1件の場合")
		void select_by_tagJapaneseName() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().tagJapaneseName("太陽").build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：タグ英語名でのselectで1件の場合")
		void select_by_tagEnglishName() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().tagEnglishName("sun").build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：selectで0件の場合")
		void select_not_found() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder().accountNo(3).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			assertEquals(0, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：複数の条件でselectする場合")
		void select_some_conditions() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList(),
					actual.stream().sorted(Comparator.comparing(PhotoTagMst::getCreatedAt)).toList());
		}
	}
	
	@Nested
	@Order(2)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class insert {
		@Test
		@Order(1)
		@DisplayName("正常系：登録成功")
		void insert_success() {
			PhotoTagMst photoTagMst = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(3)
					.createdBy(1)
					.tagJapaneseName("春")
					.tagEnglishName("spring")
					.build();
			
			Integer actualCount = photoTagMstMapper.insert(photoTagMst);
			assertEquals(1, actualCount);
			
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
			assertEquals("春", actualData.getFirst().getTagJapaneseName());
			assertEquals("spring", actualData.getFirst().getTagEnglishName());
		}
	}
	
	@Nested
	@Order(3)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		private List<PhotoTagMst> getPhotoTagMstList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE " + condition, (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.tagNo(rs.getInt("tag_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのdelete")
		void delete_by_accountNo() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().accountNo(1).build();
			Integer deleteCount = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(deleteCount, 5);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("account_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("account_no<>1");
			assertEquals(0, actualRestData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのdelete")
		void delete_by_photoNo() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().photoNo(1).build();
			Integer deleteCount = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(deleteCount, 2);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("photo_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("photo_no<>1");
			assertEquals(3, actualRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：タグ番号でのdelete")
		void delete_by_tagNo() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().tagNo(1).build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(2, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("tag_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("tag_no<>1");
			assertEquals(3, actualRestData.size());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：タグ日本語名でのdelete")
		void delete_by_tagJapaneseName() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().tagJapaneseName("太陽").build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(2, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("tag_japanese_name='太陽'");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("tag_japanese_name<>'太陽'");
			assertEquals(3, actualRestData.size());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：タグ英語名でのdelete")
		void delete_by_tagEnglishName() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().tagEnglishName("sun").build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(2, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("tag_english_name='sun'");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("tag_english_name<>'sun'");
			assertEquals(3, actualRestData.size());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：deleteで0件の場合")
		void delete_not_found() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder().accountNo(3).build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(0, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("account_no=3");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("account_no<>3");
			assertEquals(5, actualRestData.size());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：複数の条件でdeleteする場合")
		void delete_some_conditions() {
			PhotoTagMst deletePhotoTagMst = PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(1, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("account_no=1 and photo_no=1 and tag_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("account_no<>1 or photo_no<>1 or tag_no<>1");
			assertEquals(4, actualRestData.size());
		}
	}
}