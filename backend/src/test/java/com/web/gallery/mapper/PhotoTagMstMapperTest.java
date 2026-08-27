package com.web.gallery.mapper;

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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.PhotoNo;

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
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().accountNo(1L).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(2L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build();
			PhotoTagMst expectedPhotoTagMst3 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst4 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(2L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("曇天")
					.tagEnglishName("cloudy")
					.build();
			PhotoTagMst expectedPhotoTagMst5 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(3L)
					.createdBy(1L)
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
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのselectで1件の場合")
		void select_by_photoNo() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().photoNo(1L).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(2L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：タグ番号でのselectで1件の場合")
		void select_by_tagNo() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().tagNo(1L).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：タグ日本語名でのselectで1件の場合")
		void select_by_tagJapaneseName() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().tagJapaneseName("太陽").build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：タグ英語名でのselectで1件の場合")
		void select_by_tagEnglishName() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().tagEnglishName("sun").build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			expected.add(expectedPhotoTagMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：selectで0件の場合")
		void select_not_found() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder().accountNo(3L).build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			assertEquals(0, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：複数の条件でselectする場合")
		void select_some_conditions() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			List<PhotoTagMst> expected = new ArrayList<PhotoTagMst>();
			expected.add(expectedPhotoTagMst1);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
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
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(3L)
					.createdBy(1L)
					.tagJapaneseName("春")
					.tagEnglishName("spring")
					.build();
			
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actualCount = photoTagMstMapper.insert(photoTagMst);
			assertEquals(1, actualCount);

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
			assertEquals("春", actualData.getFirst().getTagJapaneseName());
			assertEquals("spring", actualData.getFirst().getTagEnglishName());
		}
	}
	
	@Nested
	@Order(3)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class insertBulk {
		@Test
		@Order(1)
		@DisplayName("正常系：複数件の一括登録成功")
		void insertBulk_success() {
			PhotoTagMst photoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(3L)
					.createdBy(1L)
					.tagJapaneseName("春")
					.tagEnglishName("spring")
					.build();
			PhotoTagMst photoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(4L)
					.createdBy(1L)
					.tagJapaneseName("秋")
					.tagEnglishName("autumn")
					.build();

			Integer actualCount = photoTagMstMapper.insertBulk(List.of(photoTagMst1, photoTagMst2));
			assertEquals(2, actualCount);

			List<PhotoTagMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1 and tag_no IN (3, 4) ORDER BY tag_no", (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
			assertEquals(2, actualData.size());
			assertEquals("春", actualData.get(0).getTagJapaneseName());
			assertEquals("秋", actualData.get(1).getTagJapaneseName());
		}
	}

	@Nested
	@Order(4)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		private List<PhotoTagMst> getPhotoTagMstList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE " + condition, (rs, rowNum) ->
						PhotoTagMst.builder()
							.accountNo(rs.getLong("account_no"))
							.photoNo(rs.getLong("photo_no"))
							.tagNo(rs.getLong("tag_no"))
							.createdBy(rs.getLong("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
							.tagEnglishName(rs.getObject("tag_english_name").toString())
							.build());
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのdelete")
		void delete_by_accountNo() {
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().accountNo(1L).build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().photoNo(1L).build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().tagNo(1L).build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().tagJapaneseName("太陽").build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().tagEnglishName("sun").build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder().accountNo(3L).build();
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
			PhotoTagMstCondition deletePhotoTagMst = PhotoTagMstCondition.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.build();
			Integer actual = photoTagMstMapper.delete(deletePhotoTagMst);
			assertEquals(1, actual);
			
			List<PhotoTagMst> actualData = getPhotoTagMstList("account_no=1 and photo_no=1 and tag_no=1");
			assertEquals(0, actualData.size());
			
			List<PhotoTagMst> actualRestData = getPhotoTagMstList("account_no<>1 or photo_no<>1 or tag_no<>1");
			assertEquals(4, actualRestData.size());
		}
	}

	@Nested
	@Order(5)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoTagMstMapperTest.sql")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class selectByPhotoNoList {
		@Test
		@Order(1)
		@DisplayName("正常系：写真番号リストでのselectで複数件の場合")
		void select_by_photoNoList() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder()
					.accountNo(1L)
					.photoNoList(List.of(1L, 2L))
					.build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);
			actual.forEach(e -> e.setId(null));

			PhotoTagMst expectedPhotoTagMst1 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst2 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(1L)
					.tagNo(2L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("青空")
					.tagEnglishName("bluesky")
					.build();
			PhotoTagMst expectedPhotoTagMst3 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 1, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			PhotoTagMst expectedPhotoTagMst4 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(2L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 2, 1, 2, 0, 0, 0, ZoneOffset.ofHours(0)))
					.tagJapaneseName("曇天")
					.tagEnglishName("cloudy")
					.build();
			PhotoTagMst expectedPhotoTagMst5 = PhotoTagMst.builder()
					.accountNo(1L)
					.photoNo(2L)
					.tagNo(3L)
					.createdBy(1L)
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
			assertEquals(expected.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList(),
					actual.stream().sorted(Comparator.comparing(p -> p.getCreatedAt())).toList());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：写真番号リストが空の場合は絞り込まれない")
		void select_by_photoNoList_empty() {
			PhotoTagMstCondition photoTagMst = PhotoTagMstCondition.builder()
					.accountNo(1L)
					.photoNoList(List.of())
					.build();
			List<PhotoTagMst> actual = photoTagMstMapper.select(photoTagMst);

			assertEquals(5, actual.size());
		}
	}
}