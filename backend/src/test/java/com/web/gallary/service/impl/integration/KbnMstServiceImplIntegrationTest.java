package com.web.gallary.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.model.KbnMstModel;
import com.web.gallary.service.impl.KbnMstServiceImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class KbnMstServiceImplIntegrationTest {
	@Autowired
	private KbnMstServiceImpl kbnMstServiceImpl;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPrefectureList {
		@Test
		@Order(1)
		@DisplayName("正常系：区分マスタが存在する場合")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/KbnMstServiceImplIntegrationTest.sql")
		void getPrefectureList_found() {
			List<KbnMstModel> actual = kbnMstServiceImpl.getPrefectureList();
			assertEquals(47, actual.size());
			
			List<KbnMstModel> actualSorded = actual.stream().sorted(Comparator.comparing(KbnMstModel::getSortOrder)).toList();
			assertEquals("Hokkaido", actualSorded.get(0).getKbnCode());
			assertEquals("Aomori", actualSorded.get(1).getKbnCode());
			assertEquals("Iwate", actualSorded.get(2).getKbnCode());
			assertEquals("Miyagi", actualSorded.get(3).getKbnCode());
			assertEquals("Akita", actualSorded.get(4).getKbnCode());
			assertEquals("Yamagata", actualSorded.get(5).getKbnCode());
			assertEquals("Fukushima", actualSorded.get(6).getKbnCode());
			assertEquals("Ibaraki", actualSorded.get(7).getKbnCode());
			assertEquals("Tochigi", actualSorded.get(8).getKbnCode());
			assertEquals("Gunma", actualSorded.get(9).getKbnCode());
			assertEquals("Saitama", actualSorded.get(10).getKbnCode());
			assertEquals("Chiba", actualSorded.get(11).getKbnCode());
			assertEquals("Tokyo", actualSorded.get(12).getKbnCode());
			assertEquals("Kanagawa", actualSorded.get(13).getKbnCode());
			assertEquals("Niigata", actualSorded.get(14).getKbnCode());
			assertEquals("Toyama", actualSorded.get(15).getKbnCode());
			assertEquals("Ishikawa", actualSorded.get(16).getKbnCode());
			assertEquals("Fukui", actualSorded.get(17).getKbnCode());
			assertEquals("Yamanashi", actualSorded.get(18).getKbnCode());
			assertEquals("Nagano", actualSorded.get(19).getKbnCode());
			assertEquals("Gifu", actualSorded.get(20).getKbnCode());
			assertEquals("Shizuoka", actualSorded.get(21).getKbnCode());
			assertEquals("Aichi", actualSorded.get(22).getKbnCode());
			assertEquals("Mie", actualSorded.get(23).getKbnCode());
			assertEquals("Shiga", actualSorded.get(24).getKbnCode());
			assertEquals("Kyoto", actualSorded.get(25).getKbnCode());
			assertEquals("Osaka", actualSorded.get(26).getKbnCode());
			assertEquals("Hyogo", actualSorded.get(27).getKbnCode());
			assertEquals("Nara", actualSorded.get(28).getKbnCode());
			assertEquals("Wakayama", actualSorded.get(29).getKbnCode());
			assertEquals("Tottori", actualSorded.get(30).getKbnCode());
			assertEquals("Shimane", actualSorded.get(31).getKbnCode());
			assertEquals("Okayama", actualSorded.get(32).getKbnCode());
			assertEquals("Hiroshima", actualSorded.get(33).getKbnCode());
			assertEquals("Yamaguchi", actualSorded.get(34).getKbnCode());
			assertEquals("Tokushima", actualSorded.get(35).getKbnCode());
			assertEquals("Kagawa", actualSorded.get(36).getKbnCode());
			assertEquals("Ehime", actualSorded.get(37).getKbnCode());
			assertEquals("Kochi", actualSorded.get(38).getKbnCode());
			assertEquals("Fukuoka", actualSorded.get(39).getKbnCode());
			assertEquals("Saga", actualSorded.get(40).getKbnCode());
			assertEquals("Nagasaki", actualSorded.get(41).getKbnCode());
			assertEquals("Kumamoto", actualSorded.get(42).getKbnCode());
			assertEquals("Oita", actualSorded.get(43).getKbnCode());
			assertEquals("Miyazaki", actualSorded.get(44).getKbnCode());
			assertEquals("Kagoshima", actualSorded.get(45).getKbnCode());
			assertEquals("Okinawa", actualSorded.get(46).getKbnCode());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分マスタが存在しない場合")
		@Sql("/sql/common/cleanup.sql")
		void getPrefectureList_not_found() {
			List<KbnMstModel> actual = kbnMstServiceImpl.getPrefectureList();
			assertEquals(0, actual.size());
		}
	}
}