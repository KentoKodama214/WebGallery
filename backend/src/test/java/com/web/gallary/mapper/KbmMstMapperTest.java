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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallary.entity.KbnMst;

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
			KbnMst kbnMst = KbnMst.builder().kbnClassCode("sex").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("woman")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(2)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("女性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("woman")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分コードでのselectで1件以上の場合")
		void select_by_kbnCode() {
			KbnMst kbnMst = KbnMst.builder().kbnCode("man").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：並び順でのselectで1件の場合")
		void select_by_sortOrder() {
			KbnMst kbnMst = KbnMst.builder().sortOrder(47).build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Okinawa")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(47)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("沖縄")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Okinawa")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：区分グループコードでのselectで1件の場合")
		void select_by_kbnGroupCode() {
			KbnMst kbnMst = KbnMst.builder().kbnGroupCode("Shikoku").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Tokushima")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(36)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("徳島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Tokushima")
					.explanation("")
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagawa")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(37)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("香川")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kagawa")
					.explanation("")
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Ehime")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(38)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("愛媛")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Ehime")
					.explanation("")
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kochi")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(39)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("高知")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kochi")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：区分クラス日本語名でのselectで1件の場合")
		void select_by_kbnClassJapaneseName() {
			KbnMst kbnMst = KbnMst.builder().kbnClassJapaneseName("性別").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("woman")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(2)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("女性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("woman")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：区分グループ日本語名でのselectで1件の場合")
		void select_by_kbnGroupJapaneseName() {
			KbnMst kbnMst = KbnMst.builder().kbnGroupJapaneseName("四国").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Tokushima")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(36)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("徳島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Tokushima")
					.explanation("")
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagawa")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(37)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("香川")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kagawa")
					.explanation("")
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Ehime")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(38)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("愛媛")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Ehime")
					.explanation("")
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kochi")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(39)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("高知")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kochi")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：区分日本語名でのselectで1件の場合")
		void select_by_kbnJapaneseName() {
			KbnMst kbnMst = KbnMst.builder().kbnJapaneseName("男性").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：区分クラス英語名でのselectで1件の場合")
		void select_by_kbnClassEnglishName() {
			KbnMst kbnMst = KbnMst.builder().kbnClassEnglishName("sex").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("woman")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(2)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("女性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("woman")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：区分グループ英語名でのselectで1件の場合")
		void select_by_kbnGroupEnglishName() {
			KbnMst kbnMst = KbnMst.builder().kbnGroupEnglishName("Shikoku").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Tokushima")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(36)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("徳島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Tokushima")
					.explanation("")
					.build();
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagawa")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(37)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("香川")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kagawa")
					.explanation("")
					.build();
			KbnMst expectedKbnMst3 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Ehime")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(38)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("愛媛")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Ehime")
					.explanation("")
					.build();
			KbnMst expectedKbnMst4 = KbnMst.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kochi")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(39)
					.kbnGroupCode("Shikoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("四国")
					.kbnJapaneseName("高知")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Shikoku")
					.kbnEnglishName("Kochi")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			expected.add(expectedKbnMst3);
			expected.add(expectedKbnMst4);
			
			assertEquals(4, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：区分英語名でのselectで1件の場合")
		void select_by_kbnEnglishName() {
			KbnMst kbnMst = KbnMst.builder().kbnEnglishName("man").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：説明でのselectで1件の場合")
		void select_by_explanation() {
			KbnMst kbnMst = KbnMst.builder().explanation("サイトを管理・運営する人").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst = KbnMst.builder()
					.kbnClassCode("authority")
					.kbnCode("administrator")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(3)
					.kbnGroupCode("")
					.kbnClassJapaneseName("権限")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("管理者")
					.kbnClassEnglishName("authority")
					.kbnGroupEnglishName("")
					.kbnEnglishName("administrator")
					.explanation("サイトを管理・運営する人")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：selectで0件の場合")
		void select_not_found() {
			KbnMst kbnMst = KbnMst.builder().kbnCode("superman").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			List<KbnMst> expected = new ArrayList<KbnMst>();
			
			assertEquals(0, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：selectで2件以上の場合")
		void select_kbnMsts() {
			KbnMst kbnMst = KbnMst.builder().kbnClassCode("sex").build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			KbnMst expectedKbnMst2 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("woman")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(2)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("女性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("woman")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			expected.add(expectedKbnMst2);
			
			assertEquals(2, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：複数の条件でselectする場合")
		void select_some_conditions() {
			KbnMst kbnMst = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.build();
			
			List<KbnMst> actual = kbnMstMapper.select(kbnMst);
			
			KbnMst expectedKbnMst1 = KbnMst.builder()
					.kbnClassCode("sex")
					.kbnCode("man")
					.createdBy(0)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("性別")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("男性")
					.kbnClassEnglishName("sex")
					.kbnGroupEnglishName("")
					.kbnEnglishName("man")
					.explanation("")
					.build();
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(expectedKbnMst1);
			
			assertEquals(1, actual.size());
			assertEquals(expected.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList(),
					actual.stream().sorted(Comparator.comparing(KbnMst::getSortOrder)).toList());
		}
	}
}