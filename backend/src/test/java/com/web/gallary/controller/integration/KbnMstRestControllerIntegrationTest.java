package com.web.gallary.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.NoSuchElementException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class KbnMstRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/KbnMstRestControllerIntegrationTest.sql")
	class getPrefectures {
		@Test
		@Order(1)
		@DisplayName("正常系：都道府県一覧をグループ別に取得できる")
		void getPrefectures_success() throws Exception {
			mockMvc.perform(
					get("/api/v1/prefectures")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(3))
				// 北海道・東北グループ
				.andExpect(jsonPath("$[0].groupName").value("北海道・東北"))
				.andExpect(jsonPath("$[0].prefectures.length()").value(3))
				.andExpect(jsonPath("$[0].prefectures[0].kbnCode").value("Hokkaido"))
				.andExpect(jsonPath("$[0].prefectures[0].kbnJapaneseName").value("北海道"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnCode").value("Aomori"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnJapaneseName").value("青森"))
				.andExpect(jsonPath("$[0].prefectures[2].kbnCode").value("Iwate"))
				.andExpect(jsonPath("$[0].prefectures[2].kbnJapaneseName").value("岩手"))
				// 関東グループ
				.andExpect(jsonPath("$[1].groupName").value("関東"))
				.andExpect(jsonPath("$[1].prefectures.length()").value(2))
				.andExpect(jsonPath("$[1].prefectures[0].kbnCode").value("Tochigi"))
				.andExpect(jsonPath("$[1].prefectures[0].kbnJapaneseName").value("栃木"))
				.andExpect(jsonPath("$[1].prefectures[1].kbnCode").value("Gunma"))
				.andExpect(jsonPath("$[1].prefectures[1].kbnJapaneseName").value("群馬"))
				// 四国グループ
				.andExpect(jsonPath("$[2].groupName").value("四国"))
				.andExpect(jsonPath("$[2].prefectures.length()").value(2))
				.andExpect(jsonPath("$[2].prefectures[0].kbnCode").value("Tokushima"))
				.andExpect(jsonPath("$[2].prefectures[0].kbnJapaneseName").value("徳島"))
				.andExpect(jsonPath("$[2].prefectures[1].kbnCode").value("Kagawa"))
				.andExpect(jsonPath("$[2].prefectures[1].kbnJapaneseName").value("香川"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：1グループのみの場合")
		@Sql("/sql/common/cleanup.sql")
		@Sql(statements = {
			"insert into common.kbn_mst values('prefecture', 'Tokyo', 0, now(), 1, 'Kanto', '都道府県', '関東', '東京', 'prefecture', 'Kanto', 'Tokyo', '')",
			"insert into common.kbn_mst values('prefecture', 'Kanagawa', 0, now(), 2, 'Kanto', '都道府県', '関東', '神奈川', 'prefecture', 'Kanto', 'Kanagawa', '')"
		})
		void getPrefectures_single_group() throws Exception {
			mockMvc.perform(
					get("/api/v1/prefectures")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].groupName").value("関東"))
				.andExpect(jsonPath("$[0].prefectures.length()").value(2))
				.andExpect(jsonPath("$[0].prefectures[0].kbnCode").value("Tokyo"))
				.andExpect(jsonPath("$[0].prefectures[0].kbnJapaneseName").value("東京"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnCode").value("Kanagawa"))
				.andExpect(jsonPath("$[0].prefectures[1].kbnJapaneseName").value("神奈川"));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：都道府県が0件の場合はNoSuchElementExceptionが発生する")
		@Sql("/sql/common/cleanup.sql")
		void getPrefectures_empty() throws Exception {
			ServletException exception = assertThrows(ServletException.class, () ->
					mockMvc.perform(
							get("/api/v1/prefectures")
									.with(csrf())
					)
			);
			assertInstanceOf(NoSuchElementException.class, exception.getCause());
		}
	}
}
