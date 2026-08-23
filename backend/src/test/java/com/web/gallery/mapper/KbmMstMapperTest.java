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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.Explanation;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnClassEnglishName;
import com.web.gallery.domain.common.KbnClassJapaneseName;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.domain.common.KbnEnglishName;
import com.web.gallery.domain.common.KbnGroupCode;
import com.web.gallery.domain.common.KbnGroupEnglishName;
import com.web.gallery.domain.common.KbnGroupJapaneseName;
import com.web.gallery.domain.common.KbnJapaneseName;
import com.web.gallery.domain.common.SortOrder;
import com.web.gallery.entity.KbnMst;
import com.web.gallery.entity.KbnMstCondition;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class KbmMstMapperTest {
	@Autowired
	private KbnMstMapper kbnMstMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/KbnMstMapperTest.sql")
	class select {
		@Test
		@Order(1)
		@DisplayName("正常系：区分クラスコードでのselectで1件以上の場合")
		void select_by_kbnClassCode() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnClassCode(new KbnClassCode("sex")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("woman"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(2))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("女性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("woman"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分コードでのselectで1件以上の場合")
		void select_by_kbnCode() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnCode(new KbnCode("man")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：並び順でのselectで1件の場合")
		void select_by_sortOrder() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().sortOrder(new SortOrder(47)).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Okinawa"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(47))
					.kbnGroupCode(new KbnGroupCode("Kyushu_Okinawa"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("九州・沖縄"))
					.kbnJapaneseName(new KbnJapaneseName("沖縄"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Kyushu_Okinawa"))
					.kbnEnglishName(new KbnEnglishName("Okinawa"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：区分グループコードでのselectで1件の場合")
		void select_by_kbnGroupCode() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnGroupCode(new KbnGroupCode("Shikoku")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Tokushima"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(36))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("徳島"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Tokushima"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kagawa"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(37))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("香川"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kagawa"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Ehime"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(38))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("愛媛"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Ehime"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kochi"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(39))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("高知"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kochi"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：区分クラス日本語名でのselectで1件の場合")
		void select_by_kbnClassJapaneseName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnClassJapaneseName(new KbnClassJapaneseName("性別")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("woman"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(2))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("女性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("woman"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：区分グループ日本語名でのselectで1件の場合")
		void select_by_kbnGroupJapaneseName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnGroupJapaneseName(new KbnGroupJapaneseName("四国")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Tokushima"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(36))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("徳島"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Tokushima"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kagawa"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(37))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("香川"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kagawa"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Ehime"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(38))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("愛媛"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Ehime"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kochi"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(39))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("高知"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kochi"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：区分日本語名でのselectで1件の場合")
		void select_by_kbnJapaneseName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnJapaneseName(new KbnJapaneseName("男性")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：区分クラス英語名でのselectで1件の場合")
		void select_by_kbnClassEnglishName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnClassEnglishName(new KbnClassEnglishName("sex")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("woman"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(2))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("女性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("woman"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：区分グループ英語名でのselectで1件の場合")
		void select_by_kbnGroupEnglishName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Tokushima"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(36))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("徳島"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Tokushima"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kagawa"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(37))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("香川"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kagawa"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Ehime"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(38))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("愛媛"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Ehime"))
					.explanation(new Explanation(""))
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("prefecture"))
					.kbnCode(new KbnCode("Kochi"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(39))
					.kbnGroupCode(new KbnGroupCode("Shikoku"))
					.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
					.kbnJapaneseName(new KbnJapaneseName("高知"))
					.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
					.kbnGroupEnglishName(new KbnGroupEnglishName("Shikoku"))
					.kbnEnglishName(new KbnEnglishName("Kochi"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：区分英語名でのselectで1件の場合")
		void select_by_kbnEnglishName() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnEnglishName(new KbnEnglishName("man")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：説明でのselectで1件の場合")
		void select_by_explanation() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().explanation(new Explanation("サイトを管理・運営する人")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("authority"))
					.kbnCode(new KbnCode("administrator"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(3))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("権限"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("管理者"))
					.kbnClassEnglishName(new KbnClassEnglishName("authority"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("administrator"))
					.explanation(new Explanation("サイトを管理・運営する人"))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：selectで0件の場合")
		void select_not_found() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnCode(new KbnCode("superman")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			List<KbnMst> expected = new ArrayList<KbnMst>();
			
			assertEquals(0, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：selectで2件以上の場合")
		void select_kbnMsts() {
			KbnMstCondition kbnMst = KbnMstCondition.builder().kbnClassCode(new KbnClassCode("sex")).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("woman"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(2))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("女性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("woman"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：複数の条件でselectする場合")
		void select_some_conditions() {
			KbnMstCondition kbnMst = KbnMstCondition.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode(new KbnClassCode("sex"))
					.kbnCode(new KbnCode("man"))
					.createdBy(new CreatedBy(0L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.sortOrder(new SortOrder(1))
					.kbnGroupCode(new KbnGroupCode(""))
					.kbnClassJapaneseName(new KbnClassJapaneseName("性別"))
					.kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
					.kbnJapaneseName(new KbnJapaneseName("男性"))
					.kbnClassEnglishName(new KbnClassEnglishName("sex"))
					.kbnGroupEnglishName(new KbnGroupEnglishName(""))
					.kbnEnglishName(new KbnEnglishName("man"))
					.explanation(new Explanation(""))
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList(),
					actual.stream().sorted(Comparator.comparing(k -> k.getSortOrder().value())).toList());
		}
	}
}