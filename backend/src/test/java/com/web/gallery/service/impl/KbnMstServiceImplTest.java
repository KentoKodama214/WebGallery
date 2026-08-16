package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.web.gallery.model.KbnMstModel;
import com.web.gallery.model.KbnMstModelList;
import com.web.gallery.repository.impl.KbnMstRepositoryImpl;

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
			KbnMstModelList kbnMstModelList = KbnMstModelList.of(List.of(
					KbnMstModel.builder()
							.kbnClassCode(new KbnClassCode("prefecture"))
							.kbnCode(new KbnCode("Hokkaido"))
							.sortOrder(new SortOrder(1))
							.kbnGroupCode(new KbnGroupCode("Hokkaido_Tohoku"))
							.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
							.kbnGroupJapaneseName(new KbnGroupJapaneseName("北海道・東北"))
							.kbnJapaneseName(new KbnJapaneseName("北海道"))
							.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
							.kbnGroupEnglishName(new KbnGroupEnglishName("Hokkaido_Tohoku"))
							.kbnEnglishName(new KbnEnglishName("Hokkaido"))
							.explanation(new Explanation("北海道はでっかいどう"))
							.build(),
					KbnMstModel.builder()
							.kbnClassCode(new KbnClassCode("prefecture"))
							.kbnCode(new KbnCode("Okinawa"))
							.sortOrder(new SortOrder(47))
							.kbnGroupCode(new KbnGroupCode("Kyushu_Okinawa"))
							.kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
							.kbnGroupJapaneseName(new KbnGroupJapaneseName("九州・沖縄"))
							.kbnJapaneseName(new KbnJapaneseName("沖縄"))
							.kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
							.kbnGroupEnglishName(new KbnGroupEnglishName("Kyushu_Okinawa"))
							.kbnEnglishName(new KbnEnglishName("Okinawa"))
							.explanation(new Explanation("沖縄は南国"))
							.build()));
			doReturn(kbnMstModelList).when(kbnMstRepositoryImpl).get("prefecture");
			assertEquals(kbnMstModelList, kbnMstServiceImpl.getPrefectureList());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：区分マスタが存在しない場合")
		void getPrefectureList_not_found() {
			KbnMstModelList kbnMstModelList = KbnMstModelList.empty();
			doReturn(kbnMstModelList).when(kbnMstRepositoryImpl).get("prefecture");
			assertEquals(kbnMstModelList, kbnMstServiceImpl.getPrefectureList());
		}
	}
}