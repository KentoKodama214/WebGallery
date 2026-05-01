package com.web.gallary.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.gallary.AccountPrincipal;
import com.web.gallary.entity.Account;
import com.web.gallary.entity.PhotoFavorite;
import com.web.gallary.entity.PhotoMst;
import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.ErrorEnum;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PhotoRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/integration/PhotoRestControllerIntegrationTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータがある場合")
		void getPhotoList_with_null_parameter() throws JsonProcessingException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(false))
				.andReturn();
			
			String jsonResponse = result.getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
			assertEquals(5, photoList.size());
			
			assertEquals(1, photoList.get(0).get("accountNo").asInt());
			assertEquals(9, photoList.get(0).get("photoNo").asInt());
			assertFalse(photoList.get(0).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC19.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption19", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(1).get("accountNo").asInt());
			assertEquals(8, photoList.get(1).get("photoNo").asInt());
			assertFalse(photoList.get(1).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC18.jpg", photoList.get(1).get("imageFilePath").asText());
			assertEquals("caption18", photoList.get(1).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(1).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(2).get("accountNo").asInt());
			assertEquals(7, photoList.get(2).get("photoNo").asInt());
			assertFalse(photoList.get(2).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC17.jpg", photoList.get(2).get("imageFilePath").asText());
			assertEquals("caption17", photoList.get(2).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(2).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(3).get("accountNo").asInt());
			assertEquals(6, photoList.get(3).get("photoNo").asInt());
			assertFalse(photoList.get(3).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC16.jpg", photoList.get(3).get("imageFilePath").asText());
			assertEquals("caption16", photoList.get(3).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(3).get("directionKbn").asText()));
			
			assertEquals(1, photoList.get(4).get("accountNo").asInt());
			assertEquals(5, photoList.get(4).get("photoNo").asInt());
			assertFalse(photoList.get(4).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC15.jpg", photoList.get(4).get("imageFilePath").asText());
			assertEquals("caption15", photoList.get(4).get("caption").asText());
			assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault(photoList.get(4).get("directionKbn").asText()));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：タグに半角スペースが含まれている場合")
		void getPhotoList_with_halfspace_tag() throws JsonProcessingException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽 青空")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andReturn();
			
			String jsonResponse = result.getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
			assertEquals(1, photoList.size());
			
			assertEquals(1, photoList.get(0).get("accountNo").asInt());
			assertEquals(1, photoList.get(0).get("photoNo").asInt());
			assertFalse(photoList.get(0).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption11", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：タグに全角スペースが含まれている場合")
		void getPhotoList_with_fullspace_tag() throws JsonProcessingException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽　青空")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andReturn();
			
			String jsonResponse = result.getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
			assertEquals(1, photoList.size());
			
			assertEquals(1, photoList.get(0).get("accountNo").asInt());
			assertEquals(1, photoList.get(0).get("photoNo").asInt());
			assertFalse(photoList.get(0).get("isFavorite").asBoolean());
			assertEquals("https://www.xxx.com/aaaaaaaa/DSC11.jpg", photoList.get(0).get("imageFilePath").asText());
			assertEquals("caption11", photoList.get(0).get("caption").asText());
			assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault(photoList.get(0).get("directionKbn").asText()));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_not_found_photo() throws JsonProcessingException, Exception {
			String photoAccountId = "aaaaaaaa";

			MvcResult result = mockMvc.perform(
					get("/api/v1/accounts/" + photoAccountId + "/photos")
					.param("tagList", "太陽　海")
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andReturn();
			
			String jsonResponse = result.getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode photoList = objectMapper.readTree(jsonResponse).get("photoList");
			assertEquals(0, photoList.size());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class savePhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：新規登録。写真タグなし、撮影日時なし。Nullパラメータあり")
		void savePhoto_addPhoto_not_photoTag_and_photoAt() throws JsonProcessingException, Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=4", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(rs.getInt("account_no"))
						.photoNo(rs.getInt("photo_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
						.locationNo(rs.getInt("location_no"))
						.imageFilePath(rs.getString("image_file_path"))
						.photoJapaneseTitle(rs.getString("photo_japanese_title"))
						.photoEnglishTitle(rs.getString("photo_english_title"))
						.caption(rs.getString("caption"))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(rs.getInt("focal_length"))
						.fValue(rs.getBigDecimal("f_value"))
						.shutterSpeed(rs.getBigDecimal("shutter_speed"))
						.iso(rs.getInt("iso"))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(2, actualPhotoMst.getFirst().getAccountNo());
			assertEquals(4, actualPhotoMst.getFirst().getPhotoNo());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().plusHours(9));
			assertEquals(0, actualPhotoMst.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC111.jpg", actualPhotoMst.getFirst().getImageFilePath());
			assertEquals("", actualPhotoMst.getFirst().getPhotoJapaneseTitle());
			assertEquals("", actualPhotoMst.getFirst().getPhotoEnglishTitle());
			assertEquals("", actualPhotoMst.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(0, actualPhotoMst.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMst.getFirst().getShutterSpeed()));
			assertEquals(0, actualPhotoMst.getFirst().getIso());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=4", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(rs.getInt("account_no"))
								.photoNo(rs.getInt("photo_no"))
								.tagNo(rs.getInt("tag_no"))
								.createdBy(rs.getInt("created_by"))
								.createdAt(rs.getObject("created_at", OffsetDateTime.class))
								.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
								.tagEnglishName(rs.getObject("tag_english_name").toString())
								.build());
			assertEquals(0, actualPhotoTagMst.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：新規登録。写真タグあり、撮影日時あり。Nullパラメータなし")
		void savePhoto_addPhoto_with_photoTag_and_photoAt() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "caption111")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "title111")
					.param("photoJapaneseTitle", "タイトル111")
					.param("photoAt", LocalDateTime.of(2000, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "0.01")
					.param("iso", "100")
					.param("photoTagRegistRequestList[0].accountNo", "2")
					.param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRegistRequestList[0].tagEnglishName", "sun")
					.param("photoTagRegistRequestList[1].accountNo", "2")
					.param("photoTagRegistRequestList[1].tagJapaneseName", "青空")
					.param("photoTagRegistRequestList[1].tagEnglishName", "bluesky")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=4", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(rs.getInt("account_no"))
						.photoNo(rs.getInt("photo_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
						.locationNo(rs.getInt("location_no"))
						.imageFilePath(rs.getString("image_file_path"))
						.photoJapaneseTitle(rs.getString("photo_japanese_title"))
						.photoEnglishTitle(rs.getString("photo_english_title"))
						.caption(rs.getString("caption"))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(rs.getInt("focal_length"))
						.fValue(rs.getBigDecimal("f_value"))
						.shutterSpeed(rs.getBigDecimal("shutter_speed"))
						.iso(rs.getInt("iso"))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(2, actualPhotoMst.getFirst().getAccountNo());
			assertEquals(4, actualPhotoMst.getFirst().getPhotoNo());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().plusHours(9));
			assertEquals(0, actualPhotoMst.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC111.jpg", actualPhotoMst.getFirst().getImageFilePath());
			assertEquals("タイトル111", actualPhotoMst.getFirst().getPhotoJapaneseTitle());
			assertEquals("title111", actualPhotoMst.getFirst().getPhotoEnglishTitle());
			assertEquals("caption111", actualPhotoMst.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(24, actualPhotoMst.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualPhotoMst.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualPhotoMst.getFirst().getShutterSpeed()));
			assertEquals(100, actualPhotoMst.getFirst().getIso());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=4", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(rs.getInt("account_no"))
								.photoNo(rs.getInt("photo_no"))
								.tagNo(rs.getInt("tag_no"))
								.createdBy(rs.getInt("created_by"))
								.createdAt(rs.getObject("created_at", OffsetDateTime.class))
								.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
								.tagEnglishName(rs.getObject("tag_english_name").toString())
								.build());
			assertEquals(2, actualPhotoTagMst.size());
			
			assertEquals(2, actualPhotoTagMst.get(0).getAccountNo());
			assertEquals(4, actualPhotoTagMst.get(0).getPhotoNo());
			assertEquals(1, actualPhotoTagMst.get(0).getTagNo());
			assertEquals("太陽", actualPhotoTagMst.get(0).getTagJapaneseName());
			assertEquals("sun", actualPhotoTagMst.get(0).getTagEnglishName());
			assertEquals(2, actualPhotoTagMst.get(1).getAccountNo());
			assertEquals(4, actualPhotoTagMst.get(1).getPhotoNo());
			assertEquals(2, actualPhotoTagMst.get(1).getTagNo());
			assertEquals("青空", actualPhotoTagMst.get(1).getTagJapaneseName());
			assertEquals("bluesky", actualPhotoTagMst.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：更新。写真タグあり、撮影日時あり。Nullパラメータなし")
		void savePhoto_updatePhoto_with_photoTag_and_photoAt() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("photoNo", "1")
					.param("caption", "caption111")
					.param("imageFilePath", "https://www.xxx.com/bbbbbbbb/DSC21.jpg")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "title111")
					.param("photoJapaneseTitle", "タイトル111")
					.param("photoAt", LocalDateTime.of(2000, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.param("focalLength", "24")
					.param("fValue", "8.0")
					.param("shutterSpeed", "0.01")
					.param("iso", "100")
					.param("photoTagRegistRequestList[0].accountNo", "2")
					.param("photoTagRegistRequestList[0].photoNo", "1")
					.param("photoTagRegistRequestList[0].tagJapaneseName", "太陽")
					.param("photoTagRegistRequestList[0].tagEnglishName", "sun")
					.param("photoTagRegistRequestList[1].accountNo", "2")
					.param("photoTagRegistRequestList[1].photoNo", "1")
					.param("photoTagRegistRequestList[1].tagJapaneseName", "青空")
					.param("photoTagRegistRequestList[1].tagEnglishName", "bluesky")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真登録が完了しました。"));
			
			// photo_mst登録チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 2 and photo_no=1", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(rs.getInt("account_no"))
						.photoNo(rs.getInt("photo_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
						.locationNo(rs.getInt("location_no"))
						.imageFilePath(rs.getString("image_file_path"))
						.photoJapaneseTitle(rs.getString("photo_japanese_title"))
						.photoEnglishTitle(rs.getString("photo_english_title"))
						.caption(rs.getString("caption"))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(rs.getInt("focal_length"))
						.fValue(rs.getBigDecimal("f_value"))
						.shutterSpeed(rs.getBigDecimal("shutter_speed"))
						.iso(rs.getInt("iso"))
						.build());
			
			assertEquals(1, actualPhotoMst.size());
			assertEquals(2, actualPhotoMst.getFirst().getAccountNo());
			assertEquals(1, actualPhotoMst.getFirst().getPhotoNo());
			assertFalse(actualPhotoMst.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualPhotoMst.getFirst().getPhotoAt().plusHours(9));
			assertEquals(0, actualPhotoMst.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/bbbbbbbb/DSC21.jpg", actualPhotoMst.getFirst().getImageFilePath());
			assertEquals("タイトル111", actualPhotoMst.getFirst().getPhotoJapaneseTitle());
			assertEquals("title111", actualPhotoMst.getFirst().getPhotoEnglishTitle());
			assertEquals("caption111", actualPhotoMst.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualPhotoMst.getFirst().getDirectionKbn());
			assertEquals(24, actualPhotoMst.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualPhotoMst.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualPhotoMst.getFirst().getShutterSpeed()));
			assertEquals(100, actualPhotoMst.getFirst().getIso());
			
			// photo_tag_mst登録チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_tag_mst WHERE account_no=2 and photo_no=1", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(rs.getInt("account_no"))
								.photoNo(rs.getInt("photo_no"))
								.tagNo(rs.getInt("tag_no"))
								.createdBy(rs.getInt("created_by"))
								.createdAt(rs.getObject("created_at", OffsetDateTime.class))
								.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
								.tagEnglishName(rs.getObject("tag_english_name").toString())
								.build());
			assertEquals(2, actualPhotoTagMst.size());
			
			assertEquals(2, actualPhotoTagMst.get(0).getAccountNo());
			assertEquals(1, actualPhotoTagMst.get(0).getPhotoNo());
			assertEquals(1, actualPhotoTagMst.get(0).getTagNo());
			assertEquals("太陽", actualPhotoTagMst.get(0).getTagJapaneseName());
			assertEquals("sun", actualPhotoTagMst.get(0).getTagEnglishName());
			assertEquals(2, actualPhotoTagMst.get(1).getAccountNo());
			assertEquals(1, actualPhotoTagMst.get(1).getPhotoNo());
			assertEquals(2, actualPhotoTagMst.get(1).getTagNo());
			assertEquals("青空", actualPhotoTagMst.get(1).getTagJapaneseName());
			assertEquals("bluesky", actualPhotoTagMst.get(1).getTagEnglishName());
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：アクセス不正。ForbiddenAccountExceptionをthrowする")
		void savePhoto_ForbiddenAccountException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "1")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/aaaaaaaa/photo_list"));
		}
		
		@Test
		@Order(5)
		@DisplayName("異常系：登録上限に達している。PhotoNotAdditableExceptionをthrowする")
		void savePhoto_PhotoNotAdditableException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "1")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.REACHED_REGISTRATION_LIMIT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/aaaaaaaa/photo_list"));
		}
		
		@Test
		@Order(6)
		@DisplayName("異常系：画像ファイル、ファイルパスともにnull。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_null() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(7)
		@DisplayName("異常系：画像ファイルがnull、ファイルパスがblank。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_file_and_filepath_is_blank() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.MINI)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(8)
		@DisplayName("異常系：画像ファイル、ファイルパス以外のパラメータ不正。BadRequestExceptionをthrowする")
		void savePhoto_BadRequestException_others() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC111.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "")
					.param("imageFilePath", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(9)
		@DisplayName("異常系：FileDuplicateExceptionをthrowする")
		void savePhoto_FileDuplicateException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			MockMultipartFile multipartFile = new MockMultipartFile(
					"imageFile",
					"DSC21.jpg",
					MediaType.MULTIPART_FORM_DATA_VALUE,
					"sample image".getBytes());
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("caption", "")
					.param("imageFilePath", "")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.DUPLICATE_PHOTO_FILE.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/bbbbbbbb/photo_list"));
		}
		
		@Test
		@Order(10)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void savePhoto_UpdateFailureException() throws Exception {
			String photoAccountId = "bbbbbbbb";
			
			Account sessionAccount = Account.builder()
					.accountNo(2)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					multipart("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.param("accountNo", "2")
					.param("photoNo", "99")
					.param("caption", "caption21")
					.param("imageFilePath", "https://www.xxx.com/DSC99.jpg")
					.param("directionKbn", "VERTICAL")
					.param("photoEnglishTitle", "")
					.param("photoJapaneseTitle", "")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_PHOTO.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/bbbbbbbb/photo_list"));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/PhotoRestControllerIntegrationTest.sql")
	class deletePhoto {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deletePhoto_success() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_success.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("写真削除が完了しました。"));
			
			// photo_mst削除チェック
			List<PhotoMst> actualPhotoMst = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst where account_no = 1 and photo_no=1", (rs, rowNum) ->
					PhotoMst.builder()
						.accountNo(rs.getInt("account_no"))
						.photoNo(rs.getInt("photo_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
						.locationNo(rs.getInt("location_no"))
						.imageFilePath(rs.getString("image_file_path"))
						.photoJapaneseTitle(rs.getString("photo_japanese_title"))
						.photoEnglishTitle(rs.getString("photo_english_title"))
						.caption(rs.getString("caption"))
						.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
						.focalLength(rs.getInt("focal_length"))
						.fValue(rs.getBigDecimal("f_value"))
						.shutterSpeed(rs.getBigDecimal("shutter_speed"))
						.iso(rs.getInt("iso"))
						.build());
			assertTrue(actualPhotoMst.getFirst().getIsDeleted());
			
			// photo_tag_mst削除チェック
			List<PhotoTagMst> actualPhotoTagMst = jdbcTemplate.query(
						"SELECT * FROM photo.photo_tag_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
							PhotoTagMst.builder()
								.accountNo(rs.getInt("account_no"))
								.photoNo(rs.getInt("photo_no"))
								.tagNo(rs.getInt("tag_no"))
								.createdBy(rs.getInt("created_by"))
								.createdAt(rs.getObject("created_at", OffsetDateTime.class))
								.tagJapaneseName(rs.getObject("tag_japanese_name").toString())
								.tagEnglishName(rs.getObject("tag_english_name").toString())
								.build());
			assertEquals(0, actualPhotoTagMst.size());
			
			// photo_favorite削除チェック
			List<PhotoFavorite> actualPhotoFavoriteData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_favorite WHERE favorite_photo_account_no=1 and favorite_photo_no=1", (rs, rowNum) ->
						PhotoFavorite.builder()
							.accountNo(rs.getInt("account_no"))
							.favoritePhotoAccountNo(rs.getInt("favorite_photo_account_no"))
							.favoritePhotoNo(rs.getInt("favorite_photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.build());
			assertEquals(0, actualPhotoFavoriteData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：不正アクセス。ForbiddenAccountExceptionをthrowする")
		void deletePhoto_ForbiddenAccountException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "eeeeeeee";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("EEEEEEEE")
					.password("$2a$10$password5")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_forbidden.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_PHOTO.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + loginAccountId + "/photo_list"));
			
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：パラメータ不正。BadRequestExceptionをthrowする")
		void deletePhoto_BadRequestException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_badrequest.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
		
		@Test
		@Order(4)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deletePhoto_UpdateFailureException() throws Exception {
			String photoAccountId = "aaaaaaaa";
			String loginAccountId = "aaaaaaaa";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(loginAccountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					delete("/api/v1/accounts/" + photoAccountId + "/photos")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_photo_update_failure.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_DELETE_PHOTO.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_DELETE_PHOTO.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + loginAccountId + "/photo_list"));
		}
	}
}