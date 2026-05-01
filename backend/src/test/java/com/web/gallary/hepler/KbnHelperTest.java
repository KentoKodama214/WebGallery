package com.web.gallary.hepler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.helper.KbnHelper;
import com.web.gallary.model.KbnMstModel;

@SpringBootTest
@ActiveProfiles("test")
public class KbnHelperTest {
	@Autowired
	private KbnHelper kbnHepler;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class convertToLinkedHashMap {
		@Test
		@Order(1)
		@DisplayName("正常系：区分グループなし")
		void convertToLinkedHashMap_not_kbnGroup() {
			List<KbnMstModel> kbnMstModelList = new ArrayList<KbnMstModel>();
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Aomori")
					.sortOrder(2)
					.kbnGroupCode("")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("青森")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("")
					.kbnEnglishName("Aomori")
					.explanation("青森は本州最北")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Hokkaido")
					.sortOrder(1)
					.kbnGroupCode("")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("北海道")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("")
					.kbnEnglishName("Hokkaido")
					.explanation("北海道はでっかいどう")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Okinawa")
					.sortOrder(47)
					.kbnGroupCode("")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("沖縄")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("")
					.kbnEnglishName("Okinawa")
					.explanation("沖縄は南国")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagoshima")
					.sortOrder(46)
					.kbnGroupCode("")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("")
					.kbnJapaneseName("鹿児島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("")
					.kbnEnglishName("Kagoshima")
					.explanation("鹿児島は九州最南")
					.build());
			
			Map<String, List<KbnMstModel>> actual
				= kbnHepler.convertToLinkedHashMap(kbnMstModelList);
			
			List<KbnMstModel> kbnMstModelList1 = actual.get("");
			assertEquals(4, kbnMstModelList1.size());
			assertEquals("Hokkaido", kbnMstModelList1.get(0).getKbnCode());
			assertEquals("Aomori", kbnMstModelList1.get(1).getKbnCode());
			assertEquals("Kagoshima", kbnMstModelList1.get(2).getKbnCode());
			assertEquals("Okinawa", kbnMstModelList1.get(3).getKbnCode());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分グループあり")
		void convertToLinkedHashMap_with_kbnGroup() {
			List<KbnMstModel> kbnMstModelList = new ArrayList<KbnMstModel>();
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Aomori")
					.sortOrder(2)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("青森")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Aomori")
					.explanation("青森は本州最北")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Hokkaido")
					.sortOrder(1)
					.kbnGroupCode("Hokkaido_Tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北")
					.kbnJapaneseName("北海道")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Hokkaido_Tohoku")
					.kbnEnglishName("Hokkaido")
					.explanation("北海道はでっかいどう")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Okinawa")
					.sortOrder(47)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("沖縄")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Okinawa")
					.explanation("沖縄は南国")
					.build());
			kbnMstModelList.add(KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Kagoshima")
					.sortOrder(46)
					.kbnGroupCode("Kyushu_Okinawa")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("九州・沖縄")
					.kbnJapaneseName("鹿児島")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("Kyushu_Okinawa")
					.kbnEnglishName("Kagoshima")
					.explanation("鹿児島は九州最南")
					.build());
			
			Map<String, List<KbnMstModel>> actual
				= kbnHepler.convertToLinkedHashMap(kbnMstModelList);
			
			List<KbnMstModel> kbnMstModelList1 = actual.get("北海道・東北");
			assertEquals(2, kbnMstModelList1.size());
			assertEquals("Hokkaido", kbnMstModelList1.get(0).getKbnCode());
			assertEquals("Aomori", kbnMstModelList1.get(1).getKbnCode());
			List<KbnMstModel> kbnMstModelList2 = actual.get("九州・沖縄");
			assertEquals(2, kbnMstModelList2.size());
			assertEquals("Kagoshima", kbnMstModelList2.get(0).getKbnCode());
			assertEquals("Okinawa", kbnMstModelList2.get(1).getKbnCode());
		}
	}
}