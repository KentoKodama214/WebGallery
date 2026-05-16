package com.web.gallary.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.web.gallary.helper.KbnHelper;
import com.web.gallary.model.KbnMstModel;
import com.web.gallary.service.KbnMstService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class KbnMstRestControllerTest {
	@InjectMocks
	private KbnMstRestController kbnMstRestController;

	@Mock
	private KbnMstService kbnMstService;

	@Mock
	private KbnHelper kbnHelper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(kbnMstRestController).build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPrefectures {
		@Test
		@Order(1)
		@DisplayName("正常系：都道府県一覧を取得できること")
		void getPrefectures_success() throws Exception {
			List<KbnMstModel> prefectureList = new ArrayList<>();
			KbnMstModel hokkaido = KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Hokkaido")
					.sortOrder(1)
					.kbnGroupCode("hokkaido_tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北地方")
					.kbnJapaneseName("北海道")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("hokkaido_tohoku")
					.kbnEnglishName("Hokkaido")
					.explanation("")
					.build();
			KbnMstModel aomori = KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Aomori")
					.sortOrder(2)
					.kbnGroupCode("hokkaido_tohoku")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("北海道・東北地方")
					.kbnJapaneseName("青森県")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("hokkaido_tohoku")
					.kbnEnglishName("Aomori")
					.explanation("")
					.build();
			KbnMstModel tokyo = KbnMstModel.builder()
					.kbnClassCode("prefecture")
					.kbnCode("Tokyo")
					.sortOrder(13)
					.kbnGroupCode("kanto")
					.kbnClassJapaneseName("都道府県")
					.kbnGroupJapaneseName("関東地方")
					.kbnJapaneseName("東京都")
					.kbnClassEnglishName("prefecture")
					.kbnGroupEnglishName("kanto")
					.kbnEnglishName("Tokyo")
					.explanation("")
					.build();

			prefectureList.add(hokkaido);
			prefectureList.add(aomori);
			prefectureList.add(tokyo);

			Map<String, List<KbnMstModel>> groupedMap = new LinkedHashMap<>();
			groupedMap.put("北海道・東北地方", List.of(hokkaido, aomori));
			groupedMap.put("関東地方", List.of(tokyo));

			doReturn(prefectureList).when(kbnMstService).getPrefectureList();
			doReturn(groupedMap).when(kbnHelper).convertToLinkedHashMap(prefectureList);

			mockMvc.perform(get("/api/v1/prefectures"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].groupName").value("北海道・東北地方"))
				.andExpect(jsonPath("$[0].prefectures[0].kbnCode").value("Hokkaido"))
				.andExpect(jsonPath("$[0].prefectures[0].kbnJapaneseName").value("北海道"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnCode").value("Aomori"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnJapaneseName").value("青森県"))
				.andExpect(jsonPath("$[1].groupName").value("関東地方"))
				.andExpect(jsonPath("$[1].prefectures[0].kbnCode").value("Tokyo"))
				.andExpect(jsonPath("$[1].prefectures[0].kbnJapaneseName").value("東京都"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：空のリストの場合は空配列を返すこと")
		void getPrefectures_empty() throws Exception {
			List<KbnMstModel> emptyList = new ArrayList<>();
			Map<String, List<KbnMstModel>> emptyMap = new LinkedHashMap<>();

			doReturn(emptyList).when(kbnMstService).getPrefectureList();
			doReturn(emptyMap).when(kbnHelper).convertToLinkedHashMap(emptyList);

			mockMvc.perform(get("/api/v1/prefectures"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
		}
	}
}
