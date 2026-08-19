package com.web.gallery.repository.impl;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.entity.KbnMst;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.domain.common.SortOrder;
import com.web.gallery.domain.common.KbnGroupCode;
import com.web.gallery.domain.common.KbnClassJapaneseName;
import com.web.gallery.domain.common.KbnGroupJapaneseName;
import com.web.gallery.domain.common.KbnJapaneseName;
import com.web.gallery.domain.common.KbnClassEnglishName;
import com.web.gallery.domain.common.KbnGroupEnglishName;
import com.web.gallery.domain.common.KbnEnglishName;
import com.web.gallery.domain.common.Explanation;
import com.web.gallery.mapper.KbnMstMapper;
import com.web.gallery.model.KbnMstModelList;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class KbnMstRepositoryImplTest {
	@InjectMocks
	private KbnMstRepositoryImpl kbnMstRepositoryImpl;
	
	@Mock
	private KbnMstMapper kbnMstMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class get {
		@Test
		@Order(1)
		@DisplayName("正常系：区分マスタが取得できた場合")
		void get_found() {
			String kbnClassCode = "prefecture";
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			expected.add(KbnMst.builder()
					.kbnClassCode(new KbnClassCode(kbnClassCode))
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
					.build());
			expected.add(KbnMst.builder()
					.kbnClassCode(new KbnClassCode(kbnClassCode))
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
					.build());
			
			ArgumentCaptor<KbnMst> kbnMstCaptor = ArgumentCaptor.forClass(KbnMst.class);
			doReturn(expected).when(kbnMstMapper).select(kbnMstCaptor.capture());
			
			KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode(kbnClassCode));
			
			KbnMst kbnMstCapture = kbnMstCaptor.getValue();
			assertEquals(new KbnClassCode(kbnClassCode), kbnMstCapture.getKbnClassCode());
			
			assertEquals(expected.size(), actual.size());
			assertEquals(expected.get(0).getKbnClassCode(), actual.get(0).getKbnClassCode());
			assertEquals(expected.get(0).getKbnCode(), actual.get(0).getKbnCode());
			assertEquals(expected.get(0).getSortOrder(), actual.get(0).getSortOrder());
			assertEquals(expected.get(0).getKbnGroupCode(), actual.get(0).getKbnGroupCode());
			assertEquals(expected.get(0).getKbnClassJapaneseName(), actual.get(0).getKbnClassJapaneseName());
			assertEquals(expected.get(0).getKbnGroupJapaneseName(), actual.get(0).getKbnGroupJapaneseName());
			assertEquals(expected.get(0).getKbnJapaneseName(), actual.get(0).getKbnJapaneseName());
			assertEquals(expected.get(0).getKbnClassEnglishName(), actual.get(0).getKbnClassEnglishName());
			assertEquals(expected.get(0).getKbnGroupEnglishName(), actual.get(0).getKbnGroupEnglishName());
			assertEquals(expected.get(0).getKbnEnglishName(), actual.get(0).getKbnEnglishName());
			assertEquals(expected.get(0).getExplanation(), actual.get(0).getExplanation());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：区分マスタが取得できなかった場合")
		void get_not_found() {
			String kbnClassCode = "prefecture";
			
			List<KbnMst> expected = new ArrayList<KbnMst>();
			ArgumentCaptor<KbnMst> kbnMstCaptor = ArgumentCaptor.forClass(KbnMst.class);
			doReturn(expected).when(kbnMstMapper).select(kbnMstCaptor.capture());
			
			KbnMstModelList actual = kbnMstRepositoryImpl.get(new KbnClassCode(kbnClassCode));

			KbnMst kbnMstCapture = kbnMstCaptor.getValue();
			assertEquals(new KbnClassCode(kbnClassCode), kbnMstCapture.getKbnClassCode());
			assertEquals(0, actual.size());
		}
	}
}