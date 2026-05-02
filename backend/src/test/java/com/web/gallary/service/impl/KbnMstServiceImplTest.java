package com.web.gallary.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.model.KbnMstModel;
import com.web.gallary.repository.impl.KbnMstRepositoryImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class KbnMstServiceImplTest {
	@InjectMocks
	private KbnMstServiceImpl kbnMstServiceImpl;
	
	@Mock
	private KbnMstRepositoryImpl kbnMstRepositoryImpl;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPrefectureList {
		@Test
		@Order(1)
		@DisplayName("正常系：区分マスタが存在する場合")
		void getPrefectureList_found() {
			List<KbnMstModel> kbnMstModelList = new ArrayList<KbnMstModel>();
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
			doReturn(kbnMstModelList).when(kbnMstRepositoryImpl).get("prefecture");
			assertEquals(kbnMstModelList, kbnMstServiceImpl.getPrefectureList());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分マスタが存在しない場合")
		void getPrefectureList_not_found() {
			List<KbnMstModel> kbnMstModelList = new ArrayList<KbnMstModel>();
			doReturn(kbnMstModelList).when(kbnMstRepositoryImpl).get("prefecture");
			assertEquals(kbnMstModelList, kbnMstServiceImpl.getPrefectureList());
		}
	}
}